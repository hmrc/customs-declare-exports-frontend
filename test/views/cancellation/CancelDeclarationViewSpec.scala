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
  private val cancelDeclarationPage = app.injector.instanceOf[cancel_declaration]
  private def createView(form: Form[CancelDeclaration] = form): Html =
    cancelDeclarationPage(form)(fakeRequest, messages)

  def testView(
    functionalReferenceId: String,
    declarationId: String,
    statementDescription: String,
    cancellationReason: String,
    elementId: String,
    errorMsg: String,
    hrefPageLink: String,
    idOfErrorElement: String,
    expectedMessage: String
  ): Unit = {

    val view = createView(
      CancelDeclaration.form
        .fillAndValidate(
          CancelDeclaration(functionalReferenceId, declarationId, statementDescription, cancellationReason)
        )
    )

    checkErrorsSummary(view)
    checkErrorLink(view, elementId, errorMsg, hrefPageLink)

    view.getElementById(idOfErrorElement).text() must be(expectedMessage)
  }

  "Cancel DeclarationView on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() must be(messages("cancellation.title"))
    }

    "display empty input with label for 'Functional Reference ID'" in {

      val view = createView()

      view.getElementById("functionalReferenceId-label").text() must be(messages("cancellation.functionalReferenceId"))
      view.getElementById("functionalReferenceId").attr("value") must be("")
    }

    "display empty input with label for 'declaration Id'" in {

      val view = createView()

      view.getElementById("declarationId-label").text() must be(messages("cancellation.declarationId"))
      view.getElementById("declarationId").attr("value") must be("")
    }

    "display empty input with label for 'statement Description'" in {

      val view = createView()

      view.getElementById("statementDescription-label").text() must be(messages("cancellation.statementDescription"))
      view.getElementById("statementDescription").attr("value") must be("")
    }

    "display empty input with label for 'Country'" in {

      val view = createView()

      view.getElementById("changeReason-label").text() must be(messages("cancellation.changeReason"))
      view.getElementById("changeReason").attr("value") must be("")
    }

    "display three radio buttons with description (not selected)" in {

      val view = createView(CancelDeclaration.form.fill(CancelDeclaration("", "", "", "")))

      val noLongerRequired = view.getElementById("noLongerRequired")
      noLongerRequired.attr("checked") must be("")
      val noLongerRequiredLabel = view.getElementById("noLongerRequired-label")
      noLongerRequiredLabel.text() must be(messages("cancellation.reason.noLongerRequired"))

      val otherReason = view.getElementById("otherReason")
      otherReason.attr("checked") must be("")
      val otherReasonLabel = view.getElementById("otherReason-label")
      otherReasonLabel.text() must be(messages("cancellation.reason.otherReason"))

      val duplication = view.getElementById("duplication")
      duplication.attr("checked") must be("")
      val duplicationLabel = view.getElementById("duplication-label")
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
      checkErrorLink(view, "functionalReferenceId-error", "error.required", "#functionalReferenceId")
      checkErrorLink(view, "declarationId-error", "error.required", "#declarationId")
      checkErrorLink(view, "statementDescription-error", "error.required", "#statementDescription")
      checkErrorLink(view, "changeReason-error", "error.required", "#changeReason")

      view.getElementById("error-message-functionalReferenceId-input").text() must be(messages("error.required"))
      view.getElementById("error-message-declarationId-input").text() must be(messages("error.required"))
      view.getElementById("error-message-statementDescription-input").text() must be(messages("error.required"))
      view.getElementById("error-message-changeReason-input").text() must be(messages("error.required"))
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

        view.getElementById("error-message-functionalReferenceId-input").text() must be(
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

        view.getElementById("error-message-functionalReferenceId-input").text() must be(
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
        checkErrorLink(
          view,
          "functionalReferenceId-error",
          "cancellation.functionalReferenceId.wrongFormat",
          "#functionalReferenceId"
        )

        view.getElementById("error-message-functionalReferenceId-input").text() must be(
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
        checkErrorLink(view, "declarationId-error", "cancellation.declarationId.tooLong", "#declarationId")

        view.getElementById("error-message-declarationId-input").text() must be(
          messages("cancellation.declarationId.tooLong")
        )
      }

      "is empty" in {

        testView(
          "1SA123456789012-1FSA1234567",
          "",
          "Some Description",
          NoLongerRequired.toString,
          "declarationId-error",
          "cancellation.declarationId.empty",
          "#declarationId",
          "error-message-declarationId-input",
          messages("cancellation.declarationId.empty")
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
        checkErrorLink(view, "declarationId-error", "cancellation.declarationId.wrongFormat", "#declarationId")

        view.getElementById("error-message-declarationId-input").text() must be(
          messages("cancellation.declarationId.wrongFormat")
        )
      }
    }

    "display error when description" when {

      "is entered but is too long" in {

        testView(
          "1SA123456789012-1FSA1234567",
          "123456789",
          createRandomAlphanumericString(600),
          NoLongerRequired.toString,
          "statementDescription-error",
          "cancellation.statementDescription.tooLong",
          "#statementDescription",
          "error-message-statementDescription-input",
          messages("cancellation.statementDescription.tooLong")
        )
      }

      "is empty " in {

        testView(
          "1SA123456789012-1FSA1234567",
          "123456789",
          "",
          NoLongerRequired.toString,
          "statementDescription-error",
          "cancellation.statementDescription.empty",
          "#statementDescription",
          "error-message-statementDescription-input",
          messages("cancellation.statementDescription.empty")
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
        checkErrorLink(
          view,
          "statementDescription-error",
          "cancellation.statementDescription.wrongFormat",
          "#statementDescription"
        )

        view.getElementById("error-message-statementDescription-input").text() must be(
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
        checkErrorLink(view, "changeReason-error", "cancellation.changeReason.error.wrongValue", "#changeReason")

        view.getElementById("error-message-changeReason-input").text() must be(
          messages("cancellation.changeReason.error.wrongValue")
        )
      }
    }

  }
}
