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
import forms.declaration.WarehouseIdentification
import javax.inject.Inject
import models.{ExportsDeclaration, Mode}
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.warehouse_identification

import scala.concurrent.{ExecutionContext, Future}

class WarehouseIdentificationController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  warehouseIdentificationPage: warehouse_identification
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  import forms.declaration.WarehouseIdentification._

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.locations.warehouseIdentification match {
      case Some(data) => Ok(warehouseIdentificationPage(mode, form().fill(data)))
      case _          => Ok(warehouseIdentificationPage(mode, form()))
    }
  }

  def saveWarehouse(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[WarehouseIdentification]) =>
          Future.successful(BadRequest(warehouseIdentificationPage(mode, formWithErrors))),
        form =>
          updateCache(form)
            .map(_ => navigator.continueTo(controllers.declaration.routes.BorderTransportController.displayPage(mode)))
      )
  }

  private def updateCache(
    formData: WarehouseIdentification
  )(implicit request: JourneyRequest[_]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect { model =>
      val updatedLocations = model.locations.copy(warehouseIdentification = Some(formData))
      model.copy(locations = updatedLocations)
    }
}
