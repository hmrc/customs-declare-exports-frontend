/*
 * Copyright 2021 HM Revenue & Customs
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

package views.declaration.declarationitems

import base.Injector
import controllers.declaration.routes
import models.Mode
import org.jsoup.nodes.Document
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.declarationitems.items_add_item
import views.tags.ViewTest

@ViewTest
class ItemsAddItemViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = instanceOf[items_add_item]
  private def createView(mode: Mode = Mode.Normal): Document = page(mode)(journeyRequest(), messages)

  "ItemsAddItem View" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("declaration.itemsAdd.title")
      messages must haveTranslationFor("declaration.itemsAdd.paragraph.1")
      messages must haveTranslationFor("declaration.itemsAdd.paragraph.2")
      messages must haveTranslationFor("declaration.itemsAdd.paragraph.3")
    }

    val view = createView()

    "display 'Back' button" in {

      view.getElementById("back-link") must haveHref(routes.PreviousDocumentsSummaryController.displayPage())
    }

    "display section header" in {

      view.getElementById("section-header") must containMessage("declaration.section.5")
    }

    "display title" in {

      view.getElementsByClass("govuk-heading-xl").first() must containMessage("declaration.itemsAdd.title")
    }

    "display paragraph" in {

      val paragraphs = view.getElementsByClass("govuk-body")

      paragraphs.get(0) must containMessage("declaration.itemsAdd.paragraph.1")
      paragraphs.get(1) must containMessage("declaration.itemsAdd.paragraph.2")
      paragraphs.get(2) must containMessage("declaration.itemsAdd.paragraph.3")
    }

    "display 'Add item' button" in {

      view must containElementWithID("add")
      view.getElementById("add") must containMessage("site.add.item")
    }

    "display 'Save and come back later' link" in {

      view must containElementWithID("submit_and_return")
      view.getElementById("submit_and_return") must containMessage("site.save_and_come_back_later")
    }
  }

}
