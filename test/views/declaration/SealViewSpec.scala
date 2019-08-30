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
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.seal
import views.tags.ViewTest
import views.html.components.fields.field_text

@ViewTest
class SealViewSpec extends SealFields with CommonMessages {

  private val sealPage = app.injector.instanceOf[seal]
  def createView(form: Form[Seal] = form, container: Boolean = false): Html =
    sealPage(Mode.Normal, form, Seq.empty, container)(fakeRequest, messages)

  "Seal View" should {

    "display page title" in {
      val view = createView()

      view.getElementById("title").text() must be(messages("standard.seal.title"))
    }

    "display header" in {
      val view = createView()

      view.select("legend>h1").text() must be(messages("standard.seal.title"))
    }

    "display 'Back' button that links to 'add-transport-containers'  or 'transport-details' page" in {
      val view = createView()

      val backLinkContainer = createView(container = true).getElementById("link-back")

      backLinkContainer.text() must be(messages(backCaption))
      backLinkContainer.attr("href") must be("/customs-declare-exports/declaration/add-transport-containers")

      val backLinkTrader = createView().getElementById("link-back")

      backLinkTrader.text() must be(messages(backCaption))
      backLinkTrader.attr("href") must be("/customs-declare-exports/declaration/transport-details")
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
}

trait SealFields extends ViewSpec {
  val form: Form[Seal] = Seal.form()

  val id = field_text(field = form("id"), label = "7/18 Seal identification number").body

}
