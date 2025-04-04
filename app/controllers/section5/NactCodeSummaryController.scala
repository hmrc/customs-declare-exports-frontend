/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.section5

import controllers.actions.{AuthAction, JourneyAction}
import controllers.section5.routes.NactCodeAddController
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.helpers.ItemHelper.nextPageAfterNactCodePages
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.DeclarationType._
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section5.nactCode.nact_codes

import javax.inject.Inject

class NactCodeSummaryController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  nactCodesPage: nact_codes
) extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType(nonClearanceJourneys)) { implicit request =>
    request.cacheModel.itemBy(itemId).flatMap(_.nactCodes) match {
      case Some(nactCodes) if nactCodes.nonEmpty => Ok(nactCodesPage(itemId, anotherYesNoForm.withSubmissionErrors, nactCodes))
      case _                                     => navigator.continueTo(NactCodeAddController.displayPage(itemId))
    }
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType(nonClearanceJourneys)) { implicit request =>
    val nactCodes = request.cacheModel.itemBy(itemId).flatMap(_.nactCodes).getOrElse(List.empty)
    anotherYesNoForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[YesNoAnswer]) => BadRequest(nactCodesPage(itemId, formWithErrors, nactCodes)),
        validYesNo =>
          validYesNo.answer match {
            case YesNoAnswers.yes => navigator.continueTo(NactCodeAddController.displayPage(itemId))
            case YesNoAnswers.no  => navigator.continueTo(nextPageAfterNactCodePages(itemId))
          }
      )
  }

  private def anotherYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.nationalAdditionalCode.add.answer.empty")
}
