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

package controllers.declaration

import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.routes.{CommodityMeasureController, CusCodeController, TaricCodeSummaryController}
import controllers.navigation.Navigator
import forms.declaration.UNDangerousGoodsCode.form
import forms.declaration.{CommodityDetails, UNDangerousGoodsCode}
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.un_dangerous_goods_code

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UNDangerousGoodsCodeController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  unDangerousGoodsCodePage: un_dangerous_goods_code
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form.withSubmissionErrors
    request.cacheModel.itemBy(itemId).flatMap(_.dangerousGoodsCode) match {
      case Some(dangerousGoodsCode) => Ok(unDangerousGoodsCodePage(itemId, frm.fill(dangerousGoodsCode)))
      case _                        => Ok(unDangerousGoodsCodePage(itemId, frm))
    }
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[UNDangerousGoodsCode]) => Future.successful(BadRequest(unDangerousGoodsCodePage(itemId, formWithErrors))),
        validForm =>
          updateExportsCache(itemId, validForm).map { _ =>
            redirectToNextPage(itemId)
          }
      )
  }

  private def redirectToNextPage(itemId: String)(implicit request: JourneyRequest[AnyContent]): Result =
    if (request.isType(DeclarationType.CLEARANCE)) {
      if (request.cacheModel.itemBy(itemId).exists(_.isExportInventoryCleansingRecord))
        navigator.continueTo(CommodityMeasureController.displayPage(itemId))
      else
        navigator.continueTo(routes.PackageInformationSummaryController.displayPage(itemId))
    } else if (request.cacheModel.isCommodityCodeOfItemPrefixedWith(itemId, CommodityDetails.commodityCodeChemicalPrefixes))
      navigator.continueTo(CusCodeController.displayPage(itemId))
    else
      navigator.continueTo(TaricCodeSummaryController.displayPage(itemId))

  private def updateExportsCache(itemId: String, updatedItem: UNDangerousGoodsCode)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.updatedItem(itemId, item => item.copy(dangerousGoodsCode = Some(updatedItem))))
}
