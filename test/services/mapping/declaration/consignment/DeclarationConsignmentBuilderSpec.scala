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
import forms.declaration.destinationCountries.DestinationCountries
import forms.declaration.{CarrierDetails, CarrierDetailsSpec, TransportDetails}
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json
import services.mapping.declaration.DeclarationConsignmentBuilder
import uk.gov.hmrc.http.cache.client.CacheMap

class DeclarationConsignmentBuilderSpec extends WordSpec with Matchers {

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
  }
}
