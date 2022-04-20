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

import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.routes.{BorderTransportController, ExpressConsignmentController, TransportCountryController}
import controllers.helpers.TransportSectionHelper.isPostalOrFTIModeOfTransport
import controllers.navigation.Navigator
import controllers.routes.RootController
import forms.declaration.DepartureTransport
import forms.declaration.InlandOrBorder.Border
import models.DeclarationType.{CLEARANCE, STANDARD, SUPPLEMENTARY}
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.helpers.DepartureTransportHelper
import views.html.declaration.departure_transport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DepartureTransportController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  departureTransportHelper: DepartureTransportHelper,
  departureTransportPage: departure_transport
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  private def form(implicit request: JourneyRequest[_]): Form[DepartureTransport] =
    DepartureTransport.form(departureTransportHelper.transportCodes)

  private val validTypes = Seq(STANDARD, SUPPLEMENTARY, CLEARANCE)

  def displayPage(mode: Mode): Action[AnyContent] =
    (authenticate andThen journeyType(validTypes)) { implicit request =>
      if (!isPostalOrFTIModeOfTransport(request.cacheModel.transportLeavingBorderCode)) {
        val frm = form.withSubmissionErrors
        val transport = request.cacheModel.transport
        val formData = DepartureTransport(transport.meansOfTransportOnDepartureType, transport.meansOfTransportOnDepartureIDNumber)

        Ok(departureTransportPage(mode, frm.fill(formData)))
      } else Results.Redirect(RootController.displayPage)
    }

  def submitForm(mode: Mode): Action[AnyContent] =
    (authenticate andThen journeyType(validTypes)).async { implicit request =>
      if (!isPostalOrFTIModeOfTransport(request.cacheModel.transportLeavingBorderCode))
        form.bindFromRequest
          .fold(
            formWithErrors => Future.successful(BadRequest(departureTransportPage(mode, formWithErrors))),
            updateCache(_).map(_ => navigator.continueTo(mode, nextPage))
          )
      else Future.successful(Results.Redirect(RootController.displayPage))
    }

  private def nextPage(implicit request: JourneyRequest[AnyContent]): Mode => Call =
    request.declarationType match {
      case CLEARANCE => ExpressConsignmentController.displayPage
      case STANDARD | SUPPLEMENTARY =>
        if (request.cacheModel.isInlandOrBorder(Border)) TransportCountryController.displayPage
        else BorderTransportController.displayPage
    }

  private def updateCache(formData: DepartureTransport)(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.updateDepartureTransport(formData))
}
