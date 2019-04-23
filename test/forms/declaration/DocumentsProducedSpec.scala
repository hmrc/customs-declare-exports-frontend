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

package forms.declaration

import base.TestHelper
import forms.common.Date._
import forms.common.DateSpec.{correctDate, correctDateJSON, incorrectDate}
import forms.declaration.additionaldocuments.DocumentIdentifierAndPart._
import forms.declaration.additionaldocuments.DocumentIdentifierAndPartSpec._
import forms.declaration.additionaldocuments.DocumentsProduced
import forms.declaration.additionaldocuments.DocumentsProduced._
import helpers.views.components.DateMessages
import helpers.views.declaration.DocumentsProducedMessages
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString, JsValue}

class DocumentsProducedSpec extends WordSpec with MustMatchers with DocumentsProducedMessages with DateMessages {

  import DocumentsProducedSpec._

  "Documents Produced form with mapping used to bind data" should {

    "return form with errors" when {

      "provided with Document Type Code" which {

        "is longer than 4 characters" in {

          val input = JsObject(Map(documentTypeCodeKey -> JsString("12345")))
          val expectedErrors = Seq(FormError(documentTypeCodeKey, documentTypeCodeError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is shorter than 4 characters" in {

          val input = JsObject(Map(documentTypeCodeKey -> JsString("123")))
          val expectedErrors = Seq(FormError(documentTypeCodeKey, documentTypeCodeError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {

          val input = JsObject(Map(documentTypeCodeKey -> JsString("12#$")))
          val expectedErrors = Seq(FormError(documentTypeCodeKey, documentTypeCodeError))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Document Identifier and Part" which {

        "has missing Document Identifier" in {

          val input = JsObject(Map(documentIdentifierAndPartKey -> JsObject(Map(documentPartKey -> JsString("ABC12")))))
          val expectedErrors = Seq(FormError(documentIdentifierAndPartKey, documentIdentifierAndPartError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "has missing Document Part" in {

          val input = JsObject(
            Map(documentIdentifierAndPartKey -> JsObject(Map(documentIdentifierKey -> JsString("ABCDEF1234567890"))))
          )
          val expectedErrors = Seq(FormError(documentIdentifierAndPartKey, documentIdentifierAndPartError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains errors in its fields" in {

          val input = JsObject(Map(documentIdentifierAndPartKey -> incorrectDocumentIdentifierAndPartJSON))
          val expectedErrors = Seq(
            FormError(s"$documentIdentifierAndPartKey.$documentIdentifierKey", documentIdentifierError),
            FormError(s"$documentIdentifierAndPartKey.$documentPartKey", documentPartError)
          )

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Document Status" which {

        "is longer than 2 characters" in {

          val input = JsObject(Map(documentStatusKey -> JsString("ABC")))
          val expectedErrors = Seq(FormError(documentStatusKey, documentStatusError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {

          val input = JsObject(Map(documentStatusKey -> JsString("A@")))
          val expectedErrors = Seq(FormError(documentStatusKey, documentStatusError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains digits" in {

          val input = JsObject(Map(documentStatusKey -> JsString("A4")))
          val expectedErrors = Seq(FormError(documentStatusKey, documentStatusError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains lower case letters" in {

          val input = JsObject(Map(documentStatusKey -> JsString("Ab")))
          val expectedErrors = Seq(FormError(documentStatusKey, documentStatusError))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Document Status Reason" which {

        "is longer than 35 characters" in {

          val input = JsObject(Map(documentStatusReasonKey -> JsString(TestHelper.createRandomAlphanumericString(36))))
          val expectedErrors =
            Seq(FormError(documentStatusReasonKey, documentStatusReasonError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {

          val input = JsObject(Map(documentStatusReasonKey -> JsString("AB!@#$")))
          val expectedErrors =
            Seq(FormError(documentStatusReasonKey, documentStatusReasonError))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Issuing Authority Name" which {

        "is longer than 70 characters" in {

          val input = JsObject(Map(issuingAuthorityNameKey -> JsString(TestHelper.createRandomAlphanumericString(71))))
          val expectedErrors =
            Seq(FormError(issuingAuthorityNameKey, issuingAuthorityNameLengthError))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Date Of Validity" which {

        "is in incorrect format" in {

          val input = JsObject(
            Map(
              dateOfValidityKey -> JsObject(
                Map(yearKey -> JsString("2000"), monthKey -> JsString("13"), dayKey -> JsString("32"))
              )
            )
          )
          val expectedErrors = Seq(FormError(dateOfValidityKey, dateFormatError))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Measurement Unit" which {

        "is longer than 4 characters" in {

          val input = JsObject(Map(measurementUnitKey -> JsString("AB345")))
          val expectedErrors =
            Seq(FormError(measurementUnitKey, measurementUnitLengthError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is shorter than 4 characters" in {

          val input = JsObject(Map(measurementUnitKey -> JsString("AB3")))
          val expectedErrors =
            Seq(FormError(measurementUnitKey, measurementUnitLengthError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {

          val input = JsObject(Map(measurementUnitKey -> JsString("AB#@")))
          val expectedErrors =
            Seq(FormError(measurementUnitKey, measurementUnitSpecialCharactersError))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Document Quantity" which {

        "has more than 16 digits in total" in {

          val input = JsObject(Map(documentQuantityKey -> JsString("1234567890.1234567")))
          val expectedErrors =
            Seq(FormError(documentQuantityKey, documentQuantityPrecisionError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "has more than 6 decimal places" in {

          val input = JsObject(Map(documentQuantityKey -> JsString("0.1234567")))
          val expectedErrors =
            Seq(FormError(documentQuantityKey, documentQuantityScaleError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is smaller than zero" in {

          val input = JsObject(Map(documentQuantityKey -> JsString("-1.23")))
          val expectedErrors = Seq(FormError(documentQuantityKey, documentQuantityError))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with correct Measurement Unit but no Document Quantity" in {

        val input = JsObject(Map(measurementUnitKey -> JsString("AB34")))
        val expectedErrors = Seq(FormError("", measurementUnitAndQuantityError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with correct Document Quantity but no Measurement Unit" in {

        val input = JsObject(Map(documentQuantityKey -> JsString("123.45")))
        val expectedErrors = Seq(FormError("", measurementUnitAndQuantityError))

        testFailedValidationErrors(input, expectedErrors)
      }

      def testFailedValidationErrors(input: JsValue, expectedErrors: Seq[FormError]): Unit = {
        val form = DocumentsProduced.form().bind(input)
        expectedErrors.foreach(form.errors must contain(_))
      }
    }

    "return form without errors" when {

      "provided with correct data" in {

        val form = DocumentsProduced.form().bind(correctDocumentsProducedJSON)
        form.hasErrors must be(false)
      }

      "provided with empty data" in {

        val form = DocumentsProduced.form().bind(emptyDocumentsProducedJSON)
        form.hasErrors must be(false)
      }

      "provided with Issuing Authority Name containing special characters" in {

        val input = JsObject(Map(issuingAuthorityNameKey -> JsString("Issuing Authority Name with ''' added")))
        val form = DocumentsProduced.form().bind(input)

        form.hasErrors must be(false)
      }
    }

  }

}

object DocumentsProducedSpec {
  private val categoryCode = "A"
  private val typeCode = "B12"

  val correctDocumentsProduced: DocumentsProduced = DocumentsProduced(
    documentTypeCode = Some(categoryCode + typeCode),
    documentIdentifierAndPart = Some(correctDocumentIdentifierAndPart),
    documentStatus = Some("AB"),
    documentStatusReason = Some("DocumentStatusReason"),
    issuingAuthorityName = Some("Issuing Authority Name"),
    dateOfValidity = Some(correctDate),
    measurementUnit = Some("AB12"),
    documentQuantity = Some(BigDecimal("1234567890.123456"))
  )

  val correctDocumentsProducedMap: Map[String, String] = Map(
    documentTypeCodeKey -> (categoryCode + typeCode),
    s"$documentIdentifierAndPartKey.$documentIdentifierKey" -> "ABCDEF1234567890",
    s"$documentIdentifierAndPartKey.$documentPartKey" -> "ABC12",
    documentStatusKey -> "AB",
    documentStatusReasonKey -> "DocumentStatusReason",
    issuingAuthorityNameKey -> "Issuing Authority Name",
    s"$dateOfValidityKey.$yearKey" -> correctDate.year.get.toString,
    s"$dateOfValidityKey.$monthKey" -> correctDate.month.get.toString,
    s"$dateOfValidityKey.$dayKey" -> correctDate.day.get.toString,
    measurementUnitKey -> "AB12",
    documentQuantityKey -> "1234567890.123456"
  )

  val correctDocumentsProducedJSON: JsValue = JsObject(
    Map(
      documentTypeCodeKey -> JsString(categoryCode + typeCode),
      documentIdentifierAndPartKey -> correctDocumentIdentifierAndPartJSON,
      documentStatusKey -> JsString("AB"),
      documentStatusReasonKey -> JsString("DocumentStatusReason"),
      issuingAuthorityNameKey -> JsString("Issuing Authority Name"),
      dateOfValidityKey -> correctDateJSON,
      measurementUnitKey -> JsString("AB12"),
      documentQuantityKey -> JsString("1234567890.123456")
    )
  )

  val incorrectDocumentsProduced: DocumentsProduced = DocumentsProduced(
    documentTypeCode = Some(TestHelper.createRandomAlphanumericString(5)),
    documentIdentifierAndPart = Some(incorrectDocumentIdentifierAndPart),
    documentStatus = Some("ABC"),
    documentStatusReason = Some(TestHelper.createRandomAlphanumericString(36)),
    issuingAuthorityName = Some(TestHelper.createRandomAlphanumericString(71)),
    dateOfValidity = Some(incorrectDate),
    measurementUnit = Some(TestHelper.createRandomAlphanumericString(5)),
    documentQuantity = Some(BigDecimal("12345678901234567"))
  )

  val incorrectDocumentsProducedMap: Map[String, String] = Map(
    documentTypeCodeKey -> TestHelper.createRandomAlphanumericString(5),
    s"$documentIdentifierAndPartKey.$documentIdentifierKey" -> TestHelper.createRandomAlphanumericString(31),
    s"$documentIdentifierAndPartKey.$documentPartKey" -> TestHelper.createRandomAlphanumericString(6),
    documentStatusKey -> "ABC",
    documentStatusReasonKey -> TestHelper.createRandomAlphanumericString(36),
    issuingAuthorityNameKey -> TestHelper.createRandomAlphanumericString(71),
    s"$dateOfValidityKey.$yearKey" -> incorrectDate.year.get.toString,
    s"$dateOfValidityKey.$monthKey" -> incorrectDate.month.get.toString,
    s"$dateOfValidityKey.$dayKey" -> incorrectDate.day.get.toString,
    measurementUnitKey -> TestHelper.createRandomAlphanumericString(5),
    documentQuantityKey -> "12345678901234567"
  )

  val emptyDocumentsProduced = DocumentsProduced(
    documentTypeCode = None,
    documentIdentifierAndPart = None,
    documentStatus = None,
    documentStatusReason = None,
    issuingAuthorityName = None,
    dateOfValidity = None,
    measurementUnit = None,
    documentQuantity = None
  )

  val emptyDocumentsProducedMap: Map[String, String] = Map(
    documentTypeCodeKey -> "",
    s"$documentIdentifierAndPartKey.$documentIdentifierKey" -> "",
    s"$documentIdentifierAndPartKey.$documentPartKey" -> "",
    documentStatusKey -> "",
    documentStatusReasonKey -> "",
    issuingAuthorityNameKey -> "",
    s"$dateOfValidityKey.$yearKey" -> "",
    s"$dateOfValidityKey.$monthKey" -> "",
    s"$dateOfValidityKey.$dayKey" -> "",
    measurementUnitKey -> "",
    documentQuantityKey -> ""
  )

  val emptyDocumentsProducedJSON: JsValue = JsObject(
    Map(
      documentTypeCodeKey -> JsString(""),
      documentIdentifierAndPartKey -> emptyDocumentIdentifierAndPartJSON,
      documentStatusKey -> JsString(""),
      documentStatusReasonKey -> JsString(""),
      issuingAuthorityNameKey -> JsString(""),
      dateOfValidityKey -> JsObject(Map("year" -> JsString(""), "month" -> JsString(""), "day" -> JsString(""))),
      measurementUnitKey -> JsString(""),
      documentQuantityKey -> JsString("")
    )
  )

}
