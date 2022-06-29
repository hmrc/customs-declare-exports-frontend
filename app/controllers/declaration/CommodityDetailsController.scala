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
import controllers.navigation.Navigator
import forms.declaration.CommodityDetails
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.commodity_details

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommodityDetailsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  commodityDetailsPage: commodity_details
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val form = CommodityDetails.form(request.declarationType).withSubmissionErrors
    request.cacheModel.itemBy(itemId).flatMap(_.commodityDetails) match {
      case Some(commodityDetails) => Ok(commodityDetailsPage(mode, itemId, form.fill(commodityDetails)))
      case _                      => Ok(commodityDetailsPage(mode, itemId, form))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    CommodityDetails
      .form(request.declarationType)
      .bindFromRequest
      .fold(
        formWithErrors => Future.successful(BadRequest(commodityDetailsPage(mode, itemId, formWithErrors))),
        commodityDetails => updateExportsCache(itemId, trimCommodityCode(commodityDetails)).map(_ => nextPage(mode, itemId))
      )
  }

  private def nextPage(mode: Mode, itemId: String)(implicit request: JourneyRequest[AnyContent]): Result =
    if (request.isType(DeclarationType.CLEARANCE) && request.cacheModel.isNotExs) {
      if (request.cacheModel.itemBy(itemId).exists(_.isExportInventoryCleansingRecord))
        navigator.continueTo(mode, controllers.declaration.routes.CommodityMeasureController.displayPage(_, itemId))
      else
        navigator.continueTo(mode, routes.PackageInformationSummaryController.displayPage(_, itemId))
    } else {
      navigator.continueTo(mode, routes.UNDangerousGoodsCodeController.displayPage(_, itemId))
    }

  private def updateExportsCache(itemId: String, commodityDetails: CommodityDetails)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { declaration =>
      val postFormAppliedModel = declaration.updatedItem(itemId, item => item.copy(commodityDetails = Some(commodityDetails)))

      if (!postFormAppliedModel.isCommodityCodeOfItemPrefixedWith(itemId, CommodityDetails.commodityCodeChemicalPrefixes))
        postFormAppliedModel.updatedItem(itemId, item => item.copy(cusCode = None))
      else
        postFormAppliedModel
    }

  private def trimCommodityCode(commodityDetails: CommodityDetails): CommodityDetails =
    commodityDetails.copy(combinedNomenclatureCode = commodityDetails.combinedNomenclatureCode.map(_.trim))
}
