/*
 * Copyright 2019 HM Revenue & Customs
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
import forms.declaration.officeOfExit.{OfficeOfExitForms, OfficeOfExitSupplementary}
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.office_of_exit_supplementary
import views.tags.ViewTest

@ViewTest
class OfficeOfExitSupplementaryViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = new office_of_exit_supplementary(mainTemplate)
  private val form: Form[OfficeOfExitSupplementary] = OfficeOfExitForms.supplementaryForm()
  private def createView(mode: Mode = Mode.Normal, form: Form[OfficeOfExitSupplementary] = form): Document =
    page(mode, form)(journeyRequest(), stubMessages())

  "Office of Exit View on empty page" should {
    val view = createView()

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("declaration.officeOfExit")
      messages must haveTranslationFor("declaration.summary.locations.header")
      messages must haveTranslationFor("declaration.officeOfExit.hint")
      messages must haveTranslationFor("declaration.officeOfExit.length")
      messages must haveTranslationFor("declaration.officeOfExit.empty")
    }

    "display page title" in {
      view.getElementById("title").text() mustBe "declaration.officeOfExit.title"
    }

    "display section header" in {
      view.getElementById("section-header").text() must include("declaration.summary.locations.header")
    }

    "display empty input with label for Country" in {
      view.getElementById("officeId-hint").text() mustBe "declaration.officeOfExit.hint"
      view.getElementById("officeId").attr("value") mustBe empty
    }

    "display 'Back' button that links to 'Location of Goods' page" in {

      val backButton = view.getElementById("link-back")

      backButton.text() mustBe "site.back"
      backButton.getElementById("link-back") must haveHref(controllers.declaration.routes.LocationController.displayPage(Mode.Normal))
    }

    "display 'Save and continue' button" in {
      val saveButton = view.getElementById("submit")
      saveButton.text() mustBe "site.save_and_continue"
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton.text() mustBe "site.save_and_come_back_later"
    }
  }

  "Office of Exit View for invalid input" should {

    "display error when Office of Exit is incorrect" in {
      val view =
        createView(form = OfficeOfExitForms.supplementaryForm.fillAndValidate(OfficeOfExitSupplementary("123456789")))

      checkErrorsSummary(view)
      haveFieldErrorLink("officeId", "#officeId")

      view.getElementById("error-message-officeId-input").text() mustBe "declaration.officeOfExit.length"
    }

    "display error when Office of Exit is empty" in {

      val view = createView(form = OfficeOfExitForms.supplementaryForm.fillAndValidate(OfficeOfExitSupplementary("")))

      checkErrorsSummary(view)
      haveFieldErrorLink("officeId", "#officeId")

      view.getElementById("error-message-officeId-input").text() mustBe "declaration.officeOfExit.empty"
    }
  }

  "Office of Exit View when filled" should {

    "display data in Office of Exit input" in {

      val view = createView(form = OfficeOfExitForms.supplementaryForm.fill(OfficeOfExitSupplementary("12345678")))

      view.getElementById("officeId").attr("value") mustBe "12345678"
    }
  }
}
