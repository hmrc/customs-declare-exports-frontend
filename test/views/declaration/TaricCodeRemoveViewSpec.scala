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

package views.declaration

import base.Injector
import forms.common.YesNoAnswer
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.taric_code_remove
import views.tags.ViewTest

@ViewTest
class TaricCodeRemoveViewSpec extends UnitViewSpec with Stubs with CommonMessages with Injector {

  private val itemId = "item1"
  private val taricCode = "TARI"
  private val form: Form[YesNoAnswer] = YesNoAnswer.form()
  private val page = instanceOf[taric_code_remove]

  private def createView(form: Form[YesNoAnswer] = form, code: String = taricCode): Document =
    page(Mode.Normal, itemId, code, form)(request, messages)

  "Taric Code Remove View" should {
    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.taricAdditionalCodes.remove.header")
    }

    "display Additional Code label" in {
      view.getElementsByClass("govuk-summary-list__key") must containMessageForElements("declaration.taricAdditionalCodes.table.header")
    }

    "display Taric code to remove" in {
      view.getElementsByClass("govuk-summary-list__value").text() mustBe taricCode
    }

    "display 'Back' button that links to 'taric codes summary' page" in {
      val backLinkContainer = view.getElementById("back-link")

      backLinkContainer must containMessage(backCaption)
      backLinkContainer.getElementById("back-link") must haveHref(
        controllers.declaration.routes.TaricCodeSummaryController.displayPage(Mode.Normal, itemId)
      )
    }

    "display 'Save and continue' button on page" in {
      val saveButton = view.getElementById("submit")
      saveButton must containMessage(saveAndContinueCaption)
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton must containMessage(saveAndReturnCaption)
    }
  }

  "Taric Code Remove View for invalid input" should {

    "display error if nothing is entered" in {
      val view = createView(YesNoAnswer.form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#code_yes")

      view must containErrorElementWithMessageKey("error.yesNo.required")
    }

  }
}
