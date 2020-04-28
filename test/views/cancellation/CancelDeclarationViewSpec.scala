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

import base.Injector
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
class CancelDeclarationViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  private val form: Form[CancelDeclaration] = CancelDeclaration.form
  private val cancelDeclarationPage = instanceOf[cancel_declaration]

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

      backButton must containText("site.back")
      backButton must haveHref(routes.ChoiceController.displayPage(Some(Choice(CancelDec))))
    }
  }

  "Cancellation View for invalid input" should {

    "display errors" when {

      val view = createView(CancelDeclaration.form.bind(Map[String, String]()))

      "functionalReferenceID is empty" in {
        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#functionalReferenceId")
        view must containErrorElementWithMessage("error.required")
      }

      "mrn is empty" in {
        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#mrn")
        view must containErrorElementWithMessage("error.required")
      }

      "statementDescription is empty" in {
        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#statementDescription")
        view must containErrorElementWithMessage("error.required")
      }

      "changeReason is empty" in {
        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#changeReason")
        view must containErrorElementWithMessage("cancellation.changeReason.error.wrongValue")
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

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#functionalReferenceId")

        view must containErrorElementWithMessage("cancellation.functionalReferenceId.error.length")
      }

      "is entered but is in the wrong format" in {

        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(CancelDeclaration(Lrn("12345566++"), "123456789", "Some Description", NoLongerRequired.toString))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#functionalReferenceId")

        view must containErrorElementWithMessage("cancellation.functionalReferenceId.error.specialCharacter")
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

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#mrn")

        view must containErrorElementWithMessage("cancellation.mrn.error.tooLong")
      }

      "is empty" in {

        testView("", "Some Description", "mrn", "empty")
      }

      "is entered but is in the wrong format" in {

        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(CancelDeclaration(Lrn("1SA123456789012-1FSA1234567"), "1234567890123-", "Some Description", NoLongerRequired.toString))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#mrn")

        view must containErrorElementWithMessage("cancellation.mrn.error.wrongFormat")
      }
    }

    "display error when description" when {

      "is entered but is too long" in {

        val longDesc = createRandomAlphanumericString(600)
        testView("123456789", longDesc, "statementDescription", "invalid")
      }

      "is empty " in {

        testView("123456789", "", "statementDescription", "empty")
      }

      "is entered but is in the wrong format" in {

        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(
              CancelDeclaration(Lrn("1SA123456789012-1FSA1234567"), "1234567890123", "Some Description$$$$", NoLongerRequired.toString)
            )
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#statementDescription")

        view must containErrorElementWithMessage("cancellation.statementDescription.error.invalid")
      }
    }

    "display error when change reason" when {
      "is entered but the value is unknown" in {

        val view = createView(
          CancelDeclaration.form
            .fillAndValidate(CancelDeclaration(Lrn("1SA123456789012-1FSA1234567"), "1234567890123", "Some Description", "wrong value"))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#changeReason")

        view must containErrorElementWithMessage("cancellation.changeReason.error.wrongValue")
      }
    }
  }
}
