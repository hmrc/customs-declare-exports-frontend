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

package services.mapping.goodsshipment.consignment

import forms.declaration._
import models.ExportsCacheModel
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.verify
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import services.cache.ExportsCacheModelBuilder
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment

class ConsignmentBuilderSpec extends WordSpec with Matchers with ExportsCacheModelBuilder with MockitoSugar {

  private val mockContainerCodeBuilder = mock[ContainerCodeBuilder]
  private val mockGoodsLocationBuilder = mock[GoodsLocationBuilder]
  private val mockDepartureTransportMeansBuilder = mock[DepartureTransportMeansBuilder]
  private val mockArrivalTransportMeansBuilder = mock[ArrivalTransportMeansBuilder]
  private val mockTransportEquipmentBuilder = mock[TransportEquipmentBuilder]

  private val builder = new ConsignmentBuilder(
    mockGoodsLocationBuilder,
    mockContainerCodeBuilder,
    mockDepartureTransportMeansBuilder,
    mockArrivalTransportMeansBuilder,
    mockTransportEquipmentBuilder
  )

  "ConsignmentBuilder" should {

    "correctly map to the WCO-DEC GoodsShipment.Consignment instance" when {
      "correct data is present" in {
        val borderModeOfTransportCode = "BCode"
        val meansOfTransportOnDepartureType = "T"
        val meansOfTransportOnDepartureIDNumber = "12345"

        val model: ExportsCacheModel =
          aCacheModel(
            withGoodsLocation(GoodsLocationTestData.correctGoodsLocation),
            withBorderTransport(
              borderModeOfTransportCode,
              meansOfTransportOnDepartureType,
              Some(meansOfTransportOnDepartureIDNumber)
            ),
            withWarehouseIdentification(WarehouseIdentificationSpec.correctWarehouseIdentification),
            withTransportDetails(Some("Portugal"), container = true, "40", Some("1234567878ui"), Some("A")),
            withSeals(Seq(Seal("first"), Seal("second")))
          )

        val goodsShipment: Declaration.GoodsShipment = new Declaration.GoodsShipment

        builder.buildThenAdd(model, goodsShipment)

        verify(mockGoodsLocationBuilder)
          .buildThenAdd(refEq(GoodsLocationTestData.correctGoodsLocation), any[GoodsShipment.Consignment])

        verify(mockContainerCodeBuilder)
          .buildThenAdd(
            refEq(
              TransportDetails(
                meansOfTransportCrossingTheBorderNationality = Some("Portugal"),
                container = true,
                meansOfTransportCrossingTheBorderType = "40",
                meansOfTransportCrossingTheBorderIDNumber = Some("1234567878ui"),
                paymentMethod = Some("A")
              )
            ),
            any[GoodsShipment.Consignment]
          )

        verify(mockDepartureTransportMeansBuilder)
          .buildThenAdd(
            refEq(
              BorderTransport(
                borderModeOfTransportCode,
                meansOfTransportOnDepartureType,
                Some(meansOfTransportOnDepartureIDNumber)
              )
            ),
            any[GoodsShipment.Consignment]
          )

        verify(mockArrivalTransportMeansBuilder)
          .buildThenAdd(
            refEq(WarehouseIdentificationSpec.correctWarehouseIdentification),
            any[GoodsShipment.Consignment]
          )

        verify(mockTransportEquipmentBuilder)
          .buildThenAdd(refEq(Seq(Seal("first"), Seal("second"))), any[GoodsShipment.Consignment])

      }
    }
  }
}
