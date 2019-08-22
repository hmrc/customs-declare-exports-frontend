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
    natureOfTransactionPage(form)(fakeRequest, messages)

  "Nature Of Transaction View on empty page" should {

    "display page title" in {

      getElementById(createView(), "title").text() must be(messages(title))
    }

    "display section header" in {

      getElementById(createView(), "section-header").text() must be(messages(header))
    }

    "display radio button with all possible options" in {

      val view = createView()

      getElementById(view, "Purchase-label").text() must be(messages(purchaseOption, "", ""))
      getElementById(view, "Return-label").text() must be(messages(returnOption, "", ""))
      getElementById(view, "Donation-label").text() must be(messages(donationOption, "", ""))
      getElementById(view, "Processing-label").text() must be(messages(processingOption, "", ""))
      getElementById(view, "Processed-label").text() must be(messages(processedOption, "", ""))
      getElementById(view, "NationalPurposes-label").text() must be(messages(nationalPurposesOption, "", ""))
      getElementById(view, "Military-label").text() must be(messages(militaryOption, "", ""))
      getElementById(view, "Construction-label").text() must be(messages(constructionOption, "", ""))
      getElementById(view, "Other-label").text() must be(messages(otherOption, "", ""))
    }

    "display 'Back' button that links to 'Total Number Of Items' page" in {

      val backButton = getElementById(createView(), "link-back")

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

      getElementById(view, "error-message-natureType-input").text() must be(messages(natureOfTransactionEmpty))
    }

    "display error when nature of transaction is incorrect" in {

      val view = createView(NatureOfTransaction.form().fillAndValidate(NatureOfTransaction("ABC")))

      checkErrorsSummary(view)
      checkErrorLink(view, "natureType-error", natureOfTransactionError, "#natureType")

      getElementById(view, "error-message-natureType-input").text() must be(messages(natureOfTransactionError))
    }
  }
}
