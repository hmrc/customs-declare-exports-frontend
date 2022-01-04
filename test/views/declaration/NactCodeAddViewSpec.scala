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
import forms.declaration.NactCode
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.nact_code_add
import views.tags.ViewTest

@ViewTest
class NactCodeAddViewSpec extends UnitViewSpec with ExportsTestData with Stubs with CommonMessages with Injector {

  private val itemId = "item1"
  private val form: Form[NactCode] = NactCode.form()
  private val page = instanceOf[nact_code_add]

  private def createView(form: Form[NactCode] = form): Document =
    page(Mode.Normal, itemId, form)(journeyRequest(), messages)

  "Nact Code Add View" should {
    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.nationalAdditionalCode.addnext.header")
    }

    "display 'Back' button that links to 'Nact code summary' page" in {
      val backLinkContainer = view.getElementById("back-link")

      backLinkContainer.getElementById("back-link") must haveHref(
        controllers.declaration.routes.NactCodeSummaryController.displayPage(Mode.Normal, itemId)
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

  "Nact Code Add View for invalid input" should {

    "display error if nothing is entered" in {
      val view = createView(NactCode.form().fillAndValidate(NactCode("")))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#nactCode")

      view must containErrorElementWithMessageKey("declaration.nationalAdditionalCode.error.empty")
    }

    "display error if incorrect nact code is entered" in {
      val view = createView(NactCode.form().fillAndValidate(NactCode("12345678901234567890")))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#nactCode")

      view must containErrorElementWithMessageKey("declaration.nationalAdditionalCode.error.invalid")
    }

  }

  "Nact Code Add View when filled" should {

    "display data in Nact code input" in {

      val view = createView(NactCode.form().fill(NactCode("VATR")))

      view.getElementById("nactCode").attr("value") must be("VATR")
    }
  }
}
