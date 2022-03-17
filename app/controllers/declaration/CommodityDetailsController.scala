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
import forms.declaration.CommodityDetails.form
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
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
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form(request.declarationType).withSubmissionErrors
    request.cacheModel.itemBy(itemId).flatMap(_.commodityDetails) match {
      case Some(commodityDetails) => Ok(commodityDetailsPage(mode, itemId, frm.fill(commodityDetails)))
      case _                      => Ok(commodityDetailsPage(mode, itemId, frm))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form(request.declarationType).bindFromRequest
      .fold(
        (formWithErrors: Form[CommodityDetails]) => Future.successful(BadRequest(commodityDetailsPage(mode, itemId, formWithErrors))),
        validForm => updateExportsCache(itemId, validForm).map(_ => redirectToNextPage(mode, itemId))
      )
  }

  private def redirectToNextPage(mode: Mode, itemId: String)(implicit request: JourneyRequest[AnyContent]): Result =
    if (request.isType(DeclarationType.CLEARANCE) && request.cacheModel.isNotExs) {
      if (request.cacheModel.itemBy(itemId).exists(_.isExportInventoryCleansingRecord))
        navigator.continueTo(mode, controllers.declaration.routes.CommodityMeasureController.displayPage(_, itemId))
      else
        navigator.continueTo(mode, routes.PackageInformationSummaryController.displayPage(_, itemId))
    } else {
      navigator.continueTo(mode, routes.UNDangerousGoodsCodeController.displayPage(_, itemId))
    }

  private def updateExportsCache(itemId: String, updatedItem: CommodityDetails)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { model =>
      val postFormAppliedModel = model.updatedItem(itemId, item => item.copy(commodityDetails = Some(updatedItem)))

      if (!postFormAppliedModel.isCommodityCodeOfItemPrefixedWith(itemId, CommodityDetails.commodityCodeChemicalPrefixes))
        postFormAppliedModel.updatedItem(itemId, item => item.copy(cusCode = None))
      else
        postFormAppliedModel
    }
}
