/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.declaration.Seal
import helpers.views.declaration.CommonMessages
import models.Mode
import org.jsoup.nodes.Document
import org.scalatest.MustMatchers
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.seal_add
import views.tags.ViewTest

@ViewTest
class SealAddViewSpec extends UnitViewSpec with ExportsTestData with Stubs with MustMatchers with CommonMessages with Injector {

  val containerId = "867126538"
  private val form: Form[Seal] = Seal.form()
  private val page = instanceOf[seal_add]

  private def createView(form: Form[Seal] = form): Document = page(Mode.Normal, form, containerId)(journeyRequest(), messages)

  "Seal Add View" should {
    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1").text() must be(messages("declaration.seal.title", containerId))
    }

    "display section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.6")
    }

    "display 'Back' button that links to 'seals summary' page" in {
      val backLinkContainer = view.getElementById("back-link")

      backLinkContainer.text() must be(messages(backCaption))
      backLinkContainer.getElementById("back-link") must haveHref(
        controllers.declaration.routes.SealController.displaySealSummary(Mode.Normal, containerId)
      )
    }

    "display 'Save and continue' button on page" in {
      val saveButton = view.getElementById("submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton.text() must be(messages(saveAndReturnCaption))
    }
  }

  "Seal Add View for invalid input" should {

    "display error if nothing is entered" in {
      val view = createView(Seal.form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#id")

      view must containErrorElementWithMessageKey("error.required")
    }

    "display error if incorrect seal is entered" in {
      val view = createView(Seal.form().fillAndValidate(Seal("Invalid!!!")))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#id")

      view must containErrorElementWithMessageKey("declaration.transport.sealId.error.invalid")
    }

  }
}
