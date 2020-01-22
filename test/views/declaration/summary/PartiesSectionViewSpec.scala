/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.declaration.Eori
import models.declaration.DeclarationAdditionalActorsData
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.parties_section

class PartiesSectionViewSpec extends UnitViewSpec with ExportsTestData {

  val exampleEori = "GB123456"
  val exampleAddress = Address("fullName", "addressLine", "townOrCity", "postCode", "GB")
  val data = aDeclaration(
    withExporterDetails(Some(Eori(exampleEori)), Some(exampleAddress)),
    withConsigneeDetails(Some(Eori(exampleEori)), Some(exampleAddress)),
    withDeclarantDetails(Some(Eori(exampleEori)), Some(exampleAddress)),
    withRepresentativeDetails(Some(Eori(exampleEori)), Some(exampleAddress), Some("1")),
    withCarrierDetails(Some(Eori(exampleEori)), Some(exampleAddress)),
    withDeclarationAdditionalActors(DeclarationAdditionalActorsData(Seq.empty)),
    withDeclarationHolders()
  )

  "Parties section" must {

    val view = parties_section(data)(messages, journeyRequest())

    "contains exporter details with change button" in {

      view.getElementById("exporter-eori-label").text() mustBe messages("declaration.summary.parties.exporter.eori")
      view.getElementById("exporter-address-label").text() mustBe messages("declaration.summary.parties.exporter.address")

      val List(change1, accessibleChange1) = view.getElementById("exporter-eori-change").text().split(" ").toList

      change1 mustBe messages("site.change")
      accessibleChange1 mustBe messages("declaration.summary.parties.exporter.eori.change")

      view.getElementById("exporter-eori-change") must haveHref(controllers.declaration.routes.ExporterDetailsController.displayPage())

      val List(change2, accessibleChange2) = view.getElementById("exporter-address-change").text().split(" ").toList

      change2 mustBe messages("site.change")
      accessibleChange2 mustBe messages("declaration.summary.parties.exporter.address.change")

      view.getElementById("exporter-address-change") must haveHref(controllers.declaration.routes.ExporterDetailsController.displayPage())
    }

    "contains consignee details with change button" in {

      view.getElementById("consignee-eori-label").text() mustBe messages("declaration.summary.parties.consignee.eori")
      view.getElementById("consignee-address-label").text() mustBe messages("declaration.summary.parties.consignee.address")

      val List(change1, accessibleChange1) = view.getElementById("consignee-eori-change").text().split(" ").toList

      change1 mustBe messages("site.change")
      accessibleChange1 mustBe messages("declaration.summary.parties.consignee.eori.change")

      view.getElementById("consignee-eori-change") must haveHref(controllers.declaration.routes.ConsigneeDetailsController.displayPage())

      val List(change2, accessibleChange2) = view.getElementById("consignee-address-change").text().split(" ").toList

      change2 mustBe messages("site.change")
      accessibleChange2 mustBe messages("declaration.summary.parties.consignee.address.change")

      view.getElementById("consignee-address-change") must haveHref(controllers.declaration.routes.ConsigneeDetailsController.displayPage())
    }

    "contains declarant details with change button" in {

      view.getElementById("declarant-eori-label").text() mustBe messages("declaration.summary.parties.declarant.eori")
      view.getElementById("declarant-address-label").text() mustBe messages("declaration.summary.parties.declarant.address")

      val List(change1, accessibleChange1) = view.getElementById("declarant-eori-change").text().split(" ").toList

      change1 mustBe messages("site.change")
      accessibleChange1 mustBe messages("declaration.summary.parties.declarant.eori.change")

      view.getElementById("declarant-eori-change") must haveHref(controllers.declaration.routes.DeclarantDetailsController.displayPage())

      val List(change2, accessibleChange2) = view.getElementById("declarant-address-change").text().split(" ").toList

      change2 mustBe messages("site.change")
      accessibleChange2 mustBe messages("declaration.summary.parties.declarant.address.change")

      view.getElementById("declarant-address-change") must haveHref(controllers.declaration.routes.DeclarantDetailsController.displayPage())
    }

    "contains representative details with change button" in {

      view.getElementById("representative-eori-label").text() mustBe messages("declaration.summary.parties.representative.eori")
      view.getElementById("representative-address-label").text() mustBe messages("declaration.summary.parties.representative.address")

      val List(change1, accessibleChange1) = view.getElementById("representative-eori-change").text().split(" ").toList

      change1 mustBe messages("site.change")
      accessibleChange1 mustBe messages("declaration.summary.parties.representative.eori.change")

      view.getElementById("representative-eori-change") must haveHref(controllers.declaration.routes.RepresentativeDetailsController.displayPage())

      val List(change2, accessibleChange2) = view.getElementById("representative-address-change").text().split(" ").toList

      change2 mustBe messages("site.change")
      accessibleChange2 mustBe messages("declaration.summary.parties.representative.address.change")

      view.getElementById("representative-address-change") must haveHref(controllers.declaration.routes.RepresentativeDetailsController.displayPage())
    }

    "display status code with change button" in {

      view.getElementById("representationType-label").text() mustBe messages("declaration.summary.parties.representative.type")
      view.getElementById("representationType").text() mustBe messages("declaration.summary.parties.representative.type.1")

      val List(change, accessibleChange) = view.getElementById("representationType-change").text().split(" ").toList

      change mustBe messages("site.change")
      accessibleChange mustBe messages("declaration.summary.parties.representative.type.change")

      view.getElementById("representationType-change") must haveHref(controllers.declaration.routes.RepresentativeDetailsController.displayPage())
    }

    "display additional actors section" in {

      view.getElementById("additionalActors-label").text() mustBe messages("declaration.summary.parties.additional")
    }

    "display holders section" in {

      view.getElementById("holders-label").text() mustBe messages("declaration.summary.parties.holders")
    }
  }

  "contains carrier details with change button" in {
    val view = parties_section(data)(messages, journeyRequest())

    view.getElementById("carrier-eori-label").text() mustBe messages("declaration.summary.parties.carrier.eori")
    view.getElementById("carrier-address-label").text() mustBe messages("declaration.summary.parties.carrier.address")

    val List(change1, accessibleChange1) = view.getElementById("carrier-eori-change").text().split(" ").toList

    change1 mustBe messages("site.change")
    accessibleChange1 mustBe messages("declaration.summary.parties.carrier.eori.change")

    view.getElementById("carrier-eori-change") must haveHref(controllers.declaration.routes.CarrierDetailsController.displayPage())

    val List(change2, accessibleChange2) = view.getElementById("carrier-address-change").text().split(" ").toList

    change2 mustBe messages("site.change")
    accessibleChange2 mustBe messages("declaration.summary.parties.carrier.address.change")

    view.getElementById("carrier-address-change") must haveHref(controllers.declaration.routes.CarrierDetailsController.displayPage())
  }

  "does not contain exporter when section not answered" in {
    val view = parties_section(aDeclarationAfter(data, withoutExporterDetails()))(messages, journeyRequest())

    view.getElementById("exporter-eori-label") mustBe null
    view.getElementById("exporter-address-label") mustBe null
  }

  "does not contain consignee when section not answered" in {
    val view = parties_section(aDeclarationAfter(data, withoutConsigneeDetails()))(messages, journeyRequest())

    view.getElementById("consignee-eori-label") mustBe null
    view.getElementById("consignee-address-label") mustBe null
  }

  "does not contain declarant when section not answered" in {
    val view = parties_section(aDeclarationAfter(data, withoutDeclarantDetails()))(messages, journeyRequest())

    view.getElementById("declarant-eori-label") mustBe null
    view.getElementById("declarant-address-label") mustBe null
  }

  "does not contain representative when section not answered" in {
    val view = parties_section(aDeclarationAfter(data, withoutRepresentativeDetails()))(messages, journeyRequest())

    view.getElementById("representative-eori-label") mustBe null
    view.getElementById("representative-address-label") mustBe null
  }

  "does not contain carrier details when section not answered" in {
    val view = parties_section(aDeclarationAfter(data, withoutCarrierDetails()))(messages, journeyRequest())

    view.getElementById("carrier-eori-label") mustBe null
    view.getElementById("carrier-address-label") mustBe null
  }

  "does not contain anything when there are no parties" in {
    val view = parties_section(aDeclaration())(messages, journeyRequest())

    view.getAllElements.text() must be(empty)
  }

}
