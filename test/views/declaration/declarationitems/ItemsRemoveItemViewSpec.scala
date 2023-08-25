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

package views.declaration.declarationitems

import base.Injector
import controllers.declaration.routes.{ItemsSummaryController, SummaryController}
import forms.common.YesNoAnswer
import models.DeclarationType.{DeclarationType, STANDARD}
import models.declaration.ExportItem
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import services.cache.ExportsTestHelper
import tools.Stubs
import views.components.gds.Styles
import views.declaration.spec.{PageWithButtonsSpec, UnitViewSpec}
import views.html.declaration.declarationitems.items_remove_item
import views.tags.ViewTest

@ViewTest
class ItemsRemoveItemViewSpec extends UnitViewSpec with ExportsTestHelper with Stubs with Injector with PageWithButtonsSpec {

  private val page = instanceOf[items_remove_item]
  private val form = YesNoAnswer.form()
  private val itemIdx = 0
  private val itemDisplayNum = itemIdx + 1
  override val typeAndViewInstance: (DeclarationType, (JourneyRequest[_], Messages) => HtmlFormat.Appendable) =
    (STANDARD, page(form, exportItem, itemIdx, fromSummary = false)(_, _))

  private def createView(form: Form[YesNoAnswer] = form, item: ExportItem, fromSummary: Boolean = false): Document =
    page(form, item, itemIdx, fromSummary)(journeyRequest(), messages)

  private val exportItem = anItem()

  "ItemsRemoveItem View" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("declaration.itemsRemove.title")
    }

    val view = createView(item = exportItem)

    "display 'Back' button pointing to /declaration-items-list" in {
      view.getElementById("back-link") must haveHref(ItemsSummaryController.displayItemsSummaryPage)
    }

    "display 'Back' button pointing to /saved-summary" in {
      createView(item = exportItem, fromSummary = true).getElementById("back-link") must haveHref(SummaryController.displayPage)
    }

    "display error section" in {
      val formWithErrors = form.copy(errors = Seq(FormError("errorKey", "declaration.cusCode.error.empty")))
      val view = createView(form = formWithErrors, item = exportItem)

      view must haveGovukGlobalErrorSummary
      view must containElementWithClass("govuk-error-summary__list")
    }

    "display section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.5")
    }

    "display title" in {
      view.getElementsByClass(Styles.gdsPageLegend).first() must containMessage("declaration.itemsRemove.title", itemDisplayNum)
    }

    "display Item Section table" in {
      view must containElementWithID(s"declaration-items-summary-$itemDisplayNum")
    }

    "not display Item Section header" in {}

    "display Yes - No form" in {
      view must containElementWithClass("govuk-radios")
      view must containElementWithID("code_yes")
      view must containElementWithID("code_no")
    }

    checkSaveAndContinueButtonIsDisplayed(view)

    "display confirm and continue button when editing from summary" in {
      val button = createView(form, exportItem, true).getElementById("save_and_return_to_summary")
      button must containMessage("site.confirm_and_continue")
    }
  }
}
