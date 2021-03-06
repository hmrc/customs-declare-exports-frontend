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

package controllers.declaration

import scala.concurrent.{ExecutionContext, Future}

import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import controllers.util.MultipleItemsHelper.remove
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.additionaldocuments.AdditionalDocument
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.ListItem
import views.html.declaration.additionalDocuments.additional_document_remove

class AdditionalDocumentRemoveController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalDocumentRemovePage: additional_document_remove
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String, documentId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    findAdditionalDocument(itemId, documentId) match {
      case Some(document) => Ok(additionalDocumentRemovePage(mode, itemId, documentId, document, removeYesNoForm.withSubmissionErrors()))
      case _              => returnToSummary(mode, itemId)
    }
  }

  def submitForm(mode: Mode, itemId: String, documentId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    findAdditionalDocument(itemId, documentId) match {
      case Some(document) =>
        removeYesNoForm
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(additionalDocumentRemovePage(mode, itemId, documentId, document, formWithErrors))),
            _.answer match {
              case YesNoAnswers.yes => removeAdditionalDocument(itemId, document).map(_ => returnToSummary(mode, itemId))
              case YesNoAnswers.no  => Future.successful(returnToSummary(mode, itemId))
            }
          )
      case _ => Future.successful(returnToSummary(mode, itemId))
    }

  }

  private def removeYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.additionalDocument.remove.empty")

  private def returnToSummary(mode: Mode, itemId: String)(implicit request: JourneyRequest[AnyContent]): Result =
    navigator.continueTo(mode, routes.AdditionalDocumentsController.displayPage(_, itemId))

  private def findAdditionalDocument(itemId: String, id: String)(implicit request: JourneyRequest[AnyContent]): Option[AdditionalDocument] =
    ListItem.findById(id, request.cacheModel.listOfAdditionalDocuments(itemId))

  private def removeAdditionalDocument(itemId: String, itemToRemove: AdditionalDocument)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] = {
    val additionalDocuments = request.cacheModel.additionalDocuments(itemId)
    val documents = remove(additionalDocuments.documents, itemToRemove.equals(_: AdditionalDocument))
    val updatedDocumentsData =
      additionalDocuments.copy(isRequired = if (documents.nonEmpty) additionalDocuments.isRequired else None, documents = documents)
    updateExportsDeclarationSyncDirect(_.updatedItem(itemId, _.copy(additionalDocuments = Some(updatedDocumentsData))))
  }
}
