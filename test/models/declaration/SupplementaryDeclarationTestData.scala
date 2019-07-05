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

import forms.common.Date
import forms.declaration.ConsigneeDetailsSpec._
import forms.declaration.ConsignmentReferencesSpec._
import forms.declaration.DeclarantDetailsSpec._
import forms.declaration.DestinationCountriesSpec._
import forms.declaration.DispatchLocation.AllowedDispatchLocations
import forms.declaration.DispatchLocationSpec._
import forms.declaration.ExporterDetailsSpec._
import forms.declaration.   GoodsLocationTestData._
import forms.declaration.NatureOfTransactionSpec._
import forms.declaration.OfficeOfExitSupplementarySpec._
import forms.declaration.RepresentativeDetailsSpec._
import forms.declaration.TotalNumberOfItemsSpec._
import forms.declaration.TransportInformationContainerSpec.{
  correctTransportInformationContainerData,
  correctTransportInformationContainerDataJSON
}
import forms.declaration.WarehouseIdentificationSpec._
import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDec
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDec.AllowedAdditionalDeclarationTypes
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDecSpec._
import forms.declaration.additionaldocuments.{DocumentIdentifierAndPart, DocumentWriteOff, DocumentsProduced}
import forms.declaration.destinationCountries.DestinationCountries
import forms.declaration.officeOfExit.OfficeOfExitForms
import forms.{Choice, ChoiceSpec}
import models.declaration.DeclarationAdditionalActorsDataSpec._
import models.declaration.DeclarationHoldersDataSpec._
import models.declaration.dectype.DeclarationTypeSupplementarySpec._
import models.declaration.governmentagencygoodsitem.Formats._
import models.declaration.governmentagencygoodsitem.{Amount, GovernmentAgencyGoodsItem}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json._
import services.ExportsItemsCacheIds
import services.mapping.governmentagencygoodsitem.GovernmentAgencyGoodsItemBuilderSpec
import uk.gov.hmrc.http.cache.client.CacheMap

class SupplementaryDeclarationTestData extends WordSpec with MustMatchers {
  import SupplementaryDeclarationTestData._

  "Method apply(CacheMap)" should {
    "return SupplementaryDeclarationDataSpec" which {

      "has all fields equal to None" when {
        "CacheMap is empty" in {
          val emptyCacheMap = CacheMap("CacheID", Map.empty)
          val supplementaryDeclarationData = SupplementaryDeclarationData(emptyCacheMap)

          assertAllNone(supplementaryDeclarationData)
        }

        "CacheMap contains only foreign keys" in {
          val cacheMap = CacheMap("CacheID", Map("ForeignKey1234567" -> JsObject(Map("key" -> JsString("value")))))
          val supplementaryDeclarationData = SupplementaryDeclarationData(cacheMap)

          assertAllNone(supplementaryDeclarationData)
        }

        def assertAllNone(supplementaryDeclarationData: SupplementaryDeclarationData): Unit = {
          supplementaryDeclarationData.declarationType mustNot be(defined)
          supplementaryDeclarationData.consignmentReferences mustNot be(defined)
          supplementaryDeclarationData.parties mustNot be(defined)
          supplementaryDeclarationData.locations mustNot be(defined)
          supplementaryDeclarationData.transportInformationContainerData mustNot be(defined)
        }
      }

      "has properly formatted DeclarationType field" when {
        "CacheMap contains record for DispatchLocation only" in {
          val cacheMap = CacheMap("CacheID", Map(DispatchLocation.formId -> correctDispatchLocationJSON))
          val supplementaryDeclarationData = SupplementaryDeclarationData(cacheMap)

          supplementaryDeclarationData.declarationType must be(defined)
          supplementaryDeclarationData.declarationType.get.dispatchLocation.get.dispatchLocation must equal(
            AllowedDispatchLocations.OutsideEU
          )
          supplementaryDeclarationData.declarationType.get.additionalDeclarationType mustNot be(defined)
        }

        "CacheMap contains record for AdditionalDeclarationType only" in {
          val cacheMap =
            CacheMap(
              "CacheID",
              Map(
                AdditionalDeclarationTypeSupplementaryDec.formId -> correctAdditionalDeclarationTypeSupplementaryDecJSON
              )
            )
          val supplementaryDeclarationData = SupplementaryDeclarationData(cacheMap)

          supplementaryDeclarationData.declarationType must be(defined)
          supplementaryDeclarationData.declarationType.get.dispatchLocation mustNot be(defined)
          supplementaryDeclarationData.declarationType.get.additionalDeclarationType.get.additionalDeclarationType must equal(
            AllowedAdditionalDeclarationTypes.Simplified
          )
        }

        "CacheMap contains records for both DispatchLocation and AdditionalDeclarationType" in {
          val cacheMap = CacheMap(
            "CacheID",
            Map(
              DispatchLocation.formId -> correctDispatchLocationJSON,
              AdditionalDeclarationTypeSupplementaryDec.formId -> correctAdditionalDeclarationTypeSupplementaryDecJSON
            )
          )

          val supplementaryDeclarationData = SupplementaryDeclarationData(cacheMap)

          supplementaryDeclarationData.declarationType must be(defined)
          supplementaryDeclarationData.declarationType.get.dispatchLocation.get.dispatchLocation must equal(
            AllowedDispatchLocations.OutsideEU
          )
          supplementaryDeclarationData.declarationType.get.additionalDeclarationType.get.additionalDeclarationType must equal(
            AllowedAdditionalDeclarationTypes.Simplified
          )
        }

        "CacheMap contains records for neither DispatchLocation nor AdditionalDeclarationType" in {
          val emptyCacheMap = CacheMap("CacheID", Map.empty)
          val supplementaryDeclarationData = SupplementaryDeclarationData(emptyCacheMap)

          supplementaryDeclarationData.declarationType mustNot be(defined)
        }
      }

      "has properly mapped fields" when {
        "CacheMap contains single record" in {
          val consignmentReferences = Json.fromJson[ConsignmentReferences](correctConsignmentReferencesJSON).get
          val cacheMap = CacheMap("CacheID", Map(ConsignmentReferences.id -> correctConsignmentReferencesJSON))

          val supplementaryDeclarationData = SupplementaryDeclarationData(cacheMap)

          supplementaryDeclarationData.consignmentReferences must be(defined)
          supplementaryDeclarationData.consignmentReferences.get.lrn must equal(consignmentReferences.lrn)
          supplementaryDeclarationData.consignmentReferences.get.ducr must be(defined)
          supplementaryDeclarationData.consignmentReferences.get.ducr.get must equal(consignmentReferences.ducr.get)
        }

        "CacheMap contains 2 records" in {
          val consignmentReferences = Json.fromJson[ConsignmentReferences](correctConsignmentReferencesJSON).get
          val exporterDetails = Json.fromJson[ExporterDetails](correctExporterDetailsJSON).get

          val cacheMap = CacheMap(
            "CacheID",
            Map(
              ConsignmentReferences.id -> correctConsignmentReferencesJSON,
              ExporterDetails.id -> correctExporterDetailsJSON
            )
          )

          val supplementaryDeclarationData = SupplementaryDeclarationData(cacheMap)

          supplementaryDeclarationData.consignmentReferences must be(defined)
          supplementaryDeclarationData.consignmentReferences.get.lrn must equal(consignmentReferences.lrn)
          supplementaryDeclarationData.consignmentReferences.get.ducr must be(defined)
          supplementaryDeclarationData.consignmentReferences.get.ducr.get must equal(consignmentReferences.ducr.get)
          supplementaryDeclarationData.parties must be(defined)
          supplementaryDeclarationData.parties.get.exporterDetails must be(defined)
          supplementaryDeclarationData.parties.get.exporterDetails.get must equal(exporterDetails)
        }

        "CacheMap contains 2 records and 3 foreign keys" in {
          val consignmentReferences = Json.fromJson[ConsignmentReferences](correctConsignmentReferencesJSON).get
          val exporterDetails = Json.fromJson[ExporterDetails](correctExporterDetailsJSON).get

          val cacheMap = CacheMap(
            "CacheID",
            Map(
              ConsignmentReferences.id -> correctConsignmentReferencesJSON,
              ExporterDetails.id -> correctExporterDetailsJSON,
              "ForeignKey_1" -> JsObject(Map("key_1" -> JsString("value_1"))),
              "ForeignKey_2" -> JsObject(Map("key_2" -> JsString("value_2"))),
              "ForeignKey_3" -> JsObject(Map("key_3" -> JsString("value_3")))
            )
          )

          val supplementaryDeclarationData = SupplementaryDeclarationData(cacheMap)

          supplementaryDeclarationData.consignmentReferences must be(defined)
          supplementaryDeclarationData.consignmentReferences.get.lrn must equal(consignmentReferences.lrn)
          supplementaryDeclarationData.consignmentReferences.get.ducr must be(defined)
          supplementaryDeclarationData.consignmentReferences.get.ducr.get must equal(consignmentReferences.ducr.get)
          supplementaryDeclarationData.parties must be(defined)
          supplementaryDeclarationData.parties.get.exporterDetails must be(defined)
          supplementaryDeclarationData.parties.get.exporterDetails.get must equal(exporterDetails)
        }

        "CacheMap contains all records" in {
          val supplementaryDeclarationData = SupplementaryDeclarationData(cacheMapAllRecords)

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

object SupplementaryDeclarationTestData {
  lazy val cacheMapAllRecords = CacheMap(
    id = "CacheID",
    data = Map(
      Choice.choiceId -> ChoiceSpec.correctSupplementaryChoiceJSON,
      DispatchLocation.formId -> correctDispatchLocationJSON,
      AdditionalDeclarationTypeSupplementaryDec.formId -> correctAdditionalDeclarationTypeSupplementaryDecJSON,
      ConsignmentReferences.id -> correctConsignmentReferencesJSON,
      ExporterDetails.id -> correctExporterDetailsJSON,
      DeclarantDetails.id -> Json.toJson(DeclarantDetailsSpec.correctDeclarantDetailsEORIOnly),
      RepresentativeDetails.formId -> correctRepresentativeDetailsJSON,
      Document.formId -> DocumentSpec.correctPreviousDocumentsJSONList,
      CarrierDetails.id -> CarrierDetailsSpec.correctCarrierDetailsJSON,
      ConsigneeDetails.id -> correctConsigneeDetailsJSON,
      DeclarationAdditionalActorsData.formId -> correctAdditionalActorsDataJSON,
      DeclarationHoldersData.formId -> correctDeclarationHoldersDataJSON,
      DestinationCountries.formId -> correctDestinationCountriesJSON,
      GoodsLocation.formId -> correctGoodsLocationJSON,
      WarehouseIdentification.formId -> correctWarehouseIdentificationJSON,
      OfficeOfExitForms.formId -> correctOfficeOfExitJSON,
      TransportInformationContainerData.id -> correctTransportInformationContainerDataJSON,
      TransportDetails.formId -> Json.toJson(
        TransportDetails(Some("Portugal"), true, "40", Some("1234567878ui"), Some("A"))
      ),
      BorderTransport.formId -> Json.toJson(BorderTransport("3", "10", Some("123112yu78"))),
      TotalNumberOfItems.formId -> correctTotalNumberOfItemsDecimalValuesJSON,
      NatureOfTransaction.formId -> correctNatureOfTransactionJSON,
      DeclarationAdditionalActors.formId -> correctAdditionalActorsDataJSON,
      AdditionalInformationData.formId -> Json.toJson(
        AdditionalInformationData(items = Seq(AdditionalInformation("code", "description")))
      ),
      DocumentsProducedData.formId -> Json.toJson(
        DocumentsProducedData(
          documents = Seq(
            DocumentsProduced(
              documentTypeCode = Some("123"),
              documentIdentifierAndPart = Some(DocumentIdentifierAndPart(Some("docId"), Some("DocPart"))),
              documentStatus = Some("567"),
              documentStatusReason = Some("statusReason"),
              issuingAuthorityName = Some("issuingAuthName"),
              dateOfValidity = Some(date),
              documentWriteOff =
                Some(DocumentWriteOff(measurementUnit = Some("KGM"), documentQuantity = Some(BigDecimal(12))))
            )
          )
        )
      ),
      ProcedureCodesData.formId -> Json.toJson(
        ProcedureCodesData(procedureCode = Some("procedureCode"), additionalProcedureCodes = Seq("code1", "code2"))
      ),
      PackageInformation.formId -> Json.toJson(
        Seq(
          PackageInformation(typesOfPackages = Some("AA"), numberOfPackages = Some(2), shippingMarks = Some("mark1")),
          PackageInformation(typesOfPackages = Some("AB"), numberOfPackages = Some(4), shippingMarks = Some("mark2"))
        )
      ),
      ExportsItemsCacheIds.itemsId -> GovernmentAgencyGoodsItemBuilderSpec.itemsJsonList,
      CommodityMeasure.commodityFormId -> Json.toJson(
        CommodityMeasure(supplementaryUnits = Some("2"), netMass = "23", grossMass = "23")
      ),
      ItemType.id -> Json.toJson(
        ItemType(
          combinedNomenclatureCode = "nonClamCode",
          taricAdditionalCodes = Seq("tarricCode1", "tarricCode2", "tarricCode3"),
          nationalAdditionalCodes = Seq("nationalAddCode1", "nationalAddCode2", "nationalAddCode3"),
          descriptionOfGoods = "description",
          cusCode = Some("CustCode"),
          unDangerousGoodsCode = Some("999"),
          statisticalValue = "12"
        )
      )
    )
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
