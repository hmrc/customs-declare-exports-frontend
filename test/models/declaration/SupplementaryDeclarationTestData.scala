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

package models.declaration

import java.time.Instant

import forms.common.Date
import forms.declaration.ConsignmentReferencesSpec._
import forms.declaration.DeclarantDetailsSpec._
import forms.declaration.DeclarationAdditionalActorsSpec.correctAdditionalActors1
import forms.declaration.DestinationCountriesSpec._
import forms.declaration.DispatchLocation.AllowedDispatchLocations
import forms.declaration.DispatchLocationSpec._
import forms.declaration.ExporterDetailsSpec._
import forms.declaration.GoodsLocationTestData._
import forms.declaration.NatureOfTransactionSpec._
import forms.declaration.OfficeOfExitSupplementarySpec._
import forms.declaration.RepresentativeDetailsSpec._
import forms.declaration.TotalNumberOfItemsSpec._
import forms.declaration.TransportCodes.Rail
import forms.declaration.WarehouseIdentificationSpec._
import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDec.AllowedAdditionalDeclarationTypes
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDecSpec._
import forms.declaration.additionaldocuments.{DocumentIdentifierAndPart, DocumentWriteOff, DocumentsProduced}
import models.declaration.DeclarationAdditionalActorsDataSpec._
import models.declaration.DeclarationHoldersDataSpec._
import models.declaration.dectype.DeclarationTypeSupplementarySpec._
import models.declaration.governmentagencygoodsitem.Formats._
import models.declaration.governmentagencygoodsitem.{Amount, GovernmentAgencyGoodsItem}
import models.{DeclarationStatus, ExportsDeclaration}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json._
import services.cache.ExportItem

class SupplementaryDeclarationTestData extends WordSpec with MustMatchers {
  import SupplementaryDeclarationTestData._

  "Method apply(CacheMap)" should {
    "return SupplementaryDeclarationDataSpec" which {

      "has all fields equal to None" when {
        "CacheMap is empty" in {
          val supplementaryDeclarationData = SupplementaryDeclarationData(declaration)

          supplementaryDeclarationData.declarationType mustNot be(defined)
          supplementaryDeclarationData.consignmentReferences mustNot be(defined)
          supplementaryDeclarationData.parties mustNot be(defined)
          supplementaryDeclarationData.locations mustNot be(defined)
          supplementaryDeclarationData.transportInformationContainerData mustNot be(defined)
        }
      }

      "has properly formatted DeclarationType field" when {
        "CacheMap contains record for DispatchLocation only" in {
          val supplementaryDeclarationData =
            SupplementaryDeclarationData(declaration.copy(dispatchLocation = Some(correctDispatchLocation)))

          supplementaryDeclarationData.declarationType must be(defined)
          supplementaryDeclarationData.declarationType.get.dispatchLocation.get.dispatchLocation must equal(
            AllowedDispatchLocations.OutsideEU
          )
          supplementaryDeclarationData.declarationType.get.additionalDeclarationType mustNot be(defined)
        }

        "CacheMap contains record for AdditionalDeclarationType only" in {
          val supplementaryDeclarationData = SupplementaryDeclarationData(
            declaration.copy(additionalDeclarationType = Some(correctAdditionalDeclarationTypeSupplementaryDec))
          )

          supplementaryDeclarationData.declarationType must be(defined)
          supplementaryDeclarationData.declarationType.get.dispatchLocation mustNot be(defined)
          supplementaryDeclarationData.declarationType.get.additionalDeclarationType.get.additionalDeclarationType must equal(
            AllowedAdditionalDeclarationTypes.Simplified
          )
        }

        "CacheMap contains records for both DispatchLocation and AdditionalDeclarationType" in {
          val supplementaryDeclarationData = SupplementaryDeclarationData(
            declaration.copy(
              additionalDeclarationType = Some(correctAdditionalDeclarationTypeSupplementaryDec),
              dispatchLocation = Some(correctDispatchLocation)
            )
          )

          supplementaryDeclarationData.declarationType must be(defined)
          supplementaryDeclarationData.declarationType.get.dispatchLocation.get.dispatchLocation must equal(
            AllowedDispatchLocations.OutsideEU
          )
          supplementaryDeclarationData.declarationType.get.additionalDeclarationType.get.additionalDeclarationType must equal(
            AllowedAdditionalDeclarationTypes.Simplified
          )
        }

        "CacheMap contains records for neither DispatchLocation nor AdditionalDeclarationType" in {
          val supplementaryDeclarationData = SupplementaryDeclarationData(declaration)

          supplementaryDeclarationData.declarationType mustNot be(defined)
        }
      }

      "has properly mapped fields" when {
        "CacheMap contains single record" in {
          val consignmentReferences = Json.fromJson[ConsignmentReferences](correctConsignmentReferencesJSON).get

          val supplementaryDeclarationData =
            SupplementaryDeclarationData(declaration.copy(consignmentReferences = Some(correctConsignmentReferences)))

          supplementaryDeclarationData.consignmentReferences must be(defined)
          supplementaryDeclarationData.consignmentReferences.get.lrn must equal(consignmentReferences.lrn)
          supplementaryDeclarationData.consignmentReferences.get.ducr must be(defined)
          supplementaryDeclarationData.consignmentReferences.get.ducr.get must equal(consignmentReferences.ducr.get)
        }

        "CacheMap contains 2 records" in {
          val consignmentReferences = Json.fromJson[ConsignmentReferences](correctConsignmentReferencesJSON).get
          val exporterDetails = Json.fromJson[ExporterDetails](correctExporterDetailsJSON).get

          val supplementaryDeclarationData = SupplementaryDeclarationData(
            declaration.copy(
              consignmentReferences = Some(correctConsignmentReferences),
              parties = Parties(exporterDetails = Some(correctExporterDetails))
            )
          )

          supplementaryDeclarationData.consignmentReferences must be(defined)
          supplementaryDeclarationData.consignmentReferences.get.lrn must equal(consignmentReferences.lrn)
          supplementaryDeclarationData.consignmentReferences.get.ducr must be(defined)
          supplementaryDeclarationData.consignmentReferences.get.ducr.get must equal(consignmentReferences.ducr.get)
          supplementaryDeclarationData.parties must be(defined)
          supplementaryDeclarationData.parties.get.exporterDetails must be(defined)
          supplementaryDeclarationData.parties.get.exporterDetails.get must equal(exporterDetails)
        }

        "CacheMap contains all records" in {
          val supplementaryDeclarationData = SupplementaryDeclarationData(allRecords)

          supplementaryDeclarationData.declarationType must be(defined)
          supplementaryDeclarationData.consignmentReferences must be(defined)
          supplementaryDeclarationData.parties must be(defined)
          supplementaryDeclarationData.parties.get.exporterDetails must be(defined)
          supplementaryDeclarationData.parties.get.declarantDetails must be(defined)
          supplementaryDeclarationData.parties.get.representativeDetails must be(defined)
          supplementaryDeclarationData.parties.get.declarationAdditionalActorsData must be(defined)

          supplementaryDeclarationData.parties.get.declarationHoldersData must be(defined)
          supplementaryDeclarationData.locations must be(defined)
          supplementaryDeclarationData.locations.get.destinationCountries must be(defined)
          supplementaryDeclarationData.locations.get.goodsLocation must be(defined)
          supplementaryDeclarationData.locations.get.warehouseIdentification must be(defined)
          supplementaryDeclarationData.locations.get.officeOfExit must be(defined)
          supplementaryDeclarationData.transportInformationContainerData must be(defined)
        }
      }

    }
  }
}

object TransportInformationContainerSpec {
  private val containerId = "id"
  val correctTransportInformationContainerData =
    TransportInformationContainerData(Seq(TransportInformationContainer(id = "M1l3s")))
  val emptyTransportInformationContainerData = TransportInformationContainer("")
  val correctTransportInformationContainerJSON: JsValue = JsObject(Map(containerId -> JsString("container-M1l3s")))
  val incorrectTransportInformationContainerJSON: JsValue = JsObject(Map(containerId -> JsString("123456789012345678")))
  val emptyTransportInformationContainerJSON: JsValue = JsObject(Map(containerId -> JsString("")))
  val correctTransportInformationContainerDataJSON: JsValue = Json.toJson(correctTransportInformationContainerData)
}

object SupplementaryDeclarationTestData {
  import TransportInformationContainerSpec._
  lazy val allRecords = declaration.copy(
    dispatchLocation = Some(correctDispatchLocation),
    additionalDeclarationType = Some(correctAdditionalDeclarationTypeSupplementaryDec),
    consignmentReferences = Some(correctConsignmentReferences),
    transportDetails = Some(TransportDetails(Some("Portugal"), true, "40", Some("1234567878ui"), Some("A"))),
    containerData = Some(correctTransportInformationContainerData),
    natureOfTransaction = Some(correctNatureOfTransaction),
    totalNumberOfItems = Some(correctTotalNumberOfItemsDecimalValues),
    borderTransport = Some(BorderTransport("3", "10", Some("123112yu78"))),
    seals = Seq(Seal("123"), Seal("4321")),
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
      destinationCountries = Some(DestinationCountriesSpec.correctDestinationCountries),
      goodsLocation = Some(correctGoodsLocation),
      warehouseIdentification = Some(correctWarehouseIdentification),
      officeOfExit = Some(correctOfficeOfExit)
    )
  )

  lazy val allRecordsXmlMarshallingTest = allRecords.copy(
    items = Set(
      ExportItem(
        "itemid",
        sequenceId = 1,
        itemType = Some(
          ItemType(
            combinedNomenclatureCode = "classificationsId",
            taricAdditionalCodes = Seq("taricAdditionalCodes"),
            nationalAdditionalCodes = Seq("nationalAdditionalCodes"),
            descriptionOfGoods = "commodityDescription",
            cusCode = Some("cusCode"),
            unDangerousGoodsCode = Some("999"),
            statisticalValue = "100"
          )
        ),
        documentsProducedData = Some(
          DocumentsProducedData(
            Seq(
              DocumentsProduced(
                documentTypeCode = Some("C501"),
                documentIdentifierAndPart = Some(
                  DocumentIdentifierAndPart(documentIdentifier = Some("SYSUYSU123"), documentPart = Some("24554"))
                ),
                documentStatus = Some("PND"),
                documentStatusReason = Some("Reason"),
                issuingAuthorityName = Some("issuingAuthorityName"),
                dateOfValidity = Some(Date(year = Some(2017), month = Some(1), day = Some(1))),
                documentWriteOff =
                  Some(DocumentWriteOff(measurementUnit = Some("KGM"), documentQuantity = Some(BigDecimal("10"))))
              )
            )
          )
        ),
        additionalInformation = Some(AdditionalInformationData(Seq(AdditionalInformation("code", "description")))),
        commodityMeasure = Some(CommodityMeasure(Some("2"), "90", "100")),
        additionalFiscalReferencesData = Some(
          AdditionalFiscalReferencesData(
            Seq(AdditionalFiscalReference("PL", "12345"), AdditionalFiscalReference("FR", "54321"))
          )
        ),
        procedureCodes = Some(ProcedureCodesData(Some("CUPR"), Seq("CC", "PR"))),
        packageInformation = List(PackageInformation("AA", 2, "mark1"), PackageInformation("AB", 4, "mark2"))
      )
    ),
    totalNumberOfItems = Some(TotalNumberOfItems(Some("1212312.12"), Some("1212121.12345"), "123")),
    parties = Parties(
      exporterDetails = Some(correctExporterDetails),
      declarantDetails = Some(correctDeclarantDetailsEORIOnly),
      consigneeDetails = Some(ConsigneeDetails(EntityDetailsSpec.correctEntityDetails)),
      representativeDetails = Some(correctRepresentativeDetails),
      declarationAdditionalActorsData = Some(DeclarationAdditionalActorsData(Seq(correctAdditionalActors1))),
      declarationHoldersData = Some(
        DeclarationHoldersData(
          Seq(
            DeclarationHolder(authorisationTypeCode = Some("1234"), eori = Some("PL213472539481923")),
            DeclarationHolder(authorisationTypeCode = Some("4321"), eori = Some("PT213472539481923"))
          )
        )
      ),
      carrierDetails = Some(CarrierDetails(EntityDetailsSpec.correctEntityDetails))
    ),
    locations = Locations(
      destinationCountries = Some(DestinationCountriesSpec.correctDestinationCountries),
      goodsLocation = Some(correctGoodsLocation),
      warehouseIdentification =
        Some(WarehouseIdentification(Some("12345678"), Some("R"), Some("1234567GB"), Some(Rail))),
      officeOfExit = Some(correctOfficeOfExit)
    ),
    previousDocuments = Some(PreviousDocumentsData(Seq(Document("X", "MCR", "DocumentReference", Some("123")))))
  )

  lazy val correctGovernmentAgencyGoodsItemJSON: JsValue = JsArray(Seq(Json.toJson(createGovernmentAgencyGoodsItem())))
  lazy val correctStatisticalValueAmountJSON: JsValue =
    JsObject(Map("currencyId" -> JsString("GBP"), "value" -> JsString("44")))
  lazy val correctPackageInformationJSON: JsValue = JsArray(Seq(correctPackageInformationJSON))
  lazy val supplementaryDeclarationDataAllValues = SupplementaryDeclarationData(
    declarationType = Some(correctDeclarationType),
    consignmentReferences = Some(correctConsignmentReferences),
    parties = Some(
      Parties(
        exporterDetails = Some(correctExporterDetails),
        declarantDetails = Some(correctDeclarantDetails),
        representativeDetails = Some(correctRepresentativeDetails),
        declarationAdditionalActorsData = Some(correctAdditionalActorsData),
        declarationHoldersData = Some(correctDeclarationHoldersData)
      )
    ),
    locations = Some(
      Locations(
        destinationCountries = Some(correctDestinationCountries),
        goodsLocation = Some(correctGoodsLocation),
        warehouseIdentification = Some(correctWarehouseIdentification),
        officeOfExit = Some(correctOfficeOfExit)
      )
    ),
    transportInformationContainerData = Some(correctTransportInformationContainerData),
    items = Some(
      Items(
        totalNumberOfItems = Some(correctTotalNumberOfItemsDecimalValues),
        natureOfTransaction = Some(correctNatureOfTransaction)
      )
    )
  )
  val date = Date(Some(12), Some(12), Some(2019))
  val correctPackingJSON: JsValue = JsObject(
    Map("sequenceNumeric" -> JsString("0"), "marksNumbersId" -> JsString("wefdsf"), "typeCode" -> JsString("22"))
  )
  val declaration = ExportsDeclaration(None, DeclarationStatus.DRAFT, "sessionId", Instant.now(), Instant.now(), "SMP")

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
