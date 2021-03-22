/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import config.{AppConfig, SfusConfig}
import controllers.actions.{AuthAction, VerifiedEmailAction}
import forms.Choice
import forms.Choice.AllowedChoiceValues._
import forms.Choice._
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.choice_page

class ChoiceController @Inject()(
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  mcc: MessagesControllerComponents,
  choicePage: choice_page,
  appConfig: AppConfig,
  sfusConfig: SfusConfig
) extends FrontendController(mcc) with I18nSupport {

  private lazy val availableJourneys = appConfig.availableJourneys()

  def displayPage(previousChoice: Option[Choice]): Action[AnyContent] = (authenticate andThen verifyEmail) { implicit request =>
    def pageForPreviousChoice(previousChoice: Option[Choice]) = {
      val form = Choice.form()
      choicePage(previousChoice.fold(form)(form.fill), availableJourneys, sfusConfig)
    }
    Ok(pageForPreviousChoice(previousChoice))
  }

  def submitChoice(): Action[AnyContent] = (authenticate andThen verifyEmail) { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[Choice]) => BadRequest(choicePage(formWithErrors, availableJourneys, sfusConfig)),
        choice =>
          choice.value match {
            case CreateDec =>
              Redirect(controllers.declaration.routes.DeclarationChoiceController.displayPage())
            case CancelDec =>
              Redirect(controllers.routes.CancelDeclarationController.displayPage())
            case ContinueDec =>
              Redirect(controllers.routes.SavedDeclarationsController.displayDeclarations())
            case Submissions =>
              Redirect(controllers.routes.SubmissionsController.displayListOfSubmissions())
        }
      )
  }
}
