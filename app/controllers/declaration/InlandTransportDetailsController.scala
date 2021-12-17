/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.declaration.routes.{DepartureTransportController, ExpressConsignmentController, TransportContainerController}
import controllers.helpers.TransportSectionHelper.isPostalOrFTIModeOfTransport
import controllers.navigation.Navigator
import forms.declaration.InlandModeOfTransportCode
import forms.declaration.InlandModeOfTransportCode._
import models.DeclarationType.{DeclarationType, SUPPLEMENTARY}
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.inland_transport_details

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InlandTransportDetailsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  inlandTransportDetailsPage: inland_transport_details
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  private val validJourneys = Seq(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY)

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType(validJourneys)) { implicit request =>
    val frm = form.withSubmissionErrors
    request.cacheModel.locations.inlandModeOfTransportCode match {
      case Some(data) => Ok(inlandTransportDetailsPage(mode, frm.fill(data)))
      case _          => Ok(inlandTransportDetailsPage(mode, frm))
    }
  }

  def submit(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    InlandModeOfTransportCode
      .form
      .bindFromRequest
      .fold(
        formWithErrors => Future.successful(BadRequest(inlandTransportDetailsPage(mode, formWithErrors))),
        code => updateCache(code).map(_ => navigator.continueTo(mode, nextPage(request.declarationType, code)))
      )
  }

  private def nextPage(declarationType: DeclarationType, code: InlandModeOfTransportCode): Mode => Call =
    if (isPostalOrFTIModeOfTransport(code.inlandModeOfTransportCode))
      declarationType match {
        case SUPPLEMENTARY => TransportContainerController.displayContainerSummary
        case _             => ExpressConsignmentController.displayPage
      }
    else DepartureTransportController.displayPage

  private def updateCache(code: InlandModeOfTransportCode)(implicit request: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => model.copy(locations = model.locations.copy(inlandModeOfTransportCode = Some(code))))
}
