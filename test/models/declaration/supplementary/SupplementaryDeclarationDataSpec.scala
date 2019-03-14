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

package models.declaration.supplementary

import forms.supplementary.AdditionalDeclarationType.AllowedAdditionalDeclarationTypes
import forms.supplementary.AdditionalDeclarationTypeSpec._
import forms.supplementary.AdditionalInformationSpec._
import forms.supplementary.ConsigneeDetailsSpec._
import forms.supplementary.ConsignmentReferencesSpec._
import forms.supplementary.DeclarantDetailsSpec._
import forms.supplementary.DestinationCountriesSpec._
import forms.supplementary.DispatchLocation.AllowedDispatchLocations
import forms.supplementary.DispatchLocationSpec._
import forms.supplementary.DocumentSpec._
import forms.supplementary.DocumentsProducedSpec._
import forms.supplementary.ExporterDetailsSpec._
import forms.supplementary.GoodsItemNumberSpec._
import forms.supplementary.GoodsLocationSpec._
import forms.supplementary.ItemTypeSpec._
import forms.supplementary.OfficeOfExitSpec._
import forms.supplementary.RepresentativeDetailsSpec._
import forms.supplementary.SupervisingCustomsOfficeSpec._
import forms.supplementary.TotalNumberOfItemsSpec._
import forms.supplementary.TransactionTypeSpec._
import forms.supplementary.TransportInformationSpec._
import forms.supplementary.WarehouseIdentificationSpec._
import forms.supplementary._
import models.declaration.supplementary.DeclarationAdditionalActorsDataSpec._
import models.declaration.supplementary.DeclarationHoldersDataSpec._
import models.declaration.supplementary.DeclarationTypeSpec._
import models.declaration.supplementary.ProcedureCodesDataSpec._
import models.declaration.supplementary.SupplementaryDeclarationData.suppDecFunctionCode
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
          supplementaryDeclarationData.transportInformation mustNot be(defined)
          supplementaryDeclarationData.items mustNot be(defined)
          supplementaryDeclarationData.previousDocuments mustNot be(defined)
          supplementaryDeclarationData.additionalInformationData mustNot be(defined)
          supplementaryDeclarationData.documentsProducedData mustNot be(defined)
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
            CacheMap("CacheID", Map(AdditionalDeclarationType.formId -> correctAdditionalDeclarationTypeJSON))
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
              AdditionalDeclarationType.formId -> correctAdditionalDeclarationTypeJSON
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

        "CacheMap contains 3 records" in {
          val consignmentReferences = Json.fromJson[ConsignmentReferences](correctConsignmentReferencesJSON).get
          val exporterDetails = Json.fromJson[ExporterDetails](correctExporterDetailsJSON).get
          val procedureCodes = Json.fromJson[ProcedureCodesData](correctProcedureCodesJSON).get

          val cacheMap = CacheMap(
            "CacheID",
            Map(
              ConsignmentReferences.id -> correctConsignmentReferencesJSON,
              ExporterDetails.id -> correctExporterDetailsJSON,
              ProcedureCodesData.formId -> correctProcedureCodesJSON
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
          supplementaryDeclarationData.locations must be(defined)
          supplementaryDeclarationData.locations.get.procedureCodesData must be(defined)
          supplementaryDeclarationData.locations.get.procedureCodesData.get.procedureCode must equal(
            procedureCodes.procedureCode
          )
          supplementaryDeclarationData.locations.get.procedureCodesData.get.additionalProcedureCodes must equal(
            procedureCodes.additionalProcedureCodes
          )
        }

        "CacheMap contains 3 records and 3 foreign keys" in {
          val consignmentReferences = Json.fromJson[ConsignmentReferences](correctConsignmentReferencesJSON).get
          val exporterDetails = Json.fromJson[ExporterDetails](correctExporterDetailsJSON).get
          val procedureCodes = Json.fromJson[ProcedureCodesData](correctProcedureCodesJSON).get

          val cacheMap = CacheMap(
            "CacheID",
            Map(
              ConsignmentReferences.id -> correctConsignmentReferencesJSON,
              ExporterDetails.id -> correctExporterDetailsJSON,
              ProcedureCodesData.formId -> correctProcedureCodesJSON,
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
          supplementaryDeclarationData.locations must be(defined)
          supplementaryDeclarationData.locations.get.procedureCodesData must be(defined)
          supplementaryDeclarationData.locations.get.procedureCodesData.get.procedureCode must be(defined)
          supplementaryDeclarationData.locations.get.procedureCodesData.get.procedureCode.get must equal(
            procedureCodes.procedureCode.get
          )
          supplementaryDeclarationData.locations.get.procedureCodesData.get.additionalProcedureCodes must equal(
            procedureCodes.additionalProcedureCodes
          )
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
          supplementaryDeclarationData.locations.get.procedureCodesData must be(defined)
          supplementaryDeclarationData.locations.get.supervisingCustomsOffice must be(defined)
          supplementaryDeclarationData.locations.get.warehouseIdentification must be(defined)
          supplementaryDeclarationData.locations.get.officeOfExit must be(defined)
          supplementaryDeclarationData.transportInformation must be(defined)
          supplementaryDeclarationData.items must be(defined)
          supplementaryDeclarationData.items.get.totalNumberOfItems must be(defined)
          supplementaryDeclarationData.items.get.transactionType must be(defined)
          supplementaryDeclarationData.items.get.goodsItemNumber must be(defined)
          supplementaryDeclarationData.items.get.itemType must be(defined)
          supplementaryDeclarationData.previousDocuments must be(defined)
          supplementaryDeclarationData.additionalInformationData must be(defined)
          supplementaryDeclarationData.documentsProducedData must be(defined)
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
        val additionalInformationData = correctAdditionalInformation
        val supplementaryDeclarationData = SupplementaryDeclarationData(
          declarationType = Some(declarationType),
          parties = Some(parties),
          additionalInformationData = Some(additionalInformationData)
        )

        val map = supplementaryDeclarationData.toMap

        map.size must equal(3)
        map.keys must contain(DeclarationType.id)
        map.get(DeclarationType.id) must be(defined)
        map(DeclarationType.id) must equal(declarationType)
        map.keys must contain(Parties.id)
        map.get(Parties.id) must be(defined)
        map(Parties.id) must equal(parties)
        map.keys must contain(AdditionalInformationData.formId)
        map.get(AdditionalInformationData.formId) must be(defined)
        map(AdditionalInformationData.formId) must equal(additionalInformationData)
      }
    }

    "SupplementaryDeclarationData contains all fields populated" should {
      "return Map with all elements" in {
        val data = supplementaryDeclarationDataAllValues

        val map = data.toMap

        val supplementaryDeclarationDataFieldsAmount = 9
        map.size must equal(supplementaryDeclarationDataFieldsAmount)

        map.keys must contain(DeclarationType.id)
        map(DeclarationType.id) must equal(data.declarationType.get)
        map.keys must contain(ConsignmentReferences.id)
        map(ConsignmentReferences.id) must equal(data.consignmentReferences.get)
        map.keys must contain(Parties.id)
        map(Parties.id) must equal(data.parties.get)
        map.keys must contain(Locations.id)
        map(Locations.id) must equal(data.locations.get)
        map.keys must contain(TransportInformation.id)
        map(TransportInformation.id) must equal(data.transportInformation.get)
        map.keys must contain(Items.id)
        map(Items.id) must equal(data.items.get)
        map.keys must contain(Document.formId)
        map(Document.formId) must equal(data.previousDocuments.get)
        map.keys must contain(AdditionalInformationData.formId)
        map(AdditionalInformationData.formId) must equal(data.additionalInformationData.get)
        map.keys must contain(DocumentsProducedData.formId)
        map(DocumentsProducedData.formId) must equal(data.documentsProducedData.get)
      }
    }

  }

  "Method toMetadataProperties" should {
    "invoke the same method on every data element contained" in new SimpleTest {
      supplementaryDeclarationData.toMetadataProperties()

      verify(declarationTypeMock, times(1)).toMetadataProperties()
      verify(consignmentReferencesMock, times(1)).toMetadataProperties()
      verify(partiesMock, times(1)).toMetadataProperties()
      verify(locationsMock, times(1)).toMetadataProperties()
      verify(transportInformationMock, times(1)).toMetadataProperties()
      verify(itemsMock, times(1)).toMetadataProperties()
      verify(previousDocumentsMock, times(1)).toMetadataProperties()
      verify(additionalInformationDataMock, times(1)).toMetadataProperties()
      verify(documentsProducedDataMock, times(1)).toMetadataProperties()
    }

    "return Map being summary of all data elements returned Maps" in new TestMapConcatenation {
      supplementaryDeclarationData.toMetadataProperties() must equal(
        functionCodeMap ++ declarationTypeMap ++ consignmentReferencesMap ++ partiesMap ++ locationsMap ++ transportInformationMap ++ itemsMap ++ previousDocumentsMap ++ additionalInformationMap ++ documentsProducedMap
      )
    }

    trait SimpleTest {
      val declarationTypeMock = mock(classOf[DeclarationType])
      val consignmentReferencesMock = mock(classOf[ConsignmentReferences])
      val partiesMock = mock(classOf[Parties])
      val locationsMock = mock(classOf[Locations])
      val transportInformationMock = mock(classOf[TransportInformation])
      val itemsMock = mock(classOf[Items])
      val previousDocumentsMock = mock(classOf[Document])
      val additionalInformationDataMock = mock(classOf[AdditionalInformationData])
      val documentsProducedDataMock = mock(classOf[DocumentsProducedData])
      val supplementaryDeclarationData = SupplementaryDeclarationData(
        declarationType = Some(declarationTypeMock),
        consignmentReferences = Some(consignmentReferencesMock),
        parties = Some(partiesMock),
        locations = Some(locationsMock),
        transportInformation = Some(transportInformationMock),
        items = Some(itemsMock),
        previousDocuments = Some(previousDocumentsMock),
        additionalInformationData = Some(additionalInformationDataMock),
        documentsProducedData = Some(documentsProducedDataMock)
      )

      when(declarationTypeMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
      when(consignmentReferencesMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
      when(partiesMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
      when(locationsMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
      when(transportInformationMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
      when(itemsMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
      when(previousDocumentsMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
      when(additionalInformationDataMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
      when(documentsProducedDataMock.toMetadataProperties()).thenReturn(Map.empty[String, String])
    }

    trait TestMapConcatenation extends SimpleTest {
      val functionCodeMap = Map("declaration.functionCode" -> suppDecFunctionCode)
      val declarationTypeMap = Map("DeclarationType" -> "DeclarationTypeValue")
      val consignmentReferencesMap = Map("ConsignmentReferences" -> "ConsignmentReferencesValue")
      val partiesMap = Map("Parties" -> "PartiesValue")
      val locationsMap = Map("Locations" -> "LocationsValue")
      val transportInformationMap = Map("TransportInformation" -> "TransportInformationValue")
      val itemsMap = Map("Items" -> "ItemsValue")
      val previousDocumentsMap = Map("PreviousDocuments" -> "PreviousDocumentsValue")
      val additionalInformationMap = Map("AdditionalInformation" -> "AdditionalInformationValue")
      val documentsProducedMap = Map("DocumentsProduced" -> "DocumentsProducedValue")
      when(declarationTypeMock.toMetadataProperties()).thenReturn(declarationTypeMap)
      when(consignmentReferencesMock.toMetadataProperties()).thenReturn(consignmentReferencesMap)
      when(partiesMock.toMetadataProperties()).thenReturn(partiesMap)
      when(locationsMock.toMetadataProperties()).thenReturn(locationsMap)
      when(transportInformationMock.toMetadataProperties()).thenReturn(transportInformationMap)
      when(itemsMock.toMetadataProperties()).thenReturn(itemsMap)
      when(previousDocumentsMock.toMetadataProperties()).thenReturn(previousDocumentsMap)
      when(additionalInformationDataMock.toMetadataProperties()).thenReturn(additionalInformationMap)
      when(documentsProducedDataMock.toMetadataProperties()).thenReturn(documentsProducedMap)
    }
  }

}

object SupplementaryDeclarationDataSpec {

  lazy val cacheMapAllRecords = CacheMap(
    id = "CacheID",
    data = Map(
      DispatchLocation.formId -> correctDispatchLocationJSON,
      AdditionalDeclarationType.formId -> correctAdditionalDeclarationTypeJSON,
      ConsignmentReferences.id -> correctConsignmentReferencesJSON,
      ExporterDetails.id -> correctExporterDetailsJSON,
      DeclarantDetails.id -> correctDeclarantDetailsJSON,
      RepresentativeDetails.formId -> correctRepresentativeDetailsJSON,
      ConsigneeDetails.id -> correctConsigneeDetailsJSON,
      DeclarationAdditionalActorsData.formId -> correctAdditionalActorsDataJSON,
      DeclarationHoldersData.formId -> correctDeclarationHoldersDataJSON,
      DestinationCountries.formId -> correctDestinationCountriesJSON,
      GoodsLocation.formId -> correctGoodsLocationJSON,
      ProcedureCodesData.formId -> correctProcedureCodesJSON,
      SupervisingCustomsOffice.formId -> correctSupervisingCustomsOfficeJSON,
      WarehouseIdentification.formId -> correctWarehouseIdentificationJSON,
      OfficeOfExit.formId -> correctOfficeOfExitJSON,
      TransportInformation.id -> correctTransportInformationJSON,
      TotalNumberOfItems.formId -> correctTotalNumberOfItemsDecimalValuesJSON,
      TransactionType.formId -> correctTransactionTypeJSON,
      GoodsItemNumber.formId -> correctGoodsItemNumberJSON,
      ItemType.id -> correctItemTypeJSON,
      Document.formId -> correctPreviousDocumentsJSON,
      AdditionalInformationData.formId -> correctAdditionalInformationDataJSON,
      DocumentsProducedData.formId -> correctDocumentsProducedDataJSON
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
        declarationHoldersData = Some(correctDeclarationHolder)
      )
    ),
    locations = Some(
      Locations(
        destinationCountries = Some(correctDestinationCountries),
        goodsLocation = Some(correctGoodsLocation),
        procedureCodesData = Some(correctProcedureCodes),
        supervisingCustomsOffice = Some(correctSupervisingCustomsOffice),
        warehouseIdentification = Some(correctWarehouseIdentification),
        officeOfExit = Some(correctOfficeOfExit)
      )
    ),
    transportInformation = Some(correctTransportInformation),
    items = Some(
      Items(
        totalNumberOfItems = Some(correctTotalNumberOfItemsDecimalValues),
        transactionType = Some(correctTransactionType),
        goodsItemNumber = Some(correctGoodsItemNumber),
        itemType = Some(correctItemType)
      )
    ),
    previousDocuments = Some(correctPreviousDocument),
    additionalInformationData = Some(correctAdditionalInformation),
    documentsProducedData = Some(correctDocumentsProducedData)
  )

}
