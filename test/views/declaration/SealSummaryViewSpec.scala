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

import base.Injector
import forms.common.YesNoAnswer
import forms.declaration.Seal
import helpers.views.declaration.CommonMessages
import models.Mode
import org.jsoup.nodes.Document
import org.scalatest.MustMatchers
import play.api.data.Form
import unit.tools.Stubs
import views.declaration.spec.{UnitViewSpec, UnitViewSpec2}
import views.html.declaration.seal_summary
import views.tags.ViewTest

@ViewTest
class SealSummaryViewSpec extends UnitViewSpec2 with Stubs with MustMatchers with CommonMessages with Injector {

  val containerId = "212374"
  val sealId = "76434574"
  val seal = Seal(sealId)
  private val form: Form[YesNoAnswer] = YesNoAnswer.form()
  private val page = instanceOf[seal_summary]

  private def createView(form: Form[YesNoAnswer] = form, seals: Seq[Seal] = Seq(seal)): Document =
    page(Mode.Normal, form, containerId, seals)(journeyRequest(), messages)

  "Seal Summary View" should {
    val view = createView()

    "display page title for no seals" in {
      val noSealsView = createView(seals = Seq.empty)
      val title = messages("declaration.seal.add.first", containerId)
      noSealsView.getElementsByTag("h1").text() must be(title)
      noSealsView.title() must include(title)
    }

    "display page title for one seal" in {
      val noSealsView = createView(seals = Seq(seal))
      val title = messages("declaration.seal.summary.title", containerId)
      noSealsView.getElementsByTag("h1").text() must be(title)
      noSealsView.title() must include(title)
    }

    "display page title for multiple seals" in {
      val noSealsView = createView(seals = Seq(seal, seal))
      val title = messages("declaration.seal.summary.multiple.title", 2, containerId)
      noSealsView.getElementsByTag("h1").text() must be(title)
      noSealsView.title() must include(title)
    }

    "display summary of seals" in {
      view.getElementById("removable_elements-row0-label").text() must be(sealId)
    }

    "display 'Back' button that links to 'containers summary' page" in {
      val backLinkContainer = view.getElementById("back-link")

      backLinkContainer must containMessage(backCaption)
      backLinkContainer.getElementById("back-link") must haveHref(
        controllers.declaration.routes.TransportContainerController.displayContainerSummary(Mode.Normal)
      )
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

  "Seal Summary View for invalid input" should {

    "display error if nothing is entered" in {
      val view = createView(YesNoAnswer.form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#yesNo")

      view must containErrorElementWithMessageKey("error.yesNo.required")
    }

  }
}
