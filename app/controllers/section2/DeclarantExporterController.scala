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
import controllers.section2.routes._
import forms.section2.DeclarantIsExporter.form
import forms.section2.DeclarantIsExporter
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section2.declarant_exporter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarantExporterController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  declarantExporterPage: declarant_exporter
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form.withSubmissionErrors
    request.cacheModel.parties.declarantIsExporter match {
      case Some(data) => Ok(declarantExporterPage(frm.fill(data)))
      case _          => Ok(declarantExporterPage(frm))
    }
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(declarantExporterPage(formWithErrors))),
        declarantIsExporter =>
          updateCache(declarantIsExporter)
            .map(_ => navigator.continueTo(nextPage(declarantIsExporter)))
      )
  }

  def nextPage(declarantIsExporter: DeclarantIsExporter)(implicit request: JourneyRequest[AnyContent]): Call =
    if (!declarantIsExporter.isYes) ExporterEoriNumberController.displayPage
    else
      request.declarationType match {
        case DeclarationType.SUPPLEMENTARY => ConsigneeDetailsController.displayPage
        case DeclarationType.CLEARANCE     => IsExsController.displayPage
        case _                             => ThirdPartyGoodsTransportationController.displayPage
      }

  private def updateCache(declarantIsExporter: DeclarantIsExporter)(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { model =>
      if (!declarantIsExporter.isYes) model.copy(parties = model.parties.copy(declarantIsExporter = Some(declarantIsExporter)))
      else
        // clear possible previous answers to irrelevant questions
        model.copy(parties =
          model.parties.copy(declarantIsExporter = Some(declarantIsExporter), exporterDetails = None, representativeDetails = None)
        )
    }
}
