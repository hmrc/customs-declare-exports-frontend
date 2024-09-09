/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.section6

import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.helpers.TransportSectionHelper.skipDepartureTransport
import controllers.navigation.Navigator
import controllers.section6.routes._
import forms.section6.DepartureTransport
import forms.section6.DepartureTransport.form
import forms.section6.InlandOrBorder.Border
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.TransportCodeService
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.helpers.DepartureTransportHelper
import views.html.section6.departure_transport

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
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  val displayPage: Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val pageToDisplay = () => {
      val frm = form(departureTransportHelper.transportCodes).withSubmissionErrors
      val transport = request.cacheModel.transport
      val formData = DepartureTransport(transport.meansOfTransportOnDepartureType, transport.meansOfTransportOnDepartureIDNumber)
      Future.successful(Ok(departureTransportPage(frm.fill(formData))))
    }
    submit(pageToDisplay)
  }

  val submitForm: Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val verifyFormAndUpdateCache = () =>
      form(departureTransportHelper.transportCodes)
        .bindFromRequest()
        .fold(formWithErrors => Future.successful(BadRequest(departureTransportPage(formWithErrors))), updateCache(_).map(_ => nextPage))

    submit(verifyFormAndUpdateCache)
  }

  private def submit(fun: () => Future[Result])(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    if (skipDepartureTransport(request.cacheModel)) updateCache(DepartureTransport(None, None)).map(_ => nextPage) else fun()

  private def nextPage(implicit request: JourneyRequest[AnyContent]): Result = {
    val call =
      if (request.declarationType == CLEARANCE) ExpressConsignmentController.displayPage
      else if (request.cacheModel.isInlandOrBorder(Border)) TransportCountryController.displayPage
      else BorderTransportController.displayPage
    navigator.continueTo(call)
  }

  private def updateCache(formData: DepartureTransport)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.updateDepartureTransport(formData))
}
