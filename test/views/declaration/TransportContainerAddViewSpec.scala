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

import forms.declaration.ContainerAdd
import helpers.views.declaration.CommonMessages
import models.Mode
import org.jsoup.nodes.Document
import org.scalatest.MustMatchers
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.transport_container_add
import views.tags.ViewTest

@ViewTest
class TransportContainerAddViewSpec extends UnitViewSpec with ExportsTestData with Stubs with MustMatchers with CommonMessages {

  private val form: Form[ContainerAdd] = ContainerAdd.form()
  private val page = new transport_container_add(mainTemplate)
  private val realMessages = validatedMessages

  private def createView(form: Form[ContainerAdd] = form): Document =
    page(Mode.Normal, form)(journeyRequest(), realMessages)

  "Transport Containers Add View" should {
    val view = createView()

    "display page title" in {
      view.getElementById("title").text() must be(realMessages("declaration.transportInformation.containers.title"))
    }

    "display 'Back' button that links to 'containers summary' page" in {
      val backLinkContainer = view.getElementById("link-back")

      backLinkContainer.getElementById("link-back") must haveHref(
        controllers.declaration.routes.TransportContainerController.displayContainerSummary(Mode.Normal)
      )
    }

    "display 'Save and continue' button on page" in {
      val saveButton = view.getElementById("submit")
      saveButton.text() must be(realMessages(saveAndContinueCaption))
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton.text() must be(realMessages(saveAndReturnCaption))
    }
  }

  "Transport Containers Add View for invalid input" should {

    "display error if nothing is entered" in {
      val view = createView(ContainerAdd.form().bind(Map[String, String]()))

      view.select("#error-message-id-input").text() must be(realMessages("declaration.transportInformation.containerId.empty"))
    }

    "display error if incorrect containerId is entered" in {
      val view = createView(ContainerAdd.form().fillAndValidate(ContainerAdd(Some("12345678901234567890"))))

      view.select("#error-message-id-input").text() must be(realMessages("declaration.transportInformation.containerId.error.length"))
    }

  }

  "Transport Containers Add View when filled" should {

    "display data in Container ID input" in {

      val view = createView(ContainerAdd.form().fill(ContainerAdd(Some("Test"))))

      view.getElementById("id").attr("value") must be("Test")
    }
  }
}
