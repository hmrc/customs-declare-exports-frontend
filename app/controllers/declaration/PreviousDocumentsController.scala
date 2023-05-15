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

package controllers.declaration

import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.PreviousDocumentsController.PreviousDocumentsFormGroupId
import controllers.helpers.MultipleItemsHelper
import controllers.navigation.Navigator
import forms.declaration.{Document, PreviousDocumentsData}
import forms.declaration.Document.form
import forms.declaration.PreviousDocumentsData.maxAmountOfItems
import models.requests.JourneyRequest
import models.ExportsDeclaration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import services.DocumentTypeService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.previousDocuments.previous_documents

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
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(previousDocumentsPage(form(documentTypeService)))
  }

  def submit(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val documents = request.cacheModel.previousDocuments.getOrElse(PreviousDocumentsData(Seq.empty)).documents

    MultipleItemsHelper
      .add(form(documentTypeService).bindFromRequest(), documents, maxAmountOfItems, PreviousDocumentsFormGroupId, "declaration.previousDocuments")
      .fold(
        formWithErrors => Future.successful(BadRequest(previousDocumentsPage(formWithErrors))),
        updateCache(_).map(_ => navigator.continueTo(routes.PreviousDocumentsSummaryController.displayPage))
      )
  }

  private def updateCache(documents: Seq[Document])(implicit req: JourneyRequest[_]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model => model.copy(previousDocuments = Some(PreviousDocumentsData(documents))))
}

object PreviousDocumentsController {
  val PreviousDocumentsFormGroupId: String = "previousDocuments"
}
