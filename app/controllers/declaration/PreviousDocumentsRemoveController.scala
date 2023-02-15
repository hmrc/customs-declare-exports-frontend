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
import controllers.navigation.Navigator
import controllers.helpers.MultipleItemsHelper
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.Document
import models.requests.JourneyRequest
import models.ExportsDeclaration
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.ListItem
import views.html.declaration.previousDocuments.previous_documents_remove

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PreviousDocumentsRemoveController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  removePage: previous_documents_remove
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with WithUnsafeDefaultFormBinding {

  def displayPage(id: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    findDocument(id) match {
      case Some(document) => Ok(removePage(id, document, removeYesNoForm))
      case _              => returnToSummary()
    }
  }

  def submit(id: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    findDocument(id) match {
      case Some(document) =>
        removeYesNoForm
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(removePage(id, document, formWithErrors))),
            validAnswer =>
              validAnswer.answer match {
                case YesNoAnswers.yes =>
                  removeDocument(document).map { _ =>
                    returnToSummary()
                  }
                case YesNoAnswers.no => Future.successful(returnToSummary())
              }
          )
      case _ => Future.successful(returnToSummary())
    }
  }

  private def removeYesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.previousDocuments.remove.empty")

  private def findDocument(id: String)(implicit request: JourneyRequest[AnyContent]): Option[Document] =
    ListItem.findById(id, request.cacheModel.previousDocuments.map(_.documents).getOrElse(Seq.empty))

  private def returnToSummary()(implicit request: JourneyRequest[AnyContent]) =
    navigator.continueTo(routes.PreviousDocumentsSummaryController.displayPage)

  private def removeDocument(documentToRemove: Document)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] = {
    val cachedDocuments = request.cacheModel.previousDocuments.map(_.documents).getOrElse(Seq.empty)
    val updatedDocuments = MultipleItemsHelper.remove(cachedDocuments, documentToRemove.equals(_: Document))
    updateDeclarationFromRequest(_.updatePreviousDocuments(updatedDocuments))
  }
}
