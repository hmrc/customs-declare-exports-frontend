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

package views.helpers.summary

import base.Injector
import controllers.declaration.routes._
import forms.common.{Address, Eori}
import forms.declaration.AuthorisationProcedureCodeChoice.{Choice1040, ChoiceOthers}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.{STANDARD_FRONTIER, STANDARD_PRE_LODGED}
import forms.declaration.{AdditionalActor, IsExs}
import forms.declaration.authorisationHolder.AuthorisationHolder
import models.declaration.RepresentativeDetails
import models.DeclarationType._
import org.scalatest.Assertion
import play.api.mvc.Call
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec

class Card2ForPartiesSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  private val eori = "GB123456"
  private val address = Address("fullName", "addressLine", "townOrCity", "postCode", "GB")
  private val expectedAddress = "fullName addressLine townOrCity postCode GB"

  private val declaration = aDeclaration(
    withDeclarantIsExporter("No"),
    withEntryIntoDeclarantsRecords("Yes"),
    withPersonPresentingGoods(Some(Eori(eori))),
    withExporterDetails(Some(Eori(eori)), Some(address)),
    withIsExs(IsExs("No")),
    withRepresentativeDetails(Some(Eori(eori)), Some("1"), Some("Yes")),
    withCarrierDetails(Some(Eori(eori)), Some(address)),
    withConsigneeDetails(Some(Eori(eori)), Some(address)),
    withConsignorDetails(Some(Eori(eori)), Some(address)),
    withAdditionalActors(),
    withAuthorisationHolders()
  )

  private val card2ForParties = instanceOf[Card2ForParties]

  "Parties section" should {
    val view = card2ForParties.eval(declaration)(messages)

    "have the expected heading" in {
      view.getElementsByTag("h2").first.text mustBe messages(s"declaration.summary.section.2")
    }

    "show 'are you the exporter'" in {
      val row = view.getElementsByClass("declarant-is-exporter")
      val call = Some(DeclarantExporterController.displayPage)
      checkSummaryRow(row, "parties.declarantIsExporter", messages("site.no"), call, "parties.declarantIsExporter")
    }

    "show the 'Is this EIDR' section" in {
      val row = view.getElementsByClass("is-entry-into-declarants-records")
      val call = Some(EntryIntoDeclarantsRecordsController.displayPage)
      checkSummaryRow(row, "parties.eidr", messages("site.yes"), call, "parties.eidr")
    }

    "show the 'Person Presenting the Goods' section" in {
      val row = view.getElementsByClass("person-presenting-goods")
      val call = Some(PersonPresentingGoodsDetailsController.displayPage)
      checkSummaryRow(row, "parties.personPresentingGoods", eori, call, "parties.personPresentingGoods")
    }

    "show the exporter details" in {
      checkDetails("exporter", ExporterEoriNumberController.displayPage)
    }

    "show the 'Is Exs' section" in {
      val row = view.getElementsByClass("isExs")
      val call = Some(IsExsController.displayPage)
      checkSummaryRow(row, "parties.exs", messages("site.no"), call, "parties.exs")
    }

    "show the representative agent" in {
      val row = view.getElementsByClass("representative-other-agent")
      val call = Some(RepresentativeAgentController.displayPage)
      checkSummaryRow(row, "parties.representative.agent", messages("site.yes"), call, "parties.representative.agent")
    }

    "show the representative eori" in {
      val row = view.getElementsByClass("representative-eori")
      val call = Some(RepresentativeEntityController.displayPage)
      checkSummaryRow(row, "parties.representative.eori", eori, call, "parties.representative.eori")
    }

    "show the representative status code" in {
      val row = view.getElementsByClass("representative-type")
      val call = Some(RepresentativeStatusController.displayPage)
      val value = messages("declaration.summary.parties.representative.type.1")
      checkSummaryRow(row, "parties.representative.type", value, call, "parties.representative.type")
    }

    "show the carrier details" in {
      checkDetails("carrier", CarrierEoriNumberController.displayPage)
    }

    "show the consignee details" in {
      checkDetails("consignee", ConsigneeDetailsController.displayPage)
    }

    "show the consignor details" in {
      checkDetails("consignor", ConsignorEoriNumberController.displayPage)
    }

    "have an empty additional actors section" when {
      "no additional actors have been entered" in {
        val row = view.getElementsByClass("additional-actors-heading")
        val call = Some(AdditionalActorsAddController.displayPage)
        checkSummaryRow(row, "parties.actors", messages("site.none"), call, "parties.actors")
      }
    }

    "show the an additional actors section" in {
      val type1 = "CS"
      val type2 = "MF"
      val expectedType1 = "Consolidator"
      val expectedType2 = "Manufacturer"

      val eori1 = "GB123456789012"
      val eori2 = "GB123456789012"

      val actor1 = AdditionalActor(Some(Eori(eori1)), Some(type1))
      val actor2WithoutType = AdditionalActor(Some(Eori(eori2)), None)
      val actor3WithoutEori = AdditionalActor(None, Some(type2))
      val declaration1 = aDeclarationAfter(declaration, withAdditionalActors(actor1, actor2WithoutType, actor3WithoutEori))

      val view = card2ForParties.eval(declaration1)(messages)

      val call = Some(AdditionalActorsSummaryController.displayPage)

      val heading = view.getElementsByClass("additional-actors-heading")
      checkSummaryRow(heading, "parties.actors", "", None, "")

      val actor1Type = view.getElementsByClass("additional-actor-1-type")
      checkSummaryRow(actor1Type, "parties.actors.type", expectedType1, call, "parties.actors")

      val actor1Eori = view.getElementsByClass("additional-actor-1-eori")
      checkSummaryRow(actor1Eori, "parties.actors.eori", eori1, None, "ign")

      view.getElementsByClass("additional-actor-2-type") mustBe empty

      val actor2Eori = view.getElementsByClass("additional-actor-2-eori")
      checkSummaryRow(actor2Eori, "parties.actors.eori", eori2, call, "parties.actors")

      val actor3Type = view.getElementsByClass("additional-actor-3-type")
      checkSummaryRow(actor3Type, "parties.actors.type", expectedType2, call, "parties.actors")

      view.getElementsByClass("additional-actor-3-eori") mustBe empty
    }

    "have an empty authorisation holders section" when {
      "no authorisation holders have been entered" in {
        val row = view.getElementsByClass("authorisation-holders-heading")
        val call = Some(AuthorisationProcedureCodeChoiceController.displayPage)
        checkSummaryRow(row, "parties.holders", messages("site.none"), call, "parties.holders")
      }
    }

    "show the an authorisation holders section" in {
      val type1 = "OPO"
      val type2 = "AEOC"
      val expectedType1 = "OPO - Outward Processing authorisation"
      val expectedType2 = "AEOC - Authorised Economic Operator - Customs simplifications"

      val eori1 = "GB123456789012"
      val eori2 = "GB123456789012"

      val holder1 = AuthorisationHolder(Some(type1), Some(Eori(eori1)), None)
      val holder2WithoutCode = AuthorisationHolder(None, Some(Eori(eori2)), None)
      val holder3WithoutEori = AuthorisationHolder(Some(type2), None, None)
      val declaration1 = aDeclarationAfter(declaration, withAuthorisationHolders(holder1, holder2WithoutCode, holder3WithoutEori))

      val view = card2ForParties.eval(declaration1)(messages)

      val call = Some(AuthorisationProcedureCodeChoiceController.displayPage)

      val heading = view.getElementsByClass("authorisation-holders-heading")
      checkSummaryRow(heading, "parties.holders", "", None, "")

      val holder1Type = view.getElementsByClass("authorisation-holder-1-type")
      checkSummaryRow(holder1Type, "parties.holders.type", expectedType1, call, "parties.holders")

      val holder1Eori = view.getElementsByClass("authorisation-holder-1-eori")
      checkSummaryRow(holder1Eori, "parties.holders.eori", eori1, None, "ign")

      view.getElementsByClass("authorisation-holder-2-type") mustBe empty

      val holder2Eori = view.getElementsByClass("authorisation-holder-2-eori")
      checkSummaryRow(holder2Eori, "parties.holders.eori", eori2, call, "parties.holders")

      val holder3Type = view.getElementsByClass("authorisation-holder-3-type")
      checkSummaryRow(holder3Type, "parties.holders.type", expectedType2, call, "parties.holders")

      view.getElementsByClass("authorisation-holder-3-eori") mustBe empty
    }

    "not show the declarant when the section is undefined" in {
      val view = card2ForParties.eval(aDeclarationAfter(declaration, withoutDeclarantDetails))(messages)
      view.getElementsByClass("declarant-address") mustBe empty
    }

    "not show is-declarant-exporter when the section is undefined" in {
      val view = card2ForParties.eval(aDeclarationAfter(declaration, withoutDeclarantIsExporter))(messages)
      view.getElementsByClass("declarant-is-exporter") mustBe empty
    }

    "not show is-eidr when the section is undefined" in {
      val view = card2ForParties.eval(aDeclarationAfter(declaration, withoutEntryIntoDeclarantsRecords))(messages)
      view.getElementsByClass("is-entry-into-declarants-records") mustBe empty
    }

    "not show the person presenting goods when the section is undefined" in {
      val view = card2ForParties.eval(aDeclarationAfter(declaration, withoutPersonPresentingGoods))(messages)
      view.getElementsByClass("person-presenting-goods") mustBe empty
    }

    "not show the exporter when the section is undefined" in {
      val view = card2ForParties.eval(aDeclarationAfter(declaration, withoutExporterDetails))(messages)
      view.getElementsByClass("exporter-eori") mustBe empty
      view.getElementsByClass("exporter-address") mustBe empty
    }

    "not show is-exs when the section is undefined" in {
      val view = card2ForParties.eval(aDeclarationAfter(declaration, withoutIsExs))(messages)
      view.getElementsByClass("isExs") mustBe empty
      view.getElementsByClass("isExs") mustBe empty
    }

    "not show the representative eori when not representing another agent" in {
      val details = Some(RepresentativeDetails(None, Some("2"), Some("No")))
      val parties = declaration.parties.copy(representativeDetails = details)
      val view = card2ForParties.eval(declaration.copy(parties = parties), true)(messages)

      view.getElementsByClass("representative-eori").text() mustBe empty
    }

    "not show the representative when the section is undefined" in {
      val view = card2ForParties.eval(aDeclarationAfter(declaration, withoutRepresentativeDetails))(messages)
      view.getElementsByClass("representative-other-agent") mustBe empty
      view.getElementsByClass("representative-eori") mustBe empty
      view.getElementsByClass("representative-address") mustBe empty
      view.getElementsByClass("representative-type") mustBe empty
    }

    "not show the carrier details when the section is undefined" in {
      val view = card2ForParties.eval(aDeclarationAfter(declaration, withoutCarrierDetails))(messages)
      view.getElementsByClass("carrier-eori") mustBe empty
      view.getElementsByClass("carrier-address") mustBe empty
    }

    "not show the consignee when the section is undefined" in {
      val view = card2ForParties.eval(aDeclarationAfter(declaration, withoutConsigneeDetails))(messages)
      view.getElementsByClass("consignee-eori") mustBe empty
      view.getElementsByClass("consignee-address") mustBe empty
    }

    "not show the consignor when the section is undefined" in {
      val view = card2ForParties.eval(aDeclarationAfter(declaration, withoutConsignorDetails))(messages)
      view.getElementsByClass("consignor-eori") mustBe empty
      view.getElementsByClass("consignor-address") mustBe empty
    }

    "not show the additional actors when the section is undefined" in {
      val view = card2ForParties.eval(aDeclarationAfter(declaration, withoutAdditionalActors))(messages)
      view.getElementsByClass("additional-actors-heading") mustBe empty
    }

    "not show the authorisation holders when the section is undefined" in {
      val view = card2ForParties.eval(aDeclarationAfter(declaration, withoutAuthorisationHolders))(messages)
      view.getElementsByClass("authorisation-holders-heading") mustBe empty
    }

    "not show anything when parties only has undefined attributes" in {
      val view = card2ForParties.eval(aDeclaration())(messages)
      view.getAllElements.text() mustBe empty
    }

    "NOT have change links" when {
      "'actionsEnabled' is false" in {
        val view = card2ForParties.eval(declaration, false)(messages)
        view.getElementsByClass(summaryActionsClassName) mustBe empty
      }
    }

    def checkDetails(key: String, call: Call): Assertion = {
      val row1 = view.getElementsByClass(s"$key-eori")
      checkSummaryRow(row1, s"parties.$key.eori", eori, Some(call), s"parties.$key.eori")

      val row2 = view.getElementsByClass(s"$key-address")
      checkSummaryRow(row2, s"parties.$key.address", expectedAddress, Some(call), s"parties.$key.address")
    }
  }

  "Card2ForParties.content" should {
    "return the expected CYA card" in {
      val cardContent = card2ForParties.content(declaration)
      cardContent.getElementsByClass("parties-card").text mustBe messages("declaration.summary.section.2")
    }
  }

  "Card2ForParties.backLink" when {

    "the declaration has Authorisation Holders" should {

      "go to AuthorisationHolderSummaryController" in {
        val request = journeyRequest(aDeclaration(withAuthorisationHolders(AuthorisationHolder(None, None, None))))
        card2ForParties.backLink(request) mustBe AuthorisationHolderSummaryController.displayPage
      }
    }

    "the declaration has no Authorisation Holders" should {

      "go to AuthorisationProcedureCodeChoiceController" when {

        List(STANDARD, SIMPLIFIED, SUPPLEMENTARY).foreach { declarationType =>
          s"on $declarationType journeys" in {
            val request = journeyRequest(aDeclaration(withType(declarationType)))
            card2ForParties.backLink(request) mustBe AuthorisationProcedureCodeChoiceController.displayPage
          }
        }
      }

      "go to AuthorisationHolderSummaryController" when {

        List(OCCASIONAL, CLEARANCE).foreach { declarationType =>
          s"on $declarationType journeys" in {
            val request = journeyRequest(aDeclaration(withType(declarationType)))
            card2ForParties.backLink(request) mustBe AuthorisationHolderRequiredController.displayPage
          }
        }

        List(STANDARD_PRE_LODGED, STANDARD_FRONTIER).foreach { adt =>
          s"on $adt journeys and" when {
            "AuthorisationProcedureCode is 1040" in {
              val withAPC = withAuthorisationProcedureCodeChoice(Choice1040)
              val request = journeyRequest(aDeclaration(withType(STANDARD), withAdditionalDeclarationType(adt), withAPC))
              card2ForParties.backLink(request) mustBe AuthorisationHolderRequiredController.displayPage
            }
          }
        }

        "on STANDARD_PRE_LODGED journeys and" when {
          "AuthorisationProcedureCode is 'Others'" in {
            val withADT = withAdditionalDeclarationType(STANDARD_PRE_LODGED)
            val withAPC = withAuthorisationProcedureCodeChoice(ChoiceOthers)
            val request = journeyRequest(aDeclaration(withType(STANDARD), withADT, withAPC))
            card2ForParties.backLink(request) mustBe AuthorisationHolderRequiredController.displayPage
          }
        }
      }
    }
  }

  "Card2ForParties.continueTo" should {
    "go to DestinationCountryController" in {
      card2ForParties.continueTo(journeyRequest()) mustBe DestinationCountryController.displayPage
    }
  }
}
