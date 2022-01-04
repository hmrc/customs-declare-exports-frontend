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

import scala.concurrent.{ExecutionContext, Future}
import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import forms.declaration.BorderTransport
import forms.declaration.BorderTransport._
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.border_transport

import javax.inject.Inject

class BorderTransportController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  borderTransport: border_transport
)(implicit ec: ExecutionContext, codeListConnector: CodeListConnector)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  private val validTypes = Seq(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY)

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType(validTypes)) { implicit request =>
    val transport = request.cacheModel.transport
    val borderTransportData = (
      transport.meansOfTransportCrossingTheBorderType,
      transport.meansOfTransportCrossingTheBorderIDNumber,
      transport.meansOfTransportCrossingTheBorderNationality
    ) match {
      case (Some(meansType), Some(meansId), meansNationality) => Some(BorderTransport(meansNationality, meansType, meansId))
      case _                                                  => None
    }
    val frm = form().withSubmissionErrors()
    borderTransportData match {
      case Some(data) => Ok(borderTransport(mode, frm.fill(data)))
      case _          => Ok(borderTransport(mode, frm))
    }
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType(validTypes)).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[BorderTransport]) => Future.successful(BadRequest(borderTransport(mode, formWithErrors))),
        borderTransport => updateCache(borderTransport).map(_ => navigator.continueTo(mode, nextPage))
      )
  }

  private def nextPage(implicit request: JourneyRequest[AnyContent]): Mode => Call =
    request.declarationType match {
      case DeclarationType.STANDARD      => routes.ExpressConsignmentController.displayPage
      case DeclarationType.SUPPLEMENTARY => routes.TransportContainerController.displayContainerSummary
    }

  private def updateCache(formData: BorderTransport)(implicit r: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(_.updateBorderTransport(formData))
}
