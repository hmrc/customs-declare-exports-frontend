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
import forms.common.Date
import forms.common.Date._
import forms.declaration.DocumentsProduced._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString, JsValue}

class DocumentsProducedSpec extends WordSpec with MustMatchers {

  import DocumentsProducedSpec._

  "Documents Produced form with mapping used to bind data" should {

    "return form with errors" when {

      "provided with Document Type Code" which {

        "is longer than 4 characters" in {

          val input = JsObject(Map(documentTypeCodeKey -> JsString("12345")))
          val expectedErrors = Seq(FormError(documentTypeCodeKey, "supplementary.addDocument.documentTypeCode.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is shorter than 4 characters" in {

          val input = JsObject(Map(documentTypeCodeKey -> JsString("123")))
          val expectedErrors = Seq(FormError(documentTypeCodeKey, "supplementary.addDocument.documentTypeCode.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {

          val input = JsObject(Map(documentTypeCodeKey -> JsString("12#$")))
          val expectedErrors = Seq(FormError(documentTypeCodeKey, "supplementary.addDocument.documentTypeCode.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Document Identifier" which {

        "is longer than 30 characters" in {

          val input = JsObject(Map(documentIdentifierKey -> JsString(TestHelper.createRandomAlphanumericString(31))))
          val expectedErrors =
            Seq(FormError(documentIdentifierKey, "supplementary.addDocument.documentIdentifier.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {

          val input = JsObject(Map(documentIdentifierKey -> JsString("12#$")))
          val expectedErrors =
            Seq(FormError(documentIdentifierKey, "supplementary.addDocument.documentIdentifier.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Document Part" which {

        "is longer than 5 characters" in {

          val input = JsObject(Map(documentPartKey -> JsString("123456")))
          val expectedErrors = Seq(FormError(documentPartKey, "supplementary.addDocument.documentPart.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {

          val input = JsObject(Map(documentPartKey -> JsString("12#$")))
          val expectedErrors = Seq(FormError(documentPartKey, "supplementary.addDocument.documentPart.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Document Status" which {

        "is longer than 2 characters" in {

          val input = JsObject(Map(documentStatusKey -> JsString("ABC")))
          val expectedErrors = Seq(FormError(documentStatusKey, "supplementary.addDocument.documentStatus.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {

          val input = JsObject(Map(documentStatusKey -> JsString("A@")))
          val expectedErrors = Seq(FormError(documentStatusKey, "supplementary.addDocument.documentStatus.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains digits" in {

          val input = JsObject(Map(documentStatusKey -> JsString("A4")))
          val expectedErrors = Seq(FormError(documentStatusKey, "supplementary.addDocument.documentStatus.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains lower case letters" in {

          val input = JsObject(Map(documentStatusKey -> JsString("Ab")))
          val expectedErrors = Seq(FormError(documentStatusKey, "supplementary.addDocument.documentStatus.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Document Status Reason" which {

        "is longer than 35 characters" in {

          val input = JsObject(Map(documentStatusReasonKey -> JsString(TestHelper.createRandomAlphanumericString(36))))
          val expectedErrors =
            Seq(FormError(documentStatusReasonKey, "supplementary.addDocument.documentStatusReason.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {

          val input = JsObject(Map(documentStatusReasonKey -> JsString("AB!@#$")))
          val expectedErrors =
            Seq(FormError(documentStatusReasonKey, "supplementary.addDocument.documentStatusReason.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Issuing Authority Name" which {

        "is longer than 70 characters" in {

          val input = JsObject(Map(issuingAuthorityNameKey -> JsString(TestHelper.createRandomAlphanumericString(71))))
          val expectedErrors =
            Seq(FormError(issuingAuthorityNameKey, "supplementary.addDocument.issuingAuthorityName.error.length"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Date Of Validity" which {

        "is in incorrect format" in {

          val input = JsObject(
            Map(
              dateOfValidityKey -> JsObject(
                Map("year" -> JsString("1999"), "month" -> JsString("13"), "day" -> JsString("32"))
              )
            )
          )
          val expectedErrors = Seq(
            FormError(dateOfValidityKey + ".year", "dateTime.date.year.error"),
            FormError(dateOfValidityKey + ".month", "dateTime.date.month.error"),
            FormError(dateOfValidityKey + ".day", "dateTime.date.day.error")
          )

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Measurement Unit" which {

        "is longer than 4 characters" in {

          val input = JsObject(Map(measurementUnitKey -> JsString("AB345")))
          val expectedErrors =
            Seq(FormError(measurementUnitKey, "supplementary.addDocument.measurementUnit.error.length"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is shorter than 4 characters" in {

          val input = JsObject(Map(measurementUnitKey -> JsString("AB3")))
          val expectedErrors =
            Seq(FormError(measurementUnitKey, "supplementary.addDocument.measurementUnit.error.length"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {

          val input = JsObject(Map(measurementUnitKey -> JsString("AB#@")))
          val expectedErrors =
            Seq(FormError(measurementUnitKey, "supplementary.addDocument.measurementUnit.error.specialCharacters"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Document Quantity" which {

        "has more than 16 digits in total" in {

          val input = JsObject(Map(documentQuantityKey -> JsString("1234567890.1234567")))
          val expectedErrors =
            Seq(FormError(documentQuantityKey, "supplementary.addDocument.documentQuantity.error.precision"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "has more than 6 decimal places" in {

          val input = JsObject(Map(documentQuantityKey -> JsString("0.1234567")))
          val expectedErrors =
            Seq(FormError(documentQuantityKey, "supplementary.addDocument.documentQuantity.error.scale"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is smaller than zero" in {

          val input = JsObject(Map(documentQuantityKey -> JsString("-1.23")))
          val expectedErrors = Seq(FormError(documentQuantityKey, "supplementary.addDocument.documentQuantity.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with correct Document Identifier but no Document Part" in {

        val input = JsObject(Map(documentIdentifierKey -> JsString("ABCD1234")))
        val expectedErrors = Seq(FormError("", "supplementary.addDocument.error.documentIdentifierAndPart"))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with correct Document Part but no Document Identifier" in {

        val input = JsObject(Map(documentPartKey -> JsString("ABC45")))
        val expectedErrors = Seq(FormError("", "supplementary.addDocument.error.documentIdentifierAndPart"))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with correct Measurement Unit but no Document Quantity" in {

        val input = JsObject(Map(measurementUnitKey -> JsString("AB34")))
        val expectedErrors = Seq(FormError("", "supplementary.addDocument.error.measurementUnitAndQuantity"))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with correct Document Quantity but no Measurement Unit" in {

        val input = JsObject(Map(documentQuantityKey -> JsString("123.45")))
        val expectedErrors = Seq(FormError("", "supplementary.addDocument.error.measurementUnitAndQuantity"))

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
    documentIdentifier = Some("ABCDEF1234567890"),
    documentPart = Some("ABC12"),
    documentStatus = Some("AB"),
    documentStatusReason = Some("DocumentStatusReason"),
    issuingAuthorityName = Some("Issuing Authority Name"),
    dateOfValidity = Some(Date(year = Some("2020"), month = Some("04"), day = Some("13"))),
    measurementUnit = Some("AB12"),
    documentQuantity = Some(BigDecimal("1234567890.123456"))
  )

  val correctDocumentsProducedMap: Map[String, String] = Map(
    "documentTypeCode" -> (categoryCode + typeCode),
    "documentIdentifier" -> "ABCDEF1234567890",
    "documentPart" -> "ABC12",
    "documentStatus" -> "AB",
    "documentStatusReason" -> "DocumentStatusReason",
    issuingAuthorityNameKey -> "Issuing Authority Name",
    s"$dateOfValidityKey.$yearKey" -> "2020",
    s"$dateOfValidityKey.$monthKey" -> "04",
    s"$dateOfValidityKey.$dayKey" -> "13",
    measurementUnitKey -> "AB12",
    "documentQuantity" -> "1234567890.123456"
  )

  val correctDocumentsProducedJSON: JsValue = JsObject(
    Map(
      "documentTypeCode" -> JsString(categoryCode + typeCode),
      "documentIdentifier" -> JsString("ABCDEF1234567890"),
      "documentPart" -> JsString("ABC12"),
      "documentStatus" -> JsString("AB"),
      "documentStatusReason" -> JsString("DocumentStatusReason"),
      issuingAuthorityNameKey -> JsString("Issuing Authority Name"),
      dateOfValidityKey -> JsObject(
        Map("year" -> JsString("2020"), "month" -> JsString("04"), "day" -> JsString("13"))
      ),
      measurementUnitKey -> JsString("AB12"),
      "documentQuantity" -> JsString("1234567890.123456")
    )
  )

  val emptyDocumentsProduced = DocumentsProduced(
    documentTypeCode = None,
    documentIdentifier = None,
    documentPart = None,
    documentStatus = None,
    documentStatusReason = None,
    issuingAuthorityName = None,
    dateOfValidity = None,
    measurementUnit = None,
    documentQuantity = None
  )

  val emptyDocumentsProducedMap: Map[String, String] = Map(
    "documentTypeCode" -> "",
    "documentIdentifier" -> "",
    "documentPart" -> "",
    "documentStatus" -> "",
    "documentStatusReason" -> "",
    issuingAuthorityNameKey -> "",
    s"$dateOfValidityKey.$yearKey" -> "",
    s"$dateOfValidityKey.$monthKey" -> "",
    s"$dateOfValidityKey.$dayKey" -> "",
    measurementUnitKey -> "",
    "documentQuantity" -> ""
  )

  val emptyDocumentsProducedJSON: JsValue = JsObject(
    Map(
      "documentTypeCode" -> JsString(""),
      "documentIdentifier" -> JsString(""),
      "documentPart" -> JsString(""),
      "documentStatus" -> JsString(""),
      "documentStatusReason" -> JsString(""),
      issuingAuthorityNameKey -> JsString(""),
      dateOfValidityKey -> JsObject(Map("year" -> JsString(""), "month" -> JsString(""), "day" -> JsString(""))),
      measurementUnitKey -> JsString(""),
      "documentQuantity" -> JsString("")
    )
  )

}
