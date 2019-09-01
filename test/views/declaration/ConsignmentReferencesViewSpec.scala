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
import controllers.util.SaveAndReturn
import forms.Ducr
import forms.declaration.ConsignmentReferences
import helpers.views.declaration.{CommonMessages, ConsignmentReferencesMessages}
import models.Mode
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.AppViewSpec
import views.html.declaration.consignment_references
import views.tags.ViewTest

@ViewTest
class ConsignmentReferencesViewSpec extends AppViewSpec with ConsignmentReferencesMessages with CommonMessages {

  /*
   * Seems like DUCR is Declaration UCR which is alphanumeric number up to 35 digits
   */

  private val properDUCR = "7GB000000000000-12345"
  private val incorrectDUCR = "7GB000000000000-1234512345123451234512345"

  private val form: Form[ConsignmentReferences] = ConsignmentReferences.form()
  private val consignmentReferencesPage = app.injector.instanceOf[consignment_references]
  private def createView(form: Form[ConsignmentReferences] = form): Html =
    consignmentReferencesPage(Mode.Normal, form)(fakeRequest, messages)

  "Consignment References View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() must be(messages(header))
    }

    "display section header" in {

      createView().getElementById("section-header").text() must be(messages("Your references"))
    }

    "display empty input with label for DUCR" in {

      val view = createView()

      view.getElementById("ducr_ducr-label").text() must be(messages(ducrInfo))
      view.getElementById("ducr_ducr-hint").text() must be(messages(ducrHint))
      view.getElementById("ducr_ducr").attr("value") must be("")
    }

    "display empty input with label for LRN" in {

      val view = createView()

      view.getElementById("lrn-label").text() must be(messages(lrnInfo))
      view.getElementById("lrn-hint").text() must be(messages(lrnHint))
      view.getElementById("lrn").attr("value") must be("")
    }

    "display empty input with label for UCR" in {

      val view = createView()

      view.getElementById("personalUcr-label").text() must be(messages(ucrInfo))
      view.getElementById("personalUcr-hint").text() must be(messages(ucrHint))
      view.getElementById("personalUcr").attr("value") must be("")
    }

    "display 'Back' button that links to 'Declaration Type' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/type")
    }

    "display 'Save and continue' button on page" in {
      val view = createView()
      val saveButton = view.getElementById("submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }

    "display 'Save and return' button on page" in {
      val view = createView()
      val saveButton = view.getElementById("submit_and_return")
      saveButton.text() must be(messages(saveAndReturnCaption))
      saveButton.attr("name") must be(SaveAndReturn.toString)
    }
  }

  "Consignment References View for invalid input" should {

    "display error for empty LRN" in {

      val view =
        createView(ConsignmentReferences.form().fillAndValidate(ConsignmentReferences(Ducr(properDUCR), "")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, lrnEmpty, "#lrn")

      view.select("#error-message-lrn-input").text() must be(messages(lrnEmpty))
    }

    "display error when LRN is longer then 22 characters" in {

      val view = createView(
        ConsignmentReferences
          .form()
          .fillAndValidate(ConsignmentReferences(Ducr(properDUCR), TestHelper.createRandomAlphanumericString(23)))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, lrnLength, "#lrn")

      view.select("#error-message-lrn-input").text() must be(messages(lrnLength))
    }

    "display error when LRN contains special character" in {

      val view =
        createView(ConsignmentReferences.form().fillAndValidate(ConsignmentReferences(Ducr(properDUCR), "#@#$")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, lrnSpecialCharacter, "#lrn")

      view.select("#error-message-lrn-input").text() must be(messages(lrnSpecialCharacter))
    }

    "display error when DUCR is incorrect and LRN empty" in {

      val view =
        createView(ConsignmentReferences.form().fillAndValidate(ConsignmentReferences(Ducr(incorrectDUCR), "")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, ducrError, "#ducr_ducr")
      checkErrorLink(view, 2, lrnEmpty, "#lrn")

      view.select("#error-message-ducr_ducr-input").text() must be(messages(ducrError))
      view.select("#error-message-lrn-input").text() must be(messages(lrnEmpty))
    }

    "display error when DUCR is incorrect and LRN is longer then 22 characters" in {

      val view = createView(
        ConsignmentReferences
          .form()
          .fillAndValidate(ConsignmentReferences(Ducr(incorrectDUCR), TestHelper.createRandomAlphanumericString(23)))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, ducrError, "#ducr_ducr")
      checkErrorLink(view, 2, lrnLength, "#lrn")

      view.select("#error-message-ducr_ducr-input").text() must be(messages(ducrError))
      view.select("#error-message-lrn-input").text() must be(messages(lrnLength))
    }

    "display error when DUCR is incorrect and LRN contains special character" in {

      val view =
        createView(ConsignmentReferences.form().fillAndValidate(ConsignmentReferences(Ducr(incorrectDUCR), "$$%")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, ducrError, "#ducr_ducr")
      checkErrorLink(view, 2, lrnSpecialCharacter, "#lrn")

      view.select("#error-message-ducr_ducr-input").text() must be(messages(ducrError))
      view.select("#error-message-lrn-input").text() must be(messages(lrnSpecialCharacter))
    }
  }

  "Consignment References View when filled" should {

    "display data in DUCR input" in {

      val view =
        createView(ConsignmentReferences.form().fill(ConsignmentReferences(Ducr("9GB12345678901234-SHIP1234-1"), "")))

      view.getElementById("ducr_ducr").attr("value") must be("9GB12345678901234-SHIP1234-1")
      view.getElementById("lrn").attr("value") must be("")
    }

    "display data in LRN input" in {

      val view = createView(ConsignmentReferences.form().fill(ConsignmentReferences(Ducr(""), "test1")))

      view.getElementById("ducr_ducr").attr("value") must be("")
      view.getElementById("lrn").attr("value") must be("test1")
    }

    "display data in all inputs" in {

      val view =
        createView(
          ConsignmentReferences.form().fill(ConsignmentReferences(Ducr("GB/ABC4-ASIUDYFAHSDJF"), "test1", Some("ucr")))
        )

      view.getElementById("ducr_ducr").attr("value") must be("GB/ABC4-ASIUDYFAHSDJF")
      view.getElementById("lrn").attr("value") must be("test1")
      view.getElementById("personalUcr").attr("value") must be("ucr")
    }

    "display error when UCR is longer then 35 characters" in {

      val view = createView(
        ConsignmentReferences
          .form()
          .fillAndValidate(
            ConsignmentReferences(
              Ducr(properDUCR),
              TestHelper.createRandomAlphanumericString(22),
              Some(TestHelper.createRandomAlphanumericString(36))
            )
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, ucrLength, "#personalUcr")

      view.select("#error-message-personalUcr-input").text() must be(messages(ucrLength))
    }

    "display error when UCR contains special character" in {

      val view =
        createView(
          ConsignmentReferences.form().fillAndValidate(ConsignmentReferences(Ducr(properDUCR), "test2", Some("#@#$")))
        )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, ucrSpecialCharacter, "#personalUcr")

      view.select("#error-message-personalUcr-input").text() must be(messages(ucrSpecialCharacter))
    }

  }
}
