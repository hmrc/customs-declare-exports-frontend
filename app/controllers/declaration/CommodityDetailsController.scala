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
import models.DeclarationType.CLEARANCE
import models.ExportsDeclaration.isCodePrefixedWith
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.CatAndDogFurCommodityCodes.allCatAndDogFurCommCodes
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.commodity_details

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CommodityDetailsController @Inject() (
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
        commodityDetails => {
          val trimmedDetails = trimCommodityCode(commodityDetails)
          updateExportsCache(itemId, trimmedDetails).map(_ => nextPage(mode, itemId, trimmedDetails))
        }
      )
  }

  private def nextPage(mode: Mode, itemId: String, details: CommodityDetails)(implicit request: JourneyRequest[AnyContent]): Result = {
    val currentItem = request.cacheModel.itemBy(itemId)

    (request.declarationType, request.cacheModel.isNotExs) match {
      case (CLEARANCE, true) if currentItem.exists(_.isExportInventoryCleansingRecord) =>
        navigator.continueTo(mode, controllers.declaration.routes.CommodityMeasureController.displayPage(_, itemId))
      case (CLEARANCE, true) =>
        navigator.continueTo(mode, routes.PackageInformationSummaryController.displayPage(_, itemId))
      case (CLEARANCE, _) =>
        navigator.continueTo(mode, routes.UNDangerousGoodsCodeController.displayPage(_, itemId))
      case _ if allCatAndDogFurCommCodes.contains(details.combinedNomenclatureCode.getOrElse("")) =>
        navigator.continueTo(mode, routes.CatOrDogFurController.displayPage(_, itemId))
      case _ =>
        navigator.continueTo(mode, routes.UNDangerousGoodsCodeController.displayPage(_, itemId))
    }
  }

  private def updateExportsCache(itemId: String, commodityDetails: CommodityDetails)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { declaration =>
      val catOrDogFurDetails =
        if (allCatAndDogFurCommCodes.contains(commodityDetails.combinedNomenclatureCode.getOrElse("")))
          request.cacheModel.itemBy(itemId).flatMap(_.catOrDogFurDetails)
        else None
      val cusCode =
        if (isCodePrefixedWith(commodityDetails.combinedNomenclatureCode, CommodityDetails.commodityCodeChemicalPrefixes))
          request.cacheModel.itemBy(itemId).flatMap(_.cusCode)
        else None

      declaration
        .updatedItem(itemId, item => item.copy(commodityDetails = Some(commodityDetails), catOrDogFurDetails = catOrDogFurDetails, cusCode = cusCode))
    }

  private def trimCommodityCode(commodityDetails: CommodityDetails): CommodityDetails =
    commodityDetails.copy(combinedNomenclatureCode = commodityDetails.combinedNomenclatureCode.map(_.trim))
}
