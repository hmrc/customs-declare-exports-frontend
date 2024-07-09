/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.section4

import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.{ModelCacheable, SubmissionErrors}
import controllers.helpers.MultipleItemsHelper
import controllers.navigation.Navigator
import controllers.section4.PreviousDocumentsController.PreviousDocumentsFormGroupId
import controllers.section4.routes.PreviousDocumentsSummaryController
import forms.section4.{Document, PreviousDocumentsData}
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.DocumentTypeService
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.ListItem
import views.html.section4.previousDocuments.previous_documents_change

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PreviousDocumentsChangeController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  changePage: previous_documents_change,
  documentTypeService: DocumentTypeService
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage(id: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    findDocument(id) match {
      case Some(document) => Ok(changePage(id, Document.form(documentTypeService).fill(document).withSubmissionErrors))
      case _              => returnToSummary()
    }
  }

  def submit(id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    findDocument(id) match {
      case Some(existingDocument) =>
        val boundForm = Document.form(documentTypeService).bindFromRequest()
        val documentsWithoutExisting = request.cacheModel.previousDocuments.map(_.documents).getOrElse(Seq.empty).filterNot(_ == existingDocument)

        MultipleItemsHelper
          .add(
            boundForm,
            documentsWithoutExisting,
            PreviousDocumentsData.maxAmountOfItems,
            PreviousDocumentsFormGroupId,
            "declaration.previousDocuments"
          )
          .fold(formWithErrors => Future.successful(BadRequest(changePage(id, formWithErrors))), updateCache(_).map(_ => returnToSummary()))

      case _ => Future.successful(returnToSummary())
    }
  }

  private def findDocument(id: String)(implicit request: JourneyRequest[AnyContent]): Option[Document] =
    ListItem.findById(id, request.cacheModel.previousDocuments.map(_.documents).getOrElse(Seq.empty))

  private def returnToSummary()(implicit request: JourneyRequest[AnyContent]): Result =
    navigator.continueTo(PreviousDocumentsSummaryController.displayPage)

  private def updateCache(updatedDocuments: Seq[Document])(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.updatePreviousDocuments(updatedDocuments))
}
