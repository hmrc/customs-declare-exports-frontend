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
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.{ExportItem, ExportItemIdGeneratorService, ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.items_summary

import scala.concurrent.{ExecutionContext, Future}

class ItemsSummaryController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  exportsCacheService: ExportsCacheService,
  exportItemIdGeneratorService: ExportItemIdGeneratorService,
  mcc: MessagesControllerComponents,
  itemsSummaryPage: items_summary
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc) with I18nSupport with SessionIdAware {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(itemsSummaryPage(request.cacheModel.items.toList))
  }

  def addItem(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val newItem = ExportItem(id = exportItemIdGeneratorService.generateItemId())
    updateCache(journeySessionId, newItem)
      .map(_ => Redirect(controllers.declaration.routes.ProcedureCodesController.displayPage(newItem.id)))
  }

  def removeItem(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    exportsCacheService.get(journeySessionId) flatMap {
      case Some(model) =>
        model.items.find(_.id == itemId) match {
          case Some(itemToDelete) =>
            val updatedItems = model.copy(items = model.items - itemToDelete).items.zipWithIndex.map {
              case (item, index) => item.copy(sequenceId = index + 1)
            }
            exportsCacheService.update(journeySessionId, model.copy(items = updatedItems)).map { _ =>
              Redirect(routes.ItemsSummaryController.displayPage())
            }
          case _ =>
            Future.successful(Redirect(routes.ItemsSummaryController.displayPage()))
        }
      case _ =>
        Future.successful(Redirect(routes.ItemsSummaryController.displayPage()))
    }
  }

  private def updateCache(sessionId: String, exportItem: ExportItem): Future[Option[ExportsCacheModel]] =
    exportsCacheService.get(sessionId).flatMap {
      case Some(model) => {
        exportsCacheService
          .update(sessionId, model.copy(items = model.items + exportItem.copy(sequenceId = model.items.size + 1)))
      }
      case None => Future.successful(None)
    }
}
