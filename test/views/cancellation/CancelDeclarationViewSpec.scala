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
import forms.{CancelDeclarationDescription, Choice, Lrn}
import org.jsoup.nodes.Document
import play.api.data.Form
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.cancel_declaration
import views.tags.ViewTest

@ViewTest
class CancelDeclarationViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  private val form: Form[CancelDeclarationDescription] = CancelDeclarationDescription.form
  private val cancelDeclarationPage = instanceOf[cancel_declaration]

  "Cancel DeclarationView on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() mustBe messages("cancellation.title")
    }

    "display empty input with label for 'statement Description'" in {

      val view = createView()

      view.getElementsByAttributeValue("for", "statementDescription").text() mustBe messages("cancellation.statementDescription")
      view.getElementById("statementDescription").attr("value") mustBe empty
    }

    "display three radio buttons with description (not selected)" in {

      val view = createView(CancelDeclarationDescription.form.fill(CancelDeclarationDescription("", "")))

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
    val view = createView(CancelDeclarationDescription.form.bind(Map[String, String]()))

    "display statementDescription error" when {

      "field is empty " in {
        testView("", "statementDescription", "empty")
      }

      "field is entered but is too long" in {
        val longDesc = createRandomAlphanumericString(600)
        testView(longDesc, "statementDescription", "length")
      }

      "field is entered but is in the wrong format" in {
        val view = createView(
          CancelDeclarationDescription.form
            .fillAndValidate(CancelDeclarationDescription(NoLongerRequired.toString, "Some Description$$$$"))
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
          CancelDeclarationDescription.form
            .fillAndValidate(CancelDeclarationDescription("wrong value", "Some Description"))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#noLongerRequired")

        view must containErrorElementWithMessageKey("cancellation.changeReason.error.wrongValue")
      }
    }
  }

  def testView(description: String, key: String, errorType: String): Unit = {
    val view = createView(
      CancelDeclarationDescription.form
        .fillAndValidate(CancelDeclarationDescription(NoLongerRequired.toString, description))
    )

    view must haveGovukGlobalErrorSummary
    view must containErrorElementWithTagAndHref("a", s"#$key")

    view must containErrorElementWithMessage(messages(s"cancellation.$key.error.$errorType"))
  }

  private def createView(form: Form[CancelDeclarationDescription] = form): Document =
    cancelDeclarationPage(form, Lrn("098765432"), "34567890", "456789")(request, messages)
}
