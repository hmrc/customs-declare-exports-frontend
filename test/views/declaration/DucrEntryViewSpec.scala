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

import base.{ExportsTestData, Injector}
import controllers.declaration.routes
import controllers.declaration.routes.DeclarantDetailsController
import forms.Ducr
import forms.Ducr.form
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.{SUPPLEMENTARY_EIDR, SUPPLEMENTARY_SIMPLIFIED}
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
      messages must haveTranslationFor("declaration.consignmentReferences.header")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.info")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.paragraph")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.paragraph.bullet1")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.paragraph.bullet2")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.paragraph.bullet3")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.paragraph.bullet4")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.paragraph.bullet5")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.hint")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.inset.1")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.error.empty")
      messages must haveTranslationFor("declaration.consignmentReferences.ducr.error.invalid")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.ducr.hint1")
      messages must haveTranslationFor("declaration.consignmentReferences.lrn.info")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.lrn.info")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.lrn.hint")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.mrn.info")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.mrn.hint1")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.mrn.hint2")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.mrn.error.empty")
      messages must haveTranslationFor("declaration.consignmentReferences.supplementary.mrn.error.invalid")
    }
  }

  "Ducr Entry View" should {
    onEveryDeclarationJourney() { implicit request =>
      "display page title" in {
        createView().getElementById("title").text() mustBe messages("declaration.consignmentReferences.header")
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
        view.getElementById("ducr_ducr").attr("value") mustBe ExportsTestData.ducr
      }

      "display error when DUCR is incorrect" in {

        val frm = form.fillAndValidate(Ducr(incorrectDUCR))
        val view = createView(frm)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#ducr_ducr")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.ducr.error.invalid")

      }

      checkSaveAndContinueButtonIsDisplayed(createView())
    }
  }

  "Ducr Entry View" should {
    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
      val view = createView()

      "display empty input with label for DUCR" in {

        val expectedBodyTextListMessageKeys = Seq(
          "declaration.consignmentReferences.ducr.paragraph.bullet1",
          "declaration.consignmentReferences.ducr.paragraph.bullet2",
          "declaration.consignmentReferences.ducr.paragraph.bullet3",
          "declaration.consignmentReferences.ducr.paragraph.bullet4",
          "declaration.consignmentReferences.ducr.paragraph.bullet5"
        )

        view.getElementsByClass("govuk-body").get(0).text mustBe messages("declaration.consignmentReferences.ducr.paragraph")

        expectedBodyTextListMessageKeys.foreach { messageKey =>
          view.getElementsByClass("govuk-list").get(0) must containMessage(messageKey)
        }

        view.getElementById("ducr_ducr-hint").text mustBe messages("declaration.consignmentReferences.ducr.hint")
        view.getElementById("ducr_ducr").attr("value") mustBe empty
      }

      "not display empty input with label for MRN" in {
        view.getElementsByAttributeValue("for", "mrn") mustBe empty
      }

      "display inset text for DUCR" in {
        val expectedInsetText =
          Seq(messages("declaration.consignmentReferences.ducr.inset.1"), messages("declaration.consignmentReferences.ducr.inset.2")).mkString(" ")

        view.getElementsByClass("govuk-inset-text").get(0).text mustBe expectedInsetText
      }

      "display 'Back' button that links to 'Declarant Details' page" in {
        val backButton = createView().getElementById("back-link")
        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(DeclarantDetailsController.displayPage().url)
      }

    }
  }

  "Ducr Entry Vieww" when {

    "AdditionalDeclarationType is SUPPLEMENTARY_SIMPLIFIED" should {
      val view = createView()(withRequest(SUPPLEMENTARY_SIMPLIFIED))

      "display empty input with label for DUCR" in {
        val expectedHintText =
          Seq(messages("declaration.consignmentReferences.supplementary.ducr.hint1"), messages("declaration.consignmentReferences.ducr.hint"))
            .mkString(" ")

        view.getElementsByAttributeValue("for", "ducr_ducr").text() mustBe messages("declaration.consignmentReferences.ducr.info")
        view.getElementById("ducr_ducr-hint").text() mustBe expectedHintText
        view.getElementById("ducr_ducr").attr("value") mustBe empty
      }

      "do not display inset text for DUCR or LRN" in {
        view.getElementsByClass("govuk-inset-text").size() mustBe 0
      }
    }

    "AdditionalDeclarationType is SUPPLEMENTARY_EIDR" should {
      val view = createView()(withRequest(SUPPLEMENTARY_EIDR))

      "display empty input with label for DUCR" in {
        val expectedHintText =
          Seq(messages("declaration.consignmentReferences.supplementary.ducr.hint1"), messages("declaration.consignmentReferences.ducr.hint"))
            .mkString(" ")

        view.getElementsByAttributeValue("for", "ducr_ducr").text() mustBe messages("declaration.consignmentReferences.ducr.info")
        view.getElementById("ducr_ducr-hint").text() mustBe expectedHintText
        view.getElementById("ducr_ducr").attr("value") mustBe empty
      }

      "do not display inset text for DUCR or LRN" in {
        view.getElementsByClass("govuk-inset-text").size() mustBe 0
      }
    }
  }

  "Ducr Entry View" should {

    onClearance { implicit request =>
      "display 'Back' button that links to 'Declaration Type' page" in {
        val backButton = createView().getElementById("back-link")
        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(routes.AdditionalDeclarationTypeController.displayPage().url)
      }
    }

  }

}
