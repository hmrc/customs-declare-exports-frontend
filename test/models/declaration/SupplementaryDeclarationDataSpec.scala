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

import forms.declaration.ConsigneeDetailsSpec._
import forms.declaration.ConsignmentReferencesSpec._
import forms.declaration.DeclarantDetailsSpec._
import forms.declaration.DestinationCountriesSupplementarySpec._
import forms.declaration.DispatchLocation.AllowedDispatchLocations
import forms.declaration.DispatchLocationSpec._
import forms.declaration.ExporterDetailsSpec._
import forms.declaration.GoodsItemNumberSpec._
import forms.declaration.GoodsLocationSpec._
import forms.declaration.OfficeOfExitSpec._
import forms.declaration.RepresentativeDetailsSpec._
import forms.declaration.TotalNumberOfItemsSpec._
import forms.declaration.TransactionTypeSpec._
import forms.declaration.WarehouseIdentificationSpec._
import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDec
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDec.AllowedAdditionalDeclarationTypes
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDecSpec._
import forms.declaration.destinationCountries.DestinationCountries
import models.declaration.DeclarationAdditionalActorsDataSpec._
import models.declaration.DeclarationHoldersDataSpec._
import models.declaration.SupplementaryDeclarationData.SchemaMandatoryValues._
import models.declaration.dectype.DeclarationTypeSupplementary
import models.declaration.dectype.DeclarationTypeSupplementarySpec._
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, Json}
import uk.gov.hmrc.http.cache.client.CacheMap

class SupplementaryDeclarationDataSpec extends WordSpec with MustMatchers {
  import SupplementaryDeclarationDataSpec._

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
        }
      }

    }
  }

  "Method toMap" when {

    "SupplementaryDeclarationData contains no data" should {
      "return empty Map" in {
        val supplementaryDeclarationData = SupplementaryDeclarationData()
        val map = supplementaryDeclarationData.toMap

        map must equal(Map.empty)
      }
    }

    "SupplementaryDeclarationData contains single field populated" should {
      "return Map with single element" in {
        val parties = Parties(exporterDetails = Some(correctExporterDetails))
        val supplementaryDeclarationData = SupplementaryDeclarationData(parties = Some(parties))

        val map = supplementaryDeclarationData.toMap

        map.size must equal(1)
        map.keys must contain(Parties.id)
        map.get(Parties.id) must be(defined)
        map(Parties.id) must equal(parties)
      }
    }

    "SupplementaryDeclarationData contains 3 fields populated" should {
      "return Map with 3 elements" in {
        val declarationType = correctDeclarationType
        val parties = Parties(exporterDetails = Some(correctExporterDetails))
        val supplementaryDeclarationData =
          SupplementaryDeclarationData(declarationType = Some(declarationType), parties = Some(parties))

        val map = supplementaryDeclarationData.toMap

        map.size must equal(2)
        map.keys must contain(DeclarationTypeSupplementary.id)
        map.get(DeclarationTypeSupplementary.id) must be(defined)
        map(DeclarationTypeSupplementary.id) must equal(declarationType)
        map.keys must contain(Parties.id)
        map.get(Parties.id) must be(defined)
        map(Parties.id) must equal(parties)
      }
    }

    "SupplementaryDeclarationData contains all fields populated" should {
      "return Map with all elements" in {
        val data = supplementaryDeclarationDataAllValues

        val map = data.toMap

        val supplementaryDeclarationDataFieldsAmount = 5
        map.size must equal(supplementaryDeclarationDataFieldsAmount)

        map.keys must contain(DeclarationTypeSupplementary.id)
        map(DeclarationTypeSupplementary.id) must equal(data.declarationType.get)
        map.keys must contain(ConsignmentReferences.id)
        map(ConsignmentReferences.id) must equal(data.consignmentReferences.get)
        map.keys must contain(Parties.id)
        map(Parties.id) must equal(data.parties.get)
        map.keys must contain(Locations.id)
        map(Locations.id) must equal(data.locations.get)
        map.keys must contain(Items.id)
        map(Items.id) must equal(data.items.get)
      }
    }

  }

  "Method toMetadataProperties" should {

    "contain mandatory values" in new SimpleTest {
      val functionCodeTuple = ("declaration.functionCode", functionCode)
      val wcoDataModelVersionCodeTuple = ("wcoDataModelVersionCode", wcoDataModelVersionCode)
      val wcoTypeNameTuple = ("wcoTypeName", wcoTypeName)
      val responsibleCountryCodeTuple = ("responsibleCountryCode", responsibleCountryCode)
      val responsibleAgencyNameTuple = ("responsibleAgencyName", responsibleAgencyName)
      val agencyAssignedCustomizationVersionCodeTuple =
        ("agencyAssignedCustomizationVersionCode", agencyAssignedCustomizationVersionCode)

      val metadataProperties: Map[String, String] = supplementaryDeclarationData.toMetadataProperties()

      metadataProperties must contain(functionCodeTuple)
      metadataProperties must contain(wcoDataModelVersionCodeTuple)
      metadataProperties must contain(wcoTypeNameTuple)
      metadataProperties must contain(responsibleCountryCodeTuple)
      metadataProperties must contain(responsibleAgencyNameTuple)
      metadataProperties must contain(agencyAssignedCustomizationVersionCodeTuple)
    }

    "invoke the same method on every sub-element contained" in new SimpleTest {
      supplementaryDeclarationData.toMetadataProperties()

      verify(declarationTypeMock, times(1)).toMetadataProperties()
      verify(consignmentReferencesMock, times(1)).toMetadataProperties()
      verify(partiesMock, times(1)).toMetadataProperties()
      verify(locationsMock, times(1)).toMetadataProperties()
      verify(itemsMock, times(1)).toMetadataProperties()
    }

    "return Map being sum of all Maps returned by sub-elements" in new TestMapConcatenation {
      val metadataProperties: Map[String, String] = supplementaryDeclarationData.toMetadataProperties()

      metadataProperties must contain(declarationTypeTuple)
      metadataProperties must contain(consignmentReferencesTuple)
      metadataProperties must contain(partiesTuple)
      metadataProperties must contain(locationsTuple)
      metadataProperties must contain(itemsTuple)
    }
  }

  trait SimpleTest {
    val declarationTypeMock = mock(classOf[DeclarationTypeSupplementary])
    val consignmentReferencesMock = mock(classOf[ConsignmentReferences])
    val partiesMock = mock(classOf[Parties])
    val locationsMock = mock(classOf[Locations])
    val itemsMock = mock(classOf[Items])
    val supplementaryDeclarationData = SupplementaryDeclarationData(
      declarationType = Some(declarationTypeMock),
      consignmentReferences = Some(consignmentReferencesMock),
      parties = Some(partiesMock),
      locations = Some(locationsMock),
      items = Some(itemsMock)
    )

    when(declarationTypeMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    when(consignmentReferencesMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    when(partiesMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    when(locationsMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    when(itemsMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
  }

  trait TestMapConcatenation extends SimpleTest {
    val declarationTypeTuple = ("DeclarationType", "DeclarationTypeValue")
    val consignmentReferencesTuple = ("ConsignmentReferences", "ConsignmentReferencesValue")
    val partiesTuple = ("Parties", "PartiesValue")
    val locationsTuple = ("Locations", "LocationsValue")
    val itemsTuple = ("Items", "ItemsValue")
    when(declarationTypeMock.toMetadataProperties()).thenReturn(Map(declarationTypeTuple))
    when(consignmentReferencesMock.toMetadataProperties()).thenReturn(Map(consignmentReferencesTuple))
    when(partiesMock.toMetadataProperties()).thenReturn(Map(partiesTuple))
    when(locationsMock.toMetadataProperties()).thenReturn(Map(locationsTuple))
    when(itemsMock.toMetadataProperties()).thenReturn(Map(itemsTuple))
  }

}

object SupplementaryDeclarationDataSpec {

  lazy val cacheMapAllRecords = CacheMap(
    id = "CacheID",
    data = Map(
      DispatchLocation.formId -> correctDispatchLocationJSON,
      AdditionalDeclarationTypeSupplementaryDec.formId -> correctAdditionalDeclarationTypeSupplementaryDecJSON,
      ConsignmentReferences.id -> correctConsignmentReferencesJSON,
      ExporterDetails.id -> correctExporterDetailsJSON,
      DeclarantDetails.id -> correctDeclarantDetailsJSON,
      RepresentativeDetails.formId -> correctRepresentativeDetailsJSON,
      ConsigneeDetails.id -> correctConsigneeDetailsJSON,
      DeclarationAdditionalActorsData.formId -> correctAdditionalActorsDataJSON,
      DeclarationHoldersData.formId -> correctDeclarationHoldersDataJSON,
      DestinationCountries.formId -> correctDestinationCountriesSupplementaryJSON,
      GoodsLocation.formId -> correctGoodsLocationJSON,
      WarehouseIdentification.formId -> correctWarehouseIdentificationJSON,
      OfficeOfExit.formId -> correctOfficeOfExitJSON,
      TotalNumberOfItems.formId -> correctTotalNumberOfItemsDecimalValuesJSON,
      TransactionType.formId -> correctTransactionTypeJSON,
      GoodsItemNumber.formId -> correctGoodsItemNumberJSON
    )
  )

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
        destinationCountries = Some(correctDestinationCountriesSupplementary),
        goodsLocation = Some(correctGoodsLocation),
        warehouseIdentification = Some(correctWarehouseIdentification),
        officeOfExit = Some(correctOfficeOfExit)
      )
    ),
    items = Some(
      Items(
        totalNumberOfItems = Some(correctTotalNumberOfItemsDecimalValues),
        transactionType = Some(correctTransactionType),
        goodsItemNumber = Some(correctGoodsItemNumber)
      )
    )
  )
}
