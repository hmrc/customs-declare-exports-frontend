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
import forms.common.YesNoAnswer.Yes
import forms.declaration.additionaldocuments.AdditionalDocument
import models.declaration.AdditionalDocuments
import models.{DeclarationType, Mode}
import play.api.Configuration
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.additional_documents

class AdditionalDocumentsViewSpec extends UnitViewSpec with ExportsTestHelper {

  private val item = anItem(withItemId("itemId"), withSequenceId(1))
  private val itemWithLicenceReq = anItem(withItemId("itemId"), withSequenceId(1)).copy(isLicenceRequired = Some(true))
  private val itemWithNoLicenceReq = anItem(withItemId("itemId"), withSequenceId(1)).copy(isLicenceRequired = Some(false))

  private val documents = Seq(
    AdditionalDocument(Some("typ1"), Some("identifier1"), None, None, None, None, None),
    AdditionalDocument(Some("typ2"), Some("identifier2"), None, None, None, None, None)
  )

  "Supporting documents view" when {

    val injector = new Injector {
      override val configuration: Configuration = Configuration(ConfigFactory.parseString("microservice.services.features.waiver999L=enabled"))
    }
    val additionalDocumentsSection = injector.instanceOf[additional_documents]

    "without documents or defined licence answer" should {

      "display title and additional docs row only" in {

        val view = additionalDocumentsSection(Mode.Normal, item, AdditionalDocuments(None, Seq.empty), DeclarationType.STANDARD)(messages)
        val section = view.getElementById("additional-docs-section-item-1")
        val docsRow = view.getElementsByClass("additional-documents-1-row")

        section.child(0) must containMessage("declaration.summary.items.item.additionalDocuments")

        Option(view.getElementsByClass("licences-1-row").first()) must be(None)

        docsRow must haveSummaryKey(messages("declaration.summary.items.item.additionalDocuments"))
        docsRow must haveSummaryValue("No")
        docsRow must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.additionalDocuments.changeAll", "1")
        docsRow must haveSummaryActionsHref(routes.AdditionalDocumentsController.displayPage(Mode.Normal, "itemId"))
      }

    }

    "with documents and licence answer" should {

      onJourney(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY, DeclarationType.SIMPLIFIED, DeclarationType.OCCASIONAL)(
        aDeclaration(withItem(itemWithLicenceReq))
      ) { implicit request =>
        "licence required" should {
          "display licences row" in {
            val view =
              additionalDocumentsSection(Mode.Normal, itemWithLicenceReq, AdditionalDocuments(Yes, documents), request.declarationType)(messages)
            val licencesRow = view.getElementsByClass("licences-1-row")

            licencesRow must haveSummaryKey(messages("declaration.summary.items.item.licences"))

          }

          "show h3 header" in {

            val view =
              additionalDocumentsSection(Mode.Normal, itemWithLicenceReq, AdditionalDocuments(Yes, documents), request.declarationType)(messages)

            view.getElementsByTag("h3") must containMessageForElements("declaration.summary.items.item.additionalDocuments")

          }
        }
      }

      onJourney(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY, DeclarationType.SIMPLIFIED, DeclarationType.OCCASIONAL)(
        aDeclaration(withItem(item))
      ) { implicit request =>
        "no licence required" should {
          "display section heading" in {
            val view =
              additionalDocumentsSection(Mode.Normal, itemWithNoLicenceReq, AdditionalDocuments(Yes, documents), request.declarationType)(messages)
            val section = view.getElementById("additional-docs-section-item-1")

            section.child(0).text() mustBe messages("declaration.summary.items.item.additionalDocuments")

          }

          "show row with link to `IsLicenceRequiredController`" in {

            val view =
              additionalDocumentsSection(Mode.Normal, itemWithNoLicenceReq, AdditionalDocuments(Yes, documents), request.declarationType)(messages)
            val row = view.getElementsByClass("licences-1-row")

            row must haveSummaryKey(messages("declaration.summary.items.item.licences"))
            row must haveSummaryValue("No")

            row must haveSummaryActionsTexts("site.change", "declaration.summary.items.item.additionalDocuments.changeAll", "1")

            row must haveSummaryActionsHref(routes.IsLicenceRequiredController.displayPage(Mode.Normal, "itemId"))

          }
        }
      }

      onJourney(DeclarationType.CLEARANCE) { implicit request =>
        "display section heading" in {
          val view = additionalDocumentsSection(Mode.Normal, item, AdditionalDocuments(Yes, documents), request.declarationType)(messages)
          val section = view.getElementById("additional-docs-section-item-1")

          section.child(0).text() mustBe messages("declaration.summary.items.item.clearance.additionalDocuments")

        }
      }

      "display all additional documents with change buttons" in {

        val view = additionalDocumentsSection(Mode.Normal, item, AdditionalDocuments(Yes, documents), DeclarationType.STANDARD)(messages)
        val table = view.getElementById("additional-documents-1-table")

        table.getElementsByClass("govuk-table__header").get(0).text() mustBe messages("declaration.summary.items.item.additionalDocuments.code")
        table.getElementsByClass("govuk-table__header").get(1).text() mustBe messages("declaration.summary.items.item.additionalDocuments.identifier")
        table.getElementsByClass("govuk-table__header").get(2).text() mustBe messages("site.change.header")

        val row1 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(0)
        row1.getElementsByClass("govuk-table__cell").get(0).text() mustBe "typ1"
        row1.getElementsByClass("govuk-table__cell").get(1).text() mustBe "identifier1"
        val row1ChangeLink = row1.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
        row1ChangeLink must haveHref(routes.AdditionalDocumentsController.displayPage(Mode.Normal, "itemId"))
        row1ChangeLink must containMessage("site.change")
        row1ChangeLink must containMessage("declaration.summary.items.item.additionalDocuments.change", "typ1", "identifier1", 1)

        val row2 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(1)
        row2.getElementsByClass("govuk-table__cell").get(0).text() mustBe "typ2"
        row2.getElementsByClass("govuk-table__cell").get(1).text() mustBe "identifier2"
        val row2ChangeLink = row2.getElementsByClass("govuk-table__cell").get(2).getElementsByTag("a").first()
        row2ChangeLink must haveHref(routes.AdditionalDocumentsController.displayPage(Mode.Normal, "itemId"))
        row2ChangeLink must containMessage("site.change")
        row2ChangeLink must containMessage("declaration.summary.items.item.additionalDocuments.change", "typ2", "identifier2", 1)
      }

      "display all additional documents without change buttons" when {

        "actionsEnabled is false" in {

          val view =
            additionalDocumentsSection(Mode.Normal, item, AdditionalDocuments(Yes, documents), DeclarationType.STANDARD, actionsEnabled = false)(
              messages
            )
          val table = view.getElementById("additional-documents-1-table")

          table.getElementsByClass("govuk-table__header").get(0).text() mustBe messages("declaration.summary.items.item.additionalDocuments.code")
          table.getElementsByClass("govuk-table__header").get(1).text() mustBe messages(
            "declaration.summary.items.item.additionalDocuments.identifier"
          )

          val row1 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(0)
          row1.getElementsByClass("govuk-table__cell").get(0).text() mustBe "typ1"
          row1.getElementsByClass("govuk-table__cell").get(1).text() mustBe "identifier1"
          val row1ChangeLink = row1.getElementsByClass("govuk-table__cell").get(2)
          row1ChangeLink.attr("href") mustBe empty
          row1ChangeLink.text() mustBe empty

          val row2 = table.getElementsByClass("govuk-table__body").first().getElementsByClass("govuk-table__row").get(1)
          row2.getElementsByClass("govuk-table__cell").get(0).text() mustBe "typ2"
          row2.getElementsByClass("govuk-table__cell").get(1).text() mustBe "identifier2"
          val row2ChangeLink = row2.getElementsByClass("govuk-table__cell").get(2)
          row2ChangeLink.attr("href") mustBe empty
          row2ChangeLink.text() mustBe empty
        }

      }

    }
  }

  "Supporting documents view with 999l disabled" when {

    val injector = new Injector {
      override val configuration: Configuration = Configuration(ConfigFactory.parseString("microservice.services.features.waiver999L=disabled"))
    }
    val additionalDocumentsSection = injector.instanceOf[additional_documents]

    "with documents" should {

      "display section heading" in {

        val view = additionalDocumentsSection(Mode.Normal, item, AdditionalDocuments(Yes, documents), DeclarationType.STANDARD)(messages)
        val section = view.getElementById("additional-docs-section-item-1")

        section.child(0).firstElementSibling().text() mustBe messages("declaration.summary.items.item.clearance.additionalDocuments")
      }
    }

  }

}
