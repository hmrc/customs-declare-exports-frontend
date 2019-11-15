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
import forms.declaration.WarehouseDetails
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.inland_transport_details

import scala.concurrent.{ExecutionContext, Future}

class InlandTransportDetailsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  inlandTransportDetailsPage: inland_transport_details
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  import forms.declaration.WarehouseDetails._

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.locations.warehouseIdentification match {
      case Some(data) => Ok(inlandTransportDetailsPage(mode, form().fill(data)))
      case _          => Ok(inlandTransportDetailsPage(mode, form()))
    }
  }

  def submit(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[WarehouseDetails]) => Future.successful(BadRequest(inlandTransportDetailsPage(mode, formWithErrors))),
        form => {
          val nextStep = request.cacheModel.`type` match {
            case DeclarationType.STANDARD | DeclarationType.SUPPLEMENTARY =>
              controllers.declaration.routes.DepartureTransportController.displayPage(mode)
            case DeclarationType.SIMPLIFIED =>
              controllers.declaration.routes.BorderTransportController.displayPage(mode)
          }
          updateCache(form)
            .map(_ => navigator.continueTo(nextStep))
        }
      )
  }

  private def updateCache(formData: WarehouseDetails)(implicit request: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect { model =>
      val warehouseDetails = model.locations.warehouseIdentification
        .map(
          dbWarehouseDetails =>
            WarehouseDetails(
              identificationNumber = dbWarehouseDetails.identificationNumber,
              supervisingCustomsOffice = dbWarehouseDetails.supervisingCustomsOffice,
              inlandModeOfTransportCode = formData.inlandModeOfTransportCode
          )
        )
        .getOrElse(WarehouseDetails(inlandModeOfTransportCode = formData.inlandModeOfTransportCode))
      model.copy(locations = model.locations.copy(warehouseIdentification = Some(warehouseDetails)))
    }
}
