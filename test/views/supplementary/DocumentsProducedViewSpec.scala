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

import base.TestHelper
import forms.supplementary.DocumentsProduced
import helpers.{CommonMessages, DocumentsProducedMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.html.supplementary.documents_produced
import views.supplementary.spec.ViewSpec
import views.tags.ViewTest

@ViewTest
class DocumentsProducedViewSpec extends ViewSpec with DocumentsProducedMessages with CommonMessages {

  private val form: Form[DocumentsProduced] = DocumentsProduced.form()
  private val filledForm = DocumentsProduced(Some("test"), Some("test1"), Some("test2"), Some("test3"), Some("test4"), Some("test5"))

  private def createView(form: Form[DocumentsProduced] = form): Html = documents_produced(appConfig, form, Seq())(fakeRequest, messages)

  "Documents Produced View" should {

    "have proper messages for labels" in {

      assertMessage(title, "2/3 Do you need to add any documents?")
      assertMessage(hint, "Including certificates, authorisations or additional references")
      assertMessage(documentTypeCode, "Document type code")
      assertMessage(documentIdentifier, "Document identifier")
      assertMessage(documentPart, "Document part")
      assertMessage(documentStatus, "Document status")
      assertMessage(documentStatusReason, "Document status reason")
      assertMessage(documentQuantity, "Quantity")
    }

    "have proper messages for error labels" in {

      assertMessage(documentTypeCodeError, "Incorrect document type code")
      assertMessage(documentIdentifierError, "Incorrect document identifier")
      assertMessage(documentPartError, "Incorrect document part")
      assertMessage(documentStatusError, "Incorrect document status")
      assertMessage(documentStatusReasonError, "Incorrect document status reason")
      assertMessage(maximumAmountReached, "You cannot have more than 99 documents")
      assertMessage(duplicatedItem, "You cannot add an already existent document")
      assertMessage(notDefined, "Please provide some document information")
    }
  }

  "Documents Produced View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title))
    }

    "display header with hint" in {

      val view = createView()

      getElementByCss(view, "legend>h1").text() must be(messages(title))
      getElementByCss(view, "legend>span").text() must be(messages(hint))
    }

    "display empty input with label for Document type code" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(3)>label>span").text() must be(messages(documentTypeCode))
      getElementById(view, "documentTypeCode").attr("value") must be("")
    }

    "display empty input with label for Document identifier" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(4)>label>span").text() must be(messages(documentIdentifier))
      getElementById(view, "documentIdentifier").attr("value") must be("")
    }

    "display empty input with label for Document part" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(5)>label>span").text() must be(messages(documentPart))
      getElementById(view, "documentPart").attr("value") must be("")
    }

    "display empty input with label for Document status" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(6)>label>span").text() must be(messages(documentStatus))
      getElementById(view, "documentStatus").attr("value") must be("")
    }

    "display empty input with label for Document status reason" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(7)>label>span").text() must be(messages(documentStatusReason))
      getElementById(view, "documentStatusReason").attr("value") must be("")
    }

    "display empty input with label for Document quantity" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(8)>label>span").text() must be(messages(documentQuantity))
      getElementById(view, "documentQuantity").attr("value") must be("")
    }

    "display \"Back\" button that links to \"Additional Information\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/supplementary/additional-information")
    }

    "display both \"Add\" and \"Save and continue\" button on page" in {

      val view = createView()

      val addButton = getElementByCss(view, "#add")
      addButton.text() must be(messages(addCaption))

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Documents Produced View for invalid input" should {

    "display error for Document type code" in {

      val view = createView(DocumentsProduced.form().fillAndValidate(DocumentsProduced(
        Some(TestHelper.createRandomString(5)),
        Some("1234"),
        Some("1234"),
        Some("AV"),
        Some("1234"),
        Some("1234")
      )))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentTypeCodeError, "#documentTypeCode")

      getElementByCss(view, "#error-message-documentTypeCode-input").text() must be(messages(documentTypeCodeError))
    }

    "display error for Document identifier" in {

      val view = createView(DocumentsProduced.form().fillAndValidate(DocumentsProduced(
        Some("1234"),
        Some(TestHelper.createRandomString(31)),
        Some("1234"),
        Some("AV"),
        Some("1234"),
        Some("1234")
      )))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentIdentifierError, "#documentIdentifier")

      getElementByCss(view, "#error-message-documentIdentifier-input").text() must be(messages(documentIdentifierError))
    }

    "display error for Document part" in {

      val view = createView(DocumentsProduced.form().fillAndValidate(DocumentsProduced(
        Some("1234"),
        Some("1234"),
        Some(TestHelper.createRandomString(6)),
        Some("AV"),
        Some("1234"),
        Some("1234")
      )))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentPartError, "#documentPart")

      getElementByCss(view, "#error-message-documentPart-input").text() must be(messages(documentPartError))
    }

    "display error for Document status" in {

      val view = createView(DocumentsProduced.form().fillAndValidate(DocumentsProduced(
        Some("1234"),
        Some("1234"),
        Some("1234"),
        Some("ABC"),
        Some("1234"),
        Some("1234")
      )))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentStatusError, "#documentStatus")

      getElementByCss(view, "#error-message-documentStatus-input").text() must be(messages(documentStatusError))
    }

    "display error for Document status reason" in {

      val view = createView(DocumentsProduced.form().fillAndValidate(DocumentsProduced(
        Some("1234"),
        Some("1234"),
        Some("1234"),
        Some("AV"),
        Some(TestHelper.createRandomString(36)),
        Some("1234")
      )))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentStatusReasonError, "#documentStatusReason")

      getElementByCss(view, "#error-message-documentStatusReason-input").text() must be(messages(documentStatusReasonError))
    }

    "display error for Document quantity" in {

      val view = createView(DocumentsProduced.form().fillAndValidate(DocumentsProduced(
        Some("1234"),
        Some("1234"),
        Some("1234"),
        Some("AV"),
        Some("1234"),
        Some("12345678901234567")
      )))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentQuantityError, "#documentQuantity")

      getElementByCss(view, "#error-message-documentQuantity-input").text() must be(messages(documentQuantityError))
    }

    "display errors for all fields" in {

      val form = DocumentsProduced.form().fillAndValidate(DocumentsProduced(
        Some(TestHelper.createRandomString(5)),
        Some(TestHelper.createRandomString(31)),
        Some(TestHelper.createRandomString(6)),
        Some("ABC"),
        Some(TestHelper.createRandomString(36)),
        Some("12345678901234567")
      ))

      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentTypeCodeError, "#documentTypeCode")
      checkErrorLink(view, 2, documentIdentifierError, "#documentIdentifier")
      checkErrorLink(view, 3, documentPartError, "#documentPart")
      checkErrorLink(view, 4, documentStatusError, "#documentStatus")
      checkErrorLink(view, 5, documentStatusReasonError, "#documentStatusReason")
      checkErrorLink(view, 6, documentQuantityError, "#documentQuantity")

      getElementByCss(view, "#error-message-documentTypeCode-input").text() must be(messages(documentTypeCodeError))
      getElementByCss(view, "#error-message-documentIdentifier-input").text() must be(messages(documentIdentifierError))
      getElementByCss(view, "#error-message-documentPart-input").text() must be(messages(documentPartError))
      getElementByCss(view, "#error-message-documentStatus-input").text() must be(messages(documentStatusError))
      getElementByCss(view, "#error-message-documentStatusReason-input").text() must be(messages(documentStatusReasonError))
      getElementByCss(view, "#error-message-documentQuantity-input").text() must be(messages(documentQuantityError))
    }
  }

  "Documents Produced View when filled" should {

    "display data in both inputs" in {

      val form = DocumentsProduced.form().fill(filledForm)
      val view = createView(form)

      getElementById(view, "documentTypeCode").attr("value") must be("test")
      getElementById(view, "documentIdentifier").attr("value") must be("test1")
      getElementById(view, "documentPart").attr("value") must be("test2")
      getElementById(view, "documentStatus").attr("value") must be("test3")
      getElementById(view, "documentStatusReason").attr("value") must be("test4")
      getElementById(view, "documentQuantity").attr("value") must be("test5")
    }

    "display one item in table" in {

      val view = documents_produced(appConfig, form, Seq(filledForm))(fakeRequest, messages)

      getElementByCss(view, "th:nth-child(1)").text() must be(messages(documentTypeCode))
      getElementByCss(view, "th:nth-child(2)").text() must be(messages(documentIdentifier))
      getElementByCss(view, "th:nth-child(3)").text() must be(messages(documentPart))
      getElementByCss(view, "th:nth-child(4)").text() must be(messages(documentStatus))
      getElementByCss(view, "th:nth-child(5)").text() must be(messages(documentStatusReason))
      getElementByCss(view, "th:nth-child(6)").text() must be(messages(documentQuantity))

      getElementByCss(view, "tr>td:nth-child(1)").text() must be("test")
      getElementByCss(view, "tr>td:nth-child(2)").text() must be("test1")
      getElementByCss(view, "tr>td:nth-child(3)").text() must be("test2")
      getElementByCss(view, "tr>td:nth-child(4)").text() must be("test3")
      getElementByCss(view, "tr>td:nth-child(5)").text() must be("test4")
      getElementByCss(view, "tr>td:nth-child(6)").text() must be("test5")

      val removeButton = getElementByCss(view, "tbody>tr>td:nth-child(7)>button")

      removeButton.text() must be("Remove")
      removeButton.attr("value") must be(filledForm.toJson.toString())
    }
  }
}
