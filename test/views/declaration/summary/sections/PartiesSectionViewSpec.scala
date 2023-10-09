/*
 * Copyright 2023 HM Revenue & Customs
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

package views.declaration.summary.sections

import base.Injector
import controllers.declaration.routes._
import forms.common.{Address, Eori}
import forms.declaration.IsExs
import forms.declaration.declarationHolder.DeclarationHolder
import models.declaration.{DeclarationAdditionalActorsData, RepresentativeDetails}
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.sections.parties_section

class PartiesSectionViewSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  private val exampleEori = "GB123456"
  private val exampleAddress = Address("fullName", "addressLine", "townOrCity", "postCode", "GB")
  private val exampleAddressContents = "fullName addressLine townOrCity postCode GB"
  private val data = aDeclaration(
    withEntryIntoDeclarantsRecords("Yes"),
    withPersonPresentingGoodsDetails(Some(Eori(exampleEori))),
    withDeclarantIsExporter("No"),
    withIsExs(IsExs("No")),
    withExporterDetails(Some(Eori(exampleEori)), Some(exampleAddress)),
    withConsigneeDetails(Some(Eori(exampleEori)), Some(exampleAddress)),
    withDeclarantDetails(Some(Eori(exampleEori)), Some(exampleAddress)),
    withRepresentativeDetails(Some(Eori(exampleEori)), Some("1"), Some("Yes")),
    withCarrierDetails(Some(Eori(exampleEori)), Some(exampleAddress)),
    withDeclarationAdditionalActors(DeclarationAdditionalActorsData(Seq.empty)),
    withDeclarationHolders()
  )

  private val partiesSection = instanceOf[parties_section]

  "Parties section" must {

    onEveryDeclarationJourney() { implicit request =>
      val view = partiesSection(data)(messages)

      "contains 'are you exporter' with change button" in {
        val isExporterRow = view.getElementsByClass("declarantIsExporter-row")

        isExporterRow must haveSummaryKey(messages("declaration.summary.parties.declarantIsExporter"))
        isExporterRow must haveSummaryValue(messages("site.no"))
        isExporterRow must haveSummaryActionsTexts("site.change", "declaration.summary.parties.declarantIsExporter.change")
        isExporterRow must haveSummaryActionWithPlaceholder(DeclarantExporterController.displayPage)
      }

      "contains exporter details with change button" in {
        val eoriRow = view.getElementsByClass("exporter-eori-row")

        eoriRow must haveSummaryKey(messages("declaration.summary.parties.exporter.eori"))
        eoriRow must haveSummaryValue(exampleEori)
        eoriRow must haveSummaryActionsTexts("site.change", "declaration.summary.parties.exporter.eori.change")
        eoriRow must haveSummaryActionWithPlaceholder(ExporterEoriNumberController.displayPage)

        val addressRow = view.getElementsByClass("exporter-address-row")

        addressRow must haveSummaryKey(messages("declaration.summary.parties.exporter.address"))
        addressRow must haveSummaryValue(exampleAddressContents)
        addressRow must haveSummaryActionsTexts("site.change", "declaration.summary.parties.exporter.address.change")
        addressRow must haveSummaryActionWithPlaceholder(ExporterEoriNumberController.displayPage)
      }

      "contains consignee details with change button" in {
        val eoriRow = view.getElementsByClass("consignee-eori-row")

        eoriRow must haveSummaryKey(messages("declaration.summary.parties.consignee.eori"))
        eoriRow must haveSummaryValue(exampleEori)
        eoriRow must haveSummaryActionsTexts("site.change", "declaration.summary.parties.consignee.eori.change")
        eoriRow must haveSummaryActionWithPlaceholder(ConsigneeDetailsController.displayPage)

        val addressRow = view.getElementsByClass("consignee-address-row")

        addressRow must haveSummaryKey(messages("declaration.summary.parties.consignee.address"))
        addressRow must haveSummaryValue(exampleAddressContents)
        addressRow must haveSummaryActionsTexts("site.change", "declaration.summary.parties.consignee.address.change")
        addressRow must haveSummaryActionWithPlaceholder(ConsigneeDetailsController.displayPage)
      }

      "contains declarant eori" in {
        val eoriRow = view.getElementsByClass("declarant-eori-row")

        eoriRow must haveSummaryKey(messages("declaration.summary.parties.declarant.eori"))
        eoriRow must haveSummaryValue(exampleEori)
      }

      "contains representing another agent with change button" in {
        val eoriRow = view.getElementsByClass("representingAnotherAgent-row")

        eoriRow must haveSummaryKey(messages("declaration.summary.parties.representative.agent"))
        eoriRow must haveSummaryValue(messages("site.yes"))
        eoriRow must haveSummaryActionsTexts("site.change", "declaration.summary.parties.representative.agent.change")
        eoriRow must haveSummaryActionWithPlaceholder(RepresentativeAgentController.displayPage)
      }

      "contains representative eori with change button" in {
        val eoriRow = view.getElementsByClass("representative-eori-row")

        eoriRow must haveSummaryKey(messages("declaration.summary.parties.representative.eori"))
        eoriRow must haveSummaryValue(exampleEori)
        eoriRow must haveSummaryActionsTexts("site.change", "declaration.summary.parties.representative.eori.change")
        eoriRow must haveSummaryActionWithPlaceholder(RepresentativeEntityController.displayPage)
      }

      "does not contains representative eori when not representing another agent" in {
        val nonAgentView =
          partiesSection(data.copy(parties = data.parties.copy(representativeDetails = Some(RepresentativeDetails(None, Some("2"), Some("No"))))))(
            messages
          )
        val eoriRow = nonAgentView.getElementsByClass("representative-eori-row")

        eoriRow.text() must be(empty)
      }

      "contains representative status code with change button" in {
        val row = view.getElementsByClass("representationType-row")

        row must haveSummaryKey(messages("declaration.summary.parties.representative.type"))
        row must haveSummaryValue(messages("declaration.summary.parties.representative.type.1"))

        row must haveSummaryActionsTexts("site.change", "declaration.summary.parties.representative.type.change")
        row must haveSummaryActionWithPlaceholder(RepresentativeStatusController.displayPage)
      }

      "contains additional actors section" in {
        val row = view.getElementsByClass("additionalActors-row")

        row must haveSummaryKey(messages("declaration.summary.parties.actors"))
        row must haveSummaryValue(messages("site.no"))
      }

      "contains carrier details with change button" in {
        val eoriRow = view.getElementsByClass("carrier-eori-row")

        eoriRow must haveSummaryKey(messages("declaration.summary.parties.carrier.eori"))
        eoriRow must haveSummaryValue(exampleEori)
        eoriRow must haveSummaryActionsTexts("site.change", "declaration.summary.parties.carrier.eori.change")
        eoriRow must haveSummaryActionWithPlaceholder(CarrierEoriNumberController.displayPage)

        val addressRow = view.getElementsByClass("carrier-address-row")

        addressRow must haveSummaryKey(messages("declaration.summary.parties.carrier.address"))
        addressRow must haveSummaryValue(exampleAddressContents)
        addressRow must haveSummaryActionsTexts("site.change", "declaration.summary.parties.carrier.address.change")
        addressRow must haveSummaryActionWithPlaceholder(CarrierEoriNumberController.displayPage)
      }

      "does not contain exporter when section not answered" in {
        val view = partiesSection(aDeclarationAfter(data, withoutExporterDetails()))(messages)

        view.getElementsByClass("exporter-eori-row") mustBe empty
        view.getElementsByClass("exporter-address-row") mustBe empty
      }

      "does not contain consignee when section not answered" in {
        val view = partiesSection(aDeclarationAfter(data, withoutConsigneeDetails()))(messages)

        view.getElementsByClass("consignee-eori-row") mustBe empty
        view.getElementsByClass("consignee-address-row") mustBe empty
      }

      "does not contain declarant when section not answered" in {
        val view = partiesSection(aDeclarationAfter(data, withoutDeclarantDetails()))(messages)

        view.getElementsByClass("declarant-eori-row") mustBe empty
        view.getElementsByClass("declarant-address-row") mustBe empty
      }

      "does not contain representative when section not answered" in {
        val view = partiesSection(aDeclarationAfter(data, withoutRepresentativeDetails()))(messages)

        view.getElementsByClass("representative-eori-row") mustBe empty
        view.getElementsByClass("representative-address-row") mustBe empty
      }

      "does not contain carrier details when section not answered" in {
        val view = partiesSection(aDeclarationAfter(data, withoutCarrierDetails()))(messages)

        view.getElementsByClass("carrier-eori-row") mustBe empty
        view.getElementsByClass("carrier-address-row") mustBe empty
      }

      "does not contain anything when there are no parties" in {
        val view = partiesSection(aDeclaration())(messages)

        view.getAllElements.text() must be(empty)
      }

      "contains an empty authorisation holders section" when {
        "no authorisation holders have been entered" in {
          val row = view.getElementsByClass("authorisation-holders-summary")

          row must haveSummaryKey(messages("declaration.summary.parties.holders"))
          row must haveSummaryValue(messages("site.none"))
          row must haveSummaryActionsTexts("site.change", "declaration.summary.parties.holders.empty.change")
          row must haveSummaryActionWithPlaceholder(AuthorisationProcedureCodeChoiceController.displayPage)
        }
      }

      "contains an authorisation holders section" in {
        val type1 = "OPO"
        val type2 = "AEOC"
        val eori1 = "GB123456789012"
        val eori2 = "GB123456789012"

        val holder1 = DeclarationHolder(Some(type1), Some(Eori(eori1)), None)
        val holder2WithoutCode = DeclarationHolder(None, Some(Eori(eori2)), None)
        val holder3WithoutEori = DeclarationHolder(Some(type2), None, None)
        val declaration = aDeclarationAfter(data, withDeclarationHolders(holder1, holder2WithoutCode, holder3WithoutEori))

        val view = partiesSection(declaration)(messages)

        val summaryList = view.getElementsByClass("authorisation-holders-summary").get(0)
        val summaryListRows = summaryList.getElementsByClass("govuk-summary-list__row")
        summaryListRows.size mustBe 5

        val heading = summaryListRows.get(0).getElementsByClass("authorisation-holder-heading")
        heading must haveSummaryKey(messages("declaration.summary.parties.holders"))
        heading must haveSummaryValue("")

        val holder1Type = summaryListRows.get(1).getElementsByClass("authorisation-holder-type-1")
        holder1Type must haveSummaryKey(messages("declaration.summary.parties.holders.type"))
        holder1Type must haveSummaryValue("OPO - Outward Processing authorisation")
        holder1Type must haveSummaryActionsTexts("site.change", "declaration.summary.parties.holders.change", type1, eori1)
        holder1Type must haveSummaryActionWithPlaceholder(AuthorisationProcedureCodeChoiceController.displayPage)

        val holder1Eori = summaryListRows.get(2).getElementsByClass("authorisation-holder-eori-1")
        holder1Eori must haveSummaryKey(messages("declaration.summary.parties.holders.eori"))
        holder1Eori must haveSummaryValue(eori1)
        holder1Eori.get(0).getElementsByClass("govuk-summary-list__actions").size mustBe 0

        val holder2Eori = summaryListRows.get(3).getElementsByClass("authorisation-holder-eori-2")
        holder2Eori must haveSummaryKey(messages("declaration.summary.parties.holders.eori"))
        holder2Eori must haveSummaryValue(eori2)
        holder2Eori must haveSummaryActionsTexts("site.change", "declaration.summary.parties.holders.change", "", eori2)
        holder2Eori must haveSummaryActionWithPlaceholder(AuthorisationProcedureCodeChoiceController.displayPage)

        val holder3Type = summaryListRows.get(4).getElementsByClass("authorisation-holder-type-3")
        holder3Type must haveSummaryKey(messages("declaration.summary.parties.holders.type"))
        holder3Type must haveSummaryValue("AEOC - Authorised Economic Operator - Customs simplifications")
        holder3Type must haveSummaryActionsTexts("site.change", "declaration.summary.parties.holders.change", type2, "")
        holder3Type must haveSummaryActionWithPlaceholder(AuthorisationProcedureCodeChoiceController.displayPage)
      }
    }

    onClearance { implicit request =>
      val view = partiesSection(data)(messages)
      "contains 'Is Exs' section with change button" in {

        val isExsRow = view.getElementsByClass("isExs-row")

        isExsRow must haveSummaryKey(messages("declaration.summary.parties.exs"))
        isExsRow must haveSummaryValue(messages("site.no"))
        isExsRow must haveSummaryActionsTexts("site.change", "declaration.summary.parties.exs.change")
        isExsRow must haveSummaryActionWithPlaceholder(IsExsController.displayPage)
      }

      "contains 'Is this EIDR' section with change button" in {
        val isEidrRow = view.getElementsByClass("is-entry-into-declarants-records-row")

        isEidrRow must haveSummaryKey(messages("declaration.summary.parties.eidr"))
        isEidrRow must haveSummaryValue(messages("site.yes"))
        isEidrRow must haveSummaryActionsTexts("site.change", "declaration.summary.parties.eidr.change")
        isEidrRow must haveSummaryActionWithPlaceholder(EntryIntoDeclarantsRecordsController.displayPage)
      }

      "contains 'Person Presenting the Goods' section with change button" in {
        val personPresentingGoodsRow = view.getElementsByClass("person-presenting-goods-row")

        personPresentingGoodsRow must haveSummaryKey(messages("declaration.summary.parties.personPresentingGoods"))
        personPresentingGoodsRow must haveSummaryValue(exampleEori)
        personPresentingGoodsRow must haveSummaryActionsTexts("site.change", "declaration.summary.parties.personPresentingGoods.change")
        personPresentingGoodsRow must haveSummaryActionWithPlaceholder(PersonPresentingGoodsDetailsController.displayPage)
      }
    }

    onEveryDeclarationJourney() { implicit request =>
      "NOT have change links" when {
        "'actionsEnabled' is false" in {
          val view = partiesSection(data, false)(messages)
          view.getElementsByClass("govuk-summary-list__actions") mustBe empty
        }
      }
    }
  }
}
