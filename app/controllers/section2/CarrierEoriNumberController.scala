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
import controllers.section2.routes.{CarrierDetailsController, ConsigneeDetailsController}
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section2.carrier.{CarrierDetails, CarrierEoriNumber}
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD}
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section2.carrier_eori_number

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CarrierEoriNumberController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  carrierEoriDetailsPage: carrier_eori_number,
  override val exportsCacheService: ExportsCacheService
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  private val validJourneys = List(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE)

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType(validJourneys)) { implicit request =>
    carrierDetails match {
      case Some(data) => Ok(carrierEoriDetailsPage(form.fill(CarrierEoriNumber(data))))
      case _          => Ok(carrierEoriDetailsPage(form))
    }
  }

  def submit(): Action[AnyContent] = (authenticate andThen journeyType(validJourneys)).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(carrierEoriDetailsPage(formWithErrors))),
        carrierEoriNumber => updateCache(carrierEoriNumber, carrierDetails).map(_ => navigator.continueTo(nextPage(carrierEoriNumber.hasEori)))
      )
  }

  private def carrierDetails(implicit request: JourneyRequest[_]): Option[CarrierDetails] =
    request.cacheModel.parties.carrierDetails

  private def form(implicit request: JourneyRequest[_]): Form[CarrierEoriNumber] =
    CarrierEoriNumber.form.withSubmissionErrors

  private def nextPage(hasEori: String): Call =
    if (hasEori == YesNoAnswers.yes) ConsigneeDetailsController.displayPage else CarrierDetailsController.displayPage

  private def updateCache(carrierEoriNumber: CarrierEoriNumber, savedCarrierDetails: Option[CarrierDetails])(
    implicit r: JourneyRequest[AnyContent]
  ): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model =>
      model.copy(parties = model.parties.copy(carrierDetails = Some(CarrierDetails.from(carrierEoriNumber, savedCarrierDetails))))
    )
}
