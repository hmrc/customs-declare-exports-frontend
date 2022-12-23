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
import forms.common.YesNoAnswer.form
import forms.declaration.Seal
import models.DeclarationType.STANDARD
import org.jsoup.nodes.Document
import play.api.data.Form
import views.components.gds.Styles
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.seal_summary
import views.tags.ViewTest

@ViewTest
class SealSummaryViewSpec extends PageWithButtonsSpec with Injector {

  val containerId = "212374"
  val sealId = "76434574"
  val seal = Seal(sealId)

  val page = instanceOf[seal_summary]

  override val typeAndViewInstance = (STANDARD, page(form(), containerId, Seq(seal))(_, _))

  def createView(frm: Form[YesNoAnswer] = form(), seals: Seq[Seal] = Seq(seal)): Document =
    page(frm, containerId, seals)(journeyRequest(), messages)

  "Seal Summary View" should {
    val view = createView()

    "display page title for no seals" in {
      val noSealsView = createView(seals = Seq.empty)
      val title = messages("declaration.seal.add.first", containerId)
      noSealsView.getElementsByClass(Styles.gdsPageLegend).text() must be(title)
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

    "display section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.6")
    }

    "display table with headers" in {
      view.getElementsByTag("th").get(0).text() mustBe messages("declaration.seal.summary.heading")
    }

    "have visually hidden headers for Remove links" in {
      view.getElementsByTag("th").get(1).text() mustBe messages("site.remove.header")
    }

    "display summary of seals" in {
      view.getElementById("removable_elements-row0-label").text() must be(sealId)
    }

    "display 'Back' button that links to 'containers summary' page" in {
      val backLinkContainer = view.getElementById("back-link")

      backLinkContainer must containMessage(backToPreviousQuestionCaption)
      backLinkContainer.getElementById("back-link") must haveHref(controllers.declaration.routes.TransportContainerController.displayContainerSummary)
    }

    checkAllSaveButtonsAreDisplayed(createView())
  }

  "Seal Summary View for invalid input" should {
    "display error if nothing is entered" in {
      val view = createView(form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#code_yes")

      view must containErrorElementWithMessageKey("error.yesNo.required")
    }
  }
}
