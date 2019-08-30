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
import forms.declaration.TransportInformationContainer
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.add_transport_containers
import views.tags.ViewTest

@ViewTest
class TransportInformationContainersViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val form: Form[TransportInformationContainer] = TransportInformationContainer.form()

  private def createView(
    mode: Mode = Mode.Normal,
    form: Form[TransportInformationContainer] = form,
    containers: Seq[TransportInformationContainer] = Seq.empty
  ): Document =
    new add_transport_containers(mainTemplate)(mode, form, containers)(journeyRequest, stubMessages())

  "Transport Information Containers View on empty page" should {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest)
      messages("supplementary.transportInfo.containers.title") mustBe "Transport Information Containers"
      messages("supplementary.transportInfo.containerId") mustBe "Enter the container ID"
    }

    "display page title" in {
      createView().select("title").text() must be("supplementary.transportInfo.containers.title")
    }

    "display header" in {
      createView().select("legend>h1").text() must be("supplementary.transportInfo.containers.title")
    }

    "display empty input with label for Container ID" in {
      val view = createView()

      view.getElementById("id").attr("value") must be("")
      view.getElementById("id-label").text() must be("supplementary.transportInfo.containerId")
    }

    "display 'Back' button that links to 'Transport Information' page" in {
      val backButton = createView().getElementById("link-back")

      backButton.text() must be("site.back")
      backButton.getElementById("link-back") must haveHref(
        controllers.declaration.routes.TransportDetailsController.displayPage(Mode.Normal)
      )
    }

    "display both 'Add' and 'Save and continue' button on page" in {
      val view = createView()

      val addButton = view.getElementById("add")
      addButton.text() must be("site.add")

      val saveButton = view.getElementById("submit")
      saveButton.text() must be("site.save_and_continue")
    }

    "display 'Save and return' button on page" in {
      val view = createView()

      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton.text() must be("site.save_and_come_back_later")
    }
  }

  "Transport Information Container View when filled" should {

    "display data in Container ID input" in {
      val view = createView(form = TransportInformationContainer.form().fill(TransportInformationContainer("Test")))

      view.getElementById("id").attr("value") must be("Test")
    }

    "display one row with data in table" in {
      val view = createView(Mode.Normal, form, Seq(TransportInformationContainer("Test")))

      // table header
      view.getElementById("removable_elements-heading") must containText("")

      // table row
      view.getElementById("removable_elements-row0-label") must containText("Test")
      view.getElementById("removable_elements-row0-remove_button") must containText("")
    }
  }
}
