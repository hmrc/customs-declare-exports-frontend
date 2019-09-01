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
import controllers.util._
import forms.declaration.Document._
import forms.declaration.PreviousDocumentsData._
import forms.declaration.{Document, PreviousDocumentsData}
import handlers.ErrorHandler
import javax.inject.Inject
import models.{ExportsDeclaration, Mode}
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.previous_documents

import scala.concurrent.{ExecutionContext, Future}

class PreviousDocumentsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  previousDocumentsPage: previous_documents,
  override val exportsCacheService: ExportsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.previousDocuments match {
      case Some(data) => Ok(previousDocumentsPage(mode, form(), data.documents))
      case _          => Ok(previousDocumentsPage(mode, form(), Seq.empty))
    }
  }

  def savePreviousDocuments(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit request =>
      import MultipleItemsHelper._

      val boundForm = form().bindFromRequest()
      val actionTypeOpt = FormAction.bindFromRequest()

      val cache = request.cacheModel.previousDocuments.getOrElse(PreviousDocumentsData(Seq.empty))

      actionTypeOpt match {
        case SaveAndContinue | SaveAndReturn =>
          saveAndContinue(boundForm, cache.documents, isScreenMandatory, maxAmountOfItems).fold(
            formWithErrors =>
              Future.successful(BadRequest(previousDocumentsPage(mode, formWithErrors, cache.documents))),
            updatedCache =>
              if (updatedCache != cache.documents)
                updateCache(PreviousDocumentsData(updatedCache))
                  .map(
                    _ => navigator.continueTo(controllers.declaration.routes.ItemsSummaryController.displayPage(mode))
                  )
              else
                Future.successful(
                  navigator.continueTo(controllers.declaration.routes.ItemsSummaryController.displayPage(mode))
              )
          )

        case Add =>
          add(boundForm, cache.documents, PreviousDocumentsData.maxAmountOfItems).fold(
            formWithErrors =>
              Future.successful(BadRequest(previousDocumentsPage(mode, formWithErrors, cache.documents))),
            updatedCache =>
              updateCache(PreviousDocumentsData(updatedCache))
                .map(
                  _ =>
                    navigator.continueTo(controllers.declaration.routes.PreviousDocumentsController.displayPage(mode))
              )
          )

        case Remove(ids) =>
          val itemToRemove = Document.fromJsonString(ids.head)
          val updatedDocuments = MultipleItemsHelper.remove(cache.documents, itemToRemove.contains(_: Document))
          updateCache(PreviousDocumentsData(updatedDocuments))
            .map(_ => Ok(previousDocumentsPage(mode, boundForm.discardingErrors, updatedDocuments)))

        case _ => Future.successful(BadRequest(previousDocumentsPage(mode, boundForm, cache.documents)))
      }
  }

  private def updateCache(
    formData: PreviousDocumentsData
  )(implicit req: JourneyRequest[_]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => model.copy(previousDocuments = Some(formData)))
}
