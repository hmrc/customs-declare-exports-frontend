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
import forms.declaration.TransportDetails
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json
import services.cache.ExportsCacheModelBuilder
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration

class FreightBuilderSpec extends WordSpec with Matchers with ExportsCacheModelBuilder {

  "FreightBuilder" should {
    "correctly map to the WCO-DEC Consignment.Freight instance" when {
      "when a payment method has been submitted" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(TransportDetails.formId -> Json.toJson(TransportDetails(None, false, "A", None, Some("A"))))
          )
        val freight = FreightBuilder.build(cacheMap, Choice(AllowedChoiceValues.StandardDec))
        freight.getPaymentMethodCode.getValue should be("A")
      }

      "not map to the WCO-DEC Consignment.Freight instance" when {
        "no payment method has been submitted" in {
          implicit val cacheMap: CacheMap =
            CacheMap(
              "CacheID",
              Map(TransportDetails.formId -> Json.toJson(TransportDetails(None, false, "A", None, None)))
            )
          val freight = FreightBuilder.build(cacheMap, Choice(AllowedChoiceValues.StandardDec))
          freight should be(null)
        }
      }
    }

    "build then add" when {
      "no transport details" in {
        // Given
        val model = aCacheModel(withoutTransportDetails())
        val consignment = new Declaration.Consignment()

        // When
        new FreightBuilder().buildThenAdd(model, consignment)

        // Then
        consignment.getFreight shouldBe null
      }

      "payment method is empty" in {
        // Given
        val model = aCacheModel(withTransportDetails(paymentMethod = None))
        val consignment = new Declaration.Consignment()

        // When
        new FreightBuilder().buildThenAdd(model, consignment)

        // Then
        consignment.getFreight shouldBe null
      }

      "payment method is populated" in {
        // Given
        val model = aCacheModel(withTransportDetails(paymentMethod = Some("method")))
        val consignment = new Declaration.Consignment()

        // When
        new FreightBuilder().buildThenAdd(model, consignment)

        // Then
        consignment.getFreight.getPaymentMethodCode.getValue shouldBe "method"
      }
    }
  }
}
