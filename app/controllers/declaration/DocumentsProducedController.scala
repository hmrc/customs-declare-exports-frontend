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
import controllers.declaration.routes.{DocumentsProducedController, ItemsSummaryController}
import controllers.util.CacheIdGenerator.{cacheId, goodsItemCacheId}
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.declaration.DocumentsProduced
import forms.declaration.DocumentsProduced.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.DocumentsProducedData
import models.declaration.DocumentsProducedData.{formId, maxNumberOfItems}
import models.requests.JourneyRequest
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.{CustomsCacheService, ItemsCachingService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.documents_produced

import scala.concurrent.{ExecutionContext, Future}

class DocumentsProducedController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService,
  itemsCache: ItemsCachingService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService
      .fetchAndGetEntry[DocumentsProducedData](goodsItemCacheId, formId)
      .map {
        case Some(data) => Ok(documents_produced(appConfig, form, data.documents))
        case _          => Ok(documents_produced(appConfig, form, Seq()))
      }
  }

  def saveForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form.bindFromRequest()
    val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded(_))
    val cachedData = customsCacheService
      .fetchAndGetEntry[DocumentsProducedData](goodsItemCacheId, formId)
      .map(_.getOrElse(DocumentsProducedData(Seq())))

    cachedData.flatMap { cache =>
      boundForm
        .fold(
          (formWithErrors: Form[DocumentsProduced]) =>
            Future.successful(BadRequest(documents_produced(appConfig, formWithErrors, cache.documents))),
          validForm =>
            actionTypeOpt match {
              case Some(Add)             => addItem(validForm, cache)
              case Some(SaveAndContinue) => saveAndContinue(validForm, cache)
              case Some(Remove(keys))    => removeItem(keys, cache)
              case _                     => errorHandler.displayErrorPage()
          }
        )
    }
  }

  private def saveAndContinue(
    userInput: DocumentsProduced,
    cachedData: DocumentsProducedData
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier): Future[Result] =
    (userInput, cachedData.documents) match {
      case (document, Seq())     => saveAndRedirect(document, Seq())
      case (document, documents) => handleSaveAndContinueCache(document, documents)
    }

  private def handleSaveAndContinueCache(document: DocumentsProduced, documents: Seq[DocumentsProduced])(
    implicit request: JourneyRequest[_]
  ) =
    document match {
      case _ if documents.length >= maxNumberOfItems =>
        handleErrorPage(Seq(("", "supplementary.addDocument.error.maximumAmount")), document, documents)

      case _ if documents.contains(document) =>
        handleErrorPage(Seq(("", "supplementary.addDocument.error.duplicated")), document, documents)

      case _ => saveAndRedirect(document, documents)
    }

  private def saveAndRedirect(
    document: DocumentsProduced,
    documents: Seq[DocumentsProduced]
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier): Future[Result] =
    if (document.isDefined) {
      val updateDocs = DocumentsProducedData(documents :+ document)
      customsCacheService
        .cache[DocumentsProducedData](goodsItemCacheId, formId, updateDocs)
        .flatMap { _ =>
          addGoodsItem(document, updateDocs.documents)
        }
    } else
      addGoodsItem(document)

  private def addGoodsItem(
    document: DocumentsProduced,
    docs: Seq[DocumentsProduced] = Seq.empty
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier) =
    itemsCache.addItemToCache(goodsItemCacheId, cacheId).flatMap {
      case true => Future.successful(Redirect(ItemsSummaryController.displayForm()))
      case false =>
        handleErrorPage(Seq(("", "supplementary.addgoodsitems.addallpages.error")), document, docs)
    }

  private def addItem(
    userInput: DocumentsProduced,
    cachedData: DocumentsProducedData
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier): Future[Result] =
    (userInput, cachedData.documents) match {
      case (_, documents) if documents.length >= maxNumberOfItems =>
        handleErrorPage(Seq(("", "supplementary.addDocument.error.maximumAmount")), userInput, cachedData.documents)

      case (document, documents) if documents.contains(document) =>
        handleErrorPage(Seq(("", "supplementary.addDocument.error.duplicated")), userInput, cachedData.documents)

      case (document, documents) => {
        if (document.isDefined) {
          val updatedCache = DocumentsProducedData(documents :+ document)
          customsCacheService
            .cache[DocumentsProducedData](goodsItemCacheId, formId, updatedCache)
            .map(_ => Redirect(DocumentsProducedController.displayForm()))
        } else handleErrorPage(Seq(("", "supplementary.addDocument.error.notDefined")), userInput, cachedData.documents)
      }
    }

  private def removeItem(keys: Seq[String], cachedData: DocumentsProducedData)(
    implicit request: JourneyRequest[_],
    hc: HeaderCarrier
  ): Future[Result] = keys.headOption.fold(errorHandler.displayErrorPage()) { index =>
    val updatedCache = cachedData.copy(documents = cachedData.documents.patch(index.toInt, Nil, 1))
    customsCacheService.cache[DocumentsProducedData](goodsItemCacheId, formId, updatedCache).map { _ =>
      Redirect(DocumentsProducedController.displayForm())
    }
  }

  private def handleErrorPage(
    fieldWithError: Seq[(String, String)],
    userInput: DocumentsProduced,
    documents: Seq[DocumentsProduced]
  )(implicit request: Request[_]): Future[Result] = {
    val updatedErrors = fieldWithError.map((FormError.apply(_: String, _: String)).tupled)

    val formWithError = form.fill(userInput).copy(errors = updatedErrors)

    Future.successful(BadRequest(documents_produced(appConfig, formWithError, documents)))
  }

}
