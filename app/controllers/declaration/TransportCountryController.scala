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
import controllers.declaration.routes.{ExpressConsignmentController, TransportContainerController}
import controllers.navigation.Navigator
import forms.declaration.TransportCountry
import models.DeclarationType.{STANDARD, SUPPLEMENTARY}
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.helpers.ModeOfTransportCodeHelper
import views.html.declaration.transport_country

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransportCountryController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  transportCountry: transport_country
)(implicit ec: ExecutionContext, codeListConnector: CodeListConnector)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  private val validTypes = Seq(STANDARD, SUPPLEMENTARY)

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType(validTypes)) { implicit request =>
    val transportMode = ModeOfTransportCodeHelper.transportMode(request.cacheModel.transportLeavingBorderCode)
    val form = TransportCountry.form(transportMode).withSubmissionErrors
    request.cacheModel.transport.transportCrossingTheBorderNationality match {
      case Some(data) =>
        Ok(transportCountry(mode, transportMode, form.fill(data)))

      case _ => Ok(transportCountry(mode, transportMode, form))
    }
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType(validTypes)).async { implicit request =>
    val transportMode = ModeOfTransportCodeHelper.transportMode(request.cacheModel.transportLeavingBorderCode)
    TransportCountry
      .form(transportMode)
      .bindFromRequest
      .fold(
        formWithErrors => Future.successful(BadRequest(transportCountry(mode, transportMode, formWithErrors))),
        updateCache(_).map(_ => navigator.continueTo(mode, nextPage))
      )
  }

  private def nextPage(implicit request: JourneyRequest[AnyContent]): Mode => Call =
    request.declarationType match {
      case STANDARD      => ExpressConsignmentController.displayPage
      case SUPPLEMENTARY => TransportContainerController.displayContainerSummary
    }

  private def updateCache(transportCountry: TransportCountry)(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { declaration =>
      declaration.copy(transport = declaration.transport.copy(transportCrossingTheBorderNationality = Some(transportCountry)))
    }
}
