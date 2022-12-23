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
import controllers.declaration.routes.TaricCodeSummaryController
import forms.declaration.TaricCode
import forms.declaration.TaricCode.form
import org.jsoup.nodes.Document
import play.api.data.Form
import views.declaration.spec.UnitViewSpec
import views.html.declaration.taric_code_add
import views.tags.ViewTest

@ViewTest
class TaricCodeAddViewSpec extends UnitViewSpec with Injector {

  val page = instanceOf[taric_code_add]

  def createView(frm: Form[TaricCode] = form): Document =
    page(itemId, frm)(journeyRequest(), messages)

  "Taric Code Add View" should {
    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.taricAdditionalCodes.addnext.header")
    }

    "display 'Back' button that links to 'Taric code summary' page" in {
      val backLinkContainer = view.getElementById("back-link")
      backLinkContainer.getElementById("back-link") must haveHref(TaricCodeSummaryController.displayPage(itemId))
    }

    checkAllSaveButtonsAreDisplayed(createView())
  }

  "Taric Code Add View for invalid input" should {

    "display error if nothing is entered" in {
      val view = createView(form.fillAndValidate(TaricCode("")))
      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#taricCode")
      view must containErrorElementWithMessageKey("declaration.taricAdditionalCodes.error.empty")
    }

    "display error if incorrect tric code is entered" in {
      val view = createView(form.fillAndValidate(TaricCode("12345678901234567890")))
      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#taricCode")
      view must containErrorElementWithMessageKey("declaration.taricAdditionalCodes.error.invalid")
    }
  }

  "Taric Code Add View when filled" should {
    "display data in taric code input" in {
      val view = createView(form.fill(TaricCode("4321")))
      view.getElementById("taricCode").attr("value") must be("4321")
    }
  }
}
