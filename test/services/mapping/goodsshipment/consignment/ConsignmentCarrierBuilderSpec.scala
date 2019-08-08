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
import forms.common.Address
import org.scalatest.{Matchers, WordSpec}
import services.cache.ExportsDeclarationBuilder
import wco.datamodel.wco.dec_dms._2.Declaration

class ConsignmentCarrierBuilderSpec extends WordSpec with Matchers with ExportsDeclarationBuilder {

  "ConsignmentCarrierBuilderSpec" should {

    "build then add" when {
      "no carrier details" in {
        // Given
        val model = aDeclaration(withoutCarrierDetails())
        val consignment = new Declaration.Consignment()

        // When
        new ConsignmentCarrierBuilder().buildThenAdd(model, consignment)

        // Then
        consignment.getCarrier shouldBe null
      }

      "address is empty" in {
        // Given
        val model = aDeclaration(withCarrierDetails(eori = Some("eori"), address = None))
        val consignment = new Declaration.Consignment()

        // When
        new ConsignmentCarrierBuilder().buildThenAdd(model, consignment)

        // Then
        consignment.getCarrier.getAddress shouldBe null
      }

      "eori is empty" in {
        // Given
        val model = aDeclaration(
          withCarrierDetails(eori = None, address = Some(Address("name", "line", "city", "postcode", "United Kingdom")))
        )
        val consignment = new Declaration.Consignment()

        // When
        new ConsignmentCarrierBuilder().buildThenAdd(model, consignment)

        // Then
        consignment.getCarrier.getID shouldBe null
      }

      "fully populated" in {
        // Given
        val model = aDeclaration(
          withCarrierDetails(
            eori = Some("eori"),
            address = Some(Address("name", "line", "city", "postcode", "United Kingdom"))
          )
        )
        val consignment = new Declaration.Consignment()

        // When
        new ConsignmentCarrierBuilder().buildThenAdd(model, consignment)

        // Then
        consignment.getCarrier.getID.getValue shouldBe "eori"
        consignment.getCarrier.getName.getValue shouldBe "name"
        consignment.getCarrier.getAddress.getLine.getValue shouldBe "line"
        consignment.getCarrier.getAddress.getCityName.getValue shouldBe "city"
        consignment.getCarrier.getAddress.getPostcodeID.getValue shouldBe "postcode"
        consignment.getCarrier.getAddress.getCountryCode.getValue shouldBe "GB"
      }

      "empty address components" in {
        // Given
        val model = aDeclaration(withCarrierDetails(eori = Some("eori"), address = Some(Address("", "", "", "", ""))))
        val consignment = new Declaration.Consignment()

        // When
        new ConsignmentCarrierBuilder().buildThenAdd(model, consignment)

        // Then
        consignment.getCarrier.getName shouldBe null
        consignment.getCarrier.getAddress shouldBe null
      }

      "invalid country" in {
        // Given
        val model = aDeclaration(
          withCarrierDetails(eori = Some("eori"), address = Some(Address("name", "line", "city", "postcode", "other")))
        )
        val consignment = new Declaration.Consignment()

        // When
        new ConsignmentCarrierBuilder().buildThenAdd(model, consignment)

        // Then
        consignment.getCarrier.getAddress.getCountryCode.getValue shouldBe ""
      }
    }
  }
}
