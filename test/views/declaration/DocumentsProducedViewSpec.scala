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

import base.TestHelper
import forms.common.Date._
import forms.declaration.DocumentsProducedSpec._
import forms.declaration.additionaldocuments.DocumentIdentifierAndPart._
import forms.declaration.additionaldocuments.DocumentIdentifierAndPartSpec._
import forms.declaration.additionaldocuments.DocumentWriteOff._
import forms.declaration.additionaldocuments.DocumentWriteOffSpec._
import forms.declaration.additionaldocuments.DocumentsProduced
import forms.declaration.additionaldocuments.DocumentsProduced._
import helpers.views.components.DateMessages
import helpers.views.declaration.{CommonMessages, DocumentsProducedMessages}
import play.api.data.Form
import play.api.libs.json.Json
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.documents_produced
import views.tags.ViewTest

@ViewTest
class DocumentsProducedViewSpec extends ViewSpec with DocumentsProducedMessages with DateMessages with CommonMessages {

  private val form: Form[DocumentsProduced] = DocumentsProduced.form()

  private def createView(form: Form[DocumentsProduced] = form, cachedDocuments: Seq[DocumentsProduced] = Seq()): Html =
    documents_produced(itemId, appConfig, form, cachedDocuments)(fakeRequest, messages)

  "Documents Produced View" should {

    "have proper messages for labels" in {

      assertMessage(title, "2/3 Do you need to add any documents?")
      assertMessage(hint, "Including certificates, authorisations or additional references")
      assertMessage(documentTypeCode, "Document type code")
      assertMessage(documentIdentifier, "Document identifier")
      assertMessage(documentPart, "Document part")
      assertMessage(documentStatus, "Document status")
      assertMessage(documentStatusReason, "Document status reason")
      assertMessage(issuingAuthorityName, "Issuing authority name")
      assertMessage(dateOfValidity, "Date of validity")
      assertMessage(measurementUnit, "Measurement unit and qualifier")
      assertMessage(documentQuantity, "Quantity")
    }

    "have proper messages for error labels" in {

      assertMessage(documentTypeCodeError, "Incorrect document type code")
      assertMessage(documentIdentifierError, "Incorrect document identifier")
      assertMessage(documentPartError, "Incorrect document part")
      assertMessage(documentStatusError, "Incorrect document status")
      assertMessage(documentStatusReasonError, "Incorrect document status reason")
      assertMessage(issuingAuthorityNameLengthError, "Issuing authority name is too long")
      assertMessage(measurementUnitLengthError, "Measurement unit and qualifier has to be exactly 4 characters long")
      assertMessage(
        measurementUnitSpecialCharactersError,
        "Measurement unit and qualifier cannot contain special characters"
      )
      assertMessage(maximumAmountReachedError, "You cannot have more than 99 documents")
      assertMessage(duplicatedItemError, "You cannot add an already existent document")
      assertMessage(notDefinedError, "Please provide some document information")
      assertMessage(documentIdentifierAndPartError, "Please provide both Document Identifier and Document Part")
      assertMessage(measurementUnitAndQuantityError, "Please provide both Measurement Unit and Document Quantity")
    }
  }

  "Documents Produced View on empty page" should {

    "display page title" in {

      getElementById(createView(), "title").text() must be(messages(title))
    }

    "display section header" in {

      getElementById(createView(), "section-header").text() must be("Your references")
    }

    "display header with hint" in {

      val view = createView()

      getElementByCss(view, "legend>h1").text() must be(messages(title))
      getElementByCss(view, "legend>span").text() must be(messages(hint))
    }

    "display empty input with label for Document type code" in {

      val view = createView()

      getElementById(view, s"$documentTypeCodeKey-label").text() must be(messages(documentTypeCode))
      getElementById(view, s"$documentTypeCodeKey").attr("value") must be("")
    }

    "display empty input with label for Document identifier" in {

      val view = createView()

      getElementById(view, s"${documentIdentifierAndPartKey}_$documentIdentifierKey-label").text() must be(
        messages(documentIdentifier)
      )
      getElementById(view, s"${documentIdentifierAndPartKey}_$documentIdentifierKey").attr("value") must be("")
    }

    "display empty input with label for Document part" in {

      val view = createView()

      getElementById(view, s"${documentIdentifierAndPartKey}_$documentPartKey-label").text() must be(
        messages(documentPart)
      )
      getElementById(view, s"${documentIdentifierAndPartKey}_$documentPartKey").attr("value") must be("")
    }

    "display empty input with label for Document status" in {

      val view = createView()

      getElementById(view, s"$documentStatusKey-label").text() must be(messages(documentStatus))
      getElementById(view, s"$documentStatusKey").attr("value") must be("")
    }

    "display empty input with label for Document status reason" in {

      val view = createView()

      getElementById(view, s"$documentStatusReasonKey-label").text() must be(messages(documentStatusReason))
      getElementById(view, s"$documentStatusReasonKey").attr("value") must be("")
    }

    "display empty input with label for Issuing Authority Name" in {

      val view = createView()

      getElementById(view, s"$issuingAuthorityNameKey-label").text() must be(messages(issuingAuthorityName))
      getElementById(view, issuingAuthorityNameKey).attr("value") must be("")
    }

    "display empty input with label for Date of Validity" in {

      val view = createView()

      getElementById(view, s"$dateOfValidityKey-label").text() must be(messages(dateOfValidity))
      getElementById(view, dateOfValidityKey).attr("value") must be("")
    }

    "display empty input with label for Measurement Unit" in {

      val view = createView()

      getElementById(view, s"${documentWriteOffKey}_$measurementUnitKey-label").text() must be(
        messages(measurementUnit)
      )
      getElementById(view, s"${documentWriteOffKey}_$measurementUnitKey").attr("value") must be("")
    }

    "display empty input with label for Document quantity" in {

      val view = createView()

      getElementById(view, s"${documentWriteOffKey}_$documentQuantityKey-label").text() must be(
        messages(documentQuantity)
      )
      getElementById(view, s"${documentWriteOffKey}_$documentQuantityKey").attr("value") must be("")
    }

    "display 'Back' button that links to 'Additional Information' page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be(s"/customs-declare-exports/declaration/items/$itemId/additional-information")
    }

    "display both 'Add' and 'Save and continue' button on page" in {

      val view = createView()

      val addButton = getElementByCss(view, "#add")
      addButton.text() must be(messages(addCaption))

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Documents Produced View for invalid input" should {

    /*
     * Again works same for both Add and Save and Continue button
     */

    "display error for Document type code" in {

      val view = createView(
        DocumentsProduced
          .form()
          .fillAndValidate(
            correctDocumentsProduced.copy(documentTypeCode = Some(TestHelper.createRandomAlphanumericString(5)))
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentTypeCodeError, s"#$documentTypeCodeKey")

      getElementByCss(view, s"#error-message-$documentTypeCodeKey-input").text() must be(
        messages(documentTypeCodeError)
      )
    }

    "display error for Document identifier" in {

      val documentsProducedWithIncorrectDocumentIdentifier = correctDocumentsProduced.copy(
        documentIdentifierAndPart = Some(
          correctDocumentIdentifierAndPart
            .copy(documentIdentifier = Some(incorrectDocumentIdentifierAndPart.documentIdentifier.get))
        )
      )
      val view =
        createView(DocumentsProduced.form().bind(Json.toJson(documentsProducedWithIncorrectDocumentIdentifier)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentIdentifierError, s"#${documentIdentifierAndPartKey}_$documentIdentifierKey")

      getElementByCss(view, s"#error-message-${documentIdentifierAndPartKey}_$documentIdentifierKey-input")
        .text() must be(messages(documentIdentifierError))
    }

    "display error for Document part" in {

      val documentsProducedWithIncorrectDocumentPart = correctDocumentsProduced.copy(
        documentIdentifierAndPart = Some(
          correctDocumentIdentifierAndPart
            .copy(documentPart = Some(incorrectDocumentIdentifierAndPart.documentPart.get))
        )
      )
      val view = createView(DocumentsProduced.form().bind(Json.toJson(documentsProducedWithIncorrectDocumentPart)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentPartError, s"#${documentIdentifierAndPartKey}_$documentPartKey")

      getElementByCss(view, s"#error-message-${documentIdentifierAndPartKey}_$documentPartKey-input").text() must be(
        messages(documentPartError)
      )
    }

    "display error for Document Identifier and Part" when {

      "provided with Document Identifier but no Document Part" in {

        val documentsProducedWithIncorrectDocumentIdentifierAndPart = correctDocumentsProduced.copy(
          documentIdentifierAndPart = Some(
            emptyDocumentIdentifierAndPart
              .copy(documentIdentifier = Some(correctDocumentIdentifierAndPart.documentIdentifier.get))
          )
        )
        val view = createView(
          DocumentsProduced.form().bind(Json.toJson(documentsProducedWithIncorrectDocumentIdentifierAndPart))
        )

        checkErrorsSummary(view)
        checkErrorLink(view, 1, documentIdentifierAndPartError, s"#$documentIdentifierAndPartKey")

        getElementByCss(view, s"#error-message-$documentIdentifierAndPartKey-input").text() must be(
          messages(documentIdentifierAndPartError)
        )
      }

      "provided with Document Part but no Document Identifier" in {

        val documentsProducedWithIncorrectDocumentIdentifierAndPart = correctDocumentsProduced.copy(
          documentIdentifierAndPart = Some(
            emptyDocumentIdentifierAndPart
              .copy(documentPart = Some(correctDocumentIdentifierAndPart.documentPart.get))
          )
        )
        val view = createView(
          DocumentsProduced.form().bind(Json.toJson(documentsProducedWithIncorrectDocumentIdentifierAndPart))
        )

        checkErrorsSummary(view)
        checkErrorLink(view, 1, documentIdentifierAndPartError, s"#$documentIdentifierAndPartKey")

        getElementByCss(view, s"#error-message-$documentIdentifierAndPartKey-input").text() must be(
          messages(documentIdentifierAndPartError)
        )
      }
    }

    "display error for Document status" in {

      val view = createView(
        DocumentsProduced
          .form()
          .fillAndValidate(correctDocumentsProduced.copy(documentStatus = Some("ABC")))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentStatusError, s"#$documentStatusKey")

      getElementByCss(view, s"#error-message-$documentStatusKey-input").text() must be(messages(documentStatusError))
    }

    "display error for Document status reason" in {

      val view = createView(
        DocumentsProduced
          .form()
          .fillAndValidate(
            correctDocumentsProduced.copy(documentStatusReason = Some(TestHelper.createRandomAlphanumericString(36)))
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentStatusReasonError, s"#$documentStatusReasonKey")

      getElementByCss(view, s"#error-message-$documentStatusReasonKey-input").text() must be(
        messages(documentStatusReasonError)
      )
    }

    "display error for Issuing Authority Name" in {

      val view = createView(
        DocumentsProduced
          .form()
          .fillAndValidate(
            correctDocumentsProduced.copy(issuingAuthorityName = Some(TestHelper.createRandomAlphanumericString(71)))
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, issuingAuthorityNameLengthError, s"#$issuingAuthorityNameKey")

      getElementByCss(view, s"#error-message-$issuingAuthorityNameKey-input").text() must be(
        messages(issuingAuthorityNameLengthError)
      )
    }

    "display error for Date of Validity" when {

      "year is out of range (2000-2099)" in {

        val view = createView(
          DocumentsProduced
            .form()
            .bind(
              correctDocumentsProducedMap ++ Map(
                s"$dateOfValidityKey.$yearKey" -> "1999",
                s"$dateOfValidityKey.$monthKey" -> "12",
                s"$dateOfValidityKey.$dayKey" -> "30"
              )
            )
        )

        checkErrorsSummary(view)
        checkErrorLink(view, 1, dateOutOfRangeError, s"#$dateOfValidityKey")

        getElementByCss(view, s"#error-message-$dateOfValidityKey-input").text() must be(messages(dateOutOfRangeError))
      }

      "provided with non-existing month and day" in {

        val view = createView(
          DocumentsProduced
            .form()
            .bind(
              correctDocumentsProducedMap ++ Map(
                s"$dateOfValidityKey.$monthKey" -> "13",
                s"$dateOfValidityKey.$dayKey" -> "32"
              )
            )
        )

        checkErrorsSummary(view)
        checkErrorLink(view, 1, dateFormatError, s"#$dateOfValidityKey")

        getElementByCss(view, s"#error-message-$dateOfValidityKey-input").text() must be(messages(dateFormatError))
      }
    }

    "display error for Measurement Unit" in {

      val documentsProducedWithIncorrectMeasurementUnit = correctDocumentsProduced.copy(
        documentWriteOff =
          Some(correctDocumentWriteOff.copy(measurementUnit = incorrectDocumentWriteOff.measurementUnit))
      )
      val view = createView(DocumentsProduced.form().bind(Json.toJson(documentsProducedWithIncorrectMeasurementUnit)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, measurementUnitLengthError, s"#${documentWriteOffKey}_$measurementUnitKey")

      getElementByCss(view, s"#error-message-${documentWriteOffKey}_$measurementUnitKey-input").text() must be(
        messages(measurementUnitLengthError)
      )
    }

    "display error for Document quantity" in {

      val documentsProducedWithIncorrectDocumentQuantity = correctDocumentsProduced.copy(
        documentWriteOff =
          Some(correctDocumentWriteOff.copy(documentQuantity = incorrectDocumentWriteOff.documentQuantity))
      )
      val view = createView(DocumentsProduced.form().bind(Json.toJson(documentsProducedWithIncorrectDocumentQuantity)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentQuantityPrecisionError, s"#${documentWriteOffKey}_$documentQuantityKey")

      getElementByCss(view, s"#error-message-${documentWriteOffKey}_$documentQuantityKey-input").text() must be(
        messages(documentQuantityPrecisionError)
      )
    }

    "display error for Document WriteOff" when {

      "provided with Measurement Unit but no Document Quantity" in {

        val documentsProducedWithIncorrectDocumentWriteOff = correctDocumentsProduced.copy(
          documentWriteOff = Some(emptyDocumentWriteOff.copy(measurementUnit = correctDocumentWriteOff.measurementUnit))
        )
        val view =
          createView(DocumentsProduced.form().bind(Json.toJson(documentsProducedWithIncorrectDocumentWriteOff)))

        checkErrorsSummary(view)
        checkErrorLink(view, 1, measurementUnitAndQuantityError, s"#$documentWriteOffKey")

        getElementByCss(view, s"#error-message-$documentWriteOffKey-input").text() must be(
          messages(measurementUnitAndQuantityError)
        )
      }

      "provided with Document Quantity but no Measurement Unit" in {

        val documentsProducedWithIncorrectDocumentWriteOff = correctDocumentsProduced.copy(
          documentWriteOff =
            Some(emptyDocumentWriteOff.copy(documentQuantity = correctDocumentWriteOff.documentQuantity))
        )
        val view =
          createView(DocumentsProduced.form().bind(Json.toJson(documentsProducedWithIncorrectDocumentWriteOff)))

        checkErrorsSummary(view)
        checkErrorLink(view, 1, measurementUnitAndQuantityError, s"#$documentWriteOffKey")

        getElementByCss(view, s"#error-message-$documentWriteOffKey-input").text() must be(
          messages(measurementUnitAndQuantityError)
        )
      }
    }

    "display errors for all fields" in {

      val form = DocumentsProduced.form().bind(incorrectDocumentsProducedMap)

      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentTypeCodeError, s"#$documentTypeCodeKey")
      checkErrorLink(view, 2, documentIdentifierError, s"#${documentIdentifierAndPartKey}_$documentIdentifierKey")
      checkErrorLink(view, 3, documentPartError, s"#${documentIdentifierAndPartKey}_$documentPartKey")
      checkErrorLink(view, 4, documentStatusError, s"#$documentStatusKey")
      checkErrorLink(view, 5, documentStatusReasonError, s"#$documentStatusReasonKey")
      checkErrorLink(view, 6, issuingAuthorityNameLengthError, s"#$issuingAuthorityNameKey")
      checkErrorLink(view, 7, dateFormatError, s"#$dateOfValidityKey")
      checkErrorLink(view, 8, measurementUnitLengthError, s"#${documentWriteOffKey}_$measurementUnitKey")
      checkErrorLink(view, 9, documentQuantityPrecisionError, s"#${documentWriteOffKey}_$documentQuantityKey")

      getElementByCss(view, s"#error-message-$documentTypeCodeKey-input").text() must be(
        messages(documentTypeCodeError)
      )
      getElementByCss(view, s"#error-message-${documentIdentifierAndPartKey}_$documentIdentifierKey-input")
        .text() must be(messages(documentIdentifierError))
      getElementByCss(view, s"#error-message-${documentIdentifierAndPartKey}_$documentPartKey-input").text() must be(
        messages(documentPartError)
      )
      getElementByCss(view, s"#error-message-$documentStatusKey-input").text() must be(messages(documentStatusError))
      getElementByCss(view, s"#error-message-$documentStatusReasonKey-input").text() must be(
        messages(documentStatusReasonError)
      )
      getElementByCss(view, s"#error-message-$issuingAuthorityNameKey-input").text() must be(
        messages(issuingAuthorityNameLengthError)
      )
      getElementByCss(view, s"#error-message-$dateOfValidityKey-input").text() must be(messages(dateFormatError))
      getElementByCss(view, s"#error-message-${documentWriteOffKey}_$measurementUnitKey-input").text() must be(
        messages(measurementUnitLengthError)
      )
      getElementByCss(view, s"#error-message-${documentWriteOffKey}_$documentQuantityKey-input").text() must be(
        messages(documentQuantityPrecisionError)
      )
    }
  }

  "Documents Produced View when filled" should {

    "display data in all inputs" in {

      val data = correctDocumentsProduced
      val form = DocumentsProduced.form().fill(data)
      val view = createView(form)

      getElementById(view, documentTypeCodeKey).attr("value") must equal(data.documentTypeCode.value)
      getElementById(view, s"${documentIdentifierAndPartKey}_$documentIdentifierKey").attr("value") must equal(
        data.documentIdentifierAndPart.value.documentIdentifier.value
      )
      getElementById(view, s"${documentIdentifierAndPartKey}_$documentPartKey").attr("value") must equal(
        data.documentIdentifierAndPart.value.documentPart.value
      )
      getElementById(view, documentStatusKey).attr("value") must equal(data.documentStatus.value)
      getElementById(view, documentStatusReasonKey).attr("value") must equal(data.documentStatusReason.value)
      getElementById(view, issuingAuthorityNameKey).attr("value") must equal(data.issuingAuthorityName.value)
      getElementById(view, s"${dateOfValidityKey}_$dayKey").attr("value") must equal(
        data.dateOfValidity.value.day.value.toString
      )
      getElementById(view, s"${dateOfValidityKey}_$monthKey").attr("value") must equal(
        data.dateOfValidity.value.month.value.toString
      )
      getElementById(view, s"${dateOfValidityKey}_$yearKey").attr("value") must equal(
        data.dateOfValidity.value.year.value.toString
      )
      getElementById(view, s"${documentWriteOffKey}_$measurementUnitKey").attr("value") must equal(
        data.documentWriteOff.get.measurementUnit.value
      )
      getElementById(view, s"${documentWriteOffKey}_$documentQuantityKey").attr("value") must equal(
        data.documentWriteOff.get.documentQuantity.value.toString
      )
    }

    "display a table with previously entered document" in {

      val view = createView(cachedDocuments = Seq(correctDocumentsProduced))

      getElementByCss(view, "table.form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(1)").text() must equal(
        messages(documentTypeCode)
      )
      getElementByCss(view, "table.form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(2)").text() must equal(
        messages(documentIdentifier)
      )
      getElementByCss(view, "table.form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(3)").text() must equal(
        messages(documentPart)
      )
      getElementByCss(view, "table.form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(4)").text() must equal(
        messages(documentStatus)
      )
      getElementByCss(view, "table.form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(5)").text() must equal(
        messages(documentStatusReason)
      )
      getElementByCss(view, "table.form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(6)").text() must equal(
        messages(issuingAuthorityName)
      )
      getElementByCss(view, "table.form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(7)").text() must equal(
        messages(dateOfValidity)
      )
      getElementByCss(view, "table.form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(8)").text() must equal(
        messages(measurementUnit)
      )
      getElementByCss(view, "table.form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(9)").text() must equal(
        messages(documentQuantity)
      )

      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(1)").text() must equal(
        correctDocumentsProduced.documentTypeCode.get
      )
      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(2)").text() must equal(
        correctDocumentsProduced.documentIdentifierAndPart.get.documentIdentifier.get
      )
      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(3)").text() must equal(
        correctDocumentsProduced.documentIdentifierAndPart.get.documentPart.get
      )
      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(4)").text() must equal(
        correctDocumentsProduced.documentStatus.get
      )
      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(5)").text() must equal(
        correctDocumentsProduced.documentStatusReason.get
      )
      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(6)").text() must equal(
        correctDocumentsProduced.issuingAuthorityName.get
      )
      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(7)").text() must equal(
        correctDocumentsProduced.dateOfValidity.get.toString
      )
      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(8)").text() must equal(
        correctDocumentsProduced.documentWriteOff.get.measurementUnit.get
      )
      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(9)").text() must equal(
        correctDocumentsProduced.documentWriteOff.get.documentQuantity.get.toString
      )

      val removeButton = getElementByCss(view, "tbody>tr>td:nth-child(10)>button")
      val firstItemIndex = "0"

      removeButton.text() must be("Remove")
      removeButton.attr("value") must be(firstItemIndex)
    }
  }
}
