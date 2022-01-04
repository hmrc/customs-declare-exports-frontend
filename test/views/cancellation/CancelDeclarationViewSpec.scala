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

package views.cancellation

import base.Injector
import base.TestHelper.createRandomAlphanumericString
import controllers.routes
import forms.Choice.AllowedChoiceValues.CancelDec
import forms.cancellation.CancellationChangeReason.NoLongerRequired
import forms.{CancelDeclaration, Choice, Lrn}
import forms.CancelDeclaration.mrnKey
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.cancel_declaration
import views.tags.ViewTest

@ViewTest
class CancelDeclarationViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  private val form: Form[CancelDeclaration] = CancelDeclaration.form
  private val cancelDeclarationPage = instanceOf[cancel_declaration]

  "Cancel DeclarationView on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() mustBe messages("cancellation.title")
    }

    "display empty input with label for 'Functional Reference ID'" in {

      val view = createView()

      view.getElementsByAttributeValue("for", "functionalReferenceId").text() mustBe messages("cancellation.functionalReferenceId")
      view.getElementById("functionalReferenceId").attr("value") mustBe empty
    }

    "display empty input with label for 'mrn'" in {

      val view = createView()

      view.getElementsByAttributeValue("for", "mrn").text() mustBe messages("cancellation.mrn")
      view.getElementById("mrn").attr("value") mustBe empty
    }

    "display empty input with label for 'statement Description'" in {

      val view = createView()

      view.getElementsByAttributeValue("for", "statementDescription").text() mustBe messages("cancellation.statementDescription")
      view.getElementById("statementDescription").attr("value") mustBe empty
    }

    "display three radio buttons with description (not selected)" in {

      val view = createView(CancelDeclaration.form.fill(CancelDeclaration(Lrn(""), "", "", "")))

      val noLongerRequired = view.getElementById("noLongerRequired")
      noLongerRequired.attr("checked") mustBe empty
      val noLongerRequiredLabel = view.getElementsByAttributeValue("for", "noLongerRequired")
      noLongerRequiredLabel.text() mustBe messages("cancellation.reason.noLongerRequired")

      val otherReason = view.getElementById("otherReason")
      otherReason.attr("checked") mustBe empty
      val otherReasonLabel = view.getElementsByAttributeValue("for", "otherReason")
      otherReasonLabel.text() mustBe messages("cancellation.reason.otherReason")

      val duplication = view.getElementById("duplication")
      duplication.attr("checked") mustBe empty
      val duplicationLabel = view.getElementsByAttributeValue("for", "duplication")
      duplicationLabel.text() mustBe messages("cancellation.reason.duplication")
    }

    "display 'Submit' button on page" in {

      val view = createView()

      val saveButton = view.select("#submit")
      saveButton.text() mustBe messages("cancellation.submitButton")
    }

    "display 'Back' button that links to 'Choice' page with Cancel declaration selected" in {
      val backButton = createView().getElementById("back-link")

      backButton must containMessage("site.back")
      backButton must haveHref(routes.ChoiceController.displayPage(Some(Choice(CancelDec))))
    }
  }

  "Cancellation View for invalid input" should {
    val view = createView(CancelDeclaration.form.bind(Map[String, String]()))

    "display referenceId error" when {
      "field is empty" in {
        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#functionalReferenceId")
        view must containErrorElementWithMessageKey("error.required")
      }

      "field is entered but is too long" in {
        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(
              CancelDeclaration(
                Lrn("1SA1234567890121FSA1234567IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII"),
                "123456789",
                "Some Description",
                NoLongerRequired.toString
              )
            )
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#functionalReferenceId")

        view must containErrorElementWithMessageKey("cancellation.functionalReferenceId.error.length")
      }

      "field is entered but is in the wrong format" in {
        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(CancelDeclaration(Lrn("12345566++"), "123456789", "Some Description", NoLongerRequired.toString))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#functionalReferenceId")

        view must containErrorElementWithMessageKey("cancellation.functionalReferenceId.error.specialCharacter")
      }
    }

    "display mrn error" when {
      "field is empty" in {
        testView("", "Some Description", "mrn", "empty")
      }

      "field is entered but is too long" in {
        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(
              CancelDeclaration(Lrn("1SA123456789012-1FSA1234567"), createRandomAlphanumericString(19), "Some Description", NoLongerRequired.toString)
            )
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#mrn")

        view must containErrorElementWithMessageKey("cancellation.mrn.error.length")
      }

      "field is entered but is in the wrong format" in {
        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(CancelDeclaration(Lrn("1SA123456789012-1FSA1234567"), "1234567890123-", "Some Description", NoLongerRequired.toString))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#mrn")

        view must containErrorElementWithMessageKey("cancellation.mrn.error.wrongFormat")
      }

      "field value is not found for the current user" in {
        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(
              CancelDeclaration(Lrn("1SA123456789012-1FSA1234567"), "20GB46J8TMJ4RFGVA0", "Some Description", NoLongerRequired.toString)
            )
            .copy(errors = Seq(FormError.apply(mrnKey, "cancellation.mrn.error.denied")))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#mrn")

        view must containErrorElementWithMessageKey("cancellation.mrn.error.denied")
      }

      "field value has already been submitted in a previous cancellation request" in {
        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(
              CancelDeclaration(Lrn("1SA123456789012-1FSA1234567"), "20GB46J8TMJ4RFGVA0", "Some Description", NoLongerRequired.toString)
            )
            .copy(errors = Seq(FormError.apply(mrnKey, "cancellation.duplicateRequest.error")))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#mrn")

        view must containErrorElementWithMessageKey("cancellation.duplicateRequest.error")
      }
    }

    "display statementDescription error" when {

      "field is empty " in {
        testView("123456789012345678", "", "statementDescription", "empty")
      }

      "field is entered but is too long" in {
        val longDesc = createRandomAlphanumericString(600)
        testView("123456789012345678", longDesc, "statementDescription", "length")
      }

      "field is entered but is in the wrong format" in {
        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(
              CancelDeclaration(Lrn("1SA123456789012-1FSA1234567"), "123456789012345678", "Some Description$$$$", NoLongerRequired.toString)
            )
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#statementDescription")

        view must containErrorElementWithMessageKey("cancellation.statementDescription.error.invalid")
      }
    }

    "display change reason error" when {
      "field is empty" in {
        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#noLongerRequired")
        view must containErrorElementWithMessageKey("cancellation.changeReason.error.wrongValue")
      }

      "field is entered but the value is unknown" in {
        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(CancelDeclaration(Lrn("1SA123456789012-1FSA1234567"), "1234567890123", "Some Description", "wrong value"))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#noLongerRequired")

        view must containErrorElementWithMessageKey("cancellation.changeReason.error.wrongValue")
      }
    }
  }

  def testView(mrn: String, description: String, key: String, errorType: String): Unit = {
    val view = createView(
      CancelDeclaration.form
        .fillAndValidate(CancelDeclaration(Lrn("1SA123456789012-1FSA1234567"), mrn, description, NoLongerRequired.toString))
    )

    view must haveGovukGlobalErrorSummary
    view must containErrorElementWithTagAndHref("a", s"#$key")

    view must containErrorElementWithMessage(messages(s"cancellation.$key.error.$errorType"))
  }

  private def createView(form: Form[CancelDeclaration] = form): Document =
    cancelDeclarationPage(form)(request, messages)
}
