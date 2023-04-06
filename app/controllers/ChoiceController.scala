/*
 * Copyright 2023 HM Revenue & Customs
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

import config.featureFlags.SecureMessagingInboxConfig
import config.{AppConfig, ExternalServicesConfig}
import controllers.actions.{AuthAction, VerifiedEmailAction}
import forms.Choice
import forms.Choice.AllowedChoiceValues._
import forms.Choice._
import models.requests.SessionHelper.{declarationUuid, errorFixModeSessionKey}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.dashboard.DashboardHelper.toDashboard
import views.html.choice_page

import javax.inject.Inject

class ChoiceController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  mcc: MessagesControllerComponents,
  secureMessagingInboxConfig: SecureMessagingInboxConfig,
  choicePage: choice_page,
  appConfig: AppConfig,
  externalServicesConfig: ExternalServicesConfig
) extends FrontendController(mcc) with I18nSupport with WithUnsafeDefaultFormBinding {

  lazy val availableJourneys =
    if (secureMessagingInboxConfig.isExportsSecureMessagingEnabled)
      appConfig.availableJourneys()
    else
      appConfig.availableJourneys().filterNot(_.equals(Inbox))

  def displayPage(previousChoice: Option[Choice]): Action[AnyContent] = (authenticate andThen verifyEmail) { implicit request =>
    def pageForPreviousChoice(previousChoice: Option[Choice]): HtmlFormat.Appendable = {
      val form = Choice.form
      choicePage(previousChoice.fold(form)(form.fill), availableJourneys)
    }

    Ok(pageForPreviousChoice(previousChoice)).removingFromSession(declarationUuid, errorFixModeSessionKey)
  }

  def submitChoice(): Action[AnyContent] = (authenticate andThen verifyEmail) { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[Choice]) => BadRequest(choicePage(formWithErrors, availableJourneys)),
        choice =>
          (choice.value match {
            case CreateDec   => Redirect(declaration.routes.DeclarationChoiceController.displayPage)
            case Movements   => Redirect(Call("GET", externalServicesConfig.customsMovementsFrontendUrl))
            case ContinueDec => Redirect(routes.SavedDeclarationsController.displayDeclarations())
            case CancelDec   => Redirect(routes.CancelDeclarationController.displayPage)
            case Dashboard   => Redirect(toDashboard)
            case Inbox       => Redirect(routes.SecureMessagingController.displayInbox)
          }).removingFromSession(declarationUuid, errorFixModeSessionKey)
      )
  }
}
