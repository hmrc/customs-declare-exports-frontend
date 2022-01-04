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

package views.declaration.declarationitems

import base.Injector
import controllers.declaration.routes
import forms.common.YesNoAnswer
import models.Mode
import models.declaration.ExportItem
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import services.cache.ExportsTestData
import tools.Stubs
import views.components.gds.Styles
import views.declaration.spec.UnitViewSpec
import views.html.declaration.declarationitems.items_remove_item
import views.tags.ViewTest

@ViewTest
class ItemsRemoveItemViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = instanceOf[items_remove_item]
  private val form = YesNoAnswer.form()
  private def createView(mode: Mode = Mode.Normal, form: Form[YesNoAnswer] = form, item: ExportItem): Document =
    page(mode, form, item)(journeyRequest(), messages)

  private val exportItem = anItem()

  "ItemsRemoveItem View" should {

    "have proper messages for labels" in {

      messages must haveTranslationFor("declaration.itemsRemove.title")
    }

    val view = createView(item = exportItem)

    "display 'Back' button" in {

      view.getElementById("back-link") must haveHref(routes.ItemsSummaryController.displayItemsSummaryPage())
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

      view.getElementsByClass(Styles.gdsPageLegend).first() must containMessage("declaration.itemsRemove.title", "0")
    }

    "display Item Section table" in {

      view must containElementWithID("declaration-items-summary-0")
    }

    "not display Item Section header" in {}

    "display Yes - No form" in {

      view must containElementWithClass("govuk-radios")
      view must containElementWithID("code_yes")
      view must containElementWithID("code_no")
    }

    "display buttons section" in {

      view must containElementWithID("submit")
      view must containElementWithID("submit_and_return")
    }
  }

}
