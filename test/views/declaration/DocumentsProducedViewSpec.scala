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

import base.{Injector, TestHelper}
import controllers.util.SaveAndReturn
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
import models.Mode
import org.jsoup.nodes.Document
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.documents_produced
import views.tags.ViewTest

@ViewTest
class DocumentsProducedViewSpec
    extends UnitViewSpec with DocumentsProducedMessages with DateMessages with CommonMessages with Stubs with Injector
    with OptionValues {

  val itemId = "a7sc78"
  private val form: Form[DocumentsProduced] = DocumentsProduced.form()
  private val documentsProducedPage = new documents_produced(mainTemplate)
  private def createView(
    form: Form[DocumentsProduced] = form,
    cachedDocuments: Seq[DocumentsProduced] = Seq()
  ): Document =
    documentsProducedPage(Mode.Normal, itemId, form, cachedDocuments)(request, messages)

  "Document Produced" should {

    "have correct message keys" in {

      val messages = instanceOf[MessagesApi].preferred(request)

      messages must haveTranslationFor("supplementary.addDocument.title")
      messages must haveTranslationFor("supplementary.addDocument.hint")
      messages must haveTranslationFor("supplementary.addDocument.documentTypeCode")
      messages must haveTranslationFor("supplementary.addDocument.item.documentTypeCode")
      messages must haveTranslationFor("supplementary.addDocument.documentTypeCode.error")
      messages must haveTranslationFor("supplementary.addDocument.documentIdentifier")
      messages must haveTranslationFor("supplementary.addDocument.item.documentIdentifier")
      messages must haveTranslationFor("supplementary.addDocument.documentIdentifier.error")
      messages must haveTranslationFor("supplementary.addDocument.documentPart")
      messages must haveTranslationFor("supplementary.addDocument.item.documentPart")
      messages must haveTranslationFor("supplementary.addDocument.documentPart.error")
      messages must haveTranslationFor("supplementary.addDocument.documentStatus")
      messages must haveTranslationFor("supplementary.addDocument.item.documentStatus")
      messages must haveTranslationFor("supplementary.addDocument.documentStatus.error")
      messages must haveTranslationFor("supplementary.addDocument.documentStatusReason")
      messages must haveTranslationFor("supplementary.addDocument.item.documentStatusReason")
      messages must haveTranslationFor("supplementary.addDocument.documentStatusReason.error")
      messages must haveTranslationFor("supplementary.addDocument.issuingAuthorityName")
      messages must haveTranslationFor("supplementary.addDocument.issuingAuthorityName.error.length")
      messages must haveTranslationFor("supplementary.addDocument.dateOfValidity")
      messages must haveTranslationFor("supplementary.addDocument.measurementUnit")
      messages must haveTranslationFor("supplementary.addDocument.measurementUnit.error.length")
      messages must haveTranslationFor("supplementary.addDocument.measurementUnit.error.specialCharacters")
      messages must haveTranslationFor("supplementary.addDocument.documentQuantity")
      messages must haveTranslationFor("supplementary.addDocument.item.documentQuantity")
      messages must haveTranslationFor("supplementary.addDocument.documentQuantity.error.precision")
      messages must haveTranslationFor("supplementary.addDocument.documentQuantity.error.scale")
      messages must haveTranslationFor("supplementary.addDocument.documentQuantity.error")
      messages must haveTranslationFor("supplementary.addDocument.error.maximumAmount")
      messages must haveTranslationFor("supplementary.addDocument.error.duplicated")
      messages must haveTranslationFor("supplementary.addDocument.error.notDefined")
      messages must haveTranslationFor("supplementary.addDocument.error.documentIdentifierAndPart")
      messages must haveTranslationFor("supplementary.addDocument.error.measurementUnitAndQuantity")
    }
  }

  "Documents Produced View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() mustBe messages(title)
    }

    "display section header" in {

      createView().getElementById("section-header").text() mustBe messages(
        "supplementary.summary.yourReferences.header"
      )
    }

    "display header with hint" in {

      val view = createView()

      view.select("legend>h1").text() mustBe messages(title)
      view.select("legend>span").text() must include(messages(hint))
    }

    "display empty input with label for Document type code" in {

      val view = createView()

      view.getElementById(s"$documentTypeCodeKey-label").text() mustBe messages(documentTypeCode)
      view.getElementById(s"$documentTypeCodeKey").attr("value") mustBe empty
    }

    "display empty input with label for Document identifier" in {

      val view = createView()

      view.getElementById(s"${documentIdentifierAndPartKey}_$documentIdentifierKey-label").text() mustBe
        messages(documentIdentifier)

      view.getElementById(s"${documentIdentifierAndPartKey}_$documentIdentifierKey").attr("value") mustBe empty
    }

    "display empty input with label for Document part" in {

      val view = createView()

      view.getElementById(s"${documentIdentifierAndPartKey}_$documentPartKey-label").text() mustBe
        messages(documentPart)

      view.getElementById(s"${documentIdentifierAndPartKey}_$documentPartKey").attr("value") mustBe empty
    }

    "display empty input with label for Document status" in {

      val view = createView()

      view.getElementById(s"$documentStatusKey-label").text() mustBe messages(documentStatus)
      view.getElementById(s"$documentStatusKey").attr("value") mustBe empty
    }

    "display empty input with label for Document status reason" in {

      val view = createView()

      view.getElementById(s"$documentStatusReasonKey-label").text() mustBe messages(documentStatusReason)
      view.getElementById(s"$documentStatusReasonKey").attr("value") mustBe empty
    }

    "display empty input with label for Issuing Authority Name" in {

      val view = createView()

      view.getElementById(s"$issuingAuthorityNameKey-label").text() mustBe messages(issuingAuthorityName)
      view.getElementById(issuingAuthorityNameKey).attr("value") mustBe empty
    }

    "display empty input with label for Date of Validity" in {

      val view = createView()

      view.getElementById(s"$dateOfValidityKey-label").text() mustBe messages(dateOfValidity)
      view.getElementById(dateOfValidityKey).attr("value") mustBe empty
    }

    "display empty input with label for Measurement Unit" in {

      val view = createView()

      view.getElementById(s"${documentWriteOffKey}_$measurementUnitKey-label").text() mustBe messages(measurementUnit)
      view.getElementById(s"${documentWriteOffKey}_$measurementUnitKey").attr("value") mustBe empty
    }

    "display empty input with label for Document quantity" in {

      val view = createView()

      view.getElementById(s"${documentWriteOffKey}_$documentQuantityKey-label").text() mustBe
        messages(documentQuantity)

      view.getElementById(s"${documentWriteOffKey}_$documentQuantityKey").attr("value") mustBe empty
    }

    "display 'Back' button that links to 'Additional Information' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() mustBe messages(backCaption)
      backButton.attr("href") must endWith(s"/items/$itemId/additional-information")
    }

    "display both 'Add' and 'Save and continue' button on page" in {
      val view = createView()

      val addButton = view.getElementById("add")
      addButton.text() mustBe messages(addCaption)

      val saveAndContinueButton = view.getElementById("submit")
      saveAndContinueButton.text() mustBe messages(saveAndContinueCaption)

      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton.text() mustBe messages(saveAndReturnCaption)
      saveAndReturnButton.attr("name") mustBe SaveAndReturn.toString
    }
  }

  "Documents Produced View for invalid input" should {

    "display error for Document type code" in {

      val view = createView(
        DocumentsProduced.form
          .fillAndValidate(
            correctDocumentsProduced.copy(documentTypeCode = Some(TestHelper.createRandomAlphanumericString(5)))
          )
      )

      checkErrorsSummary(view)
      view must haveFieldErrorLink(s"$documentTypeCodeKey", s"#$documentTypeCodeKey")

      view.select(s"#error-message-$documentTypeCodeKey-input").text() mustBe messages(documentTypeCodeError)
    }

    "display error for Document identifier" in {

      val documentsProducedWithIncorrectDocumentIdentifier = correctDocumentsProduced.copy(
        documentIdentifierAndPart = Some(
          correctDocumentIdentifierAndPart
            .copy(documentIdentifier = Some(incorrectDocumentIdentifierAndPart.documentIdentifier.get))
        )
      )
      val view =
        createView(DocumentsProduced.form.bind(Json.toJson(documentsProducedWithIncorrectDocumentIdentifier)))

      checkErrorsSummary(view)
      view must haveFieldErrorLink(
        s"$documentIdentifierAndPartKey.$documentIdentifierKey",
        s"#${documentIdentifierAndPartKey}_$documentIdentifierKey"
      )

      view
        .select(s"#error-message-${documentIdentifierAndPartKey}_$documentIdentifierKey-input")
        .text() mustBe messages(documentIdentifierError)
    }

    "display error for Document part" in {

      val documentsProducedWithIncorrectDocumentPart = correctDocumentsProduced.copy(
        documentIdentifierAndPart = Some(
          correctDocumentIdentifierAndPart
            .copy(documentPart = Some(incorrectDocumentIdentifierAndPart.documentPart.get))
        )
      )
      val view = createView(DocumentsProduced.form.bind(Json.toJson(documentsProducedWithIncorrectDocumentPart)))

      checkErrorsSummary(view)
      view must haveFieldErrorLink(
        s"$documentIdentifierAndPartKey.$documentPartKey",
        s"#${documentIdentifierAndPartKey}_$documentPartKey"
      )

      view.select(s"#error-message-${documentIdentifierAndPartKey}_$documentPartKey-input").text() mustBe
        messages(documentPartError)
    }

    "display error for Document Identifier and Part" when {

      "provided with Document Identifier but no Document Part" in {

        val documentsProducedWithIncorrectDocumentIdentifierAndPart = correctDocumentsProduced.copy(
          documentIdentifierAndPart = Some(
            emptyDocumentIdentifierAndPart
              .copy(documentIdentifier = Some(correctDocumentIdentifierAndPart.documentIdentifier.get))
          )
        )
        val view =
          createView(DocumentsProduced.form.bind(Json.toJson(documentsProducedWithIncorrectDocumentIdentifierAndPart)))

        checkErrorsSummary(view)
        view must haveFieldErrorLink(s"$documentIdentifierAndPartKey", s"#$documentIdentifierAndPartKey")

        view.select(s"#error-message-$documentIdentifierAndPartKey-input").text() mustBe
          messages(documentIdentifierAndPartError)
      }

      "provided with Document Part but no Document Identifier" in {

        val documentsProducedWithIncorrectDocumentIdentifierAndPart = correctDocumentsProduced.copy(
          documentIdentifierAndPart = Some(
            emptyDocumentIdentifierAndPart
              .copy(documentPart = Some(correctDocumentIdentifierAndPart.documentPart.get))
          )
        )
        val view =
          createView(DocumentsProduced.form.bind(Json.toJson(documentsProducedWithIncorrectDocumentIdentifierAndPart)))

        checkErrorsSummary(view)
        view must haveFieldErrorLink(s"$documentIdentifierAndPartKey", s"#$documentIdentifierAndPartKey")

        view.select(s"#error-message-$documentIdentifierAndPartKey-input").text() mustBe
          messages(documentIdentifierAndPartError)
      }
    }

    "display error for Document status" in {

      val view = createView(
        DocumentsProduced.form
          .fillAndValidate(correctDocumentsProduced.copy(documentStatus = Some("ABC")))
      )

      checkErrorsSummary(view)
      view must haveFieldErrorLink(s"$documentStatusKey", s"#$documentStatusKey")

      view.select(s"#error-message-$documentStatusKey-input").text() mustBe messages(documentStatusError)
    }

    "display error for Document status reason" in {

      val view = createView(
        DocumentsProduced.form
          .fillAndValidate(
            correctDocumentsProduced.copy(documentStatusReason = Some(TestHelper.createRandomAlphanumericString(36)))
          )
      )

      checkErrorsSummary(view)
      view must haveFieldErrorLink(s"$documentStatusReasonKey", s"#$documentStatusReasonKey")

      view.select(s"#error-message-$documentStatusReasonKey-input").text() mustBe messages(documentStatusReasonError)
    }

    "display error for Issuing Authority Name" in {

      val view = createView(
        DocumentsProduced.form
          .fillAndValidate(
            correctDocumentsProduced.copy(issuingAuthorityName = Some(TestHelper.createRandomAlphanumericString(71)))
          )
      )

      checkErrorsSummary(view)
      view must haveFieldErrorLink(s"$issuingAuthorityNameKey", s"#$issuingAuthorityNameKey")

      view.select(s"#error-message-$issuingAuthorityNameKey-input").text() mustBe
        messages(issuingAuthorityNameLengthError)
    }

    "display error for Date of Validity" when {

      "year is out of range (2000-2099)" in {

        val view = createView(
          DocumentsProduced.form
            .bind(
              correctDocumentsProducedMap ++ Map(
                s"$dateOfValidityKey.$yearKey" -> "1999",
                s"$dateOfValidityKey.$monthKey" -> "12",
                s"$dateOfValidityKey.$dayKey" -> "30"
              )
            )
        )

        checkErrorsSummary(view)
        view must haveFieldErrorLink(s"$dateOfValidityKey", s"#$dateOfValidityKey")

        view.select(s"#error-message-$dateOfValidityKey-input").text() mustBe messages(dateOutOfRangeError)
      }

      "provided with non-existing month and day" in {

        val view = createView(
          DocumentsProduced.form
            .bind(
              correctDocumentsProducedMap ++ Map(
                s"$dateOfValidityKey.$monthKey" -> "13",
                s"$dateOfValidityKey.$dayKey" -> "32"
              )
            )
        )

        checkErrorsSummary(view)
        view must haveFieldErrorLink(s"$dateOfValidityKey", s"#$dateOfValidityKey")

        view.select(s"#error-message-$dateOfValidityKey-input").text() mustBe messages(dateFormatError)
      }
    }

    "display error for Measurement Unit" in {

      val documentsProducedWithIncorrectMeasurementUnit = correctDocumentsProduced.copy(
        documentWriteOff =
          Some(correctDocumentWriteOff.copy(measurementUnit = incorrectDocumentWriteOff.measurementUnit))
      )
      val view = createView(DocumentsProduced.form.bind(Json.toJson(documentsProducedWithIncorrectMeasurementUnit)))

      checkErrorsSummary(view)
      view must haveFieldErrorLink(
        s"$documentWriteOffKey.$measurementUnitKey",
        s"#${documentWriteOffKey}_$measurementUnitKey"
      )

      view.select(s"#error-message-${documentWriteOffKey}_$measurementUnitKey-input").text() mustBe
        messages(measurementUnitLengthError)
    }

    "display error for Document quantity" in {

      val documentsProducedWithIncorrectDocumentQuantity = correctDocumentsProduced.copy(
        documentWriteOff =
          Some(correctDocumentWriteOff.copy(documentQuantity = incorrectDocumentWriteOff.documentQuantity))
      )
      val view = createView(DocumentsProduced.form.bind(Json.toJson(documentsProducedWithIncorrectDocumentQuantity)))

      checkErrorsSummary(view)
      view must haveFieldErrorLink(
        s"$documentWriteOffKey.$documentQuantityKey",
        s"#${documentWriteOffKey}_$documentQuantityKey"
      )

      view.select(s"#error-message-${documentWriteOffKey}_$documentQuantityKey-input").text() mustBe
        messages(documentQuantityPrecisionError)
    }

    "display error for Document WriteOff" when {

      "provided with Measurement Unit but no Document Quantity" in {

        val documentsProducedWithIncorrectDocumentWriteOff = correctDocumentsProduced.copy(
          documentWriteOff = Some(emptyDocumentWriteOff.copy(measurementUnit = correctDocumentWriteOff.measurementUnit))
        )
        val view =
          createView(DocumentsProduced.form.bind(Json.toJson(documentsProducedWithIncorrectDocumentWriteOff)))

        checkErrorsSummary(view)
        view must haveFieldErrorLink(s"$documentWriteOffKey", s"#$documentWriteOffKey")

        view.select(s"#error-message-$documentWriteOffKey-input").text() mustBe
          messages(measurementUnitAndQuantityError)
      }

      "provided with Document Quantity but no Measurement Unit" in {

        val documentsProducedWithIncorrectDocumentWriteOff = correctDocumentsProduced.copy(
          documentWriteOff =
            Some(emptyDocumentWriteOff.copy(documentQuantity = correctDocumentWriteOff.documentQuantity))
        )
        val view =
          createView(DocumentsProduced.form.bind(Json.toJson(documentsProducedWithIncorrectDocumentWriteOff)))

        checkErrorsSummary(view)
        view must haveFieldErrorLink(s"$documentWriteOffKey", s"#$documentWriteOffKey")

        view.select(s"#error-message-$documentWriteOffKey-input").text() mustBe
          messages(measurementUnitAndQuantityError)
      }
    }

    "display errors for all fields" in {

      val form = DocumentsProduced.form.bind(incorrectDocumentsProducedMap)

      val view = createView(form)

      checkErrorsSummary(view)
      view must haveFieldErrorLink(s"$documentTypeCodeKey", s"#$documentTypeCodeKey")
      view must haveFieldErrorLink(
        s"$documentIdentifierAndPartKey.$documentIdentifierKey",
        s"#${documentIdentifierAndPartKey}_$documentIdentifierKey"
      )
      view must haveFieldErrorLink(
        s"$documentIdentifierAndPartKey.$documentIdentifierKey",
        s"#${documentIdentifierAndPartKey}_$documentIdentifierKey"
      )
      view must haveFieldErrorLink(s"$documentStatusKey", s"#$documentStatusKey")
      view must haveFieldErrorLink(s"$documentStatusReasonKey", s"#$documentStatusReasonKey")
      view must haveFieldErrorLink(s"$issuingAuthorityNameKey", s"#$issuingAuthorityNameKey")
      view must haveFieldErrorLink(s"$dateOfValidityKey", s"#$dateOfValidityKey")
      view must haveFieldErrorLink(
        s"$documentWriteOffKey.$measurementUnitKey",
        s"#${documentWriteOffKey}_$measurementUnitKey"
      )
      view must haveFieldErrorLink(
        s"$documentWriteOffKey.$documentQuantityKey",
        s"#${documentWriteOffKey}_$documentQuantityKey"
      )

      view.select(s"#error-message-$documentTypeCodeKey-input").text() mustBe messages(documentTypeCodeError)
      view
        .select(s"#error-message-${documentIdentifierAndPartKey}_$documentIdentifierKey-input")
        .text() mustBe messages(documentIdentifierError)
      view.select(s"#error-message-${documentIdentifierAndPartKey}_$documentPartKey-input").text() mustBe
        messages(documentPartError)
      view.select(s"#error-message-$documentStatusKey-input").text() mustBe messages(documentStatusError)
      view.select(s"#error-message-$documentStatusReasonKey-input").text() mustBe messages(documentStatusReasonError)
      view.select(s"#error-message-$issuingAuthorityNameKey-input").text() mustBe
        messages(issuingAuthorityNameLengthError)
      view.select(s"#error-message-$dateOfValidityKey-input").text() mustBe messages(dateFormatError)
      view.select(s"#error-message-${documentWriteOffKey}_$measurementUnitKey-input").text() mustBe
        messages(measurementUnitLengthError)
      view.select(s"#error-message-${documentWriteOffKey}_$documentQuantityKey-input").text() mustBe
        messages(documentQuantityPrecisionError)
    }
  }

  "Documents Produced View when filled" should {

    "display data in all inputs" in {

      val data = correctDocumentsProduced
      val form = DocumentsProduced.form.fill(data)
      val view = createView(form)

      view.getElementById(documentTypeCodeKey).attr("value") must equal(data.documentTypeCode.value)
      view.getElementById(s"${documentIdentifierAndPartKey}_$documentIdentifierKey").attr("value") must equal(
        data.documentIdentifierAndPart.value.documentIdentifier.value
      )
      view.getElementById(s"${documentIdentifierAndPartKey}_$documentPartKey").attr("value") must equal(
        data.documentIdentifierAndPart.value.documentPart.value
      )
      view.getElementById(documentStatusKey).attr("value") must equal(data.documentStatus.value)
      view.getElementById(documentStatusReasonKey).attr("value") must equal(data.documentStatusReason.value)
      view.getElementById(issuingAuthorityNameKey).attr("value") must equal(data.issuingAuthorityName.value)
      view.getElementById(s"${dateOfValidityKey}_$dayKey").attr("value") must equal(
        data.dateOfValidity.value.day.value.toString
      )
      view.getElementById(s"${dateOfValidityKey}_$monthKey").attr("value") must equal(
        data.dateOfValidity.value.month.value.toString
      )
      view.getElementById(s"${dateOfValidityKey}_$yearKey").attr("value") must equal(
        data.dateOfValidity.value.year.value.toString
      )
      view.getElementById(s"${documentWriteOffKey}_$measurementUnitKey").attr("value") must equal(
        data.documentWriteOff.get.measurementUnit.value
      )
      view.getElementById(s"${documentWriteOffKey}_$documentQuantityKey").attr("value") must equal(
        data.documentWriteOff.get.documentQuantity.value.toString
      )
    }

    "display a table with previously entered document" in {

      val view = createView(cachedDocuments = Seq(correctDocumentsProduced))

      view.select("table.form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(1)").text() must equal(
        messages(documentTypeCode)
      )
      view.select("table.form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(2)").text() must equal(
        messages(documentIdentifier)
      )
      view.select("table.form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(3)").text() must equal(
        messages(documentPart)
      )
      view.select("table.form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(4)").text() must equal(
        messages(documentStatus)
      )
      view.select("table.form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(5)").text() must equal(
        messages(documentStatusReason)
      )
      view.select("table.form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(6)").text() must equal(
        messages(issuingAuthorityName)
      )
      view.select("table.form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(7)").text() must equal(
        messages(dateOfValidity)
      )
      view.select("table.form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(8)").text() must equal(
        messages(measurementUnit)
      )
      view.select("table.form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(9)").text() must equal(
        messages(documentQuantity)
      )

      view.select("table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(1)").text() must equal(
        correctDocumentsProduced.documentTypeCode.get
      )
      view.select("table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(2)").text() must equal(
        correctDocumentsProduced.documentIdentifierAndPart.get.documentIdentifier.get
      )
      view.select("table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(3)").text() must equal(
        correctDocumentsProduced.documentIdentifierAndPart.get.documentPart.get
      )
      view.select("table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(4)").text() must equal(
        correctDocumentsProduced.documentStatus.get
      )
      view.select("table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(5)").text() must equal(
        correctDocumentsProduced.documentStatusReason.get
      )
      view.select("table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(6)").text() must equal(
        correctDocumentsProduced.issuingAuthorityName.get
      )
      view.select("table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(7)").text() must equal(
        correctDocumentsProduced.dateOfValidity.get.toString
      )
      view.select("table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(8)").text() must equal(
        correctDocumentsProduced.documentWriteOff.get.measurementUnit.get
      )
      view.select("table.form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(9)").text() must equal(
        correctDocumentsProduced.documentWriteOff.get.documentQuantity.get.toString
      )

      val removeButton = view.select("tbody>tr>td:nth-child(10)>button")

      removeButton.text() mustBe messages("site.remove")
      removeButton.attr("value") mustBe correctDocumentsProduced.toJson.toString()
    }
  }
}
