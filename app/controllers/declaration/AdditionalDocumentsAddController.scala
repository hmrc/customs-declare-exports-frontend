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
import controllers.declaration.AdditionalDocumentsAddController.DocumentsProducedFormGroupId
import controllers.navigation.Navigator
import controllers.util._
import forms.declaration.additionaldocuments.DocumentsProduced
import forms.declaration.additionaldocuments.DocumentsProduced.{form, globalErrors}
import javax.inject.Inject
import models.declaration.DocumentsProducedData
import models.declaration.DocumentsProducedData.maxNumberOfItems
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.additionalDocuments.additional_documents_add

class AdditionalDocumentsAddController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalDocumentAddPage: additional_documents_add
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(additionalDocumentAddPage(mode, itemId, form().withSubmissionErrors(), request.cacheModel.commodityCode(itemId)))
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = globalErrors(form().bindFromRequest())

    boundForm.fold(formWithErrors => {
      Future.successful(BadRequest(additionalDocumentAddPage(mode, itemId, formWithErrors, request.cacheModel.commodityCode(itemId))))
    }, documents => {
      val documentsProducedData = request.cacheModel.documentsProducedData(itemId)
      if (documents.isDefined) saveDocuments(mode, itemId, boundForm, documentsProducedData)
      else continue(mode, itemId, documentsProducedData)
    })
  }

  private def continue(mode: Mode, itemId: String, documentsProducedData: DocumentsProducedData)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    updateCache(itemId, documentsProducedData)
      .map(
        _ =>
          if (documentsProducedData.isEmpty)
            navigator.continueTo(mode, routes.ItemsSummaryController.displayItemsSummaryPage)
          else
            navigator.continueTo(mode, routes.AdditionalDocumentsController.displayPage(_, itemId))
      )

  private def saveDocuments(mode: Mode, itemId: String, boundForm: Form[DocumentsProduced], documentsProducedData: DocumentsProducedData)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, documentsProducedData.documents, maxNumberOfItems, DocumentsProducedFormGroupId, "declaration.addDocument")
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(additionalDocumentAddPage(mode, itemId, formWithErrors, request.cacheModel.commodityCode(itemId)))),
        documents =>
          updateCache(itemId, documentsProducedData.copy(documents = documents))
            .map(_ => navigator.continueTo(mode, routes.AdditionalDocumentsController.displayPage(_, itemId)))
      )

  private def updateCache(itemId: String, documentsProducedData: DocumentsProducedData)(
    implicit req: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      model.updatedItem(itemId, item => item.copy(documentsProducedData = Some(documentsProducedData)))
    })
}

object AdditionalDocumentsAddController {
  val DocumentsProducedFormGroupId: String = "documentsProduced"
}
