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
import base.Injector
import forms.declaration.Document
import models.Mode
import org.jsoup.nodes.{Document => JsonDocument}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.previous_documents
import views.tags.ViewTest

@ViewTest
class PreviousDocumentsViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = new previous_documents(mainTemplate)
  private val form: Form[Document] = Document.form()
  private def createView(
    mode: Mode = Mode.Normal,
    form: Form[Document] = form,
    documents: Seq[Document] = Seq.empty
  ): JsonDocument =
    page(mode, form, documents)(journeyRequest(), stubMessages())

  "Previous Documents View on empty page" should {
    val view = createView()

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("supplementary.previousDocuments")
      messages must haveTranslationFor("supplementary.previousDocuments.documentCategory.label")
      messages must haveTranslationFor("supplementary.previousDocuments.documentType.label")
      messages must haveTranslationFor("supplementary.previousDocuments.documentReference.label")
      messages must haveTranslationFor("supplementary.previousDocuments.goodsItemIdentifier.label")
      messages must haveTranslationFor("supplementary.packageInformation.remove")
      messages must haveTranslationFor("supplementary.previousDocuments.X")
      messages must haveTranslationFor("supplementary.previousDocuments.Y")
      messages must haveTranslationFor("supplementary.previousDocuments.Z")
      messages must haveTranslationFor("site.remove")
      messages must haveTranslationFor("supplementary.consignmentReferences.heading")
      messages must haveTranslationFor("supplementary.previousDocuments.documentType")
      messages must haveTranslationFor("supplementary.previousDocuments.documentReference")
      messages must haveTranslationFor("supplementary.previousDocuments.goodsItemIdentifier")
      messages must haveTranslationFor("supplementary.previousDocuments.goodsItemIdentifier.hint")
      messages must haveTranslationFor("supplementary.previousDocuments.goodsItemIdentifier.error")
      messages must haveTranslationFor("supplementary.previousDocuments.documentCategory.label")
    }

    "display page title" in {
      view.select("title").text() must be("supplementary.previousDocuments")
    }

    "display section header" in {
      view.getElementById("section-header").text() must be("supplementary.consignmentReferences.heading")
    }

    "display header with hint" in {
      view.select("legend>h1").text() must be("supplementary.previousDocuments.title")
      view.select("legend>span").text() must include("")
    }

    "display three radio buttons with description (not selected)" in {

      val view = createView(form = Document.form.fill(Document("", "", "", Some(""))))

      val optionOne = view.getElementById("Temporary storage")
      optionOne.attr("checked") must be("")

      view.getElementById("Temporary storage-label").text() must be("supplementary.previousDocuments.X")

      val optionTwo = view.getElementById("Simplified declaration")
      optionTwo.attr("checked") must be("")

      view.getElementById("Simplified declaration-label").text() must be("supplementary.previousDocuments.Y")

      val optionThree = view.getElementById("Related document")
      optionThree.attr("checked") must be("")

      view.getElementById("Related document-label").text() must be("supplementary.previousDocuments.Z")
    }

    "display empty input with label for Previous document code" in {
      view.getElementById("documentType-label").text() must be("supplementary.previousDocuments.documentType")
      view.getElementById("documentType").attr("value") must be("")
    }

    "display empty input with label for Previous DUCR or MUCR" in {
      view.getElementById("documentReference-label").text() must be("supplementary.previousDocuments.documentReference")
      view.getElementById("documentReference").attr("value") must be("")
    }

    "display empty input with label for Previous Goods Identifier" in {
      view.getElementById("goodsItemIdentifier-label").text() must be(
        "supplementary.previousDocuments.goodsItemIdentifier"
      )
      view.getElementById("goodsItemIdentifier").attr("value") must be("")
    }

    "display 'Back' button that links to 'Transaction Type' page" in {

      val backButton = view.getElementById("link-back")

      backButton.text() must be("site.back")
      backButton.getElementById("link-back") must haveHref(
        controllers.declaration.routes.NatureOfTransactionController.displayPage(Mode.Normal)
      )
    }

    "display both 'Add' and 'Save and continue' button on page" in {
      val addButton = view.getElementById("add")
      addButton.text() must be("site.add")

      val saveButton = view.getElementById("submit")
      saveButton.text() must be("site.save_and_continue")
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton.text() must be("site.save_and_come_back_later")
    }
  }

  "Previous Documents View when filled" should {

    "display selected first radio button - Temporary Storage (X)" in {

      val view = createView(form = Document.form.fill(Document("X", "", "", Some(""))))

      val optionOne = view.getElementById("Temporary storage")
      optionOne.attr("checked") must be("checked")

      val optionTwo = view.getElementById("Simplified declaration")
      optionTwo.attr("checked") must be("")

      val optionThree = view.getElementById("Related document")
      optionThree.attr("checked") must be("")
    }

    "display selected second radio button - Simplified Declaration (Y)" in {

      val view = createView(form = Document.form.fill(Document("Y", "", "", Some(""))))

      val optionOne = view.getElementById("Temporary storage")
      optionOne.attr("checked") must be("")

      val optionTwo = view.getElementById("Simplified declaration")
      optionTwo.attr("checked") must be("checked")

      val optionThree = view.getElementById("Related document")
      optionThree.attr("checked") must be("")
    }

    "display selected third radio button - Previous Documents (Z)" in {

      val view = createView(form = Document.form.fill(Document("Z", "", "", Some(""))))

      val optionOne = view.getElementById("Temporary storage")
      optionOne.attr("checked") must be("")

      val optionTwo = view.getElementById("Simplified declaration")
      optionTwo.attr("checked") must be("")

      val optionThree = view.getElementById("Related document")
      optionThree.attr("checked") must be("checked")
    }

    "display data in Document type input" in {

      val view = createView(form = Document.form.fill(Document("", "Test", "", Some(""))))

      view.getElementById("documentType").attr("value") must be("Test")
      view.getElementById("documentReference").attr("value") must be("")
      view.getElementById("goodsItemIdentifier").attr("value") must be("")
    }

    "display data in Previous DUCR or MUCR input" in {

      val view = createView(form = Document.form.fill(Document("", "", "Test", Some(""))))

      view.getElementById("documentType").attr("value") must be("")
      view.getElementById("documentReference").attr("value") must be("Test")
      view.getElementById("goodsItemIdentifier").attr("value") must be("")
    }

    "display data in Previous Goods Identifier input" in {

      val view = createView(form = Document.form.fill(Document("", "", "", Some("Test"))))

      view.getElementById("documentType").attr("value") must be("")
      view.getElementById("documentReference").attr("value") must be("")
      view.getElementById("goodsItemIdentifier").attr("value") must be("Test")
    }

    "display all data entered" in {

      val view = createView(form = Document.form.fill(Document("X", "Test", "Test", Some("Test"))))

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
      val view = createView(Mode.Normal, form, prevDocuments)

      // table header
      view.select("form>table>caption").text() must be("supplementary.previousDocuments")
      view.select("form>table>thead>tr>th:nth-child(1)").text() must be(
        "supplementary.previousDocuments.documentCategory.label"
      )
      view.select("form>table>thead>tr>th:nth-child(2)").text() must be(
        "supplementary.previousDocuments.documentType.label"
      )
      view.select("form>table>thead>tr>th:nth-child(3)").text() must be(
        "supplementary.previousDocuments.documentReference.label"
      )
      view.select("form>table>thead>tr>th:nth-child(4)").text() must be(
        "supplementary.previousDocuments.goodsItemIdentifier.label"
      )
      view.select("form>table>thead>tr>th:nth-child(5)").text() must be("supplementary.packageInformation.remove")

      // row
      view.select("form>table>tbody>tr>td:nth-child(1)").text() must be("supplementary.previousDocuments.X")
      view.select("form>table>tbody>tr>td:nth-child(2)").text() must be("1")
      view.select("form>table>tbody>tr>td:nth-child(3)").text() must be("A")
      view.select("form>table>tbody>tr>td:nth-child(4)").text() must be("1")
      view.select("form>table>tbody>tr>td:nth-child(5)>button").text() must be("site.remove")
    }
  }
}
