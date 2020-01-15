/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.common.YesNoAnswer
import helpers.views.declaration.CommonMessages
import models.Mode
import models.declaration.Container
import org.jsoup.nodes.Document
import org.scalatest.MustMatchers
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.transport_container_summary_clearance
import views.tags.ViewTest

@ViewTest
class TransportContainerSummaryClearanceViewSpec extends UnitViewSpec with ExportsTestData with Stubs with MustMatchers with CommonMessages {

  private val containerId = "212374"
  private val container = Container(containerId, Seq.empty)
  private val realMessages = validatedMessages
  private val form: Form[YesNoAnswer] = YesNoAnswer.form()
  private val page = new transport_container_summary_clearance(mainTemplate)

  private def createView(form: Form[YesNoAnswer] = form, containers: Seq[Container] = Seq(container)): Document =
    page(Mode.Normal, form, containers)(journeyRequest(), realMessages)

  "Transport Containers Summary View" should {
    val view = createView()

    "display page title" in {
      view.getElementById("title").text() must be(realMessages("declaration.transportInformation.containers.title"))
    }

    "display 'Back' button that links to 'transport payment' page" in {
      val backLinkContainer = view.getElementById("back-link")

      backLinkContainer.text() must be(realMessages(backCaption))
      backLinkContainer.getElementById("back-link") must haveHref(controllers.declaration.routes.TransportPaymentController.displayPage(Mode.Normal))
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

  "Transport Containers Summary View for invalid input" should {

    "display error if nothing is entered" in {
      val view = createView(YesNoAnswer.form().bind(Map[String, String]()))

      view.select("#error-message-yesNo-input").text() must be(realMessages("error.yesNo.required"))
    }
  }
}
