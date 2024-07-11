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

package views.section5.items

import base.Injector
import controllers.summary.routes.SectionSummaryController
import models.DeclarationType.{CLEARANCE, STANDARD}
import org.jsoup.nodes.Document
import services.cache.ExportsTestHelper
import tools.Stubs
import views.common.UnitViewSpec
import views.html.section5.items.items_add_item
import views.tags.ViewTest

import scala.util.Try

@ViewTest
class ItemsAddItemViewSpec extends UnitViewSpec with ExportsTestHelper with Stubs with Injector {

  private val page = instanceOf[items_add_item]
  private def createView(journey: models.DeclarationType.Value = STANDARD): Document = page()(journeyRequest(journey), messages)

  "ItemsAddItem View" when {

    checkMessages(
      "declaration.itemsAdd.title",
      "declaration.clearance.itemsAdd.title",
      "declaration.itemsAdd.titleWithItem",
      "declaration.itemsAdd.titleWithItems",
      "declaration.itemsAdd.paragraph.1",
      "declaration.itemsAdd.inset.paragraph.1",
      "declaration.itemsAdd.inset.paragraph.2",
      "declaration.itemsAdd.inset.paragraph.3",
      "declaration.clearance.itemsAdd.paragraph.1",
      "declaration.itemsAdd.paragraph.2",
      "declaration.clearance.itemsAdd.paragraph.2",
      "declaration.itemsAdd.paragraph.3",
      "declaration.itemsAdd.change.hint",
      "declaration.itemsAdd.remove.hint"
    )

    "on a non-Clearance journey" should {

      val view = createView()

      "display 'Back' button" in {
        view.getElementById("back-link") must haveHref(SectionSummaryController.displayPage(4))
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.5")
      }

      "display title" in {
        view.getElementsByClass("govuk-heading-xl").first() must containMessage("declaration.itemsAdd.title")
      }

      "display paragraphs" in {
        val paragraphs = view.getElementsByClass("govuk-body")

        paragraphs.get(0) must containMessage("declaration.itemsAdd.paragraph.1")
        paragraphs.get(4) must containMessage("declaration.itemsAdd.paragraph.2")
        paragraphs.get(5) must containMessage("declaration.itemsAdd.paragraph.3")
      }

      "display inset text with paragraphs" in {
        Try(view.getElementsByClass("govuk-inset-text").first()).toOption mustBe defined

        val paragraphs = view.getElementsByClass("govuk-body")
        paragraphs.get(1) must containMessage("declaration.itemsAdd.inset.paragraph.1")
        paragraphs.get(2) must containMessage("declaration.itemsAdd.inset.paragraph.2")
        paragraphs.get(3) must containMessage("declaration.itemsAdd.inset.paragraph.3")
      }

      "display 'Add item' button" in {
        view must containElementWithID("add")
        view.getElementById("add") must containMessage("site.add.item")
      }
    }

    "on a clearance journey" should {

      val view = createView(CLEARANCE)

      "display 'Back' button" in {
        view.getElementById("back-link") must haveHref(SectionSummaryController.displayPage(4))
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.5")
      }

      "display title" in {
        view.getElementsByClass("govuk-heading-xl").first() must containMessage("declaration.clearance.itemsAdd.title")
      }

      "display paragraphs" in {
        val paragraphs = view.getElementsByClass("govuk-body")

        paragraphs.get(0) must containMessage("declaration.clearance.itemsAdd.paragraph.1")
        paragraphs.get(1) must containMessage("declaration.clearance.itemsAdd.paragraph.2")
      }

      "display 'Add item' button" in {
        view must containElementWithID("add")
        view.getElementById("add") must containMessage("site.add.item")
      }
    }
  }
}
