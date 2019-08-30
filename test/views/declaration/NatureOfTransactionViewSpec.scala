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

import forms.declaration.NatureOfTransaction
import helpers.views.declaration.{CommonMessages, NatureOfTransactionMessages}
import models.Mode
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.nature_of_transaction
import views.tags.ViewTest

@ViewTest
class NatureOfTransactionViewSpec extends ViewSpec with NatureOfTransactionMessages with CommonMessages {

  private val form: Form[NatureOfTransaction] = NatureOfTransaction.form()
  private val natureOfTransactionPage = app.injector.instanceOf[nature_of_transaction]
  private def createView(form: Form[NatureOfTransaction] = form): Html =
    natureOfTransactionPage(Mode.Normal, form)(fakeRequest, messages)

  "Nature Of Transaction View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() must be(messages(title))
    }

    "display section header" in {

      createView().getElementById("section-header").text() must be(messages(header))
    }

    "display radio button with all possible options" in {

      val view = createView()

      view.getElementById("Purchase-label").text() must be(messages(purchaseOption, "", ""))
      view.getElementById("Return-label").text() must be(messages(returnOption, "", ""))
      view.getElementById("Donation-label").text() must be(messages(donationOption, "", ""))
      view.getElementById("Processing-label").text() must be(messages(processingOption, "", ""))
      view.getElementById("Processed-label").text() must be(messages(processedOption, "", ""))
      view.getElementById("NationalPurposes-label").text() must be(messages(nationalPurposesOption, "", ""))
      view.getElementById("Military-label").text() must be(messages(militaryOption, "", ""))
      view.getElementById("Construction-label").text() must be(messages(constructionOption, "", ""))
      view.getElementById("Other-label").text() must be(messages(otherOption, "", ""))
    }

    "display 'Back' button that links to 'Total Number Of Items' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/total-numbers-of-items")
    }

    "display 'Save and continue' button on page" in {
      val saveButton = createView().getElementById("submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = createView().getElementById("submit_and_return")
      saveAndReturnButton.text() must be(messages(saveAndReturnCaption))
    }
  }

  "Nature Of Transaction View for invalid input" should {

    "display error when nature of transaction is empty" in {

      val view = createView(NatureOfTransaction.form().fillAndValidate(NatureOfTransaction("")))

      checkErrorsSummary(view)
      checkErrorLink(view, "natureType-error", natureOfTransactionEmpty, "#natureType")

      view.getElementById("error-message-natureType-input").text() must be(messages(natureOfTransactionEmpty))
    }

    "display error when nature of transaction is incorrect" in {

      val view = createView(NatureOfTransaction.form().fillAndValidate(NatureOfTransaction("ABC")))

      checkErrorsSummary(view)
      checkErrorLink(view, "natureType-error", natureOfTransactionError, "#natureType")

      view.getElementById("error-message-natureType-input").text() must be(messages(natureOfTransactionError))
    }
  }
}
