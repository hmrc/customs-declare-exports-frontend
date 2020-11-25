/*
 * Copyright 2020 HM Revenue & Customs
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
import views.html.declaration.documentsProduced.documents_produced_add

import scala.concurrent.{ExecutionContext, Future}

class DocumentsProducedAddController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  documentProducedPage: documents_produced_add
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(documentProducedPage(mode, itemId, form().withSubmissionErrors()))
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = globalErrors(form().bindFromRequest())

    boundForm.fold(
      formWithErrors => {
        Future.successful(BadRequest(documentProducedPage(mode, itemId, formWithErrors)))
      },
      documents => {
        if (documents.isDefined)
          saveDocuments(mode, itemId, boundForm, cachedDocuments(itemId).documents)
        else
          continue(mode, itemId, cachedDocuments(itemId))
      }
    )
  }

  private def cachedDocuments(itemId: String)(implicit request: JourneyRequest[AnyContent]) =
    request.cacheModel.itemBy(itemId).flatMap(_.documentsProducedData).getOrElse(DocumentsProducedData(Seq()))

  private def continue(mode: Mode, itemId: String, cachedData: DocumentsProducedData)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    updateCache(itemId, cachedData)
      .map(
        _ =>
          if (cachedData.isEmpty)
            navigator.continueTo(mode, routes.ItemsSummaryController.displayItemsSummaryPage)
          else
            navigator.continueTo(mode, routes.DocumentsProducedController.displayPage(_, itemId))
      )

  private def saveDocuments(mode: Mode, itemId: String, boundForm: Form[DocumentsProduced], cachedData: Seq[DocumentsProduced])(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    MultipleItemsHelper
      .add(boundForm, cachedData, maxNumberOfItems, DocumentsProducedFormGroupId, "declaration.addDocument")
      .fold(
        formWithErrors => Future.successful(BadRequest(documentProducedPage(mode, itemId, formWithErrors))),
        updatedCache =>
          updateCache(itemId, DocumentsProducedData(updatedCache))
            .map(_ => navigator.continueTo(mode, routes.DocumentsProducedController.displayPage(_, itemId)))
      )

  private def updateCache(itemId: String, updatedData: DocumentsProducedData)(
    implicit req: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      model.updatedItem(itemId, item => item.copy(documentsProducedData = Some(updatedData)))
    })
}

object DocumentsProducedAddController {
  val DocumentsProducedFormGroupId: String = "documentsProduced"
}
