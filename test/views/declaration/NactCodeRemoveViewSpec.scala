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
import models.DeclarationType.STANDARD
import models.Mode
import models.Mode.Normal
import org.jsoup.nodes.Document
import play.api.data.Form
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.nact_code_remove
import views.tags.ViewTest

@ViewTest
class NactCodeRemoveViewSpec extends PageWithButtonsSpec with Injector {

  val nactCode = "VATR"

  val page = instanceOf[nact_code_remove]

  override val typeAndViewInstance = (STANDARD, page(Normal, itemId, nactCode, form())(_, _))

  def createView(frm: Form[YesNoAnswer] = form(), code: String = nactCode, mode: Mode = Normal): Document =
    page(mode, itemId, code, frm)(request, messages)

  "Nact Code Remove View" should {
    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.nationalAdditionalCode.remove.header")
    }

    "display National Additional Code label" in {
      val elements = view.getElementsByClass("govuk-summary-list__key")
      elements must containMessageForElements("declaration.nationalAdditionalCode.table.header")
    }

    "display Nact code to remove" in {
      view.getElementsByClass("govuk-summary-list__value").text() mustBe nactCode
    }

    "display 'Back' button that links to 'nact codes summary' page" in {
      val backLinkContainer = view.getElementById("back-link")

      backLinkContainer must containMessage(backToPreviousQuestionCaption)
      backLinkContainer.getElementById("back-link") must haveHref(
        controllers.declaration.routes.NactCodeSummaryController.displayPage(Normal, itemId)
      )
    }

    val createViewWithMode: Mode => Document = mode => createView(mode = mode)
    checkAllSaveButtonsAreDisplayed(createViewWithMode)
  }

  "Nact Code Remove View for invalid input" should {
    "display error if nothing is entered" in {
      val view = createView(form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#code_yes")

      view must containErrorElementWithMessageKey("error.yesNo.required")
    }
  }
}
