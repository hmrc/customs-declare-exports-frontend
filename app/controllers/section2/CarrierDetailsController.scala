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
import controllers.section2.routes.ConsigneeDetailsController
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import forms.common.Address.{addressId, countryId}
import forms.section2.carrier.CarrierDetails
import models.DeclarationType._
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.validators.forms.AutoCompleteFieldBinding
import views.html.section2.carrier_details

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CarrierDetailsController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  carrierDetailsPage: carrier_details
)(implicit ec: ExecutionContext, codeListConnector: CodeListConnector, auditService: AuditService)
    extends FrontendController(mcc) with AutoCompleteFieldBinding with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  private val validTypes = Seq(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE)

  val displayPage: Action[AnyContent] = (authenticate andThen journeyType(validTypes)) { implicit request =>
    request.cacheModel.parties.carrierDetails match {
      case Some(data) => Ok(carrierDetailsPage(form.fill(data)))
      case _          => Ok(carrierDetailsPage(form))
    }
  }

  private def form(implicit request: JourneyRequest[_]): Form[CarrierDetails] =
    CarrierDetails.form(request.declarationType).withSubmissionErrors

  val saveAddress: Action[AnyContent] = (authenticate andThen journeyType(validTypes)).async { implicit request =>
    form
      .bindFromRequest(formValuesFromRequest(s"$addressId.$countryId"))
      .fold(
        formWithErrors => Future.successful(BadRequest(carrierDetailsPage(formWithErrors))),
        updateCache(_).map(_ => navigator.continueTo(ConsigneeDetailsController.displayPage))
      )
  }

  private def updateCache(carrierDetails: CarrierDetails)(implicit req: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(model => model.copy(parties = model.parties.copy(carrierDetails = Some(carrierDetails))))
}
