/*
 * Copyright 2022 HM Revenue & Customs
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

package views.declaration.addtionalDocuments

import base.Injector
import controllers.declaration.routes
import forms.common.YesNoAnswer
import forms.declaration.AdditionalDocumentSpec.correctAdditionalDocument
import forms.declaration.additionaldocuments.{AdditionalDocument, DocumentWriteOff}
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.{Document, Element}
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.additionalDocuments.additional_document_remove
import views.tags.ViewTest

@ViewTest
class AdditionalDocumentRemoveViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val itemId = "a7sc78"
  private val documentId = "1.2131231"

  private val additionalDocumentRemovePage = instanceOf[additional_document_remove]

  private def createView(
    mode: Mode = Mode.Normal,
    form: Form[YesNoAnswer] = YesNoAnswer.form(),
    documents: AdditionalDocument = correctAdditionalDocument
  )(implicit request: JourneyRequest[_]): Document =
    additionalDocumentRemovePage(mode, itemId, documentId, documents, form)(request, messages)

  "have proper messages for labels" in {
    messages must haveTranslationFor("declaration.additionalDocument.remove.title")
    messages must haveTranslationFor("declaration.additionalDocument.remove.code")
    messages must haveTranslationFor("declaration.additionalDocument.remove.reference")
    messages must haveTranslationFor("declaration.additionalDocument.remove.statusCode")
    messages must haveTranslationFor("declaration.additionalDocument.remove.statusReason")
    messages must haveTranslationFor("declaration.additionalDocument.remove.issuingAuthorityName")
    messages must haveTranslationFor("declaration.additionalDocument.remove.dateOfValidity")
    messages must haveTranslationFor("declaration.additionalDocument.remove.measurementUnitAndQualifier")
    messages must haveTranslationFor("declaration.additionalDocument.remove.documentQuantity")
  }

  "additional_document_remove view back link" should {
    onEveryDeclarationJourney() { implicit request =>
      "display back link" in {
        val view = createView()
        view must containElementWithID("back-link")
        view.getElementById("back-link") must haveHref(routes.AdditionalDocumentsController.displayPage(Mode.Normal, itemId))
      }
    }
  }

  "additional_document_remove view when filled" should {

    onEveryDeclarationJourney() { implicit request =>
      "display data in table" in {

        val view = createView()

        val descriptions = view.getElementsByClass("govuk-summary-list__key")
        val values = view.getElementsByClass("govuk-summary-list__value")

        descriptions.get(0).text mustBe messages("declaration.additionalDocument.remove.code")
        values.get(0).text mustBe correctAdditionalDocument.documentTypeCode.get

        descriptions.get(1).text mustBe messages("declaration.additionalDocument.remove.reference")
        values.get(1).text mustBe correctAdditionalDocument.documentIdentifier.get

        descriptions.get(2).text mustBe messages("declaration.additionalDocument.remove.statusCode")
        values.get(2).text mustBe correctAdditionalDocument.documentStatus.get

        descriptions.get(3).text mustBe messages("declaration.additionalDocument.remove.statusReason")
        values.get(3).text mustBe correctAdditionalDocument.documentStatusReason.get

        descriptions.get(4).text mustBe messages("declaration.additionalDocument.remove.issuingAuthorityName")
        values.get(4).text mustBe correctAdditionalDocument.issuingAuthorityName.get

        descriptions.get(5).text mustBe messages("declaration.additionalDocument.remove.dateOfValidity")
        values.get(5).text mustBe correctAdditionalDocument.dateOfValidity.map(_.toDisplayFormat).get

        descriptions.get(6).text mustBe messages("declaration.additionalDocument.remove.measurementUnitAndQualifier")
        values.get(6).text mustBe correctAdditionalDocument.documentWriteOff.map(_.measurementUnitDisplay).get

        descriptions.get(7).text mustBe messages("declaration.additionalDocument.remove.documentQuantity")
        values.get(7).text mustBe correctAdditionalDocument.documentWriteOff.flatMap(_.documentQuantity).map(_.toString).get
      }

      def summary(documents: AdditionalDocument): Element = createView(documents = documents).select("dl").first()

      "not display code when not present" in {
        summary(correctAdditionalDocument.copy(documentTypeCode = None)) mustNot containText(messages("declaration.additionalDocument.remove.code"))
      }

      "not display reference when not present" in {
        summary(correctAdditionalDocument.copy(documentIdentifier = None)) mustNot containText(
          messages("declaration.additionalDocument.remove.reference")
        )
      }

      "not display statusCode when not present" in {
        summary(correctAdditionalDocument.copy(documentStatus = None)) mustNot containText(
          messages("declaration.additionalDocument.remove.statusCode")
        )
      }

      "not display statusReason when not present" in {
        summary(correctAdditionalDocument.copy(documentStatusReason = None)) mustNot containText(
          messages("declaration.additionalDocument.remove.statusReason")
        )
      }

      "not display issuingAuthorityName when not present" in {
        summary(correctAdditionalDocument.copy(issuingAuthorityName = None)) mustNot containText(
          messages("declaration.additionalDocument.remove.issuingAuthorityName")
        )
      }

      "not display dateOfValidity when not present" in {
        summary(correctAdditionalDocument.copy(dateOfValidity = None)) mustNot containText(
          messages("declaration.additionalDocument.remove.dateOfValidity")
        )
      }

      "not display measurementUnitAndQualifier when not present" in {
        summary(correctAdditionalDocument.copy(documentWriteOff = Some(DocumentWriteOff(None, Some(BigDecimal(1000)))))) mustNot containText(
          messages("declaration.additionalDocument.remove.measurementUnitAndQualifier")
        )
      }

      "not display documentQuantity when not present" in {
        summary(correctAdditionalDocument.copy(documentWriteOff = Some(DocumentWriteOff(Some("KGM"), None)))) mustNot containText(
          messages("declaration.additionalDocument.remove.documentQuantity")
        )
      }

    }
  }
}
