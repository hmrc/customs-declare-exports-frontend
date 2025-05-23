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

package controllers.section2

import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.section2.routes.{ExporterDetailsController, IsExsController, RepresentativeAgentController}
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section2.exporter.{ExporterDetails, ExporterEoriNumber}
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section2.exporter_eori_number

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExporterEoriNumberController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  exporterEoriDetailsPage: exporter_eori_number,
  override val exportsCacheService: ExportsCacheService
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = ExporterEoriNumber.form.withSubmissionErrors
    request.cacheModel.parties.exporterDetails match {
      case Some(data) => Ok(exporterEoriDetailsPage(frm.fill(ExporterEoriNumber(data))))
      case _          => Ok(exporterEoriDetailsPage(frm))
    }
  }

  def submit(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    ExporterEoriNumber.form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(exporterEoriDetailsPage(formWithErrors))),
        exporterEoriNumber =>
          updateCache(exporterEoriNumber, request.cacheModel.parties.exporterDetails)
            .map(_ => navigator.continueTo(nextPage(exporterEoriNumber.hasEori)))
      )
  }

  private def nextPage(hasEori: String)(implicit request: JourneyRequest[_]): Call =
    if (hasEori == YesNoAnswers.no) ExporterDetailsController.displayPage
    else
      request.declarationType match {
        case DeclarationType.CLEARANCE => IsExsController.displayPage
        case _                         => RepresentativeAgentController.displayPage
      }

  private def updateCache(exporterEoriNumber: ExporterEoriNumber, savedExporterDetails: Option[ExporterDetails])(
    implicit r: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model =>
      model.copy(parties = model.parties.copy(exporterDetails = Some(ExporterDetails.from(exporterEoriNumber, savedExporterDetails))))
    )
}
