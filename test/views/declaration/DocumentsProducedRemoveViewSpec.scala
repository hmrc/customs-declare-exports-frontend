/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.common.YesNoAnswer
import forms.declaration.DocumentsProducedSpec.correctDocumentsProduced
import forms.declaration.additionaldocuments.{DocumentWriteOff, DocumentsProduced}
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.documentsProduced.documents_produced_remove
import views.tags.ViewTest

@ViewTest
class DocumentsProducedRemoveViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val itemId = "a7sc78"
  private val documentId = "1.2131231"

  private val page = instanceOf[documents_produced_remove]

  private def createView(
    mode: Mode = Mode.Normal,
    form: Form[YesNoAnswer] = YesNoAnswer.form(),
    documents: DocumentsProduced = correctDocumentsProduced
  )(implicit request: JourneyRequest[_]): Document = page(mode, itemId, documentId, documents, form)(request, messages)

  "have proper messages for labels" in {
    messages must haveTranslationFor("declaration.addDocument.remove.title")
    messages must haveTranslationFor("declaration.addDocument.remove.code")
    messages must haveTranslationFor("declaration.addDocument.remove.reference")
    messages must haveTranslationFor("declaration.addDocument.remove.statusCode")
    messages must haveTranslationFor("declaration.addDocument.remove.statusReason")
    messages must haveTranslationFor("declaration.addDocument.remove.issuingAuthorityName")
    messages must haveTranslationFor("declaration.addDocument.remove.dateOfValidity")
    messages must haveTranslationFor("declaration.addDocument.remove.measurementUnitAndQualifier")
    messages must haveTranslationFor("declaration.addDocument.remove.documentQuantity")
  }

  "DocumentsProduced Remove View back link" should {
    onEveryDeclarationJourney() { implicit request =>
      "display back link" in {
        val view = createView()
        view must containElementWithID("back-link")
        view.getElementById("back-link") must haveHref(controllers.declaration.routes.DocumentsProducedController.displayPage(Mode.Normal, itemId))
      }
    }
  }

  "DocumentsProduced Remove View when filled" should {

    onEveryDeclarationJourney() { implicit request =>
      "display data in table" in {

        val view = createView()

        view.select("dl>div:nth-child(1)>dt").text() mustBe messages("declaration.addDocument.remove.code")
        view.select("dl>div:nth-child(1)>dd").text() mustBe correctDocumentsProduced.documentTypeCode.get
        view.select("dl>div:nth-child(2)>dt").text() mustBe messages("declaration.addDocument.remove.reference")
        view.select("dl>div:nth-child(2)>dd").text() mustBe correctDocumentsProduced.documentIdentifier.get
        view.select("dl>div:nth-child(3)>dt").text() mustBe messages("declaration.addDocument.remove.statusCode")
        view.select("dl>div:nth-child(3)>dd").text() mustBe correctDocumentsProduced.documentStatus.get
        view.select("dl>div:nth-child(4)>dt").text() mustBe messages("declaration.addDocument.remove.statusReason")
        view.select("dl>div:nth-child(4)>dd").text() mustBe correctDocumentsProduced.documentStatusReason.get
        view.select("dl>div:nth-child(5)>dt").text() mustBe messages("declaration.addDocument.remove.issuingAuthorityName")
        view.select("dl>div:nth-child(5)>dd").text() mustBe correctDocumentsProduced.issuingAuthorityName.get
        view.select("dl>div:nth-child(6)>dt").text() mustBe messages("declaration.addDocument.remove.dateOfValidity")
        view.select("dl>div:nth-child(6)>dd").text() mustBe correctDocumentsProduced.dateOfValidity.map(_.toDisplayFormat).get
        view.select("dl>div:nth-child(7)>dt").text() mustBe messages("declaration.addDocument.remove.measurementUnitAndQualifier")
        view.select("dl>div:nth-child(7)>dd").text() mustBe correctDocumentsProduced.documentWriteOff.map(_.measurementUnitDisplay).get
        view.select("dl>div:nth-child(8)>dt").text() mustBe messages("declaration.addDocument.remove.documentQuantity")
        view.select("dl>div:nth-child(8)>dd").text() mustBe correctDocumentsProduced.documentWriteOff.flatMap(_.documentQuantity).map(_.toString).get
      }

      def summary(documents: DocumentsProduced) = createView(documents = documents).select("dl").first()

      "not display code when not present" in {
        summary(correctDocumentsProduced.copy(documentTypeCode = None)) mustNot containText(messages("declaration.addDocument.remove.code"))
      }

      "not display reference when not present" in {
        summary(correctDocumentsProduced.copy(documentIdentifier = None)) mustNot containText(messages("declaration.addDocument.remove.reference"))
      }

      "not display statusCode when not present" in {
        summary(correctDocumentsProduced.copy(documentStatus = None)) mustNot containText(messages("declaration.addDocument.remove.statusCode"))
      }

      "not display statusReason when not present" in {
        summary(correctDocumentsProduced.copy(documentStatusReason = None)) mustNot containText(
          messages("declaration.addDocument.remove.statusReason")
        )
      }

      "not display issuingAuthorityName when not present" in {
        summary(correctDocumentsProduced.copy(issuingAuthorityName = None)) mustNot containText(
          messages("declaration.addDocument.remove.issuingAuthorityName")
        )
      }

      "not display dateOfValidity when not present" in {
        summary(correctDocumentsProduced.copy(dateOfValidity = None)) mustNot containText(messages("declaration.addDocument.remove.dateOfValidity"))
      }

      "not display measurementUnitAndQualifier when not present" in {
        summary(correctDocumentsProduced.copy(documentWriteOff = Some(DocumentWriteOff(None, Some(BigDecimal(1000)))))) mustNot containText(
          messages("declaration.addDocument.remove.measurementUnitAndQualifier")
        )
      }

      "not display documentQuantity when not present" in {
        summary(correctDocumentsProduced.copy(documentWriteOff = Some(DocumentWriteOff(Some("KGM"), None)))) mustNot containText(
          messages("declaration.addDocument.remove.documentQuantity")
        )
      }

    }
  }
}
