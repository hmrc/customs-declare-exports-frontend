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

package views.declaration

import base.{Injector, TestHelper}
import config.AppConfig
import controllers.helpers.SaveAndReturn
import forms.common.Date.{dayKey, monthKey, yearKey}
import forms.common.Eori
import forms.declaration.AdditionalDocumentSpec._
import forms.declaration.CommodityDetails
import forms.declaration.additionaldocuments.AdditionalDocument
import forms.declaration.additionaldocuments.AdditionalDocument._
import forms.declaration.additionaldocuments.DocumentWriteOff._
import forms.declaration.additionaldocuments.DocumentWriteOffSpec.incorrectDocumentWriteOff
import forms.declaration.declarationHolder.DeclarationHolder
import models.declaration.ExportDeclarationTestData.{allRecords, declaration}
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import models.declaration.EoriSource
import org.jsoup.nodes.Document
import org.scalatest.Inspectors.forAll
import org.scalatest.{Assertion, OptionValues}
import play.api.data.Form
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.additionalDocuments.additional_document_edit

class AdditionalDocumentEditViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector with OptionValues {

  private val appConfig = instanceOf[AppConfig]

  private val itemId = "a7sc78"
  private val mode = Mode.Normal

  private val additionalDocumentEditPage = instanceOf[additional_document_edit]

  private def createView(input: Map[String, String])(implicit request: JourneyRequest[_]): Document = {
    val form: Form[AdditionalDocument] = AdditionalDocument.form(declaration).bind(input)
    additionalDocumentEditPage(mode, itemId, form)(request, messages)
  }

  private def createView(input: Option[AdditionalDocument] = None, exportsDeclaration: ExportsDeclaration = declaration)(
    implicit request: JourneyRequest[_]
  ): Document = {
    val form: Form[AdditionalDocument] = AdditionalDocument.form(exportsDeclaration)
    additionalDocumentEditPage(mode, itemId, input.fold(form)(form.fillAndValidate))(request, messages)
  }

  private val prefix = "declaration.additionalDocument"

  "AdditionalDocument Add/Change Controller" should {

    "have correct message keys" in {
      messages must haveTranslationFor(s"$prefix.title")
      messages must haveTranslationFor(s"$prefix.title.fromAuthCode")
      messages must haveTranslationFor(s"$prefix.text")
      messages must haveTranslationFor(s"$prefix.text.fromAuthCode.paragraph1")
      messages must haveTranslationFor(s"$prefix.text.fromAuthCode.paragraph2")

      messages must haveTranslationFor(s"$prefix.expander.title")
      messages must haveTranslationFor(s"$prefix.expander.paragraph1.withCommodityCode.text")
      messages must haveTranslationFor(s"$prefix.expander.paragraph1.withCommodityCode.link1.text")
      messages must haveTranslationFor(s"$prefix.expander.paragraph1.withoutCommodityCode.text")
      messages must haveTranslationFor(s"$prefix.expander.paragraph1.withoutCommodityCode.link1.text")
      messages must haveTranslationFor(s"$prefix.expander.paragraph2.text")
      messages must haveTranslationFor(s"$prefix.expander.paragraph3.text")
      messages must haveTranslationFor(s"$prefix.expander.paragraph3.link1.text")
      messages must haveTranslationFor(s"$prefix.expander.paragraph4.text")
      messages must haveTranslationFor(s"$prefix.expander.paragraph4.link1.text")

      messages must haveTranslationFor(s"$prefix.documentTypeCode")
      messages must haveTranslationFor(s"$prefix.documentTypeCode.text")
      messages must haveTranslationFor(s"$prefix.documentTypeCode.hint")
      messages must haveTranslationFor(s"$prefix.documentTypeCode.text.fromAuthCode")
      messages must haveTranslationFor(s"$prefix.documentTypeCode.hint.fromAuthCode")
      messages must haveTranslationFor(s"$prefix.documentTypeCode.expander.title")
      messages must haveTranslationFor(s"$prefix.documentTypeCode.expander.paragraph1.text")
      messages must haveTranslationFor(s"$prefix.documentTypeCode.expander.paragraph1.link1.text")
      messages must haveTranslationFor(s"$prefix.documentTypeCode.expander.paragraph2.text")
      messages must haveTranslationFor(s"$prefix.documentTypeCode.expander.paragraph2.link1.text")
      messages must haveTranslationFor(s"$prefix.documentTypeCode.expander.paragraph3.text")
      messages must haveTranslationFor(s"$prefix.documentTypeCode.empty")
      messages must haveTranslationFor(s"$prefix.documentTypeCode.empty.fromAuthCode")
      messages must haveTranslationFor(s"$prefix.documentTypeCode.error")

      messages must haveTranslationFor(s"$prefix.documentIdentifier")
      messages must haveTranslationFor(s"$prefix.documentIdentifier.body")
      messages must haveTranslationFor(s"$prefix.documentIdentifier.inset.fromAuthCode.paragraph1")
      messages must haveTranslationFor(s"$prefix.documentIdentifier.inset.fromAuthCode.paragraph2")
      messages must haveTranslationFor(s"$prefix.documentIdentifier.error")

      messages must haveTranslationFor(s"$prefix.documentStatus")
      messages must haveTranslationFor(s"$prefix.documentStatus.text")
      messages must haveTranslationFor(s"$prefix.documentStatus.error")

      messages must haveTranslationFor(s"$prefix.documentStatusReason")
      messages must haveTranslationFor(s"$prefix.documentStatusReason.text")
      messages must haveTranslationFor(s"$prefix.documentStatusReason.hint")
      messages must haveTranslationFor(s"$prefix.documentStatusReason.error")

      messages must haveTranslationFor(s"$prefix.issuingAuthorityName")
      messages must haveTranslationFor(s"$prefix.issuingAuthorityName.text")
      messages must haveTranslationFor(s"$prefix.issuingAuthorityName.error.length")

      messages must haveTranslationFor(s"$prefix.dateOfValidity")
      messages must haveTranslationFor(s"$prefix.dateOfValidity.hint")
      messages must haveTranslationFor(s"$prefix.dateOfValidity.error.format")
      messages must haveTranslationFor(s"$prefix.dateOfValidity.error.outOfRange")

      messages must haveTranslationFor(s"$prefix.measurementUnit.header")
      messages must haveTranslationFor(s"$prefix.measurementUnit.text")
      messages must haveTranslationFor(s"$prefix.measurementUnit.hint")
      messages must haveTranslationFor(s"$prefix.measurementUnit.link.text")
      messages must haveTranslationFor(s"$prefix.measurementUnit.error")
      messages must haveTranslationFor(s"$prefix.measurementUnit")

      messages must haveTranslationFor(s"$prefix.qualifier")
      messages must haveTranslationFor(s"$prefix.qualifier.error")

      messages must haveTranslationFor(s"$prefix.measurementUnitAndQualifier.error")

      messages must haveTranslationFor(s"$prefix.documentQuantity")
      messages must haveTranslationFor(s"$prefix.documentQuantity.hint")
      messages must haveTranslationFor(s"$prefix.documentQuantity.error")

      messages must haveTranslationFor(s"$prefix.error.maximumAmount")
      messages must haveTranslationFor(s"$prefix.error.duplicate")
      messages must haveTranslationFor(s"$prefix.error.notDefined")

      messages must haveTranslationFor(s"$prefix.summary.documentTypeCode")
      messages must haveTranslationFor(s"$prefix.summary.documentIdentifier")
      messages must haveTranslationFor(s"$prefix.summary.statusCode")
      messages must haveTranslationFor(s"$prefix.summary.heading")
      messages must haveTranslationFor(s"$prefix.summary.multiple.heading")
      messages must haveTranslationFor(s"$prefix.summary.caption")
      messages must haveTranslationFor(s"$prefix.summary.add.another")
      messages must haveTranslationFor(s"$prefix.summary.add.another.empty")
      messages must haveTranslationFor(s"$prefix.summary.change.hint")
      messages must haveTranslationFor(s"$prefix.summary.remove.hint")

      messages must haveTranslationFor(s"$prefix.remove.empty")
      messages must haveTranslationFor(s"$prefix.remove.title")
      messages must haveTranslationFor(s"$prefix.remove.code")
      messages must haveTranslationFor(s"$prefix.remove.reference")
      messages must haveTranslationFor(s"$prefix.remove.statusCode")
      messages must haveTranslationFor(s"$prefix.remove.statusReason")
      messages must haveTranslationFor(s"$prefix.remove.issuingAuthorityName")
      messages must haveTranslationFor(s"$prefix.remove.dateOfValidity")
      messages must haveTranslationFor(s"$prefix.remove.measurementUnitAndQualifier")
      messages must haveTranslationFor(s"$prefix.remove.documentQuantity")
    }
  }

  "additional_document_edit view on empty page" when {

    "the entered authorisation code requires additional documents" should {

      val declarationHolder = DeclarationHolder(Some("OPO"), Some(Eori("GB123456789012")), Some(EoriSource.OtherEori))

      onEveryDeclarationJourney(withDeclarationHolders(declarationHolder)) { implicit request =>
        val view = createView()

        "display the expected page title" in {
          view.getElementsByTag("h1") must containMessageForElements(s"$prefix.title.fromAuthCode")
        }

        "display the expected page hint" in {
          val text = view.getElementsByClass("govuk-body")
          text.get(0).text() mustBe messages(s"$prefix.text.fromAuthCode.paragraph1")
          text.get(1).text() mustBe messages(s"$prefix.text.fromAuthCode.paragraph2")
        }

        "display empty input with label for Document type code" in {
          view.getElementsByAttributeValue("for", documentTypeCodeKey) must containMessageForElements(s"$prefix.documentTypeCode")
          view.getElementById(s"$documentTypeCodeKey-text") must containMessage(s"$prefix.documentTypeCode.text.fromAuthCode")
          view.getElementById(s"$documentTypeCodeKey-hint") must containMessage(s"$prefix.documentTypeCode.hint.fromAuthCode")
          view.getElementById(documentTypeCodeKey).attr("value") mustBe empty
        }

        "display the expected insets placed after the 'Document Identifier' input field" in {
          val insets = view.getElementsByClass("govuk-inset-text").get(0)
          val paragraphs = insets.getElementsByTag("p")
          paragraphs.get(0).text() mustBe messages(s"$prefix.documentIdentifier.inset.fromAuthCode.paragraph1")
          paragraphs.get(1).text() mustBe messages(s"$prefix.documentIdentifier.inset.fromAuthCode.paragraph2")

          Option(insets.previousElementSibling().getElementById(documentIdentifierKey)) must not be None
        }
      }
    }
  }

  "additional_document_edit view on empty page" should {

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display the expected page title" in {
        view.getElementsByTag("h1") must containMessageForElements(s"$prefix.title")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.5")
      }

      "display the expected page hint" in {
        val texts = view.getElementsByClass("govuk-body")
        texts.get(0).text() mustBe messages(s"$prefix.text")
      }

      "display the top expander" when {
        "commodityCode is not present" in {
          val topExpander = view.getElementById("top-expander")

          topExpander must containHtml(messages(s"$prefix.expander.title"))
          topExpander
            .getElementsByClass("govuk-body")
            .get(0) must containText(messages(s"$prefix.expander.paragraph1.withoutCommodityCode.link1.text"))
        }
      }

      "display empty input with label for Document type code" in {
        view.getElementsByAttributeValue("for", documentTypeCodeKey) must containMessageForElements(s"$prefix.documentTypeCode")
        view.getElementById(s"$documentTypeCodeKey-hint") must containMessage(s"$prefix.documentTypeCode.hint")
        view.getElementById(documentTypeCodeKey).attr("value") mustBe empty
      }

      "display the documentTypeCode expander" in {
        view.getElementById("documentTypeCode-expander") must containHtml(messages(s"$prefix.documentTypeCode.expander.title"))
      }

      "display empty input with label and hint for Document identifier" in {
        view.getElementsByAttributeValue("for", documentIdentifierKey) must containMessageForElements(s"$prefix.documentIdentifier")
        view.getElementById(s"$documentIdentifierKey-text") must containMessage(s"$prefix.documentIdentifier.body")
        view.getElementById(documentIdentifierKey).attr("value") mustBe empty
      }

      "not have any inset text" in {
        view.getElementsByClass("govuk-inset-text").size mustBe 0
      }

      "display empty input with label for Document status" in {
        view.getElementsByAttributeValue("for", documentStatusKey) must containMessageForElements(s"$prefix.documentStatus")
        view.getElementById(s"$documentStatusKey-text") must containMessage(s"$prefix.documentStatus.text")
        view.getElementById(s"$documentStatusKey").attr("value") mustBe empty
      }

      "display empty input with label for Document status reason" in {
        view.getElementsByAttributeValue("for", documentStatusReasonKey) must containMessageForElements(s"$prefix.documentStatusReason")
        view.getElementById(s"$documentStatusReasonKey-text") must containMessage(s"$prefix.documentStatusReason.text")
        view.getElementById(s"$documentStatusReasonKey-hint") must containMessage(s"$prefix.documentStatusReason.hint")
        view.getElementById(documentStatusReasonKey).attr("value") mustBe empty
      }

      "display empty input with label for Issuing Authority Name" in {
        view.getElementsByAttributeValue("for", issuingAuthorityNameKey) must containMessageForElements(s"$prefix.issuingAuthorityName")
        view.getElementById(s"$issuingAuthorityNameKey-text") must containMessage(s"$prefix.issuingAuthorityName.text")
        view.getElementById(issuingAuthorityNameKey).attr("value") mustBe empty
      }

      "display empty input with label for Date of Validity" in {
        view.getElementById(dateOfValidityKey).getElementsByTag("legend") must containMessageForElements(s"$prefix.dateOfValidity")
        view.getElementById(s"$dateOfValidityKey-input-hint") must containMessage(s"$prefix.dateOfValidity.hint")
      }

      "display empty input with label for Measurement Unit" in {
        view.getElementById("measurementUnitAndQualifier").getElementsByTag("legend") must containMessageForElements(
          s"$prefix.measurementUnit.header"
        )
        view.getElementsByAttributeValue("for", s"${documentWriteOffKey}_$measurementUnitKey") must containMessageForElements(
          s"$prefix.measurementUnit"
        )
        view.getElementById(s"${documentWriteOffKey}_$measurementUnitKey").attr("value") mustBe empty
        view.getElementsByAttributeValue("for", s"${documentWriteOffKey}_$qualifierKey") must containMessageForElements(s"$prefix.qualifier")
        view.getElementById(s"${documentWriteOffKey}_$qualifierKey").attr("value") mustBe empty
        view.getElementById(s"${documentWriteOffKey}-text") must containMessage(s"$prefix.measurementUnit.link.text")
      }

      "display empty input with label for Document quantity" in {
        view.getElementsByAttributeValue("for", s"${documentWriteOffKey}_$documentQuantityKey") must containMessageForElements(
          s"$prefix.documentQuantity"
        )
        view.getElementById(s"${documentWriteOffKey}_$documentQuantityKey").attr("value") mustBe empty
        view.getElementById(s"${documentWriteOffKey}_$documentQuantityKey-hint") must containMessage(
          s"$prefix.documentQuantity.hint",
          messages(s"$prefix.measurementUnit.hint")
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
      val commodityCode = "4602191000"
      val item = anItem(withItemId(itemId), withCommodityDetails(CommodityDetails(Some(commodityCode), None)))

      onEveryDeclarationJourney(withItem(item)) { implicit request =>
        "a commodityCode with 10-digits is present" in {
          val topExpander = createView().getElementById("top-expander")

          topExpander must containHtml(messages(s"$prefix.expander.title"))
          val commodityCodeBody = topExpander.getElementsByClass("govuk-body").get(0)

          val expectedLinkText = messages(s"$prefix.expander.paragraph1.withCommodityCode.link1.text", commodityCode)
          val expectedHref = appConfig.commodityCodeTariffPageUrl.replace(CommodityDetails.placeholder, commodityCode)

          commodityCodeBody.text mustBe messages(s"$prefix.expander.paragraph1.withCommodityCode.text", expectedLinkText)
          commodityCodeBody.child(0) must haveHref(expectedHref)
        }
      }
    }

    "display the top expander" when {
      val commodityCode = "46021910"
      val item = anItem(withItemId(itemId), withCommodityDetails(CommodityDetails(Some(commodityCode), None)))

      onEveryDeclarationJourney(withItem(item)) { implicit request =>
        "a commodityCode with 8-digits is present" in {
          val topExpander = createView().getElementById("top-expander")

          topExpander must containHtml(messages(s"$prefix.expander.title"))
          val commodityCodeBody = topExpander.getElementsByClass("govuk-body").get(0)

          val expectedLinkText = messages(s"$prefix.expander.paragraph1.withCommodityCode.link1.text", commodityCode)
          val expectedHref = appConfig.commodityCodeTariffPageUrl.replace(CommodityDetails.placeholder, s"${commodityCode}00")

          commodityCodeBody.text mustBe messages(s"$prefix.expander.paragraph1.withCommodityCode.text", expectedLinkText)
          commodityCodeBody.child(0) must haveHref(expectedHref)
        }
      }
    }
  }

  "additional_document_edit view" should {

    onEveryDeclarationJourney() { implicit request =>
      "display error for invalid Document type code" in {
        val invalidDocumentTypeCode = Some(TestHelper.createRandomAlphanumericString(5))
        val view = createView(Some(correctAdditionalDocument.copy(documentTypeCode = invalidDocumentTypeCode)))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentTypeCodeKey")

        view must containErrorElementWithMessageKey(s"$prefix.documentTypeCode.error")
      }

      "display error for empty Document type code" in {
        val view = createView(Some(correctAdditionalDocument.copy(documentTypeCode = None)))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentTypeCodeKey")

        view must containErrorElementWithMessageKey(s"$prefix.documentTypeCode.empty")
      }

      "display error for empty Document type code when required after the entered authorisation code" in {
        val view = createView(Some(correctAdditionalDocument.copy(documentTypeCode = None)), allRecords)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentTypeCodeKey")

        view must containErrorElementWithMessageKey(s"$prefix.documentTypeCode.empty.fromAuthCode")
      }

      "display error for invalid Document identifier" in {
        val view = createView(correctAdditionalDocumentMap + (documentIdentifierKey -> "!@#$%"))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentIdentifierKey")

        view must containErrorElementWithMessageKey(s"$prefix.documentIdentifier.error")
      }

      "display error for invalid Document status" in {
        val view = createView(Some(correctAdditionalDocument.copy(documentStatus = Some("ABC"))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentStatusKey")

        view must containErrorElementWithMessageKey(s"$prefix.documentStatus.error")
      }

      "display error for invalid Document status reason" in {
        val documentStatusReason = Some(TestHelper.createRandomAlphanumericString(36))
        val view = createView(Some(correctAdditionalDocument.copy(documentStatusReason = documentStatusReason)))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentStatusReasonKey")

        view must containErrorElementWithMessageKey(s"$prefix.documentStatusReason.error")
      }

      "display error for invalid Issuing Authority Name" in {
        val issuingAuthorityName = Some(TestHelper.createRandomAlphanumericString(71))
        val view = createView(Some(correctAdditionalDocument.copy(issuingAuthorityName = issuingAuthorityName)))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$issuingAuthorityNameKey")

        view must containErrorElementWithMessageKey(s"$prefix.issuingAuthorityName.error.length")
      }

      "display error for invalid Date of Validity" when {

        "year is out of range (2000-2099)" in {
          val invalidDateOfValidity =
            Map(s"$dateOfValidityKey.$yearKey" -> "1999", s"$dateOfValidityKey.$monthKey" -> "12", s"$dateOfValidityKey.$dayKey" -> "30")
          val view = createView(correctAdditionalDocumentMap ++ invalidDateOfValidity)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#$dateOfValidityKey")

          view must containErrorElementWithMessageKey(s"$prefix.dateOfValidity.error.outOfRange")
        }

        "provided with non-existing month and day" in {
          val invalidDateOfValidity =
            Map(s"$dateOfValidityKey.$yearKey" -> "2020", s"$dateOfValidityKey.$monthKey" -> "13", s"$dateOfValidityKey.$dayKey" -> "32")
          val view = createView(correctAdditionalDocumentMap ++ invalidDateOfValidity)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#$dateOfValidityKey")

          view must containErrorElementWithMessageKey(s"$prefix.dateOfValidity.error.format")
        }

        "provided with partial date" in {
          val mapWithInvalidDateOfValidity =
            correctAdditionalDocumentMap -
              s"$dateOfValidityKey.$yearKey" +
              (s"$dateOfValidityKey.$monthKey" -> "12") +
              (s"$dateOfValidityKey.$dayKey" -> "25")

          val view = createView(mapWithInvalidDateOfValidity)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${dateOfValidityKey}_$yearKey")

          view must containErrorElementWithMessageKey("dateTime.date.year.error.empty")
        }
      }

      "display error for invalid Measurement Unit" when {

        "unit text is too long" in {
          val incorrectMeasurementUnit = s"$documentWriteOffKey.$measurementUnitKey" -> incorrectDocumentWriteOff.measurementUnit.value
          val view = createView(correctAdditionalDocumentMap + incorrectMeasurementUnit)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$measurementUnitKey")

          view must containErrorElementWithMessageKey(s"$prefix.measurementUnit.error")
        }

        "unit text contains special characters" in {
          val incorrectMeasurementUnit = s"$documentWriteOffKey.$measurementUnitKey" -> "!@#$"
          val view = createView(correctAdditionalDocumentMap + incorrectMeasurementUnit)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$measurementUnitKey")

          view must containErrorElementWithMessageKey(s"$prefix.measurementUnit.error")
        }

      }

      "display error for invalid Document quantity" when {

        "there is a precession error" in {
          val incorrectDocumentQuantity =
            s"$documentWriteOffKey.$documentQuantityKey" -> incorrectDocumentWriteOff.documentQuantity.value.toString

          val view = createView(correctAdditionalDocumentMap + incorrectDocumentQuantity)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$documentQuantityKey")

          view must containErrorElementWithMessageKey(s"$prefix.documentQuantity.error")
        }

        "there is a scale error" in {
          val incorrectDocumentQuantity = s"$documentWriteOffKey.$documentQuantityKey" -> 0.000000001D.toString
          val view = createView(correctAdditionalDocumentMap + incorrectDocumentQuantity)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$documentQuantityKey")

          view must containErrorElementWithMessageKey(s"$prefix.documentQuantity.error")
        }

        "there is an error in quantity" in {
          val incorrectDocumentQuantity = s"$documentWriteOffKey.$documentQuantityKey" -> "-1"
          val view = createView(correctAdditionalDocumentMap + incorrectDocumentQuantity)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$documentQuantityKey")

          view must containErrorElementWithMessageKey(s"$prefix.documentQuantity.error")
        }
      }

      "display errors for all fields" in {
        val view = createView(incorrectAdditionalDocumentMap)

        view must haveGovukGlobalErrorSummary

        val keys = List(
          documentTypeCodeKey,
          documentIdentifierKey,
          documentStatusKey,
          documentStatusReasonKey,
          issuingAuthorityNameKey,
          dateOfValidityKey,
          s"${documentWriteOffKey}_$measurementUnitKey",
          s"${documentWriteOffKey}_$documentQuantityKey"
        )
        forAll(keys)(key => view must containErrorElementWithTagAndHref("a", s"#${key}"))

        val errorIds = List(
          s"$prefix.documentTypeCode.error",
          s"$prefix.documentIdentifier.error",
          s"$prefix.documentStatus.error",
          s"$prefix.documentStatusReason.error",
          s"$prefix.issuingAuthorityName.error.length",
          s"$prefix.dateOfValidity.error.format",
          s"$prefix.measurementUnit.error",
          s"$prefix.documentQuantity.error"
        )
        forAll(errorIds)(errorId => view must containErrorElementWithMessageKey(errorId))
      }
    }
  }

  "additional_document_edit view when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display data in all inputs" in {
        val data = correctAdditionalDocument
        val form = AdditionalDocument.form(declaration).fill(data)
        val view = additionalDocumentEditPage(mode, itemId, form)(request, messages)

        def assert[T](elementId: String, value: Option[T]): Assertion =
          view.getElementById(elementId).attr("value") mustBe value.value.toString

        assert(documentTypeCodeKey, data.documentTypeCode)
        assert(documentIdentifierKey, data.documentIdentifier)
        assert(documentStatusKey, data.documentStatus)
        assert(documentStatusReasonKey, data.documentStatusReason)
        view.getElementById(issuingAuthorityNameKey).text() mustBe data.issuingAuthorityName.value
        assert(s"${dateOfValidityKey}_$dayKey", data.dateOfValidity.value.day)
        assert(s"${dateOfValidityKey}_$monthKey", data.dateOfValidity.value.month)
        assert(s"${dateOfValidityKey}_$yearKey", data.dateOfValidity.value.year)
        assert(s"${documentWriteOffKey}_$measurementUnitKey", data.documentWriteOff.get.measurementUnit)
        assert(s"${documentWriteOffKey}_$documentQuantityKey", data.documentWriteOff.get.documentQuantity)
      }
    }
  }
}
