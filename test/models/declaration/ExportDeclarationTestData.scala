/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.Instant
import java.util.UUID

import forms.common.{Date, Eori}
import forms.declaration.ConsignmentReferencesSpec._
import forms.declaration.DeclarantDetailsSpec._
import forms.declaration.DeclarationAdditionalActorsSpec.correctAdditionalActors1
import forms.declaration.DispatchLocationSpec._
import forms.declaration.ExporterDetailsSpec._
import forms.declaration.GoodsLocationTestData._
import forms.declaration.NatureOfTransactionSpec._
import forms.declaration.OfficeOfExitSupplementarySpec._
import forms.declaration.RepresentativeDetailsSpec._
import forms.declaration.TotalNumberOfItemsSpec._
import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDecSpec._
import forms.declaration.additionaldocuments.{DocumentWriteOff, DocumentsProduced}
import forms.{CancelDeclaration, Lrn}
import models.declaration.DeclarationAdditionalActorsDataSpec._
import models.declaration.DeclarationHoldersDataSpec._
import models.declaration.governmentagencygoodsitem.Formats._
import models.declaration.governmentagencygoodsitem.{Amount, GovernmentAgencyGoodsItem}
import models.{DeclarationStatus, DeclarationType, ExportsDeclaration}
import play.api.libs.json._

object ExportDeclarationTestData {

  private val containerId = "id"

  val correctTransportInformationContainerData = Some(Seq(Container(id = "M1l3s", Seq.empty)))
  val emptyTransportInformationContainerData = ContainerAdd(None)
  val correctTransportInformationContainerJSON: JsValue = JsObject(Map(containerId -> JsString("container-M1l3s")))
  val incorrectTransportInformationContainerJSON: JsValue = JsObject(Map(containerId -> JsString("123456789012345678")))
  val emptyTransportInformationContainerJSON: JsValue = JsObject(Map(containerId -> JsString("")))
  val correctTransportInformationContainerDataJSON: JsValue = Json.toJson(correctTransportInformationContainerData)

  lazy val allRecords = declaration.copy(
    dispatchLocation = Some(correctDispatchLocation),
    additionalDeclarationType = Some(correctAdditionalDeclarationTypeSupplementaryDec),
    consignmentReferences = Some(correctConsignmentReferences),
    natureOfTransaction = Some(correctNatureOfTransaction),
    totalNumberOfItems = Some(correctTotalNumberOfItemsDecimalValues),
    transport = Transport(
      containers = correctTransportInformationContainerData,
      borderModeOfTransportCode = Some("3"),
      meansOfTransportOnDepartureType = Some("10"),
      meansOfTransportOnDepartureIDNumber = Some("123112yu78"),
      meansOfTransportCrossingTheBorderNationality = Some("Portugal"),
      meansOfTransportCrossingTheBorderType = Some("40"),
      meansOfTransportCrossingTheBorderIDNumber = Some("1234567878ui")
    ),
    parties = Parties(
      exporterDetails = Some(correctExporterDetails),
      declarantDetails = Some(correctDeclarantDetailsEORIOnly),
      consigneeDetails = Some(ConsigneeDetails(EntityDetailsSpec.correctEntityDetails)),
      representativeDetails = Some(correctRepresentativeDetails),
      declarationAdditionalActorsData = Some(correctAdditionalActorsData),
      declarationHoldersData = Some(correctDeclarationHoldersData),
      carrierDetails = Some(CarrierDetails(EntityDetailsSpec.correctEntityDetails))
    ),
    locations = Locations(
      originationCountry = Some("GB"),
      destinationCountry = Some("PL"),
      hasRoutingCountries = Some(true),
      routingCountries = Seq("FR"),
      goodsLocation = Some(correctGoodsLocation),
      officeOfExit = Some(correctOfficeOfExit),
      warehouseIdentification = Some(WarehouseIdentificationSpec.correctWarehouseDetails),
      supervisingCustomsOffice = Some(SupervisingCustomsOfficeSpec.correctSupervisingCustomsOffice),
      inlandModeOfTransportCode = Some(InlandModeOfTransportCodeSpec.correctInlandModeOfTransportCode)
    )
  )

  lazy val cancellationDeclarationTest = CancelDeclaration(Lrn("FG7676767889"), "mrn", "description", "reason")

  lazy val allRecordsXmlMarshallingTest = allRecords.copy(
    items = Set(
      ExportItem(
        "itemid",
        sequenceId = 1,
        statisticalValue = Some(StatisticalValue(statisticalValue = "100")),
        documentsProducedData = Some(
          DocumentsProducedData(
            Seq(
              DocumentsProduced(
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
        commodityMeasure = Some(CommodityMeasure(Some("2"), "90", "100")),
        additionalFiscalReferencesData =
          Some(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("PL", "12345"), AdditionalFiscalReference("FR", "54321")))),
        procedureCodes = Some(ProcedureCodesData(Some("CUPR"), Seq("CC", "PR"))),
        packageInformation = Some(List(PackageInformation("AA", 2, "mark1"), PackageInformation("AB", 4, "mark2")))
      )
    ),
    totalNumberOfItems = Some(TotalNumberOfItems(Some("1212312.12"), Some("1212121.12345"), Some("123"))),
    parties = Parties(
      exporterDetails = Some(correctExporterDetails),
      declarantDetails = Some(correctDeclarantDetailsEORIOnly),
      consigneeDetails = Some(ConsigneeDetails(EntityDetailsSpec.correctEntityDetails)),
      representativeDetails = Some(correctRepresentativeDetails),
      declarationAdditionalActorsData = Some(DeclarationAdditionalActorsData(Seq(correctAdditionalActors1))),
      declarationHoldersData = Some(
        DeclarationHoldersData(
          Seq(
            DeclarationHolder(authorisationTypeCode = Some("1234"), eori = Some(Eori("PL213472539481923"))),
            DeclarationHolder(authorisationTypeCode = Some("4321"), eori = Some(Eori("PT213472539481923")))
          )
        )
      ),
      carrierDetails = Some(CarrierDetails(EntityDetailsSpec.correctEntityDetails))
    ),
    locations = Locations(
      originationCountry = Some("GB"),
      destinationCountry = Some("PL"),
      hasRoutingCountries = Some(true),
      routingCountries = Seq("FR"),
      goodsLocation = Some(correctGoodsLocation),
      officeOfExit = Some(correctOfficeOfExit),
      warehouseIdentification = Some(WarehouseIdentificationSpec.correctWarehouseDetails),
      supervisingCustomsOffice = Some(SupervisingCustomsOfficeSpec.correctSupervisingCustomsOffice),
      inlandModeOfTransportCode = Some(InlandModeOfTransportCodeSpec.correctInlandModeOfTransportCode)
    ),
    previousDocuments = Some(PreviousDocumentsData(Seq(Document("X", "MCR", "DocumentReference", Some("123")))))
  )

  lazy val correctGovernmentAgencyGoodsItemJSON: JsValue = JsArray(Seq(Json.toJson(createGovernmentAgencyGoodsItem())))
  lazy val correctStatisticalValueAmountJSON: JsValue =
    JsObject(Map("currencyId" -> JsString("GBP"), "value" -> JsString("44")))
  lazy val correctPackageInformationJSON: JsValue = JsArray(Seq(correctPackageInformationJSON))
  val date = Date(Some(12), Some(12), Some(2019))
  val correctPackingJSON: JsValue = JsObject(
    Map("sequenceNumeric" -> JsString("0"), "marksNumbersId" -> JsString("wefdsf"), "typeCode" -> JsString("22"))
  )
  val declaration =
    ExportsDeclaration(UUID.randomUUID.toString, DeclarationStatus.DRAFT, Instant.now(), Instant.now(), None, DeclarationType.SUPPLEMENTARY)

  def createGovernmentAgencyGoodsItem(): GovernmentAgencyGoodsItem =
    GovernmentAgencyGoodsItem(
      sequenceNumeric = 0,
      statisticalValueAmount = Some(Amount(Some("GBP"), Some(BigDecimal(12)))),
      commodity = None, //parsed from cached CommodityForm
      additionalInformations = Seq(),
      additionalDocuments = Seq(),
      governmentProcedures = Seq(),
      packagings = Seq()
    )
}
