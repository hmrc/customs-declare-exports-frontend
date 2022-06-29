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

import scala.concurrent.{ExecutionContext, Future}
import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.routes.AdditionalInformationRequiredController
import controllers.navigation.Navigator
import forms.declaration.commodityMeasure.SupplementaryUnits

import javax.inject.Inject
import models.DeclarationType.{STANDARD, SUPPLEMENTARY}
import models.Mode
import models.declaration.{CommodityMeasure, ExportItem}
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.TariffApiService
import services.TariffApiService.{CommodityCodeNotFound, SupplementaryUnitsNotRequired}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.commodityMeasure.{supplementary_units, supplementary_units_yes_no}

class SupplementaryUnitsController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  tariffApiService: TariffApiService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  supplementaryUnitsPage: supplementary_units,
  supplementaryUnitsYesNoPage: supplementary_units_yes_no
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  private val validTypes = Seq(STANDARD, SUPPLEMENTARY)

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] =
    (authenticate andThen journeyType(validTypes)).async { implicit request =>
      def formWithDataIfAny(yesNoPage: Boolean): Form[SupplementaryUnits] =
        request.cacheModel.commodityMeasure(itemId) match {
          case Some(commodityMeasure) if hasSupplementaryUnits(commodityMeasure) =>
            form(yesNoPage).fill(SupplementaryUnits(commodityMeasure))

          case _ => form(yesNoPage)
        }

      tariffApiService.retrieveCommodityInfoIfAny(request.cacheModel, itemId).flatMap {
        case Right(commodityInfo) =>
          Future.successful(Ok(supplementaryUnitsPage(mode, itemId, formWithDataIfAny(false), commodityInfo)))

        case Left(CommodityCodeNotFound) =>
          Future.successful(Ok(supplementaryUnitsYesNoPage(mode, itemId, formWithDataIfAny(true))))

        case Left(SupplementaryUnitsNotRequired) =>
          updateExportsCacheAndContinueToNextPage(mode, itemId, SupplementaryUnits(None))
      }
    }

  def submitPage(mode: Mode, itemId: String): Action[AnyContent] =
    (authenticate andThen journeyType(validTypes)).async { implicit request =>
      tariffApiService.retrieveCommodityInfoIfAny(request.cacheModel, itemId).flatMap {
        case Right(commodityInfo) =>
          form(false).bindFromRequest.fold(
            formWithErrors => Future.successful(BadRequest(supplementaryUnitsPage(mode, itemId, formWithErrors, commodityInfo))),
            updateExportsCacheAndContinueToNextPage(mode, itemId, _)
          )

        case Left(CommodityCodeNotFound) =>
          form(true).bindFromRequest.fold(
            formWithErrors => Future.successful(BadRequest(supplementaryUnitsYesNoPage(mode, itemId, formWithErrors))),
            updateExportsCacheAndContinueToNextPage(mode, itemId, _)
          )

        case Left(SupplementaryUnitsNotRequired) =>
          updateExportsCacheAndContinueToNextPage(mode, itemId, SupplementaryUnits(None))
      }
    }

  private def form(yesNoPage: Boolean)(implicit request: JourneyRequest[_]): Form[SupplementaryUnits] =
    SupplementaryUnits.form(yesNoPage).withSubmissionErrors

  private def hasSupplementaryUnits(commodityMeasure: CommodityMeasure): Boolean =
    commodityMeasure.supplementaryUnits.isDefined || commodityMeasure.supplementaryUnitsNotRequired.isDefined

  private def updateExportsCacheAndContinueToNextPage(mode: Mode, itemId: String, newUnits: SupplementaryUnits)(
    implicit r: JourneyRequest[AnyContent]
  ): Future[Result] =
    updateDeclarationFromRequest {
      _.updatedItem(itemId, item => item.copy(commodityMeasure = updateCommodityMeasure(item, newUnits)))
    }.map { _ =>
      navigator.continueTo(mode, AdditionalInformationRequiredController.displayPage(_, itemId))
    }

  private def updateCommodityMeasure(item: ExportItem, newUnits: SupplementaryUnits): Option[CommodityMeasure] = {
    val notRequired = newUnits.supplementaryUnits.fold(Some(true))(_ => Some(false))
    item.commodityMeasure match {
      case Some(commodityMeasure) =>
        Some(CommodityMeasure(newUnits.supplementaryUnits, notRequired, commodityMeasure.grossMass, commodityMeasure.netMass))

      case _ => Some(CommodityMeasure(newUnits.supplementaryUnits, notRequired, None, None))
    }
  }
}
