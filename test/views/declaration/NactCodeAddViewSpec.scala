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
import forms.declaration.NactCode.form
import models.DeclarationType.STANDARD
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestHelper
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.nact_code_add
import views.tags.ViewTest

@ViewTest
class NactCodeAddViewSpec extends PageWithButtonsSpec with ExportsTestHelper with Injector {

  val page = instanceOf[nact_code_add]

  override val typeAndViewInstance = (STANDARD, page(itemId, form)(_, _))

  private def createView(frm: Form[NactCode] = form): Document =
    page(itemId, frm)(journeyRequest(), messages)

  "Nact Code Add View" should {
    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.nationalAdditionalCode.addnext.header")
    }

    "display 'Back' button that links to 'Nact code summary' page" in {
      val backLinkContainer = view.getElementById("back-link")

      backLinkContainer.getElementById("back-link") must haveHref(controllers.declaration.routes.NactCodeSummaryController.displayPage(itemId))
    }

    checkAllSaveButtonsAreDisplayed(createView())
  }

  "Nact Code Add View for invalid input" should {

    "display error if nothing is entered" in {
      val view = createView(form.fillAndValidate(NactCode("")))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#nactCode")

      view must containErrorElementWithMessageKey("declaration.nationalAdditionalCode.error.empty")
    }

    "display error if incorrect nact code is entered" in {
      val view = createView(form.fillAndValidate(NactCode("12345678901234567890")))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#nactCode")

      view must containErrorElementWithMessageKey("declaration.nationalAdditionalCode.error.invalid")
    }

  }

  "Nact Code Add View when filled" should {
    "display data in Nact code input" in {
      val view = createView(form.fill(NactCode("VATR")))
      view.getElementById("nactCode").attr("value") must be("VATR")
    }
  }
}
