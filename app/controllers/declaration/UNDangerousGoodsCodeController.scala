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
import controllers.navigation.{ItemId, Navigator}
import forms.declaration.UNDangerousGoodsCode.form
import forms.declaration.{CommodityDetails, UNDangerousGoodsCode}
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.un_dangerous_goods_code

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UNDangerousGoodsCodeController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  unDangerousGoodsCodePage: un_dangerous_goods_code
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val frm = form().withSubmissionErrors()
    resolveBackLink(mode, itemId).map { backLink =>
      request.cacheModel.itemBy(itemId).flatMap(_.dangerousGoodsCode) match {
        case Some(dangerousGoodsCode) => Ok(unDangerousGoodsCodePage(mode, itemId, frm.fill(dangerousGoodsCode), backLink))
        case _                        => Ok(unDangerousGoodsCodePage(mode, itemId, frm, backLink))
      }
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[UNDangerousGoodsCode]) =>
          resolveBackLink(mode, itemId).map { backLink =>
            BadRequest(unDangerousGoodsCodePage(mode, itemId, formWithErrors, backLink))
        },
        validForm =>
          updateExportsCache(itemId, validForm).map { _ =>
            redirectToNextPage(mode, itemId)
        }
      )
  }

  private def redirectToNextPage(mode: Mode, itemId: String)(implicit request: JourneyRequest[AnyContent]): Result =
    if (request.isType(DeclarationType.CLEARANCE)) {
      if (request.cacheModel.itemBy(itemId).exists(_.isExportInventoryCleansingRecord))
        navigator.continueTo(mode, controllers.declaration.routes.CommodityMeasureController.displayPage(_, itemId))
      else
        navigator.continueTo(mode, controllers.declaration.routes.PackageInformationSummaryController.displayPage(_, itemId))
    } else {
      if (request.cacheModel.isCommodityCodeOfItemPrefixedWith(itemId, CommodityDetails.commodityCodeChemicalPrefixes))
        navigator.continueTo(mode, controllers.declaration.routes.CusCodeController.displayPage(_, itemId))
      else
        navigator.continueTo(mode, controllers.declaration.routes.TaricCodeSummaryController.displayPage(_, itemId))
    }

  private def resolveBackLink(mode: Mode, itemId: String)(implicit request: JourneyRequest[AnyContent]): Future[Call] =
    navigator.backLink(UNDangerousGoodsCode, mode, ItemId(itemId))

  private def updateExportsCache(itemId: String, updatedItem: UNDangerousGoodsCode)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect { model =>
      model.updatedItem(itemId, item => item.copy(dangerousGoodsCode = Some(updatedItem)))
    }
}
