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

package views.cancellation

import base.TestHelper.createRandomAlphanumericString
import forms.CancelDeclaration
import forms.cancellation.CancellationChangeReason.NoLongerRequired
import helpers.views.declaration.CommonMessages
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.cancel_declaration
import views.tags.ViewTest

@ViewTest
class CancelDeclarationViewSpec extends ViewSpec with CommonMessages {

  private val form: Form[CancelDeclaration] = CancelDeclaration.form
  private def createView(form: Form[CancelDeclaration] = form): Html =
    cancel_declaration(appConfig, form)(fakeRequest, messages)

  "Cancel Declaration View" should {

    "have proper messages for labels" in {
      assertMessage(messages("cancellation.title"), "Cancel Declaration")
      assertMessage(messages("cancellation.functionalReferenceId"), "Enter the DUCR")
      assertMessage(messages("cancellation.declarationId"), "Enter the MRN")
      assertMessage(messages("cancellation.statementDescription"), "Enter the reason for cancellation")
      assertMessage(messages("cancellation.confirmationPage.title"), "Cancellation confirmation page")
    }

    "have proper messages for error labels" in {
      assertMessage(messages("cancellation.functionalReferenceId.empty"), "Enter the DUCR")
      assertMessage(messages("cancellation.functionalReferenceId.tooLong"), "THE DUCR must be 35 digits or less")
      assertMessage(messages("cancellation.functionalReferenceId.tooShort"), "The DUCR must be 23 digits or more")
      assertMessage(messages("cancellation.functionalReferenceId.wrongFormat"), "Enter a DUCR in the correct format")

      assertMessage(messages("cancellation.declarationId.empty"), "Enter the MRN")
      assertMessage(messages("cancellation.declarationId.tooLong"), "The MRN must be 70 digits or less")
      assertMessage(messages("cancellation.declarationId.tooShort"), "The MRN must be X digits or more")
      assertMessage(messages("cancellation.declarationId.wrongFormat"), "Enter a MRN in the correct format")

      assertMessage(messages("cancellation.statementDescription.empty"), "Enter a description")
      assertMessage(messages("cancellation.statementDescription.tooLong"), "The description must be 512 digits or less")
      assertMessage(messages("cancellation.statementDescription.tooShort"), "The description must be X digits or more")
      assertMessage(
        messages("cancellation.statementDescription.wrongFormat"),
        "Enter a description in the correct format"
      )

      assertMessage(
        messages("cancellation.changeReason.error.wrongValue"),
        "Please, choose a valid cancellation reason"
      )
    }
  }

  "Cancel DeclarationView on empty page" should {

    "display page title" in {

      getElementById(createView(), "title").text() must be(messages("cancellation.title"))
    }

    "display empty input with label for 'Functional Reference ID'" in {

      val view = createView()

      getElementById(view, "functionalReferenceId-label").text() must be(messages("cancellation.functionalReferenceId"))
      getElementById(view, "functionalReferenceId").attr("value") must be("")
    }

    "display empty input with label for 'declaration Id'" in {

      val view = createView()

      getElementById(view, "declarationId-label").text() must be(messages("cancellation.declarationId"))
      getElementById(view, "declarationId").attr("value") must be("")
    }

    "display empty input with label for 'statement Description'" in {

      val view = createView()

      getElementById(view, "statementDescription-label").text() must be(messages("cancellation.statementDescription"))
      getElementById(view, "statementDescription").attr("value") must be("")
    }

    "display empty input with label for 'Country'" in {

      val view = createView()

      getElementById(view, "changeReason-label").text() must be(messages("cancellation.changeReason"))
      getElementById(view, "changeReason").attr("value") must be("")
    }

    "display three radio buttons with description (not selected)" in {

      val view = createView(CancelDeclaration.form.fill(CancelDeclaration("", "", "", "")))

      val noLongerRequired = getElementById(view, "noLongerRequired")
      noLongerRequired.attr("checked") must be("")
      val noLongerRequiredLabel = getElementById(view, "noLongerRequired-label")
      noLongerRequiredLabel.text() must be(messages("cancellation.reason.noLongerRequired"))

      val otherReason = getElementById(view, "otherReason")
      otherReason.attr("checked") must be("")
      val otherReasonLabel = getElementById(view, "otherReason-label")
      otherReasonLabel.text() must be(messages("cancellation.reason.otherReason"))

      val duplication = getElementById(view, "duplication")
      duplication.attr("checked") must be("")
      val duplicationLabel = getElementById(view, "duplication-label")
      duplicationLabel.text() must be(messages("cancellation.reason.duplication"))
    }

    "display 'Submit' button on page" in {

      val view = createView()

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages("cancellation.submitButton"))
    }
  }

  "Cancellation View for invalid input" can {

    "display errors when nothing is entered" in {

      val view = createView(CancelDeclaration.form.bind(Map[String, String]()))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, "error.required", "#functionalReferenceId")
      checkErrorLink(view, 2, "error.required", "#declarationId")
      checkErrorLink(view, 3, "error.required", "#statementDescription")
      checkErrorLink(view, 4, "error.required", "#changeReason")

      getElementById(view, "error-message-functionalReferenceId-input").text() must be(messages("error.required"))
      getElementById(view, "error-message-declarationId-input").text() must be(messages("error.required"))
      getElementById(view, "error-message-statementDescription-input").text() must be(messages("error.required"))
      getElementById(view, "error-message-changeReason-input").text() must be(messages("error.required"))
    }

    "display error when referenceId" when {
      "is entered but is too short" in {

        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(CancelDeclaration("123", "123456789", "Some Description", NoLongerRequired.toString))
        )

        checkErrorsSummary(view)
        checkErrorLink(view, 1, "cancellation.functionalReferenceId.tooShort", "#functionalReferenceId")
        checkErrorLink(view, 2, "cancellation.functionalReferenceId.wrongFormat", "#functionalReferenceId")

        getElementByCss(view, "#error-message-functionalReferenceId-input").text() must be(
          messages("cancellation.functionalReferenceId.tooShort")
        )
      }

      "is entered but is too long" in {

        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(
              CancelDeclaration(
                "1SA123456789012-1FSA1234567IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII",
                "123456789",
                "Some Description",
                NoLongerRequired.toString
              )
            )
        )

        checkErrorsSummary(view)
        checkErrorLink(view, 1, "cancellation.functionalReferenceId.tooLong", "#functionalReferenceId")
        checkErrorLink(view, 2, "cancellation.functionalReferenceId.wrongFormat", "#functionalReferenceId")

        getElementByCss(view, "#error-message-functionalReferenceId-input").text() must be(
          messages("cancellation.functionalReferenceId.tooLong")
        )
      }

      "is entered but is in the wrong format" in {

        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(
              CancelDeclaration(
                "1SA123456789012+1FSA1234567",
                "123456789",
                "Some Description",
                NoLongerRequired.toString
              )
            )
        )

        checkErrorsSummary(view)
        checkErrorLink(view, 1, "cancellation.functionalReferenceId.wrongFormat", "#functionalReferenceId")

        getElementByCss(view, "#error-message-functionalReferenceId-input").text() must be(
          messages("cancellation.functionalReferenceId.wrongFormat")
        )
      }
    }

    "display error when declarationId" when {

      "is entered but is too long" in {

        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(
              CancelDeclaration(
                "1SA123456789012-1FSA1234567",
                createRandomAlphanumericString(71),
                "Some Description",
                NoLongerRequired.toString
              )
            )
        )

        checkErrorsSummary(view)
        checkErrorLink(view, 1, "cancellation.declarationId.tooLong", "#declarationId")

        getElementByCss(view, "#error-message-declarationId-input").text() must be(
          messages("cancellation.declarationId.tooLong")
        )
      }

      "is entered but is in the wrong format" in {

        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(
              CancelDeclaration(
                "1SA123456789012-1FSA1234567",
                "1234567890123-",
                "Some Description",
                NoLongerRequired.toString
              )
            )
        )

        checkErrorsSummary(view)
        checkErrorLink(view, 1, "cancellation.declarationId.wrongFormat", "#declarationId")

        getElementByCss(view, "#error-message-declarationId-input").text() must be(
          messages("cancellation.declarationId.wrongFormat")
        )
      }
    }

    "display error when description" when {

      "is entered but is too long" in {

        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(
              CancelDeclaration(
                "1SA123456789012-1FSA1234567",
                "123456789",
                createRandomAlphanumericString(600),
                NoLongerRequired.toString
              )
            )
        )

        checkErrorsSummary(view)
        checkErrorLink(view, 1, "cancellation.statementDescription.tooLong", "#statementDescription")

        getElementByCss(view, "#error-message-statementDescription-input").text() must be(
          messages("cancellation.statementDescription.tooLong")
        )
      }

      "is entered but is in the wrong format" in {

        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(
              CancelDeclaration(
                "1SA123456789012-1FSA1234567",
                "1234567890123",
                "Some Description$$$$",
                NoLongerRequired.toString
              )
            )
        )

        checkErrorsSummary(view)
        checkErrorLink(view, 1, "cancellation.statementDescription.wrongFormat", "#statementDescription")

        getElementByCss(view, "#error-message-statementDescription-input").text() must be(
          messages("cancellation.statementDescription.wrongFormat")
        )
      }
    }

    "display error when change reason" when {
      "is entered but the value is unknown" in {

        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(
              CancelDeclaration("1SA123456789012-1FSA1234567", "1234567890123", "Some Description", "wrong value")
            )
        )

        checkErrorsSummary(view)
        checkErrorLink(view, 1, "cancellation.changeReason.error.wrongValue", "#changeReason")

        getElementByCss(view, "#error-message-changeReason-input").text() must be(
          messages("cancellation.changeReason.error.wrongValue")
        )
      }
    }
  }
}
