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
import forms.DeclarationPage
import forms.declaration.RoutingQuestionYesNo.{ChangeCountryPage, RemoveCountryPage, RoutingQuestionPage}
import forms.declaration.destinationCountries.DestinationCountries.{DestinationCountryPage, OriginationCountryPage}

import forms.declaration.{BorderTransport, Document, PackageInformation, _}

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

case class ItemId(id: String)

object Navigator {

  val standard: PartialFunction[DeclarationPage, Mode => Call] = {
    case BorderTransport           => controllers.declaration.routes.DepartureTransportController.displayPage
    case Document                  => controllers.declaration.routes.NatureOfTransactionController.displayPage
    case OriginationCountryPage    => controllers.declaration.routes.DeclarationHolderController.displayPage
    case DestinationCountryPage    => controllers.declaration.routes.OriginationCountryController.displayPage
    case RoutingQuestionPage       => controllers.declaration.routes.DestinationCountryController.displayPage
    case RemoveCountryPage         => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case ChangeCountryPage         => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case SupervisingCustomsOffice  => controllers.declaration.routes.WarehouseIdentificationController.displayPage
    case InlandModeOfTransportCode => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case WarehouseIdentification   => controllers.declaration.routes.ItemsSummaryController.displayPage
    case _                         => throw new IllegalArgumentException("Navigator back-link route not implemented")
  }
  val standardItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation => controllers.declaration.routes.StatisticalValueController.displayPage
    case _                  => throw new IllegalArgumentException("Navigator back-link route not implemented")
  }

  val supplementary: PartialFunction[DeclarationPage, Mode => Call] = {
    case BorderTransport           => controllers.declaration.routes.DepartureTransportController.displayPage
    case Document                  => controllers.declaration.routes.NatureOfTransactionController.displayPage
    case OriginationCountryPage    => controllers.declaration.routes.DeclarationHolderController.displayPage
    case DestinationCountryPage    => controllers.declaration.routes.OriginationCountryController.displayPage
    case SupervisingCustomsOffice  => controllers.declaration.routes.WarehouseIdentificationController.displayPage
    case InlandModeOfTransportCode => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case WarehouseIdentification   => controllers.declaration.routes.ItemsSummaryController.displayPage
    case _                         => throw new IllegalArgumentException("Navigator back-link route not implemented")
  }
  val supplementaryItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation => controllers.declaration.routes.StatisticalValueController.displayPage
    case _                  => throw new IllegalArgumentException("Navigator back-link route not implemented")
  }

  val simplified: PartialFunction[DeclarationPage, Mode => Call] = {
    case BorderTransport           => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case Document                  => controllers.declaration.routes.OfficeOfExitController.displayPage
    case DestinationCountryPage    => controllers.declaration.routes.DeclarationHolderController.displayPage
    case RoutingQuestionPage       => controllers.declaration.routes.DestinationCountryController.displayPage
    case RemoveCountryPage         => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case ChangeCountryPage         => controllers.declaration.routes.RoutingCountriesSummaryController.displayPage
    case SupervisingCustomsOffice  => controllers.declaration.routes.WarehouseIdentificationController.displayPage
    case InlandModeOfTransportCode => controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage
    case WarehouseIdentification   => controllers.declaration.routes.ItemsSummaryController.displayPage
    case _                         => throw new IllegalArgumentException("Navigator back-link route not implemented")
  }
  val simplifiedItemPage: PartialFunction[DeclarationPage, (Mode, String) => Call] = {
    case PackageInformation => controllers.declaration.routes.NactCodeController.displayPage
    case _                  => throw new IllegalArgumentException("Navigator back-link route not implemented")
  }

  def backLink(page: DeclarationPage, mode: Mode)(implicit request: JourneyRequest[_]): Call =
    request.declarationType match {
      case STANDARD      => standard(page)(mode)
      case SUPPLEMENTARY => supplementary(page)(mode)
      case SIMPLIFIED    => simplified(page)(mode)
    }

  def backLink(page: DeclarationPage, mode: Mode, itemId: ItemId)(implicit request: JourneyRequest[_]): Call =
    request.declarationType match {
      case STANDARD      => standardItemPage(page)(mode, itemId.id)
      case SUPPLEMENTARY => supplementaryItemPage(page)(mode, itemId.id)
      case SIMPLIFIED    => simplifiedItemPage(page)(mode, itemId.id)
    }
}
