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
import controllers.helpers._
import forms.declaration.additionaldocuments.AdditionalDocument
import forms.declaration.additionaldocuments.AdditionalDocument._
import models.declaration.AdditionalDocuments
import models.declaration.AdditionalDocuments.maxNumberOfItems
import models.requests.JourneyRequest
import models.ExportsDeclaration
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.ListItem
import views.html.declaration.additionalDocuments.additional_document_change

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalDocumentChangeController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalDocumentChangePage: additional_document_change
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage(itemId: String, documentId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    findAdditionalDocument(itemId, documentId) match {
      case Some(document) =>
        val changeForm = form(request.cacheModel).fill(document).withSubmissionErrors()
        Ok(additionalDocumentChangePage(itemId, documentId, changeForm))

      case _ => returnToSummary(itemId)
    }
  }

  def submitForm(itemId: String, documentId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    findAdditionalDocument(itemId, documentId) match {
      case Some(existingDocument) =>
        val boundForm = globalErrors(form(request.cacheModel).bindFromRequest())
        boundForm.fold(
          formWithErrors => Future.successful(BadRequest(additionalDocumentChangePage(itemId, documentId, formWithErrors))),
          updatedDocument =>
            if (updatedDocument.isDefined)
              changeDocument(itemId, documentId, existingDocument, updatedDocument, boundForm)
            else
              Future.successful(returnToSummary(itemId))
        )
      case _ => Future.successful(returnToSummary(itemId))
    }
  }

  private def changeDocument(
    itemId: String,
    documentId: String,
    existingDocument: AdditionalDocument,
    newDocument: AdditionalDocument,
    boundForm: Form[AdditionalDocument]
  )(implicit request: JourneyRequest[AnyContent]): Future[Result] = {

    val additionalDocuments = request.cacheModel.additionalDocumentsInformation(itemId)
    val existingDocuments = additionalDocuments.documents
    val documentsWithoutExisting: Seq[AdditionalDocument] = existingDocuments.filterNot(_ == existingDocument)

    MultipleItemsHelper
      .add(boundForm, documentsWithoutExisting, maxNumberOfItems, AdditionalDocumentFormGroupId, "declaration.additionalDocument")
      .fold(
        formWithErrors => Future.successful(BadRequest(additionalDocumentChangePage(itemId, documentId, formWithErrors))),
        _ => {
          val updatedDocuments = existingDocuments.map(doc => if (doc == existingDocument) newDocument else doc)
          updateCache(itemId, additionalDocuments.copy(documents = updatedDocuments))
            .map(_ => returnToSummary(itemId))
        }
      )
  }

  private def findAdditionalDocument(itemId: String, id: String)(implicit request: JourneyRequest[AnyContent]): Option[AdditionalDocument] =
    ListItem.findById(id, request.cacheModel.listOfAdditionalDocuments(itemId))

  private def returnToSummary(itemId: String)(implicit request: JourneyRequest[AnyContent]): Result =
    navigator.continueTo(routes.AdditionalDocumentsController.displayPage(itemId))

  private def updateCache(itemId: String, additionalDocuments: AdditionalDocuments)(
    implicit req: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model => model.updatedItem(itemId, item => item.copy(additionalDocuments = Some(additionalDocuments))))
}
