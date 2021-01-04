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

package forms.declaration

import base.TestHelper
import forms.common.Date._
import forms.common.DateSpec.{correctDate, correctDateJSON, incorrectDate}
import forms.declaration.additionaldocuments.DocumentWriteOff._
import forms.declaration.additionaldocuments.DocumentWriteOffSpec._
import forms.declaration.additionaldocuments.DocumentsProduced._
import forms.declaration.additionaldocuments.{DocumentWriteOff, DocumentsProduced}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString, JsValue, Json}

class DocumentsProducedSpec extends WordSpec with MustMatchers {

  import DocumentsProducedSpec._

  "Documents Produced form with mapping used to bind data" should {

    "return form with errors" when {

      "provided with Document Type Code" which {

        "is longer than 5 characters" in {

          val input = JsObject(Map(documentTypeCodeKey -> JsString("123456")))
          val expectedErrors = Seq(FormError(documentTypeCodeKey, "declaration.addDocument.documentTypeCode.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is shorter than 4 characters" in {

          val input = JsObject(Map(documentTypeCodeKey -> JsString("123")))
          val expectedErrors = Seq(FormError(documentTypeCodeKey, "declaration.addDocument.documentTypeCode.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {

          val input = JsObject(Map(documentTypeCodeKey -> JsString("12!$")))
          val expectedErrors = Seq(FormError(documentTypeCodeKey, "declaration.addDocument.documentTypeCode.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Document Identifier and Part" which {

        "is longer than 35 characters" in {
          val input = JsObject(Map(documentIdentifierKey -> JsString(TestHelper.createRandomAlphanumericString(36))))
          val expectedErrors =
            Seq(FormError(documentIdentifierKey, "declaration.addDocument.documentIdentifier.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {
          val input = JsObject(Map(documentIdentifierKey -> JsString("12#$")))
          val expectedErrors =
            Seq(FormError(documentIdentifierKey, "declaration.addDocument.documentIdentifier.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }
      "provided with Document Status" which {

        "is longer than 2 characters" in {

          val input = JsObject(Map(documentStatusKey -> JsString("ABC")))
          val expectedErrors = Seq(FormError(documentStatusKey, "declaration.addDocument.documentStatus.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {

          val input = JsObject(Map(documentStatusKey -> JsString("A@")))
          val expectedErrors = Seq(FormError(documentStatusKey, "declaration.addDocument.documentStatus.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains digits" in {

          val input = JsObject(Map(documentStatusKey -> JsString("A4")))
          val expectedErrors = Seq(FormError(documentStatusKey, "declaration.addDocument.documentStatus.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }
      "provided with Document Status Reason" which {

        "is longer than 35 characters" in {

          val input = JsObject(Map(documentStatusReasonKey -> JsString(TestHelper.createRandomAlphanumericString(36))))
          val expectedErrors =
            Seq(FormError(documentStatusReasonKey, "declaration.addDocument.documentStatusReason.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {

          val input = JsObject(Map(documentStatusReasonKey -> JsString("AB!@#$")))
          val expectedErrors =
            Seq(FormError(documentStatusReasonKey, "declaration.addDocument.documentStatusReason.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }
      "provided with Issuing Authority Name" which {

        "is longer than 70 characters" in {

          val input = JsObject(Map(issuingAuthorityNameKey -> JsString(TestHelper.createRandomAlphanumericString(71))))
          val expectedErrors =
            Seq(FormError(issuingAuthorityNameKey, "declaration.addDocument.issuingAuthorityName.error.length"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Date Of Validity" which {

        "is in incorrect format" in {

          val input =
            JsObject(Map(dateOfValidityKey -> JsObject(Map(yearKey -> JsString("2000"), monthKey -> JsString("13"), dayKey -> JsString("32")))))
          val expectedErrors = Seq(FormError(dateOfValidityKey, "declaration.addDocument.dateOfValidity.error.format"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }
      "provided with Document WriteOff" which {

        "contains errors in its fields" in {

          val input = JsObject(Map(documentWriteOffKey -> incorrectDocumentWriteOffJSON))
          val expectedErrors = Seq(
            FormError(s"$documentWriteOffKey.$measurementUnitKey", "declaration.addDocument.measurementUnit.error"),
            FormError(s"$documentWriteOffKey.$documentQuantityKey", "declaration.addDocument.documentQuantity.error")
          )

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      def testFailedValidationErrors(input: JsValue, expectedErrors: Seq[FormError]): Unit = {
        val form = DocumentsProduced.form.bind(input)
        expectedErrors.foreach(form.errors must contain(_))
      }
    }

    "return form without errors" when {

      "provided with correct data" in {

        val form = DocumentsProduced.form.bind(correctDocumentsProducedJSON)
        form.errors mustBe empty
      }

      "provided with empty data" in {

        val form = DocumentsProduced.form.bind(emptyDocumentsProducedJSON)
        form.errors mustBe empty
      }

      "provided with Issuing Authority Name containing special characters" in {

        val input = JsObject(Map(issuingAuthorityNameKey -> JsString("Issuing Authority Name with ''' added")))
        val form = DocumentsProduced.form.bind(input)

        form.errors mustBe empty
      }

      "provide the correct data" in {

        val input = JsObject(
          Map(
            documentTypeCodeKey -> JsString(categoryCode + typeCode),
            documentIdentifierKey -> JsString("ABCDEF1234567890"),
            documentStatusKey -> JsString("AB"),
            documentStatusReasonKey -> JsString("DocumentStatusReason"),
            issuingAuthorityNameKey -> JsString("Issuing Authority Name"),
            dateOfValidityKey -> correctDateJSON,
            documentWriteOffKey -> Json.toJson(DocumentWriteOff(Some("ABC"), Some(12)))
          )
        )
        val form = DocumentsProduced.form.bind(input)

        form.errors mustBe empty
      }
    }

    "convert input to upper case" when {

      "provided with document type code in lower case" in {

        val input = JsObject(Map(documentTypeCodeKey -> JsString("ab12")))
        val form = DocumentsProduced.form.bind(input)

        form.errors mustBe empty
        form.value.flatMap(_.documentTypeCode) must be(Some("AB12"))
      }

      "provided with document status in lower case" in {

        val input = JsObject(Map(documentStatusKey -> JsString("Ab")))
        val form = DocumentsProduced.form.bind(input)

        form.errors mustBe empty
        form.value.flatMap(_.documentStatus) must be(Some("AB"))
      }

    }
  }
}

object DocumentsProducedSpec {
  private val categoryCode = "A"
  private val typeCode = "B12"

  val correctDocumentsProduced: DocumentsProduced = DocumentsProduced(
    documentTypeCode = Some(categoryCode + typeCode),
    documentIdentifier = Some("ABCDEF1234567890"),
    documentStatus = Some("AB"),
    documentStatusReason = Some("DocumentStatusReason"),
    issuingAuthorityName = Some("Issuing Authority Name"),
    dateOfValidity = Some(correctDate),
    documentWriteOff = Some(correctDocumentWriteOff)
  )

  val correctDocumentsProducedMap: Map[String, String] = Map(
    documentTypeCodeKey -> (categoryCode + typeCode),
    documentIdentifierKey -> "ABCDEF1234567890",
    documentStatusKey -> "AB",
    documentStatusReasonKey -> "DocumentStatusReason",
    issuingAuthorityNameKey -> "Issuing Authority Name",
    s"$dateOfValidityKey.$yearKey" -> correctDate.year.get.toString,
    s"$dateOfValidityKey.$monthKey" -> correctDate.month.get.toString,
    s"$dateOfValidityKey.$dayKey" -> correctDate.day.get.toString,
    s"$documentWriteOffKey.$measurementUnitKey" -> "AB12",
    s"$documentWriteOffKey.$documentQuantityKey" -> "1234567890.123456"
  )

  val correctDocumentsProducedJSON: JsValue = JsObject(
    Map(
      documentTypeCodeKey -> JsString(categoryCode + typeCode),
      documentIdentifierKey -> JsString("ABCDEF1234567890"),
      documentStatusKey -> JsString("AB"),
      documentStatusReasonKey -> JsString("DocumentStatusReason"),
      issuingAuthorityNameKey -> JsString("Issuing Authority Name"),
      dateOfValidityKey -> correctDateJSON,
      documentWriteOffKey -> correctDocumentWriteOffJSON
    )
  )

  val incorrectDocumentsProducedMap: Map[String, String] = Map(
    documentTypeCodeKey -> TestHelper.createRandomAlphanumericString(5),
    documentIdentifierKey -> TestHelper.createRandomAlphanumericString(36),
    documentStatusKey -> "ABC",
    documentStatusReasonKey -> TestHelper.createRandomAlphanumericString(36),
    issuingAuthorityNameKey -> TestHelper.createRandomAlphanumericString(71),
    s"$dateOfValidityKey.$yearKey" -> incorrectDate.year.get.toString,
    s"$dateOfValidityKey.$monthKey" -> incorrectDate.month.get.toString,
    s"$dateOfValidityKey.$dayKey" -> incorrectDate.day.get.toString,
    s"$documentWriteOffKey.$measurementUnitKey" -> TestHelper.createRandomAlphanumericString(6),
    s"$documentWriteOffKey.$documentQuantityKey" -> "12345678901234567"
  )

  val emptyDocumentsProducedJSON: JsValue = JsObject(
    Map(
      documentTypeCodeKey -> JsString(""),
      documentIdentifierKey -> JsString(""),
      documentStatusKey -> JsString(""),
      documentStatusReasonKey -> JsString(""),
      issuingAuthorityNameKey -> JsString(""),
      dateOfValidityKey -> JsObject(Map("year" -> JsString(""), "month" -> JsString(""), "day" -> JsString(""))),
      documentWriteOffKey -> emptyDocumentWriteOffJSON
    )
  )
}
