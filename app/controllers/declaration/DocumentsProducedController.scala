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

import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction}
import controllers.util.CacheIdGenerator.{cacheId, goodsItemCacheId}
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.declaration.additionaldocuments.DocumentsProduced
import forms.declaration.additionaldocuments.DocumentsProduced.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.DocumentsProducedData
import models.declaration.DocumentsProducedData.{formId, maxNumberOfItems}
import models.requests.JourneyRequest
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.{ExportItem, ExportsCacheModel, ExportsCacheService}
import services.{CustomsCacheService, ItemsCachingService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.documents_produced

import scala.concurrent.{ExecutionContext, Future}

class DocumentsProducedController @Inject()(
  appConfig: AppConfig,
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  legacyCustomsCacheService: CustomsCacheService,
  exportsCacheService: ExportsCacheService,
  itemsCache: ItemsCachingService,
  mcc: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends {
  val cacheService = exportsCacheService
} with FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    legacyCustomsCacheService
      .fetchAndGetEntry[DocumentsProducedData](goodsItemCacheId, formId)
      .map {
        case Some(data) => Ok(documents_produced(itemId, appConfig, form, data.documents))
        case _          => Ok(documents_produced(itemId, appConfig, form, Seq()))
      }
  }

  def saveForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form.bindFromRequest()
    val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded(_))
    val cachedData = legacyCustomsCacheService
      .fetchAndGetEntry[DocumentsProducedData](goodsItemCacheId, formId)
      .map(_.getOrElse(DocumentsProducedData(Seq())))

    cachedData.flatMap { cache =>
      boundForm
        .fold(
          (formWithErrors: Form[DocumentsProduced]) =>
            Future.successful(BadRequest(documents_produced(itemId, appConfig, formWithErrors, cache.documents))),
          validForm =>
            actionTypeOpt match {
              case Some(Add)             => addItem(itemId, validForm, cache)
              case Some(SaveAndContinue) => saveAndContinue(itemId, validForm, cache)
              case Some(Remove(keys))    => removeItem(itemId, keys, cache)
              case _                     => errorHandler.displayErrorPage()
          }
        )
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
      updateModelInCache(itemId, document, updateDocs).flatMap(
        _ => addGoodsItem(itemId, document, updateDocs.documents)
      )
    } else addGoodsItem(itemId, document)

  private def updateModelInCache(itemId: String, document: DocumentsProduced, updatedDocs: DocumentsProducedData)(
    implicit journeyRequest: JourneyRequest[_]
  ) =
    for {
      _ <- updateCache(itemId, journeySessionId, updatedDocs)
      _ <- legacyCustomsCacheService.cache[DocumentsProducedData](goodsItemCacheId, formId, updatedDocs)
    } yield ()

  private def addGoodsItem(itemId: String, document: DocumentsProduced, docs: Seq[DocumentsProduced] = Seq.empty)(
    implicit request: JourneyRequest[_],
    hc: HeaderCarrier
  ) =
    itemsCache.addItemToCache(goodsItemCacheId, cacheId).flatMap {
      case true  => Future.successful(Redirect(routes.ItemsSummaryController.displayPage()))
      case false => handleErrorPage(itemId, Seq(("", "supplementary.addgoodsitems.addallpages.error")), document, docs)
    }

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
          updateCacheAndRedirect(
            itemId,
            DocumentsProducedData(documents :+ document),
            routes.DocumentsProducedController.displayPage(itemId)
          )
        } else
          handleErrorPage(
            itemId,
            Seq(("", "supplementary.addDocument.error.notDefined")),
            userInput,
            cachedData.documents
          )
    }

  private def removeItem(itemId: String, keys: Seq[String], cachedData: DocumentsProducedData)(
    implicit request: JourneyRequest[_],
    hc: HeaderCarrier
  ): Future[Result] = keys.headOption.fold(errorHandler.displayErrorPage()) { index =>
    val updatedCache = cachedData.copy(documents = cachedData.documents.patch(index.toInt, Nil, 1))
    updateCacheAndRedirect(itemId, updatedCache, routes.DocumentsProducedController.displayPage(itemId))
  }

  private def updateCacheAndRedirect(itemId: String, documentsToUpdate: DocumentsProducedData, redirectCall: Call)(
    implicit request: JourneyRequest[_]
  ): Future[Result] =
    for {
      _ <- updateCache(itemId, journeySessionId, documentsToUpdate)
      _ <- legacyCustomsCacheService.cache[DocumentsProducedData](goodsItemCacheId, formId, documentsToUpdate)
    } yield Redirect(redirectCall)

  private def handleErrorPage(
    itemId: String,
    fieldWithError: Seq[(String, String)],
    userInput: DocumentsProduced,
    documents: Seq[DocumentsProduced]
  )(implicit request: Request[_]): Future[Result] = {
    val updatedErrors = fieldWithError.map((FormError.apply(_: String, _: String)).tupled)

    val formWithError = form.fill(userInput).copy(errors = updatedErrors)

    Future.successful(BadRequest(documents_produced(itemId, appConfig, formWithError, documents)))
  }

  private def updateCache(
    itemId: String,
    sessionId: String,
    updatedData: DocumentsProducedData
  ): Future[Either[String, ExportsCacheModel]] =
    getAndUpdateExportCacheModel(
      sessionId,
      model => {
        val item: Option[ExportItem] = model.items
          .filter(item => item.id.equals(itemId))
          .headOption
          .map(_.copy(documentsProducedData = Some(updatedData)))
        val itemList = item.fold(model.items)(model.items.filter(item => !item.id.equals(itemId)) + _)
        exportsCacheService.update(sessionId, model.copy(items = itemList))
      }
    )
}
