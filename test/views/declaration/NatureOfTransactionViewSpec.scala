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

  "Nature Of Transaction View" should {

    "have proper labels for messages" in {

      assertMessage(pageTitle, "Nature of transaction")
      assertMessage(header, "Consignment information")
      assertMessage(title, "What was the nature of the transaction?")
      assertMessage(purchaseOption, "{0}Purchase{1} - these goods were bought outright")
      assertMessage(returnOption, "{0}Return{1} - these goods need to be returned to the sender")
      assertMessage(donationOption, "{0}Donation{1} - these goods were donated with no payment involved")
      assertMessage(processingOption, "{0}Processing{1} - these goods will be altered and returned")
      assertMessage(processedOption, "{0}Processed{1} - these goods have been altered and returned")
      assertMessage(
        nationalPurposesOption,
        "{0}National purposes{1} - these goods will be used for a specific national purpose"
      )
      assertMessage(militaryOption, "{0}Military{1} - these goods are for military or inter-governmental purposes")
      assertMessage(
        constructionOption,
        "{0}Construction{1} - these goods are under a general construction or civil engineering contract"
      )
      assertMessage(otherOption, "{0}Other{1}")
    }

    "have proper labels for error messages" in {

      assertMessage(natureOfTransactionEmpty, "Please choose nature of transaction")
      assertMessage(natureOfTransactionError, "Nature of transaction is incorrect")
    }
  }

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

      val view = createView()

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
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
