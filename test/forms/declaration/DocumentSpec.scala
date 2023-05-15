/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.declaration

import forms.common.DeclarationPageBaseSpec
import forms.declaration.DocumentSpec.json
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.OptionValues
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, JsString, Json}
import services.{DocumentType, DocumentTypeService}

class DocumentSpec extends DeclarationPageBaseSpec with OptionValues with MockitoSugar {

  val mockDocumentTypeService = mock[DocumentTypeService]
  implicit val mockMessages = mock[Messages]

  when(mockDocumentTypeService.allDocuments()(any())).thenReturn(List(DocumentType("DocumentReference", "DCS")))

  "Document mapping" should {

    "return form without errors" when {

      "provided with valid input" in {
        val correctJson = json("DCS", "DocumentReference", "12")
        val form = Document.form(mockDocumentTypeService).bind(correctJson, JsonBindMaxChars)
        form.errors mustBe empty
      }

      "provided document reference with '-' and '/'" in {
        val correctJson = json("DCS", "GB/239355053000-PATYR8987", "123")
        val form = Document.form(mockDocumentTypeService).bind(correctJson, JsonBindMaxChars)
        form.errors mustBe empty
      }

      "provided document reference with ':'" in {
        val correctJson = json("DCS", "A:12345645", "123")
        val form = Document.form(mockDocumentTypeService).bind(correctJson, JsonBindMaxChars)
        form.errors mustBe empty
      }

      "the document reference contains multiple initial and/or trailing spaces" in {
        val correctJson = json("DCS", "  ABCD1234  ", "123")
        val form = Document.form(mockDocumentTypeService).bind(correctJson, JsonBindMaxChars)
        form.errors mustBe empty
        form.value.value.documentReference mustBe "ABCD1234"
      }

      "the document reference includes single spaces" in {
        val correctJson = json("DCS", "  ABC 123 987  ", "123")
        val form = Document.form(mockDocumentTypeService).bind(correctJson, JsonBindMaxChars)
        form.errors mustBe empty
        form.value.value.documentReference mustBe "ABC 123 987"
      }
    }

    "return form with errors" when {

      "an empty document type is entered" in {
        val withEmptyType = json("", "abcd", "123")
        val form = Document.form(mockDocumentTypeService).bind(withEmptyType, JsonBindMaxChars)
        form.errors.length must be(1)
        form.errors.head.message must be("declaration.previousDocuments.documentCode.empty")
      }

      "an unknown document type is entered" in {
        val withUnknownType = json("BLAbla", "abcd", "123")
        val form = Document.form(mockDocumentTypeService).bind(withUnknownType, JsonBindMaxChars)
        form.errors.length must be(1)
        form.errors.head.message must be("declaration.previousDocuments.documentCode.error")
      }

      "an empty document reference is entered" in {
        val withEmptyReference = json("DCS", "", "123")
        val form = Document.form(mockDocumentTypeService).bind(withEmptyReference, JsonBindMaxChars)
        form.errors.length must be(1)
        form.errors.head.message must be("declaration.previousDocuments.documentReference.empty")
      }

      "a too long document reference is entered" in {
        val withTooLongReference = json("DCS", "abcd" * 9, "123")
        val form = Document.form(mockDocumentTypeService).bind(withTooLongReference, JsonBindMaxChars)
        form.errors.length must be(1)
        form.errors.head.message must be("declaration.previousDocuments.documentReference.error.length")
      }

      "a document reference with invalid characters is entered" in {
        val withIllegalReference = json("DCS", "abcd)", "123")
        val form = Document.form(mockDocumentTypeService).bind(withIllegalReference, JsonBindMaxChars)
        form.errors.length must be(1)
        form.errors.head.message must be("declaration.previousDocuments.documentReference.error")
      }

      "a document reference with consecutive spaces is entered" in {
        val withIllegalReference = json("DCS", "abcd  1234", "123")
        val form = Document.form(mockDocumentTypeService).bind(withIllegalReference, JsonBindMaxChars)
        form.errors.length must be(1)
        form.errors.head.message must be("declaration.previousDocuments.documentReference.error.spaces")
      }

      "a too long goods-identifier is entered" in {
        val withTooLongIdentifier = json("DCS", "abcd", "1234")
        val form = Document.form(mockDocumentTypeService).bind(withTooLongIdentifier, JsonBindMaxChars)
        form.errors.length must be(1)
        form.errors.head.message must be("declaration.previousDocuments.goodsItemIdentifier.error")
      }

      "a document reference with invalid characters and consecutive spaces is entered" in {
        val withIllegalReference = json("DCS", "abcd)  123", "123")
        val form = Document.form(mockDocumentTypeService).bind(withIllegalReference, JsonBindMaxChars)
        form.errors.length must be(2)
        form.errors.head.message must be("declaration.previousDocuments.documentReference.error")
        form.errors.last.message must be("declaration.previousDocuments.documentReference.error.spaces")
      }

      "a non-numeric goods-identifier is entered" in {
        val withNonNumericIdentifier = json("DCS", "abcd", "A12")
        val form = Document.form(mockDocumentTypeService).bind(withNonNumericIdentifier, JsonBindMaxChars)
        form.errors.length must be(1)
        form.errors.head.message must be("declaration.previousDocuments.goodsItemIdentifier.error")
      }
    }
  }

  "Document" when {
    testTariffContentKeys(Document, "tariff.declaration.addPreviousDocument")
  }

  "DocumentChangeOrRemove" when {
    testTariffContentKeysNoSpecialisation(DocumentChangeOrRemove, "tariff.declaration.previousDocuments.remove", getClearanceTariffKeys)
  }
}

object DocumentSpec {

  def json(documentType: String, documentReference: String, identifier: String): JsObject = Json.obj(
    "documentType" -> JsString(documentType),
    "documentReference" -> JsString(documentReference),
    "goodsItemIdentifier" -> JsString(identifier)
  )
}
