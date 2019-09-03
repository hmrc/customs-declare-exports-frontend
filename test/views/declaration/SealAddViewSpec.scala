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

import forms.declaration.Seal
import helpers.views.declaration.CommonMessages
import models.Mode
import org.jsoup.nodes.Document
import org.scalatest.MustMatchers
import play.api.data.Form
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.seal_add
import views.tags.ViewTest

@ViewTest
class SealAddViewSpec extends UnitViewSpec with Stubs with MustMatchers with CommonMessages {

  private val form: Form[Seal] = Seal.form()

  private val sealAddPage = new seal_add(mainTemplate)

  val containerId = "867126538"

  private def createView(form: Form[Seal] = form): Document =
    sealAddPage(Mode.Normal, form, containerId)

  "Seal Add View" should {

    "display page title" in {
      createView().getElementById("title").text() must be(messages("standard.seal.title"))
    }

    "display header" in {
      createView().select("legend>h1").text() must be(messages("standard.seal.title"))
    }

    "display 'Back' button that links to 'seals summary' page" in {
      val backLinkContainer = createView().getElementById("link-back")

      backLinkContainer.text() must be(messages(backCaption))
      backLinkContainer.attr("href") must be(s"/customs-declare-exports/declaration/containers/$containerId/seals")
    }

    "display 'Save and continue' button on page" in {
      val saveButton = createView().getElementById("submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = createView().getElementById("submit_and_return")
      saveAndReturnButton.text() must be(messages(saveAndReturnCaption))
    }
  }

  "Seal Add View for invalid input" should {

    "display error if nothing is entered" in {
      val view = createView(Seal.form().bind(Map[String, String]()))

      view.select("#error-message-id-input").text() must be(messages("error.required"))
    }

    "display error if incorrect seal is entered" in {
      val view = createView(Seal.form().fillAndValidate(Seal("Invalid!!!")))

      view.select("#error-message-id-input").text() must be(messages("standard.transport.sealId.alphaNumeric.error"))
    }

  }
}
