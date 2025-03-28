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
import forms.section6.Seal
import forms.section6.Seal.form
import models.DeclarationType.STANDARD
import org.jsoup.nodes.Document
import play.api.data.Form
import views.html.section6.seal_add
import views.common.PageWithButtonsSpec
import views.tags.ViewTest

@ViewTest
class SealAddViewSpec extends PageWithButtonsSpec with Injector {

  val containerId = "867126538"

  val page = instanceOf[seal_add]

  override val typeAndViewInstance = (STANDARD, page(form, containerId)(_, _))

  def createView(frm: Form[Seal] = form): Document = page(frm, containerId)

  "Seal Add View" should {
    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1").text() must be(messages("declaration.seal.title", containerId))
    }

    "display section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.6")
    }

    "display 'Back' button that links to 'seals summary' page" in {
      val backLinkContainer = view.getElementById("back-link")

      backLinkContainer.text() must be(messages(backToPreviousQuestionCaption))
      backLinkContainer.getElementById("back-link") must haveHref(SealController.displaySealSummary(containerId))
    }

    checkAllSaveButtonsAreDisplayed(createView())
  }

  "Seal Add View for invalid input" should {

    "display error if nothing is entered" in {
      val view = createView(form.bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#id")

      view must containErrorElementWithMessageKey("error.required")
    }

    "display error if incorrect seal is entered" in {
      val view = createView(form.fillAndValidate(Seal(1, "Invalid!!!")))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#id")

      view must containErrorElementWithMessageKey("declaration.transport.sealId.error.invalid")
    }
  }
}
