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
import controllers.declaration.routes.{AdditionalDocumentAddController, AdditionalDocumentsRequiredController, ItemsSummaryController}
import controllers.helpers.ErrorFixModeHelper.inErrorFixMode
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.additionaldocuments.AdditionalDocument
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.TaggedAuthCodes
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.additionalDocuments.additional_documents

import javax.inject.Inject

class AdditionalDocumentsController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  taggedAuthCodes: TaggedAuthCodes,
  additionalDocumentsPage: additional_documents
) extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val additionalDocuments = cachedAdditionalDocuments(itemId)
    if (additionalDocuments.nonEmpty) {
      val form = yesNoForm.withSubmissionErrors
      Ok(additionalDocumentsPage(itemId, form, additionalDocuments))
    } else navigator.continueTo(redirectIfNoDocuments(itemId))
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    def showFormWithErrors(formWithErrors: Form[YesNoAnswer]): Result =
      BadRequest(additionalDocumentsPage(itemId, formWithErrors, cachedAdditionalDocuments(itemId)))

    yesNoForm
      .bindFromRequest()
      .fold(showFormWithErrors, yesNoAnswer => nextPage(yesNoAnswer, itemId))
  }

  private def yesNoForm: Form[YesNoAnswer] =
    YesNoAnswer.form(errorKey = "declaration.additionalDocument.summary.add.another.empty")

  private def cachedAdditionalDocuments(itemId: String)(implicit request: JourneyRequest[_]): Seq[AdditionalDocument] =
    request.cacheModel.listOfAdditionalDocuments(itemId)

  private def nextPage(yesNoAnswer: YesNoAnswer, itemId: String)(implicit request: JourneyRequest[AnyContent]): Result =
    yesNoAnswer.answer match {
      case YesNoAnswers.yes => navigator.continueTo(AdditionalDocumentAddController.displayPage(itemId))(request)
      case _                => navigator.continueTo(ItemsSummaryController.displayItemsSummaryPage)(request)
    }

  private def redirectIfNoDocuments(itemId: String)(implicit request: JourneyRequest[_]): Call =
    if (
      inErrorFixMode
      || taggedAuthCodes.hasAuthCodeRequiringAdditionalDocs(request.cacheModel)
      || request.cacheModel.isLicenseRequired(itemId)
    ) AdditionalDocumentAddController.displayPage(itemId)
    else AdditionalDocumentsRequiredController.displayPage(itemId)
}
