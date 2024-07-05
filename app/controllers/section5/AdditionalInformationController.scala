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

package controllers.section5

import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.section5.routes._
import forms.common
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section5.AdditionalInformationSummary
import models.DeclarationType
import models.declaration.AdditionalInformationData
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section5.additionalInformation.additional_information

import javax.inject.Inject

class AdditionalInformationController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalInformationPage: additional_information
) extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val form = yesNoForm.withSubmissionErrors
    cachedAdditionalInformationData(itemId) match {

      case Some(additionalInformationData) if additionalInformationData.items.nonEmpty =>
        Ok(additionalInformationPage(itemId, form, additionalInformationData.items, backLink(itemId)))

      case Some(_) => navigator.continueTo(AdditionalInformationAddController.displayPage(itemId))
      case _       => navigator.continueTo(AdditionalInformationRequiredController.displayPage(itemId))
    }
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    yesNoForm
      .bindFromRequest()
      .fold(formWithErrors => showFormWithErrors(itemId, formWithErrors), yesNoAnswer => nextPage(yesNoAnswer, itemId))
  }

  private def yesNoForm: Form[common.YesNoAnswer] =
    YesNoAnswer.form(errorKey = "declaration.additionalInformation.add.another.empty")

  private def cachedAdditionalInformationData(itemId: String)(implicit request: JourneyRequest[_]): Option[AdditionalInformationData] =
    request.cacheModel.itemBy(itemId).flatMap(_.additionalInformation)

  private def nextPage(yesNoAnswer: YesNoAnswer, itemId: String)(implicit request: JourneyRequest[AnyContent]): Result = {
    val isClearanceJourney = request.declarationType == DeclarationType.CLEARANCE

    yesNoAnswer.answer match {
      case YesNoAnswers.yes =>
        navigator.continueTo(AdditionalInformationAddController.displayPage(itemId))

      case YesNoAnswers.no if isClearanceJourney =>
        navigator.continueTo(AdditionalDocumentsController.displayPage(itemId))

      case _ => navigator.continueTo(IsLicenceRequiredController.displayPage(itemId))
    }
  }

  private def backLink(itemId: String)(implicit request: JourneyRequest[AnyContent]): Call =
    navigator.backLinkForAdditionalInformation(AdditionalInformationSummary, itemId)

  private def showFormWithErrors(itemId: String, formWithErrors: Form[YesNoAnswer])(implicit request: JourneyRequest[AnyContent]): Result = {
    val items = cachedAdditionalInformationData(itemId).map(_.items).getOrElse(Seq.empty)
    BadRequest(additionalInformationPage(itemId, formWithErrors, items, backLink(itemId)))
  }
}
