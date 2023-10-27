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

package controllers.declaration

import controllers.actions.{AuthAction, JourneyAction}
import controllers.declaration.routes._
import controllers.helpers.TransportSectionHelper.isPostalOrFTIModeOfTransport
import controllers.navigation.Navigator
import forms.declaration.InlandModeOfTransportCode._
import forms.declaration.ModeOfTransportCode.{FixedTransportInstallations, PostalConsignment}
import forms.declaration.{InlandModeOfTransportCode, ModeOfTransportCode}
import models.DeclarationType._
import models.requests.JourneyRequest
import play.api.data.FormError
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.inland_transport_details

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InlandTransportDetailsController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  inlandTransportDetailsPage: inland_transport_details
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  private val validJourneys = allDeclarationTypesExcluding(CLEARANCE)

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType(validJourneys)) { implicit request =>
    val frm = form.withSubmissionErrors
    request.cacheModel.locations.inlandModeOfTransportCode match {
      case Some(code) => Ok(inlandTransportDetailsPage(frm.fill(code)))
      case _          => Ok(inlandTransportDetailsPage(frm))
    }
  }

  def submit(): Action[AnyContent] = (authenticate andThen journeyType(validJourneys)).async { implicit request =>
    form
      .bindFromRequest()
      .fold(formWithErrors => Future.successful(BadRequest(inlandTransportDetailsPage(formWithErrors))), validateAndUpdateCache(_))
  }

  private def nextPage(code: InlandModeOfTransportCode)(implicit request: JourneyRequest[AnyContent]): Call =
    if (!isPostalOrFTIModeOfTransport(code.inlandModeOfTransportCode) && isSimplifiedOrOccasional)
      BorderTransportController.displayPage
    else if (!isPostalOrFTIModeOfTransport(code.inlandModeOfTransportCode))
      DepartureTransportController.displayPage
    else if (isPostalOrFTIModeOfTransport(code.inlandModeOfTransportCode) && isSimplifiedOrOccasional)
      TransportCountryController.displayPage
    else if (request.isType(SUPPLEMENTARY))
      TransportContainerController.displayContainerSummary
    else
      ExpressConsignmentController.displayPage

  private def returnFormWithErrors(code: InlandModeOfTransportCode, error: String)(implicit request: JourneyRequest[_]): Future[Result] = {
    val messages = messagesApi.preferred(request).messages
    val formWithErrors = form.fill(code).copy(errors = List(FormError(formId, messages(error))))
    Future.successful(BadRequest(inlandTransportDetailsPage(formWithErrors)))
  }

  private def updateCacheAndGoNextPage(code: InlandModeOfTransportCode)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    updateDeclarationFromRequest { declaration =>
      val transportCrossingTheBorderNationality =
        if (isPostalOrFTIModeOfTransport(code.inlandModeOfTransportCode)) None else declaration.transport.transportCrossingTheBorderNationality

      declaration.copy(
        transport = declaration.transport.copy(transportCrossingTheBorderNationality = transportCrossingTheBorderNationality),
        locations = declaration.locations.copy(inlandModeOfTransportCode = Some(code))
      )
    } map { _ =>
      navigator.continueTo(nextPage(code))
    }

  private def validateAndUpdateCache(code: InlandModeOfTransportCode)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    validateWithTransportLeavingBorderCode(code).fold(updateCacheAndGoNextPage(code))(returnFormWithErrors(code, _))

  private def validateEquivalenceOfModeOfTransportCode(
    inlandModeOfTransportCode: InlandModeOfTransportCode,
    expectedModeOfTransportCode: ModeOfTransportCode,
    errorKey: String
  ): Option[String] =
    if (inlandModeOfTransportCode.inlandModeOfTransportCode.exists(_ == expectedModeOfTransportCode)) None
    else Some(s"declaration.warehouse.inlandTransportDetails.error.$errorKey")

  private def validateWithTransportLeavingBorderCode(code: InlandModeOfTransportCode)(implicit request: JourneyRequest[_]): Option[String] =
    request.cacheModel.transportLeavingBorderCode match {
      case Some(transportLeavingBorderCode) if transportLeavingBorderCode == PostalConsignment =>
        validateEquivalenceOfModeOfTransportCode(code, PostalConsignment, "not.postal")

      case Some(transportLeavingBorderCode) if transportLeavingBorderCode == FixedTransportInstallations =>
        validateEquivalenceOfModeOfTransportCode(code, FixedTransportInstallations, "not.fti")

      case _ => None
    }

  private def isSimplifiedOrOccasional(implicit request: JourneyRequest[AnyContent]): Boolean =
    request.isType(SIMPLIFIED) || request.isType(OCCASIONAL)
}
