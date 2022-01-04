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
import controllers.navigation.Navigator
import forms.declaration.DeclarantIsExporter
import forms.declaration.DeclarantIsExporter.form
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.declarant_exporter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarantExporterController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  declarantExporterPage: declarant_exporter
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form().withSubmissionErrors()
    request.cacheModel.parties.declarantIsExporter match {
      case Some(data) => Ok(declarantExporterPage(mode, frm.fill(data)))
      case _          => Ok(declarantExporterPage(mode, frm))
    }
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(declarantExporterPage(mode, formWithErrors))),
        validForm =>
          updateCache(validForm)
            .map(_ => navigator.continueTo(mode, nextPage(validForm)))
      )
  }

  def nextPage(answer: DeclarantIsExporter)(implicit request: JourneyRequest[AnyContent]): Mode => Call =
    if (answer.isExporter) {
      request.declarationType match {
        case DeclarationType.SUPPLEMENTARY =>
          controllers.declaration.routes.ConsigneeDetailsController.displayPage
        case DeclarationType.CLEARANCE =>
          controllers.declaration.routes.IsExsController.displayPage
        case DeclarationType.STANDARD | DeclarationType.SIMPLIFIED | DeclarationType.OCCASIONAL =>
          controllers.declaration.routes.CarrierEoriNumberController.displayPage
      }
    } else controllers.declaration.routes.ExporterEoriNumberController.displayPage

  private def updateCache(answer: DeclarantIsExporter)(implicit r: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      if (answer.isExporter) {
        // clear possible previous answers to irrelevant questions
        model.copy(parties = model.parties.copy(declarantIsExporter = Some(answer), exporterDetails = None, representativeDetails = None))
      } else
        model.copy(parties = model.parties.copy(declarantIsExporter = Some(answer)))
    })
}
