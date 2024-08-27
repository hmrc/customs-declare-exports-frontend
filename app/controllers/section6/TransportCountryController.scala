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

import connectors.CodeListConnector
import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.helpers.TransportSectionHelper.skipTransportCountry
import controllers.navigation.Navigator
import controllers.section6.routes.{ContainerController, ExpressConsignmentController}
import forms.section6.TransportCountry
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.validators.forms.AutoCompleteFieldBinding
import views.helpers.ModeOfTransportCodeHelper
import views.html.section6.transport_country

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransportCountryController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  transportCountry: transport_country
)(implicit ec: ExecutionContext, codeListConnector: CodeListConnector, auditService: AuditService)
    extends FrontendController(mcc) with AutoCompleteFieldBinding with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  val displayPage: Action[AnyContent] = (authenticate andThen journeyType(nonClearanceJourneys)).async { implicit request =>
    val pageToDisplay = () => {
      val transportMode = ModeOfTransportCodeHelper.transportMode(request.cacheModel.transportLeavingBorderCode)
      val form = TransportCountry.form(transportMode).withSubmissionErrors
      val page = request.cacheModel.transport.transportCrossingTheBorderNationality match {
        case Some(data) => transportCountry(transportMode, form.fill(data))
        case _          => transportCountry(transportMode, form)
      }
      Future.successful(Ok(page))
    }
    submit(pageToDisplay)
  }

  val submitForm: Action[AnyContent] = (authenticate andThen journeyType(nonClearanceJourneys)).async { implicit request =>
    val verifyFormAndUpdateCache = () => {
      val transportMode = ModeOfTransportCodeHelper.transportMode(request.cacheModel.transportLeavingBorderCode)
      TransportCountry
        .form(transportMode)
        .bindFromRequest(formValuesFromRequest(TransportCountry.transportCountry))
        .fold(formWithErrors => Future.successful(BadRequest(transportCountry(transportMode, formWithErrors))), updateCache(_).map(_ => nextPage))
    }
    submit(verifyFormAndUpdateCache)
  }

  private def submit(fun: () => Future[Result])(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    if (skipTransportCountry(request.cacheModel)) updateCache(TransportCountry(None)).map(_ => nextPage) else fun()

  private def nextPage(implicit request: JourneyRequest[AnyContent]): Result = {
    val page = request.declarationType match {
      case STANDARD | OCCASIONAL | SIMPLIFIED => ExpressConsignmentController.displayPage
      case SUPPLEMENTARY                      => ContainerController.displayContainerSummary
    }
    navigator.continueTo(page)
  }

  private def updateCache(transportCountry: TransportCountry)(implicit r: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.updateTransportCountry(transportCountry))
}
