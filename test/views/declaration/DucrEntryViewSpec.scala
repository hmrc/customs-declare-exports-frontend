/*
 * Copyright 2023 HM Revenue & Customs
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

import base.{ExportsTestData, Injector}
import controllers.declaration.routes.DucrChoiceController
import forms.Ducr
import forms.Ducr.form
import models.DeclarationType._
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.ducr_entry
import views.tags.ViewTest

@ViewTest
class DucrEntryViewSpec extends PageWithButtonsSpec with Injector {

  override val typeAndViewInstance = (STANDARD, page(Ducr.form)(_, _))

  val incorrectDUCR = "7GB000000000000-1234512345123451234512345"
  val page = instanceOf[ducr_entry]

  private def createView(form: Form[Ducr] = Ducr.form)(implicit request: JourneyRequest[_]): Document =
    page(form)(request, messages)

  "Ducr Entry" should {

    "have correct message keys" in {
      messages must haveTranslationFor("declaration.ducrEntry.header")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.info")
      messages must haveTranslationFor("declaration.ducrEntry.ducr.paragraph")
      messages must haveTranslationFor("declaration.ducrEntry.ducr.paragraph.bullet1")
      messages must haveTranslationFor("declaration.ducrEntry.ducr.paragraph.bullet2")
      messages must haveTranslationFor("declaration.ducrEntry.ducr.paragraph.bullet3")
      messages must haveTranslationFor("declaration.ducrEntry.ducr.paragraph.bullet4")
      messages must haveTranslationFor("declaration.ducrEntry.ducr.paragraph.bullet5")
      messages must haveTranslationFor("declaration.ducrEntry.ducr.hint")
      messages must haveTranslationFor("declaration.ducrEntry.ducr.inset.1")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.error.empty")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.error.invalid")
    }

    "display page title" in {
      createView().getElementById("title").text() mustBe messages("declaration.ducrEntry.header")
    }

    "display section header" in {
      createView().getElementById("section-header").text() must include(messages("declaration.section.1"))
    }

    "not display 'Exit and return' button" in {
      createView().getElementsContainingText("site.exit_and_complete_later") mustBe empty
    }

    "display data in DUCR input" in {
      val frm = form.fill(Ducr(ExportsTestData.ducr))
      val view = createView(frm)
      view.getElementById("ducr").attr("value") mustBe ExportsTestData.ducr
    }

    "display inset text for DUCR" in {
      createView().getElementsByClass("govuk-inset-text").get(0).text mustBe messages("declaration.ducrEntry.ducr.inset.1")
    }

    "display error when DUCR is incorrect" in {

      val frm = form.fillAndValidate(Ducr(incorrectDUCR))
      val view = createView(frm)

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#ducr")
      view must containErrorElementWithMessageKey("declaration.consignmentReferences.ducr.error.invalid")

    }

    checkSaveAndContinueButtonIsDisplayed(createView())

    "display empty input with label for DUCR" in {

      val expectedBodyTextListMessageKeys = Seq(
        "declaration.ducrEntry.ducr.paragraph.bullet1",
        "declaration.ducrEntry.ducr.paragraph.bullet2",
        "declaration.ducrEntry.ducr.paragraph.bullet3",
        "declaration.ducrEntry.ducr.paragraph.bullet4",
        "declaration.ducrEntry.ducr.paragraph.bullet5"
      )

      createView().getElementsByClass("govuk-label").get(0).text mustBe messages("declaration.ducrEntry.ducr.paragraph")

      expectedBodyTextListMessageKeys.foreach { messageKey =>
        createView().getElementsByClass("govuk-list").get(0) must containMessage(messageKey)
      }

      createView().getElementById("ducr-hint").text mustBe messages("declaration.ducrEntry.ducr.hint")
      createView().getElementById("ducr").attr("value") mustBe empty
    }

    "not display empty input with label for MRN" in {
      createView().getElementsByAttributeValue("for", "mrn") mustBe empty
    }

  }

  "Ducr Entry View" should {
    onJourney(STANDARD, CLEARANCE, OCCASIONAL, SIMPLIFIED) { implicit request =>
      "display 'Back' button that links to 'Ducr Choice' page" in {
        val backButton = createView().getElementById("back-link")
        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(DucrChoiceController.displayPage.url)
      }
    }
  }
}
