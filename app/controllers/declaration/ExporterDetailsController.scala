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
import controllers.navigation.Navigator
import forms.declaration.exporter.ExporterDetails
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.exporter_address

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExporterDetailsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  exporterDetailsPage: exporter_address
)(implicit ec: ExecutionContext, codeListConnector: CodeListConnector)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form().withSubmissionErrors()
    request.cacheModel.parties.exporterDetails match {
      case Some(data) => Ok(exporterDetailsPage(mode, frm.fill(data)))
      case _          => Ok(exporterDetailsPage(mode, frm))
    }
  }

  def saveAddress(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[ExporterDetails]) => Future.successful(BadRequest(exporterDetailsPage(mode, formWithErrors))),
        form =>
          updateCache(form)
            .map(_ => navigator.continueTo(mode, nextPage))
      )
  }

  def nextPage(implicit request: JourneyRequest[AnyContent]): Mode => Call =
    request.declarationType match {
      case DeclarationType.CLEARANCE => controllers.declaration.routes.IsExsController.displayPage
      case _                         => controllers.declaration.routes.RepresentativeAgentController.displayPage
    }

  private def updateCache(formData: ExporterDetails)(implicit r: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val updatedParties = model.parties.copy(exporterDetails = Some(formData))
      model.copy(parties = updatedParties)
    })

  private def form()(implicit request: JourneyRequest[AnyContent]): Form[ExporterDetails] =
    ExporterDetails.form(request.declarationType, Some(request.cacheModel))
}
