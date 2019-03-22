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

package models
import ai.x.play.json.Jsonx
import play.api.libs.json.Json
import uk.gov.hmrc.wco.dec._

object DeclarationFormats {

  implicit val measureFormats = Json.format[Measure]
  implicit val amountFormats = Json.format[Amount]
  implicit val paymentFormats = Json.format[Payment]
  implicit val dateTimeStringFormats = Json.format[DateTimeString]
  implicit val dateTimeElementFormats = Json.format[DateTimeElement]
  implicit val pointerFormats = Json.format[Pointer]
  implicit val officeFormats = Json.format[Office]
  implicit val guaranteeFormats = Json.format[ObligationGuarantee]
  implicit val governmentAgencyGoodsItemAdditionalDocumentSubmitterFormats =
    Json.format[GovernmentAgencyGoodsItemAdditionalDocumentSubmitter]
  implicit val writeOffFormats = Json.format[WriteOff]
  implicit val governmentAgencyGoodsItemAdditionalDocumentFormats =
    Json.format[GovernmentAgencyGoodsItemAdditionalDocument]
  implicit val additionalInformationFormats = Json.format[AdditionalInformation]
  implicit val roleBasedPartyFormats = Json.format[RoleBasedParty]
  implicit val addressFormats = Json.format[Address]
  implicit val namedEntityWithAddressFormats = Json.format[NamedEntityWithAddress]
  implicit val contactFormats = Json.format[Contact]
  implicit val communicationFormats = Json.format[Communication]
  implicit val importExportPartyFormats = Json.format[ImportExportParty]
  implicit val classificationFormats = Json.format[Classification]
  implicit val dangerousFormats = Json.format[DangerousGoods]
  implicit val dutyTaxFeeFormats = Json.format[DutyTaxFee]
  implicit val goodsMeasureFormats = Json.format[GoodsMeasure]
  implicit val invoiceLineFormats = Json.format[InvoiceLine]
  implicit val sealFormats = Json.format[Seal]
  implicit val transportEquipmentFormats = Json.format[TransportEquipment]
  implicit val commodityFormats = Json.format[Commodity]
  implicit val chargeDeductionFormats = Json.format[ChargeDeduction]
  implicit val customsValuationFormats = Json.format[CustomsValuation]
  implicit val destinationFormats = Json.format[Destination]
  implicit val exportCountryFormats = Json.format[ExportCountry]
  implicit val governmentProcedureFormats = Json.format[GovernmentProcedure]
  implicit val originFormats = Json.format[Origin]
  implicit val packagingFormats = Json.format[Packaging]
  implicit val tradeTermsFormats = Json.format[TradeTerms]
  implicit val previousDocumentFormats = Json.format[PreviousDocument]
  implicit val ucrFormats = Json.format[Ucr]
  implicit val valuationAdjustmentFormats = Json.format[ValuationAdjustment]
  implicit val authorisationHolderFormats = Json.format[AuthorisationHolder]
  implicit val additionalDocumentFormats = Json.format[AdditionalDocument]
  implicit val agentsFormats = Json.format[Agent]
  implicit val currencyExchangeFormats = Json.format[CurrencyExchange]
  implicit val borderTransportMeansFormats = Json.format[BorderTransportMeans]
  implicit val transportMeansFormats = Json.format[TransportMeans]

  implicit val goodsLocationAddressFormats = Json.format[GoodsLocationAddress]
  implicit val goodsLocationFormats = Json.format[GoodsLocation]
  implicit val loadingLocationFormats = Json.format[LoadingLocation]

  implicit val warehouseFormats = Json.format[Warehouse]

  implicit val governmentAgencyGoodsItemFormats = Jsonx.formatCaseClass[GovernmentAgencyGoodsItem]
}
