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

package views.declaration

import base.TestHelper
import forms.Ducr
import forms.declaration.ConsignmentReferences
import helpers.views.declaration.{CommonMessages, ConsignmentReferencesMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.consignment_references
import views.tags.ViewTest

@ViewTest
class ConsignmentReferencesViewSpec extends ViewSpec with ConsignmentReferencesMessages with CommonMessages {

  /*
   * Seems like DUCR is Declaration UCR which is alphanumeric number up to 35 digits
   */

  private val properDUCR = "7GB000000000000-12345"
  private val incorrectDUCR = "7GB000000000000-1234512345123451234512345"

  private val form: Form[ConsignmentReferences] = ConsignmentReferences.form()
  private def createView(form: Form[ConsignmentReferences] = form): Html =
    consignment_references(appConfig, form)(fakeRequest, messages)

  "Consignment References View" should {

    "have proper messages for labels" in {

      assertMessage(title, "Consignment References")
      assertMessage(header, "Your references")
      assertMessage(lrnInfo, "2/5 Enter your LRN reference number")
      assertMessage(lrnHint, "This is your own reference number of up to 22 digits")
      assertMessage(ucrInfo, "2/4 Create a UCR reference for this declaration")
      assertMessage(ucrHint, "Your own reference, which must be used only for this declaration")
    }

    "have proper messages for error labels" in {

      assertMessage(lrnEmpty, "LRN cannot be empty")
      assertMessage(lrnLength, "LRN cannot exceed 22 characters")
      assertMessage(lrnSpecialCharacter, "LRN cannot contain special characters")
    }
  }

  "Consignment References View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title))
    }

    "display header" in {

      getElementByCss(createView(), "legend>h1").text() must be(messages(header))
    }

    "display empty input with label for DUCR" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(2)>label>span:nth-child(1)").text() must be(messages(ucrInfo))
      getElementByCss(view, "form>div:nth-child(2)>label>span.form-hint").text() must be(messages(ucrHint))
      getElementById(view, "ducr_ducr").attr("value") must be("")
    }

    "display empty input with label for LRN" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(3)>label>span:nth-child(1)").text() must be(messages(lrnInfo))
      getElementByCss(view, "form>div:nth-child(3)>label>span.form-hint").text() must be(messages(lrnHint))
      getElementById(view, "lrn").attr("value") must be("")
    }

    "display \"Back\" button that links to \"Declaration Type\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/type")
    }

    "display \"Save and continue\" button on page" in {

      val view = createView()

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Consignment References View for invalid input" should {

    "display error for empty LRN" in {

      val view =
        createView(ConsignmentReferences.form().fillAndValidate(ConsignmentReferences(Some(Ducr(properDUCR)), "")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, lrnEmpty, "#lrn")

      getElementByCss(view, "#error-message-lrn-input").text() must be(messages(lrnEmpty))
    }

    "display error when LRN is longer then 22 characters" in {

      val view = createView(
        ConsignmentReferences
          .form()
          .fillAndValidate(ConsignmentReferences(Some(Ducr(properDUCR)), TestHelper.createRandomAlphanumericString(23)))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, lrnLength, "#lrn")

      getElementByCss(view, "#error-message-lrn-input").text() must be(messages(lrnLength))
    }

    "display error when LRN contains special character" in {

      val view =
        createView(ConsignmentReferences.form().fillAndValidate(ConsignmentReferences(Some(Ducr(properDUCR)), "#@#$")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, lrnSpecialCharacter, "#lrn")

      getElementByCss(view, "#error-message-lrn-input").text() must be(messages(lrnSpecialCharacter))
    }

    "display error when DUCR is incorrect and LRN empty" in {

      val view =
        createView(ConsignmentReferences.form().fillAndValidate(ConsignmentReferences(Some(Ducr(incorrectDUCR)), "")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, ucrError, "#ducr_ducr")
      checkErrorLink(view, 2, lrnEmpty, "#lrn")

      getElementByCss(view, "#error-message-ducr_ducr-input").text() must be(messages(ucrError))
      getElementByCss(view, "#error-message-lrn-input").text() must be(messages(lrnEmpty))
    }

    "display error when DUCR is incorrect and LRN is longer then 22 characters" in {

      val view = createView(
        ConsignmentReferences
          .form()
          .fillAndValidate(ConsignmentReferences(Some(Ducr(incorrectDUCR)), TestHelper.createRandomAlphanumericString(23)))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, ucrError, "#ducr_ducr")
      checkErrorLink(view, 2, lrnLength, "#lrn")

      getElementByCss(view, "#error-message-ducr_ducr-input").text() must be(messages(ucrError))
      getElementByCss(view, "#error-message-lrn-input").text() must be(messages(lrnLength))
    }

    "display error when DUCR is incorrect and LRN contains special character" in {

      val view = createView(
        ConsignmentReferences.form().fillAndValidate(ConsignmentReferences(Some(Ducr(incorrectDUCR)), "$$%"))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, ucrError, "#ducr_ducr")
      checkErrorLink(view, 2, lrnSpecialCharacter, "#lrn")

      getElementByCss(view, "#error-message-ducr_ducr-input").text() must be(messages(ucrError))
      getElementByCss(view, "#error-message-lrn-input").text() must be(messages(lrnSpecialCharacter))
    }
  }

  "Consignment References View when filled" should {

    "display data in DUCR input" in {

      val view = createView(ConsignmentReferences.form().fill(ConsignmentReferences(Some(Ducr("12345")), "")))

      getElementById(view, "ducr_ducr").attr("value") must be("12345")
      getElementById(view, "lrn").attr("value") must be("")
    }

    "display data in LRN input" in {

      val view = createView(ConsignmentReferences.form().fill(ConsignmentReferences(Some(Ducr("")), "test1")))

      getElementById(view, "ducr_ducr").attr("value") must be("")
      getElementById(view, "lrn").attr("value") must be("test1")
    }

    "display data in both inputs" in {

      val view = createView(ConsignmentReferences.form().fill(ConsignmentReferences(Some(Ducr("12345")), "test1")))

      getElementById(view, "ducr_ducr").attr("value") must be("12345")
      getElementById(view, "lrn").attr("value") must be("test1")
    }
  }
}
