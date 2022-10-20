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
import controllers.declaration.routes.TransportContainerController
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.form
import forms.declaration.Seal
import models.DeclarationType.STANDARD
import models.declaration.Container
import org.jsoup.nodes.Document
import play.api.data.Form
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.transport_container_remove
import views.tags.ViewTest

@ViewTest
class TransportContainerRemoveViewSpec extends PageWithButtonsSpec with Injector {

  val containerId = "434732435324"
  val sealId = "934545754"
  val container = Container(containerId, Seq(Seal(sealId)))

  val page = instanceOf[transport_container_remove]

  override val typeAndViewInstance = (STANDARD, page(form(), container)(_, _))

  def createView(frm: Form[YesNoAnswer] = form(), mode: Mode = Normal): Document = page(frm, container)

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

      backLinkContainer must containMessage(backToPreviousQuestionCaption)
      backLinkContainer.getElementById("back-link") must haveHref(TransportContainerController.displayContainerSummary())
    }

    val createViewWithMode: Mode => Document = mode => createView(mode = mode)
    checkAllSaveButtonsAreDisplayed(createViewWithMode)
  }

  "Seal Remove View for invalid input" should {
    "display error if nothing is entered" in {
      val view = createView(form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#code_yes")

      view must containErrorElementWithMessageKey("error.yesNo.required")
    }
  }
}
