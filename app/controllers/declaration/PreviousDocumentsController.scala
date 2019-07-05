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

import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction}
import controllers.util.CacheIdGenerator.cacheId
import controllers.util._
import forms.declaration.Document._
import forms.declaration.PreviousDocumentsData
import forms.declaration.PreviousDocumentsData._
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.previous_documents

import scala.concurrent.{ExecutionContext, Future}

class PreviousDocumentsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService,
  mcc: MessagesControllerComponents,
  previousDocumentsPage: previous_documents,
  override val cacheService: ExportsCacheService
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService.fetchAndGetEntry[PreviousDocumentsData](cacheId, formId).map {
      case Some(data) => Ok(previousDocumentsPage(form, data.documents))
      case _          => Ok(previousDocumentsPage(form, Seq.empty))
    }
  }

  def savePreviousDocuments(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    import MultipleItemsHelper._

    val boundForm = form.bindFromRequest()

    val actionTypeOpt = request.body.asFormUrlEncoded.map(FormAction.fromUrlEncoded)

    val cachedData = customsCacheService
      .fetchAndGetEntry[PreviousDocumentsData](cacheId, formId)
      .map(_.getOrElse(PreviousDocumentsData(Seq.empty)))

    cachedData.flatMap { cache =>
      actionTypeOpt match {
        case Some(Add) =>
          add(boundForm, cache.documents, PreviousDocumentsData.maxAmountOfItems).fold(
            formWithErrors => Future.successful(BadRequest(previousDocumentsPage(formWithErrors, cache.documents))),
            updatedCache =>
              updateCache(PreviousDocumentsData(updatedCache))
                .map(_ => Redirect(controllers.declaration.routes.PreviousDocumentsController.displayForm()))
          )

        case Some(Remove(ids)) => {
          val updatedCache = remove(ids.headOption, cache.documents)
          updateCache(PreviousDocumentsData(updatedCache))
            .map(_ => Redirect(controllers.declaration.routes.PreviousDocumentsController.displayForm()))
        }

        case Some(SaveAndContinue) => {
          saveAndContinue(boundForm, cache.documents, isScreenMandatory, maxAmountOfItems).fold(
            formWithErrors => Future.successful(BadRequest(previousDocumentsPage(formWithErrors, cache.documents))),
            updatedCache =>
              if (updatedCache != cache.documents)
                updateCache(PreviousDocumentsData(updatedCache))
                  .map(_ => Redirect(controllers.declaration.routes.ItemsSummaryController.displayPage()))
              else
                Future.successful(Redirect(controllers.declaration.routes.ItemsSummaryController.displayPage()))
          )
        }

        case _ => errorHandler.displayErrorPage()
      }
    }
  }

  private def updateCache(formData: PreviousDocumentsData)(implicit req: JourneyRequest[_]): Future[Unit] =
    for {
      _ <- getAndUpdateExportCacheModel(
        journeySessionId,
        model =>
          cacheService
            .update(journeySessionId, model.copy(previousDocuments = Some(formData)))
      )
      _ <- customsCacheService.cache[PreviousDocumentsData](cacheId, formId, formData)
    } yield Unit
}
