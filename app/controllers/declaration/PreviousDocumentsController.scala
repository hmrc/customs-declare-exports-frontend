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
import controllers.util._
import forms.declaration.Document._
import forms.declaration.PreviousDocumentsData
import forms.declaration.PreviousDocumentsData._
import handlers.ErrorHandler
import javax.inject.Inject
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
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  previousDocumentsPage: previous_documents,
  override val exportsCacheService: ExportsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    exportsCacheService.get(journeySessionId).map(_.flatMap(_.previousDocuments)).map {
      case Some(data) => Ok(previousDocumentsPage(form(), data.documents))
      case _          => Ok(previousDocumentsPage(form(), Seq.empty))
    }
  }

  def savePreviousDocuments(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    import MultipleItemsHelper._

    val boundForm = form().bindFromRequest()
    val actionTypeOpt = FormAction.bindFromRequest()

    val cachedData = exportsCacheService
      .get(journeySessionId)
      .map(_.flatMap(_.previousDocuments))
      .map(_.getOrElse(PreviousDocumentsData(Seq.empty)))

    cachedData.flatMap { cache =>
      actionTypeOpt match {
        case Some(SaveAndContinue) =>
          saveAndContinue(boundForm, cache.documents, isScreenMandatory, maxAmountOfItems).fold(
            formWithErrors => Future.successful(BadRequest(previousDocumentsPage(formWithErrors, cache.documents))),
            updatedCache =>
              if (updatedCache != cache.documents)
                updateCache(PreviousDocumentsData(updatedCache))
                  .map(_ => Redirect(controllers.declaration.routes.ItemsSummaryController.displayPage()))
              else
                Future.successful(Redirect(controllers.declaration.routes.ItemsSummaryController.displayPage()))
          )

        case Some(Add) =>
          add(boundForm, cache.documents, PreviousDocumentsData.maxAmountOfItems).fold(
            formWithErrors => Future.successful(BadRequest(previousDocumentsPage(formWithErrors, cache.documents))),
            updatedCache =>
              updateCache(PreviousDocumentsData(updatedCache))
                .map(_ => Redirect(controllers.declaration.routes.PreviousDocumentsController.displayForm()))
          )

        case Some(Remove(ids)) => {
          val updatedDocuments = remove(ids.headOption, cache.documents)
          updateCache(PreviousDocumentsData(updatedDocuments))
            .map(_ => Ok(previousDocumentsPage(boundForm.discardingErrors, updatedDocuments)))
        }

        case _ => Future.successful(BadRequest(previousDocumentsPage(boundForm, cache.documents)))
      }
    }
  }

  private def updateCache(formData: PreviousDocumentsData)(implicit req: JourneyRequest[_]) =
    getAndUpdateExportCacheModel(
      journeySessionId,
      model =>
        exportsCacheService
          .update(journeySessionId, model.copy(previousDocuments = Some(formData)))
    )
}
