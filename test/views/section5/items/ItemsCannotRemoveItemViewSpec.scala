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
import controllers.declaration.routes.{SectionSummaryController, SummaryController}
import controllers.section5.routes.ItemsSummaryController
import models.declaration.ExportItem
import org.jsoup.nodes.Document
import play.api.mvc.Call
import services.cache.ExportsTestHelper
import tools.Stubs
import views.common.UnitViewSpec
import views.components.gds.Styles
import views.html.section5.items.items_cannot_remove
import views.tags.ViewTest

@ViewTest
class ItemsCannotRemoveItemViewSpec extends UnitViewSpec with ExportsTestHelper with Stubs with Injector {

  private val page = instanceOf[items_cannot_remove]
  private val itemIdx = 0
  private val itemDisplayNum = itemIdx + 1
  private val parentDecId = "id"

  private def createView(item: ExportItem, referrer: Call = ItemsSummaryController.displayItemsSummaryPage): Document =
    page(item, itemIdx, parentDecId, referrer)(journeyRequest(), messages)

  private val exportItem = anItem()

  "ItemsRemoveItem View" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("declaration.itemsCannotRemove.title")
    }

    val view = createView(exportItem)

    "display 'Back' button pointing to /declaration-items-list" in {
      view.getElementById("back-link") must haveHref(ItemsSummaryController.displayItemsSummaryPage)
    }

    "display 'Back' button pointing to /summary-section/5" in {
      val view = createView(exportItem, SectionSummaryController.displayPage(5))
      view.getElementById("back-link") must haveHref(SectionSummaryController.displayPage(5))
    }

    "display 'Back' button pointing to /saved-summary" in {
      val view = createView(exportItem, SummaryController.displayPage)
      view.getElementById("back-link") must haveHref(SummaryController.displayPage)
    }

    "display section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.5")
    }

    "display title" in {
      view.getElementsByClass(Styles.gdsPageHeading).first() must containMessage("declaration.itemsCannotRemove.title", itemDisplayNum)
    }

    "display Item Section table" in {
      view must containElementWithID(s"declaration-items-summary-$itemDisplayNum")
    }

    "display cancel button" in {
      val button = createView(item = exportItem).getElementsByClass("govuk-button").first()
      button must containMessage("site.cancel")
    }

    "display warning text" in {
      val view = createView(item = exportItem)
      view.getElementsByClass("govuk-warning-text").first() must containText(
        messages("declaration.itemsCannotRemove.warning", messages("declaration.itemsCannotRemove.warning.linkText"))
      )
    }
  }
}
