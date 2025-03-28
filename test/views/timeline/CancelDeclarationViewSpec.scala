/*
 * Copyright 2024 HM Revenue & Customs
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

package views.timeline

import base.Injector
import base.TestHelper.createRandomAlphanumericString
import controllers.timeline.routes.DeclarationDetailsController
import forms.section1.Lrn
import forms.timeline.CancelDeclarationDescription
import forms.timeline.CancellationChangeReason.NoLongerRequired
import models.requests.{CancelDeclarationData, SessionHelper}
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import tools.Stubs
import views.common.UnitViewSpec
import views.helpers.CommonMessages
import views.html.timeline.cancel_declaration
import views.tags.ViewTest

@ViewTest
class CancelDeclarationViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  private val form: Form[CancelDeclarationDescription] = CancelDeclarationDescription.form
  private val cancelDeclarationPage = instanceOf[cancel_declaration]

  private val submissionId = "d84bedc0"
  private val mrn = "456789"
  private val lrn = Lrn("098765432")
  private val ducr = "34567890"

  private val cancelDeclarationData = CancelDeclarationData(submissionId, mrn, lrn, ducr)

  private def createView(form: Form[CancelDeclarationDescription] = form): Document =
    cancelDeclarationPage(form, cancelDeclarationData)(journeyRequest(aDeclaration(), (SessionHelper.declarationUuid, "decId")), messages)

  "Cancel DeclarationView on empty page" should {
    val view = createView()

    "display page title" in {
      view.getElementById("title").text mustBe messages("cancellation.title")
    }

    "display the 'MRN' hint" in {
      view.getElementsByClass("submission-mrn").first.text mustBe messages("mrn.heading", mrn)
    }

    "not have View declaration summary link" in {
      Option(view.getElementById("view_declaration_summary")) mustBe None
    }

    "display ducr and lrn" in {
      view.getElementsByClass("govuk-body").first.text mustBe messages("cancellation.ducr", ducr)
      view.getElementsByClass("govuk-body").last.text mustBe messages("cancellation.lrn", lrn.lrn)
    }

    "display empty input with label for 'statement Description'" in {
      view.getElementsByAttributeValue("for", "statementDescription").text mustBe messages("cancellation.statementDescription")
      view.getElementById("statementDescription").attr("value") mustBe empty
    }

    "display three radio buttons with description (not selected)" in {

      val view = createView(CancelDeclarationDescription.form.fill(CancelDeclarationDescription("", "")))

      val noLongerRequired = view.getElementById("noLongerRequired")
      noLongerRequired.attr("checked") mustBe empty
      val noLongerRequiredLabel = view.getElementsByAttributeValue("for", "noLongerRequired")
      noLongerRequiredLabel.text mustBe messages("cancellation.reason.noLongerRequired")

      val otherReason = view.getElementById("otherReason")
      otherReason.attr("checked") mustBe empty
      val otherReasonLabel = view.getElementsByAttributeValue("for", "otherReason")
      otherReasonLabel.text mustBe messages("cancellation.reason.otherReason")

      val duplication = view.getElementById("duplication")
      duplication.attr("checked") mustBe empty
      val duplicationLabel = view.getElementsByAttributeValue("for", "duplication")
      duplicationLabel.text mustBe messages("cancellation.reason.duplication")
    }

    "display 'Submit' button on page" in {
      view.select("#submit").text mustBe messages("site.submit")
    }

    "display 'Back' button that links to 'Choice' page with Cancel declaration selected" in {
      val backButton = view.getElementById("back-link")
      backButton must containMessage("site.back")
      backButton must haveHref(DeclarationDetailsController.displayPage(submissionId))
    }
  }

  "Cancellation View for invalid input" should {
    val view = createView(CancelDeclarationDescription.form.bind(Map[String, String]()))

    "field value has already been submitted in a previous cancellation request" in {
      val view = createView(
        CancelDeclarationDescription.form
          .fillAndValidate(CancelDeclarationDescription(NoLongerRequired.toString, "Some Description"))
          .copy(errors = Seq(FormError(CancelDeclarationDescription.statementDescriptionKey, "cancellation.duplicateRequest.error")))
      )

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", s"#${CancelDeclarationDescription.statementDescriptionKey}")

      view must containErrorElementWithMessageKey("cancellation.duplicateRequest.error")
    }

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
}
