/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.routes
import forms.Choice.AllowedChoiceValues.CancelDec
import forms.cancellation.CancellationChangeReason.NoLongerRequired
import forms.{CancelDeclaration, Choice, Lrn}
import helpers.views.declaration.CommonMessages
import org.jsoup.nodes.Document
import play.api.data.Form
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.cancel_declaration
import views.tags.ViewTest

@ViewTest
class CancelDeclarationViewSpec extends UnitViewSpec with CommonMessages with Stubs {

  private val form: Form[CancelDeclaration] = CancelDeclaration.form
  private val cancelDeclarationPage = new cancel_declaration(mainTemplate)

  def testView(
    functionalReferenceId: String,
    mrn: String,
    statementDescription: String,
    cancellationReason: String,
    elementId: String,
    hrefPageLink: String,
    idOfErrorElement: String,
    expectedMessage: String
  ): Unit = {

    val view = createView(
      CancelDeclaration.form
        .fillAndValidate(CancelDeclaration(Lrn(functionalReferenceId), mrn, statementDescription, cancellationReason))
    )

    view must haveGlobalErrorSummary
    view must haveFieldErrorLink(elementId, hrefPageLink)

    view.getElementById(idOfErrorElement).text() mustBe expectedMessage
  }

  private def createView(form: Form[CancelDeclaration] = form): Document =
    cancelDeclarationPage(form)(request, messages)

  "Cancel DeclarationView on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() mustBe messages("cancellation.title")
    }

    "display empty input with label for 'Functional Reference ID'" in {

      val view = createView()

      view.getElementById("functionalReferenceId-label").text() mustBe messages("cancellation.functionalReferenceId")
      view.getElementById("functionalReferenceId").attr("value") mustBe empty
    }

    "display empty input with label for 'mrn'" in {

      val view = createView()

      view.getElementById("mrn-label").text() mustBe messages("cancellation.mrn")
      view.getElementById("mrn").attr("value") mustBe empty
    }

    "display empty input with label for 'statement Description'" in {

      val view = createView()

      view.getElementById("statementDescription-label").text() mustBe messages("cancellation.statementDescription")
      view.getElementById("statementDescription").attr("value") mustBe empty
    }

    "display empty input with label for 'Country'" in {

      val view = createView()

      view.getElementById("changeReason-label").text() mustBe messages("cancellation.changeReason")
      view.getElementById("changeReason").attr("value") mustBe empty
    }

    "display three radio buttons with description (not selected)" in {

      val view = createView(CancelDeclaration.form.fill(CancelDeclaration(Lrn(""), "", "", "")))

      val noLongerRequired = view.getElementById("noLongerRequired")
      noLongerRequired.attr("checked") mustBe empty
      val noLongerRequiredLabel = view.getElementById("noLongerRequired-label")
      noLongerRequiredLabel.text() mustBe messages("cancellation.reason.noLongerRequired")

      val otherReason = view.getElementById("otherReason")
      otherReason.attr("checked") mustBe empty
      val otherReasonLabel = view.getElementById("otherReason-label")
      otherReasonLabel.text() mustBe messages("cancellation.reason.otherReason")

      val duplication = view.getElementById("duplication")
      duplication.attr("checked") mustBe empty
      val duplicationLabel = view.getElementById("duplication-label")
      duplicationLabel.text() mustBe messages("cancellation.reason.duplication")
    }

    "display 'Submit' button on page" in {

      val view = createView()

      val saveButton = view.select("#submit")
      saveButton.text() mustBe messages("cancellation.submitButton")
    }

    "display 'Back' button that links to 'Choice' page with Cancel declaration selected" in {
      val backButton = createView().getElementById("back-link")

      backButton must containText("site.back")
      backButton must haveHref(routes.ChoiceController.displayPage(Some(Choice(CancelDec))))
    }
  }

  "Cancellation View for invalid input" should {

    "display errors" when {

      val view = createView(CancelDeclaration.form.bind(Map[String, String]()))

      "functionalReferenceID is empty" in {
        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("functionalReferenceId", "#functionalReferenceId")
        view.getElementById("error-message-functionalReferenceId-input").text() mustBe messages("error.required")
      }

      "mrn is empty" in {
        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("mrn", "#mrn")
        view.getElementById("error-message-mrn-input").text() mustBe messages("error.required")
      }

      "statementDescription is empty" in {
        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("statementDescription", "#statementDescription")
        view.getElementById("error-message-statementDescription-input").text() mustBe messages("error.required")
      }

      "changeReason is empty" in {
        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("changeReason", "#changeReason")
        view.getElementById("error-message-changeReason-input").text() mustBe messages("error.required")
      }
    }

    "display error when referenceId" when {
      "is entered but is too long" in {

        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(
              CancelDeclaration(
                Lrn("1SA123456789012-1FSA1234567IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII"),
                "123456789",
                "Some Description",
                NoLongerRequired.toString
              )
            )
        )

        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("functionalReferenceId", "#functionalReferenceId")

        view.getElementById("error-message-functionalReferenceId-input").text() mustBe
          messages("cancellation.functionalReferenceId.error.length")
      }

      "is entered but is in the wrong format" in {

        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(CancelDeclaration(Lrn("12345566++"), "123456789", "Some Description", NoLongerRequired.toString))
        )

        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("functionalReferenceId", "#functionalReferenceId")

        view.getElementById("error-message-functionalReferenceId-input").text() mustBe
          messages("cancellation.functionalReferenceId.error.specialCharacter")
      }
    }

    "display error when mrn" when {

      "is entered but is too long" in {

        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(
              CancelDeclaration(Lrn("1SA123456789012-1FSA1234567"), createRandomAlphanumericString(71), "Some Description", NoLongerRequired.toString)
            )
        )

        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("mrn", "#mrn")

        view.getElementById("error-message-mrn-input").text() mustBe
          messages("cancellation.mrn.tooLong")
      }

      "is empty" in {

        testView(
          "1SA123456789012-1FSA1234567",
          "",
          "Some Description",
          NoLongerRequired.toString,
          "mrn",
          "#mrn",
          "error-message-mrn-input",
          messages("cancellation.mrn.empty")
        )
      }

      "is entered but is in the wrong format" in {

        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(CancelDeclaration(Lrn("1SA123456789012-1FSA1234567"), "1234567890123-", "Some Description", NoLongerRequired.toString))
        )

        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("mrn", "#mrn")

        view.getElementById("error-message-mrn-input").text() mustBe
          messages("cancellation.mrn.wrongFormat")
      }
    }

    "display error when description" when {

      "is entered but is too long" in {

        testView(
          "1SA123456789012-1FSA1234567",
          "123456789",
          createRandomAlphanumericString(600),
          NoLongerRequired.toString,
          "statementDescription",
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
          "statementDescription",
          "#statementDescription",
          "error-message-statementDescription-input",
          messages("cancellation.statementDescription.empty")
        )
      }

      "is entered but is in the wrong format" in {

        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(
              CancelDeclaration(Lrn("1SA123456789012-1FSA1234567"), "1234567890123", "Some Description$$$$", NoLongerRequired.toString)
            )
        )

        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("statementDescription", "#statementDescription")

        view.getElementById("error-message-statementDescription-input").text() mustBe
          messages("cancellation.statementDescription.wrongFormat")
      }
    }

    "display error when change reason" when {
      "is entered but the value is unknown" in {

        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(CancelDeclaration(Lrn("1SA123456789012-1FSA1234567"), "1234567890123", "Some Description", "wrong value"))
        )

        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("changeReason", "#changeReason")

        view.getElementById("error-message-changeReason-input").text() mustBe
          messages("cancellation.changeReason.error.wrongValue")
      }
    }
  }
}
