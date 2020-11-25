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
import controllers.navigation.Navigator
import controllers.util._
import controllers.declaration.PreviousDocumentsController.PreviousDocumentsFormGroupId
import forms.declaration.Document._
import forms.declaration.{Document, PreviousDocumentsData}
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.previousDocuments.previous_documents

import scala.concurrent.{ExecutionContext, Future}

class PreviousDocumentsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  previousDocumentsPage: previous_documents,
  override val exportsCacheService: ExportsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(previousDocumentsPage(mode, form()))
  }

  def savePreviousDocuments(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = if (isFirstPreviousDocument) Document.treatLikeOptional(form().bindFromRequest()) else form().bindFromRequest()

    val cache = request.cacheModel.previousDocuments.getOrElse(PreviousDocumentsData(Seq.empty))

    if (boundForm.value.isEmpty && boundForm.errors.isEmpty)
      updateCache(PreviousDocumentsData(Seq.empty)).map { _ =>
        navigator.continueTo(mode, controllers.declaration.routes.ItemsSummaryController.displayAddItemPage)
      } else
      MultipleItemsHelper
        .add(
          boundForm,
          cache.documents,
          PreviousDocumentsData.maxAmountOfItems,
          fieldId = PreviousDocumentsFormGroupId,
          "declaration.previousDocuments"
        )
        .fold(
          formWithErrors => Future.successful(BadRequest(previousDocumentsPage(mode, formWithErrors))),
          updatedCache =>
            updateCache(PreviousDocumentsData(updatedCache))
              .map(_ => navigator.continueTo(mode, controllers.declaration.routes.PreviousDocumentsSummaryController.displayPage))
        )
  }

  private def updateCache(formData: PreviousDocumentsData)(implicit req: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => model.copy(previousDocuments = Some(formData)))

  private def isFirstPreviousDocument()(implicit request: JourneyRequest[AnyContent]): Boolean = !request.cacheModel.hasPreviousDocuments
}

object PreviousDocumentsController {
  val PreviousDocumentsFormGroupId: String = "previousDocuments"
}
