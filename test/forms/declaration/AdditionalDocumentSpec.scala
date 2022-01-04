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

package forms.declaration

import base.{TestHelper, UnitSpec}
import forms.common.Date._
import forms.common.DateSpec.{correctDate, incorrectDate}
import forms.declaration.additionaldocuments.AdditionalDocument
import forms.declaration.additionaldocuments.AdditionalDocument._
import forms.declaration.additionaldocuments.DocumentWriteOff._
import forms.declaration.additionaldocuments.DocumentWriteOffSpec._
import models.declaration.ExportDeclarationTestData.{allRecords, declaration}
import play.api.data.FormError

class AdditionalDocumentSpec extends UnitSpec {

  import AdditionalDocumentSpec._

  "AdditionalDocument form" should {

    "contain errors" when {

      "the user enters a Document Type Code" which {

        "is empty, but the user has selected 'yes' in the related 'yes/no' page" in {
          val input = emptyAdditionalDocumentMap
          val expectedErrors = Seq(FormError(documentTypeCodeKey, "declaration.additionalDocument.documentTypeCode.empty"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is empty, but it was required after the entered authorisation code" in {
          val input = emptyAdditionalDocumentMap
          val expectedError = FormError(documentTypeCodeKey, "declaration.additionalDocument.documentTypeCode.empty.fromAuthCode")

          val form = AdditionalDocument.form(allRecords).bind(input)
          form.errors must contain(expectedError)
        }

        "is longer than 5 characters" in {
          val input = Map(documentTypeCodeKey -> "123456")
          val expectedErrors = Seq(FormError(documentTypeCodeKey, "declaration.additionalDocument.documentTypeCode.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is shorter than 4 characters" in {
          val input = Map(documentTypeCodeKey -> "123")
          val expectedErrors = Seq(FormError(documentTypeCodeKey, "declaration.additionalDocument.documentTypeCode.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {
          val input = Map(documentTypeCodeKey -> "12!$")
          val expectedErrors = Seq(FormError(documentTypeCodeKey, "declaration.additionalDocument.documentTypeCode.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "the user enters a Document Identifier" which {

        "is longer than 35 characters" in {
          val input = Map(documentTypeCodeKey -> "AB12", documentIdentifierKey -> TestHelper.createRandomAlphanumericString(36))
          val expectedErrors =
            Seq(FormError(documentIdentifierKey, "declaration.additionalDocument.documentIdentifier.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {
          val input = Map(documentTypeCodeKey -> "AB12", documentIdentifierKey -> "12#$")
          val expectedErrors =
            Seq(FormError(documentIdentifierKey, "declaration.additionalDocument.documentIdentifier.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "the user enters a Document Status" which {

        "is longer than 2 characters" in {
          val input = Map(documentTypeCodeKey -> "AB12", documentStatusKey -> "ABC")
          val expectedErrors = Seq(FormError(documentStatusKey, "declaration.additionalDocument.documentStatus.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {
          val input = Map(documentTypeCodeKey -> "AB12", documentStatusKey -> "A@")
          val expectedErrors = Seq(FormError(documentStatusKey, "declaration.additionalDocument.documentStatus.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains digits" in {
          val input = Map(documentTypeCodeKey -> "AB12", documentStatusKey -> "A4")
          val expectedErrors = Seq(FormError(documentStatusKey, "declaration.additionalDocument.documentStatus.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "the user does not enters a Document Status Reason" when {
        val documentTypeCodeRequiringAReason = "Y900"
        val documentStatusRequiringAReason = "XX"

        "they entered a Document code requiring a Status Reason" in {
          val input = Map(documentTypeCodeKey -> documentTypeCodeRequiringAReason, documentStatusKey -> "A4")
          val expectedErrors = Seq(FormError(documentStatusReasonKey, "declaration.additionalDocument.documentStatusReason.required.forDocumentCode"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "they entered a Status code requiring a Status Reason" in {
          val input = Map(documentTypeCodeKey -> "AB12", documentStatusKey -> documentStatusRequiringAReason)
          val expectedErrors = Seq(FormError(documentStatusReasonKey, "declaration.additionalDocument.documentStatusReason.required.forStatusCode"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "they entered a Document code and a Status code that both requiring a Status Reason" in {
          val input = Map(documentTypeCodeKey -> documentTypeCodeRequiringAReason, documentStatusKey -> documentStatusRequiringAReason)
          val expectedErrors = Seq(FormError(documentStatusReasonKey, "declaration.additionalDocument.documentStatusReason.required.forDocumentCode"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "the user enters a Document Status Reason" which {

        "is longer than 35 characters" in {
          val input = Map(documentTypeCodeKey -> "AB12", documentStatusReasonKey -> TestHelper.createRandomAlphanumericString(36))
          val expectedErrors =
            Seq(FormError(documentStatusReasonKey, "declaration.additionalDocument.documentStatusReason.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {
          val input = Map(documentTypeCodeKey -> "AB12", documentStatusReasonKey -> "AB!@#$")
          val expectedErrors =
            Seq(FormError(documentStatusReasonKey, "declaration.additionalDocument.documentStatusReason.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "the user enters an Issuing Authority Name" which {

        "is longer than 70 characters" in {
          val input = Map(documentTypeCodeKey -> "AB12", issuingAuthorityNameKey -> TestHelper.createRandomAlphanumericString(71))
          val expectedErrors =
            Seq(FormError(issuingAuthorityNameKey, "declaration.additionalDocument.issuingAuthorityName.error.length"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "the user enters a Date Of Validity" which {

        "is in incorrect format" in {
          val input = Map(
            documentTypeCodeKey -> "AB12",
            s"$dateOfValidityKey.$yearKey" -> "2000",
            s"$dateOfValidityKey.$monthKey" -> "13",
            s"$dateOfValidityKey.$dayKey" -> "32"
          )
          val expectedErrors = Seq(FormError(dateOfValidityKey, "declaration.additionalDocument.dateOfValidity.error.format"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "the user enters a Document WriteOff" which {

        "contains errors in its fields" in {
          val input = Map(
            documentTypeCodeKey -> "AB12",
            s"$documentWriteOffKey.$measurementUnitKey" -> TestHelper.createRandomAlphanumericString(6),
            s"$documentWriteOffKey.$documentQuantityKey" -> "12345678901234567"
          )
          val expectedErrors = Seq(
            FormError(s"$documentWriteOffKey.$measurementUnitKey", "declaration.additionalDocument.measurementUnit.error"),
            FormError(s"$documentWriteOffKey.$documentQuantityKey", "declaration.additionalDocument.documentQuantity.error")
          )

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      def testFailedValidationErrors(input: Map[String, String], expectedErrors: Seq[FormError]): Unit = {
        val form = AdditionalDocument.form(declaration).bind(input)

        expectedErrors.foreach(form.errors must contain(_))
      }
    }

    "not contain errors" when {

      "the user enters correct data" in {
        val form = AdditionalDocument.form(declaration).bind(correctAdditionalDocumentMap)
        form.errors mustBe empty
      }

      "the user enters only required form fields" in {
        val form = AdditionalDocument.form(declaration).bind(bareMinimumAdditionalDocument)
        form.errors mustBe empty
      }

      "the user enters an Issuing Authority Name containing special characters" in {
        val input = Map(documentTypeCodeKey -> "AB12", issuingAuthorityNameKey -> "Issuing Authority Name with ''' added")
        val form = AdditionalDocument.form(declaration).bind(input)

        form.errors mustBe empty
      }
    }

    "convert input to upper case" when {

      "the user enters a document type code in lower case" in {
        val input = Map(documentTypeCodeKey -> "ab12")
        val form = AdditionalDocument.form(declaration).bind(input)

        form.errors mustBe empty
        form.value.flatMap(_.documentTypeCode) must be(Some("AB12"))
      }

      "the user enters a document status in lower case" in {
        val input = Map(documentTypeCodeKey -> "AB12", documentStatusKey -> "Ab")
        val form = AdditionalDocument.form(declaration).bind(input)

        form.errors mustBe empty
        form.value.flatMap(_.documentStatus) must be(Some("AB"))
      }
    }

    "trim white spaces" when {
      "provided with document identifier with white spaces at beginning and end of string" in {
        val trimmedValue = TestHelper.createRandomAlphanumericString(20)
        val input = Map(documentTypeCodeKey -> "AB12", documentIdentifierKey -> s"\n \t${trimmedValue}\t \n")
        val form = AdditionalDocument.form(declaration).bind(input)

        form.errors mustBe empty
        form.value.flatMap(_.documentIdentifier) must be(Some(trimmedValue))
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

  val bareMinimumAdditionalDocument: Map[String, String] = Map(
    documentTypeCodeKey -> (categoryCode + typeCode),
    documentIdentifierKey -> "ABCDEF1234567890",
    documentStatusKey -> "",
    documentStatusReasonKey -> "",
    issuingAuthorityNameKey -> "",
    s"$dateOfValidityKey.$yearKey" -> "",
    s"$dateOfValidityKey.$monthKey" -> "",
    s"$dateOfValidityKey.$dayKey" -> "",
    s"$documentWriteOffKey.$measurementUnitKey" -> "",
    s"$documentWriteOffKey.$documentQuantityKey" -> ""
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
    s"$documentWriteOffKey.$measurementUnitKey" -> "AB1",
    s"$documentWriteOffKey.$documentQuantityKey" -> "1234567890.123456"
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

  val emptyAdditionalDocumentMap: Map[String, String] = Map(
    documentTypeCodeKey -> "",
    documentIdentifierKey -> "",
    documentStatusKey -> "",
    documentStatusReasonKey -> "",
    issuingAuthorityNameKey -> "",
    s"$dateOfValidityKey.$yearKey" -> "",
    s"$dateOfValidityKey.$monthKey" -> "",
    s"$dateOfValidityKey.$dayKey" -> "",
    s"$documentWriteOffKey.$measurementUnitKey" -> "",
    s"$documentWriteOffKey.$documentQuantityKey" -> ""
  )
}
