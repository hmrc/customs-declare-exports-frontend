/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.section5

import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.ErrorHandler
import controllers.helpers.SequenceIdHelper.handleSequencing
import controllers.section5.routes.ItemsSummaryController
import controllers.summary.routes.{SectionSummaryController, SummaryController}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.DeclarationType.CLEARANCE
import models.ExportsDeclaration
import models.declaration.ExportItem
import models.declaration.submissions.Submission
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.http.HeaderNames
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl._
import uk.gov.hmrc.play.bootstrap.binders.{OnlyRelative, RedirectUrl}
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section5.items.{items_cannot_remove, items_remove_item}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveItemsSummaryController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  exportsCacheService: ExportsCacheService,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  itemsCannotRemovePage: items_cannot_remove,
  removeItemPage: items_remove_item
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithUnsafeDefaultFormBinding {

  private val removeItemForm: Form[YesNoAnswer] = YesNoAnswer.form(errorKey = "declaration.itemsRemove.error.empty")

  def displayRemoveItemConfirmationPage(itemId: String): Action[AnyContent] =
    (authenticate andThen journeyType).async { implicit request =>
      val referrer = referralPage(request.headers.get(HeaderNames.REFERER))
      request.cacheModel.itemWithIndexBy(itemId) match {
        case Some((item, idx)) if request.cacheModel.isAmendmentDraft =>
          viewToRemoveItem(
            item = item,
            remove = Ok(removeItemPage(removeItemForm, item, idx, referrer)),
            cannotRemove = submission => Ok(itemsCannotRemovePage(item, idx, submission.uuid, referrer))
          )

        case Some((item, idx)) => Future.successful(Ok(removeItemPage(removeItemForm, item, idx, referrer)))

        case _ => Future.successful(Redirect(referrer))
      }
    }

  def removeItem(itemId: String, redirectUrl: RedirectUrl): Action[AnyContent] =
    (authenticate andThen journeyType).async { implicit request =>
      val referrer = referralPage(Some(redirectUrl.get(OnlyRelative).url))
      removeItemForm
        .bindFromRequest()
        .fold(
          formWithErrors =>
            request.cacheModel.itemWithIndexBy(itemId) match {
              case Some((item, idx)) if request.cacheModel.isAmendmentDraft =>
                viewToRemoveItem(
                  item = item,
                  remove = BadRequest(removeItemPage(formWithErrors, item, idx, referrer)),
                  cannotRemove = submission => BadRequest(itemsCannotRemovePage(item, idx, submission.uuid, referrer))
                )

              case Some((item, idx)) => Future.successful(BadRequest(removeItemPage(formWithErrors, item, idx, referrer)))

              case _ => errorHandler.internalError(s"Could not find ExportItem with id = [$itemId] to remove")
            },
          _.answer match {
            case YesNoAnswers.yes =>
              removeItemFromCache(itemId).map { _ =>
                Redirect(SectionSummaryController.displayPage(5))
              }

            case YesNoAnswers.no => Future.successful(Redirect(referrer))
          }
        )
    }

  private def viewToRemoveItem(item: ExportItem, remove: Result, cannotRemove: Submission => Result)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    findParentDeclaration flatMap {
      case Some(parentDeclaration) =>
        customsDeclareExportsConnector.findSubmission(parentDeclaration.declarationMeta.associatedSubmissionId.getOrElse("MISSING")) flatMap {
          case Some(submission) => Future.successful(canItemBeRemoved(item, parentDeclaration, remove, cannotRemove(submission)))
          case _                => errorHandler.internalError(noSubmissionErrorMsg(parentDeclaration.declarationMeta.associatedSubmissionId))
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
        val (updatedItems, updatedMeta) = handleSequencing(filteredItems, request.cacheModel.declarationMeta)
        val updatedModel = removeWarehouseIdentification(request.cacheModel.copy(items = updatedItems, declarationMeta = updatedMeta))
        exportsCacheService.update(updatedModel, request.eori)

      case None => Future.successful(request.cacheModel)
    }

  private def removeWarehouseIdentification(declaration: ExportsDeclaration): ExportsDeclaration =
    if (declaration.isType(CLEARANCE) || declaration.requiresWarehouseId) declaration
    else declaration.copy(locations = declaration.locations.copy(warehouseIdentification = None))

  private def noSubmissionErrorMsg(maybeAssociatedSubmissionId: Option[String]) =
    s"Could not find submission for associatedSubmissionId [${maybeAssociatedSubmissionId}]"

  private def noParentDecIdMsg(implicit request: JourneyRequest[AnyContent]) =
    s"Could not find parentDecId from declaration [${request.cacheModel.id}]"

  private lazy val referrals = Map(
    ItemsSummaryController.displayItemsSummaryPage.url -> ItemsSummaryController.displayItemsSummaryPage,
    SectionSummaryController.displayPage(5).url -> SectionSummaryController.displayPage(5),
    SummaryController.displayPage.url -> SummaryController.displayPage
  )

  private def referralPage(maybeReferrer: Option[String]): Call =
    maybeReferrer.flatMap { referrer =>
      referrals.collectFirst {
        case (route, call) if referrer.endsWith(route) => call
      }
    }.getOrElse(SummaryController.displayPage)
}
