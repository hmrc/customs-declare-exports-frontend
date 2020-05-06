/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.DeclarationPage
import forms.declaration.Document
import forms.declaration.officeOfExit.OfficeOfExitOutsideUK
import models.DeclarationType.DeclarationType
import models.{DeclarationType, Mode}
import org.jsoup.nodes.{Document => JsonDocument}
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
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
    documents: Seq[Document] = Seq.empty,
    messages: Messages = stubMessages(),
    navigationForm: DeclarationPage = Document,
    declarationType: DeclarationType = DeclarationType.STANDARD
  ): JsonDocument =
    page(mode, navigationForm, form, documents)(journeyRequest(declarationType), messages)

  "Previous Documents View on empty page" should {
    val view = createView()

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("supplementary.previousDocuments.documentCategory.label")
      messages must haveTranslationFor("supplementary.previousDocuments.documentType.label")
      messages must haveTranslationFor("supplementary.previousDocuments.documentReference.label")
      messages must haveTranslationFor("supplementary.previousDocuments.goodsItemIdentifier.label")
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

    "display same page title as header" in {
      val viewWithMessage = createView(messages = realMessagesApi.preferred(request))
      viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
    }

    "display section header" in {
      view.getElementById("section-header").text() must include("supplementary.consignmentReferences.heading")
    }

    "display three radio buttons with description (not selected)" in {

      val view = createView(form = Document.form.fill(Document("", "", "", Some(""))))

      val optionOne = view.getElementById("Temporary storage")
      optionOne.attr("checked") mustBe empty

      view.getElementById("Temporary storage-label").text() must be("supplementary.previousDocuments.X")

      val optionTwo = view.getElementById("Simplified declaration")
      optionTwo.attr("checked") mustBe empty

      view.getElementById("Simplified declaration-label").text() must be("supplementary.previousDocuments.Y")

      val optionThree = view.getElementById("Related document")
      optionThree.attr("checked") mustBe empty

      view.getElementById("Related document-label").text() must be("supplementary.previousDocuments.Z")
    }

    "display empty input with label for Previous document code" in {
      view.getElementById("documentType-label").text() must be("supplementary.previousDocuments.documentType")
      view.getElementById("documentType").attr("value") mustBe empty
    }

    "display empty input with label for Previous DUCR or MUCR" in {
      view.getElementById("documentReference-label").text() must be("supplementary.previousDocuments.documentReference")
      view.getElementById("documentReference").attr("value") mustBe empty
    }

    "display empty input with label for Previous Goods Identifier" in {
      view.getElementById("goodsItemIdentifier-label").text() must be("supplementary.previousDocuments.goodsItemIdentifier")
      view.getElementById("goodsItemIdentifier").attr("value") mustBe empty
    }

    "display 'Back' button that links to 'Transaction Type' page" when {
      onJourney(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY) { request =>
        "has a valid back button" in {

          val backButton = view.getElementById("back-link")

          backButton.text() must be("site.back")
          backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.NatureOfTransactionController.displayPage(Mode.Normal))
        }
      }

      onJourney(DeclarationType.CLEARANCE, DeclarationType.OCCASIONAL, DeclarationType.SIMPLIFIED) { request =>
        "with Office Of Exit outside UK" in {

          val backButton = createView(declarationType = DeclarationType.SIMPLIFIED, navigationForm = Document).getElementById("back-link")

          backButton.text() must be("site.back")
          backButton.getElementById("back-link") must haveHref(
            controllers.declaration.routes.OfficeOfExitOutsideUkController.displayPage(Mode.Normal)
          )
        }
      }

      onJourney(DeclarationType.CLEARANCE, DeclarationType.OCCASIONAL, DeclarationType.SIMPLIFIED) { request =>
        " with Office Of Exit Inside UK" in {

          val backButton =
            createView(declarationType = DeclarationType.SIMPLIFIED, navigationForm = OfficeOfExitOutsideUK).getElementById("back-link")

          backButton.text() must be("site.back")
          backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.OfficeOfExitController.displayPage(Mode.Normal))
        }
      }
    }

    "display both 'Add' and 'Save and continue' button on page" in {
      val addButton = view.getElementById("add")
      addButton.text() must be("site.add supplementary.previousDocuments.add.hint")

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
      optionTwo.attr("checked") mustBe empty

      val optionThree = view.getElementById("Related document")
      optionThree.attr("checked") mustBe empty
    }

    "display selected second radio button - Simplified Declaration (Y)" in {

      val view = createView(form = Document.form.fill(Document("Y", "", "", Some(""))))

      val optionOne = view.getElementById("Temporary storage")
      optionOne.attr("checked") mustBe empty

      val optionTwo = view.getElementById("Simplified declaration")
      optionTwo.attr("checked") must be("checked")

      val optionThree = view.getElementById("Related document")
      optionThree.attr("checked") mustBe empty
    }

    "display selected third radio button - Previous Documents (Z)" in {

      val view = createView(form = Document.form.fill(Document("Z", "", "", Some(""))))

      val optionOne = view.getElementById("Temporary storage")
      optionOne.attr("checked") mustBe empty

      val optionTwo = view.getElementById("Simplified declaration")
      optionTwo.attr("checked") mustBe empty

      val optionThree = view.getElementById("Related document")
      optionThree.attr("checked") must be("checked")
    }

    "display data in Document type input" in {

      val view = createView(form = Document.form.fill(Document("", "Test", "", Some(""))))

      view.getElementById("documentType").attr("value") must be("Test")
      view.getElementById("documentReference").attr("value") mustBe empty
      view.getElementById("goodsItemIdentifier").attr("value") mustBe empty
    }

    "display data in Previous DUCR or MUCR input" in {

      val view = createView(form = Document.form.fill(Document("", "", "Test", Some(""))))

      view.getElementById("documentType").attr("value") mustBe empty
      view.getElementById("documentReference").attr("value") must be("Test")
      view.getElementById("goodsItemIdentifier").attr("value") mustBe empty
    }

    "display data in Previous Goods Identifier input" in {

      val view = createView(form = Document.form.fill(Document("", "", "", Some("Test"))))

      view.getElementById("documentType").attr("value") mustBe empty
      view.getElementById("documentReference").attr("value") mustBe empty
      view.getElementById("goodsItemIdentifier").attr("value") must be("Test")
    }

    "display all data entered" in {

      val view = createView(form = Document.form.fill(Document("X", "Test", "Test", Some("Test"))))

      val optionOne = view.getElementById("Temporary storage")
      optionOne.attr("checked") must be("checked")

      val optionTwo = view.getElementById("Simplified declaration")
      optionTwo.attr("checked") mustBe empty

      val optionThree = view.getElementById("Related document")
      optionThree.attr("checked") mustBe empty

      view.getElementById("documentType").attr("value") must be("Test")
      view.getElementById("documentReference").attr("value") must be("Test")
      view.getElementById("goodsItemIdentifier").attr("value") must be("Test")
    }

    "display one row with data in table" in {

      val prevDocuments = Seq(Document("X", "1", "A", Some("1")))
      val view = createView(Mode.Normal, form, prevDocuments)

      // table header
      view.select("form>table>caption").text() must be("supplementary.previousDocuments")
      view.select("form>table>thead>tr>th:nth-child(1)").text() must be("supplementary.previousDocuments.documentCategory.label")
      view.select("form>table>thead>tr>th:nth-child(2)").text() must be("supplementary.previousDocuments.documentType.label")
      view.select("form>table>thead>tr>th:nth-child(3)").text() must be("supplementary.previousDocuments.documentReference.label")
      view.select("form>table>thead>tr>th:nth-child(4)").text() must be("supplementary.previousDocuments.goodsItemIdentifier.label")
      // remove button column
      view.select("form>table>thead>tr>td").text() must be("")

      // row
      view.select("form>table>tbody>tr>th:nth-child(1)").text() must be("supplementary.previousDocuments.X")
      view.select("form>table>tbody>tr>td:nth-child(2)").text() must be("1")
      view.select("form>table>tbody>tr>td:nth-child(3)").text() must be("A")
      view.select("form>table>tbody>tr>td:nth-child(4)").text() must be("1")
      view.select("form>table>tbody>tr>td:nth-child(5)>button").text() must be("site.remove supplementary.previousDocuments.remove.hint")
    }
  }
}
