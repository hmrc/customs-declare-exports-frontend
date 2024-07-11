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

package controllers.section6

import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.section6.routes.ContainerController
import forms.section6.TransportPayment
import forms.section6.TransportPayment._
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section6.transport_payment

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransportPaymentController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  transportPayment: transport_payment
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  private val validTypes = Seq(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE)

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType(validTypes)) { implicit request =>
    val frm = form.withSubmissionErrors
    request.cacheModel.transport.transportPayment match {
      case Some(data) => Ok(transportPayment(frm.fill(data)))
      case _          => Ok(transportPayment(frm))
    }
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType(validTypes)).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(transportPayment(formWithErrors))),
        transportPayment => updateCache(transportPayment).map(_ => nextPage())
      )
  }

  private def nextPage()(implicit request: JourneyRequest[AnyContent]): Result =
    navigator.continueTo(ContainerController.displayContainerSummary)

  private def updateCache(formData: TransportPayment)(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.updateTransportPayment(formData))
}
