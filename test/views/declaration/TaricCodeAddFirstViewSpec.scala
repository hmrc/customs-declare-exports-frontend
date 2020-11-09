/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.declaration.TaricCodeFirst
import helpers.views.declaration.CommonMessages
import models.Mode
import org.jsoup.nodes.Document
import org.scalatest.MustMatchers
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.components.gds.Styles
import views.declaration.spec.UnitViewSpec
import views.html.declaration.taric_code_add_first
import views.tags.ViewTest

@ViewTest
class TaricCodeAddFirstViewSpec extends UnitViewSpec with ExportsTestData with Stubs with MustMatchers with CommonMessages with Injector {

  private val itemId = "item1"
  private val form: Form[TaricCodeFirst] = TaricCodeFirst.form()
  private val page = instanceOf[taric_code_add_first]

  private def createView(form: Form[TaricCodeFirst] = form): Document =
    page(Mode.Normal, itemId, form)(journeyRequest(), messages)

  "Taric Code Add First View" should {
    val view = createView()

    "display page title" in {
      view.getElementsByClass(Styles.gdsPageLegend) must containMessageForElements("declaration.taricAdditionalCodes.addfirst.header")
    }

    "display 'Back' button that links to 'CUS codes' page" in {
      val backLinkContainer = view.getElementById("back-link")

      backLinkContainer.getElementById("back-link") must haveHref(controllers.declaration.routes.CusCodeController.displayPage(Mode.Normal, itemId))
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

  "Taric Code Add First View for invalid input" should {

    "display errors when invalid" in {
      val view = createView(TaricCodeFirst.form().fillAndValidate(TaricCodeFirst(Some("12345678901234567890"))))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#taricCode")

      view must containErrorElementWithMessageKey("declaration.taricAdditionalCodes.error.invalid")
    }

    "display errors when empty" in {
      val view = createView(TaricCodeFirst.form().fillAndValidate(TaricCodeFirst(Some(""))))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#taricCode")

      view must containErrorElementWithMessageKey("declaration.taricAdditionalCodes.error.empty")
    }

  }

  "Taric Code Add First View when filled" should {

    "display data in taric code input" in {

      val view = createView(TaricCodeFirst.form().fill(TaricCodeFirst(Some("ABCD"))))

      view.getElementById("taricCode").attr("value") must be("ABCD")
    }
  }
}
