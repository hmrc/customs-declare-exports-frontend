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
import forms.declaration.additionaldocuments.AdditionalDocument._
import forms.declaration.additionaldocuments.{AdditionalDocument, DocumentWriteOff}
import base.UnitSpec
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString, JsValue, Json}

class AdditionalDocumentSpec extends UnitSpec {

  import AdditionalDocumentSpec._

  "AdditionalDocument form with mapping used to bind data" should {

    "return form with errors" when {

      "provided with Document Type Code" which {

        "is empty when it is instead required" in {

          val input = emptyAdditionalDocumentJSON
          val expectedErrors = Seq(FormError(documentTypeCodeKey, "declaration.additionalDocument.documentTypeCode.empty"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is empty when it is instead required after the entered authorisation code" in {

          val input = emptyAdditionalDocumentJSON
          val expectedError = FormError(documentTypeCodeKey, "declaration.additionalDocument.documentTypeCode.empty.fromAuthCode")

          val form = AdditionalDocument.form(isAuthCodeRequiringAdditionalDocuments = true).bind(input)
          form.errors must contain(expectedError)
        }

        "is longer than 5 characters" in {

          val input = JsObject(Map(documentTypeCodeKey -> JsString("123456")))
          val expectedErrors = Seq(FormError(documentTypeCodeKey, "declaration.additionalDocument.documentTypeCode.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is shorter than 4 characters" in {

          val input = JsObject(Map(documentTypeCodeKey -> JsString("123")))
          val expectedErrors = Seq(FormError(documentTypeCodeKey, "declaration.additionalDocument.documentTypeCode.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {

          val input = JsObject(Map(documentTypeCodeKey -> JsString("12!$")))
          val expectedErrors = Seq(FormError(documentTypeCodeKey, "declaration.additionalDocument.documentTypeCode.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Document Identifier and Part" which {

        "is longer than 35 characters" in {
          val input =
            JsObject(Map(documentTypeCodeKey -> JsString("AB12"), documentIdentifierKey -> JsString(TestHelper.createRandomAlphanumericString(36))))
          val expectedErrors =
            Seq(FormError(documentIdentifierKey, "declaration.additionalDocument.documentIdentifier.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {
          val input = JsObject(Map(documentTypeCodeKey -> JsString("AB12"), documentIdentifierKey -> JsString("12#$")))
          val expectedErrors =
            Seq(FormError(documentIdentifierKey, "declaration.additionalDocument.documentIdentifier.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Document Status" which {

        "is longer than 2 characters" in {

          val input = JsObject(Map(documentTypeCodeKey -> JsString("AB12"), documentStatusKey -> JsString("ABC")))
          val expectedErrors = Seq(FormError(documentStatusKey, "declaration.additionalDocument.documentStatus.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {

          val input = JsObject(Map(documentTypeCodeKey -> JsString("AB12"), documentStatusKey -> JsString("A@")))
          val expectedErrors = Seq(FormError(documentStatusKey, "declaration.additionalDocument.documentStatus.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains digits" in {

          val input = JsObject(Map(documentTypeCodeKey -> JsString("AB12"), documentStatusKey -> JsString("A4")))
          val expectedErrors = Seq(FormError(documentStatusKey, "declaration.additionalDocument.documentStatus.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Document Status Reason" which {

        "is longer than 35 characters" in {

          val input =
            JsObject(Map(documentTypeCodeKey -> JsString("AB12"), documentStatusReasonKey -> JsString(TestHelper.createRandomAlphanumericString(36))))
          val expectedErrors =
            Seq(FormError(documentStatusReasonKey, "declaration.additionalDocument.documentStatusReason.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {

          val input = JsObject(Map(documentTypeCodeKey -> JsString("AB12"), documentStatusReasonKey -> JsString("AB!@#$")))
          val expectedErrors =
            Seq(FormError(documentStatusReasonKey, "declaration.additionalDocument.documentStatusReason.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Issuing Authority Name" which {

        "is longer than 70 characters" in {

          val input =
            JsObject(Map(documentTypeCodeKey -> JsString("AB12"), issuingAuthorityNameKey -> JsString(TestHelper.createRandomAlphanumericString(71))))
          val expectedErrors =
            Seq(FormError(issuingAuthorityNameKey, "declaration.additionalDocument.issuingAuthorityName.error.length"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Date Of Validity" which {

        "is in incorrect format" in {

          val input = JsObject(
            Map(
              documentTypeCodeKey -> JsString("AB12"),
              dateOfValidityKey -> JsObject(Map(yearKey -> JsString("2000"), monthKey -> JsString("13"), dayKey -> JsString("32")))
            )
          )
          val expectedErrors = Seq(FormError(dateOfValidityKey, "declaration.additionalDocument.dateOfValidity.error.format"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Document WriteOff" which {

        "contains errors in its fields" in {

          val input = JsObject(Map(documentTypeCodeKey -> JsString("AB12"), documentWriteOffKey -> incorrectDocumentWriteOffJSON))
          val expectedErrors = Seq(
            FormError(s"$documentWriteOffKey.$measurementUnitKey", "declaration.additionalDocument.measurementUnit.error"),
            FormError(s"$documentWriteOffKey.$documentQuantityKey", "declaration.additionalDocument.documentQuantity.error")
          )

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      def testFailedValidationErrors(input: JsValue, expectedErrors: Seq[FormError]): Unit = {
        val form = AdditionalDocument.form().bind(input)
        expectedErrors.foreach(form.errors must contain(_))
      }
    }

    "return form without errors" when {

      "provided with correct data" in {

        val form = AdditionalDocument.form().bind(correctAdditionalDocumentJSON)
        form.errors mustBe empty
      }

      "provided with Issuing Authority Name containing special characters" in {

        val input =
          JsObject(Map(documentTypeCodeKey -> JsString("AB12"), issuingAuthorityNameKey -> JsString("Issuing Authority Name with ''' added")))
        val form = AdditionalDocument.form().bind(input)

        form.errors mustBe empty
      }

      "provide the correct data" in {

        val input = JsObject(
          Map(
            documentTypeCodeKey -> JsString("AB12"),
            documentIdentifierKey -> JsString("ABCDEF1234567890"),
            documentStatusKey -> JsString("AB"),
            documentStatusReasonKey -> JsString("DocumentStatusReason"),
            issuingAuthorityNameKey -> JsString("Issuing Authority Name"),
            dateOfValidityKey -> correctDateJSON,
            documentWriteOffKey -> Json.toJson(DocumentWriteOff(Some("ABC"), Some(12)))
          )
        )

        val form = AdditionalDocument.form().bind(input)
        form.errors mustBe empty
      }
    }

    "convert input to upper case" when {

      "provided with document type code in lower case" in {

        val input = JsObject(Map(documentTypeCodeKey -> JsString("ab12")))
        val form = AdditionalDocument.form().bind(input)

        form.errors mustBe empty
        form.value.flatMap(_.documentTypeCode) must be(Some("AB12"))
      }

      "provided with document status in lower case" in {

        val input = JsObject(Map(documentTypeCodeKey -> JsString("AB12"), documentStatusKey -> JsString("Ab")))
        val form = AdditionalDocument.form().bind(input)

        form.errors mustBe empty
        form.value.flatMap(_.documentStatus) must be(Some("AB"))
      }

    }
  }
}

object AdditionalDocumentSpec {
  private val categoryCode = "A"
  private val typeCode = "B12"

  val correctAdditionalDocument: AdditionalDocument = AdditionalDocument(
    documentTypeCode = Some(categoryCode + typeCode),
    documentIdentifier = Some("ABCDEF1234567890"),
    documentStatus = Some("AB"),
    documentStatusReason = Some("DocumentStatusReason"),
    issuingAuthorityName = Some("Issuing Authority Name"),
    dateOfValidity = Some(correctDate),
    documentWriteOff = Some(correctDocumentWriteOff)
  )

  val correctAdditionalDocumentMap: Map[String, String] = Map(
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

  val correctAdditionalDocumentJSON: JsValue = JsObject(
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

  val incorrectAdditionalDocumentMap: Map[String, String] = Map(
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

  val emptyAdditionalDocumentJSON: JsValue = JsObject(
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
