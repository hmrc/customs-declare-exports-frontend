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

package views.supplementary

import forms.Ducr
import forms.supplementary.ConsignmentReferences
import play.api.data.Form
import play.twirl.api.Html
import views.helpers.{Item, ViewSpec}
import views.html.supplementary.consignment_references
import views.tags.ViewTest

@ViewTest
class ConsignmentReferencesViewSpec extends ViewSpec {

  private val form: Form[ConsignmentReferences] = ConsignmentReferences.form()

  private val prefix = s"${basePrefix}consignmentReferences."

  private val title = Item(prefix, "title")
  private val hint = Item(prefix, "header")
  private val lrn = Item(prefix, "lrn")
  private val ucr = Item(prefix, "ucr")
  private val ucrError = Item("error.", "ducr")
  private val lrnEmpty = Item(prefix + "lrn.error.", "empty")
  private val lrnLength = Item(prefix + "lrn.error.", "length")
  private val lrnSpecialCharacter = Item(prefix + "lrn.error.", "specialCharacter")

  private def createView(form: Form[ConsignmentReferences] = form): Html =
    consignment_references(appConfig, form)(fakeRequest, messages)

  "Consignment References View" should {

    "have proper messages for labels" in {

      assertMessage(title.withPrefix, "Consignment References")
      assertMessage(hint.withPrefix, "Your references")
      assertMessage(lrn.withInfo, "2/5 Enter your LRN reference number")
      assertMessage(lrn.withHint, "This is your own reference number of up to 22 digits")
      assertMessage(ucr.withInfo, "2/4 Create a UCR reference for this declaration")
      assertMessage(ucr.withHint, "Your own reference, which must be used only for this declaration")
    }

    "have proper messages for error labels" in {

      assertMessage(ucrError.withPrefix, "Incorrect DUCR")
      assertMessage(lrnEmpty.withPrefix, "LRN cannot be empty")
      assertMessage(lrnLength.withPrefix, "LRN cannot exceed 22 characters")
      assertMessage(lrnSpecialCharacter.withPrefix, "LRN cannot contain special characters")
    }
  }

  "Consignment References View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title.withPrefix))
    }

    "display header" in {

      val view = createView()

      getElementByCss(view, "legend>h1").text() must be(messages(hint.withPrefix))
    }

    "display empty input with label for UCR" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(2)>label>span:nth-child(1)").text() must be(messages(ucr.withInfo))
      getElementByCss(view, "form>div:nth-child(2)>label>span.form-hint").text() must be(messages(ucr.withHint))
      getElementById(view, "ducr_ducr").attr("value") must be("")
    }

    "display empty input with label for LRN" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(3)>label>span:nth-child(1)").text() must be(messages(lrn.withInfo))
      getElementByCss(view, "form>div:nth-child(3)>label>span.form-hint").text() must be(messages(lrn.withHint))
      getElementById(view, "lrn").attr("value") must be("")
    }

    "display \"Back\" button that links to \"Declaration Type\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be("Back")
      backButton.attr("href") must be("/customs-declare-exports/declaration/supplementary/type")
    }

    "display \"Save and continue\" button on page" in {

      val view = createView()

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be("Save and continue")
    }
  }

  "Consignment References View for invalid input" should {

    "display error for empty LRN" in {

      val view = createView(ConsignmentReferences.form().withError(lrn.key, messages(lrnEmpty.withPrefix)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, lrnEmpty.withPrefix, "#lrn")

      getElementByCss(view, "#error-message-lrn-input").text() must be(messages(lrnEmpty.withPrefix))
    }

    "display error when LRN is longer then 22 characters" in {

      val view = createView(ConsignmentReferences.form().withError(lrn.key, messages(lrnLength.withPrefix)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, lrnLength.withPrefix, "#lrn")

      getElementByCss(view, "#error-message-lrn-input").text() must be(messages(lrnLength.withPrefix))
    }

    "display error when LRN contains special character" in {

      val view = createView(ConsignmentReferences.form().withError(lrn.key, messages(lrnSpecialCharacter.withPrefix)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, lrnSpecialCharacter.withPrefix, "#lrn")

      getElementByCss(view, "#error-message-lrn-input").text() must be(messages(lrnSpecialCharacter.withPrefix))
    }

    "display error when UCR is incorrect and LRN empty" in {

      val view = createView(
        ConsignmentReferences
          .form()
          .withError("ducr.ducr", messages(ucrError.withPrefix))
          .withError(lrn.key, messages(lrnEmpty.withPrefix))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, ucrError.withPrefix, "#ducr_ducr")
      checkErrorLink(view, 2, lrnEmpty.withPrefix, "#lrn")

      getElementByCss(view, "#error-message-ducr_ducr-input").text() must be(messages(ucrError.withPrefix))
      getElementByCss(view, "#error-message-lrn-input").text() must be(messages(lrnEmpty.withPrefix))
    }

    "display error when UCR is incorrect and LRN is longer then 22 characters" in {

      val view = createView(
        ConsignmentReferences
          .form()
          .withError("ducr.ducr", messages(ucrError.withPrefix))
          .withError(lrn.key, messages(lrnLength.withPrefix))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, ucrError.withPrefix, "#ducr_ducr")
      checkErrorLink(view, 2, lrnLength.withPrefix, "#lrn")

      getElementByCss(view, "#error-message-ducr_ducr-input").text() must be(messages(ucrError.withPrefix))
      getElementByCss(view, "#error-message-lrn-input").text() must be(messages(lrnLength.withPrefix))
    }

    "display error when UCR is incorrect and LRN contains special character" in {

      val view = createView(
        ConsignmentReferences
          .form()
          .withError("ducr.ducr", messages(ucrError.withPrefix))
          .withError(lrn.key, messages(lrnSpecialCharacter.withPrefix))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, ucrError.withPrefix, "#ducr_ducr")
      checkErrorLink(view, 2, lrnSpecialCharacter.withPrefix, "#lrn")

      getElementByCss(view, "#error-message-ducr_ducr-input").text() must be(messages(ucrError.withPrefix))
      getElementByCss(view, "#error-message-lrn-input").text() must be(messages(lrnSpecialCharacter.withPrefix))
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
