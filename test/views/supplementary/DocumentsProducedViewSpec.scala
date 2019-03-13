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

import forms.supplementary.DocumentsProduced
import play.api.data.Form
import play.twirl.api.Html
import views.helpers.{Item, ViewSpec}
import views.html.supplementary.documents_produced
import views.tags.ViewTest

@ViewTest
class DocumentsProducedViewSpec extends ViewSpec {

  private val form: Form[DocumentsProduced] = DocumentsProduced.form()
  private val filledForm = DocumentsProduced(Some("test"), Some("test1"), Some("test2"), Some("test3"), Some("test4"), Some("test5"))

  private val prefix = s"${basePrefix}addDocument."

  private val title = Item(prefix, "title")
  private val hint = Item(prefix, "hint")
  private val documentTypeCode = Item(prefix, "documentTypeCode")
  private val documentIdentifier = Item(prefix, "documentIdentifier")
  private val documentPart = Item(prefix, "documentPart")
  private val documentStatus = Item(prefix, "documentStatus")
  private val documentStatusReason = Item(prefix, "documentStatusReason")
  private val documentQuantity = Item(prefix, "documentQuantity")
  private val maximumAmount = Item(prefix, "maximumAmount")
  private val duplicated = Item(prefix, "duplicated")
  private val notDefined = Item(prefix, "isNotDefined")

  private def createView(form: Form[DocumentsProduced] = form): Html = documents_produced(appConfig, form, Seq())(fakeRequest, messages)

  "Documents Produced View" should {

    "have proper messages for labels" in {

      assertMessage(title.withPrefix, "2/3 Do you need to add any documents?")
      assertMessage(hint.withPrefix, "Including certificates, authorisations or additional references")
      assertMessage(documentTypeCode.withPrefix, "Document type code")
      assertMessage(documentIdentifier.withPrefix, "Document identifier")
      assertMessage(documentPart.withPrefix, "Document part")
      assertMessage(documentStatus.withPrefix, "Document status")
      assertMessage(documentStatusReason.withPrefix, "Document status reason")
      assertMessage(documentQuantity.withPrefix, "Quantity")
    }

    "have proper messages for error labels" in {

      assertMessage(documentTypeCode.withError, "Incorrect document type code")
      assertMessage(documentIdentifier.withError, "Incorrect document identifier")
      assertMessage(documentPart.withError, "Incorrect document part")
      assertMessage(documentStatus.withError, "Incorrect document status")
      assertMessage(documentStatusReason.withError, "Incorrect document status reason")
    }
  }

  "Documents Produced View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title.withPrefix))
    }

    "display header with hint" in {

      val view = createView()

      getElementByCss(view, "legend>h1").text() must be(messages(title.withPrefix))
      getElementByCss(view, "legend>span").text() must be(messages(hint.withPrefix))
    }

    "display empty input with label for Document type code" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(3)>label>span").text() must be(messages(documentTypeCode.withPrefix))
      getElementById(view, documentTypeCode.key).attr("value") must be("")
    }

    "display empty input with label for Document identifier" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(4)>label>span").text() must be(messages(documentIdentifier.withPrefix))
      getElementById(view, documentIdentifier.key).attr("value") must be("")
    }

    "display empty input with label for Document part" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(5)>label>span").text() must be(messages(documentPart.withPrefix))
      getElementById(view, documentPart.key).attr("value") must be("")
    }

    "display empty input with label for Document status" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(6)>label>span").text() must be(messages(documentStatus.withPrefix))
      getElementById(view, documentStatus.key).attr("value") must be("")
    }

    "display empty input with label for Document status reason" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(7)>label>span").text() must be(
        messages(documentStatusReason.withPrefix)
      )
      getElementById(view, documentStatusReason.key).attr("value") must be("")
    }

    "display empty input with label for Document quantity" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(8)>label>span").text() must be(messages(documentQuantity.withPrefix))
      getElementById(view, documentQuantity.key).attr("value") must be("")
    }

    "display \"Back\" button that links to \"Additional Information\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be("Back")
      backButton.attr("href") must be("/customs-declare-exports/declaration/supplementary/additional-information")
    }

    "display both \"Add\" and \"Save and continue\" button on page" in {

      val view = createView()

      val addButton = getElementByCss(view, "#add")
      addButton.text() must be("Add")

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be("Save and continue")
    }
  }

  "Documents Produced View for invalid input" should {

    "display error for Document type code" in {

      val view = createView(DocumentsProduced.form().withError(documentTypeCode.key, messages(documentTypeCode.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentTypeCode.withError, documentTypeCode.asLink)

      getElementByCss(view, "#error-message-documentTypeCode-input").text() must be(
        messages(documentTypeCode.withError)
      )
    }

    "display error for Document identifier" in {

      val view = createView(DocumentsProduced.form().withError(documentIdentifier.key, messages(documentIdentifier.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentIdentifier.withError, documentIdentifier.asLink)

      getElementByCss(view, "#error-message-documentIdentifier-input").text() must be(
        messages(documentIdentifier.withError)
      )
    }

    "display error for Document part" in {

      val view = createView(DocumentsProduced.form().withError(documentPart.key, messages(documentPart.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentPart.withError, documentPart.asLink)

      getElementByCss(view, "#error-message-documentPart-input").text() must be(messages(documentPart.withError))
    }

    "display error for Document status" in {

      val view = createView(DocumentsProduced.form().withError(documentStatus.key, messages(documentStatus.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentStatus.withError, documentStatus.asLink)

      getElementByCss(view, "#error-message-documentStatus-input").text() must be(messages(documentStatus.withError))
    }

    "display error for Document status reason" in {

      val view = createView(DocumentsProduced.form().withError(documentStatusReason.key, messages(documentStatusReason.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentStatusReason.withError, documentStatusReason.asLink)

      getElementByCss(view, "#error-message-documentStatusReason-input").text() must be(
        messages(documentStatusReason.withError)
      )
    }

    "display error for Document quantity" in {

      val view = createView(DocumentsProduced.form().withError(documentQuantity.key, messages(documentQuantity.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentQuantity.withError, documentQuantity.asLink)

      getElementByCss(view, "#error-message-documentQuantity-input").text() must be(
        messages(documentQuantity.withError)
      )
    }

    "display error for duplicated document" in {

      val view = createView(DocumentsProduced.form().withError("", messages(duplicated.withPrefix)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, duplicated.withPrefix, "#")
    }

    "display error for more then 99 documents" in {

      val view = createView(DocumentsProduced.form().withError("", messages(maximumAmount.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, maximumAmount.withError, "#")
    }

    "display error for trying to save empty document" in {

      val view = createView(DocumentsProduced.form().withError("", messages(notDefined.withPrefix)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, notDefined.withPrefix, "#")
    }

    "display errors for all fields" in {

      val form = DocumentsProduced.form()
        .withError(documentTypeCode.key, messages(documentTypeCode.withError))
        .withError(documentIdentifier.key, messages(documentIdentifier.withError))
        .withError(documentPart.key, messages(documentPart.withError))
        .withError(documentStatus.key, messages(documentStatus.withError))
        .withError(documentStatusReason.key, messages(documentStatusReason.withError))
        .withError(documentQuantity.key, messages(documentQuantity.withError))

      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentTypeCode.withError, documentTypeCode.asLink)
      checkErrorLink(view, 2, documentIdentifier.withError, documentIdentifier.asLink)
      checkErrorLink(view, 3, documentPart.withError, documentPart.asLink)
      checkErrorLink(view, 4, documentStatus.withError, documentStatus.asLink)
      checkErrorLink(view, 5, documentStatusReason.withError, documentStatusReason.asLink)
      checkErrorLink(view, 6, documentQuantity.withError, documentQuantity.asLink)

      getElementByCss(view, "#error-message-documentTypeCode-input").text() must be(
        messages(documentTypeCode.withError)
      )
      getElementByCss(view, "#error-message-documentIdentifier-input").text() must be(
        messages(documentIdentifier.withError)
      )
      getElementByCss(view, "#error-message-documentPart-input").text() must be(messages(documentPart.withError))
      getElementByCss(view, "#error-message-documentStatus-input").text() must be(messages(documentStatus.withError))
      getElementByCss(view, "#error-message-documentStatusReason-input").text() must be(
        messages(documentStatusReason.withError)
      )
      getElementByCss(view, "#error-message-documentQuantity-input").text() must be(
        messages(documentQuantity.withError)
      )
    }
  }

  "Documents Produced View when filled" should {

    "display data in both inputs" in {

      val form = DocumentsProduced.form().fill(filledForm)
      val view = createView(form)

      getElementById(view, documentTypeCode.key).attr("value") must be("test")
      getElementById(view, documentIdentifier.key).attr("value") must be("test1")
      getElementById(view, documentPart.key).attr("value") must be("test2")
      getElementById(view, documentStatus.key).attr("value") must be("test3")
      getElementById(view, documentStatusReason.key).attr("value") must be("test4")
      getElementById(view, documentQuantity.key).attr("value") must be("test5")
    }

    "display one item in table" in {

      val view = documents_produced(appConfig, form, Seq(filledForm))(fakeRequest, messages)

      getElementByCss(view, "th:nth-child(1)").text() must be(messages(documentTypeCode.withPrefix))
      getElementByCss(view, "th:nth-child(2)").text() must be(messages(documentIdentifier.withPrefix))
      getElementByCss(view, "th:nth-child(3)").text() must be(messages(documentPart.withPrefix))
      getElementByCss(view, "th:nth-child(4)").text() must be(messages(documentStatus.withPrefix))
      getElementByCss(view, "th:nth-child(5)").text() must be(messages(documentStatusReason.withPrefix))
      getElementByCss(view, "th:nth-child(6)").text() must be(messages(documentQuantity.withPrefix))

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
