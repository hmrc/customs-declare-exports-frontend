/*
 * Copyright 2019 HM Revenue & Customs
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
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.declaration.additionaldocuments.DocumentsProduced
import forms.declaration.additionaldocuments.DocumentsProduced.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.ExportsDeclaration
import models.declaration.DocumentsProducedData
import models.declaration.DocumentsProducedData.maxNumberOfItems
import models.requests.JourneyRequest
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.{ExportItem, ExportsCacheService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.documents_produced

import scala.concurrent.{ExecutionContext, Future}

class DocumentsProducedController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  documentProducedPage: documents_produced
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.itemBy(itemId).flatMap(_.documentsProducedData).map(_.documents) match {
      case Some(data) => Ok(documentProducedPage(itemId, form(), data))
      case _          => Ok(documentProducedPage(itemId, form(), Seq()))
    }
  }

  def saveForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form().bindFromRequest()
    val actionTypeOpt = FormAction.bindFromRequest()
    val cachedData: Future[DocumentsProducedData] = exportsCacheService
      .getItemByIdAndSession(itemId, journeySessionId)
      .map(_.flatMap(_.documentsProducedData).getOrElse(DocumentsProducedData(Seq())))

    cachedData.flatMap { cache: DocumentsProducedData =>
      actionTypeOpt match {
        case Some(Add) if !boundForm.hasErrors             => addItem(itemId, boundForm.get, cache)
        case Some(SaveAndContinue) if !boundForm.hasErrors => saveAndContinue(itemId, boundForm.get, cache)
        case Some(Remove(keys))                            => removeItem(itemId, keys, boundForm, cache)
        case _                                             => Future.successful(BadRequest(documentProducedPage(itemId, boundForm, cache.documents)))
      }
    }
  }

  private def saveAndContinue(itemId: String, userInput: DocumentsProduced, cachedData: DocumentsProducedData)(
    implicit request: JourneyRequest[_],
    hc: HeaderCarrier
  ): Future[Result] =
    (userInput, cachedData.documents) match {
      case (document, Seq())     => saveAndRedirect(itemId, document, Seq())
      case (document, documents) => handleSaveAndContinueCache(itemId, document, documents)
    }

  private def handleSaveAndContinueCache(
    itemId: String,
    document: DocumentsProduced,
    documents: Seq[DocumentsProduced]
  )(implicit request: JourneyRequest[_]) =
    document match {
      case _ if documents.length >= maxNumberOfItems =>
        handleErrorPage(itemId, Seq(("", "supplementary.addDocument.error.maximumAmount")), document, documents)

      case _ if documents.contains(document) =>
        handleErrorPage(itemId, Seq(("", "supplementary.addDocument.error.duplicated")), document, documents)

      case _ => saveAndRedirect(itemId, document, documents)
    }

  private def saveAndRedirect(itemId: String, document: DocumentsProduced, documents: Seq[DocumentsProduced])(
    implicit request: JourneyRequest[_],
    hc: HeaderCarrier
  ): Future[Result] =
    if (document.isDefined) {
      val updateDocs = DocumentsProducedData(documents :+ document)
      updateModelInCache(itemId, document, updateDocs)
        .map(_ => Redirect(routes.ItemsSummaryController.displayPage()))
    } else Future.successful(Redirect(routes.ItemsSummaryController.displayPage()))

  private def updateModelInCache(itemId: String, document: DocumentsProduced, updatedDocs: DocumentsProducedData)(
    implicit journeyRequest: JourneyRequest[_]
  ) = updateCache(itemId, journeySessionId, updatedDocs)

  private def addItem(itemId: String, userInput: DocumentsProduced, cachedData: DocumentsProducedData)(
    implicit request: JourneyRequest[_],
    hc: HeaderCarrier
  ): Future[Result] =
    (userInput, cachedData.documents) match {
      case (_, documents) if documents.length >= maxNumberOfItems =>
        handleErrorPage(
          itemId,
          Seq(("", "supplementary.addDocument.error.maximumAmount")),
          userInput,
          cachedData.documents
        )

      case (document, documents) if documents.contains(document) =>
        handleErrorPage(
          itemId,
          Seq(("", "supplementary.addDocument.error.duplicated")),
          userInput,
          cachedData.documents
        )

      case (document, documents) =>
        if (document.isDefined) {
          updateCache(itemId, journeySessionId, DocumentsProducedData(documents :+ document))
            .map(_ => Redirect(routes.DocumentsProducedController.displayPage(itemId)))
        } else
          handleErrorPage(
            itemId,
            Seq(("", "supplementary.addDocument.error.notDefined")),
            userInput,
            cachedData.documents
          )
    }

  private def removeItem(
    itemId: String,
    keys: Seq[String],
    boundForm: Form[DocumentsProduced],
    cachedData: DocumentsProducedData
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier): Future[Result] =
    keys.headOption.fold(errorHandler.displayErrorPage()) { index =>
      val updatedCache = cachedData.copy(documents = cachedData.documents.patch(index.toInt, Nil, 1))
      updateCache(itemId, journeySessionId, updatedCache).map(
        _ => Ok(documentProducedPage(itemId, boundForm.discardingErrors, updatedCache.documents))
      )
    }

  private def handleErrorPage(
    itemId: String,
    fieldWithError: Seq[(String, String)],
    userInput: DocumentsProduced,
    documents: Seq[DocumentsProduced]
  )(implicit request: Request[_]): Future[Result] = {
    val updatedErrors = fieldWithError.map((FormError.apply(_: String, _: String)).tupled)

    val formWithError = form().fill(userInput).copy(errors = updatedErrors)

    Future.successful(BadRequest(documentProducedPage(itemId, formWithError, documents)))
  }

  private def updateCache(
    itemId: String,
    sessionId: String,
    updatedData: DocumentsProducedData
  ): Future[Option[ExportsDeclaration]] =
    getAndUpdateExportCacheModel(
      sessionId,
      model => {
        val item: Option[ExportItem] = model.items
          .find(item => item.id.equals(itemId))
          .map(_.copy(documentsProducedData = Some(updatedData)))
        val itemList = item.fold(model.items)(model.items.filter(item => !item.id.equals(itemId)) + _)
        exportsCacheService.update(sessionId, model.copy(items = itemList))
      }
    )
}
