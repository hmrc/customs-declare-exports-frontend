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
import controllers.util.{Add, FormAction, SaveAndContinue, SaveAndReturn}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import javax.inject.Inject
import models.{ExportsDeclaration, Mode}
import models.declaration.ExportItem
import models.requests.JourneyRequest
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.{ExportItemIdGeneratorService, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.declarationitems.{items_add_item, items_summary}

import scala.concurrent.{ExecutionContext, Future}

class ItemsSummaryController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  exportItemIdGeneratorService: ExportItemIdGeneratorService,
  mcc: MessagesControllerComponents,
  addItemPage: items_add_item,
  itemsSummaryPage: items_summary
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  private def yesNoForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.itemsSummary.addAnotherItem.error.empty")

  def displayAddItemPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (request.cacheModel.items.isEmpty)
      Ok(addItemPage(mode))
    else
      navigator.continueTo(mode, controllers.declaration.routes.ItemsSummaryController.displayItemsSummaryPage)
  }

  def addFirstItem(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    createNewItemInCache()
      .map(newItem => navigator.continueTo(mode, controllers.declaration.routes.ProcedureCodesController.displayPage(_, newItem.id)))
  }

  def displayItemsSummaryPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (request.cacheModel.items.isEmpty)
      navigator.continueTo(mode, controllers.declaration.routes.ItemsSummaryController.displayAddItemPage)
    else
      Ok(itemsSummaryPage(mode, yesNoForm, request.cacheModel.items.toList))
  }

  //TODO Should we add validation for POST without items?
  def submit(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val incorrectItems: Seq[FormError] = buildIncorrectItemsErrors(request)

    yesNoForm
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(itemsSummaryPage(mode, formWithErrors, request.cacheModel.items.toList, incorrectItems))),
        validYesNo =>
          validYesNo.answer match {
            case YesNoAnswers.yes =>
              createNewItemInCache()
                .map(newItem => navigator.continueTo(mode, controllers.declaration.routes.ProcedureCodesController.displayPage(_, newItem.id)))

            case YesNoAnswers.no if incorrectItems.nonEmpty =>
              Future.successful(BadRequest(itemsSummaryPage(mode, yesNoForm.fill(validYesNo), request.cacheModel.items.toList, incorrectItems)))

            case YesNoAnswers.no =>
              Future.successful(navigator.continueTo(mode, controllers.declaration.routes.WarehouseIdentificationController.displayPage))
        }
      )
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

  def removeItem(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    request.cacheModel.itemBy(itemId) match {
      case Some(itemToDelete) =>
        val updatedItems =
          request.cacheModel.copy(items = request.cacheModel.items.filterNot(_.equals(itemToDelete))).items.zipWithIndex.map {
            case (item, index) => item.copy(sequenceId = index + 1)
          }
        exportsCacheService.update(request.cacheModel.copy(items = updatedItems)).map { _ =>
          navigator.continueTo(mode, routes.ItemsSummaryController.displayItemsSummaryPage)
        }
      case _ =>
        Future.successful(Redirect(routes.ItemsSummaryController.displayItemsSummaryPage(mode)))
    }
  }
}
