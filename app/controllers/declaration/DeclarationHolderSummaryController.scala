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

package controllers.declaration

import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.routes.{DeclarationHolderAddController, DestinationCountryController}
import controllers.helpers.DeclarationHolderHelper.declarationHolders
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declarationHolder.declaration_holder_summary

import javax.inject.Inject

class DeclarationHolderSummaryController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  declarationHolderPage: declaration_holder_summary
) extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (declarationHolders.isEmpty) navigator.continueTo(DeclarationHolderAddController.displayPage)
    else Ok(declarationHolderPage(yesNoForm.withSubmissionErrors, declarationHolders))
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    yesNoForm
      .bindFromRequest()
      .fold(
        formWithErrors => BadRequest(declarationHolderPage(formWithErrors, declarationHolders)),
        _.answer match {
          case YesNoAnswers.yes => navigator.continueTo(DeclarationHolderAddController.displayPage)
          case YesNoAnswers.no  => navigator.continueTo(DestinationCountryController.displayPage)
        }
      )
  }

  private def yesNoForm: Form[YesNoAnswer] =
    YesNoAnswer.form(errorKey = "declaration.declarationHolders.add.another.empty")
}
