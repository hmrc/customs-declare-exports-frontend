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

import base.ExportsTestData.eori
import base.Injector
import forms.common.Eori
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.AuthorisationProcedureCodeChoice
import forms.declaration.AuthorisationProcedureCodeChoice.{Choice1007, Choice1040, ChoiceOthers}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import forms.declaration.declarationHolder.DeclarationHolder
import models.DeclarationType.{CLEARANCE, DeclarationType, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import org.jsoup.nodes.Document
import org.scalatest.{Assertion, GivenWhenThen}
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.DeclarationHolderHelper.bodyClassId
import views.html.declaration.declarationHolder.declaration_holder_edit_content
import views.tags.ViewTest

@ViewTest
class DeclarationHolderEditContentSpec extends UnitViewSpec with GivenWhenThen with Injector with Stubs {

  private val prefix = "declaration.declarationHolder"
  private val hintId = "authorisationTypeCode-hint"
  private val insetId = "govuk-inset-text"

  "DeclarationHolderEditContent partial" should {
    "have message keys" in {
      messages must haveTranslationFor(s"$prefix.title")
      messages must haveTranslationFor(s"$prefix.paragraph")
      messages must haveTranslationFor(s"$prefix.body.1007.link")
      messages must haveTranslationFor(s"$prefix.body.exrr.roro.exports")
      messages must haveTranslationFor(s"$prefix.body.clearance.eidr.1040")
      messages must haveTranslationFor(s"$prefix.body.clearance.eidr.1007")
      messages must haveTranslationFor(s"$prefix.body.clearance.eidr.others")
      messages must haveTranslationFor(s"$prefix.body.simplified")
      messages must haveTranslationFor(s"$prefix.body.simplified.arrived.1007")
      messages must haveTranslationFor(s"$prefix.body.supplementary")
      messages must haveTranslationFor(s"$prefix.authorisationCode")
      messages must haveTranslationFor(s"$prefix.authorisationCode.empty")
      messages must haveTranslationFor(s"$prefix.authCode.hint.clearance")
      messages must haveTranslationFor(s"$prefix.authCode.hint.standard.1040")
      messages must haveTranslationFor(s"$prefix.authCode.hint.standard.prelodged.1007")
      messages must haveTranslationFor(s"$prefix.authCode.hint.standard.prelodged.others")
      messages must haveTranslationFor(s"$prefix.authCode.inset.excise.title")
      messages must haveTranslationFor(s"$prefix.authCode.inset.excise.bullet1")
      messages must haveTranslationFor(s"$prefix.authCode.inset.excise.bullet1.link")
      messages must haveTranslationFor(s"$prefix.authCode.inset.excise.bullet2")
      messages must haveTranslationFor(s"$prefix.authCode.inset.excise.bullet2.link")
      messages must haveTranslationFor(s"$prefix.authCode.inset.excise.bullet3")
      messages must haveTranslationFor(s"$prefix.authCode.inset.excise.bullet3.link")
      messages must haveTranslationFor(s"$prefix.authCode.inset.special.title")
      messages must haveTranslationFor(s"$prefix.authCode.inset.special.bullet1")
      messages must haveTranslationFor(s"$prefix.authCode.inset.special.bullet1.link")
      messages must haveTranslationFor(s"$prefix.authCode.inset.special.bullet2")
      messages must haveTranslationFor(s"$prefix.authCode.inset.special.bullet3")
      messages must haveTranslationFor(s"$prefix.authCode.inset.special.bullet4")
      messages must haveTranslationFor(s"$prefix.eori")
      messages must haveTranslationFor(s"$prefix.eori.body.exrr.v1")
      messages must haveTranslationFor(s"$prefix.eori.body.exrr.v2")
      messages must haveTranslationFor(s"$prefix.eori.body.exrr.v3")
      messages must haveTranslationFor(s"$prefix.eori.user.text")
      messages must haveTranslationFor(s"$prefix.eori.other.text")
      messages must haveTranslationFor(s"$prefix.eori.other.label")
      messages must haveTranslationFor(s"$prefix.eori.other.hint")
      messages must haveTranslationFor(s"$prefix.eori.other.error.empty")
      messages must haveTranslationFor(s"$prefix.eori.error.radio")
    }
  }

  /*
    Testing partial's dynamic content provided by calling the DeclarationHolderHelper class
   */

  "'Declaration Holder Edit Content' partial" should {

    val declarationHolderPage = instanceOf[declaration_holder_edit_content]

    def createPartial(implicit request: JourneyRequest[_]): Document = {
      val form = DeclarationHolder.form(eori, request.cacheModel.additionalDeclarationType)
      declarationHolderPage(Mode.Normal, form, eori)(request, messages)
    }

    "display the expected body (the text under page's H1)" when {

      onStandard(aDecl1(STANDARD, Some(STANDARD_FRONTIER))) { implicit request =>
        And("the declaration is of type A")
        createPartial.getElementsByClass(bodyClassId).text must include(messages(s"$prefix.body.exrr.roro.exports"))
      }

      onOccasional(aDecl1(OCCASIONAL, Some(OCCASIONAL_FRONTIER))) { implicit request =>
        And("the declaration is of type B")
        createPartial.getElementsByClass(bodyClassId).text must include(messages(s"$prefix.body.exrr.roro.exports"))
      }

      onSimplified(aDecl1(SIMPLIFIED, Some(SIMPLIFIED_PRE_LODGED))) { implicit request =>
        And("the declaration is of type F")
        createPartial.getElementsByClass(bodyClassId).text must include(messages(s"$prefix.body.simplified"))
      }

      onSimplified(aDecl1(SIMPLIFIED, Some(SIMPLIFIED_FRONTIER), Some(Choice1040))) { implicit request =>
        And("the declaration is of type C")
        And("the Procedure Code is 1040")
        val text = createPartial.getElementsByClass(bodyClassId).text
        text must include(messages(s"$prefix.body.simplified"))
        text must include(messages(s"$prefix.body.exrr.roro.exports"))
      }

      onSimplified(aDecl1(SIMPLIFIED, Some(SIMPLIFIED_FRONTIER), Some(ChoiceOthers))) { implicit request =>
        And("the declaration is of type C")
        And("the Procedure Code is 'Others'")
        val text = createPartial.getElementsByClass(bodyClassId).text
        text must include(messages(s"$prefix.body.simplified"))
        text must include(messages(s"$prefix.body.exrr.roro.exports"))
      }

      onSimplified(aDecl1(SIMPLIFIED, Some(SIMPLIFIED_FRONTIER), Some(Choice1007))) { implicit request =>
        And("the declaration is of type C")
        And("the Procedure Code is 1007")
        val bodyWithLink = createPartial.getElementsByClass(bodyClassId)
        bodyWithLink.text mustBe messages(s"$prefix.body.simplified.arrived.1007", messages(s"$prefix.body.1007.link"))
        bodyWithLink.get(0).child(0) must haveHref(minimalAppConfig.permanentExportOrDispatch.section)
      }

      onSupplementary { implicit request =>
        createPartial.getElementsByClass(bodyClassId).text must include(messages(s"$prefix.body.supplementary"))
      }

      onClearance(aDecl1(CLEARANCE, Some(CLEARANCE_PRE_LODGED), Some(Choice1040), Some(YesNoAnswers.yes))) { implicit request =>
        And("the declaration is of type K and EIDR")
        And("the Procedure Code is 1040")
        createPartial.getElementsByClass(bodyClassId).text must include(messages(s"$prefix.body.clearance.eidr.1040"))
      }

      onClearance(aDecl1(CLEARANCE, Some(CLEARANCE_FRONTIER), Some(Choice1040), Some(YesNoAnswers.yes))) { implicit request =>
        And("the declaration is of type J and EIDR")
        And("the Procedure Code is 1040")
        val body = createPartial.getElementsByClass(bodyClassId).text
        body must include(messages(s"$prefix.body.clearance.eidr.1040"))
        body must include(messages(s"$prefix.body.exrr.roro.exports"))
      }

      onClearance(aDecl1(CLEARANCE, Some(CLEARANCE_PRE_LODGED), Some(ChoiceOthers), Some(YesNoAnswers.yes))) { implicit request =>
        And("the declaration is of type K and EIDR")
        And("the Procedure Code is 'Others'")
        createPartial.getElementsByClass(bodyClassId).text must include(messages(s"$prefix.body.clearance.eidr.others"))
      }

      onClearance(aDecl1(CLEARANCE, Some(CLEARANCE_FRONTIER), Some(ChoiceOthers), Some(YesNoAnswers.yes))) { implicit request =>
        And("the declaration is of type J and EIDR")
        And("the Procedure Code is 'Others'")
        val body = createPartial.getElementsByClass(bodyClassId).text
        body must include(messages(s"$prefix.body.clearance.eidr.others"))
        body must include(messages(s"$prefix.body.exrr.roro.exports"))
      }

      onClearance(aDecl1(CLEARANCE, Some(CLEARANCE_FRONTIER), None, Some(YesNoAnswers.no))) { implicit request =>
        And("the declaration is of type J and not EIDR")
        createPartial.getElementsByClass(bodyClassId).text must include(messages(s"$prefix.body.exrr.roro.exports"))
      }

      onClearance(aDecl1(CLEARANCE, None, Some(Choice1007), Some(YesNoAnswers.yes))) { implicit request =>
        And("the declaration is EIDR")
        And("the Procedure Code is 1007")
        val bodyWithLink = createPartial.getElementsByClass(bodyClassId)
        bodyWithLink.text mustBe messages(s"$prefix.body.clearance.eidr.1007", messages(s"$prefix.body.1007.link"))
        bodyWithLink.get(0).child(0) must haveHref(minimalAppConfig.permanentExportOrDispatch.section)
      }
    }

    "display the expected hint for the 'Authorisation Type Code' input field" when {

      onStandard(aDecl1(STANDARD, Some(STANDARD_PRE_LODGED), Some(Choice1007))) { implicit request =>
        And("the declaration is of type D")
        And("the Procedure Code is 1007")
        createPartial.getElementById(hintId).text mustBe messages(s"$prefix.authCode.hint.standard.prelodged.1007")
      }

      onStandard(aDecl1(STANDARD, Some(STANDARD_PRE_LODGED), Some(ChoiceOthers))) { implicit request =>
        And("the declaration is of type D")
        And("the Procedure Code is 'Others'")
        createPartial.getElementById(hintId).text mustBe messages(s"$prefix.authCode.hint.standard.prelodged.others")
      }

      onStandard(aDecl1(STANDARD, None, Some(Choice1040))) { implicit request =>
        And("the Procedure Code is 1040")
        createPartial.getElementById(hintId).text mustBe messages(s"$prefix.authCode.hint.standard.1040")
      }

      onClearance(aDecl1(CLEARANCE, Some(CLEARANCE_PRE_LODGED), None, Some(YesNoAnswers.no))) { implicit request =>
        And("the declaration is of type K and not EIDR")
        createPartial.getElementById(hintId).text mustBe messages(s"$prefix.authCode.hint.clearance")
      }
    }

    "display the expected inset text below the 'Authorisation Type Code' input field" when {

      onStandard(aDecl1(STANDARD, None, Some(Choice1007))) { implicit request =>
        verifyInsetTextForExciseRemovals
      }

      onSimplified(aDecl1(SIMPLIFIED, Some(SIMPLIFIED_PRE_LODGED), Some(Choice1007))) { implicit request =>
        And("the declaration is of type F")
        verifyInsetTextForExciseRemovals
      }

      onJourney(STANDARD, SIMPLIFIED, CLEARANCE)(
        aDeclaration(withAuthorisationProcedureCodeChoice(ChoiceOthers), withEntryIntoDeclarantsRecords(YesNoAnswers.yes))
      ) { implicit request =>
        And("the Procedure Code is 1007")

        val insetText = createPartial.getElementsByClass(insetId).get(0).children
        insetText.get(0).text mustBe messages(s"$prefix.authCode.inset.special.title")

        val list = insetText.get(1).child(0)
        list.tag.getName mustBe "ol"
        assert(list.classNames.contains("govuk-list--number"))

        val bulletPoints = list.children
        bulletPoints.size mustBe 4

        val key = s"$prefix.authCode.inset.special.bullet"
        removeBlanksIfAnyBeforeDot(bulletPoints.get(0).text) mustBe messages(s"${key}1", messages(s"${key}1.link"))
        bulletPoints.get(0).child(0) must haveHref(minimalAppConfig.previousProcedureCodes)
        bulletPoints.get(1).text mustBe messages(s"${key}2")
        bulletPoints.get(2).text mustBe messages(s"${key}3")
        bulletPoints.get(3).text mustBe messages(s"${key}4")
      }

      def verifyInsetTextForExciseRemovals(implicit request: JourneyRequest[_]): Assertion = {
        And("the Procedure Code is 1007")

        val insetText = createPartial.getElementsByClass(insetId).get(0).children
        insetText.get(0).text mustBe messages(s"$prefix.authCode.inset.excise.title")

        val list = insetText.get(1).child(0)
        list.tag.getName mustBe "ul"
        assert(list.classNames.contains("govuk-list--bullet"))

        val bulletPoints = list.children
        bulletPoints.size mustBe 3

        val key = s"$prefix.authCode.inset.excise.bullet"
        bulletPoints.get(0).text mustBe messages(s"${key}1", messages(s"${key}1.link"))
        bulletPoints.get(0).child(0) must haveHref(minimalAppConfig.permanentExportOrDispatch.authHolder)
        bulletPoints.get(1).text mustBe messages(s"${key}2", messages(s"${key}2.link"))
        bulletPoints.get(1).child(0) must haveHref(minimalAppConfig.permanentExportOrDispatch.conditions)
        bulletPoints.get(2).text mustBe messages(s"${key}3", messages(s"${key}3.link"))
        bulletPoints.get(2).child(0) must haveHref(minimalAppConfig.permanentExportOrDispatch.documents)
      }
    }

    "not display any body (the text under page's H1) and, for the 'Authorisation Type Code' input field, any hint or inset Text" when {
      onJourney(STANDARD, OCCASIONAL, SIMPLIFIED, SUPPLEMENTARY, CLEARANCE) { implicit request =>
        val partial = createPartial
        partial.getElementsByClass(bodyClassId).size mustBe (if (request.declarationType == SUPPLEMENTARY) 1 else 0)
        Option(partial.getElementById(hintId)) mustBe None
        partial.getElementsByClass(insetId).size mustBe 0
      }
    }

    "not display any help text under the EORI radios' label" when {

      onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
        And("the declaration is not of type FRONTIER")
        val paragraphs = createPartial.getElementsByClass("govuk-body")
        paragraphs.size mustBe 2
      }
    }

    "display a help text under the EORI radios' label" when {

      onStandard(aDecl2(STANDARD, STANDARD_FRONTIER)) { implicit request =>
        And("the declaration is of type A")
        val paragraphs = createPartial.getElementsByClass("govuk-body")
        paragraphs.size mustBe 4
        paragraphs.get(2).text mustBe messages("declaration.declarationHolder.eori.body.exrr.v2")
      }

      onStandard(aDecl2(SIMPLIFIED, SIMPLIFIED_FRONTIER)) { implicit request =>
        And("the declaration is of type C")
        val paragraphs = createPartial.getElementsByClass("govuk-body")
        paragraphs.size mustBe 3
        paragraphs.get(1).text mustBe messages("declaration.declarationHolder.eori.body.exrr.v2")
      }

      onStandard(aDecl2(OCCASIONAL, OCCASIONAL_FRONTIER)) { implicit request =>
        And("the declaration is of type B")
        val paragraphs = createPartial.getElementsByClass("govuk-body")
        paragraphs.size mustBe 4
        paragraphs.get(2).text mustBe messages("declaration.declarationHolder.eori.body.exrr.v2")
      }

      onStandard(aDecl2(CLEARANCE, CLEARANCE_FRONTIER)) { implicit request =>
        And("the declaration is of type J")
        val paragraphs = createPartial.getElementsByClass("govuk-body")
        paragraphs.size mustBe 3
        paragraphs.get(1).text mustBe messages("declaration.declarationHolder.eori.body.exrr.v2")
      }

      onStandard(aDecl2(STANDARD, STANDARD_FRONTIER, Some("Yes"))) { implicit request =>
        And("the declaration is of type A")
        And("the user answers 'Yes' on /are-you-the-exporter")
        val paragraphs = createPartial.getElementsByClass("govuk-body")
        paragraphs.size mustBe 4
        paragraphs.get(2).text mustBe messages("declaration.declarationHolder.eori.body.exrr.v1")
      }

      onStandard(aDecl2(SIMPLIFIED, SIMPLIFIED_FRONTIER, Some("Yes"))) { implicit request =>
        And("the declaration is of type C")
        And("the user answers 'Yes' on /are-you-the-exporter")
        val paragraphs = createPartial.getElementsByClass("govuk-body")
        paragraphs.size mustBe 3
        paragraphs.get(1).text mustBe messages("declaration.declarationHolder.eori.body.exrr.v1")
      }

      onStandard(aDecl2(OCCASIONAL, OCCASIONAL_FRONTIER, Some("Yes"))) { implicit request =>
        And("the declaration is of type B")
        And("the user answers 'Yes' on /are-you-the-exporter")
        val paragraphs = createPartial.getElementsByClass("govuk-body")
        paragraphs.size mustBe 4
        paragraphs.get(2).text mustBe messages("declaration.declarationHolder.eori.body.exrr.v1")
      }

      onStandard(aDecl2(CLEARANCE, CLEARANCE_FRONTIER, Some("Yes"))) { implicit request =>
        And("the declaration is of type J")
        And("the user answers 'Yes' on /are-you-the-exporter")
        val paragraphs = createPartial.getElementsByClass("govuk-body")
        paragraphs.size mustBe 3
        paragraphs.get(1).text mustBe messages("declaration.declarationHolder.eori.body.exrr.v1")
      }

      onStandard(aDecl2(STANDARD, STANDARD_FRONTIER, Some("No"))) { implicit request =>
        And("the declaration is of type A")
        And("the user answers 'No' on /are-you-the-exporter")
        val paragraphs = createPartial.getElementsByClass("govuk-body")
        paragraphs.size mustBe 4
        paragraphs.get(2).text mustBe messages("declaration.declarationHolder.eori.body.exrr.v3")
      }

      onStandard(aDecl2(SIMPLIFIED, SIMPLIFIED_FRONTIER, Some("No"))) { implicit request =>
        And("the declaration is of type C")
        And("the user answers 'No' on /are-you-the-exporter")
        val paragraphs = createPartial.getElementsByClass("govuk-body")
        paragraphs.size mustBe 3
        paragraphs.get(1).text mustBe messages("declaration.declarationHolder.eori.body.exrr.v3")
      }

      onStandard(aDecl2(OCCASIONAL, OCCASIONAL_FRONTIER, Some("No"))) { implicit request =>
        And("the declaration is of type B")
        And("the user answers 'No' on /are-you-the-exporter")
        val paragraphs = createPartial.getElementsByClass("govuk-body")
        paragraphs.size mustBe 4
        paragraphs.get(2).text mustBe messages("declaration.declarationHolder.eori.body.exrr.v3")
      }

      onStandard(aDecl2(CLEARANCE, CLEARANCE_FRONTIER, Some("No"))) { implicit request =>
        And("the declaration is of type J")
        And("the user answers 'No' on /are-you-the-exporter")
        val paragraphs = createPartial.getElementsByClass("govuk-body")
        paragraphs.size mustBe 3
        paragraphs.get(1).text mustBe messages("declaration.declarationHolder.eori.body.exrr.v3")
      }

      onStandard(aDecl2(STANDARD, STANDARD_FRONTIER, Some("No"), Some("GB12345678"))) { implicit request =>
        And("the declaration is of type A")
        And("the user answers 'No' on /are-you-the-exporter")
        And("the user enters an EORI on /exporter-eori-number")
        val paragraphs = createPartial.getElementsByClass("govuk-body")
        paragraphs.size mustBe 4
        paragraphs.get(2).text mustBe messages("declaration.declarationHolder.eori.body.exrr.v2")
      }

      onStandard(aDecl2(SIMPLIFIED, SIMPLIFIED_FRONTIER, Some("No"), Some("GB12345678"))) { implicit request =>
        And("the declaration is of type C")
        And("the user answers 'No' on /are-you-the-exporter")
        And("the user enters an EORI on /exporter-eori-number")
        val paragraphs = createPartial.getElementsByClass("govuk-body")
        paragraphs.size mustBe 3
        paragraphs.get(1).text mustBe messages("declaration.declarationHolder.eori.body.exrr.v2")
      }

      onStandard(aDecl2(OCCASIONAL, OCCASIONAL_FRONTIER, Some("No"), Some("GB12345678"))) { implicit request =>
        And("the declaration is of type B")
        And("the user answers 'No' on /are-you-the-exporter")
        And("the user enters an EORI on /exporter-eori-number")
        val paragraphs = createPartial.getElementsByClass("govuk-body")
        paragraphs.size mustBe 4
        paragraphs.get(2).text mustBe messages("declaration.declarationHolder.eori.body.exrr.v2")
      }

      onStandard(aDecl2(CLEARANCE, CLEARANCE_FRONTIER, Some("No"), Some("GB12345678"))) { implicit request =>
        And("the declaration is of type J")
        And("the user answers 'No' on /are-you-the-exporter")
        And("the user enters an EORI on /exporter-eori-number")
        val paragraphs = createPartial.getElementsByClass("govuk-body")
        paragraphs.size mustBe 3
        paragraphs.get(1).text mustBe messages("declaration.declarationHolder.eori.body.exrr.v2")
      }
    }
  }

  private def aDecl1(
    declarationType: DeclarationType,
    additionalDeclarationType: Option[AdditionalDeclarationType],
    authorisationProcedureCodeChoice: Option[Option[AuthorisationProcedureCodeChoice]] = None,
    isEidr: Option[String] = None
  ): ExportsDeclaration = {
    val modifiers: List[ExportsDeclarationModifier] = List(
      Some(withType(declarationType)),
      additionalDeclarationType.map(withAdditionalDeclarationType),
      authorisationProcedureCodeChoice.map(withAuthorisationProcedureCodeChoice),
      isEidr.map(withEntryIntoDeclarantsRecords)
    ).flatten

    aDeclaration(modifiers: _*)
  }

  private def aDecl2(
    declarationType: DeclarationType,
    additionalDeclarationType: AdditionalDeclarationType,
    isExporter: Option[String] = None,
    eori: Option[String] = None
  ): ExportsDeclaration = {
    val modifiers: List[ExportsDeclarationModifier] = List(
      Some(withType(declarationType)),
      Some(withAdditionalDeclarationType(additionalDeclarationType)),
      isExporter.map(withDeclarantIsExporter),
      eori.map(eori => withExporterDetails(Some(Eori(eori))))
    ).flatten

    aDeclaration(modifiers: _*)
  }
}
