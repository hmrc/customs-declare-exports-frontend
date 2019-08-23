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
import controllers.util.{Add, FormAction, SaveAndContinue, SaveAndReturn}
import javax.inject.Inject
import models.ExportsDeclaration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.{ExportItem, ExportItemIdGeneratorService, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.items_summary

import scala.concurrent.{ExecutionContext, Future}

class ItemsSummaryController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  exportItemIdGeneratorService: ExportItemIdGeneratorService,
  mcc: MessagesControllerComponents,
  itemsSummaryPage: items_summary
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(itemsSummaryPage(request.cacheModel.items.toList))
  }

  def submit(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val action = FormAction.bindFromRequest()
    action match {
      case Some(Add) =>
        val newItem = ExportItem(id = exportItemIdGeneratorService.generateItemId())
        exportsCacheService
          .update(
            request.cacheModel
              .copy(items = request.cacheModel.items + newItem.copy(sequenceId = request.cacheModel.items.size + 1))
          )
          .map(_ => Redirect(controllers.declaration.routes.ProcedureCodesController.displayPage(newItem.id)))
      case Some(SaveAndContinue) | Some(SaveAndReturn) =>
        Future.successful(
          navigator.continueTo(controllers.declaration.routes.WarehouseIdentificationController.displayForm())
        )
    }
  }

  def removeItem(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    request.cacheModel.itemBy(itemId) match {
      case Some(itemToDelete) =>
        val updatedItems =
          request.cacheModel.copy(items = request.cacheModel.items - itemToDelete).items.zipWithIndex.map {
            case (item, index) => item.copy(sequenceId = index + 1)
          }
        exportsCacheService.update(request.cacheModel.copy(items = updatedItems)).map { _ =>
          Redirect(routes.ItemsSummaryController.displayPage())
        }
      case _ =>
        Future.successful(Redirect(routes.ItemsSummaryController.displayPage()))
    }
  }
}
