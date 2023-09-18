/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.declaration.routes.{BorderTransportController, ExpressConsignmentController, TransportCountryController}
import controllers.helpers.TransportSectionHelper
import controllers.helpers.TransportSectionHelper.isPostalOrFTIModeOfTransport
import controllers.navigation.Navigator
import controllers.routes.RootController
import forms.declaration.DepartureTransport
import forms.declaration.DepartureTransport.form
import forms.declaration.InlandOrBorder.Border
import models.DeclarationType.CLEARANCE
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.TransportCodeService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.helpers.DepartureTransportHelper
import views.html.declaration.departure_transport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DepartureTransportController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  implicit val transportCodeService: TransportCodeService,
  departureTransportHelper: DepartureTransportHelper,
  departureTransportPage: departure_transport
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    if (filterJourneyOnDestinationCountries) {
      if (isPostalOrFTIModeOfTransport(request.cacheModel.transportLeavingBorderCode)) Results.Redirect(RootController.displayPage)
      else {
        val frm = form(departureTransportHelper.transportCodes).withSubmissionErrors
        val transport = request.cacheModel.transport
        val formData = DepartureTransport(transport.meansOfTransportOnDepartureType, transport.meansOfTransportOnDepartureIDNumber)

        Ok(departureTransportPage(frm.fill(formData)))
      }
    } else navigator.continueTo(nextPage)
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    if (filterJourneyOnDestinationCountries) {
      val code = request.cacheModel.transportLeavingBorderCode

      if (isPostalOrFTIModeOfTransport(code)) Future.successful(Results.Redirect(RootController.displayPage))
      else
        form(departureTransportHelper.transportCodes)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(departureTransportPage(formWithErrors))),
            updateCache(_).map(_ => navigator.continueTo(nextPage))
          )
    } else Future.successful(navigator.continueTo(nextPage))
  }

  private def nextPage(implicit request: JourneyRequest[AnyContent]): Call =
    if (request.declarationType == CLEARANCE) ExpressConsignmentController.displayPage
    else if (request.cacheModel.isInlandOrBorder(Border)) TransportCountryController.displayPage
    else BorderTransportController.displayPage

  private def updateCache(formData: DepartureTransport)(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.updateDepartureTransport(formData))

  private def filterJourneyOnDestinationCountries(implicit request: JourneyRequest[AnyContent]): Boolean =
    !(request.cacheModel.locations.destinationCountry exists { country =>
      TransportSectionHelper.destinationCountriesSkipDeparture.contains(country)
    })

}
