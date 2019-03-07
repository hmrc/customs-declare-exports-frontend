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

package controllers.supplementary

import config.AppConfig
import controllers.actions.AuthAction
import controllers.supplementary.routes.{DocumentsProducedController, SummaryPageController}
import controllers.util.CacheIdGenerator.supplementaryCacheId
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.supplementary.DocumentsProduced
import forms.supplementary.DocumentsProduced.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.supplementary.DocumentsProducedData
import models.declaration.supplementary.DocumentsProducedData.{formId, maxNumberOfItems}
import models.requests.AuthenticatedRequest
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.CustomsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.supplementary.documents_produced

import scala.concurrent.{ExecutionContext, Future}

class DocumentsProducedController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService
      .fetchAndGetEntry[DocumentsProducedData](supplementaryCacheId, formId)
      .map {
        case Some(data) => Ok(documents_produced(appConfig, form, data.documents))
        case _          => Ok(documents_produced(appConfig, form, Seq()))
      }
  }

  def saveForm(): Action[AnyContent] = authenticate.async { implicit request =>
    val boundForm = form.bindFromRequest()
    val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded(_))
    val cachedData = customsCacheService
      .fetchAndGetEntry[DocumentsProducedData](supplementaryCacheId, formId)
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
              case Some(Remove(values))  => removeItem(retrieveItem(Json.parse(values.headOption.get)), cache)
              case _                     => errorHandler.displayErrorPage()
          }
        )
    }
  }

  private def saveAndContinue(
    userInput: DocumentsProduced,
    cachedData: DocumentsProducedData
  )(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier): Future[Result] =
    (userInput, cachedData.documents) match {
      case (document, Seq())     => saveAndRedirect(document, Seq())
      case (document, documents) => handleSaveAndContinueCache(document, documents)
    }

  private def handleSaveAndContinueCache(document: DocumentsProduced, documents: Seq[DocumentsProduced])(
    implicit request: AuthenticatedRequest[_]
  ) =
    document match {
      case _ if documents.length >= maxNumberOfItems =>
        handleErrorPage(Seq(("", "supplementary.addDocument.maximumAmount.error")), document, documents)

      case _ if documents.contains(document) =>
        handleErrorPage(Seq(("", "supplementary.addDocument.duplicated")), document, documents)

      case _ => saveAndRedirect(document, documents)
    }

  private def saveAndRedirect(
    document: DocumentsProduced,
    documents: Seq[DocumentsProduced]
  )(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier): Future[Result] =
    if (document.isDefined)
      customsCacheService
        .cache[DocumentsProducedData](supplementaryCacheId, formId, DocumentsProducedData(documents :+ document))
        .map { _ =>
          Redirect(SummaryPageController.displayPage())
        } else Future.successful(Redirect(SummaryPageController.displayPage()))

  private def addItem(
    userInput: DocumentsProduced,
    cachedData: DocumentsProducedData
  )(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier): Future[Result] =
    (userInput, cachedData.documents) match {
      case (_, documents) if documents.length >= maxNumberOfItems =>
        handleErrorPage(Seq(("", "supplementary.addDocument.maximumAmount.error")), userInput, cachedData.documents)

      case (document, documents) if documents.contains(document) =>
        handleErrorPage(Seq(("", "supplementary.addDocument.duplicated")), userInput, cachedData.documents)

      case (document, documents) => {
        if (document.isDefined) {
          val updatedCache = DocumentsProducedData(documents :+ document)
          customsCacheService
            .cache[DocumentsProducedData](supplementaryCacheId, formId, updatedCache)
            .map(_ => Redirect(DocumentsProducedController.displayForm()))
        } else handleErrorPage(Seq(("", "supplementary.addDocument.isNotDefined")), userInput, cachedData.documents)
      }
    }

  private def removeItem(
    docToRemove: DocumentsProduced,
    cachedData: DocumentsProducedData
  )(implicit request: AuthenticatedRequest[_], hc: HeaderCarrier): Future[Result] =
    if (cachedData.containsItem(docToRemove)) {
      val updatedCache = cachedData.copy(documents = cachedData.documents.filterNot(_ == docToRemove))

      customsCacheService.cache[DocumentsProducedData](supplementaryCacheId, formId, updatedCache).map { _ =>
        Redirect(DocumentsProducedController.displayForm())
      }
    } else errorHandler.displayErrorPage()

  private def handleErrorPage(
    fieldWithError: Seq[(String, String)],
    userInput: DocumentsProduced,
    documents: Seq[DocumentsProduced]
  )(implicit request: Request[_]): Future[Result] = {
    val updatedErrors = fieldWithError.map((FormError.apply(_: String, _: String)).tupled)

    val formWithError = form.fill(userInput).copy(errors = updatedErrors)

    Future.successful(BadRequest(documents_produced(appConfig, formWithError, documents)))
  }

  private def retrieveItem(value: JsValue): DocumentsProduced = DocumentsProduced.fromJson(value)

}
