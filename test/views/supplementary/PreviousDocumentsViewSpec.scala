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
import forms.supplementary.Document
import helpers.views.supplementary.{CommonMessages, PreviousDocumentsMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.html.supplementary.previous_documents
import views.supplementary.spec.ViewSpec

class PreviousDocumentsViewSpec extends ViewSpec with PreviousDocumentsMessages with CommonMessages {

  private val form: Form[Document] = Document.form()
  private def createView(form: Form[Document] = form): Html = previous_documents(form, Seq())(fakeRequest, messages, appConfig)

  "Previous Documents View" should {

    "have proper messages for labels" in {

      assertMessage(previousDocuments, "Previous documents")
      assertMessage(title, "2/1 Enter previous DUCR or MUCR references linked to these goods")
      assertMessage(hint, "For example, 8GB-123456789101-SHIP1")
      assertMessage(documentX, "Temporary storage")
      assertMessage(documentY, "Simplified declaration")
      assertMessage(documentZ, "Previous document")
      assertMessage(documentType, "Enter previous document code")
      assertMessage(documentReference, "Enter the reference for the previous DUCR or MUCR")
      assertMessage(documentGoodsIdentifier, "Enter the goods item this previous document relates to")
      assertMessage(documentCategoryLabel, "Document Category")
      assertMessage(documentTypeLabel, "Document Type")
      assertMessage(documentReferenceLabel, "Document Reference")
      assertMessage(documentGoodsIdentifierLabel, "Goods Item Identifier")
      assertMessage(removePackageInformation, "Remove Packaging Information")
    }

    "have proper messages for error labels" in {

      assertMessage(documentCategoryEmpty, "Document category cannot be empty")
      assertMessage(documentCategoryError, "Document category is incorrect")
      assertMessage(documentTypeEmpty, "Previous document code cannot be empty")
      assertMessage(documentTypeError, "Previous document code is incorrect")
      assertMessage(documentReferenceEmpty, "Reference for the DUCR or MUCR cannot be empty")
      assertMessage(documentReferenceError, "Reference for the DUCR or MUCR is incorrect")
      assertMessage(documentGoodsIdentifierError, "This field is incorrect")
    }
  }

  "Previous Documents View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(previousDocuments))
    }

    "display header with hint" in {

      val view = createView()

      getElementByCss(view, "legend>h1").text() must be(messages(title))
      getElementByCss(view, "legend>span").text() must be(messages(hint))
    }

    "display three radio buttons with description (not selected)" in {

      val view = createView(Document.form().fill(Document("", "", "", Some(""))))

      val optionOne = getElementById(view, "Temporary storage")
      optionOne.attr("checked") must be("")

      getElementByCss(view, "#documentCategory>div:nth-child(2)>label").text() must be(messages(documentX))

      val optionTwo = getElementById(view, "Simplified declaration")
      optionTwo.attr("checked") must be("")

      getElementByCss(view, "#documentCategory>div:nth-child(3)>label").text() must be(messages(documentY))

      val optionThree = getElementById(view, "Previous document")
      optionThree.attr("checked") must be("")

      getElementByCss(view, "#documentCategory>div:nth-child(4)>label").text() must be(messages(documentZ))
    }

    "display empty input with label for Previous document code" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(4)>label>span").text() must be(messages(documentType))
      getElementById(view, "documentType").attr("value") must be("")
    }

    "display empty input with label for Previous DUCR or MUCR" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(5)>label>span").text() must be(messages(documentReference))
      getElementById(view, "documentReference").attr("value") must be("")
    }

    "display empty input with label for Previous Goods Identifier" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(6)>label>span").text() must be(messages(documentGoodsIdentifier))
      getElementById(view, "documentReference").attr("value") must be("")
    }

    "display \"Back\" button that links to \"Transaction Type\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/supplementary/transaction-type")
    }

    "display both \"Add\" and \"Save and continue\" button on page" in {

      val view = createView()

      val addButton = getElementByCss(view, "#add")
      addButton.text() must be(messages(addCaption))

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Previous Documents View when filled" should {

    "display selected first radio button - Temporary Storage (X)" in {

      val view = createView(Document.form().fill(Document("X", "", "", Some(""))))

      val optionOne = getElementById(view, "Temporary storage")
      optionOne.attr("checked") must be("checked")

      val optionTwo = getElementById(view, "Simplified declaration")
      optionTwo.attr("checked") must be("")

      val optionThree = getElementById(view, "Previous document")
      optionThree.attr("checked") must be("")
    }

    "display selected second radio button - Simplified Declaration (Y)" in {

      val view = createView(Document.form().fill(Document("Y", "", "", Some(""))))

      val optionOne = getElementById(view, "Temporary storage")
      optionOne.attr("checked") must be("")

      val optionTwo = getElementById(view, "Simplified declaration")
      optionTwo.attr("checked") must be("checked")

      val optionThree = getElementById(view, "Previous document")
      optionThree.attr("checked") must be("")
    }

    "display selected third radio button - Previous Documents (Z)" in {

      val view = createView(Document.form().fill(Document("Z", "", "", Some(""))))

      val optionOne = getElementById(view, "Temporary storage")
      optionOne.attr("checked") must be("")

      val optionTwo = getElementById(view, "Simplified declaration")
      optionTwo.attr("checked") must be("")

      val optionThree = getElementById(view, "Previous document")
      optionThree.attr("checked") must be("checked")
    }

    "display data in Document type input" in {

      val view = createView(Document.form().fill(Document("", "Test", "", Some(""))))

      getElementById(view, "documentType").attr("value") must be("Test")
    }

    "display data in Previous DUCR or MUCR input" in {

      val view = createView(Document.form().fill(Document("", "", "Test", Some(""))))

      getElementById(view, "documentReference").attr("value") must be("Test")
    }

    "display data in Previous Goods Identifier input" in {

      val view = createView(Document.form().fill(Document("", "", "", Some("Test"))))

      getElementById(view, "goodsItemIdentifier").attr("value") must be("Test")
    }

    "display all data entered" in {

      val view = createView(Document.form().fill(Document("X", "Test", "Test", Some("Test"))))

      val optionOne = getElementById(view, "Temporary storage")
      optionOne.attr("checked") must be("checked")

      val optionTwo = getElementById(view, "Simplified declaration")
      optionTwo.attr("checked") must be("")

      val optionThree = getElementById(view, "Previous document")
      optionThree.attr("checked") must be("")

      getElementById(view, "documentType").attr("value") must be("Test")
      getElementById(view, "documentReference").attr("value") must be("Test")
      getElementById(view, "goodsItemIdentifier").attr("value") must be("Test")
    }
  }
}
