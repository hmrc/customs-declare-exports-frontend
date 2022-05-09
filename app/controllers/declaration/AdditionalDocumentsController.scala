/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.additionaldocuments.AdditionalDocument
import models.Mode
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.additionalDocuments.additional_documents

import javax.inject.Inject

class AdditionalDocumentsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalDocumentsPage: additional_documents
) extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val additionalDocuments = cachedAdditionalDocuments(itemId)
    if (additionalDocuments.nonEmpty) {
      val frm = yesNoForm.withSubmissionErrors()
      Ok(additionalDocumentsPage(mode, itemId, frm, additionalDocuments))
    } else navigator.continueTo(mode, redirectIfNoDocuments(mode, itemId), mode.isErrorFix)
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    def showFormWithErrors(formWithErrors: Form[YesNoAnswer]): Result =
      BadRequest(additionalDocumentsPage(mode, itemId, formWithErrors, cachedAdditionalDocuments(itemId)))

    yesNoForm
      .bindFromRequest()
      .fold(showFormWithErrors, yesNoAnswer => nextPage(mode, yesNoAnswer, itemId))
  }

  private def yesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.additionalDocument.summary.add.another.empty")

  private def cachedAdditionalDocuments(itemId: String)(implicit request: JourneyRequest[_]): Seq[AdditionalDocument] =
    request.cacheModel.listOfAdditionalDocuments(itemId)

  private def nextPage(mode: Mode, yesNoAnswer: YesNoAnswer, itemId: String)(implicit request: JourneyRequest[AnyContent]): Result =
    yesNoAnswer.answer match {
      case YesNoAnswers.yes =>
        navigator.continueTo(mode, routes.AdditionalDocumentAddController.displayPage(_, itemId), mode.isErrorFix)(request, hc)
      case _ =>
        navigator.continueTo(mode, routes.ItemsSummaryController.displayItemsSummaryPage)(request, hc)
    }

  private def redirectIfNoDocuments(mode: Mode, itemId: String)(implicit request: JourneyRequest[_]): Mode => Call =
    if (mode.isErrorFix || request.cacheModel.hasAuthCodeRequiringAdditionalDocs || request.cacheModel.isLicenseRequired(itemId))
      routes.AdditionalDocumentAddController.displayPage(_, itemId)
    else routes.AdditionalDocumentsRequiredController.displayPage(_, itemId)
}
