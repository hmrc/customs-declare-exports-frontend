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

package services.mapping.declaration.consignment
import forms.Choice
import forms.Choice.AllowedChoiceValues
import forms.common.Address
import forms.declaration.destinationCountries.DestinationCountries
import forms.declaration.{CarrierDetails, CarrierDetailsSpec, TransportDetails}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import play.api.libs.json.Json
import services.cache.{ExportsCacheModel, ExportsCacheModelBuilder}
import services.mapping.goodsshipment.consignment.ConsignmentCarrierBuilder
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration

class DeclarationConsignmentBuilderSpec extends WordSpec with Matchers with MockitoSugar with BeforeAndAfterEach with ExportsCacheModelBuilder {

  private val freightBuilder = mock[FreightBuilder]
  private val iteneraryBuilder = mock[IteneraryBuilder]
  private val consignmentCarrierBuilder = mock[ConsignmentCarrierBuilder]

  private def builder = new DeclarationConsignmentBuilder(freightBuilder, iteneraryBuilder, consignmentCarrierBuilder)

  override def afterEach(): Unit = {
    reset(freightBuilder,  iteneraryBuilder, consignmentCarrierBuilder)
  }

  "DeclarationConsignmentBuilder" should {
    "correctly map to the WCO-DEC Consignment instance" when {
      "when a payment method, routing countries and Carrier have been submitted" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(
              TransportDetails.formId -> Json.toJson(TransportDetails(None, false, "A", None, Some("A"))),
              DestinationCountries.formId -> Json.toJson(DestinationCountries("GB", Seq("FR", "DE"), "PL")),
              CarrierDetails.id -> CarrierDetailsSpec.correctCarrierDetailsJSON
            )
          )
        val consignment = DeclarationConsignmentBuilder.build(cacheMap, Choice(AllowedChoiceValues.StandardDec))
        consignment.getFreight.getPaymentMethodCode.getValue should be("A")
        consignment.getItinerary.size() should be(2)
        consignment.getItinerary.get(0).getSequenceNumeric.intValue() should be(0)
        consignment.getItinerary.get(0).getRoutingCountryCode.getValue should be("FR")
        consignment.getItinerary.get(1).getSequenceNumeric.intValue() should be(1)
        consignment.getItinerary.get(1).getRoutingCountryCode.getValue should be("DE")
        consignment.getCarrier.getID.getValue should be("9GB1234567ABCDEF")
        consignment.getCarrier.getAddress.getLine.getValue should be("Address Line")
        consignment.getCarrier.getAddress.getCityName.getValue should be("Town or City")
        consignment.getCarrier.getAddress.getPostcodeID.getValue should be("AB12 34CD")
        consignment.getCarrier.getAddress.getCountryCode.getValue should be("PL")
        consignment.getCarrier.getName.getValue should be("Full Name")
      }

      "not map to the WCO-DEC Consignment instance" when {
        "no payment method or routing countries have been submitted" in {
          implicit val cacheMap: CacheMap =
            CacheMap(
              "CacheID",
              Map(
                TransportDetails.formId -> Json.toJson(TransportDetails(None, false, "A", None, None)),
                DestinationCountries.formId -> Json.toJson(DestinationCountries("GB", Seq(), "FR")),
                CarrierDetails.id -> CarrierDetailsSpec.correctCarrierDetailsEORIOnlyJSON
              )
            )
          val consignment = DeclarationConsignmentBuilder.build(cacheMap, Choice(AllowedChoiceValues.StandardDec))
          consignment.getFreight should be(null)
          consignment.getItinerary.size() should be(0)
          consignment.getCarrier.getID.getValue should be("9GB1234567ABCDEF")
        }
      }
    }

    "build then add" when {

      "standard journey" in {
        // Given
        val model = aCacheModel(withChoice(AllowedChoiceValues.StandardDec))
        val declaration = new Declaration()

        // When
        builder.buildThenAdd(model, declaration)

        // Then
        declaration.getConsignment should not be(null)
        verify(freightBuilder).buildThenAdd(refEq(model), any[Declaration.Consignment])
        verify(iteneraryBuilder).buildThenAdd(refEq(model), any[Declaration.Consignment])
        verify(consignmentCarrierBuilder).buildThenAdd(refEq(model), any[Declaration.Consignment])
      }

      "other journey" in {
        // Given
        val model = aCacheModel(withChoice("other"))
        val declaration = new Declaration()

        // When
        builder.buildThenAdd(model, declaration)

        // Then
        declaration.getConsignment shouldBe null
        verify(freightBuilder, never()).buildThenAdd(refEq(model), any[Declaration.Consignment])
        verify(iteneraryBuilder, never()).buildThenAdd(refEq(model), any[Declaration.Consignment])
        verify(consignmentCarrierBuilder, never()).buildThenAdd(refEq(model), any[Declaration.Consignment])
      }
    }
  }

}
