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
import controllers.navigation.Navigator
import controllers.util.MultipleItemsHelper.remove
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.additionaldocuments.DocumentsProduced
import javax.inject.Inject
import models.declaration.DocumentsProducedData
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.ListItem
import views.html.declaration.documentsProduced.documents_produced_remove

import scala.concurrent.{ExecutionContext, Future}

class DocumentsProducedRemoveController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  documentRemovePage: documents_produced_remove
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String, documentId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    findDocument(itemId, documentId) match {
      case Some(document) => Ok(documentRemovePage(mode, itemId, documentId, document, removeYesNoForm.withSubmissionErrors()))
      case _              => returnToSummary(mode, itemId)
    }
  }

  def submitForm(mode: Mode, itemId: String, documentId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    findDocument(itemId, documentId) match {
      case Some(document) =>
        removeYesNoForm
          .bindFromRequest()
          .fold(
            (formWithErrors: Form[YesNoAnswer]) =>
              Future.successful(BadRequest(documentRemovePage(mode, itemId, documentId, document, formWithErrors))),
            formData => {
              formData.answer match {
                case YesNoAnswers.yes =>
                  removeDocumentsProduced(itemId, document)
                    .map(_ => returnToSummary(mode, itemId))
                case YesNoAnswers.no =>
                  Future.successful(returnToSummary(mode, itemId))
              }
            }
          )
      case _ => Future.successful(returnToSummary(mode, itemId))
    }

  }

  private def removeYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.addDocument.remove.empty")

  private def returnToSummary(mode: Mode, itemId: String)(implicit request: JourneyRequest[AnyContent]) =
    navigator.continueTo(mode, routes.DocumentsProducedController.displayPage(_, itemId))

  private def findDocument(itemId: String, id: String)(implicit request: JourneyRequest[AnyContent]): Option[DocumentsProduced] =
    ListItem.findById(id, request.cacheModel.itemBy(itemId).flatMap(_.documentsProducedData).map(_.documents).getOrElse(Seq.empty))

  private def removeDocumentsProduced(itemId: String, itemToRemove: DocumentsProduced)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] = {
    val cachedDocuments = request.cacheModel.itemBy(itemId).flatMap(_.documentsProducedData).map(_.documents).getOrElse(Seq.empty)
    val updatedDocumentsData = DocumentsProducedData(remove(cachedDocuments, itemToRemove.equals(_: DocumentsProduced)))

    updateExportsDeclarationSyncDirect(model => {
      model.updatedItem(itemId, item => item.copy(documentsProducedData = Some(updatedDocumentsData)))
    })
  }

}
