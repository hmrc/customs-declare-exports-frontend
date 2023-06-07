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

package controllers.navigation

import controllers.declaration.routes
import controllers.helpers.ErrorFixModeHelper.{inErrorFixMode, setErrorFixMode}
import controllers.helpers._
import controllers.routes.RejectedNotificationsController
import forms.DeclarationPage
import forms.declaration._
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import models.requests.SessionHelper.{getValue, submissionActionId}
import play.api.mvc.{AnyContent, Call, Result, Results}
import services.TariffApiService.SupplementaryUnitsNotRequired
import services.{TaggedAuthCodes, TariffApiService}

import javax.inject.{Inject, Singleton}
import scala.annotation.nowarn
import scala.concurrent.{ExecutionContext, Future}

case class ItemId(id: String)

@Singleton
class Navigator @Inject() (
  val taggedAuthCodes: TaggedAuthCodes,
  tariffApiService: TariffApiService,
  val inlandOrBorderHelper: InlandOrBorderHelper,
  val supervisingCustomsOfficeHelper: SupervisingCustomsOfficeHelper
) extends CommonNavigator with StandardNavigator with OccasionalNavigator with SimplifiedNavigator with SupplementaryNavigator
    with ClearanceNavigator {

  def continueTo(factory: Call)(implicit request: JourneyRequest[AnyContent]): Result = {
    val formAction = FormAction.bindFromRequest()
    if (inErrorFixMode) handleErrorFixMode(factory, formAction)
    else if (formAction == SaveAndReturnToSummary) Results.Redirect(routes.SummaryController.displayPage)
    else Results.Redirect(factory)
  }

  private def handleErrorFixMode(factory: Call, formAction: FormAction)(implicit request: JourneyRequest[_]): Result =
    (formAction, request.cacheModel.declarationMeta.parentDeclarationId) match {
      case (SaveAndReturnToErrors, Some(parentId)) =>
        val call = getValue(submissionActionId).fold {
          RejectedNotificationsController.displayPage(parentId)
        } { actionId =>
          val draftDeclarationId = request.cacheModel.id
          RejectedNotificationsController.displayPageOnUnacceptedAmendment(actionId, Some(draftDeclarationId))
        }
        Results.Redirect(call)

      case (Add | Remove(_) | SaveAndContinue, _) => setErrorFixMode(Results.Redirect(factory))
      case _                                      => setErrorFixMode(Results.Redirect(factory).flashing(request.flash))
    }

  def backLink(page: DeclarationPage)(implicit request: JourneyRequest[_]): Call = {
    val specific: PartialFunction[DeclarationPage, Object] = request.declarationType match {
      case STANDARD      => standardCacheDependent.orElse(standard)
      case SUPPLEMENTARY => supplementaryCacheDependent.orElse(supplementary)
      case SIMPLIFIED    => simplifiedCacheDependent.orElse(simplified)
      case OCCASIONAL    => occasionalCacheDependent.orElse(occasional)
      case CLEARANCE     => clearanceCacheDependent.orElse(clearance)
    }

    (commonCacheDependent.orElse(common).orElse(specific)(page): @nowarn) match {
      case mapping: Call                                 => mapping
      case mapping: (ExportsDeclaration => Call) @nowarn => mapping(request.cacheModel)
    }
  }

  def backLinkForAdditionalInformation(
    page: DeclarationPage,
    itemId: String
  )(implicit request: JourneyRequest[_], ec: ExecutionContext): Future[Call] = {
    def pageSelection: Future[Call] =
      tariffApiService.retrieveCommodityInfoIfAny(request.cacheModel, itemId) map {
        case Left(SupplementaryUnitsNotRequired) => routes.CommodityMeasureController.displayPage(itemId)
        case _                                   => routes.SupplementaryUnitsController.displayPage(itemId)
      }

    page match {
      case AdditionalInformationSummary | AdditionalInformationRequired =>
        request.declarationType match {
          case STANDARD | SUPPLEMENTARY => pageSelection
          case _                        => Future.successful(backLink(page, ItemId(itemId)))
        }
      case _ => Future.successful(backLink(page, ItemId(itemId)))
    }
  }

  def backLink(page: DeclarationPage, itemId: ItemId)(implicit request: JourneyRequest[_]): Call = {
    val specific: PartialFunction[DeclarationPage, Object] = request.declarationType match {
      case STANDARD      => standardCacheItemDependent.orElse(standardItemPage)
      case SUPPLEMENTARY => supplementaryCacheItemDependent.orElse(supplementaryItemPage)
      case SIMPLIFIED    => simplifiedCacheItemDependent.orElse(simplifiedItemPage)
      case OCCASIONAL    => occasionalCacheItemDependent.orElse(occasionalItemPage)
      case CLEARANCE     => clearanceCacheItemDependent.orElse(clearanceItemPage)
    }
    (commonCacheItemDependent.orElse(commonItem).orElse(specific)(page): @nowarn) match {
      case mapping: (String => Call) @nowarn                       => mapping(itemId.id)
      case mapping: ((ExportsDeclaration, String) => Call) @nowarn => mapping(request.cacheModel, itemId.id)
    }
  }
}
