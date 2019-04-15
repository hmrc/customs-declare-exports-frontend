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
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.seal
import views.tags.ViewTest
import views.html.components.{input_text}

@ViewTest
class SealViewSpec extends SealFields with CommonMessages {

  def createView(form: Form[Seal] = form, container: Boolean = false): Html =
    seal(form, Seq.empty, container)(appConfig, fakeRequest, messages)

  "Seal View" should {

    "display page title" in {
      val view = createView()

      getElementById(view, "title").text() must be(messages("standard.seal.title"))
    }

    "display header" in {
      val view = createView()

      getElementByCss(view, "legend>h1").text() must be(messages("standard.seal.title"))
    }

    "display \"Back\" button that links to \"add-transport-containers\"  or \"transport-details\" page" in {
      val view = createView()

      val backLinkContainer = getElementById(createView(container = true), "link-back")

      backLinkContainer.text() must be(messages(backCaption))
      backLinkContainer.attr("href") must be("/customs-declare-exports/declaration/add-transport-containers")

      val backLinkTrader = getElementById(createView(), "link-back")

      backLinkTrader.text() must be(messages(backCaption))
      backLinkTrader.attr("href") must be("/customs-declare-exports/declaration/transport-details")
    }

    "display \"Save and continue\" button on page" in {
      val view = createView()

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }

    "have all fields with labels" in {
      val view = createView()
      view.body must include(id)
    }
  }
}

trait SealFields extends ViewSpec {
  val form: Form[Seal] = Seal.form()

  val id = input_text(field = form("id"), label = "7/18 Seal identification number").body

}
