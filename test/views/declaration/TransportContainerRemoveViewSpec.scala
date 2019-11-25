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

import forms.common.YesNoAnswer
import forms.declaration.Seal
import helpers.views.declaration.CommonMessages
import models.Mode
import models.declaration.Container
import org.jsoup.nodes.Document
import org.scalatest.MustMatchers
import play.api.data.Form
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.transport_container_remove
import views.tags.ViewTest

@ViewTest
class TransportContainerRemoveViewSpec extends UnitViewSpec with Stubs with MustMatchers with CommonMessages {

  val containerId = "434732435324"
  val sealId = "934545754"
  val container = Container(containerId, Seq(Seal(sealId)))
  private val realMessages = validatedMessages
  private val form: Form[YesNoAnswer] = YesNoAnswer.form()
  private val page = new transport_container_remove(mainTemplate)

  private def createView(form: Form[YesNoAnswer] = form, container: Container = container): Document =
    page(Mode.Normal, form, container)(request, realMessages)

  "Transport Containers Remove View" should {
    val view = createView()

    "display page title" in {
      view.getElementById("title").text() must be(realMessages("declaration.transportInformation.container.remove.title"))
    }

    "display container and seal to remove" in {
      view.getElementById("container-table").text() must include(containerId)
      view.getElementById("container-table").text() must include(sealId)
    }

    "display 'Back' button that links to 'container summary' page" in {
      val backLinkContainer = view.getElementById("link-back")

      backLinkContainer must containText(realMessages(backCaption))
      backLinkContainer.getElementById("link-back") must haveHref(
        controllers.declaration.routes.TransportContainerController.displayContainerSummary(Mode.Normal)
      )
    }

    "display 'Save and continue' button on page" in {
      val saveButton = view.getElementById("submit")
      saveButton must containText(realMessages(saveAndContinueCaption))
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton must containText(realMessages(saveAndReturnCaption))
    }
  }

  "Seal Remove View for invalid input" should {

    "display error if nothing is entered" in {
      val view = createView(YesNoAnswer.form().bind(Map[String, String]()))

      view.select("#error-message-yesNo-input").text() must be(realMessages("error.yesNo.required"))
    }

  }
}
