/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.helpers.MultipleItemsHelper
import controllers.navigation.Navigator
import controllers.section4.PreviousDocumentsController.PreviousDocumentsFormGroupId
import controllers.section4.routes.PreviousDocumentsSummaryController
import forms.section4.Document.{documentTypeId, form}
import forms.section4.PreviousDocumentsData.maxAmountOfItems
import forms.section4.{Document, PreviousDocumentsData}
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.DocumentTypeService
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.validators.forms.AutoCompleteFieldBinding
import views.html.section4.previousDocuments.previous_documents

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PreviousDocumentsController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  previousDocumentsPage: previous_documents,
  override val exportsCacheService: ExportsCacheService,
  documentTypeService: DocumentTypeService
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with AutoCompleteFieldBinding with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  val displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(previousDocumentsPage(form(documentTypeService)))
  }

  val submit: Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val documents = request.cacheModel.previousDocuments.getOrElse(PreviousDocumentsData(Seq.empty)).documents
    val binding = form(documentTypeService).bindFromRequest(formValuesFromRequest(documentTypeId))

    MultipleItemsHelper
      .add(binding, documents, maxAmountOfItems, PreviousDocumentsFormGroupId, "declaration.previousDocuments")
      .fold(
        formWithErrors => Future.successful(BadRequest(previousDocumentsPage(formWithErrors))),
        updateCache(_).map(_ => navigator.continueTo(PreviousDocumentsSummaryController.displayPage))
      )
  }

  private def updateCache(documents: Seq[Document])(implicit req: JourneyRequest[_]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model => model.copy(previousDocuments = Some(PreviousDocumentsData(documents))))
}

object PreviousDocumentsController {
  val PreviousDocumentsFormGroupId: String = "previousDocuments"
}
