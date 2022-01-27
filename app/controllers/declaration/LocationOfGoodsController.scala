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
import controllers.helpers.LocationOfGoodsHelper.skipLocationOfGoods
import controllers.navigation.Navigator
import controllers.routes.RootController
import forms.declaration.LocationOfGoods
import models.Mode
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.location_of_goods

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LocationOfGoodsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  mcc: MessagesControllerComponents,
  locationOfGoods: location_of_goods,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator
)(implicit ec: ExecutionContext, codeListConnector: CodeListConnector)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (skipLocationOfGoods(request.cacheModel)) Results.Redirect(RootController.displayPage)
    else {
      val form = LocationOfGoods.form.withSubmissionErrors
      request.cacheModel.locations.goodsLocation match {
        case Some(data) => Ok(locationOfGoods(mode, form.fill(data.toForm)))
        case _          => Ok(locationOfGoods(mode, form))
      }
    }
  }

  def saveLocation(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    if (skipLocationOfGoods(request.cacheModel)) Future.successful(Results.Redirect(RootController.displayPage))
    else {
      LocationOfGoods.form
        .bindFromRequest
        .fold(
          (formWithErrors: Form[LocationOfGoods]) => Future.successful(BadRequest(locationOfGoods(mode, formWithErrors))),
          formData =>
            updateDeclarationFromRequest(model => model.copy(locations = model.locations.copy(goodsLocation = Some(formData.toModel())))).map { _ =>
              navigator.continueTo(mode, controllers.declaration.routes.OfficeOfExitController.displayPage)
          }
        )
    }
  }
}
