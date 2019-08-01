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
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.add_transport_containers
import views.tags.ViewTest

@ViewTest
class TransportInformationContainersViewSpec
    extends ViewSpec with TransportInformationContainerMessages with CommonMessages {

  private val form: Form[TransportInformationContainer] = TransportInformationContainer.form()
  private val transportContainersPage = app.injector.instanceOf[add_transport_containers]
  private def createView(form: Form[TransportInformationContainer] = form): Html =
    transportContainersPage(form, Seq())(fakeRequest, messages)

  "Transport Information Containers View" should {

    "have proper labels for messages" in {

      assertMessage(container, "7/2 Were the goods in a container?")
      assertMessage(containersTitle, "7/10 Transport Information Containers")
      assertMessage(transportContainerId, "7/10 Enter the container ID")
      assertMessage(ticTitle, "Container ID")
    }

    "have proper labels for error messages" in {

      assertMessage(ticEmpty, "Container ID cannot be empty")
      assertMessage(ticError, "Container ID is incorrect")
      assertMessage(ticErrorAlphaNumeric, "Only alphanumeric characters allowed")
      assertMessage(ticErrorLength, "Only 17 alphanumeric characters are allowed")
    }
  }

  "Transport Information Containers View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(containersTitle))
    }

    "display header" in {

      getElementByCss(createView(), "legend>h1").text() must be(messages(containersTitle))
    }

    "display empty input with label for Container ID" in {

      val view = createView()

      getElementByCss(view, "form>div.form-field>label>span").text() must be(messages(transportContainerId))
      getElementById(view, "id").attr("value") must be("")
    }

    "display 'Back' button that links to 'Transport Information' page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/transport-details")
    }

    "display both 'Add' and 'Save and continue' button on page" in {

      val view = createView()

      val addButton = getElementByCss(view, "#add")
      addButton.text() must be(messages(addCaption))

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Transport Information Container View when filled" should {

    "display data in Container ID input" in {

      val view = createView(TransportInformationContainer.form().fill(TransportInformationContainer("Test")))

      getElementById(view, "id").attr("value") must be("Test")
    }

    "display one row with data in table" in {

      val view =
        transportContainersPage(form, Seq(TransportInformationContainer("Test")))(fakeRequest, messages)

      // table header
      getElementByCss(view, "form>div.field-group>table>thead>tr>th").text() must be(messages(ticTitle))

      // table row
      getElementByCss(view, "form>div.field-group>table>tbody>tr>td:nth-child(1)").text() must be("Test")
      getElementByCss(view, "form>div.field-group>table>tbody>tr>td:nth-child(2)>button").text() must be(
        messages(removeCaption)
      )
    }
  }
}
