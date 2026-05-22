/*
 * Copyright 2024 HM Revenue & Customs
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

package views.section6

import base.Injector
import controllers.section6.routes.ContainerController
import forms.section6.ContainerAdd
import forms.section6.ContainerAdd.form
import models.DeclarationType.STANDARD
import org.jsoup.nodes.Document
import play.api.data.Form
import views.html.section6.container_add
import views.common.PageWithButtonsSpec
import views.tags.ViewTest

@ViewTest
class ContainerAddViewSpec extends PageWithButtonsSpec with Injector {

  val page = instanceOf[container_add]

  override val typeAndViewInstance = (STANDARD, page(form)(_, _))

  def createView(frm: Form[ContainerAdd] = form): Document = page(frm)(journeyRequest(), messages)

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
      backLinkContainer.getElementById("back-link") must haveHref(ContainerController.displayContainerSummary())
    }

    checkAllSaveButtonsAreDisplayed(createView())
  }

  "Transport Containers Add View for invalid input" should {

    "display error if nothing is entered" in {
      val view = createView(form.bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#id")

      view must containErrorElementWithMessageKey("declaration.transportInformation.containerId.empty")
    }

    "display error if incorrect containerId is entered" in {
      val view = createView(form.fillAndValidate(ContainerAdd(Some("abc123@#"))))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#id")

      view must containErrorElementWithMessageKey("declaration.transportInformation.containerId.error.invalid")
    }

    "display error if containerId is too long" in {
      val view = createView(form.fillAndValidate(ContainerAdd(Some("12345678901234567890"))))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#id")

      view must containErrorElementWithMessageKey("declaration.transportInformation.containerId.error.length")
    }
  }

  "Transport Containers Add View when filled" should {
    "display data in Container ID input" in {
      val view = createView(form.fill(ContainerAdd(Some("Test"))))

      view.getElementById("id").attr("value") must be("Test")
    }
  }
}
