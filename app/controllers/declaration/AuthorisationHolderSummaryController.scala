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
import controllers.declaration.routes.{AuthorisationHolderAddController, SectionSummaryController}
import controllers.helpers.AuthorisationHolderHelper.authorisationHolders
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.authorisationHolder.authorisation_holder_summary

import javax.inject.Inject

class AuthorisationHolderSummaryController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  authorisationHolderPage: authorisation_holder_summary
) extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (authorisationHolders.isEmpty) navigator.continueTo(AuthorisationHolderAddController.displayPage)
    else Ok(authorisationHolderPage(yesNoForm.withSubmissionErrors, authorisationHolders))
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    yesNoForm
      .bindFromRequest()
      .fold(
        formWithErrors => BadRequest(authorisationHolderPage(formWithErrors, authorisationHolders)),
        _.answer match {
          case YesNoAnswers.yes => navigator.continueTo(AuthorisationHolderAddController.displayPage)
          case YesNoAnswers.no  => navigator.continueTo(SectionSummaryController.displayPage(2))
        }
      )
  }

  private def yesNoForm: Form[YesNoAnswer] =
    YesNoAnswer.form(errorKey = "declaration.authorisationHolder.add.another.empty")
}
