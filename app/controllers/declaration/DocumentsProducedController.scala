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
import controllers.navigation.Navigator
import controllers.util.MultipleItemsHelper.remove
import controllers.util._
import forms.declaration.additionaldocuments.DocumentsProduced
import forms.declaration.additionaldocuments.DocumentsProduced.form
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.DocumentsProducedData
import models.declaration.DocumentsProducedData.maxNumberOfItems
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.documents_produced

import scala.concurrent.{ExecutionContext, Future}

class DocumentsProducedController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  documentProducedPage: documents_produced
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.itemBy(itemId).flatMap(_.documentsProducedData).map(_.documents) match {
      case Some(data) => Ok(documentProducedPage(mode, itemId, form(), data))
      case _          => Ok(documentProducedPage(mode, itemId, form(), Seq()))
    }
  }

  def saveForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form().bindFromRequest()
    val actionTypeOpt = FormAction.bindFromRequest()
    val cache =
      request.cacheModel
        .itemBy(itemId)
        .flatMap(_.documentsProducedData)
        .getOrElse(DocumentsProducedData(Seq()))

    actionTypeOpt match {
      case Add if !boundForm.hasErrors => addItem(mode, itemId, boundForm.get, cache)
      case SaveAndContinue | SaveAndReturn if !boundForm.hasErrors =>
        saveAndContinue(mode, itemId, boundForm.get, cache)
      case Remove(keys) => removeItem(mode, itemId, keys, boundForm, cache)
      case _            => Future.successful(BadRequest(documentProducedPage(mode, itemId, boundForm, cache.documents)))
    }
  }

  private def saveAndContinue(mode: Mode, itemId: String, userInput: DocumentsProduced, cachedData: DocumentsProducedData)(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] =
    (userInput, cachedData.documents) match {
      case (document, Seq())     => saveAndRedirect(mode, itemId, document, Seq())
      case (document, documents) => handleSaveAndContinueCache(mode, itemId, document, documents)
    }

  private def handleSaveAndContinueCache(mode: Mode, itemId: String, document: DocumentsProduced, documents: Seq[DocumentsProduced])(
    implicit request: JourneyRequest[AnyContent]
  ) =
    document match {
      case _ if documents.length >= maxNumberOfItems =>
        handleErrorPage(mode, itemId, Seq(("", "supplementary.addDocument.error.maximumAmount")), document, documents)

      case _ if documents.contains(document) =>
        handleErrorPage(mode, itemId, Seq(("", "supplementary.addDocument.error.duplicated")), document, documents)

      case _ => saveAndRedirect(mode, itemId, document, documents)
    }

  private def saveAndRedirect(mode: Mode, itemId: String, document: DocumentsProduced, documents: Seq[DocumentsProduced])(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] =
    if (document.isDefined) {
      val updateDocs = DocumentsProducedData(documents :+ document)
      updateModelInCache(itemId, document, updateDocs)
        .map(_ => navigator.continueTo(routes.ItemsSummaryController.displayPage(mode)))
    } else Future.successful(navigator.continueTo(routes.ItemsSummaryController.displayPage(mode)))

  private def updateModelInCache(itemId: String, document: DocumentsProduced, updatedDocs: DocumentsProducedData)(
    implicit journeyRequest: JourneyRequest[AnyContent]
  ) = updateCache(itemId, updatedDocs)

  private def addItem(mode: Mode, itemId: String, userInput: DocumentsProduced, cachedData: DocumentsProducedData)(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] =
    (userInput, cachedData.documents) match {
      case (_, documents) if documents.length >= maxNumberOfItems =>
        handleErrorPage(mode, itemId, Seq(("", "supplementary.addDocument.error.maximumAmount")), userInput, cachedData.documents)

      case (document, documents) if documents.contains(document) =>
        handleErrorPage(mode, itemId, Seq(("", "supplementary.addDocument.error.duplicated")), userInput, cachedData.documents)

      case (document, documents) =>
        if (document.isDefined) {
          updateCache(itemId, DocumentsProducedData(documents :+ document))
            .map(_ => navigator.continueTo(routes.DocumentsProducedController.displayPage(mode, itemId)))
        } else
          handleErrorPage(mode, itemId, Seq(("", "supplementary.addDocument.error.notDefined")), userInput, cachedData.documents)
    }

  private def removeItem(mode: Mode, itemId: String, values: Seq[String], boundForm: Form[DocumentsProduced], cachedData: DocumentsProducedData)(
    implicit request: JourneyRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] = {
    val itemToRemove = DocumentsProduced.fromJsonString(values.head)
    val updatedCache =
      cachedData.copy(documents = remove(cachedData.documents, itemToRemove.contains(_: DocumentsProduced)))
    updateCache(itemId, updatedCache).map(_ => navigator.continueTo(routes.DocumentsProducedController.displayPage(mode, itemId)))
  }

  private def handleErrorPage(
    mode: Mode,
    itemId: String,
    fieldWithError: Seq[(String, String)],
    userInput: DocumentsProduced,
    documents: Seq[DocumentsProduced]
  )(implicit request: Request[_]): Future[Result] = {
    val updatedErrors = fieldWithError.map((FormError.apply(_: String, _: String)).tupled)

    val formWithError = form().fill(userInput).copy(errors = updatedErrors)

    Future.successful(BadRequest(documentProducedPage(mode, itemId, formWithError, documents)))
  }

  private def updateCache(itemId: String, updatedData: DocumentsProducedData)(
    implicit req: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      model.updatedItem(itemId, item => item.copy(documentsProducedData = Some(updatedData)))
    })
}
