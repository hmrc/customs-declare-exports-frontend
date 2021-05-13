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

import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.DocumentsProducedAddController.DocumentsProducedFormGroupId
import controllers.navigation.Navigator
import controllers.util._
import forms.declaration.additionaldocuments.DocumentsProduced
import forms.declaration.additionaldocuments.DocumentsProduced.{form, globalErrors}
import models.declaration.DocumentsProducedData
import models.declaration.DocumentsProducedData.maxNumberOfItems
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.ListItem
import views.html.declaration.documentsProduced.documents_produced_change

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DocumentsProducedChangeController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  documentProducedPage: documents_produced_change
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String, documentId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    findDocument(itemId, documentId) match {
      case Some(document) => Ok(documentProducedPage(mode, itemId, documentId, form().fill(document).withSubmissionErrors()))
      case _              => returnToSummary(mode, itemId)
    }
  }

  def submitForm(mode: Mode, itemId: String, documentId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    findDocument(itemId, documentId) match {
      case Some(existingDocument) =>
        val boundForm = globalErrors(form().bindFromRequest())
        boundForm.fold(
          formWithErrors => {
            Future.successful(BadRequest(documentProducedPage(mode, itemId, documentId, formWithErrors)))
          },
          updatedDocument => {
            if (updatedDocument.isDefined)
              changeDocument(mode, itemId, documentId, existingDocument, updatedDocument, boundForm)
            else
              Future.successful(returnToSummary(mode, itemId))
          }
        )
      case _ => Future.successful(returnToSummary(mode, itemId))
    }
  }

  private def returnToSummary(mode: Mode, itemId: String)(implicit request: JourneyRequest[AnyContent]) =
    navigator.continueTo(mode, routes.DocumentsProducedController.displayPage(_, itemId))

  private def findDocument(itemId: String, id: String)(implicit request: JourneyRequest[AnyContent]): Option[DocumentsProduced] =
    ListItem.findById(id, request.cacheModel.itemBy(itemId).flatMap(_.documentsProducedData).map(_.documents).getOrElse(Seq.empty))

  private def cachedDocuments(itemId: String)(implicit request: JourneyRequest[AnyContent]) =
    request.cacheModel.itemBy(itemId).flatMap(_.documentsProducedData).map(_.documents).getOrElse(Seq.empty)

  private def changeDocument(
    mode: Mode,
    itemId: String,
    documentId: String,
    existingDocument: DocumentsProduced,
    newDocument: DocumentsProduced,
    boundForm: Form[DocumentsProduced]
  )(implicit request: JourneyRequest[AnyContent]): Future[Result] = {

    val existingDocuments = cachedDocuments(itemId)
    val documentsWithoutExisting: Seq[DocumentsProduced] = existingDocuments.filterNot(_ == existingDocument)

    MultipleItemsHelper
      .add(boundForm, documentsWithoutExisting, maxNumberOfItems, DocumentsProducedFormGroupId, "declaration.addDocument")
      .fold(
        formWithErrors => Future.successful(BadRequest(documentProducedPage(mode, itemId, documentId, formWithErrors))),
        _ => {
          val updatedDocuments: Seq[DocumentsProduced] = existingDocuments.map(doc => if (doc == existingDocument) newDocument else doc)
          updateCache(itemId, DocumentsProducedData(updatedDocuments))
            .map(_ => returnToSummary(mode, itemId))
        }
      )
  }

  private def updateCache(itemId: String, updatedData: DocumentsProducedData)(
    implicit req: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      model.updatedItem(itemId, item => item.copy(documentsProducedData = Some(updatedData)))
    })
}
