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
import forms.declaration.DocumentsProduced
import forms.declaration.DocumentsProduced._
import forms.declaration.DocumentsProducedSpec.{
  correctDocumentsProduced,
  correctDocumentsProducedMap,
  incorrectDocumentsProducedMap
}
import helpers.views.components.DateMessages
import helpers.views.declaration.{CommonMessages, DocumentsProducedMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.documents_produced
import views.tags.ViewTest

@ViewTest
class DocumentsProducedViewSpec extends ViewSpec with DocumentsProducedMessages with DateMessages with CommonMessages {

  private val form: Form[DocumentsProduced] = DocumentsProduced.form()

  private def createView(form: Form[DocumentsProduced] = form, cachedDocuments: Seq[DocumentsProduced] = Seq()): Html =
    documents_produced(appConfig, form, cachedDocuments)(fakeRequest, messages)

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

      getElementById(view, s"$documentIdentifierKey-label").text() must be(messages(documentIdentifier))
      getElementById(view, s"$documentIdentifierKey").attr("value") must be("")
    }

    "display empty input with label for Document part" in {

      val view = createView()

      getElementById(view, s"$documentPartKey-label").text() must be(messages(documentPart))
      getElementById(view, s"$documentPartKey").attr("value") must be("")
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

      getElementById(view, s"$measurementUnitKey-label").text() must be(messages(measurementUnit))
      getElementById(view, measurementUnitKey).attr("value") must be("")
    }

    "display empty input with label for Document quantity" in {

      val view = createView()

      getElementById(view, s"$documentQuantityKey-label").text() must be(messages(documentQuantity))
      getElementById(view, s"$documentQuantityKey").attr("value") must be("")
    }

    "display 'Back' button that links to 'Additional Information' page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/additional-information")
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

      val view = createView(
        DocumentsProduced
          .form()
          .fillAndValidate(
            correctDocumentsProduced.copy(documentIdentifier = Some(TestHelper.createRandomAlphanumericString(31)))
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentIdentifierError, s"#$documentIdentifierKey")

      getElementByCss(view, s"#error-message-$documentIdentifierKey-input").text() must be(
        messages(documentIdentifierError)
      )
    }

    "display error for Document part" in {

      val view = createView(
        DocumentsProduced
          .form()
          .fillAndValidate(
            correctDocumentsProduced.copy(documentPart = Some(TestHelper.createRandomAlphanumericString(6)))
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentPartError, s"#$documentPartKey")

      getElementByCss(view, s"#error-message-$documentPartKey-input").text() must be(messages(documentPartError))
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

      val view = createView(
        DocumentsProduced
          .form()
          .fillAndValidate(
            correctDocumentsProduced.copy(measurementUnit = Some(TestHelper.createRandomAlphanumericString(5)))
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, measurementUnitLengthError, s"#$measurementUnitKey")

      getElementByCss(view, s"#error-message-$measurementUnitKey-input").text() must be(
        messages(measurementUnitLengthError)
      )
    }

    "display error for Document quantity" in {

      val view = createView(
        DocumentsProduced
          .form()
          .fillAndValidate(correctDocumentsProduced.copy(documentQuantity = Some(BigDecimal("12345678901234567"))))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentQuantityPrecisionError, s"#$documentQuantityKey")

      getElementByCss(view, s"#error-message-$documentQuantityKey-input").text() must be(
        messages(documentQuantityPrecisionError)
      )
    }

    "display errors for all fields" in {

      val form = DocumentsProduced.form().bind(incorrectDocumentsProducedMap)

      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentTypeCodeError, s"#$documentTypeCodeKey")
      checkErrorLink(view, 2, documentIdentifierError, s"#$documentIdentifierKey")
      checkErrorLink(view, 3, documentPartError, s"#$documentPartKey")
      checkErrorLink(view, 4, documentStatusError, s"#$documentStatusKey")
      checkErrorLink(view, 5, documentStatusReasonError, s"#$documentStatusReasonKey")
      checkErrorLink(view, 6, issuingAuthorityNameLengthError, s"#$issuingAuthorityNameKey")
      checkErrorLink(view, 7, dateFormatError, s"#$dateOfValidityKey")
      checkErrorLink(view, 8, measurementUnitLengthError, s"#$measurementUnitKey")
      checkErrorLink(view, 9, documentQuantityPrecisionError, s"#$documentQuantityKey")

      getElementByCss(view, s"#error-message-$documentTypeCodeKey-input").text() must be(
        messages(documentTypeCodeError)
      )
      getElementByCss(view, s"#error-message-$documentIdentifierKey-input").text() must be(
        messages(documentIdentifierError)
      )
      getElementByCss(view, s"#error-message-$documentPartKey-input").text() must be(messages(documentPartError))
      getElementByCss(view, s"#error-message-$documentStatusKey-input").text() must be(messages(documentStatusError))
      getElementByCss(view, s"#error-message-$documentStatusReasonKey-input").text() must be(
        messages(documentStatusReasonError)
      )
      getElementByCss(view, s"#error-message-$issuingAuthorityNameKey-input").text() must be(
        messages(issuingAuthorityNameLengthError)
      )
      getElementByCss(view, s"#error-message-$dateOfValidityKey-input").text() must be(messages(dateFormatError))
      getElementByCss(view, s"#error-message-$measurementUnitKey-input").text() must be(
        messages(measurementUnitLengthError)
      )
      getElementByCss(view, s"#error-message-$documentQuantityKey-input").text() must be(
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
      getElementById(view, documentIdentifierKey).attr("value") must equal(data.documentIdentifier.value)
      getElementById(view, documentPartKey).attr("value") must equal(data.documentPart.value)
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
      getElementById(view, measurementUnitKey).attr("value") must equal(data.measurementUnit.value)
      getElementById(view, documentQuantityKey).attr("value") must equal(data.documentQuantity.value.toString)
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
        correctDocumentsProduced.documentIdentifier.get
      )
      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(3)").text() must equal(
        correctDocumentsProduced.documentPart.get
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
        correctDocumentsProduced.measurementUnit.get
      )
      getElementByCss(view, "table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(9)").text() must equal(
        correctDocumentsProduced.documentQuantity.get.toString
      )

      val removeButton = getElementByCss(view, "tbody>tr>td:nth-child(10)>button")
      val firstItemIndex = "0"

      removeButton.text() must be("Remove")
      removeButton.attr("value") must be(firstItemIndex)
    }
  }
}
