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
import forms.declaration.Document
import helpers.views.declaration.{CommonMessages, PreviousDocumentsMessages}
import models.Mode
import play.api.data.Form
import play.twirl.api.Html
import views.html.declaration.previous_documents
import views.declaration.spec.ViewSpec
import views.tags.ViewTest

@ViewTest
class PreviousDocumentsViewSpec extends ViewSpec with PreviousDocumentsMessages with CommonMessages {

  private val form: Form[Document] = Document.form()
  private val previousDocumentsPage = app.injector.instanceOf[previous_documents]
  private def createView(form: Form[Document] = form): Html =
    previousDocumentsPage(Mode.Normal, form, Seq())(fakeRequest, messages)

  "Previous Documents View on empty page" should {

    "display page title" in {

      createView().select("title").text() must be(messages(previousDocuments))
    }

    "display section header" in {

      createView().getElementById("section-header").text() must be("Your references")
    }

    "display header with hint" in {

      val view = createView()

      view.select("legend>h1").text() must be(messages(title))
      view.select("legend>span").text() must be(messages(hint))
    }

    "display three radio buttons with description (not selected)" in {

      val view = createView(Document.form.fill(Document("", "", "", Some(""))))

      val optionOne = view.getElementById("Temporary storage")
      optionOne.attr("checked") must be("")

      view.getElementById("Temporary storage-label").text() must be(messages(documentX))

      val optionTwo = view.getElementById("Simplified declaration")
      optionTwo.attr("checked") must be("")

      view.getElementById("Simplified declaration-label").text() must be(messages(documentY))

      val optionThree = view.getElementById("Related document")
      optionThree.attr("checked") must be("")

      view.getElementById("Related document-label").text() must be(messages(documentZ))
    }

    "display empty input with label for Previous document code" in {

      val view = createView()

      view.getElementById("documentType-label").text() must be(messages(documentType))
      view.getElementById("documentType").attr("value") must be("")
    }

    "display empty input with label for Previous DUCR or MUCR" in {

      val view = createView()

      view.getElementById("documentReference-label").text() must be(messages(documentReference))
      view.getElementById("documentReference").attr("value") must be("")
    }

    "display empty input with label for Previous Goods Identifier" in {

      val view = createView()

      view.getElementById("goodsItemIdentifier-label").text() must be(messages(documentGoodsIdentifier))
      view.getElementById("goodsItemIdentifier").attr("value") must be("")
    }

    "display 'Back' button that links to 'Transaction Type' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/nature-of-transaction")
    }

    "display both 'Add' and 'Save and continue' button on page" in {
      val view = createView()

      val addButton = view.getElementById("add")
      addButton.text() must be(messages(addCaption))

      val saveButton = view.getElementById("submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = createView().getElementById("submit_and_return")
      saveAndReturnButton.text() must be(messages(saveAndReturnCaption))
    }
  }

  "Previous Documents View when filled" should {

    "display selected first radio button - Temporary Storage (X)" in {

      val view = createView(Document.form.fill(Document("X", "", "", Some(""))))

      val optionOne = view.getElementById("Temporary storage")
      optionOne.attr("checked") must be("checked")

      val optionTwo = view.getElementById("Simplified declaration")
      optionTwo.attr("checked") must be("")

      val optionThree = view.getElementById("Related document")
      optionThree.attr("checked") must be("")
    }

    "display selected second radio button - Simplified Declaration (Y)" in {

      val view = createView(Document.form.fill(Document("Y", "", "", Some(""))))

      val optionOne = view.getElementById("Temporary storage")
      optionOne.attr("checked") must be("")

      val optionTwo = view.getElementById("Simplified declaration")
      optionTwo.attr("checked") must be("checked")

      val optionThree = view.getElementById("Related document")
      optionThree.attr("checked") must be("")
    }

    "display selected third radio button - Previous Documents (Z)" in {

      val view = createView(Document.form.fill(Document("Z", "", "", Some(""))))

      val optionOne = view.getElementById("Temporary storage")
      optionOne.attr("checked") must be("")

      val optionTwo = view.getElementById("Simplified declaration")
      optionTwo.attr("checked") must be("")

      val optionThree = view.getElementById("Related document")
      optionThree.attr("checked") must be("checked")
    }

    "display data in Document type input" in {

      val view = createView(Document.form.fill(Document("", "Test", "", Some(""))))

      view.getElementById("documentType").attr("value") must be("Test")
      view.getElementById("documentReference").attr("value") must be("")
      view.getElementById("goodsItemIdentifier").attr("value") must be("")
    }

    "display data in Previous DUCR or MUCR input" in {

      val view = createView(Document.form.fill(Document("", "", "Test", Some(""))))

      view.getElementById("documentType").attr("value") must be("")
      view.getElementById("documentReference").attr("value") must be("Test")
      view.getElementById("goodsItemIdentifier").attr("value") must be("")
    }

    "display data in Previous Goods Identifier input" in {

      val view = createView(Document.form.fill(Document("", "", "", Some("Test"))))

      view.getElementById("documentType").attr("value") must be("")
      view.getElementById("documentReference").attr("value") must be("")
      view.getElementById("goodsItemIdentifier").attr("value") must be("Test")
    }

    "display all data entered" in {

      val view = createView(Document.form.fill(Document("X", "Test", "Test", Some("Test"))))

      val optionOne = view.getElementById("Temporary storage")
      optionOne.attr("checked") must be("checked")

      val optionTwo = view.getElementById("Simplified declaration")
      optionTwo.attr("checked") must be("")

      val optionThree = view.getElementById("Related document")
      optionThree.attr("checked") must be("")

      view.getElementById("documentType").attr("value") must be("Test")
      view.getElementById("documentReference").attr("value") must be("Test")
      view.getElementById("goodsItemIdentifier").attr("value") must be("Test")
    }

    "display one row with data in table" in {

      val prevDocuments = Seq(Document("X", "1", "A", Some("1")))
      val view = previousDocumentsPage(Mode.Normal, form, prevDocuments)(fakeRequest, messages)

      // table header
      view.select("form>table>caption").text() must be(messages(previousDocuments))
      view.select("form>table>thead>tr>th:nth-child(1)").text() must be(messages(documentCategoryLabel))
      view.select("form>table>thead>tr>th:nth-child(2)").text() must be(messages(documentTypeLabel))
      view.select("form>table>thead>tr>th:nth-child(3)").text() must be(messages(documentReferenceLabel))
      view.select("form>table>thead>tr>th:nth-child(4)").text() must be(messages(documentGoodsIdentifierLabel))
      view.select("form>table>thead>tr>th:nth-child(5)").text() must be(messages(removePackageInformation))

      // row
      view.select("form>table>tbody>tr>td:nth-child(1)").text() must be(messages(documentX))
      view.select("form>table>tbody>tr>td:nth-child(2)").text() must be("1")
      view.select("form>table>tbody>tr>td:nth-child(3)").text() must be("A")
      view.select("form>table>tbody>tr>td:nth-child(4)").text() must be("1")
      view.select("form>table>tbody>tr>td:nth-child(5)>button").text() must be(messages(removeCaption))
    }
  }
}
