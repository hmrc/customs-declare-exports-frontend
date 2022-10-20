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

import base.{Injector, MockAuthAction}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.form
import models.DeclarationType.STANDARD
import org.jsoup.nodes.Document
import play.api.data.Form
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.taric_code_remove
import views.tags.ViewTest

@ViewTest
class TaricCodeRemoveViewSpec extends PageWithButtonsSpec with Injector with MockAuthAction {

  val taricCode = "TARI"

  val page = instanceOf[taric_code_remove]

  override val typeAndViewInstance = (STANDARD, page(itemId, taricCode, form())(_, _))

  def createView(frm: Form[YesNoAnswer] = form(), code: String = taricCode, mode: Mode = Normal): Document =
    page(itemId, code, frm)(getJourneyRequest(), messages)

  "Taric Code Remove View" should {
    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.taricAdditionalCodes.remove.header")
    }

    "display Additional Code label" in {
      view.getElementsByClass("govuk-summary-list__key") must containMessageForElements("declaration.taricAdditionalCodes.table.header")
    }

    "display Taric code to remove" in {
      view.getElementsByClass("govuk-summary-list__value").text() mustBe taricCode
    }

    "display 'Back' button that links to 'taric codes summary' page" in {
      val backLinkContainer = view.getElementById("back-link")

      backLinkContainer must containMessage(backToPreviousQuestionCaption)
      backLinkContainer.getElementById("back-link") must haveHref(
        controllers.declaration.routes.TaricCodeSummaryController.displayPage(itemId)
      )
    }

    val createViewWithMode: Mode => Document = mode => createView(mode = mode)
    checkAllSaveButtonsAreDisplayed(createViewWithMode)
  }

  "Taric Code Remove View for invalid input" should {
    "display error if nothing is entered" in {
      val view = createView(form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#code_yes")

      view must containErrorElementWithMessageKey("error.yesNo.required")
    }
  }
}
