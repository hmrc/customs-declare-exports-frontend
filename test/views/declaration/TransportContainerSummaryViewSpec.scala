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
import forms.common.YesNoAnswer.form
import forms.declaration.Seal
import models.DeclarationType.{STANDARD, SUPPLEMENTARY}
import models.declaration.Container
import org.jsoup.nodes.Document
import play.api.data.Form
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.transport_container_summary
import views.tags.ViewTest

@ViewTest
class TransportContainerSummaryViewSpec extends PageWithButtonsSpec with Injector {

  val containerId = "212374"
  val sealId = "76434574"
  val container = Container(containerId, List(Seal(sealId)))

  val page = instanceOf[transport_container_summary]

  override val typeAndViewInstance = (STANDARD, page(form(), List(container))(_, _))

  def createView(frm: Form[YesNoAnswer] = form(), containers: Seq[Container] = List(container)): Document =
    page(frm, containers)(journeyRequest(), messages)

  "Transport Containers Summary View" should {
    val view = createView()

    "display page title for one container" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.transportInformation.containers.title")
      view.title() must include(messages("declaration.transportInformation.containers.title"))
    }

    "display page title for multiple containers" in {
      val multiContainerView = createView(containers = List(container, container))
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
      val view = createView(containers = List(Container(containerId, Seq.empty)))

      view.getElementById("containers-row0-container").text() must be(containerId)
      view.getElementById("containers-row0-seals") must containMessage("declaration.seal.summary.noSeals")
    }

    "display 'Back' button that links to the 'Express Consignment' page" when {
      "declaration's type is STANDARD" in {
        val backLinkContainer = view.getElementById("back-link")
        backLinkContainer must containMessage(backToPreviousQuestionCaption)
        backLinkContainer must haveHref(routes.ExpressConsignmentController.displayPage)
      }
    }

    "display 'Back' button that links to the 'Transport Country' page" when {
      "declaration's type is SUPPLEMENTARY" in {
        val view = page(form(), List(container))(journeyRequest(SUPPLEMENTARY), messages)
        val backLinkContainer = view.getElementById("back-link")
        backLinkContainer must containMessage(backToPreviousQuestionCaption)
        backLinkContainer must haveHref(routes.TransportCountryController.displayPage)
      }
    }

    checkAllSaveButtonsAreDisplayed(createView())
  }

  "Transport Containers Summary View for invalid input" should {
    "display error if nothing is entered" in {
      val view = createView(form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#code_yes")

      view must containErrorElementWithMessageKey("error.yesNo.required")
    }
  }
}
