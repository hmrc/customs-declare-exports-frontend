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

package models.declaration

import forms.Lrn
import forms.common.YesNoAnswer.Yes
import forms.common.{Date, Eori}
import forms.declaration.ConsignmentReferencesSpec._
import forms.declaration.NatureOfTransactionSpec._
import forms.declaration.TransportPayment.cash
import forms.declaration._
import forms.declaration.AdditionalActorsSpec._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.SUPPLEMENTARY_SIMPLIFIED
import forms.declaration.additionaldocuments.{AdditionalDocument, DocumentWriteOff}
import forms.declaration.carrier.CarrierDetails
import forms.declaration.countries.Country
import forms.declaration.authorisationHolder.AuthorisationHolder
import forms.declaration.exporter.ExporterDetails
import forms.declaration.officeOfExit.OfficeOfExit
import models.declaration.governmentagencygoodsitem.Formats._
import models.declaration.governmentagencygoodsitem.{Amount, GovernmentAgencyGoodsItem}
import models.{CancelDeclaration, DeclarationMeta, DeclarationType, ExportsDeclaration}
import play.api.libs.json._

import java.time.Instant
import java.util.UUID

object ExportDeclarationTestData {

  private val containerId = "id"

  val declarationMeta = DeclarationMeta(None, None, DeclarationStatus.DRAFT, Instant.now, Instant.now)

  val correctTransportInformationContainerData = Some(Seq(Container(1, id = "M1l3s", List(Seal(1, "Seal1")))))
  val emptyTransportInformationContainerData = ContainerAdd(None)
  val correctTransportInformationContainerJSON: JsValue = JsObject(Map(containerId -> JsString("container-M1l3s")))
  val incorrectTransportInformationContainerJSON: JsValue = JsObject(Map(containerId -> JsString("123456789012345678")))
  val emptyTransportInformationContainerJSON: JsValue = JsObject(Map(containerId -> JsString("")))
  val correctTransportInformationContainerDataJSON: JsValue = Json.toJson(correctTransportInformationContainerData)

  val correctAdditionalActorsData = AdditionalActors(Seq(correctAdditionalActors1, correctAdditionalActors2))

  val correctAuthorisationHolder =
    AuthorisationHolder(authorisationTypeCode = Some("ACE"), eori = Some(Eori("PL213472539481923")), Some(EoriSource.OtherEori))
  val correctAuthorisationHolders = AuthorisationHolders(Seq(correctAuthorisationHolder))

  val correctExporterDetails = ExporterDetails(details = EntityDetailsSpec.correctEntityDetails)

  val correctDeclarantDetailsEORIOnly: DeclarantDetails = DeclarantDetails(details = EntityDetailsSpec.correctEntityDetailsEORIOnly)

  lazy val allRecords = declaration.copy(
    additionalDeclarationType = Some(SUPPLEMENTARY_SIMPLIFIED),
    consignmentReferences = Some(correctConsignmentReferences),
    natureOfTransaction = Some(correctNatureOfTransaction),
    totalNumberOfItems = Some(InvoiceAndPackageTotals(Some("12312312312312.12"), Some("1212121.12345"), Some("GBP"), Some("Yes"), None)),
    transport = Transport(
      expressConsignment = Yes,
      transportPayment = Some(TransportPayment(cash)),
      containers = correctTransportInformationContainerData,
      borderModeOfTransportCode = Some(TransportLeavingTheBorder(Some(ModeOfTransportCode.Road))),
      meansOfTransportOnDepartureType = Some("10"),
      meansOfTransportOnDepartureIDNumber = Some("123112yu78"),
      meansOfTransportCrossingTheBorderType = Some("40"),
      meansOfTransportCrossingTheBorderIDNumber = Some("1234567878ui"),
      transportCrossingTheBorderNationality = Some(TransportCountry(Some("PT")))
    ),
    parties = Parties(
      exporterDetails = Some(correctExporterDetails),
      declarantDetails = Some(correctDeclarantDetailsEORIOnly),
      consigneeDetails = Some(ConsigneeDetails(EntityDetailsSpec.correctEntityDetails)),
      representativeDetails = None, // Some(correctRepresentativeDetails),
      declarationAdditionalActorsData = Some(correctAdditionalActorsData),
      declarationHoldersData = Some(correctAuthorisationHolders),
      carrierDetails = Some(CarrierDetails(EntityDetailsSpec.correctEntityDetails))
    ),
    locations = Locations(
      destinationCountry = Some(Country(Some("PL"))),
      hasRoutingCountries = Some(true),
      routingCountries = Seq(RoutingCountry(1, Country(Some("FR")))),
      goodsLocation = Some(LocationOfGoods("GBAUEMAEMAEMA").toModel),
      officeOfExit = Some(OfficeOfExit("officeId")),
      warehouseIdentification = Some(WarehouseIdentificationYesNoSpec.correctWarehouseDetails),
      supervisingCustomsOffice = Some(SupervisingCustomsOfficeSpec.correctSupervisingCustomsOffice),
      inlandModeOfTransportCode = Some(InlandModeOfTransportCodeSpec.correctInlandModeOfTransportCode)
    )
  )

  lazy val cancellationDeclarationTest = CancelDeclaration("id", Lrn("FG7676767889"), "mrn", "description", "reason")

  lazy val allRecordsXmlMarshallingTest = allRecords.copy(
    items = Seq(
      ExportItem(
        "itemid",
        sequenceId = 1,
        statisticalValue = Some(StatisticalValue(statisticalValue = "100")),
        additionalDocuments = Some(
          AdditionalDocuments(
            Yes,
            Seq(
              AdditionalDocument(
                documentTypeCode = Some("C501"),
                documentIdentifier = Some("SYSUYSU123-24554"),
                documentStatus = Some("PND"),
                documentStatusReason = Some("Reason"),
                issuingAuthorityName = Some("issuingAuthorityName"),
                dateOfValidity = Some(Date(year = Some(2017), month = Some(1), day = Some(1))),
                documentWriteOff = Some(DocumentWriteOff(measurementUnit = Some("KGM"), documentQuantity = Some(BigDecimal("10"))))
              )
            )
          )
        ),
        additionalInformation = Some(AdditionalInformationData(Seq(AdditionalInformation("code", "description")))),
        commodityMeasure = Some(CommodityMeasure(Some("2"), Some(false), Some("200"), Some("100"))),
        additionalFiscalReferencesData =
          Some(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("PL", "12345"), AdditionalFiscalReference("FR", "54321")))),
        procedureCodes = Some(ProcedureCodesData(Some("CUPR"), Seq("CC", "PR"))),
        packageInformation = Some(
          List(PackageInformation(1, "pkgAA", Some("AA"), Some(2), Some("mark1")), PackageInformation(2, "pkgBB", Some("AB"), Some(4), Some("mark2")))
        )
      )
    ),
    totalNumberOfItems = Some(InvoiceAndPackageTotals(Some("1212312.12"), Some("1212121.12345"), Some("GBP"), Some("Yes"), Some("123"))),
    parties = Parties(
      exporterDetails = Some(correctExporterDetails),
      declarantDetails = Some(correctDeclarantDetailsEORIOnly),
      consigneeDetails = Some(ConsigneeDetails(EntityDetailsSpec.correctEntityDetails)),
      representativeDetails = None, // Some(correctRepresentativeDetails),
      declarationAdditionalActorsData = Some(AdditionalActors(Seq(correctAdditionalActors1))),
      declarationHoldersData = Some(
        AuthorisationHolders(
          Seq(
            AuthorisationHolder(authorisationTypeCode = Some("1234"), eori = Some(Eori("PL213472539481923")), Some(EoriSource.UserEori)),
            AuthorisationHolder(authorisationTypeCode = Some("4321"), eori = Some(Eori("PT213472539481923")), Some(EoriSource.OtherEori))
          )
        )
      ),
      carrierDetails = Some(CarrierDetails(EntityDetailsSpec.correctEntityDetails))
    ),
    locations = Locations(
      destinationCountry = Some(Country(Some("PL"))),
      hasRoutingCountries = Some(true),
      routingCountries = Seq(RoutingCountry(1, Country(Some("FR")))),
      goodsLocation = Some(LocationOfGoods("GBAUEMAEMAEMA").toModel),
      officeOfExit = Some(OfficeOfExit("officeId")),
      warehouseIdentification = Some(WarehouseIdentificationYesNoSpec.correctWarehouseDetails),
      supervisingCustomsOffice = Some(SupervisingCustomsOfficeSpec.correctSupervisingCustomsOffice),
      inlandModeOfTransportCode = Some(InlandModeOfTransportCodeSpec.correctInlandModeOfTransportCode)
    ),
    previousDocuments = Some(PreviousDocumentsData(Seq(Document("MCR", "DocumentReference", Some("123")))))
  )

  lazy val correctGovernmentAgencyGoodsItemJSON: JsValue = JsArray(Seq(Json.toJson(createGovernmentAgencyGoodsItem())))
  lazy val correctStatisticalValueAmountJSON: JsValue =
    JsObject(Map("currencyId" -> JsString("GBP"), "value" -> JsString("44")))
  lazy val correctPackageInformationJSON: JsValue = JsArray(Seq(correctPackageInformationJSON))
  val date = Date(Some(12), Some(12), Some(2019))
  val correctPackingJSON: JsValue = JsObject(
    Map("sequenceNumeric" -> JsString("0"), "marksNumbersId" -> JsString("wefdsf"), "typeCode" -> JsString("22"))
  )
  val declaration = ExportsDeclaration(UUID.randomUUID.toString, declarationMeta, DeclarationType.SUPPLEMENTARY)

  def createGovernmentAgencyGoodsItem(): GovernmentAgencyGoodsItem =
    GovernmentAgencyGoodsItem(
      sequenceNumeric = 0,
      statisticalValueAmount = Some(Amount(Some("GBP"), Some(BigDecimal(12)))),
      commodity = None, // parsed from cached CommodityForm
      additionalInformations = Seq(),
      additionalDocuments = Seq(),
      governmentProcedures = Seq(),
      packagings = Seq()
    )
}
