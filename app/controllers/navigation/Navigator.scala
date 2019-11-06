/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers.navigation

import config.AppConfig
import controllers.util.{FormAction, SaveAndReturn}
import forms.DeclarationFieldCompanion
import forms.declaration.{BorderTransport, PreviousDocumentsData}
import javax.inject.Inject
import models.DeclarationType._
import models.Mode
import models.requests.{ExportsSessionKeys, JourneyRequest}
import models.responses.FlashKeys
import play.api.mvc.{AnyContent, Call, Result, Results}
import services.audit.{AuditService, AuditTypes}
import uk.gov.hmrc.http.HeaderCarrier

class Navigator @Inject()(appConfig: AppConfig, auditService: AuditService) {

  def continueTo(call: Call)(implicit req: JourneyRequest[AnyContent], hc: HeaderCarrier): Result =
    FormAction.bindFromRequest match {
      case SaveAndReturn =>
        auditService.auditAllPagesUserInput(AuditTypes.SaveAndReturnSubmission, req.cacheModel)
        goToDraftConfirmation()
      case _ => Results.Redirect(call)
    }

  private def goToDraftConfirmation()(implicit req: JourneyRequest[_]): Result = {
    val updatedDateTime = req.cacheModel.updatedDateTime
    val expiry = updatedDateTime.plusSeconds(appConfig.draftTimeToLive.toSeconds)
    Results
      .Redirect(controllers.declaration.routes.ConfirmationController.displayDraftConfirmation())
      .flashing(FlashKeys.expiryDate -> expiry.toEpochMilli.toString)
      .removingFromSession(ExportsSessionKeys.declarationId)
  }

}

object Navigator {

  val standard: PartialFunction[DeclarationFieldCompanion, Mode => Call] = {
    case BorderTransport       => controllers.declaration.routes.DepartureTransportController.displayPage
    case PreviousDocumentsData => controllers.declaration.routes.NatureOfTransactionController.displayPage
    case _                     => ???
  }

  val supplementary: PartialFunction[DeclarationFieldCompanion, Mode => Call] = {
    case BorderTransport       => controllers.declaration.routes.DepartureTransportController.displayPage
    case PreviousDocumentsData => controllers.declaration.routes.NatureOfTransactionController.displayPage
    case _                     => ???
  }

  val simplified: PartialFunction[DeclarationFieldCompanion, Mode => Call] = {
    case BorderTransport       => controllers.declaration.routes.WarehouseIdentificationController.displayPage
    case PreviousDocumentsData => controllers.declaration.routes.OfficeOfExitController.displayPage
    case _                     => ???
  }

  def backLink(page: DeclarationFieldCompanion)(implicit request: JourneyRequest[_]): Mode => Call =
    request.declarationType match {
      case STANDARD      => standard(page)
      case SUPPLEMENTARY => supplementary(page)
      case SIMPLIFIED    => simplified(page)
    }
}
