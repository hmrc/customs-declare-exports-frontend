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
import forms.common.{Date, DateSpec}
import forms.declaration.DocumentsProduced
import forms.declaration.DocumentsProduced._
import forms.declaration.DocumentsProducedSpec.correctDocumentsProduced
import helpers.views.declaration.{CommonMessages, DocumentsProducedMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.documents_produced
import views.tags.ViewTest

@ViewTest
class DocumentsProducedViewSpec extends ViewSpec with DocumentsProducedMessages with CommonMessages {

  private val form: Form[DocumentsProduced] = DocumentsProduced.form()
  private val filledForm =
    DocumentsProduced(
      documentTypeCode = Some("test"),
      documentIdentifier = Some("test1"),
      documentPart = Some("test2"),
      documentStatus = Some("test3"),
      documentStatusReason = Some("test4"),
      issuingAuthorityName = Some("test 5"),
      dateOfValidity = Some(Date(year = Some("2020"), month = Some("04"), day = Some("13"))),
      measurementUnit = Some("AB12"),
      documentQuantity = Some(BigDecimal("234.22"))
    )

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

      getElementById(view, "documentTypeCode-label").text() must be(messages(documentTypeCode))
      getElementById(view, "documentTypeCode").attr("value") must be("")
    }

    "display empty input with label for Document identifier" in {

      val view = createView()

      getElementById(view, "documentIdentifier-label").text() must be(messages(documentIdentifier))
      getElementById(view, "documentIdentifier").attr("value") must be("")
    }

    "display empty input with label for Document part" in {

      val view = createView()

      getElementById(view, "documentPart-label").text() must be(messages(documentPart))
      getElementById(view, "documentPart").attr("value") must be("")
    }

    "display empty input with label for Document status" in {

      val view = createView()

      getElementById(view, "documentStatus-label").text() must be(messages(documentStatus))
      getElementById(view, "documentStatus").attr("value") must be("")
    }

    "display empty input with label for Document status reason" in {

      val view = createView()

      getElementById(view, "documentStatusReason-label").text() must be(messages(documentStatusReason))
      getElementById(view, "documentStatusReason").attr("value") must be("")
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

      getElementById(view, "documentQuantity-label").text() must be(messages(documentQuantity))
      getElementById(view, "documentQuantity").attr("value") must be("")
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
      checkErrorLink(view, 1, documentTypeCodeError, "#documentTypeCode")

      getElementByCss(view, "#error-message-documentTypeCode-input").text() must be(messages(documentTypeCodeError))
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
      checkErrorLink(view, 1, documentIdentifierError, "#documentIdentifier")

      getElementByCss(view, "#error-message-documentIdentifier-input").text() must be(messages(documentIdentifierError))
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
      checkErrorLink(view, 1, documentPartError, "#documentPart")

      getElementByCss(view, "#error-message-documentPart-input").text() must be(messages(documentPartError))
    }

    "display error for Document status" in {

      val view = createView(
        DocumentsProduced
          .form()
          .fillAndValidate(correctDocumentsProduced.copy(documentStatus = Some("ABC")))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentStatusError, "#documentStatus")

      getElementByCss(view, "#error-message-documentStatus-input").text() must be(messages(documentStatusError))
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
      checkErrorLink(view, 1, documentStatusReasonError, "#documentStatusReason")

      getElementByCss(view, "#error-message-documentStatusReason-input").text() must be(
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

    "display error for Date of Validity" in {

      val view = createView(
        DocumentsProduced
          .form()
          .fillAndValidate(correctDocumentsProduced.copy(dateOfValidity = Some(DateSpec.incorrectDate)))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, "dateTime.date.year.error", s"#${dateOfValidityKey}_$yearKey")
      checkErrorLink(view, 2, "dateTime.date.month.error", s"#${dateOfValidityKey}_$monthKey")
      checkErrorLink(view, 3, "dateTime.date.day.error", s"#${dateOfValidityKey}_$dayKey")

      getElementByCss(view, s"#error-message-${dateOfValidityKey}_$yearKey-input").text() must be(
        messages("dateTime.date.year.error")
      )
      getElementByCss(view, s"#error-message-${dateOfValidityKey}_$monthKey-input").text() must be(
        messages("dateTime.date.month.error")
      )
      getElementByCss(view, s"#error-message-${dateOfValidityKey}_$dayKey-input").text() must be(
        messages("dateTime.date.day.error")
      )
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
      checkErrorLink(view, 1, documentQuantityPrecisionError, "#documentQuantity")

      getElementByCss(view, "#error-message-documentQuantity-input").text() must be(
        messages(documentQuantityPrecisionError)
      )
    }

    "display errors for all fields" in {

      val form = DocumentsProduced
        .form()
        .fillAndValidate(
          DocumentsProduced(
            documentTypeCode = Some(TestHelper.createRandomAlphanumericString(5)),
            documentIdentifier = Some(TestHelper.createRandomAlphanumericString(31)),
            documentPart = Some(TestHelper.createRandomAlphanumericString(6)),
            documentStatus = Some("ABC"),
            documentStatusReason = Some(TestHelper.createRandomAlphanumericString(36)),
            issuingAuthorityName = Some(TestHelper.createRandomAlphanumericString(71)),
            dateOfValidity = Some(Date(year = Some("1999"), month = Some("13"), day = Some("32"))),
            measurementUnit = Some(TestHelper.createRandomAlphanumericString(5)),
            documentQuantity = Some(BigDecimal("12345678901234567"))
          )
        )

      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, documentTypeCodeError, s"#$documentTypeCodeKey")
      checkErrorLink(view, 2, documentIdentifierError, s"#$documentIdentifierKey")
      checkErrorLink(view, 3, documentPartError, s"#$documentPartKey")
      checkErrorLink(view, 4, documentStatusError, s"#$documentStatusKey")
      checkErrorLink(view, 5, documentStatusReasonError, s"#$documentStatusReasonKey")
      checkErrorLink(view, 6, issuingAuthorityNameLengthError, s"#$issuingAuthorityNameKey")
      checkErrorLink(view, 7, "dateTime.date.year.error", s"#${dateOfValidityKey}_$yearKey")
      checkErrorLink(view, 8, "dateTime.date.month.error", s"#${dateOfValidityKey}_$monthKey")
      checkErrorLink(view, 9, "dateTime.date.day.error", s"#${dateOfValidityKey}_$dayKey")
      checkErrorLink(view, 10, measurementUnitLengthError, s"#$measurementUnitKey")
      checkErrorLink(view, 11, documentQuantityPrecisionError, s"#$documentQuantityKey")

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
      getElementByCss(view, s"#error-message-${dateOfValidityKey}_$yearKey-input").text() must be(
        messages("dateTime.date.year.error")
      )
      getElementByCss(view, s"#error-message-${dateOfValidityKey}_$monthKey-input").text() must be(
        messages("dateTime.date.month.error")
      )
      getElementByCss(view, s"#error-message-${dateOfValidityKey}_$dayKey-input").text() must be(
        messages("dateTime.date.day.error")
      )
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

      val form = DocumentsProduced.form().fill(filledForm)
      val view = createView(form)

      getElementById(view, "documentTypeCode").attr("value") must be("test")
      getElementById(view, "documentIdentifier").attr("value") must be("test1")
      getElementById(view, "documentPart").attr("value") must be("test2")
      getElementById(view, "documentStatus").attr("value") must be("test3")
      getElementById(view, "documentStatusReason").attr("value") must be("test4")

      getElementById(view, "documentQuantity").attr("value") must be("234.22")
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
