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

import forms.declaration.TransactionType
import helpers.views.declaration.{CommonMessages, TransactionTypeMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.transaction_type
import views.tags.ViewTest

@ViewTest
class TransactionTypeViewSpec extends ViewSpec with TransactionTypeMessages with CommonMessages {

  private val form: Form[TransactionType] = TransactionType.form()
  private def createView(form: Form[TransactionType] = form): Html =
    transaction_type(appConfig, form)(fakeRequest, messages)

  "Transaction Type View" should {

    "have proper labels for messages" in {

      assertMessage(title, "Transaction type")
      assertMessage(header, "8/5 Transaction type")
      assertMessage(description, "What kind of transaction are you making?")
      assertMessage(hint, "This is single digit numerical character")
      assertMessage(identifier, "Further information on this transaction")
    }

    "have proper labels for error messages" in {

      assertMessage(documentTypeCodeEmpty, "Document type code cannot be empty")
      assertMessage(documentTypeCodeError, "Document type code is incorrect")
      assertMessage(identifierError, "Further information on this transaction are incorrect")
    }
  }

  "Transaction Type View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title))
    }

    "display header" in {

      getElementByCss(createView(), "legend>h1").text() must be(messages(header))
    }

    "display empty input with label for Document Type Code" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(3)>label>span:nth-child(1)").text() must be(messages(description))
      getElementByCss(view, "form>div:nth-child(3)>label>span.form-hint").text() must be(messages(hint))
      getElementById(view, "documentTypeCode").attr("value") must be("")
    }

    "display empty input with label for Identifier" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(4)>label>span:nth-child(1)").text() must be(messages(identifier))
      getElementByCss(view, "form>div:nth-child(4)>label>span.form-hint").text() must be(messages(hint))
      getElementById(view, "identifier").attr("value") must be("")
    }

    "display \"Back\" button that links to \"Total Number Of Items\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/total-numbers-of-items")
    }

    "display \"Save and continue\" button on page" in {

      val view = createView()

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Transaction Type View for invalid input" should {

    "display error when Document Type Code is empty" in {

      val view = createView(TransactionType.form().fillAndValidate(TransactionType("", Some("1"))))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentTypeCodeEmpty, "#documentTypeCode")

      getElementByCss(view, "#error-message-documentTypeCode-input").text() must be(messages(documentTypeCodeEmpty))
    }

    "display error when Document Type Code is incorrect" in {

      val view = createView(TransactionType.form().fillAndValidate(TransactionType("ABC", Some("1"))))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentTypeCodeError, "#documentTypeCode")

      getElementByCss(view, "#error-message-documentTypeCode-input").text() must be(messages(documentTypeCodeError))
    }

    "display error when Identifier is incorrect" in {

      val view = createView(TransactionType.form().fillAndValidate(TransactionType("1", Some("ABC"))))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, identifierError, "#identifier")

      getElementByCss(view, "#error-message-identifier-input").text() must be(messages(identifierError))
    }

    "display error when both inputs are incorrect" in {

      val view = createView(TransactionType.form().fillAndValidate(TransactionType("ABC", Some("ABC"))))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentTypeCodeError, "#documentTypeCode")
      checkErrorLink(view, 2, identifierError, "#identifier")

      getElementByCss(view, "#error-message-documentTypeCode-input").text() must be(messages(documentTypeCodeError))
      getElementByCss(view, "#error-message-identifier-input").text() must be(messages(identifierError))
    }

    "display error when both inputs are empty" in {

      val view = createView(TransactionType.form().fillAndValidate(TransactionType("", None)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentTypeCodeEmpty, "#documentTypeCode")

      getElementByCss(view, "#error-message-documentTypeCode-input").text() must be(messages(documentTypeCodeEmpty))
    }
  }

  "Transaction Type View when filled" should {

    "display data in Document Type Code input" in {

      val view = createView(TransactionType.form().fill(TransactionType("1", None)))

      getElementById(view, "documentTypeCode").attr("value") must be("1")
      getElementById(view, "identifier").attr("value") must be("")
    }

    "display data in Identifier input" in {

      val view = createView(TransactionType.form().fill(TransactionType("", Some("1"))))

      getElementById(view, "documentTypeCode").attr("value") must be("")
      getElementById(view, "identifier").attr("value") must be("1")
    }

    "display data in both inputs" in {

      val view = createView(TransactionType.form().fill(TransactionType("1", Some("1"))))

      getElementById(view, "documentTypeCode").attr("value") must be("1")
      getElementById(view, "identifier").attr("value") must be("1")
    }
  }
}
