/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.common.Date._
import forms.declaration.DocumentsProducedSpec._
import forms.declaration.additionaldocuments.DocumentWriteOff._
import forms.declaration.additionaldocuments.DocumentWriteOffSpec._
import forms.declaration.additionaldocuments.DocumentsProduced
import forms.declaration.additionaldocuments.DocumentsProduced._
import helpers.views.declaration.CommonMessages
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.documentsProduced.documents_produced_add
import views.tags.ViewTest

@ViewTest
class DocumentsProducedAddViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector with OptionValues {

  private val itemId = "a7sc78"
  private val mode = Mode.Normal

  private val form: Form[DocumentsProduced] = DocumentsProduced.form()
  private val documentsProducedAddPage = instanceOf[documents_produced_add]
  private def createView(form: Form[DocumentsProduced] = form)(implicit request: JourneyRequest[_]): Document =
    documentsProducedAddPage(mode, itemId, form)(request, messages)

  "Document Produced" should {

    "have correct message keys" in {

      val messages = instanceOf[MessagesApi].preferred(request)
      messages must haveTranslationFor("declaration.addDocument.title")
      messages must haveTranslationFor("declaration.addDocument.documentTypeCode")
      messages must haveTranslationFor("declaration.addDocument.documentIdentifier")
      messages must haveTranslationFor("declaration.addDocument.documentIdentifier.hint")
      messages must haveTranslationFor("declaration.addDocument.documentIdentifier.error")
      messages must haveTranslationFor("declaration.addDocument.documentStatusReason")
      messages must haveTranslationFor("declaration.addDocument.documentStatusReason.error")
      messages must haveTranslationFor("declaration.addDocument.documentQuantity")
      messages must haveTranslationFor("declaration.addDocument.documentQuantity.error")
      messages must haveTranslationFor("declaration.addDocument.documentStatus")
      messages must haveTranslationFor("declaration.addDocument.documentStatus.error")
      messages must haveTranslationFor("declaration.addDocument.issuingAuthorityName")
      messages must haveTranslationFor("declaration.addDocument.issuingAuthorityName.error.length")
      messages must haveTranslationFor("declaration.addDocument.error.maximumAmount")
      messages must haveTranslationFor("declaration.addDocument.error.duplicated")
      messages must haveTranslationFor("declaration.addDocument.error.notDefined")
    }
  }

  "Documents Produced View on empty page" should {
    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1").text() mustBe messagesKey("declaration.addDocument.title")
      }

      "display section header" in {
        view.getElementById("section-header").text() must include("supplementary.items")
      }

      "display empty input with label for Document type code" in {
        view.getElementsByAttributeValue("for", documentTypeCodeKey).text() mustBe messagesKey("declaration.addDocument.documentTypeCode")
        view.getElementById(s"$documentTypeCodeKey-hint").text() mustBe messagesKey("declaration.addDocument.documentTypeCode.hint")
        view.getElementById(documentTypeCodeKey).attr("value") mustBe empty
      }

      "display empty input with label and hint for Document identifier" in {
        view.getElementsByAttributeValue("for", documentIdentifierKey).text() mustBe messagesKey("declaration.addDocument.documentIdentifier")
        view.getElementById(s"$documentIdentifierKey-hint").text() mustBe messagesKey("declaration.addDocument.documentIdentifier.hint")
        view.getElementById(documentIdentifierKey).attr("value") mustBe empty
      }

      "display empty input with label for Document status" in {
        view.getElementsByAttributeValue("for", documentStatusKey).text() mustBe messagesKey("declaration.addDocument.documentStatus")
        view.getElementById(s"$documentStatusKey-hint").text() mustBe messagesKey("declaration.addDocument.documentStatus.hint")
        view.getElementById(s"$documentStatusKey").attr("value") mustBe empty
      }

      "display empty input with label for Document status reason" in {
        view.getElementsByAttributeValue("for", documentStatusReasonKey).text() mustBe messagesKey("declaration.addDocument.documentStatusReason")
        view.getElementById(s"$documentStatusReasonKey-hint").text() mustBe messagesKey("declaration.addDocument.documentStatusReason.hint")
        view.getElementById(documentStatusReasonKey).attr("value") mustBe empty
      }

      "display empty input with label for Issuing Authority Name" in {
        view.getElementsByAttributeValue("for", issuingAuthorityNameKey).text() mustBe messagesKey("declaration.addDocument.issuingAuthorityName")
        view.getElementById(s"$issuingAuthorityNameKey-hint").text() mustBe messagesKey("declaration.addDocument.issuingAuthorityName.hint")
        view.getElementById(issuingAuthorityNameKey).attr("value") mustBe empty
      }

      "display empty input with label for Date of Validity" in {
        view.getElementById(dateOfValidityKey).getElementsByTag("legend").text() mustBe messagesKey("declaration.addDocument.dateOfValidity")
        view.getElementById(s"$dateOfValidityKey-input-hint").text() mustBe messagesKey("declaration.addDocument.dateOfValidity.hint")
      }

      "display empty input with label for Measurement Unit" in {
        view.getElementById("measurementUnitAndQualifier").getElementsByTag("legend").text() mustBe messagesKey(
          "declaration.addDocument.measurementUnit.header"
        )
        view.getElementsByAttributeValue("for", s"${documentWriteOffKey}_$measurementUnitKey").text() mustBe messagesKey(
          "declaration.addDocument.measurementUnit"
        )
        view.getElementById(s"${documentWriteOffKey}_$measurementUnitKey").attr("value") mustBe empty
        view.getElementsByAttributeValue("for", s"${documentWriteOffKey}_$qualifierKey").text() mustBe messagesKey(
          "declaration.addDocument.qualifier"
        )
        view.getElementById(s"${documentWriteOffKey}_$qualifierKey").attr("value") mustBe empty
      }

      "display empty input with label for Document quantity" in {
        view.getElementsByAttributeValue("for", s"${documentWriteOffKey}_$documentQuantityKey").text() mustBe
          messagesKey("declaration.addDocument.documentQuantity")
        view.getElementById(s"${documentWriteOffKey}_$documentQuantityKey").attr("value") mustBe empty
      }

      "display 'Back' button that links to 'Additional Information Required' page when no additional info present" in {

        val backButton = view.getElementById("back-link")

        backButton.text() mustBe messagesKey(backCaption)
        backButton must haveHref(routes.AdditionalInformationRequiredController.displayPage(mode, itemId))
      }

      "display'Save and continue' button on page" in {
        val saveAndContinueButton = view.getElementById("submit")
        saveAndContinueButton.text() mustBe messagesKey(saveAndContinueCaption)

        val saveAndReturnButton = view.getElementById("submit_and_return")
        saveAndReturnButton.text() mustBe messagesKey(saveAndReturnCaption)
        saveAndReturnButton must haveAttribute("name", SaveAndReturn.toString)
      }
    }
  }

  "Documents Produced View on empty page with cached Additional Information" should {
    onEveryDeclarationJourney(withItem(anItem(withItemId(itemId), withAdditionalInformation("1234", "Description")))) { implicit request =>
      val view = createView()

      "display 'Back' button that links to 'Additional Information' page when additional info present" in {

        val backButton = view.getElementById("back-link")

        backButton.text() mustBe messagesKey(backCaption)
        backButton must haveHref(routes.AdditionalInformationController.displayPage(mode, itemId))
      }

    }
  }

  "Documents Produced View for invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
      "display error for Document type code" in {

        val view = createView(
          DocumentsProduced.form
            .fillAndValidate(correctDocumentsProduced.copy(documentTypeCode = Some(TestHelper.createRandomAlphanumericString(5))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentTypeCodeKey")

        view must containErrorElementWithMessage("declaration.addDocument.documentTypeCode.error")
      }

      "display error for Document identifier" in {

        val documentsProducedWithIncorrectDocumentIdentifier =
          correctDocumentsProduced.copy(documentIdentifier = Some("!@#$%"))
        val view =
          createView(DocumentsProduced.form.bind(Json.toJson(documentsProducedWithIncorrectDocumentIdentifier)))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentIdentifierKey")

        view must containErrorElementWithMessage("declaration.addDocument.documentIdentifier.error")
      }

      "display error for Document status" in {

        val view = createView(
          DocumentsProduced.form
            .fillAndValidate(correctDocumentsProduced.copy(documentStatus = Some("ABC")))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentStatusKey")

        view must containErrorElementWithMessage("declaration.addDocument.documentStatus.error")
      }

      "display error for Document status reason" in {

        val view = createView(
          DocumentsProduced.form
            .fillAndValidate(correctDocumentsProduced.copy(documentStatusReason = Some(TestHelper.createRandomAlphanumericString(36))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentStatusReasonKey")

        view must containErrorElementWithMessage("declaration.addDocument.documentStatusReason.error")
      }

      "display error for Issuing Authority Name" in {

        val view = createView(
          DocumentsProduced.form
            .fillAndValidate(correctDocumentsProduced.copy(issuingAuthorityName = Some(TestHelper.createRandomAlphanumericString(71))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$issuingAuthorityNameKey")

        view must containErrorElementWithMessage("declaration.addDocument.issuingAuthorityName.error")
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

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#$dateOfValidityKey")

          view must containErrorElementWithMessage("dateTime.date.error.outOfRange")
        }

        "provided with non-existing month and day" in {

          val view = createView(
            DocumentsProduced.form
              .bind(correctDocumentsProducedMap ++ Map(s"$dateOfValidityKey.$monthKey" -> "13", s"$dateOfValidityKey.$dayKey" -> "32"))
          )

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#$dateOfValidityKey")

          view must containErrorElementWithMessage("dateTime.date.error.format")
        }

        "provided with partial date" in {

          val view = createView(
            DocumentsProduced.form
              .bind(Map(s"$dateOfValidityKey.$monthKey" -> "12", s"$dateOfValidityKey.$dayKey" -> "25"))
          )

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${dateOfValidityKey}_$yearKey")

          view must containErrorElementWithMessage("dateTime.date.year.error.empty")
        }
      }

      "display error for Measurement Unit" when {

        "unit text is too long" in {
          val documentsProducedWithIncorrectMeasurementUnit = correctDocumentsProduced.copy(
            documentWriteOff = Some(correctDocumentWriteOff.copy(measurementUnit = incorrectDocumentWriteOff.measurementUnit))
          )
          val view = createView(DocumentsProduced.form.bind(Json.toJson(documentsProducedWithIncorrectMeasurementUnit)))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$measurementUnitKey")

          view must containErrorElementWithMessage("declaration.addDocument.measurementUnit.error")
        }

        "unit text contains special characters" in {
          val documentsProducedWithIncorrectMeasurementUnit =
            correctDocumentsProduced.copy(documentWriteOff = Some(correctDocumentWriteOff.copy(measurementUnit = Some("!@#$"))))
          val view = createView(DocumentsProduced.form.bind(Json.toJson(documentsProducedWithIncorrectMeasurementUnit)))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$measurementUnitKey")

          view must containErrorElementWithMessage("declaration.addDocument.measurementUnit.error")
        }

      }

      "display error for Document quantity" when {

        "there is precession error" in {
          val documentsProducedWithIncorrectDocumentQuantity = correctDocumentsProduced.copy(
            documentWriteOff = Some(correctDocumentWriteOff.copy(documentQuantity = incorrectDocumentWriteOff.documentQuantity))
          )
          val view = createView(DocumentsProduced.form.bind(Json.toJson(documentsProducedWithIncorrectDocumentQuantity)))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$documentQuantityKey")

          view must containErrorElementWithMessage("declaration.addDocument.documentQuantity.error")
        }

        "there is scale error" in {
          val documentsProducedWithIncorrectDocumentQuantity =
            correctDocumentsProduced.copy(documentWriteOff = Some(correctDocumentWriteOff.copy(documentQuantity = Some(0.000000001D))))
          val view = createView(DocumentsProduced.form.bind(Json.toJson(documentsProducedWithIncorrectDocumentQuantity)))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$documentQuantityKey")

          view must containErrorElementWithMessage("declaration.addDocument.documentQuantity.error")
        }

        "there is error in quantity" in {
          val documentsProducedWithIncorrectDocumentQuantity =
            correctDocumentsProduced.copy(documentWriteOff = Some(correctDocumentWriteOff.copy(documentQuantity = Some(-1))))
          val view = createView(DocumentsProduced.form.bind(Json.toJson(documentsProducedWithIncorrectDocumentQuantity)))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$documentQuantityKey")

          view must containErrorElementWithMessage("declaration.addDocument.documentQuantity.error")
        }
      }

      "display errors for all fields" in {

        val form = DocumentsProduced.form.bind(incorrectDocumentsProducedMap)

        val view = createView(form)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#${documentTypeCodeKey}")
        view must containErrorElementWithTagAndHref("a", s"#${documentIdentifierKey}")
        view must containErrorElementWithTagAndHref("a", s"#${documentStatusKey}")
        view must containErrorElementWithTagAndHref("a", s"#${documentStatusReasonKey}")
        view must containErrorElementWithTagAndHref("a", s"#${issuingAuthorityNameKey}")
        view must containErrorElementWithTagAndHref("a", s"#${dateOfValidityKey}")
        view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$measurementUnitKey")
        view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$documentQuantityKey")

        view must containErrorElementWithMessage("declaration.addDocument.documentTypeCode.error")
        view must containErrorElementWithMessage("declaration.addDocument.documentIdentifier.error")
        view must containErrorElementWithMessage("declaration.addDocument.documentStatus.error")
        view must containErrorElementWithMessage("declaration.addDocument.documentStatusReason.error")
        view must containErrorElementWithMessage("declaration.addDocument.issuingAuthorityName.error")
        view must containErrorElementWithMessage("dateTime.date.error.format")
        view must containErrorElementWithMessage("declaration.addDocument.measurementUnit.error")
        view must containErrorElementWithMessage("declaration.addDocument.documentQuantity.error")
      }
    }
  }

  "Documents Produced View when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display data in all inputs" in {

        val data = correctDocumentsProduced
        val form = DocumentsProduced.form.fill(data)
        val view = createView(form)

        view.getElementById(documentTypeCodeKey).attr("value") must equal(data.documentTypeCode.value)
        view.getElementById(documentIdentifierKey).attr("value") must equal(data.documentIdentifier.value)
        view.getElementById(documentStatusKey).attr("value") must equal(data.documentStatus.value)
        view.getElementById(documentStatusReasonKey).attr("value") must equal(data.documentStatusReason.value)
        view.getElementById(issuingAuthorityNameKey).text() must equal(data.issuingAuthorityName.value)
        view.getElementById(s"${dateOfValidityKey}_$dayKey").attr("value") must equal(data.dateOfValidity.value.day.value.toString)
        view.getElementById(s"${dateOfValidityKey}_$monthKey").attr("value") must equal(data.dateOfValidity.value.month.value.toString)
        view.getElementById(s"${dateOfValidityKey}_$yearKey").attr("value") must equal(data.dateOfValidity.value.year.value.toString)
        view.getElementById(s"${documentWriteOffKey}_$measurementUnitKey").attr("value") must equal(data.documentWriteOff.get.measurementUnit.value)
        view.getElementById(s"${documentWriteOffKey}_$documentQuantityKey").attr("value") must equal(
          data.documentWriteOff.get.documentQuantity.value.toString
        )
      }

    }
  }
}
