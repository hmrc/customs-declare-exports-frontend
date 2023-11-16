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

import base.ExportsTestData.mucr
import base.Injector
import controllers.declaration.routes._
import forms.common.YesNoAnswer.YesNoAnswers.yes
import forms.declaration.ConsignmentReferences
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.STANDARD_PRE_LODGED
import forms.{Ducr, Lrn, Mrn}
import models.DeclarationType._
import models.declaration.DeclarationStatus._
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.helpers.ViewDates.formatDateAtTime

import scala.concurrent.duration.Duration

class Card1ForReferencesSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  private val ducr = "DUCR"
  private val lrn = "LRN"
  private val mrn = "MRN"
  private val eidrDate = "Some datestamp"

  private val declaration = aDeclaration(
    withAdditionalDeclarationType(STANDARD_PRE_LODGED),
    withConsignmentReferences(ConsignmentReferences(Some(Ducr(ducr)), Some(Lrn(lrn)), Some(Mrn(mrn)), Some(eidrDate))),
    withLinkDucrToMucr(),
    withMucr()
  )

  private val meta = declaration.declarationMeta

  private val card1ForReferences = instanceOf[Card1ForReferences]

  "'Declaration References' section" should {
    val view = card1ForReferences.eval(declaration)(messages)

    "have, inside an Inset Text, a link to /type" when {
      "a declaration has DRAFT status and 'parentDeclarationId' is defined" in {
        val declaration1 = declaration.copy(declarationMeta = declaration.declarationMeta.copy(status = DRAFT, parentDeclarationId = Some("some id")))
        val insetText = card1ForReferences.eval(declaration1)(messages).getElementsByClass("govuk-inset-text")
        insetText.size mustBe 1

        val link = insetText.first.getElementsByClass("govuk-link").first
        link.text mustBe messages("declaration.summary.references.insets")
        link must haveHref(AdditionalDeclarationTypeController.displayPage)
      }
    }

    "not have any Inset Text" when {
      "a draft declaration has 'parentDeclarationId' undefined" in {
        val declaration1 = declaration.copy(declarationMeta = declaration.declarationMeta.copy(status = DRAFT))
        card1ForReferences.eval(declaration1)(messages).getElementsByClass("govuk-inset-text").size mustBe 0
      }
    }

    "have the expected headings" in {
      val headings = view.getElementsByTag("h2")
      headings.first.text mustBe messages("declaration.summary.heading")
      headings.last.text mustBe messages("declaration.summary.section.1")
    }

    "have a 'change links' paragraph" when {
      List(INITIAL, DRAFT, AMENDMENT_DRAFT).foreach { status =>
        s"declaration's status is $status" in {
          val declaration1 = declaration.copy(declarationMeta = declaration.declarationMeta.copy(status = status))
          val view = card1ForReferences.eval(declaration1)(messages)
          val paragraph = view.getElementsByTag("p").first
          paragraph.text mustBe messages("declaration.summary.amend.body")
        }
      }
    }

    "NOT have a 'change links' paragraph" when {
      "declaration's status is COMPLETE" in {
        val declaration1 = declaration.copy(declarationMeta = declaration.declarationMeta.copy(status = COMPLETE))
        card1ForReferences.eval(declaration1)(messages).getElementsByTag("p").size mustBe 0
      }
    }

    "show the declaration's creation date" in {
      val row = view.getElementsByClass("creation-date")
      checkSummaryRow(row, "references.creation.date", formatDateAtTime(meta.createdDateTime), None, "ign")
    }

    "show the declaration's expiration date" in {
      val row = view.getElementsByClass("expiration-date")

      val expirationDate = formatDateAtTime(meta.updatedDateTime.plusSeconds(Duration("30 days").toSeconds))
      checkSummaryRow(row, "references.expiration.date", expirationDate, None, "ign")
    }

    "show the declaration's type" in {
      val row = view.getElementsByClass("declaration-type")

      val expectedValue = messages(s"declaration.type.${declaration.`type`.toString.toLowerCase}")
      checkSummaryRow(row, "references.type", expectedValue, None, "ign")
    }

    "show the additional declaration type" in {
      val row = view.getElementsByClass("additional-declaration-type")

      val adt = declaration.additionalDeclarationType.value
      val expectedValue = messages(s"declaration.summary.references.additionalType.${adt.toString}")
      checkSummaryRow(row, "references.additionalType", expectedValue, None, "ign")
    }

    "show the 'consignment references' rows of the declaration" in {
      val call = Some(ConsignmentReferencesController.displayPage)
      val callForDucr = Some(DucrEntryController.displayPage)
      val callForLrn = Some(LocalReferenceNumberController.displayPage)

      checkSummaryRow(view.getElementsByClass("ducr"), "references.ducr", ducr, callForDucr, "references.ducr")
      checkSummaryRow(view.getElementsByClass("mrn"), "references.mrn", mrn, call, "references.mrn")
      checkSummaryRow(view.getElementsByClass("eidr-date"), "references.eidr", eidrDate, call, "references.eidr")
      checkSummaryRow(view.getElementsByClass("lrn"), "references.lrn", lrn, callForLrn, "references.lrn")
    }

    "have the DUCR and LRN rows' 'change' link pointing to /consignment-references" when {
      "the declaration's type is SUPPLEMENTARY" in {
        val declaration1 = declaration.copy(`type` = SUPPLEMENTARY)
        val view = card1ForReferences.eval(declaration1)(messages)

        val call = Some(ConsignmentReferencesController.displayPage)
        checkSummaryRow(view.getElementsByClass("ducr"), "references.ducr", ducr, call, "references.ducr")
        checkSummaryRow(view.getElementsByClass("lrn"), "references.supplementary.lrn", lrn, call, "references.lrn")
      }
    }

    "show the 'link DUCR to MUCR' answer" in {
      val row = view.getElementsByClass("link-ducr-to-mucr")

      val call = Some(LinkDucrToMucrController.displayPage)
      checkSummaryRow(row, "references.linkDucrToMucr", yes, call, "references.linkDucrToMucr")
    }

    "show the declaration's MUCR" in {
      val row = view.getElementsByClass("mucr")

      val call = Some(MucrController.displayPage)
      checkSummaryRow(row, "references.mucr", mucr, call, "references.mucr")
    }

    "NOT have change links" when {

      "'actionsEnabled' is false" in {
        val view = card1ForReferences.eval(declaration, false)(messages)
        view.getElementsByClass(summaryActionsClassName) mustBe empty
      }

      "the declaration's status is AMENDMENT_DRAFT" in {
        val declaration1 = declaration.copy(declarationMeta = declaration.declarationMeta.copy(status = AMENDMENT_DRAFT))
        val view = card1ForReferences.eval(declaration1)(messages)

        checkSummaryRow(view.getElementsByClass("ducr"), "references.ducr", ducr, None, "ign")
        checkSummaryRow(view.getElementsByClass("mrn"), "references.mrn", mrn, None, "ign")
        checkSummaryRow(view.getElementsByClass("eidr-date"), "references.eidr", eidrDate, None, "ign")
        checkSummaryRow(view.getElementsByClass("lrn"), "references.lrn", lrn, None, "ign")
        checkSummaryRow(view.getElementsByClass("link-ducr-to-mucr"), "references.linkDucrToMucr", yes, None, "ign")
        checkSummaryRow(view.getElementsByClass("mucr"), "references.mucr", mucr, None, "ign")
      }
    }
  }

  "Card1ForReferences.content" should {
    "return the expected CYA card" in {
      val cardContent = card1ForReferences.content(declaration)
      cardContent.getElementsByClass("references-card").text mustBe messages("declaration.summary.section.1")
    }
  }

  "Card1ForReferences.backLink" when {

    "on SUPPLEMENTARY journeys" should {
      "go to ConsignmentReferencesController" in {
        val request = journeyRequest(aDeclaration(withType(SUPPLEMENTARY)))
        card1ForReferences.backLink(request) mustBe ConsignmentReferencesController.displayPage
      }
    }

    List(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE).foreach { declarationType =>
      s"on $declarationType journeys and" should {

        "go to LinkDucrToMucrController" when {
          "mucr isEmpty" in {
            val request = journeyRequest(aDeclaration(withType(declarationType)))
            card1ForReferences.backLink(request) mustBe LinkDucrToMucrController.displayPage
          }
        }

        "go to MucrController" when {
          "mucr has been answered" in {
            val request = journeyRequest(aDeclaration(withType(declarationType), withMucr()))
            card1ForReferences.backLink(request) mustBe MucrController.displayPage
          }
        }
      }
    }
  }

  "Card1ForReferences.continueTo" when {

    "on CLEARANCE journeys" should {
      "go to EntryIntoDeclarantsRecordsController" in {
        val request = journeyRequest(aDeclaration(withType(CLEARANCE)))
        card1ForReferences.continueTo(request) mustBe EntryIntoDeclarantsRecordsController.displayPage
      }
    }

    List(STANDARD, SIMPLIFIED, OCCASIONAL, SUPPLEMENTARY).foreach { declarationType =>
      s"on $declarationType journeys and" should {
        "go to DeclarantExporterController" in {
          val request = journeyRequest(aDeclaration(withType(declarationType)))
          card1ForReferences.continueTo(request) mustBe DeclarantExporterController.displayPage
        }
      }
    }
  }
}
