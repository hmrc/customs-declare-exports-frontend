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
import controllers.declaration.routes
import forms.common.YesNoAnswer
import forms.declaration.Seal
import models.Mode
import models.declaration.Container
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.transport_container_summary
import views.tags.ViewTest

@ViewTest
class TransportContainerSummaryViewSpec extends UnitViewSpec with ExportsTestData with Stubs with CommonMessages with Injector {

  val containerId = "212374"
  val sealId = "76434574"
  val container = Container(containerId, Seq(Seal(sealId)))
  private val form: Form[YesNoAnswer] = YesNoAnswer.form()
  private val page = instanceOf[transport_container_summary]

  private def createView(form: Form[YesNoAnswer] = form, containers: Seq[Container] = Seq(container)): Document =
    page(Mode.Normal, form, containers)(journeyRequest(), messages)

  "Transport Containers Summary View" should {
    val view = createView()

    "display page title for one container" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.transportInformation.containers.title")
      view.title() must include(messages("declaration.transportInformation.containers.title"))
    }

    "display page title for multiple containers" in {
      val multiContainerView = createView(containers = Seq(container, container))
      multiContainerView.getElementsByTag("h1") must containMessageForElements("declaration.transportInformation.containers.multiple.title", 2)
      multiContainerView.title() must include(messages("declaration.transportInformation.containers.multiple.title", 2))
    }

    "display section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.6")
    }

    "display table with headers" in {
      val tableHead = view.getElementsByTag("th")

      tableHead.get(0).text() mustBe messages("declaration.transportInformation.containerId.title")
      tableHead.get(1).text() mustBe messages("declaration.seal.summary.heading")
    }

    "have visually hidden headers for Change and Remove links" in {
      val tableHead = view.getElementsByTag("th")

      tableHead.get(2).text() mustBe messages("site.change.header")
      tableHead.get(3).text() mustBe messages("site.remove.header")
    }

    "display summary of container with seals" in {
      view.getElementById("containers-row0-container").text() must be(containerId)
      view.getElementById("containers-row0-seals").text() must be(sealId)
    }

    "display summary of container with no seals" in {
      val view = createView(containers = Seq(Container(containerId, Seq.empty)))

      view.getElementById("containers-row0-container").text() must be(containerId)
      view.getElementById("containers-row0-seals") must containMessage("declaration.seal.summary.noSeals")
    }

    "display 'Back' button that links to 'Express Consignment' page" in {
      val backLinkContainer = view.getElementById("back-link")

      backLinkContainer must containMessage(backCaption)
      backLinkContainer.getElementById("back-link") must haveHref(routes.ExpressConsignmentController.displayPage(Mode.Normal))
    }

    "display 'Save and continue' button on page" in {
      val saveButton = view.getElementById("submit")
      saveButton must containMessage(saveAndContinueCaption)
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton must containMessage(saveAndReturnCaption)
    }
  }

  "Transport Containers Summary View for invalid input" should {

    "display error if nothing is entered" in {
      val view = createView(YesNoAnswer.form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#code_yes")

      view must containErrorElementWithMessageKey("error.yesNo.required")
    }

  }
}
