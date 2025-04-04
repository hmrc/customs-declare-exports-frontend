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
import controllers.section6.routes.SealController
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.form
import models.DeclarationType.STANDARD
import org.jsoup.nodes.Document
import play.api.data.Form
import views.html.section6.seal_remove
import views.common.PageWithButtonsSpec
import views.tags.ViewTest

@ViewTest
class SealRemoveViewSpec extends PageWithButtonsSpec with Injector {

  val containerId = "42354542"
  val sealId = "SealToRemove54214"

  val page = instanceOf[seal_remove]

  override val typeAndViewInstance = (STANDARD, page(form(), containerId, sealId)(_, _))

  def createView(frm: Form[YesNoAnswer] = form()): Document = page(frm, containerId, sealId)

  "Seal Remove View" should {
    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1").text() must be(messages("declaration.seal.remove.title", containerId))
    }

    "display section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.6")
    }

    "display Seal label" in {
      view.getElementsByClass(summaryKeyClassName) must containMessageForElements("declaration.seal.summary.heading")
    }

    "display seal to remove" in {
      view.getElementsByClass(summaryValueClassName).text() must include(sealId)
    }

    "display 'Back' button that links to 'seal summary' page" in {
      val backLinkContainer = view.getElementById("back-link")

      backLinkContainer.text() must be(messages(backToPreviousQuestionCaption))
      backLinkContainer.getElementById("back-link") must haveHref(SealController.displaySealSummary(containerId))
    }

    checkAllSaveButtonsAreDisplayed(createView())
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
