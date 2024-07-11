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

package controllers.section2

import connectors.CodeListConnector
import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.section2.routes.{IsExsController, RepresentativeAgentController}
import forms.common.Address.{addressId, countryId}
import forms.section2.exporter.ExporterDetails
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.validators.forms.AutoCompleteFieldBinding
import views.html.section2.exporter_address

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExporterDetailsController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  exporterDetailsPage: exporter_address
)(implicit ec: ExecutionContext, codeListConnector: CodeListConnector, auditService: AuditService)
    extends FrontendController(mcc) with AutoCompleteFieldBinding with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  val displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form.withSubmissionErrors
    request.cacheModel.parties.exporterDetails match {
      case Some(data) => Ok(exporterDetailsPage(frm.fill(data)))
      case _          => Ok(exporterDetailsPage(frm))
    }
  }

  val saveAddress: Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest(formValuesFromRequest(s"$addressId.$countryId"))
      .fold(
        formWithErrors => Future.successful(BadRequest(exporterDetailsPage(formWithErrors))),
        updateCache(_).map(_ => navigator.continueTo(nextPage))
      )
  }

  private def nextPage(implicit request: JourneyRequest[AnyContent]): Call =
    request.declarationType match {
      case DeclarationType.CLEARANCE => IsExsController.displayPage
      case _                         => RepresentativeAgentController.displayPage
    }

  private def updateCache(formData: ExporterDetails)(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { model =>
      val updatedParties = model.parties.copy(exporterDetails = Some(formData))
      model.copy(parties = updatedParties)
    }

  private def form(implicit request: JourneyRequest[AnyContent]): Form[ExporterDetails] =
    ExporterDetails.form(request.declarationType, Some(request.cacheModel))
}
