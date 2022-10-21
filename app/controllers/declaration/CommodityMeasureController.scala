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
import controllers.declaration.routes.{AdditionalInformationRequiredController, SupplementaryUnitsController}
import controllers.navigation.Navigator
import forms.declaration.commodityMeasure.CommodityMeasure
import models.DeclarationType.{CLEARANCE, STANDARD, SUPPLEMENTARY}
import models.ExportsDeclaration
import models.declaration.{CommodityMeasure => CommodityMeasureModel, ExportItem}
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.commodityMeasure.commodity_measure

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommodityMeasureController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  commodityMeasurePage: commodity_measure
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  private val validTypes = Seq(STANDARD, SUPPLEMENTARY, CLEARANCE)

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType(validTypes)) { implicit request =>
    request.cacheModel.commodityMeasure(itemId) match {
      case Some(data) => Ok(commodityMeasurePage(itemId, form.fill(CommodityMeasure(data))))
      case _          => Ok(commodityMeasurePage(itemId, form))
    }
  }

  def submitPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType(validTypes)).async { implicit request =>
    val test = form.bindFromRequest
    test.fold(
      formWithErrors => Future.successful(BadRequest(commodityMeasurePage(itemId, formWithErrors))),
      updateExportsCache(itemId, _).map(_ => navigator.continueTo(nextPage(itemId)))
    )
  }

  private def form(implicit request: JourneyRequest[_]): Form[CommodityMeasure] =
    CommodityMeasure.form.withSubmissionErrors

  private def nextPage(itemId: String)(implicit request: JourneyRequest[_]): Call =
    if (request.declarationType == CLEARANCE) AdditionalInformationRequiredController.displayPage(itemId)
    else SupplementaryUnitsController.displayPage(itemId)

  private def updateExportsCache(itemId: String, updatedItem: CommodityMeasure)(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest {
      _.updatedItem(itemId, item => item.copy(commodityMeasure = updateComodityMeasureModel(item, updatedItem)))
    }

  private def updateComodityMeasureModel(item: ExportItem, updatedItem: CommodityMeasure): Option[CommodityMeasureModel] =
    item.commodityMeasure match {
      case Some(commodityMeasure) =>
        Some(
          CommodityMeasureModel(
            commodityMeasure.supplementaryUnits,
            commodityMeasure.supplementaryUnitsNotRequired,
            updatedItem.grossMass,
            updatedItem.netMass
          )
        )

      case _ => Some(CommodityMeasureModel(None, None, updatedItem.grossMass, updatedItem.netMass))
    }
}
