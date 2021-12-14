/*
 * Copyright 2021 HM Revenue & Customs
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

import scala.concurrent.{ExecutionContext, Future}
import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import controllers.helpers.{FormAction, SaveAndReturn, SupervisingCustomsOfficeHelper}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.DeclarationType.CLEARANCE

import javax.inject.Inject
import models.declaration.ExportItem
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.{ExportItemIdGeneratorService, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declarationitems.{items_add_item, items_remove_item, items_summary}

class ItemsSummaryController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  exportItemIdGeneratorService: ExportItemIdGeneratorService,
  mcc: MessagesControllerComponents,
  addItemPage: items_add_item,
  itemsSummaryPage: items_summary,
  removeItemPage: items_remove_item
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  private def itemSummaryForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.itemsSummary.addAnotherItem.error.empty")
  private def removeItemForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.itemsRemove.error.empty")

  def displayAddItemPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (request.cacheModel.items.isEmpty)
      Ok(addItemPage(mode))
    else
      navigator.continueTo(mode, routes.ItemsSummaryController.displayItemsSummaryPage)
  }

  def addFirstItem(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val actionTypeOpt = FormAction.bindFromRequest()

    actionTypeOpt match {
      case SaveAndReturn => Future.successful(navigator.continueTo(mode, routes.ItemsSummaryController.displayAddItemPage))
      case _ =>
        createNewItemInCache()
          .map(newItem => navigator.continueTo(mode, routes.ProcedureCodesController.displayPage(_, newItem.id)))
    }
  }

  def displayItemsSummaryPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    removeEmptyItems().map(updatedModel => {
      updatedModel.fold(navigator.continueTo(mode, routes.ItemsSummaryController.displayAddItemPage))(
        model =>
          if (model.items.isEmpty)
            navigator.continueTo(mode, routes.ItemsSummaryController.displayAddItemPage)
          else
            Ok(itemsSummaryPage(mode, itemSummaryForm, model.items.toList))
      )
    })
  }

  //TODO Should we add validation for POST without items?
  def submit(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val incorrectItems: Seq[FormError] = buildIncorrectItemsErrors(request)

    itemSummaryForm
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(itemsSummaryPage(mode, formWithErrors, request.cacheModel.items.toList, incorrectItems))),
        validYesNo =>
          validYesNo.answer match {
            case YesNoAnswers.yes =>
              createNewItemInCache()
                .map(newItem => navigator.continueTo(mode, routes.ProcedureCodesController.displayPage(_, newItem.id)))

            case YesNoAnswers.no if incorrectItems.nonEmpty =>
              Future.successful(BadRequest(itemsSummaryPage(mode, itemSummaryForm.fill(validYesNo), request.cacheModel.items.toList, incorrectItems)))

            case YesNoAnswers.no =>
              Future.successful(navigator.continueTo(mode, nextPage))
        }
      )
  }

  private def nextPage(implicit request: JourneyRequest[AnyContent]): Mode => Call =
    request.declarationType match {
      case DeclarationType.SUPPLEMENTARY | DeclarationType.STANDARD | CLEARANCE =>
        routes.TransportLeavingTheBorderController.displayPage

      case DeclarationType.SIMPLIFIED | DeclarationType.OCCASIONAL =>
        if (request.cacheModel.requiresWarehouseId) routes.WarehouseIdentificationController.displayPage
        else SupervisingCustomsOfficeHelper.landOnOrSkipToNextPage
    }

  private def buildIncorrectItemsErrors(request: JourneyRequest[AnyContent]): Seq[FormError] =
    request.cacheModel.items.zipWithIndex.filterNot { case (item, _) => item.isCompleted(request.declarationType) }.map {
      case (item, index) =>
        FormError("item_" + index, "declaration.itemsSummary.item.incorrect", Seq(item.sequenceId.toString))
    }

  private def createNewItemInCache()(implicit request: JourneyRequest[AnyContent]): Future[ExportItem] = {
    val newItem = ExportItem(id = exportItemIdGeneratorService.generateItemId())
    exportsCacheService
      .update(
        request.cacheModel
          .copy(items = request.cacheModel.items :+ newItem.copy(sequenceId = request.cacheModel.items.size + 1))
      )
      .map(_ => newItem)
  }

  private def removeEmptyItems()(implicit request: JourneyRequest[AnyContent]) = {
    val itemsWithAnswers = request.cacheModel.items.filter(ExportItem.containsAnswers)
    exportsCacheService.update(request.cacheModel.copy(items = itemsWithAnswers))
  }

  def displayRemoveItemConfirmationPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.itemBy(itemId) match {
      case Some(item) => Ok(removeItemPage(mode, removeItemForm, item))
      case None       => navigator.continueTo(mode, routes.ItemsSummaryController.displayItemsSummaryPage)
    }
  }

  def removeItem(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    removeItemForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(request.cacheModel.itemBy(itemId) match {
            case Some(item) => BadRequest(removeItemPage(mode, formWithErrors, item))
            case None       => throw new IllegalStateException(s"Could not find ExportItem with id = [$itemId] to remove")
          }),
        validYesNo =>
          validYesNo.answer match {
            case YesNoAnswers.yes =>
              removeItemFromCache(itemId).map { _ =>
                navigator.continueTo(mode, routes.ItemsSummaryController.displayItemsSummaryPage)
              }

            case YesNoAnswers.no =>
              Future.successful(navigator.continueTo(mode, routes.ItemsSummaryController.displayItemsSummaryPage))
        }
      )
  }

  private def removeItemFromCache(itemId: String)(implicit request: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    request.cacheModel.itemBy(itemId) match {
      case Some(itemToDelete) =>
        val updatedItems =
          request.cacheModel.items.filterNot(_ == itemToDelete).zipWithIndex.map {
            case (item, index) => item.copy(sequenceId = index + 1)
          }
        val updatedModel = removeWarehouseIdentification(request.cacheModel.copy(items = updatedItems))
        exportsCacheService.update(updatedModel)

      case None => Future.successful(None)
    }

  private def removeWarehouseIdentification(declaration: ExportsDeclaration): ExportsDeclaration =
    if (declaration.isType(CLEARANCE) || declaration.requiresWarehouseId)
      declaration
    else
      declaration.copy(locations = declaration.locations.copy(warehouseIdentification = None))
}
