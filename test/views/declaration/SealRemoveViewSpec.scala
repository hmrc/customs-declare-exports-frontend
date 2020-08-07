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
import forms.common.YesNoAnswer
import forms.declaration.Seal
import helpers.views.declaration.CommonMessages
import models.Mode
import models.declaration.Container
import org.jsoup.nodes.Document
import org.scalatest.MustMatchers
import play.api.data.Form
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.seal_remove
import views.tags.ViewTest

@ViewTest
class SealRemoveViewSpec extends UnitViewSpec with Stubs with MustMatchers with CommonMessages with Injector {

  val containerId = "42354542"
  val sealId = "SealToRemove54214"
  val container = Some(Container(containerId, Seq(Seal(sealId))))
  private val form: Form[YesNoAnswer] = YesNoAnswer.form()
  private val page = instanceOf[seal_remove]

  private def createView(form: Form[YesNoAnswer] = form, containerId: String = containerId, sealId: String = sealId): Document =
    page(Mode.Normal, form, containerId, sealId)

  "Seal Remove View" should {

    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1").text() must be(messages("declaration.seal.remove.title", containerId))
    }

    "display section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.6")
    }

    "display Seal label" in {
      view.getElementsByClass("govuk-summary-list__key") must containMessageForElements("declaration.seal.summary.heading")
    }

    "display seal to remove" in {
      view.getElementsByClass("govuk-summary-list__value").text() must include(sealId)
    }

    "display 'Back' button that links to 'seal summary' page" in {
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

  "Seal Remove View for invalid input" should {
    "display error if nothing is entered" in {
      val view = createView(YesNoAnswer.form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#code_yes")

      view must containErrorElementWithMessageKey("error.yesNo.required")
    }

  }
}
