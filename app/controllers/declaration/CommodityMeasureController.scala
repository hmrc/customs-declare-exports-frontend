/*
 * Copyright 2020 HM Revenue & Customs
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
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.commodityMeasure.commodity_measure

import scala.concurrent.{ExecutionContext, Future}

class CommodityMeasureController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  commodityMeasurePage: commodity_measure
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  private def form()(implicit request: JourneyRequest[_]) = CommodityMeasure.form(request.declarationType).withSubmissionErrors()

  def displayPage(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.itemBy(itemId).flatMap(_.commodityMeasure) match {
      case Some(data) => Ok(commodityMeasurePage(mode, itemId, form().fill(data)))
      case _          => Ok(commodityMeasurePage(mode, itemId, form()))
    }
  }

  def submitForm(mode: Mode, itemId: String): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[CommodityMeasure]) => Future.successful(BadRequest(commodityMeasurePage(mode, itemId, formWithErrors))),
        validForm =>
          updateExportsCache(itemId, validForm).map { _ =>
            navigator
              .continueTo(mode, controllers.declaration.routes.AdditionalInformationRequiredController.displayPage(_, itemId))
        }
      )
  }

  private def updateExportsCache(itemId: String, updatedItem: CommodityMeasure)(
    implicit r: JourneyRequest[AnyContent]
  ): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      model.updatedItem(itemId, item => item.copy(commodityMeasure = Some(updatedItem)))
    })
}
