/*
 * Copyright 2022 HM Revenue & Customs
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
import com.typesafe.config.ConfigFactory
import controllers.declaration.routes
import forms.common.YesNoAnswer.{Yes, YesNoAnswers}
import forms.declaration.additionaldocuments.AdditionalDocument
import models.DeclarationType._
import models.Mode.Normal
import models.declaration.{AdditionalDocuments, ExportItem}
import org.jsoup.select.Elements
import org.scalatest.Assertion
import play.api.Configuration
import play.twirl.api.HtmlFormat.Appendable
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.additional_documents

class AdditionalDocumentsViewSpec extends UnitViewSpec with ExportsTestHelper {

  private val documents = Seq(
    AdditionalDocument(Some("typ1"), Some("identifier1"), None, None, None, None, None),
    AdditionalDocument(Some("typ2"), Some("identifier2"), None, None, None, None, None)
  )

  private def itemWithLicenceReq(
    licenceRequired: String,
    maybeAdditionalDocuments: Option[AdditionalDocuments] = Some(AdditionalDocuments(Yes, documents))
  ): ExportItem = {
    val isLicenceRequired = Some(licenceRequired == YesNoAnswers.yes)
    val additionalDocuments = maybeAdditionalDocuments.fold(withoutAdditionalInformation)(withAdditionalDocuments)
    anItem(withItemId("itemId"), withSequenceId(1), additionalDocuments).copy(isLicenceRequired = isLicenceRequired)
  }

  private def itemWithNoLicenceReq(maybeAdditionalDocuments: Option[AdditionalDocuments] = Some(AdditionalDocuments(Yes, documents))): ExportItem = {
    val additionalDocuments = maybeAdditionalDocuments.fold(withoutAdditionalInformation)(withAdditionalDocuments)
    anItem(withItemId("itemId"), withSequenceId(1), additionalDocuments)
  }

  "AdditionalDocuments view" when {

    val is999LKey = "microservice.services.features.waiver999L"
    val config = ConfigFactory.parseString(s"$is999LKey=enabled")
    val injector = new Injector {
      override val configuration: Configuration = Configuration(config)
    }
    val additionalDocumentsSection = injector.instanceOf[additional_documents]

    "with additionalDocuments defined but without actual documents or defined licence answer" should {
      "display title and 'No Additional documents' row only" in {
        val view = additionalDocumentsSection(Normal, itemWithNoLicenceReq(Some(AdditionalDocuments(None, Seq.empty))))(messages)
        verifyHeader(view)

        view.getElementsByClass("licences-1-row") mustBe empty
        view.getElementsByTag("table") mustBe empty

        verifyNoAdditionalDocuments(view, config.getString(is999LKey))
      }
    }

    List(YesNoAnswers.yes, YesNoAnswers.no).foreach { answer =>
      s"with licence answer equal to '$answer' and" when {

        onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL)(aDeclaration(withItem(itemWithLicenceReq(answer)))) { implicit request =>
          "with Additional Documents" should {
            "display a licences row and a table for the Additional Documents" in {
              val view = additionalDocumentsSection(Normal, itemWithLicenceReq(answer))(messages)
              verifyHeader(view)
              verifyLicenseRow(view, answer)
              verifyAdditionalDocumentsTable(view)
            }
          }
        }

        onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL)(aDeclaration(withItem(itemWithLicenceReq(answer, None)))) { implicit request =>
          "without Additional Documents" should {
            "display a licences row" in {
              val view = additionalDocumentsSection(Normal, itemWithLicenceReq(answer, None))(messages)
              verifyHeader(view)
              verifyLicenseRow(view, answer)
              verifyNoAdditionalDocuments(view, config.getString(is999LKey))
            }
          }
        }
      }
    }

    onJourney(CLEARANCE) { implicit request =>
      "with actual documents and" when {

        "actions are enabled" should {
          "not have a 'License' section and have 'Change' buttons" in {
            val view = additionalDocumentsSection(Normal, itemWithNoLicenceReq())(messages)
            verifyHeader(view)
            view.getElementsByClass("licences-1-row") mustBe empty
            verifyAdditionalDocumentsTable(view)
          }
        }

        "actions are disabled" should {
          "not have a 'License' section and not have 'Change' buttons" in {
            val view = additionalDocumentsSection(Normal, itemWithNoLicenceReq(), actionsEnabled = false)(messages)
            verifyHeader(view)
            verifyAdditionalDocumentsTable(view, false)
          }
        }
      }
    }
  }

  "AdditionalDocuments view" when {
    "with additionalDocuments defined but without actual documents or defined licence answer" should {
      "only display title and 'Other Documents' as header in the 'No Additional Documents' section" when {
        "the '999l' flag is disabled" in {
          val is999LKey = "microservice.services.features.waiver999L"
          val config = ConfigFactory.parseString(s"$is999LKey=disabled")
          val injector = new Injector {
            override val configuration: Configuration = Configuration(config)
          }
          val additionalDocumentsSection = injector.instanceOf[additional_documents]
          val view = additionalDocumentsSection(Normal, itemWithNoLicenceReq(Some(AdditionalDocuments(None, Seq.empty))))(messages)
          verifyHeader(view)

          view.getElementsByClass("licences-1-row") mustBe empty
          view.getElementsByTag("table") mustBe empty

          verifyNoAdditionalDocuments(view, config.getString(is999LKey))
        }
      }
    }
  }

  private def verifyHeader(view: Appendable): Assertion = {
    val section = view.getElementById("additional-docs-section-item-1")
    section.child(0).tagName mustBe "h3"
    section.child(0).text mustBe messages("declaration.summary.items.item.additionalDocuments")
  }

  private def verifyLicenseRow(view: Appendable, expectedValue: String): Assertion = {
    val licencesRow = view.getElementsByClass("licences-1-row")
    licencesRow must haveSummaryKey(messages("declaration.summary.items.item.licences"))
    licencesRow must haveSummaryValue(expectedValue)
    licencesRow must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.additionalDocuments.changeAll", "1")
    licencesRow must haveSummaryActionsHref(routes.IsLicenceRequiredController.displayPage(Normal, "itemId"))
  }

  private def verifyNoAdditionalDocuments(view: Appendable, flag999L: String): Assertion = {
    val is999L = flag999L == "enabled"
    val row = view.getElementsByClass("additional-documents-1-row")
    row must haveSummaryKey(messages(s"declaration.summary.items.item.additionalDocuments${if (is999L) "" else ".otherDocuments"}"))
    row must haveSummaryValue("No")
    row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.additionalDocuments.changeAll", "1")
    row must haveSummaryActionsHref(routes.AdditionalDocumentsController.displayPage(Normal, "itemId"))
  }

  private def verifyAdditionalDocumentsTable(view: Appendable, actionsEnabled: Boolean = true): Assertion = {
    val table = view.getElementById("additional-documents-1-table")

    val tableHeader = table.getElementsByClass("govuk-table__header")
    tableHeader.get(0).text mustBe messages("declaration.summary.items.item.additionalDocuments.code")
    tableHeader.get(1).text mustBe messages("declaration.summary.items.item.additionalDocuments.identifier")
    tableHeader.get(2).text mustBe messages("site.change.header")

    val rows = table.getElementsByClass("govuk-table__body").first.getElementsByClass("govuk-table__row")
    verifyAdditionalDocumentsRow(rows, 1, actionsEnabled)
    verifyAdditionalDocumentsRow(rows, 2, actionsEnabled)
  }

  private def verifyAdditionalDocumentsRow(rows: Elements, id: Int, actionsEnabled: Boolean): Assertion = {
    val row = rows.get(id - 1)
    row.getElementsByClass("govuk-table__cell").get(0).text mustBe s"typ$id"
    row.getElementsByClass("govuk-table__cell").get(1).text mustBe s"identifier$id"
    val rowChange = row.getElementsByClass("govuk-table__cell").get(2)
    if (actionsEnabled) {
      val rowChangeLink = rowChange.getElementsByTag("a").first
      rowChangeLink must haveHref(routes.AdditionalDocumentsController.displayPage(Normal, "itemId"))
      rowChangeLink must containMessage("site.change")
      rowChangeLink must containMessage("declaration.summary.items.item.additionalDocuments.change", s"typ$id", s"identifier$id", 1)
    } else {
      rowChange.attr("href") mustBe empty
      rowChange.text mustBe empty
    }
  }
}
