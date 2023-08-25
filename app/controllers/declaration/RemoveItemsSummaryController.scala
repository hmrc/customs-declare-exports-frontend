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

import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.routes.{ItemsSummaryController, SummaryController}
import controllers.helpers.SequenceIdHelper
import controllers.navigation.Navigator
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import handlers.ErrorHandler
import models.DeclarationType.CLEARANCE
import models.ExportsDeclaration
import models.declaration.ExportItem
import models.declaration.submissions.Submission
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declarationitems.{items_cannot_remove, items_remove_item}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveItemsSummaryController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  exportsCacheService: ExportsCacheService,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  navigator: Navigator,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  itemsCannotRemovePage: items_cannot_remove,
  removeItemPage: items_remove_item,
  sequenceIdHandler: SequenceIdHelper
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithUnsafeDefaultFormBinding {

  private def removeItemForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.itemsRemove.error.empty")

  def displayRemoveItemConfirmationPage(itemId: String, fromSummary: Boolean = false): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit request =>
      request.cacheModel.itemWithIndexBy(itemId) match {
        case Some((item, idx)) if request.cacheModel.isAmendmentDraft =>
          viewToRemoveItem(
            item = item,
            remove = Ok(removeItemPage(removeItemForm, item, idx, fromSummary)),
            cannotRemove = submission => Ok(itemsCannotRemovePage(item, idx, submission.uuid, fromSummary))
          )

        case Some((item, idx)) => Future.successful(Ok(removeItemPage(removeItemForm, item, idx, fromSummary)))

        case _ => Future.successful(continueTo(fromSummary))
      }
  }

  def removeItem(itemId: String, fromSummary: Boolean = false): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    removeItemForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          request.cacheModel.itemWithIndexBy(itemId) match {
            case Some((item, idx)) if request.cacheModel.isAmendmentDraft =>
              viewToRemoveItem(
                item = item,
                remove = BadRequest(removeItemPage(formWithErrors, item, idx, fromSummary)),
                cannotRemove = submission => BadRequest(itemsCannotRemovePage(item, idx, submission.uuid, fromSummary))
              )

            case Some((item, idx)) => Future.successful(BadRequest(removeItemPage(formWithErrors, item, idx, fromSummary)))

            case _ => errorHandler.internalError(s"Could not find ExportItem with id = [$itemId] to remove")
          },
        _.answer match {
          case YesNoAnswers.yes => removeItemFromCache(itemId).map(_ => continueTo(fromSummary))
          case YesNoAnswers.no  => Future.successful(continueTo(fromSummary))
        }
      )
  }

  private def continueTo(fromSummary: Boolean)(implicit request: JourneyRequest[AnyContent]): Result =
    navigator.continueTo(if (fromSummary) SummaryController.displayPage else ItemsSummaryController.displayItemsSummaryPage)

  private def viewToRemoveItem(item: ExportItem, remove: Result, cannotRemove: Submission => Result)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    findParentDeclaration flatMap {
      case Some(parentDeclaration) =>
        customsDeclareExportsConnector.findSubmissionByLatestDecId(parentDeclaration.id) flatMap {
          case Some(submission) => Future.successful(canItemBeRemoved(item, parentDeclaration, remove, cannotRemove(submission)))
          case _                => errorHandler.internalError(noSubmissionErrorMsg(parentDeclaration.id))
        }
      case _ => errorHandler.internalError(noParentDecIdMsg)
    }

  private def findParentDeclaration(implicit request: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    request.cacheModel.declarationMeta.parentDeclarationId match {
      case Some(parentDecId) => customsDeclareExportsConnector.findDeclaration(parentDecId)
      case _                 => Future.successful(None)
    }

  private def canItemBeRemoved(item: ExportItem, parentDec: ExportsDeclaration, remove: Result, cannotRemove: Result): Result =
    if (!parentDec.items.map(_.sequenceId).contains(item.sequenceId)) remove
    else cannotRemove

  private def removeItemFromCache(itemId: String)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    request.cacheModel.itemBy(itemId) match {
      case Some(itemToDelete) =>
        val filteredItems = request.cacheModel.items.filterNot(_.id == itemToDelete.id)
        val (updatedItems, updatedMeta) = sequenceIdHandler.handleSequencing(filteredItems, request.cacheModel.declarationMeta)
        val updatedModel = removeWarehouseIdentification(request.cacheModel.copy(items = updatedItems, declarationMeta = updatedMeta))
        exportsCacheService.update(updatedModel)
      case None => Future.successful(request.cacheModel)
    }

  private def removeWarehouseIdentification(declaration: ExportsDeclaration): ExportsDeclaration =
    if (declaration.isType(CLEARANCE) || declaration.requiresWarehouseId) declaration
    else declaration.copy(locations = declaration.locations.copy(warehouseIdentification = None))

  private def noSubmissionErrorMsg(parentDeclarationId: String) =
    s"Could not find submission from latestDecId [${parentDeclarationId}]"

  private def noParentDecIdMsg(implicit request: JourneyRequest[AnyContent]) =
    s"Could not find parentDecId from declaration [${request.cacheModel.id}]"
}
