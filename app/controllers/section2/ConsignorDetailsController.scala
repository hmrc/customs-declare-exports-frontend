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

import connectors.CodeListConnector
import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.section2.routes._
import forms.common.Address.{addressId, countryId}
import forms.section2.consignor.ConsignorDetails
import models.DeclarationType.CLEARANCE
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.validators.forms.AutoCompleteFieldBinding
import views.html.section2.consignor_details

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConsignorDetailsController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  consignorDetailsPage: consignor_details
)(implicit ec: ExecutionContext, codeListConnector: CodeListConnector, auditService: AuditService)
    extends FrontendController(mcc) with AutoCompleteFieldBinding with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  private val validJourneys = Seq(DeclarationType.CLEARANCE)

  val displayPage: Action[AnyContent] = (authenticate andThen journeyType(validJourneys)) { implicit request =>
    val frm = ConsignorDetails.form.withSubmissionErrors
    request.cacheModel.parties.consignorDetails match {
      case Some(data) => Ok(consignorDetailsPage(frm.fill(data)))
      case _          => Ok(consignorDetailsPage(frm))
    }
  }

  val saveAddress: Action[AnyContent] = (authenticate andThen journeyType(validJourneys)).async { implicit request =>
    ConsignorDetails.form
      .bindFromRequest(formValuesFromRequest(s"$addressId.$countryId"))
      .fold(
        formWithErrors => Future.successful(BadRequest(consignorDetailsPage(formWithErrors))),
        updateCache(_).map(_ => navigator.continueTo(nextPage))
      )
  }

  private def nextPage(implicit request: JourneyRequest[_]): Call = request.cacheModel.`type` match {
    case CLEARANCE                                   => ThirdPartyGoodsTransportationController.displayPage
    case _ if request.cacheModel.isDeclarantExporter => CarrierEoriNumberController.displayPage
    case _                                           => RepresentativeAgentController.displayPage
  }

  private def updateCache(formData: ConsignorDetails)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { model =>
      val updatedParties = model.parties.copy(consignorDetails = Some(formData))
      model.copy(parties = updatedParties)
    }
}
