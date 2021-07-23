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

package views.declaration

import base.{Injector, TestHelper}
import controllers.util.SaveAndReturn
import forms.common.Date.{dayKey, monthKey, yearKey}
import forms.common.Eori
import forms.declaration.AdditionalDocumentSpec._
import forms.declaration.CommodityDetails
import forms.declaration.additionaldocuments.AdditionalDocument
import forms.declaration.additionaldocuments.AdditionalDocument._
import forms.declaration.additionaldocuments.DocumentWriteOff._
import forms.declaration.additionaldocuments.DocumentWriteOffSpec.{correctDocumentWriteOff, incorrectDocumentWriteOff}
import forms.declaration.declarationHolder.DeclarationHolderAdd
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.additionalDocuments.additional_document_edit

class AdditionalDocumentEditViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector with OptionValues {

  private val itemId = "a7sc78"
  private val mode = Mode.Normal

  private val form: Form[AdditionalDocument] = AdditionalDocument.form()
  private val additionalDocumentEditPage = instanceOf[additional_document_edit]
  private def createView(form: Form[AdditionalDocument] = form)(implicit request: JourneyRequest[_]): Document =
    additionalDocumentEditPage(mode, itemId, form)(request, messages)

  "AdditionalDocument Add/Change Controller" should {

    "have correct message keys" in {
      messages must haveTranslationFor("declaration.additionalDocument.title")
      messages must haveTranslationFor("declaration.additionalDocument.title.fromAuthCode")
      messages must haveTranslationFor("declaration.additionalDocument.hint")
      messages must haveTranslationFor("declaration.additionalDocument.hint.fromAuthCode.paragraph1")
      messages must haveTranslationFor("declaration.additionalDocument.hint.fromAuthCode.paragraph2")

      messages must haveTranslationFor("declaration.additionalDocument.expander.title")
      messages must haveTranslationFor("declaration.additionalDocument.expander.paragraph1.withCommodityCode.text")
      messages must haveTranslationFor("declaration.additionalDocument.expander.paragraph1.withCommodityCode.link1.text")
      messages must haveTranslationFor("declaration.additionalDocument.expander.paragraph1.withoutCommodityCode.text")
      messages must haveTranslationFor("declaration.additionalDocument.expander.paragraph1.withoutCommodityCode.link1.text")
      messages must haveTranslationFor("declaration.additionalDocument.expander.paragraph2.text")
      messages must haveTranslationFor("declaration.additionalDocument.expander.paragraph3.text")
      messages must haveTranslationFor("declaration.additionalDocument.expander.paragraph3.link1.text")

      messages must haveTranslationFor("declaration.additionalDocument.documentTypeCode")
      messages must haveTranslationFor("declaration.additionalDocument.documentTypeCode.hint")
      messages must haveTranslationFor("declaration.additionalDocument.documentTypeCode.hint.fromAuthCode")
      messages must haveTranslationFor("declaration.additionalDocument.documentTypeCode.expander.title")
      messages must haveTranslationFor("declaration.additionalDocument.documentTypeCode.expander.paragraph1.text")
      messages must haveTranslationFor("declaration.additionalDocument.documentTypeCode.expander.paragraph1.link1.text")
      messages must haveTranslationFor("declaration.additionalDocument.documentTypeCode.expander.paragraph2.text")
      messages must haveTranslationFor("declaration.additionalDocument.documentTypeCode.expander.paragraph2.link1.text")
      messages must haveTranslationFor("declaration.additionalDocument.documentTypeCode.expander.paragraph3.text")
      messages must haveTranslationFor("declaration.additionalDocument.documentTypeCode.empty")
      messages must haveTranslationFor("declaration.additionalDocument.documentTypeCode.empty.fromAuthCode")
      messages must haveTranslationFor("declaration.additionalDocument.documentTypeCode.error")

      messages must haveTranslationFor("declaration.additionalDocument.documentIdentifier")
      messages must haveTranslationFor("declaration.additionalDocument.documentIdentifier.hint")
      messages must haveTranslationFor("declaration.additionalDocument.documentIdentifier.inset.fromAuthCode.paragraph1")
      messages must haveTranslationFor("declaration.additionalDocument.documentIdentifier.inset.fromAuthCode.paragraph2")
      messages must haveTranslationFor("declaration.additionalDocument.documentIdentifier.error")

      messages must haveTranslationFor("declaration.additionalDocument.documentStatus")
      messages must haveTranslationFor("declaration.additionalDocument.documentStatus.hint")
      messages must haveTranslationFor("declaration.additionalDocument.documentStatus.error")

      messages must haveTranslationFor("declaration.additionalDocument.documentStatusReason")
      messages must haveTranslationFor("declaration.additionalDocument.documentStatusReason.hint")
      messages must haveTranslationFor("declaration.additionalDocument.documentStatusReason.error")

      messages must haveTranslationFor("declaration.additionalDocument.issuingAuthorityName")
      messages must haveTranslationFor("declaration.additionalDocument.issuingAuthorityName.hint")
      messages must haveTranslationFor("declaration.additionalDocument.issuingAuthorityName.error.length")

      messages must haveTranslationFor("declaration.additionalDocument.dateOfValidity")
      messages must haveTranslationFor("declaration.additionalDocument.dateOfValidity.hint")
      messages must haveTranslationFor("declaration.additionalDocument.dateOfValidity.error.format")
      messages must haveTranslationFor("declaration.additionalDocument.dateOfValidity.error.outOfRange")

      messages must haveTranslationFor("declaration.additionalDocument.measurementUnit.header")
      messages must haveTranslationFor("declaration.additionalDocument.measurementUnit.hint")
      messages must haveTranslationFor("declaration.additionalDocument.measurementUnit.hint.link.text")
      messages must haveTranslationFor("declaration.additionalDocument.measurementUnit.error")
      messages must haveTranslationFor("declaration.additionalDocument.measurementUnit")

      messages must haveTranslationFor("declaration.additionalDocument.qualifier")
      messages must haveTranslationFor("declaration.additionalDocument.qualifier.error")

      messages must haveTranslationFor("declaration.additionalDocument.measurementUnitAndQualifier.error")

      messages must haveTranslationFor("declaration.additionalDocument.documentQuantity")
      messages must haveTranslationFor("declaration.additionalDocument.documentQuantity.hint")
      messages must haveTranslationFor("declaration.additionalDocument.documentQuantity.error")

      messages must haveTranslationFor("declaration.additionalDocument.error.maximumAmount")
      messages must haveTranslationFor("declaration.additionalDocument.error.duplicated")
      messages must haveTranslationFor("declaration.additionalDocument.error.notDefined")

      messages must haveTranslationFor("declaration.additionalDocument.summary.documentTypeCode")
      messages must haveTranslationFor("declaration.additionalDocument.summary.documentIdentifier")
      messages must haveTranslationFor("declaration.additionalDocument.summary.statusCode")

      messages must haveTranslationFor("declaration.additionalDocument.table.heading")
      messages must haveTranslationFor("declaration.additionalDocument.table.multiple.heading")
      messages must haveTranslationFor("declaration.additionalDocument.table.caption")
      messages must haveTranslationFor("declaration.additionalDocument.add.another")
      messages must haveTranslationFor("declaration.additionalDocument.add.another.empty")
      messages must haveTranslationFor("declaration.additionalDocument.remove.empty")
      messages must haveTranslationFor("declaration.additionalDocument.table.change.hint")
      messages must haveTranslationFor("declaration.additionalDocument.table.remove.hint")
      messages must haveTranslationFor("declaration.additionalDocument.remove.title")
      messages must haveTranslationFor("declaration.additionalDocument.remove.code")
      messages must haveTranslationFor("declaration.additionalDocument.remove.reference")
      messages must haveTranslationFor("declaration.additionalDocument.remove.statusCode")
      messages must haveTranslationFor("declaration.additionalDocument.remove.statusReason")
      messages must haveTranslationFor("declaration.additionalDocument.remove.issuingAuthorityName")
      messages must haveTranslationFor("declaration.additionalDocument.remove.dateOfValidity")
      messages must haveTranslationFor("declaration.additionalDocument.remove.measurementUnitAndQualifier")
      messages must haveTranslationFor("declaration.additionalDocument.remove.documentQuantity")
    }
  }

  "additional_document_edit view on empty page" when {

    "the entered authorisation code requires additional documents" should {

      val declarationHolder = DeclarationHolderAdd(Some("OPO"), Some(Eori("GB123456789012")))

      onEveryDeclarationJourney(withDeclarationHolders(declarationHolder)) { implicit request =>
        val view = createView()

        "display the expected page title" in {
          view.getElementsByTag("h1") must containMessageForElements("declaration.additionalDocument.title.fromAuthCode")
        }

        "display the expected page hint" in {
          val hints = view.getElementsByClass("govuk-hint")
          hints.get(0).text() mustBe messages("declaration.additionalDocument.hint.fromAuthCode.paragraph1")
          hints.get(1).text() mustBe messages("declaration.additionalDocument.hint.fromAuthCode.paragraph2")
        }

        "display empty input with label for Document type code" in {
          view.getElementsByAttributeValue("for", documentTypeCodeKey) must containMessageForElements(
            "declaration.additionalDocument.documentTypeCode"
          )
          view.getElementById(s"$documentTypeCodeKey-hint") must containMessage("declaration.additionalDocument.documentTypeCode.hint.fromAuthCode")
          view.getElementById(documentTypeCodeKey).attr("value") mustBe empty
        }

        "display the expected insets placed after the 'Document Identifier' input field" in {
          val insets = view.getElementsByClass("govuk-inset-text").get(0)
          val paragraphs = insets.getElementsByTag("p")
          paragraphs.get(0).text() mustBe messages("declaration.additionalDocument.documentIdentifier.inset.fromAuthCode.paragraph1")
          paragraphs.get(1).text() mustBe messages("declaration.additionalDocument.documentIdentifier.inset.fromAuthCode.paragraph2")

          Option(insets.previousElementSibling().getElementById(documentIdentifierKey)) must not be None
        }
      }
    }
  }

  "additional_document_edit view on empty page" should {

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display the expected page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.additionalDocument.title")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.5")
      }

      "display the expected page hint" in {
        val hints = view.getElementsByClass("govuk-hint")
        hints.get(0).text() mustBe messages("declaration.additionalDocument.hint")
      }

      "display the top expander" when {
        "commodityCode is not present" in {
          val hintElement = view.getElementById("top-expander")

          hintElement must containHtml(messages("declaration.additionalDocument.expander.title"))
          hintElement
            .getElementsByClass("govuk-hint")
            .get(0) must containText(messages("declaration.additionalDocument.expander.paragraph1.withoutCommodityCode.link1.text"))
        }
      }

      "display empty input with label for Document type code" in {
        view.getElementsByAttributeValue("for", documentTypeCodeKey) must containMessageForElements("declaration.additionalDocument.documentTypeCode")
        view.getElementById(s"$documentTypeCodeKey-hint") must containMessage("declaration.additionalDocument.documentTypeCode.hint")
        view.getElementById(documentTypeCodeKey).attr("value") mustBe empty
      }

      "display the documentTypeCode expander" in {
        view.getElementById("documentTypeCode-expander") must containHtml(messages("declaration.additionalDocument.documentTypeCode.expander.title"))
      }

      "display empty input with label and hint for Document identifier" in {
        view.getElementsByAttributeValue("for", documentIdentifierKey) must containMessageForElements(
          "declaration.additionalDocument.documentIdentifier"
        )
        view.getElementById(s"$documentIdentifierKey-hint") must containMessage("declaration.additionalDocument.documentIdentifier.hint")
        view.getElementById(documentIdentifierKey).attr("value") mustBe empty
      }

      "not have any inset text" in {
        view.getElementsByClass("govuk-inset-text").size mustBe 0
      }

      "display empty input with label for Document status" in {
        view.getElementsByAttributeValue("for", documentStatusKey) must containMessageForElements("declaration.additionalDocument.documentStatus")
        view.getElementById(s"$documentStatusKey-hint") must containMessage("declaration.additionalDocument.documentStatus.hint")
        view.getElementById(s"$documentStatusKey").attr("value") mustBe empty
      }

      "display empty input with label for Document status reason" in {
        view.getElementsByAttributeValue("for", documentStatusReasonKey) must containMessageForElements(
          "declaration.additionalDocument.documentStatusReason"
        )
        view.getElementById(s"$documentStatusReasonKey-hint") must containMessage("declaration.additionalDocument.documentStatusReason.hint")
        view.getElementById(documentStatusReasonKey).attr("value") mustBe empty
      }

      "display empty input with label for Issuing Authority Name" in {
        view.getElementsByAttributeValue("for", issuingAuthorityNameKey) must containMessageForElements(
          "declaration.additionalDocument.issuingAuthorityName"
        )
        view.getElementById(s"$issuingAuthorityNameKey-hint") must containMessage("declaration.additionalDocument.issuingAuthorityName.hint")
        view.getElementById(issuingAuthorityNameKey).attr("value") mustBe empty
      }

      "display empty input with label for Date of Validity" in {
        view.getElementById(dateOfValidityKey).getElementsByTag("legend") must containMessageForElements(
          "declaration.additionalDocument.dateOfValidity"
        )
        view.getElementById(s"$dateOfValidityKey-input-hint") must containMessage("declaration.additionalDocument.dateOfValidity.hint")
      }

      "display empty input with label for Measurement Unit" in {
        view.getElementById("measurementUnitAndQualifier").getElementsByTag("legend") must containMessageForElements(
          "declaration.additionalDocument.measurementUnit.header"
        )
        view.getElementsByAttributeValue("for", s"${documentWriteOffKey}_$measurementUnitKey") must containMessageForElements(
          "declaration.additionalDocument.measurementUnit"
        )
        view.getElementById(s"${documentWriteOffKey}_$measurementUnitKey").attr("value") mustBe empty
        view.getElementsByAttributeValue("for", s"${documentWriteOffKey}_$qualifierKey") must containMessageForElements(
          "declaration.additionalDocument.qualifier"
        )
        view.getElementById(s"${documentWriteOffKey}_$qualifierKey").attr("value") mustBe empty
        view.getElementById(s"${documentWriteOffKey}-hint") must containMessage("declaration.additionalDocument.measurementUnit.hint.link.text")
      }

      "display empty input with label for Document quantity" in {
        view.getElementsByAttributeValue("for", s"${documentWriteOffKey}_$documentQuantityKey") must containMessageForElements(
          "declaration.additionalDocument.documentQuantity"
        )
        view.getElementById(s"${documentWriteOffKey}_$documentQuantityKey").attr("value") mustBe empty
        view.getElementById(s"${documentWriteOffKey}_$documentQuantityKey-hint") must containMessage(
          "declaration.additionalDocument.documentQuantity.hint",
          messages("declaration.additionalDocument.measurementUnit.hint.link.text")
        )
      }

      "display'Save and continue' button on page" in {
        val saveAndContinueButton = view.getElementById("submit")
        saveAndContinueButton must containMessage(saveAndContinueCaption)

        val saveAndReturnButton = view.getElementById("submit_and_return")
        saveAndReturnButton must containMessage(saveAndReturnCaption)
        saveAndReturnButton must haveAttribute("name", SaveAndReturn.toString)
      }
    }
  }

  "additional_document_edit view on empty page" should {
    "display the top expander" when {
      val commodityCode = "46021910"
      val item = anItem(withItemId(itemId), withCommodityDetails(CommodityDetails(Some(commodityCode), None)))

      onEveryDeclarationJourney(withItem(item)) { implicit request =>
        "commodityCode is present" in {

          val hintElement = createView().getElementById("top-expander")

          hintElement must containHtml(messages("declaration.additionalDocument.expander.title"))
          hintElement
            .getElementsByClass("govuk-hint")
            .get(0) must containText(messages("declaration.additionalDocument.expander.paragraph1.withCommodityCode.link1.text", commodityCode))
        }
      }
    }
  }

  "additional_document_edit view" should {

    onEveryDeclarationJourney() { implicit request =>
      "display error for invalid Document type code" in {
        val invalidDocumentTypeCode = Some(TestHelper.createRandomAlphanumericString(5))
        val form = AdditionalDocument
          .form()
          .fillAndValidate(correctAdditionalDocument.copy(documentTypeCode = invalidDocumentTypeCode))

        val view = createView(form)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentTypeCodeKey")

        view must containErrorElementWithMessageKey("declaration.additionalDocument.documentTypeCode.error")
      }

      "display error for empty Document type code" in {
        val form = AdditionalDocument.form().fillAndValidate(correctAdditionalDocument.copy(documentTypeCode = None))
        val view = createView(form)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentTypeCodeKey")

        view must containErrorElementWithMessageKey("declaration.additionalDocument.documentTypeCode.empty")
      }

      "display error for empty Document type code when required after the entered authorisation code" in {
        val form = AdditionalDocument
          .form(isAuthCodeRequiringAdditionalDocuments = true)
          .fillAndValidate(correctAdditionalDocument.copy(documentTypeCode = None))

        val view = createView(form)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentTypeCodeKey")

        view must containErrorElementWithMessageKey("declaration.additionalDocument.documentTypeCode.empty.fromAuthCode")
      }

      "display error for invalid Document identifier" in {
        val additionalDocumentWithIncorrectDocumentIdentifier = correctAdditionalDocument.copy(documentIdentifier = Some("!@#$%"))
        val form = AdditionalDocument.form().bind(Json.toJson(additionalDocumentWithIncorrectDocumentIdentifier))
        val view = createView(form)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentIdentifierKey")

        view must containErrorElementWithMessageKey("declaration.additionalDocument.documentIdentifier.error")
      }

      "display error for invalid Document status" in {
        val form = AdditionalDocument.form().fillAndValidate(correctAdditionalDocument.copy(documentStatus = Some("ABC")))
        val view = createView(form)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentStatusKey")

        view must containErrorElementWithMessageKey("declaration.additionalDocument.documentStatus.error")
      }

      "display error for invalid Document status reason" in {
        val documentStatusReason = Some(TestHelper.createRandomAlphanumericString(36))
        val form = AdditionalDocument.form().fillAndValidate(correctAdditionalDocument.copy(documentStatusReason = documentStatusReason))
        val view = createView(form)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentStatusReasonKey")

        view must containErrorElementWithMessageKey("declaration.additionalDocument.documentStatusReason.error")
      }

      "display error for invalid Issuing Authority Name" in {
        val issuingAuthorityName = Some(TestHelper.createRandomAlphanumericString(71))
        val form = AdditionalDocument.form().fillAndValidate(correctAdditionalDocument.copy(issuingAuthorityName = issuingAuthorityName))
        val view = createView(form)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$issuingAuthorityNameKey")

        view must containErrorElementWithMessageKey("declaration.additionalDocument.issuingAuthorityName.error.length")
      }

      "display error for invalid Date of Validity" when {

        "year is out of range (2000-2099)" in {
          val invalidDateOfValidity =
            Map(s"$dateOfValidityKey.$yearKey" -> "1999", s"$dateOfValidityKey.$monthKey" -> "12", s"$dateOfValidityKey.$dayKey" -> "30")
          val form = AdditionalDocument.form().bind(correctAdditionalDocumentMap ++ invalidDateOfValidity)
          val view = createView(form)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#$dateOfValidityKey")

          view must containErrorElementWithMessageKey("declaration.additionalDocument.dateOfValidity.error.outOfRange")
        }

        "provided with non-existing month and day" in {
          val invalidDateOfValidity =
            Map(s"$dateOfValidityKey.$yearKey" -> "2020", s"$dateOfValidityKey.$monthKey" -> "13", s"$dateOfValidityKey.$dayKey" -> "32")
          val form = AdditionalDocument.form().bind(correctAdditionalDocumentMap ++ invalidDateOfValidity)
          val view = createView(form)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#$dateOfValidityKey")

          view must containErrorElementWithMessageKey("declaration.additionalDocument.dateOfValidity.error.format")
        }

        "provided with partial date" in {
          val mapWithInvalidDateOfValidity =
            correctAdditionalDocumentMap -
              s"$dateOfValidityKey.$yearKey" +
              (s"$dateOfValidityKey.$monthKey" -> "12") +
              (s"$dateOfValidityKey.$dayKey" -> "25")

          val form = AdditionalDocument.form().bind(mapWithInvalidDateOfValidity)
          val view = createView(form)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${dateOfValidityKey}_$yearKey")

          view must containErrorElementWithMessageKey("dateTime.date.year.error.empty")
        }
      }

      "display error for invalid Measurement Unit" when {

        "unit text is too long" in {
          val additionalDocumentWithIncorrectMeasurementUnit = correctAdditionalDocument.copy(
            documentWriteOff = Some(correctDocumentWriteOff.copy(measurementUnit = incorrectDocumentWriteOff.measurementUnit))
          )
          val view = createView(AdditionalDocument.form().bind(Json.toJson(additionalDocumentWithIncorrectMeasurementUnit)))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$measurementUnitKey")

          view must containErrorElementWithMessageKey("declaration.additionalDocument.measurementUnit.error")
        }

        "unit text contains special characters" in {
          val additionalDocumentWithIncorrectMeasurementUnit =
            correctAdditionalDocument.copy(documentWriteOff = Some(correctDocumentWriteOff.copy(measurementUnit = Some("!@#$"))))
          val view = createView(AdditionalDocument.form().bind(Json.toJson(additionalDocumentWithIncorrectMeasurementUnit)))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$measurementUnitKey")

          view must containErrorElementWithMessageKey("declaration.additionalDocument.measurementUnit.error")
        }

      }

      "display error for invalid Document quantity" when {

        "there is a precession error" in {
          val additionalDocumentWithIncorrectDocumentQuantity = correctAdditionalDocument.copy(
            documentWriteOff = Some(correctDocumentWriteOff.copy(documentQuantity = incorrectDocumentWriteOff.documentQuantity))
          )
          val view = createView(AdditionalDocument.form().bind(Json.toJson(additionalDocumentWithIncorrectDocumentQuantity)))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$documentQuantityKey")

          view must containErrorElementWithMessageKey("declaration.additionalDocument.documentQuantity.error")
        }

        "there is a scale error" in {
          val additionalDocumentWithIncorrectDocumentQuantity =
            correctAdditionalDocument.copy(documentWriteOff = Some(correctDocumentWriteOff.copy(documentQuantity = Some(0.000000001D))))
          val view = createView(AdditionalDocument.form().bind(Json.toJson(additionalDocumentWithIncorrectDocumentQuantity)))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$documentQuantityKey")

          view must containErrorElementWithMessageKey("declaration.additionalDocument.documentQuantity.error")
        }

        "there is an error in quantity" in {
          val additionalDocumentWithIncorrectDocumentQuantity =
            correctAdditionalDocument.copy(documentWriteOff = Some(correctDocumentWriteOff.copy(documentQuantity = Some(-1))))
          val view = createView(AdditionalDocument.form().bind(Json.toJson(additionalDocumentWithIncorrectDocumentQuantity)))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$documentQuantityKey")

          view must containErrorElementWithMessageKey("declaration.additionalDocument.documentQuantity.error")
        }
      }

      "display errors for all fields" in {

        val form = AdditionalDocument.form().bind(incorrectAdditionalDocumentMap)

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

        view must containErrorElementWithMessageKey("declaration.additionalDocument.documentTypeCode.error")
        view must containErrorElementWithMessageKey("declaration.additionalDocument.documentIdentifier.error")
        view must containErrorElementWithMessageKey("declaration.additionalDocument.documentStatus.error")
        view must containErrorElementWithMessageKey("declaration.additionalDocument.documentStatusReason.error")
        view must containErrorElementWithMessageKey("declaration.additionalDocument.issuingAuthorityName.error.length")
        view must containErrorElementWithMessageKey("declaration.additionalDocument.dateOfValidity.error.format")
        view must containErrorElementWithMessageKey("declaration.additionalDocument.measurementUnit.error")
        view must containErrorElementWithMessageKey("declaration.additionalDocument.documentQuantity.error")
      }
    }
  }

  "additional_document_edit view when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display data in all inputs" in {
        val data = correctAdditionalDocument
        val form = AdditionalDocument.form().fill(data)
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
