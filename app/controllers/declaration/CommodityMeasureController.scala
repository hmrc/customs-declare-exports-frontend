/*
 * Copyright 2019 HM Revenue & Customs
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

import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction}
import controllers.util.CacheIdGenerator.goodsItemCacheId
import forms.declaration.CommodityMeasure.{commodityFormId, form, _}
import forms.declaration.PackageInformation.formId
import forms.declaration.{CommodityMeasure, PackageInformation}
import javax.inject.Inject
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import services.cache.{ExportItem, ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.goods_measure

import scala.concurrent.{ExecutionContext, Future}

class CommodityMeasureController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  legacyCacheService: CustomsCacheService,
  exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  goodsMeasurePage: goods_measure
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends {
  val cacheService = exportsCacheService
} with FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    exportsCacheService.getItemByIdAndSession(itemId, journeySessionId).map(_.map(_.packageInformation)).flatMap {
      case Some(p) if p.nonEmpty =>
        exportsCacheService.getItemByIdAndSession(itemId, journeySessionId).map(_.flatMap(_.commodityMeasure)).map {
          case Some(data) => Ok(goodsMeasurePage(itemId, form().fill(data)))
          case _          => Ok(goodsMeasurePage(itemId, form()))
        }
      case _ => Future.successful(BadRequest(goodsMeasurePage(itemId, form().withGlobalError(ADD_ONE))))
    }
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[CommodityMeasure]) =>
          Future.successful(BadRequest(goodsMeasurePage(itemId, formWithErrors))),
        validForm =>
          updateCacheModels(itemId, validForm).map { _ =>
            Redirect(controllers.declaration.routes.AdditionalInformationController.displayPage(itemId))
        }
      )
  }

  private def updateCacheModels(itemId: String, updatedCache: CommodityMeasure)(
    implicit journeyRequest: JourneyRequest[_]
  ) =
    for {
      _ <- updateExportsCache(itemId, journeySessionId, updatedCache)
      _ <- legacyCacheService.cache[CommodityMeasure](goodsItemCacheId, commodityFormId, updatedCache)
    } yield ()

  private def updateExportsCache(
    itemId: String,
    sessionId: String,
    updatedItem: CommodityMeasure
  ): Future[Option[ExportsCacheModel]] =
    getAndUpdateExportCacheModel(
      sessionId,
      model => {
        val item: Option[ExportItem] = model.items
          .find(item => item.id.equals(itemId))
          .map(_.copy(commodityMeasure = Some(updatedItem)))
        val itemList = item.fold(model.items)(model.items.filter(item => !item.id.equals(itemId)) + _)
        exportsCacheService.update(sessionId, model.copy(items = itemList))
      }
    )
}
