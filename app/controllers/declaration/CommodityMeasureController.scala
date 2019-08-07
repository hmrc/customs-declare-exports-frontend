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

import controllers.actions.{AuthAction, JourneyAction}
import forms.declaration.CommodityMeasure
import forms.declaration.CommodityMeasure.{form, _}
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.{ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.goods_measure

import scala.concurrent.{ExecutionContext, Future}

class CommodityMeasureController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  goodsMeasurePage: goods_measure
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayPage(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    exportsCacheService.getItemByIdAndSession(itemId, journeySessionId).map { item =>
      val packageInformation = item.map(_.packageInformation)
      val commodityMeasure = item.flatMap(_.commodityMeasure)

      (packageInformation, commodityMeasure) match {
        case (Some(p), Some(data)) if p.nonEmpty => Ok(goodsMeasurePage(itemId, form().fill(data)))
        case (Some(p), _) if p.nonEmpty          => Ok(goodsMeasurePage(itemId, form()))
        case _ =>
          BadRequest(goodsMeasurePage(itemId, form().withGlobalError("supplementary.commodityMeasure.global.addOne")))
      }
    }
  }

  def submitForm(itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[CommodityMeasure]) =>
          Future.successful(BadRequest(goodsMeasurePage(itemId, formWithErrors))),
        validForm =>
          updateExportsCache(itemId, journeySessionId, validForm).map { _ =>
            Redirect(controllers.declaration.routes.AdditionalInformationController.displayPage(itemId))
        }
      )
  }

  private def updateExportsCache(
    itemId: String,
    sessionId: String,
    updatedItem: CommodityMeasure
  ): Future[Option[ExportsCacheModel]] =
    getAndUpdateExportCacheModel(
      sessionId,
      model => {
        val itemList = model.items
          .find(item => item.id.equals(itemId))
          .map(_.copy(commodityMeasure = Some(updatedItem)))
          .fold(model.items)(model.items.filter(item => !item.id.equals(itemId)) + _)

        exportsCacheService.update(sessionId, model.copy(items = itemList))
      }
    )
}
