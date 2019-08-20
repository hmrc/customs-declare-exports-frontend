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
import forms.declaration.GoodsLocation
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.goods_location

import scala.concurrent.{ExecutionContext, Future}

class LocationController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  mcc: MessagesControllerComponents,
  goodsLocationPage: goods_location,
  override val exportsCacheService: ExportsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {
  import forms.declaration.GoodsLocation._

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.locations.goodsLocation match {
      case Some(data) => Ok(goodsLocationPage(form().fill(data)))
      case _          => Ok(goodsLocationPage(form()))
    }
  }

  def saveLocation(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[GoodsLocation]) => Future.successful(BadRequest(goodsLocationPage(formWithErrors))),
        formData =>
          updateExportsDeclarationSyncDirect(
            model =>
              model.copy(locations = model.locations.copy(goodsLocation = Some(formData)))
          ).map { _ =>
            Redirect(controllers.declaration.routes.OfficeOfExitController.displayForm())
        }
      )
  }

}
