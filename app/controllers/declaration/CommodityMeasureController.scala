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
import controllers.declaration.routes.{AdditionalInformationRequiredController, SupplementaryUnitsController}
import controllers.navigation.Navigator
import forms.declaration.commodityMeasure.CommodityMeasure
import javax.inject.Inject
import models.DeclarationType.CLEARANCE
import models.declaration.{ExportItem, CommodityMeasure => CM}
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.commodityMeasure.commodity_measure

class CommodityMeasureController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  commodityMeasurePage: commodity_measure
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.itemBy(itemId).flatMap(_.commodityMeasure) match {
      case Some(data) => Ok(commodityMeasurePage(mode, itemId, form.fill(CommodityMeasure(data))))
      case _          => Ok(commodityMeasurePage(mode, itemId, form))
    }
  }

  def submitPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val test = form.bindFromRequest
    test.fold(
        formWithErrors => Future.successful(BadRequest(commodityMeasurePage(mode, itemId, formWithErrors))),
        updateExportsCache(itemId, _).map(_ => navigator.continueTo(mode, nextPage(itemId)))
      )
  }

  private def form(implicit request: JourneyRequest[_]): Form[CommodityMeasure] =
    CommodityMeasure.form.withSubmissionErrors

  private def nextPage(itemId: String)(implicit request: JourneyRequest[_]): Mode => Call =
    if (request.declarationType == CLEARANCE) AdditionalInformationRequiredController.displayPage(_, itemId)
    else SupplementaryUnitsController.displayPage(_, itemId)

  private def updateExportsCache(
    itemId: String, updatedItem: CommodityMeasure)(implicit r: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect {
      _.updatedItem(itemId, item => item.copy(commodityMeasure = updateCM(item, updatedItem)))
    }

  private def updateCM(item: ExportItem, updatedItem: CommodityMeasure): Option[CM] =
    item.commodityMeasure match {
      case Some(cm) =>
        Some(CM(cm.supplementaryUnits, cm.supplementaryUnitsNotRequired, updatedItem.grossMass, updatedItem.netMass))

      case _ => Some(CM(None, None, updatedItem.grossMass, updatedItem.netMass))
    }
}
