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
import controllers.declaration.routes.AdditionalInformationRequiredController
import controllers.navigation.Navigator
import forms.declaration.commodityMeasure.SupplementaryUnits
import javax.inject.Inject
import models.declaration.{CommodityMeasure, ExportItem}
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.commodityMeasure.supplementary_units

class SupplementaryUnitsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  supplementaryUnitsPage: supplementary_units
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val formWithDataIfAny = request.cacheModel.itemBy(itemId).flatMap(_.commodityMeasure) match {
      case Some(commodityMeasure) if hasSupplementaryUnits(commodityMeasure) => form.fill(SupplementaryUnits(commodityMeasure))
      case _                                                                 => form
    }

    Ok(supplementaryUnitsPage(mode, itemId, formWithDataIfAny))
  }

  def submitPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form.bindFromRequest
      .fold(formWithErrors => Future.successful(BadRequest(supplementaryUnitsPage(mode, itemId, formWithErrors))), updateExportsCache(itemId, _).map {
        _ =>
          navigator.continueTo(mode, AdditionalInformationRequiredController.displayPage(_, itemId))
      })
  }

  private def form(implicit request: JourneyRequest[_]): Form[SupplementaryUnits] =
    SupplementaryUnits.form.withSubmissionErrors

  private def hasSupplementaryUnits(commodityMeasure: CommodityMeasure): Boolean =
    commodityMeasure.supplementaryUnits.isDefined || commodityMeasure.supplementaryUnitsNotRequired.isDefined

  private def updateExportsCache(itemId: String, updatedItem: SupplementaryUnits)(
    implicit r: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect {
      _.updatedItem(itemId, item => item.copy(commodityMeasure = updateCommodityMeasure(item, updatedItem)))
    }

  private def updateCommodityMeasure(item: ExportItem, updatedItem: SupplementaryUnits): Option[CommodityMeasure] =
    item.commodityMeasure match {
      case Some(commodityMeasure) =>
        Some(
          CommodityMeasure(
            updatedItem.supplementaryUnits,
            updatedItem.supplementaryUnits.fold(Some(true))(_ => Some(false)),
            commodityMeasure.grossMass,
            commodityMeasure.netMass
          )
        )

      case _ =>
        Some(CommodityMeasure(updatedItem.supplementaryUnits, updatedItem.supplementaryUnits.map(_ => false), None, None))
    }
}
