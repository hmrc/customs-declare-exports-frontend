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
import views.html.declaration.documents_produced
import views.tags.ViewTest

@ViewTest
class DocumentsProducedViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector with OptionValues {

  private val itemId = "a7sc78"
  private val mode = Mode.Normal

  private val form: Form[DocumentsProduced] = DocumentsProduced.form()
  private val documentsProducedPage = instanceOf[documents_produced]
  private def createView(form: Form[DocumentsProduced] = form, cachedDocuments: Seq[DocumentsProduced] = Seq())(
    implicit request: JourneyRequest[_]
  ): Document =
    documentsProducedPage(mode, itemId, form, cachedDocuments)(request, messages)

  "Document Produced" should {

    "have correct message keys" in {

      val messages = instanceOf[MessagesApi].preferred(request)
      messages must haveTranslationFor("supplementary.addDocument.documentTypeCode")
      messages must haveTranslationFor("supplementary.addDocument.documentIdentifier")
      messages must haveTranslationFor("supplementary.addDocument.documentIdentifier.hint")
      messages must haveTranslationFor("supplementary.addDocument.documentIdentifier.error")
      messages must haveTranslationFor("supplementary.addDocument.documentStatusReason")
      messages must haveTranslationFor("supplementary.addDocument.documentStatusReason.error")
      messages must haveTranslationFor("supplementary.addDocument.documentQuantity")
      messages must haveTranslationFor("supplementary.addDocument.documentQuantity.error")
      messages must haveTranslationFor("supplementary.addDocument.documentStatus")
      messages must haveTranslationFor("supplementary.addDocument.documentStatus.error")
      messages must haveTranslationFor("supplementary.addDocument.issuingAuthorityName")
      messages must haveTranslationFor("supplementary.addDocument.issuingAuthorityName.error.length")
      messages must haveTranslationFor("supplementary.addDocument.error.maximumAmount")
      messages must haveTranslationFor("supplementary.addDocument.error.duplicated")
      messages must haveTranslationFor("supplementary.addDocument.error.notDefined")
    }
  }

  "Documents Produced View on empty page" should {
    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1").text() mustBe messagesKey("supplementary.addDocument.title")
      }

      "display section header" in {
        view.getElementById("section-header").text() must include("supplementary.summary.yourReferences.header")
      }

      "display empty input with label for Document type code" in {
        view.getElementsByAttributeValue("for", documentTypeCodeKey).text() mustBe messagesKey("supplementary.addDocument.documentTypeCode")
        view.getElementById(s"$documentTypeCodeKey-hint").text() mustBe messagesKey("supplementary.addDocument.documentTypeCode.hint")
        view.getElementById(documentTypeCodeKey).attr("value") mustBe empty
      }

      "display empty input with label and hint for Document identifier" in {
        view.getElementsByAttributeValue("for", documentIdentifierKey).text() mustBe messagesKey("supplementary.addDocument.documentIdentifier")
        view.getElementById(s"$documentIdentifierKey-hint").text() mustBe messagesKey("supplementary.addDocument.documentIdentifier.hint")
        view.getElementById(documentIdentifierKey).attr("value") mustBe empty
      }

      "display empty input with label for Document status" in {
        view.getElementsByAttributeValue("for", documentStatusKey).text() mustBe messagesKey("supplementary.addDocument.documentStatus")
        view.getElementById(s"$documentStatusKey-hint").text() mustBe messagesKey("supplementary.addDocument.documentStatus.hint")
        view.getElementById(s"$documentStatusKey").attr("value") mustBe empty
      }

      "display empty input with label for Document status reason" in {
        view.getElementsByAttributeValue("for", documentStatusReasonKey).text() mustBe messagesKey("supplementary.addDocument.documentStatusReason")
        view.getElementById(s"$documentStatusReasonKey-hint").text() mustBe messagesKey("supplementary.addDocument.documentStatusReason.hint")
        view.getElementById(documentStatusReasonKey).attr("value") mustBe empty
      }

      "display empty input with label for Issuing Authority Name" in {
        view.getElementsByAttributeValue("for", issuingAuthorityNameKey).text() mustBe messagesKey("supplementary.addDocument.issuingAuthorityName")
        view.getElementById(s"$issuingAuthorityNameKey-hint").text() mustBe messagesKey("supplementary.addDocument.issuingAuthorityName.hint")
        view.getElementById(issuingAuthorityNameKey).attr("value") mustBe empty
      }

      "display empty input with label for Date of Validity" in {
        view.getElementById(dateOfValidityKey).getElementsByTag("legend").text() mustBe messagesKey("supplementary.addDocument.dateOfValidity")
        view.getElementById(s"$dateOfValidityKey-hint").text() mustBe messagesKey("supplementary.addDocument.dateOfValidity.hint")
      }

      "display empty input with label for Measurement Unit" in {
        view.getElementById("measurementUnitAndQualifier").getElementsByTag("legend").text() mustBe messagesKey(
          "supplementary.addDocument.measurementUnit.header"
        )
        view.getElementsByAttributeValue("for", s"${documentWriteOffKey}_$measurementUnitKey").text() mustBe messagesKey(
          "supplementary.addDocument.measurementUnit"
        )
        view.getElementById(s"${documentWriteOffKey}_$measurementUnitKey").attr("value") mustBe empty
        view.getElementsByAttributeValue("for", s"${documentWriteOffKey}_$qualifierKey").text() mustBe messagesKey(
          "supplementary.addDocument.qualifier"
        )
        view.getElementById(s"${documentWriteOffKey}_$qualifierKey").attr("value") mustBe empty
      }

      "display empty input with label for Document quantity" in {
        view.getElementsByAttributeValue("for", s"${documentWriteOffKey}_$documentQuantityKey").text() mustBe
          messagesKey("supplementary.addDocument.documentQuantity")
        view.getElementById(s"${documentWriteOffKey}_$documentQuantityKey").attr("value") mustBe empty
      }

      "display 'Back' button that links to 'Additional Information' page" in {

        val backButton = view.getElementById("back-link")

        backButton.text() mustBe messagesKey(backCaption)
        backButton must haveHref(routes.AdditionalInformationController.displayPage(mode, itemId))
      }

      "display both 'Add' and 'Save and continue' button on page" in {
        val addButton = view.getElementById("add")
        addButton.text() mustBe "site.addsupplementary.addDocument.add.hint"

        val saveAndContinueButton = view.getElementById("submit")
        saveAndContinueButton.text() mustBe messagesKey(saveAndContinueCaption)

        val saveAndReturnButton = view.getElementById("submit_and_return")
        saveAndReturnButton.text() mustBe messagesKey(saveAndReturnCaption)
        saveAndReturnButton must haveAttribute("name", SaveAndReturn.toString)
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

        view must containErrorElementWithMessage("supplementary.addDocument.documentTypeCode.error")
      }

      "display error for Document identifier" in {

        val documentsProducedWithIncorrectDocumentIdentifier =
          correctDocumentsProduced.copy(documentIdentifier = Some("!@#$%"))
        val view =
          createView(DocumentsProduced.form.bind(Json.toJson(documentsProducedWithIncorrectDocumentIdentifier)))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentIdentifierKey")

        view must containErrorElementWithMessage("supplementary.addDocument.documentIdentifier.error")
      }

      "display error for Document status" in {

        val view = createView(
          DocumentsProduced.form
            .fillAndValidate(correctDocumentsProduced.copy(documentStatus = Some("ABC")))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentStatusKey")

        view must containErrorElementWithMessage("supplementary.addDocument.documentStatus.error")
      }

      "display error for Document status reason" in {

        val view = createView(
          DocumentsProduced.form
            .fillAndValidate(correctDocumentsProduced.copy(documentStatusReason = Some(TestHelper.createRandomAlphanumericString(36))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentStatusReasonKey")

        view must containErrorElementWithMessage("supplementary.addDocument.documentStatusReason.error")
      }

      "display error for Issuing Authority Name" in {

        val view = createView(
          DocumentsProduced.form
            .fillAndValidate(correctDocumentsProduced.copy(issuingAuthorityName = Some(TestHelper.createRandomAlphanumericString(71))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$issuingAuthorityNameKey")

        view must containErrorElementWithMessage("supplementary.addDocument.issuingAuthorityName.error")
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

          view must containErrorElementWithMessage("supplementary.addDocument.measurementUnit.error")
        }

        "unit text contains special characters" in {
          val documentsProducedWithIncorrectMeasurementUnit =
            correctDocumentsProduced.copy(documentWriteOff = Some(correctDocumentWriteOff.copy(measurementUnit = Some("!@#$"))))
          val view = createView(DocumentsProduced.form.bind(Json.toJson(documentsProducedWithIncorrectMeasurementUnit)))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$measurementUnitKey")

          view must containErrorElementWithMessage("supplementary.addDocument.measurementUnit.error")
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

          view must containErrorElementWithMessage("supplementary.addDocument.documentQuantity.error")
        }

        "there is scale error" in {
          val documentsProducedWithIncorrectDocumentQuantity =
            correctDocumentsProduced.copy(documentWriteOff = Some(correctDocumentWriteOff.copy(documentQuantity = Some(0.000000001D))))
          val view = createView(DocumentsProduced.form.bind(Json.toJson(documentsProducedWithIncorrectDocumentQuantity)))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$documentQuantityKey")

          view must containErrorElementWithMessage("supplementary.addDocument.documentQuantity.error")
        }

        "there is error in quantity" in {
          val documentsProducedWithIncorrectDocumentQuantity =
            correctDocumentsProduced.copy(documentWriteOff = Some(correctDocumentWriteOff.copy(documentQuantity = Some(-1))))
          val view = createView(DocumentsProduced.form.bind(Json.toJson(documentsProducedWithIncorrectDocumentQuantity)))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$documentQuantityKey")

          view must containErrorElementWithMessage("supplementary.addDocument.documentQuantity.error")
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

        view must containErrorElementWithMessage("supplementary.addDocument.documentTypeCode.error")
        view must containErrorElementWithMessage("supplementary.addDocument.documentIdentifier.error")
        view must containErrorElementWithMessage("supplementary.addDocument.documentStatus.error")
        view must containErrorElementWithMessage("supplementary.addDocument.documentStatusReason.error")
        view must containErrorElementWithMessage("supplementary.addDocument.issuingAuthorityName.error")
        view must containErrorElementWithMessage("dateTime.date.error.format")
        view must containErrorElementWithMessage("supplementary.addDocument.measurementUnit.error")
        view must containErrorElementWithMessage("supplementary.addDocument.documentQuantity.error")
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

      "display a table with previously entered document" which {

        val view = createView(cachedDocuments = Seq(correctDocumentsProduced))

        "have header row" that {
          val header = view.getElementById("documents_produced")

          "have header for Document Type" in {
            header.getElementsByClass("govuk-table__header").get(0).text() mustBe messagesKey("supplementary.addDocument.summary.documentTypeCode")
          }

          "have header for Document Identifier" in {
            header.getElementsByClass("govuk-table__header").get(1).text() mustBe messagesKey("supplementary.addDocument.summary.documentIdentifier")
          }

        }

        "have data row" that {

          "have Document Type" in {
            view.getElementById("documents_produced-row0-code").text() must equal(correctDocumentsProduced.documentTypeCode.get)
          }

          "have Document Identifier" in {
            view.getElementById("documents_produced-row0-identifier").text() must equal(correctDocumentsProduced.documentIdentifier.get)
          }

          "have remove button" in {
            val removeButton = view.getElementById("documents_produced-row0-remove_button").selectFirst("button")
            removeButton.text() mustBe messages("site.removesupplementary.addDocument.remove.hint")
            removeButton.attr("value") mustBe correctDocumentsProduced.toJson.toString()
          }
        }
      }
    }
  }
}
