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

package services.mapping.goodsshipment

import forms.declaration.ConsigneeDetailsSpec.correctConsigneeDetailsFull
import forms.declaration.ConsignmentReferencesSpec.correctConsignmentReferences
import forms.declaration.DeclarationAdditionalActorsSpec.{correctAdditionalActors1, correctAdditionalActors2}
import forms.declaration.DestinationCountriesSpec.correctDestinationCountries
import forms.declaration.DocumentSpec.correctPreviousDocument
import forms.declaration.GoodsLocationTestData.correctGoodsLocation
import forms.declaration.NatureOfTransactionSpec.correctNatureOfTransaction
import forms.declaration.PreviousDocumentsData
import forms.declaration.WarehouseIdentificationSpec.correctWarehouseIdentification
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.verify
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import services.cache.{ExportItem, ExportsCacheModelBuilder}
import services.mapping.goodsshipment.consignment.ConsignmentBuilder
import services.mapping.governmentagencygoodsitem.GovernmentAgencyGoodsItemBuilder
import wco.datamodel.wco.dec_dms._2.Declaration

class GoodsShipmentBuilderSpec extends WordSpec with Matchers with ExportsCacheModelBuilder with MockitoSugar {

  private val mockGoodsShipmentNatureOfTransactionBuilder = mock[GoodsShipmentNatureOfTransactionBuilder]
  private val mockConsigneeBuilder = mock[ConsigneeBuilder]
  private val mockConsignmentBuilder = mock[ConsignmentBuilder]
  private val mockDestinationBuilder = mock[DestinationBuilder]
  private val mockExportCountryBuilder = mock[ExportCountryBuilder]
  private val mockUcrBuilder = mock[UCRBuilder]
  private val mockWarehouseBuilder = mock[WarehouseBuilder]
  private val mockPreviousDocumentBuilder = mock[PreviousDocumentsBuilder]
  private val mockAEOMutualRecognitionPartiesBuilder = mock[AEOMutualRecognitionPartiesBuilder]
  private val governmentAgencyItemBuilder = mock[GovernmentAgencyGoodsItemBuilder]

  private def builder = new GoodsShipmentBuilder(
    mockGoodsShipmentNatureOfTransactionBuilder,
    mockConsigneeBuilder,
    mockConsignmentBuilder,
    mockDestinationBuilder,
    mockExportCountryBuilder,
    governmentAgencyItemBuilder,
    mockUcrBuilder,
    mockWarehouseBuilder,
    mockPreviousDocumentBuilder,
    mockAEOMutualRecognitionPartiesBuilder
  )

  "GoodsShipmentBuilder" should {

    "build then add" when {
      "full declaration" in {
        val model = aCacheModel(
          withNatureOfTransaction(correctNatureOfTransaction),
          withConsigneeDetails(correctConsigneeDetailsFull),
          withDeclarationAdditionalActors(correctAdditionalActors1, correctAdditionalActors2),
          withGoodsLocation(correctGoodsLocation),
          withDestinationCountries(correctDestinationCountries),
          withWarehouseIdentification(correctWarehouseIdentification),
          withConsignmentReferences(correctConsignmentReferences),
          withPreviousDocuments(correctPreviousDocument),
          withItem()
        )
        val declaration = new Declaration()

        builder.buildThenAdd(model, declaration)
        verify(mockGoodsShipmentNatureOfTransactionBuilder)
          .buildThenAdd(refEq(correctNatureOfTransaction), any[Declaration.GoodsShipment])

        verify(mockConsigneeBuilder)
          .buildThenAdd(refEq(correctConsigneeDetailsFull), any[Declaration.GoodsShipment])

        verify(mockConsignmentBuilder)
          .buildThenAdd(refEq(model), any[Declaration.GoodsShipment])

        verify(mockDestinationBuilder)
          .buildThenAdd(refEq(correctDestinationCountries), any[Declaration.GoodsShipment])

        verify(mockGoodsShipmentNatureOfTransactionBuilder)
          .buildThenAdd(refEq(correctNatureOfTransaction), any[Declaration.GoodsShipment])

        verify(mockExportCountryBuilder)
          .buildThenAdd(refEq(correctDestinationCountries), any[Declaration.GoodsShipment])

        verify(mockUcrBuilder)
          .buildThenAdd(refEq(correctConsignmentReferences), any[Declaration.GoodsShipment])

        verify(mockWarehouseBuilder)
          .buildThenAdd(refEq(correctWarehouseIdentification), any[Declaration.GoodsShipment])

        verify(mockPreviousDocumentBuilder)
          .buildThenAdd(refEq(PreviousDocumentsData(Seq(correctPreviousDocument))), any[Declaration.GoodsShipment])

        verify(governmentAgencyItemBuilder).buildThenAdd(any[ExportItem], any[Declaration.GoodsShipment])

      }
    }
  }

}
