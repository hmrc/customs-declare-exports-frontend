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
import forms.DeclarationPage
import forms.declaration.consignor.{ConsignorDetails, ConsignorEoriNumber}
import forms.declaration.{CarrierDetails, ExporterDetails, IsExs, RepresentativeAgent}
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{DeclarationType, Mode}
import models.DeclarationType.CLEARANCE
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.carrier_details

import scala.concurrent.{ExecutionContext, Future}

class CarrierDetailsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  carrierDetailsPage: carrier_details
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  private val validTypes = Seq(DeclarationType.STANDARD, DeclarationType.SIMPLIFIED, DeclarationType.OCCASIONAL, DeclarationType.CLEARANCE)

  private def form()(implicit request: JourneyRequest[_]) = CarrierDetails.form(request.declarationType)

  def displayPage(mode: Mode): Action[AnyContent] =
    (authenticate andThen journeyType(validTypes)) { implicit request =>
      request.cacheModel.parties.carrierDetails match {
        case Some(data) => Ok(carrierDetailsPage(mode, navigationPage, form().fill(data)))
        case _          => Ok(carrierDetailsPage(mode, navigationPage, form()))
      }
    }

  def saveAddress(mode: Mode): Action[AnyContent] =
    (authenticate andThen journeyType(validTypes)).async { implicit request =>
      form()
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[CarrierDetails]) => Future.successful(BadRequest(carrierDetailsPage(mode, navigationPage, formWithErrors))),
          form =>
            updateCache(form).map { _ =>
              navigator.continueTo(mode, controllers.declaration.routes.ConsigneeDetailsController.displayPage)
          }
        )
    }

  private def navigationPage(implicit request: JourneyRequest[_]): DeclarationPage =
    request.declarationType match {
      case CLEARANCE if request.cacheModel.parties.consignorDetails.flatMap(_.details.eori.map(_.value)).getOrElse("").nonEmpty => ConsignorDetails
      case CLEARANCE if request.cacheModel.parties.consignorDetails.flatMap(_.details.address).isDefined                        => RepresentativeAgent
      case _                                                                                                                    => CarrierDetails
    }

  private def updateCache(formData: CarrierDetails)(implicit req: JourneyRequest[AnyContent]) =
    updateExportsDeclarationSyncDirect(model => {
      val updatedParties = model.parties.copy(carrierDetails = Some(formData))
      model.copy(parties = updatedParties)
    })

}
