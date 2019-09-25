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
import controllers.navigation.Navigator
import forms.declaration.CommodityMeasure
import forms.declaration.CommodityMeasure.form
import javax.inject.Inject
import models.{ExportsDeclaration, Mode}
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.goods_measure

import scala.concurrent.{ExecutionContext, Future}

class CommodityMeasureController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  goodsMeasurePage: goods_measure
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) {
    implicit request =>
      val item = request.cacheModel.itemBy(itemId)
      val packageInformation = item.map(_.packageInformation)
      val commodityMeasure = item.flatMap(_.commodityMeasure)

      (packageInformation, commodityMeasure) match {
        case (Some(p), Some(data)) if p.nonEmpty => Ok(goodsMeasurePage(mode, itemId, form().fill(data)))
        case (Some(p), _) if p.nonEmpty          => Ok(goodsMeasurePage(mode, itemId, form()))
        case _ =>
          BadRequest(
            goodsMeasurePage(mode, itemId, form().withGlobalError("supplementary.commodityMeasure.global.addOne"))
          )
      }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit request =>
      form()
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[CommodityMeasure]) =>
            Future.successful(BadRequest(goodsMeasurePage(mode, itemId, formWithErrors))),
          validForm =>
            updateExportsCache(itemId, validForm).map { _ =>
              navigator
                .continueTo(controllers.declaration.routes.AdditionalInformationController.displayPage(mode, itemId))
          }
        )
  }

  private def updateExportsCache(itemId: String, updatedItem: CommodityMeasure)(
    implicit r: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val itemList = model.items
        .find(item => item.id.equals(itemId))
        .map(_.copy(commodityMeasure = Some(updatedItem)))
        .fold(model.items)(model.items.filter(item => !item.id.equals(itemId)) + _)
      model.copy(items = itemList)
    })
}
