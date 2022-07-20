/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.declaration.ContainerAdd
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestHelper
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.transport_container_add
import views.tags.ViewTest

@ViewTest
class TransportContainerAddViewSpec extends UnitViewSpec with ExportsTestHelper with Stubs with CommonMessages with Injector {

  private val form: Form[ContainerAdd] = ContainerAdd.form()
  private val page = instanceOf[transport_container_add]

  private def createView(form: Form[ContainerAdd] = form, mode: Mode = Mode.Normal): Document =
    page(mode, form)(journeyRequest(), messages)

  "Transport Containers Add View" should {
    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.transportInformation.containers.add.title")
    }

    "display section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.6")
    }

    "display 'Back' button that links to 'containers summary' page" in {
      val backLinkContainer = view.getElementById("back-link")

      backLinkContainer.getElementById("back-link") must haveHref(
        controllers.declaration.routes.TransportContainerController.displayContainerSummary(Mode.Normal)
      )
    }

    val createViewWithMode: Mode => Document = mode => createView(mode = mode)
    checkAllSaveButtonsAreDisplayed(createViewWithMode)
  }

  "Transport Containers Add View for invalid input" should {

    "display error if nothing is entered" in {
      val view = createView(ContainerAdd.form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#id")

      view must containErrorElementWithMessageKey("declaration.transportInformation.containerId.empty")
    }

    "display error if incorrect containerId is entered" in {
      val view = createView(ContainerAdd.form().fillAndValidate(ContainerAdd(Some("abc123@#"))))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#id")

      view must containErrorElementWithMessageKey("declaration.transportInformation.containerId.error.invalid")
    }

    "display error if containerId is too long" in {
      val view = createView(ContainerAdd.form().fillAndValidate(ContainerAdd(Some("12345678901234567890"))))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#id")

      view must containErrorElementWithMessageKey("declaration.transportInformation.containerId.error.length")
    }
  }

  "Transport Containers Add View when filled" should {

    "display data in Container ID input" in {

      val view = createView(ContainerAdd.form().fill(ContainerAdd(Some("Test"))))

      view.getElementById("id").attr("value") must be("Test")
    }
  }
}
