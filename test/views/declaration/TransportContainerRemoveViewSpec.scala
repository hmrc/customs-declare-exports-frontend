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
import forms.common.YesNoAnswer
import forms.declaration.Seal
import models.Mode
import models.declaration.Container
import org.jsoup.nodes.Document
import play.api.data.Form
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.transport_container_remove
import views.tags.ViewTest

@ViewTest
class TransportContainerRemoveViewSpec extends UnitViewSpec with Stubs with CommonMessages with Injector {

  val containerId = "434732435324"
  val sealId = "934545754"
  val container = Container(containerId, Seq(Seal(sealId)))
  private val form: Form[YesNoAnswer] = YesNoAnswer.form()
  private val page = instanceOf[transport_container_remove]

  private def createView(form: Form[YesNoAnswer] = form, container: Container = container, mode: Mode = Mode.Normal): Document =
    page(mode, form, container)(request, messages)

  "Transport Containers Remove View" should {
    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.transportInformation.container.remove.title")
    }

    "display section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.6")
    }

    "display container and seal labels" in {
      view.getElementsByClass("govuk-summary-list__key").get(0) must containMessage("declaration.transportInformation.containerId.title")
      view.getElementsByClass("govuk-summary-list__key").get(1) must containMessage("declaration.seal.summary.heading")
    }

    "display container and seal to remove" in {
      view.getElementsByClass("govuk-summary-list__value").get(0).text() must include(containerId)
      view.getElementsByClass("govuk-summary-list__value").get(1).text() must include(sealId)
    }

    "display 'Back' button that links to 'container summary' page" in {
      val backLinkContainer = view.getElementById("back-link")

      backLinkContainer must containMessage(backCaption)
      backLinkContainer.getElementById("back-link") must haveHref(
        controllers.declaration.routes.TransportContainerController.displayContainerSummary(Mode.Normal)
      )
    }

    val createViewWithMode: Mode => Document = mode => createView(mode = mode)
    checkAllSaveButtonsAreDisplayed(createViewWithMode)
  }

  "Seal Remove View for invalid input" should {

    "display error if nothing is entered" in {
      val view = createView(YesNoAnswer.form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#code_yes")

      view must containErrorElementWithMessageKey("error.yesNo.required")
    }

  }
}
