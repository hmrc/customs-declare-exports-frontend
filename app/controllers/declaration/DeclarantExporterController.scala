/*
 * Copyright 2020 HM Revenue & Customs
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
import javax.inject.Inject
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.declarant_exporter

import scala.concurrent.{ExecutionContext, Future}

class DeclarantExporterController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  declaranExporterPage: declarant_exporter
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.parties.declarantIsExporter match {
      case Some(data) => Ok(declaranExporterPage(mode, form().fill(data)))
      case _          => Ok(declaranExporterPage(mode, form()))
    }
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(declaranExporterPage(mode, formWithErrors))),
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
          controllers.declaration.routes.ConsignorEoriNumberController.displayPage
        case DeclarationType.STANDARD | DeclarationType.SIMPLIFIED | DeclarationType.OCCASIONAL =>
          controllers.declaration.routes.CarrierDetailsController.displayPage
      }
    } else controllers.declaration.routes.ExporterDetailsController.displayPage

  private def updateCache(answer: DeclarantIsExporter)(implicit r: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      if (answer.isExporter) {
        // clear possible previous answers to irrelevant questions
        model.copy(parties = model.parties.copy(declarantIsExporter = Some(answer), exporterDetails = None, representativeDetails = None))
      } else
        model.copy(parties = model.parties.copy(declarantIsExporter = Some(answer)))

    })
}
