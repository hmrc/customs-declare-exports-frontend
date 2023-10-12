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
import controllers.declaration.routes.{ItemsSummaryController, ProcedureCodesController, TransportLeavingTheBorderController}
import controllers.helpers.SequenceIdHelper
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.DeclarationType.CLEARANCE
import models.ExportsDeclaration
import models.declaration.ExportItem
import models.requests.JourneyRequest
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.{ExportItemIdGeneratorService, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declarationitems.{items_add_item, items_summary}

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
  sequenceIdHandler: SequenceIdHelper
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithUnsafeDefaultFormBinding {

  private def itemSummaryForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.itemsSummary.addAnotherItem.error.empty")

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
    request.declarationType match {
      case CLEARANCE =>
        itemSummaryForm.fill(YesNoAnswer(YesNoAnswers.no))
        Future.successful(navigator.continueTo(TransportLeavingTheBorderController.displayPage))
      case _ =>
        val incorrectItems: Seq[FormError] = buildIncorrectItemsErrors(request)

        itemSummaryForm
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(itemsSummaryPage(formWithErrors, request.cacheModel.items.toList, incorrectItems))),
            validYesNo =>
              validYesNo.answer match {
                case YesNoAnswers.yes =>
                  createNewItemInCache.map(itemId => navigator.continueTo(ProcedureCodesController.displayPage(itemId)))

                case YesNoAnswers.no if incorrectItems.nonEmpty =>
                  Future.successful(BadRequest(itemsSummaryPage(itemSummaryForm.fill(validYesNo), request.cacheModel.items.toList, incorrectItems)))

                case YesNoAnswers.no =>
                  Future.successful(navigator.continueTo(TransportLeavingTheBorderController.displayPage))
              }
          )
    }
  }

  private def buildIncorrectItemsErrors(request: JourneyRequest[AnyContent]): Seq[FormError] =
    request.cacheModel.items.zipWithIndex.filterNot { case (item, _) =>
      item.isCompleted(request.declarationType)
    }.map { case (item, index) =>
      FormError("item_" + index, "declaration.itemsSummary.item.incorrect", Seq(item.sequenceId.toString))
    }

  private def createNewItemInCache(implicit request: JourneyRequest[AnyContent]): Future[String] = {
    val newItemId = exportItemIdGeneratorService.generateItemId()
    val itemsToSequence = request.cacheModel.items :+ ExportItem(id = newItemId)
    val (itemsSequenced, updatedMeta) = sequenceIdHandler.handleSequencing(itemsToSequence, request.cacheModel.declarationMeta)

    exportsCacheService.update(request.cacheModel.copy(items = itemsSequenced, declarationMeta = updatedMeta)).map(_ => newItemId)
  }

  private def removeEmptyItems(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] = {
    val itemsWithAnswers = request.cacheModel.items.filter(ExportItem.containsAnswers)
    val (itemsSequenced, updatedMeta) = sequenceIdHandler.handleSequencing(itemsWithAnswers, request.cacheModel.declarationMeta)
    exportsCacheService.update(request.cacheModel.copy(items = itemsSequenced, declarationMeta = updatedMeta))
  }

}
