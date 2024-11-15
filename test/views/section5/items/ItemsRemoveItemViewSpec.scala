/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.section5.routes.ItemsSummaryController
import controllers.summary.routes.{SectionSummaryController, SummaryController}
import forms.common.YesNoAnswer
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.api.data.{Form, FormError}
import play.api.mvc.Call
import views.common.UnitViewSpec
import views.components.gds.Styles
import views.html.section5.items.items_remove_item
import views.tags.ViewTest

@ViewTest
class ItemsRemoveItemViewSpec extends UnitViewSpec with Injector {

  private val page = instanceOf[items_remove_item]
  private val form = YesNoAnswer.form()
  private val itemIdx = 0
  private val itemDisplayNum = itemIdx + 1

  private val defaultCall = SummaryController.displayPage

  private def createView(form: Form[YesNoAnswer] = form, referrer: Call = defaultCall): Document =
    page(form, exportItem, itemIdx, referrer)(journeyRequest(), messages)

  private val exportItem = anItem()

  "ItemsRemoveItem View" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("declaration.itemsRemove.title")
    }

    val view = createView()

    "display 'Back' button pointing to /declaration-items-list" in {
      val view = createView(referrer = ItemsSummaryController.displayItemsSummaryPage)
      checkBackButton(view, ItemsSummaryController.displayItemsSummaryPage)
    }

    "display 'Back' button pointing to /summary-section/5" in {
      val view = createView(referrer = SectionSummaryController.displayPage(5))
      checkBackButton(view, SectionSummaryController.displayPage(5))
    }

    "display 'Back' button pointing to /saved-summary" in {
      checkBackButton(view, defaultCall)
    }

    "display error section" in {
      val formWithErrors = form.copy(errors = Seq(FormError("errorKey", "declaration.cusCode.error.empty")))
      val view = createView(form = formWithErrors)

      view must haveGovukGlobalErrorSummary
      view must containElementWithClass("govuk-error-summary__list")
    }

    "display section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.5")
    }

    "display title" in {
      view.getElementsByClass(Styles.gdsPageLegend).first() must containMessage("declaration.itemsRemove.title", itemDisplayNum)
    }

    "not display Item Section header" in {}

    "display Yes - No form" in {
      view must containElementWithClass("govuk-radios")
      view must containElementWithID("code_yes")
      view must containElementWithID("code_no")
    }

    "display confirm and continue button when editing from summary" in {
      val button = view.getElementById("save_and_return_to_summary")
      button must containMessage("site.confirm_and_continue")
    }
  }

  private def checkBackButton(view: Document, call: Call): Assertion = {
    val backButton = view.getElementById("back-link")
    backButton.text mustBe messages("site.back")
    backButton must haveHref(call)
  }
}
