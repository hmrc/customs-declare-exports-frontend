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
import controllers.section1.routes._
import controllers.section2.routes.{DeclarantExporterController, EntryIntoDeclarantsRecordsController}
import forms.common.Eori
import forms.common.YesNoAnswer.YesNoAnswers.yes
import forms.declaration.{Ducr, Lrn, Mrn}
import forms.section1.additionaldeclarationtype.AdditionalDeclarationType.STANDARD_PRE_LODGED
import forms.section1.ConsignmentReferences
import models.DeclarationType._
import models.ExportsDeclaration
import models.declaration.DeclarationStatus._
import models.declaration.submissions.RequestType.SubmissionRequest
import models.declaration.submissions.Submission
import org.jsoup.select.Elements
import org.scalatest.OptionValues
import play.api.libs.json.Json
import play.twirl.api.Html
import services.cache.ExportsTestHelper
import views.helpers.EnhancedStatusHelper.asText
import views.helpers.ViewDates
import views.helpers.ViewDates.formatDateAtTime
import views.helpers.summary.Card1ForReferencesSpec.{notifications, submission}
import views.common.UnitViewSpec

import scala.concurrent.duration.Duration

class Card1ForReferencesSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  private val ducr = "DUCR"
  private val lrn = "LRN"
  private val mrn = "MRN"
  private val eidrDate = "Some datestamp"
  private val eori = "GB123456"

  private val declaration = aDeclaration(
    withAdditionalDeclarationType(STANDARD_PRE_LODGED),
    withDeclarantDetails(Some(Eori(eori))),
    withConsignmentReferences(ConsignmentReferences(Some(Ducr(ducr)), Some(Lrn(lrn)), Some(Mrn(mrn)), Some(eidrDate))),
    withLinkDucrToMucr(),
    withMucr()
  )

  private val meta = declaration.declarationMeta

  private val card1ForReferences = instanceOf[Card1ForReferences]

  private def finalCyaView(decl: ExportsDeclaration, actionEnabled: Boolean = true, hasErrors: Boolean = false): Html =
    card1ForReferences.eval(decl, actionEnabled, hasErrors)(messages)

  private def miniCyaView(decl: ExportsDeclaration = declaration, actionEnabled: Boolean = true): Html =
    card1ForReferences.content(decl, actionEnabled)(messages)

  "Card1ForReferences.content" should {
    val view = miniCyaView()

    "return the expected CYA card" in {
      val card = miniCyaView().getElementsByTag("h2").first
      card.text mustBe messages("declaration.summary.section.1")
      assert(card.hasClass("references-card"))
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

    "not have a 'MRN' row (when the maybeSubmission parameter is None)" in {
      view.getElementsByClass("submission-mrn").size mustBe 0
    }

    "not have 'Notification status' rows (when the maybeSubmission parameter is None)" in {
      view.getElementsByClass("notification-status").size mustBe 0
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

    "show the declarant eori" in {
      val row = view.getElementsByClass("declarant-eori")
      checkSummaryRow(row, "parties.declarant.eori", eori, None, "ign")
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
        val view = miniCyaView(declaration.copy(`type` = SUPPLEMENTARY))

        val call = Some(ConsignmentReferencesController.displayPage)
        checkSummaryRow(view.getElementsByClass("ducr"), "references.ducr", ducr, call, "references.ducr")
        checkSummaryRow(view.getElementsByClass("lrn"), "references.lrn", lrn, call, "references.lrn")
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
        miniCyaView(actionEnabled = false).getElementsByClass(summaryActionsClassName) mustBe empty
      }

      "the declaration's status is AMENDMENT_DRAFT" in {
        val declaration1 = declaration.copy(declarationMeta = declaration.declarationMeta.copy(status = AMENDMENT_DRAFT))
        val view = miniCyaView(declaration1)

        checkSummaryRow(view.getElementsByClass("ducr"), "references.ducr", ducr, None, "ign")
        checkSummaryRow(view.getElementsByClass("mrn"), "references.mrn", mrn, None, "ign")
        checkSummaryRow(view.getElementsByClass("eidr-date"), "references.eidr", eidrDate, None, "ign")
        checkSummaryRow(view.getElementsByClass("lrn"), "references.lrn", lrn, None, "ign")
        checkSummaryRow(view.getElementsByClass("link-ducr-to-mucr"), "references.linkDucrToMucr", yes, None, "ign")
        checkSummaryRow(view.getElementsByClass("mucr"), "references.mucr", mucr, None, "ign")
      }
    }
  }

  "Card1ForReferences.eval" should {

    "have, inside an Inset Text, a link to /type" when {
      "a declaration has DRAFT status and 'parentDeclarationId' is defined" in {
        val declaration1 = declaration.copy(declarationMeta = declaration.declarationMeta.copy(status = DRAFT, parentDeclarationId = Some("some id")))
        val insetText = finalCyaView(declaration1).getElementsByClass("govuk-inset-text")
        insetText.size mustBe 1

        val link = insetText.first.getElementsByClass("govuk-link").first
        link.text mustBe messages("declaration.summary.references.insets")
        link must haveHref(AdditionalDeclarationTypeController.displayPage)
      }
    }

    "not have any Inset Text" when {
      "a draft declaration has 'parentDeclarationId' undefined" in {
        val declaration1 = declaration.copy(declarationMeta = declaration.declarationMeta.copy(status = DRAFT))
        finalCyaView(declaration1).getElementsByClass("govuk-inset-text").size mustBe 0
      }
    }

    "have the expected headings" in {
      val headings = finalCyaView(declaration).getElementsByTag("h2")
      headings.first.text mustBe messages("declaration.summary.heading")
      headings.last.text mustBe messages("declaration.summary.section.1")
    }

    "have a 'change links' paragraph" when {
      List(INITIAL, DRAFT, AMENDMENT_DRAFT).foreach { status =>
        s"declaration's status is $status" in {
          val declaration1 = declaration.copy(declarationMeta = declaration.declarationMeta.copy(status = status))
          val paragraph = finalCyaView(declaration1).getElementsByTag("p").first
          paragraph.text mustBe messages("declaration.summary.amend.body")
        }
      }
    }

    "NOT have a 'change links' paragraph" when {
      "declaration's status is COMPLETE" in {
        val declaration1 = declaration.copy(declarationMeta = declaration.declarationMeta.copy(status = COMPLETE))
        finalCyaView(declaration1).getElementsByTag("p").size mustBe 0
      }
    }

    "return a marked (with a red left border) card" when {
      "the declaration has errors" in {
        val summaryCard = finalCyaView(declaration, hasErrors = true).getElementsByClass("govuk-summary-card").get(0)
        assert(summaryCard.hasClass("govuk-summary-card--error"))
      }
    }

    "show a 'MRN' row (when the maybeSubmission parameter is NOT None)" in {
      val view = card1ForReferences.eval(declaration, maybeSubmission = Some(submission))
      val row = view.getElementsByClass("submission-mrn")
      checkSummaryRow(row, "references.submission.mrn", submission.mrn.value, None, "")
    }

    "show one or more 'Notification status' rows (when the maybeSubmission parameter is NOT None)" in {
      val view = card1ForReferences.eval(declaration, maybeSubmission = Some(submission))
      val rows: Elements = view.getElementsByClass("notification-status")
      for (ix <- 0 until notifications.size) {
        val row = new Elements(rows.get(ix))
        row must haveSummaryKey(asText(notifications(ix).enhancedStatus))
        row must haveSummaryValue(ViewDates.formatDateAtTime(notifications(ix).dateTimeIssued))
        row.first.getElementsByClass(summaryActionsClassName).size mustBe 0
      }
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

object Card1ForReferencesSpec extends OptionValues {

  val submission = Json
    .parse(s"""{
      |    "uuid" : "TEST-N3fwz-PAwaKfh4",
      |    "eori" : "IT165709468566000",
      |    "lrn" : "MBxIq",
      |    "ducr" : "7SI755462446188-51Z8126",
      |    "actions" : [
      |        {
      |            "id" : "abdf6423-b7fd-4f40-b325-c34bdcdfb203",
      |            "requestType" : "CancellationRequest",
      |            "requestTimestamp" : "2022-07-06T08:05:20.477Z[UTC]",
      |            "versionNo" : 1,
      |            "decId" : "id"
      |        },
      |        {
      |            "id" : "dddf6423-b7fd-4f40-b325-c34bdcdfb204",
      |            "requestType" : "SubmissionRequest",
      |            "requestTimestamp" : "2022-06-04T09:08:20.800Z[UTC]",
      |            "notifications" : [
      |                {
      |                    "notificationId" : "149a4470-f29c-4e33-8a75-b9a119a50c06",
      |                    "dateTimeIssued" : "2022-06-04T08:15:22Z[UTC]",
      |                    "enhancedStatus" : "GOODS_ARRIVED"
      |                },
      |                {
      |                    "notificationId" : "149a4470-f29c-4e33-8a75-b9a119a50c06",
      |                    "dateTimeIssued" : "2022-06-04T08:10:22Z[UTC]",
      |                    "enhancedStatus" : "CLEARED"
      |                },
      |                {
      |                    "notificationId" : "149a4470-f29c-4e33-8a75-b9a119a50c06",
      |                    "dateTimeIssued" : "2022-06-04T08:05:22Z[UTC]",
      |                    "enhancedStatus" : "GOODS_HAVE_EXITED"
      |                }
      |            ],
      |            "versionNo" : 1,
      |            "decId" : "id"
      |        }
      |    ],
      |    "enhancedStatusLastUpdated" : "2022-07-06T08:15:22Z[UTC]",
      |    "latestEnhancedStatus" : "GOODS_ARRIVED",
      |    "mrn" : "18GBJ4L5DKXCVUUNZZ",
      |    "latestDecId" : "TEST-N3fwz-PAwaKfh4",
      |    "latestVersionNo" : 1,
      |    "blockAmendments" : false
      |}
      |""".stripMargin)
    .as[Submission]

  val notifications = submission.actions.find(_.requestType == SubmissionRequest).value.notifications.value
}
