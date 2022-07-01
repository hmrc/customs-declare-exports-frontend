/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.declaration.PreviousDocumentsController.PreviousDocumentsFormGroupId
import controllers.navigation.Navigator
import controllers.helpers.MultipleItemsHelper
import forms.declaration.{Document, PreviousDocumentsData}
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.ListItem
import views.html.declaration.previousDocuments.previous_documents_change

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PreviousDocumentsChangeController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  changePage: previous_documents_change
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage(mode: Mode, id: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    findDocument(id) match {
      case Some(document) => Ok(changePage(mode, id, Document.form.fill(document).withSubmissionErrors))
      case _              => returnToSummary(mode)
    }
  }

  def submit(mode: Mode, id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    findDocument(id) match {
      case Some(existingDocument) =>
        val boundForm = Document.form.bindFromRequest
        val documentsWithoutExisting = request.cacheModel.previousDocuments.map(_.documents).getOrElse(Seq.empty).filterNot(_ == existingDocument)

        MultipleItemsHelper
          .add(
            boundForm,
            documentsWithoutExisting,
            PreviousDocumentsData.maxAmountOfItems,
            PreviousDocumentsFormGroupId,
            "declaration.previousDocuments"
          )
          .fold(formWithErrors => Future.successful(BadRequest(changePage(mode, id, formWithErrors))), updateCache(_).map(_ => returnToSummary(mode)))

      case _ => Future.successful(returnToSummary(mode))
    }
  }

  private def findDocument(id: String)(implicit request: JourneyRequest[AnyContent]): Option[Document] =
    ListItem.findById(id, request.cacheModel.previousDocuments.map(_.documents).getOrElse(Seq.empty))

  private def returnToSummary(mode: Mode)(implicit request: JourneyRequest[AnyContent]): Result =
    navigator.continueTo(mode, routes.PreviousDocumentsSummaryController.displayPage)

  private def updateCache(updatedDocuments: Seq[Document])(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.updatePreviousDocuments(updatedDocuments))
}
