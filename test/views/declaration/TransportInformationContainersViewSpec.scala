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

import forms.declaration.TransportInformationContainer
import helpers.views.declaration.{CommonMessages, TransportInformationContainerMessages}
import models.Mode
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.AppViewSpec
import views.html.declaration.add_transport_containers
import views.tags.ViewTest

@ViewTest
class TransportInformationContainersViewSpec
    extends AppViewSpec with TransportInformationContainerMessages with CommonMessages {

  private val form: Form[TransportInformationContainer] = TransportInformationContainer.form()
  private val transportContainersPage = app.injector.instanceOf[add_transport_containers]
  private def createView(form: Form[TransportInformationContainer] = form): Html =
    transportContainersPage(Mode.Normal, form, Seq())(fakeRequest, messages)

  "Transport Information Containers View on empty page" should {

    "display page title" in {

      createView().select("title").text() must be(messages(containersTitle))
    }

    "display header" in {

      createView().select("legend>h1").text() must be(messages(containersTitle))
    }

    "display empty input with label for Container ID" in {

      val view = createView()

      view.getElementById("id").attr("value") must be("")
      view.getElementById("id-label").text() must be(messages(transportContainerId))
    }

    "display 'Back' button that links to 'Transport Information' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/transport-details")
    }

    "display both 'Add' and 'Save and continue' button on page" in {

      val view = createView()

      val addButton = view.getElementById("add")
      addButton.text() must be(messages(addCaption))

      val saveButton = view.getElementById("submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }

    "display 'Save and return' button on page" in {

      val view = createView()

      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton.text() must be(messages(saveAndReturnCaption))
    }
  }

  "Transport Information Container View when filled" should {

    "display data in Container ID input" in {

      val view = createView(TransportInformationContainer.form().fill(TransportInformationContainer("Test")))

      view.getElementById("id").attr("value") must be("Test")
    }

    "display one row with data in table" in {

      val view =
        transportContainersPage(Mode.Normal, form, Seq(TransportInformationContainer("Test")))(fakeRequest, messages)

      // table header
      view.getElementById("removable_elements-heading") must containText(messages(ticTitle))

      // table row
      view.getElementById("removable_elements-row0-label") must containText("Test")
      view.getElementById("removable_elements-row0-remove_button") must containText(messages(removeCaption))
    }
  }
}
