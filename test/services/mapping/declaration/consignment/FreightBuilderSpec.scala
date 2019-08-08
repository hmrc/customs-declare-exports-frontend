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
import org.scalatest.{Matchers, WordSpec}
import services.cache.ExportsDeclarationBuilder
import wco.datamodel.wco.dec_dms._2.Declaration

class FreightBuilderSpec extends WordSpec with Matchers with ExportsDeclarationBuilder {

  "FreightBuilder" should {

    "build then add" when {
      "no transport details" in {
        // Given
        val model = aDeclaration(withoutTransportDetails())
        val consignment = new Declaration.Consignment()

        // When
        new FreightBuilder().buildThenAdd(model, consignment)

        // Then
        consignment.getFreight shouldBe null
      }

      "payment method is empty" in {
        // Given
        val model = aDeclaration(withTransportDetails(paymentMethod = None))
        val consignment = new Declaration.Consignment()

        // When
        new FreightBuilder().buildThenAdd(model, consignment)

        // Then
        consignment.getFreight shouldBe null
      }

      "payment method is populated" in {
        // Given
        val model = aDeclaration(withTransportDetails(paymentMethod = Some("method")))
        val consignment = new Declaration.Consignment()

        // When
        new FreightBuilder().buildThenAdd(model, consignment)

        // Then
        consignment.getFreight.getPaymentMethodCode.getValue shouldBe "method"
      }
    }
  }
}
