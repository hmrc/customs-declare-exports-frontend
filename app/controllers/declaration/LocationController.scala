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

import connectors.CodeListConnector
import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import forms.declaration.GoodsLocationForm
import models.Mode
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.goods_location

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LocationController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  mcc: MessagesControllerComponents,
  goodsLocationPage: goods_location,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator
)(implicit ec: ExecutionContext, codeListConnector: CodeListConnector)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {
  import forms.declaration.GoodsLocationForm._

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form().withSubmissionErrors()
    request.cacheModel.locations.goodsLocation match {
      case Some(data) => Ok(goodsLocationPage(mode, frm.fill(data.toForm)))
      case _          => Ok(goodsLocationPage(mode, frm))
    }
  }

  def saveLocation(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[GoodsLocationForm]) => Future.successful(BadRequest(goodsLocationPage(mode, formWithErrors))),
        formData =>
          updateDeclarationFromRequest(model => model.copy(locations = model.locations.copy(goodsLocation = Some(formData.toModel())))).map { _ =>
            navigator.continueTo(mode, controllers.declaration.routes.OfficeOfExitController.displayPage)
        }
      )
  }
}
