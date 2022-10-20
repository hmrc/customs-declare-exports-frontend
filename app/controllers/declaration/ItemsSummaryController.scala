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
import controllers.declaration.routes.{ItemsSummaryController, ProcedureCodesController, TransportLeavingTheBorderController, WarehouseIdentificationController}
import controllers.helpers.SupervisingCustomsOfficeHelper
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.ExportsDeclaration
import models.declaration.ExportItem
import models.requests.JourneyRequest
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.{ExportItemIdGeneratorService, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declarationitems.{items_add_item, items_remove_item, items_summary}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ItemsSummaryController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  exportItemIdGeneratorService: ExportItemIdGeneratorService,
  mcc: MessagesControllerComponents,
  addItemPage: items_add_item,
  itemsSummaryPage: items_summary,
  removeItemPage: items_remove_item,
  supervisingCustomsOfficeHelper: SupervisingCustomsOfficeHelper
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithDefaultFormBinding {

  private def itemSummaryForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.itemsSummary.addAnotherItem.error.empty")
  private def removeItemForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.itemsRemove.error.empty")

  def displayAddItemPage(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (request.cacheModel.items.isEmpty) Ok(addItemPage())
    else navigator.continueTo(ItemsSummaryController.displayItemsSummaryPage)
  }

  def addFirstItem(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    createNewItemInCache.map(itemId => navigator.continueTo(ProcedureCodesController.displayPage(itemId)))
  }

  def displayItemsSummaryPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    removeEmptyItems.map { declaration =>
      if (declaration.items.isEmpty) navigator.continueTo(ItemsSummaryController.displayAddItemPage)
      else Ok(itemsSummaryPage(itemSummaryForm, declaration.items.toList))
    }
  }

  // TODO Should we add validation for POST without items?
  def submit(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val incorrectItems: Seq[FormError] = buildIncorrectItemsErrors(request)

    itemSummaryForm.bindFromRequest
      .fold(
        formWithErrors => Future.successful(BadRequest(itemsSummaryPage(formWithErrors, request.cacheModel.items.toList, incorrectItems))),
        validYesNo =>
          validYesNo.answer match {
            case YesNoAnswers.yes =>
              createNewItemInCache.map(itemId => navigator.continueTo(ProcedureCodesController.displayPage(itemId)))

            case YesNoAnswers.no if incorrectItems.nonEmpty =>
              Future.successful(BadRequest(itemsSummaryPage(itemSummaryForm.fill(validYesNo), request.cacheModel.items.toList, incorrectItems)))

            case YesNoAnswers.no =>
              Future.successful(navigator.continueTo(nextPage))
          }
      )
  }

  private def nextPage(implicit request: JourneyRequest[AnyContent]): Call =
    request.declarationType match {
      case SUPPLEMENTARY | STANDARD | CLEARANCE => TransportLeavingTheBorderController.displayPage

      case SIMPLIFIED | OCCASIONAL =>
        if (request.cacheModel.requiresWarehouseId) WarehouseIdentificationController.displayPage
        else supervisingCustomsOfficeHelper.landOnOrSkipToNextPage(request.cacheModel)
    }

  private def buildIncorrectItemsErrors(request: JourneyRequest[AnyContent]): Seq[FormError] =
    request.cacheModel.items.zipWithIndex.filterNot { case (item, _) =>
      item.isCompleted(request.declarationType)
    }.map { case (item, index) =>
      FormError("item_" + index, "declaration.itemsSummary.item.incorrect", Seq(item.sequenceId.toString))
    }

  private def createNewItemInCache(implicit request: JourneyRequest[AnyContent]): Future[String] = {
    val newItem = ExportItem(id = exportItemIdGeneratorService.generateItemId())
    val items = request.cacheModel.items :+ newItem.copy(sequenceId = request.cacheModel.items.size + 1)
    exportsCacheService.update(request.cacheModel.copy(items = items)).map(_ => newItem.id)
  }

  private def removeEmptyItems(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] = {
    val itemsWithAnswers = request.cacheModel.items.filter(ExportItem.containsAnswers)
    exportsCacheService.update(request.cacheModel.copy(items = itemsWithAnswers))
  }

  def displayRemoveItemConfirmationPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.itemBy(itemId) match {
      case Some(item) => Ok(removeItemPage(removeItemForm, item))
      case None       => navigator.continueTo(ItemsSummaryController.displayItemsSummaryPage)
    }
  }

  def removeItem(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    removeItemForm.bindFromRequest
      .fold(
        formWithErrors =>
          Future.successful(request.cacheModel.itemBy(itemId) match {
            case Some(item) => BadRequest(removeItemPage(formWithErrors, item))
            case None       => throw new IllegalStateException(s"Could not find ExportItem with id = [$itemId] to remove")
          }),
        _.answer match {
          case YesNoAnswers.yes =>
            removeItemFromCache(itemId).map(_ => navigator.continueTo(ItemsSummaryController.displayItemsSummaryPage))

          case YesNoAnswers.no =>
            Future.successful(navigator.continueTo(ItemsSummaryController.displayItemsSummaryPage))
        }
      )
  }

  private def removeItemFromCache(itemId: String)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    request.cacheModel.itemBy(itemId) match {
      case Some(itemToDelete) =>
        val updatedItems =
          request.cacheModel.items.filterNot(_ == itemToDelete).zipWithIndex.map { case (item, index) =>
            item.copy(sequenceId = index + 1)
          }

        val updatedModel = removeWarehouseIdentification(request.cacheModel.copy(items = updatedItems))
        exportsCacheService.update(updatedModel)

      case None => Future.successful(request.cacheModel)
    }

  private def removeWarehouseIdentification(declaration: ExportsDeclaration): ExportsDeclaration =
    if (declaration.isType(CLEARANCE) || declaration.requiresWarehouseId) declaration
    else declaration.copy(locations = declaration.locations.copy(warehouseIdentification = None))
}
