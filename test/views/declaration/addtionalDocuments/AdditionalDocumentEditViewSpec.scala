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

package views.declaration.addtionalDocuments

import base.{Injector, TestHelper}
import config.AppConfig
import connectors.FileBasedCodeListConnector
import controllers.helpers.SaveAndReturn
import forms.common.Date.{dayKey, monthKey, yearKey}
import forms.common.Eori
import forms.declaration.AdditionalDocumentSpec._
import forms.declaration.CommodityDetails
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import forms.declaration.additionaldocuments.AdditionalDocument
import forms.declaration.additionaldocuments.AdditionalDocument._
import forms.declaration.additionaldocuments.DocumentWriteOff._
import forms.declaration.additionaldocuments.DocumentWriteOffSpec.incorrectDocumentWriteOff
import forms.declaration.declarationHolder.DeclarationHolder
import models.ExportsDeclaration
import models.Mode.Normal
import models.declaration.{EoriSource, ExportItem}
import models.declaration.ExportDeclarationTestData.{allRecords, declaration}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.{Assertion, OptionValues}
import org.scalatest.Inspectors.forAll
import play.api.data.Form
import services.view.HolderOfAuthorisationCodes
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.additionalDocuments.additional_document_edit

import java.util.Locale.ENGLISH

class AdditionalDocumentEditViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector with OptionValues {

  private val appConfig = instanceOf[AppConfig]

  private val itemId = "a7sc78"

  private val additionalDocumentEditPage = instanceOf[additional_document_edit]

  private def createView(implicit request: JourneyRequest[_]): Document =
    additionalDocumentEditPage(Normal, itemId, AdditionalDocument.form(request.cacheModel))(request, messages)

  private def createView(input: Map[String, String])(implicit request: JourneyRequest[_]): Document = {
    val form: Form[AdditionalDocument] = AdditionalDocument.form(declaration).bind(input)
    additionalDocumentEditPage(Normal, itemId, form)(request, messages)
  }

  private def createView(input: Option[AdditionalDocument] = None, declaration: ExportsDeclaration = declaration)(
    implicit request: JourneyRequest[_]
  ): Document = {
    val form: Form[AdditionalDocument] = AdditionalDocument.form(declaration)
    additionalDocumentEditPage(Normal, itemId, input.fold(form)(form.fillAndValidate))(request, messages)
  }

  private val prefix = "declaration.additionalDocument"

  "AdditionalDocument Add/Change Controller" should {

    "have correct message keys" in {
      messages must haveTranslationFor(s"$prefix.status")
      messages must haveTranslationFor(s"$prefix.status.text")
      messages must haveTranslationFor(s"$prefix.status.error")

      messages must haveTranslationFor(s"$prefix.statusReason")
      messages must haveTranslationFor(s"$prefix.statusReason.text")
      messages must haveTranslationFor(s"$prefix.statusReason.hint")
      messages must haveTranslationFor(s"$prefix.statusReason.error")

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

      messages must haveTranslationFor(s"$prefix.quantity")
      messages must haveTranslationFor(s"$prefix.quantity.hint")
      messages must haveTranslationFor(s"$prefix.quantity.error")

      messages must haveTranslationFor(s"$prefix.error.maximumAmount")
      messages must haveTranslationFor(s"$prefix.error.duplicate")
      messages must haveTranslationFor(s"$prefix.error.notDefined")

      messages must haveTranslationFor(s"$prefix.summary.documentCode")
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

      messages must haveTranslationFor(s"$prefix.hint.prefix")
      messages must haveTranslationFor(s"$prefix.AEOF.hint")
      messages must haveTranslationFor(s"$prefix.CGU.hint")
      messages must haveTranslationFor(s"$prefix.CSE.hint")
      messages must haveTranslationFor(s"$prefix.EIR.hint")
      messages must haveTranslationFor(s"$prefix.EPSS.hint")
      messages must haveTranslationFor(s"$prefix.EUS.hint")
      messages must haveTranslationFor(s"$prefix.IPO.hint")
      messages must haveTranslationFor(s"$prefix.OPO.hint")
      messages must haveTranslationFor(s"$prefix.SDE.hint")
      messages must haveTranslationFor(s"$prefix.SIVA.hint")
      messages must haveTranslationFor(s"$prefix.TEA.hint")
      messages must haveTranslationFor(s"$prefix.CWP.hint")
      messages must haveTranslationFor(s"$prefix.DPO.hint")
      messages must haveTranslationFor(s"$prefix.MOU.hint")
    }
  }

  "additional_document_edit view" when {

    val clearanceJourneys = List(CLEARANCE_FRONTIER, CLEARANCE_PRE_LODGED)
    val holders = List(DeclarationHolder(Some("OPO"), None, None), DeclarationHolder(Some("FZ"), None, None))

    val authCodeHelper = new HolderOfAuthorisationCodes(new FileBasedCodeListConnector(appConfig), mockMerchandiseInBagConfig)

    allAdditionalDeclarationTypes.foreach { declarationType =>
      val item = anItem(withItemId(itemId))
      val hintPrefix = messages(s"${prefix}.hint.prefix")
      val hintSeparator = ", "

      s"the declaration is of type $declarationType" should {
        "display no hint text on the Document Identifier field" when {
          "user has selected no auth codes" in {
            implicit val request = withRequest(declarationType, withItem(item))

            val text = Option(createView.getElementById("documentIdentifier-hint"))
            text mustBe None
          }

          "user has selected no auth codes that requiring hint text" in {
            val authCode1 = "OTHER"
            val holders = List(DeclarationHolder(Some(authCode1), Some(Eori("GB123456789012")), Some(EoriSource.OtherEori)))
            implicit val request = withRequest(declarationType, withDeclarationHolders(holders: _*), withItem(item))

            val text = Option(createView.getElementById("documentIdentifier-hint"))
            text mustBe None
          }
        }

        "display hint text on the Document Identifier field for that auth code" when {
          "user has selected one auth codes that requiring hint text" in {
            val authCode1 = "AEOF"
            val holders = List(DeclarationHolder(Some(authCode1), Some(Eori("GB123456789012")), Some(EoriSource.OtherEori)))
            implicit val request = withRequest(declarationType, withDeclarationHolders(holders: _*), withItem(item))

            val text = createView.getElementById("documentIdentifier-hint").getElementsByClass("govuk-hint").get(0).text()
            val expectedAuthCodeHintText = s"${hintPrefix}${hintSeparator}" + messages(s"${prefix}.${authCode1}.hint")
            text mustBe expectedAuthCodeHintText
          }
        }

        "display 2 hint texts on the Document Identifier field for those auth codes" when {
          "user has selected two auth codes that requiring hint text" in {
            val authCodes = List("AEOF", "TEA")
            val holders = authCodes.map(authCode => DeclarationHolder(Some(authCode), Some(Eori("GB123456789012")), Some(EoriSource.OtherEori)))
            implicit val request = withRequest(declarationType, withDeclarationHolders(holders: _*), withItem(item))

            val text = createView.getElementById("documentIdentifier-hint").getElementsByClass("govuk-hint").get(0).text()

            val codeHints = authCodes.map(authCode => messages(s"${prefix}.${authCode}.hint")).mkString(hintSeparator)
            text mustBe s"${hintPrefix}${hintSeparator}${codeHints}"
          }
        }

        "display 3 hint texts on the Document Identifier field for those auth codes" when {
          "user has selected three auth codes that requiring hint text" in {
            val authCodes = List("AEOF", "TEA", "SIVA")
            val holders = authCodes.map(authCode => DeclarationHolder(Some(authCode), Some(Eori("GB123456789012")), Some(EoriSource.OtherEori)))
            implicit val request = withRequest(declarationType, withDeclarationHolders(holders: _*), withItem(item))

            val text = createView.getElementById("documentIdentifier-hint").getElementsByClass("govuk-hint").get(0).text()

            val codeHints = authCodes.map(authCode => messages(s"${prefix}.${authCode}.hint")).mkString(hintSeparator)
            text mustBe s"${hintPrefix}${hintSeparator}${codeHints}"
          }
        }

        "display 3 hint texts on the Document Identifier field for the first 3 auth codes" when {
          "user has selected more than three auth codes that requiring hint text" in {
            val authCodes = List("AEOF", "TEA", "SIVA", "DPO")
            val holders = authCodes.map(authCode => DeclarationHolder(Some(authCode), Some(Eori("GB123456789012")), Some(EoriSource.OtherEori)))
            implicit val request = withRequest(declarationType, withDeclarationHolders(holders: _*), withItem(item))

            val text = createView.getElementById("documentIdentifier-hint").getElementsByClass("govuk-hint").get(0).text()

            val codeHints = authCodes.take(3).map(authCode => messages(s"${prefix}.${authCode}.hint")).mkString(hintSeparator)
            text mustBe s"${hintPrefix}${hintSeparator}${codeHints}"

            val excludedAuthHint = messages(s"${prefix}.${authCodes(3)}.hint")
            text.contains(excludedAuthHint) mustBe false
          }
        }
      }
    }

    allAdditionalDeclarationTypes.filterNot(clearanceJourneys.contains).foreach { declarationType =>
      s"the declaration is of type $declarationType and" when {

        val itemWithLicenseRequired = anItem(withItemId(itemId), withLicenseRequired())

        "has auth Codes requiring additional docs, and a license is required ('V1' content)" should {
          implicit val request = withRequest(declarationType, withDeclarationHolders(holders: _*), withItem(itemWithLicenseRequired))

          "display the expected page title" in {
            createView.getElementsByTag("h1").text mustBe messages(s"$prefix.v1.title")
          }

          "display the expected page body" in {
            val view = createView
            val text = view.getElementsByClass("govuk-body")
            text.get(0).text mustBe messages(s"$prefix.v1.body.1")
            text.get(1).text mustBe messages(s"$prefix.v1.body.2")

            val bulletPoints = view.getElementsByClass("govuk-list--bullet").first.children
            bulletPoints.size mustBe 2
            bulletPoints.get(0).text mustBe authCodeHelper.codeDescription(ENGLISH, "OPO")
            bulletPoints.get(1).text mustBe authCodeHelper.codeDescription(ENGLISH, "FZ")
          }

          "display the expected top expander" in topExpander(false)
          "display the expected 'Document Code' section" in documentCode(1)
          "display the 'Document Code' expander" in documentCodeExpander
          "display the expected 'Document Identifier' section" in documentIdentifier(1)
          "NOT display any inset text" in { createView.getElementsByClass("govuk-inset-text").size mustBe 0 }
        }

        "does NOT have auth Codes requiring additional docs, but a license is required ('V2' content)" should {
          implicit val request = withRequest(declarationType, withItem(itemWithLicenseRequired))

          "display the expected page title" in {
            createView.getElementsByTag("h1").text mustBe messages(s"$prefix.v2.title")
          }

          "display the expected page body" in {
            createView.getElementsByClass("govuk-body").get(0).text mustBe messages(s"$prefix.v2.body")
          }

          "display the expected top expander" in topExpander(false)
          "display the expected 'Document Code' section" in documentCode(2)
          "display the 'Document Code' expander" in documentCodeExpander
          "display the expected 'Document Identifier' section" in documentIdentifier(2)

          "display the expected insets placed after the 'Document Identifier' input field" in {
            val insets = createView.getElementsByClass("govuk-inset-text")
            val paragraphs = insets.get(0).getElementsByClass("govuk-body")
            paragraphs.size mustBe 2
            paragraphs.get(0).text mustBe messages(s"$prefix.identifier.inset.body.1")
            paragraphs.get(1).text mustBe messages(s"$prefix.identifier.inset.body.2")
          }
        }

        "has auth Codes requiring additional docs, but a license is NOT required ('V3' content)" should {
          implicit val request = withRequest(declarationType, withDeclarationHolders(holders: _*))

          "display the expected page title" in {
            createView.getElementsByTag("h1").text mustBe messages(s"$prefix.v3.title")
          }

          "display the expected page body" in {
            val view = createView
            val text = view.getElementsByClass("govuk-body")
            text.get(0).text mustBe messages(s"$prefix.v3.body.1")
            text.get(1).text mustBe messages(s"$prefix.v3.body.2")

            val bulletPoints = view.getElementsByClass("govuk-list--bullet").first.children
            bulletPoints.size mustBe 2
            bulletPoints.get(0).text mustBe authCodeHelper.codeDescription(ENGLISH, "OPO")
            bulletPoints.get(1).text mustBe authCodeHelper.codeDescription(ENGLISH, "FZ")
          }

          "display the expected top expander" in topExpander(true)
          "display the expected 'Document Code' section" in documentCode(3)
          "display the 'Document Code' expander" in documentCodeExpander
          "display the expected 'Document Identifier' section" in documentIdentifier(3)

          "display the expected insets placed after the 'Document Identifier' input field" in {
            val insets = createView.getElementsByClass("govuk-inset-text")
            val paragraphs = insets.get(0).getElementsByClass("govuk-body")
            paragraphs.size mustBe 1
            paragraphs.get(0).text mustBe messages(s"$prefix.v3.identifier.inset.body")
          }
        }

        "does NOT have auth Codes requiring additional docs, and a license is NOT required ('V4' content)" should {
          implicit val request = withRequest(declarationType)

          "display the expected page title" in {
            createView.getElementsByTag("h1").text mustBe messages(s"$prefix.v4.title")
          }

          "display the expected page body" in {
            createView.getElementsByClass("govuk-body").get(0).text mustBe messages(s"$prefix.v4.body")
          }

          "NOT display the top expander" in { Option(createView.getElementById("top-expander")) mustBe None }
          "display the expected 'Document Code' section" in documentCode(4)
          "display the 'Document Code' expander" in documentCodeExpander
          "display the expected 'Document Identifier' section" in documentIdentifier(4)
          "NOT display any inset text" in { createView.getElementsByClass("govuk-inset-text").size mustBe 0 }
        }
      }
    }

    clearanceJourneys.foreach { declarationType =>
      s"the declaration is of type $declarationType and" when {

        "has auth Codes requiring additional docs ('V5' content)" should {
          implicit val request = withRequest(declarationType, withDeclarationHolders(holders: _*))

          "display the expected page title" in {
            createView.getElementsByTag("h1").text mustBe messages(s"$prefix.v5.title")
          }

          "display the expected page body" in {
            val view = createView
            val text = view.getElementsByClass("govuk-body")
            text.get(0).text mustBe messages(s"$prefix.v5.body.1")
            text.get(1).text mustBe messages(s"$prefix.v5.body.2")
          }

          "display the expected top expander" in topExpander(true)
          "display the expected 'Document Code' section" in documentCode(5)
          "display the 'Document Code' expander" in documentCodeExpander
          "display the expected 'Document Identifier' section" in documentIdentifier(5)

          "display the expected insets placed after the 'Document Identifier' input field" in {
            val insets = createView.getElementsByClass("govuk-inset-text")
            val paragraphs = insets.get(0).getElementsByClass("govuk-body")
            paragraphs.size mustBe 2
            paragraphs.get(0).text mustBe messages(s"$prefix.identifier.inset.body.1")
            paragraphs.get(1).text mustBe messages(s"$prefix.identifier.inset.body.2")
          }
        }

        "does NOT have auth Codes requiring additional docs ('V6' content)" should {
          implicit val request = withRequest(declarationType)

          "display the expected page title" in {
            createView.getElementsByTag("h1").text mustBe messages(s"$prefix.v6.title")
          }

          "display the expected page body" in {
            createView.getElementsByClass("govuk-body").get(0).text mustBe messages(s"$prefix.v6.body")
          }

          "display the expected top expander" in topExpander(true)
          "display the expected 'Document Code' section" in documentCode(6)
          "display the 'Document Code' expander" in documentCodeExpander
          "display the expected 'Document Identifier' section" in documentIdentifier(6)
          "NOT display any inset text" in { createView.getElementsByClass("govuk-inset-text").size mustBe 0 }
        }
      }
    }

    def topExpander(hasLastParagraph: Boolean)(implicit request: JourneyRequest[_]): Unit =
      List("", "46021910", "4602191000").foreach { commodityCode =>
        val declaration = request.cacheModel
        val commodityDetails = if (commodityCode.isEmpty) None else Some(CommodityDetails(Some(commodityCode), None))
        val item = declaration.items.headOption.fold(ExportItem(itemId, commodityDetails = commodityDetails)) {
          _.copy(commodityDetails = commodityDetails)
        }

        val topExpander = createView(journeyRequest(declaration.copy(items = List(item)))).getElementById("top-expander")

        topExpander.child(0).text mustBe messages(s"$prefix.expander.title")

        val paragraphs = topExpander.getElementsByClass("govuk-body")
        paragraphs.size mustBe (if (hasLastParagraph) 4 else 3)

        if (commodityCode.isEmpty) {
          val placeholder1 = messages(s"$prefix.expander.body.1.withoutCommodityCode.link")
          paragraphs.get(0).text mustBe messages(s"$prefix.expander.body.1.withoutCommodityCode", placeholder1)
          paragraphs.get(0).child(0) must haveHref(appConfig.tradeTariffSections)
        } else {
          val commodityCodeAsRef = if (commodityCode.length == 8) s"${commodityCode}00" else commodityCode
          val placeholder1 = messages(s"$prefix.expander.body.1.withCommodityCode.link", commodityCode)
          paragraphs.get(0).text mustBe messages(s"$prefix.expander.body.1.withCommodityCode", placeholder1)

          val expectedHref = appConfig.commodityCodeTariffPageUrl.replace(CommodityDetails.placeholder, commodityCodeAsRef)
          paragraphs.get(0).child(0) must haveHref(expectedHref)
        }

        paragraphs.get(1).text mustBe messages(s"$prefix.expander.body.2")
        paragraphs.get(2).text mustBe messages(s"$prefix.expander.body.3", messages(s"$prefix.expander.body.3.link"))
        paragraphs.get(2).child(0) must haveHref(appConfig.additionalDocumentsLicenceTypes)

        if (hasLastParagraph) {
          paragraphs.get(3).text mustBe messages(s"$prefix.expander.body.4", messages(s"$prefix.expander.body.4.link"))
          paragraphs.get(3).child(0) must haveHref(appConfig.guidance.commodityCode0306310010)
        }
      }

    def documentCode(version: Int)(implicit request: JourneyRequest[_]): Unit = {
      val view = createView
      view.getElementsByAttributeValue("for", documentTypeCodeKey) must containMessageForElements(s"$prefix.code")
      view.getElementById(s"$documentTypeCodeKey-body") must containMessage(s"$prefix.v$version.code.body")
      view.getElementById(s"$documentTypeCodeKey-hint") must containMessage(s"$prefix.v$version.code.hint")
      view.getElementById(documentTypeCodeKey).attr("value") mustBe empty
    }

    def documentCodeExpander(implicit request: JourneyRequest[_]): Unit = {
      val expander = createView.getElementById("documentCode-expander")

      expander.child(0).text mustBe messages(s"$prefix.code.expander.title")

      val paragraphs = expander.getElementsByClass("govuk-body")
      paragraphs.size mustBe 3

      paragraphs.get(0).text mustBe messages(s"$prefix.code.expander.body.1", messages(s"$prefix.code.expander.body.1.link"))
      paragraphs.get(0).child(0) must haveHref(appConfig.additionalDocumentsUnionCodes)

      paragraphs.get(1).text mustBe messages(s"$prefix.code.expander.body.2", messages(s"$prefix.code.expander.body.2.link"))
      paragraphs.get(1).child(0) must haveHref(appConfig.additionalDocumentsReferenceCodes)

      paragraphs.get(2).text mustBe messages(s"$prefix.code.expander.body.3")
    }

    def documentIdentifier(version: Int)(implicit request: JourneyRequest[_]): Unit = {
      val view = createView
      view.getElementsByAttributeValue("for", documentIdentifierKey) must containMessageForElements(s"$prefix.identifier")

      val paragraphs = view.getElementsByClass("govuk-body")
      version match {
        case 1 => paragraphs.get(9).text mustBe messages(s"$prefix.identifier.body")
        case 2 => paragraphs.get(8).text mustBe messages(s"$prefix.identifier.body")
        case 3 =>
          paragraphs.get(10).text mustBe messages(s"$prefix.v3.identifier.body.1")
          paragraphs.get(11).text mustBe messages(s"$prefix.v3.identifier.body.2")

        case 4 => paragraphs.get(5).text mustBe messages(s"$prefix.v4.identifier.body")
        case 5 => paragraphs.get(10).text mustBe messages(s"$prefix.identifier.body")
        case 6 => paragraphs.get(9).text mustBe messages(s"$prefix.identifier.body")
      }

      view.getElementById(documentIdentifierKey).attr("value") mustBe empty
    }
  }

  "additional_document_edit view on empty page" should {
    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.5")
      }

      "display empty input with label for Document status" in {
        view.getElementsByAttributeValue("for", documentStatusKey) must containMessageForElements(s"$prefix.status")
        view.getElementById(s"$documentStatusKey-text") must containMessage(s"$prefix.status.text")
        view.getElementById(s"$documentStatusKey").attr("value") mustBe empty
      }

      "display empty input with label for Document status reason" in {
        view.getElementsByAttributeValue("for", documentStatusReasonKey) must containMessageForElements(s"$prefix.statusReason")
        view.getElementById(s"$documentStatusReasonKey-text") must containMessage(s"$prefix.statusReason.text")
        view.getElementById(s"$documentStatusReasonKey-hint") must containMessage(s"$prefix.statusReason.hint")
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
        view.getElementsByAttributeValue("for", s"${documentWriteOffKey}_$documentQuantityKey") must containMessageForElements(s"$prefix.quantity")
        view.getElementById(s"${documentWriteOffKey}_$documentQuantityKey").attr("value") mustBe empty
        view.getElementById(s"${documentWriteOffKey}_$documentQuantityKey-hint") must containMessage(
          s"$prefix.quantity.hint",
          messages(s"$prefix.measurementUnit.hint")
        )
      }

      "display 'Save and continue' button on page" in {
        val saveAndContinueButton = view.getElementById("submit")
        saveAndContinueButton must containMessage(saveAndContinueCaption)

        val saveAndReturnButton = view.getElementById("submit_and_return")
        saveAndReturnButton must containMessage(saveAndReturnCaption)
        saveAndReturnButton must haveAttribute("name", SaveAndReturn.toString)
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

        view must containErrorElementWithMessageKey(s"$prefix.code.error")
      }

      "display error for empty Document type code" in {
        val view = createView(Some(correctAdditionalDocument.copy(documentTypeCode = None)))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentTypeCodeKey")

        view must containErrorElementWithMessageKey(s"$prefix.code.empty")
      }

      "display error for empty Document type code when required after the entered authorisation code" in {
        val view = createView(Some(correctAdditionalDocument.copy(documentTypeCode = None)), allRecords)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentTypeCodeKey")

        view must containErrorElementWithMessageKey(s"$prefix.code.empty.fromAuthCode")
      }

      "display error for invalid Document identifier" in {
        val view = createView(correctAdditionalDocumentMap + (documentIdentifierKey -> "!@#$%"))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentIdentifierKey")

        view must containErrorElementWithMessageKey(s"$prefix.identifier.error")
      }

      "display error for invalid Document status" in {
        val view = createView(Some(correctAdditionalDocument.copy(documentStatus = Some("ABC"))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentStatusKey")

        view must containErrorElementWithMessageKey(s"$prefix.status.error")
      }

      "display error for invalid Document status reason" in {
        val documentStatusReason = Some(TestHelper.createRandomAlphanumericString(36))
        val view = createView(Some(correctAdditionalDocument.copy(documentStatusReason = documentStatusReason)))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$documentStatusReasonKey")

        view must containErrorElementWithMessageKey(s"$prefix.statusReason.error")
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

          view must containErrorElementWithMessageKey(s"$prefix.quantity.error")
        }

        "there is a scale error" in {
          val incorrectDocumentQuantity = s"$documentWriteOffKey.$documentQuantityKey" -> 0.000000001d.toString
          val view = createView(correctAdditionalDocumentMap + incorrectDocumentQuantity)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$documentQuantityKey")

          view must containErrorElementWithMessageKey(s"$prefix.quantity.error")
        }

        "there is an error in quantity" in {
          val incorrectDocumentQuantity = s"$documentWriteOffKey.$documentQuantityKey" -> "-1"
          val view = createView(correctAdditionalDocumentMap + incorrectDocumentQuantity)

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#${documentWriteOffKey}_$documentQuantityKey")

          view must containErrorElementWithMessageKey(s"$prefix.quantity.error")
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
          s"$prefix.code.error",
          s"$prefix.identifier.error",
          s"$prefix.status.error",
          s"$prefix.statusReason.error",
          s"$prefix.issuingAuthorityName.error.length",
          s"$prefix.dateOfValidity.error.format",
          s"$prefix.measurementUnit.error",
          s"$prefix.quantity.error"
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
        val view = additionalDocumentEditPage(Normal, itemId, form)(request, messages)

        def assert[T](elementId: String, value: Option[T]): Assertion =
          view.getElementById(elementId).attr("value") mustBe value.value.toString

        assert(documentTypeCodeKey, data.documentTypeCode)
        assert(documentIdentifierKey, data.documentIdentifier)
        assert(documentStatusKey, data.documentStatus)
        assert(documentStatusReasonKey, data.documentStatusReason)
        view.getElementById(issuingAuthorityNameKey).text mustBe data.issuingAuthorityName.value
        assert(s"${dateOfValidityKey}_$dayKey", data.dateOfValidity.value.day)
        assert(s"${dateOfValidityKey}_$monthKey", data.dateOfValidity.value.month)
        assert(s"${dateOfValidityKey}_$yearKey", data.dateOfValidity.value.year)
        assert(s"${documentWriteOffKey}_$measurementUnitKey", data.documentWriteOff.get.measurementUnit)
        assert(s"${documentWriteOffKey}_$documentQuantityKey", data.documentWriteOff.get.documentQuantity)
      }
    }
  }
}
