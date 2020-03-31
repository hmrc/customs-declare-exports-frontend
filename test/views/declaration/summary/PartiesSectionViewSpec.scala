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

import base.Injector
import forms.common.{Address, Eori}
import models.Mode
import models.declaration.DeclarationAdditionalActorsData
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.parties_section_gds

class PartiesSectionViewSpec extends UnitViewSpec with ExportsTestData with Injector {

  val exampleEori = "GB123456"
  val exampleAddress = Address("fullName", "addressLine", "townOrCity", "postCode", "GB")
  val exampleAddressContents = "fullName addressLine townOrCity postCode GB"
  val data = aDeclaration(
    withExporterDetails(Some(Eori(exampleEori)), Some(exampleAddress)),
    withConsigneeDetails(Some(Eori(exampleEori)), Some(exampleAddress)),
    withDeclarantDetails(Some(Eori(exampleEori)), Some(exampleAddress)),
    withRepresentativeDetails(Some(Eori(exampleEori)), Some(exampleAddress), Some("1")),
    withCarrierDetails(Some(Eori(exampleEori)), Some(exampleAddress)),
    withDeclarationAdditionalActors(DeclarationAdditionalActorsData(Seq.empty)),
    withDeclarationHolders()
  )

  private val section = instanceOf[parties_section_gds]

  "Parties section" must {

    onEveryDeclarationJourney() { request =>
      val view = section(Mode.Change, data)(messages, request)

      "contains exporter details with change button" in {

        val eoriRow = view.getElementsByClass("exporter-eori-row")

        eoriRow must haveSummaryKey(messages("declaration.summary.parties.exporter.eori"))
        eoriRow must haveSummaryValue(exampleEori)
        eoriRow must haveSummaryActionsText("site.change declaration.summary.parties.exporter.eori.change")
        eoriRow must haveSummaryActionsHref(controllers.declaration.routes.ExporterDetailsController.displayPage(Mode.Change))

        val addressRow = view.getElementsByClass("exporter-address-row")

        addressRow must haveSummaryKey(messages("declaration.summary.parties.exporter.address"))
        addressRow must haveSummaryValue(exampleAddressContents)
        addressRow must haveSummaryActionsText("site.change declaration.summary.parties.exporter.address.change")
        addressRow must haveSummaryActionsHref(controllers.declaration.routes.ExporterDetailsController.displayPage(Mode.Change))
      }

      "contains consignee details with change button" in {

        val eoriRow = view.getElementsByClass("consignee-eori-row")

        eoriRow must haveSummaryKey(messages("declaration.summary.parties.consignee.eori"))
        eoriRow must haveSummaryValue(exampleEori)
        eoriRow must haveSummaryActionsText("site.change declaration.summary.parties.consignee.eori.change")
        eoriRow must haveSummaryActionsHref(controllers.declaration.routes.ConsigneeDetailsController.displayPage(Mode.Change))

        val addressRow = view.getElementsByClass("consignee-address-row")

        addressRow must haveSummaryKey(messages("declaration.summary.parties.consignee.address"))
        addressRow must haveSummaryValue(exampleAddressContents)
        addressRow must haveSummaryActionsText("site.change declaration.summary.parties.consignee.address.change")
        addressRow must haveSummaryActionsHref(controllers.declaration.routes.ConsigneeDetailsController.displayPage(Mode.Change))
      }

      "contains declarant details with change button" in {

        val eoriRow = view.getElementsByClass("declarant-eori-row")

        eoriRow must haveSummaryKey(messages("declaration.summary.parties.declarant.eori"))
        eoriRow must haveSummaryValue(exampleEori)
        eoriRow must haveSummaryActionsText("site.change declaration.summary.parties.declarant.eori.change")
        eoriRow must haveSummaryActionsHref(controllers.declaration.routes.DeclarantDetailsController.displayPage(Mode.Change))

        val addressRow = view.getElementsByClass("declarant-address-row")

        addressRow must haveSummaryKey(messages("declaration.summary.parties.declarant.address"))
        addressRow must haveSummaryValue(exampleAddressContents)
        addressRow must haveSummaryActionsText("site.change declaration.summary.parties.declarant.address.change")
        addressRow must haveSummaryActionsHref(controllers.declaration.routes.DeclarantDetailsController.displayPage(Mode.Change))
      }

      "contains representative details with change button" in {

        val eoriRow = view.getElementsByClass("representative-eori-row")

        eoriRow must haveSummaryKey(messages("declaration.summary.parties.representative.eori"))
        eoriRow must haveSummaryValue(exampleEori)
        eoriRow must haveSummaryActionsText("site.change declaration.summary.parties.representative.eori.change")
        eoriRow must haveSummaryActionsHref(controllers.declaration.routes.RepresentativeDetailsController.displayPage(Mode.Change))

        val addressRow = view.getElementsByClass("representative-address-row")

        addressRow must haveSummaryKey(messages("declaration.summary.parties.representative.address"))
        addressRow must haveSummaryValue(exampleAddressContents)
        addressRow must haveSummaryActionsText("site.change declaration.summary.parties.representative.address.change")
        addressRow must haveSummaryActionsHref(controllers.declaration.routes.RepresentativeDetailsController.displayPage(Mode.Change))
      }

      "display status code with change button" in {

        val row = view.getElementsByClass("representationType-row")

        row must haveSummaryKey(messages("declaration.summary.parties.representative.type"))
        row must haveSummaryValue(messages("declaration.summary.parties.representative.type.1"))

        row must haveSummaryActionsText("site.change declaration.summary.parties.representative.type.change")
        row must haveSummaryActionsHref(controllers.declaration.routes.RepresentativeDetailsController.displayPage(Mode.Change))
      }

      "display additional actors section" in {

        val row = view.getElementsByClass("additionalActors-row")

        row must haveSummaryKey(messages("declaration.summary.parties.additional"))
        row must haveSummaryValue(messages("site.no"))
      }

      "display holders section" in {

        val row = view.getElementsByClass("holders-row")

        row must haveSummaryKey(messages("declaration.summary.parties.holders"))
        row must haveSummaryValue(messages("site.no"))
      }

      "contains carrier details with change button" in {

        val eoriRow = view.getElementsByClass("carrier-eori-row")

        eoriRow must haveSummaryKey(messages("declaration.summary.parties.carrier.eori"))
        eoriRow must haveSummaryValue(exampleEori)
        eoriRow must haveSummaryActionsText("site.change declaration.summary.parties.carrier.eori.change")
        eoriRow must haveSummaryActionsHref(controllers.declaration.routes.CarrierDetailsController.displayPage(Mode.Change))

        val addressRow = view.getElementsByClass("carrier-address-row")

        addressRow must haveSummaryKey(messages("declaration.summary.parties.carrier.address"))
        addressRow must haveSummaryValue(exampleAddressContents)
        addressRow must haveSummaryActionsText("site.change declaration.summary.parties.carrier.address.change")
        addressRow must haveSummaryActionsHref(controllers.declaration.routes.CarrierDetailsController.displayPage(Mode.Change))
      }
    }
  }

  "does not contain exporter when section not answered" in {
    val view = section(Mode.Normal, aDeclarationAfter(data, withoutExporterDetails()))(messages, journeyRequest())

    view.getElementsByClass("exporter-eori-row") mustBe empty
    view.getElementsByClass("exporter-address-row") mustBe empty
  }

  "does not contain consignee when section not answered" in {
    val view = section(Mode.Normal, aDeclarationAfter(data, withoutConsigneeDetails()))(messages, journeyRequest())

    view.getElementsByClass("consignee-eori-row") mustBe empty
    view.getElementsByClass("consignee-address-row") mustBe empty
  }

  "does not contain declarant when section not answered" in {
    val view = section(Mode.Normal, aDeclarationAfter(data, withoutDeclarantDetails()))(messages, journeyRequest())

    view.getElementsByClass("declarant-eori-row") mustBe empty
    view.getElementsByClass("declarant-address-row") mustBe empty
  }

  "does not contain representative when section not answered" in {
    val view = section(Mode.Normal, aDeclarationAfter(data, withoutRepresentativeDetails()))(messages, journeyRequest())

    view.getElementsByClass("representative-eori-row") mustBe empty
    view.getElementsByClass("representative-address-row") mustBe empty
  }

  "does not contain carrier details when section not answered" in {
    val view = section(Mode.Normal, aDeclarationAfter(data, withoutCarrierDetails()))(messages, journeyRequest())

    view.getElementsByClass("carrier-eori-row") mustBe empty
    view.getElementsByClass("carrier-address-row") mustBe empty
  }

  "does not contain anything when there are no parties" in {
    val view = section(Mode.Normal, aDeclaration())(messages, journeyRequest())

    view.getAllElements.text() must be(empty)
  }

}
