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

package views.declaration.summary

import forms.common.Address
import models.declaration.DeclarationAdditionalActorsData
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.parties_section

class PartiesSectionViewSpec extends UnitViewSpec with ExportsTestData {

  val exampleEori = "GB123456"
  val exampleAddress = Address("fullName", "addressLine", "townOrCity", "postCode", "GB")
  val data = aDeclaration(
    withExporterDetails(Some(exampleEori), Some(exampleAddress)),
    withConsigneeDetails(Some(exampleEori), Some(exampleAddress)),
    withDeclarantDetails(Some(exampleEori), Some(exampleAddress)),
    withRepresentativeDetails(Some(exampleEori), Some(exampleAddress), Some("1")),
    withCarrierDetails(Some(exampleEori), Some(exampleAddress)),
    withDeclarationAdditionalActors(DeclarationAdditionalActorsData(Seq.empty)),
    withDeclarationHolders()
  )

  val view = parties_section(data)(messages, journeyRequest())

  "Parties section" should {

    "contains exporter details" in {

      view.getElementById("exporter-eori-label").text() mustBe messages("declaration.summary.parties.exporter.eori")
      view.getElementById("exporter-address-label").text() mustBe messages("declaration.summary.parties.exporter.address")
    }

    "contains consignee details" in {

      view.getElementById("consignee-eori-label").text() mustBe messages("declaration.summary.parties.consignee.eori")
      view.getElementById("consignee-address-label").text() mustBe messages("declaration.summary.parties.consignee.address")
    }

    "contains declarant details" in {

      view.getElementById("declarant-eori-label").text() mustBe messages("declaration.summary.parties.declarant.eori")
      view.getElementById("declarant-address-label").text() mustBe messages("declaration.summary.parties.declarant.address")
    }

    "contains representative details" in {

      view.getElementById("representative-eori-label").text() mustBe messages("declaration.summary.parties.representative.eori")
      view.getElementById("representative-address-label").text() mustBe messages("declaration.summary.parties.representative.address")
    }

    "contains carrier details" in {

      view.getElementById("carrier-eori-label").text() mustBe messages("declaration.summary.parties.carrier.eori")
      view.getElementById("carrier-address-label").text() mustBe messages("declaration.summary.parties.carrier.address")
    }

    "display status code" in {

      view.getElementById("representationType-label").text() mustBe messages("declaration.summary.parties.representative.type")
      view.getElementById("representationType").text() mustBe "Declarant"
    }

    "display additional actors section" in {

      view.getElementById("additionalActors-label").text() mustBe messages("declaration.summary.parties.additional")
    }

    "display holders section" in {

      view.getElementById("holders-label").text() mustBe messages("declaration.summary.parties.holders")
    }
  }
}
