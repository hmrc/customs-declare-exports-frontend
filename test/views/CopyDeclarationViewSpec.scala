/*
 * Copyright 2023 HM Revenue & Customs
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

package views

import base.ExportsTestData._
import base.{Injector, TestHelper}
import controllers.routes.DeclarationDetailsController
import forms.CopyDeclaration.form
import forms.declaration.ConsignmentReferences.ducrId
import forms.{CopyDeclaration, Ducr, Lrn}
import models.DeclarationType.{CLEARANCE, SUPPLEMENTARY}
import models.requests.{ExportsSessionKeys, JourneyRequest}
import org.jsoup.nodes.Document
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.copy_declaration
import views.tags.ViewTest

@ViewTest
class CopyDeclarationViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  private val page = instanceOf[copy_declaration]

  private def createView(implicit request: JourneyRequest[_]): Document = page(form)(request, messages)

  private def fill(data: CopyDeclaration)(implicit request: JourneyRequest[_]): Document =
    page(form.fill(data))(request, messages)

  private def validate(data: CopyDeclaration)(implicit request: JourneyRequest[_]): Document =
    page(form.fillAndValidate(data))(request, messages)

  "CopyDeclaration page on empty page" should {

    "not have View declaration summary link" in {
      val view = createView(journeyRequest(aDeclaration(), (ExportsSessionKeys.declarationId, "decId")))
      Option(view.getElementById("view_declaration_summary")) mustBe None
    }

    onEveryDeclarationJourney() { implicit request =>
      val view = createView

      "display 'Back' button that links to /submissions/:id/information" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage(backCaption)
        backButton must haveHref(DeclarationDetailsController.displayPage(request.cacheModel.id).url)
      }

      "display page title" in {
        view.getElementById("title").text mustBe messages("declaration.copy.title")
      }

      "display the expected label, body and hint for the DUCR field" in {
        view.getElementsByClass("govuk-label").get(0).text mustBe messages("declaration.copy.ducr.label")
        view.getElementsByClass("govuk-hint").get(0) must containMessage("declaration.copy.ducr.hint")

        val input = view.getElementsByAttributeValue("name", ducrId).get(0)
        input.tagName mustBe "input"
        input.classNames.contains("govuk-input") mustBe true
      }

      "display the expected label, body and hint for the LRN field" in {
        view.getElementsByClass("govuk-label").get(1).text mustBe messages("declaration.copy.lrn.label")
        view.getElementsByClass("govuk-body").get(0) must containMessage("declaration.copy.lrn.paragraph")
        view.getElementsByClass("govuk-hint").get(1) must containMessage("declaration.copy.lrn.hint")

        val input = view.getElementsByAttributeValue("name", "lrn").get(0)
        input.tagName mustBe "input"
        input.classNames.contains("govuk-input") mustBe true
      }

      "display the expected tariff details" in {
        val tariffTitle = view.getElementsByClass("govuk-details__summary-text")
        tariffTitle.text mustBe messages(s"tariff.expander.title.common")

        val tariffDetails = view.getElementsByClass("govuk-details__text").first
        val actualText = removeBlanksIfAnyBeforeDot(tariffDetails.text)

        val prefix = "tariff.declaration.consignmentReferences"
        val expectedText = request.declarationType match {
          case CLEARANCE => s"""
            ${messages(s"$prefix.1.clearance.text", messages(s"$prefix.1.clearance.linkText.0"))}
            ${messages(s"$prefix.2.clearance.text")}
            ${messages(s"$prefix.3.clearance.text")}
          """
          case _ => s"""${if (request.declarationType == SUPPLEMENTARY) messages(s"$prefix.1.supplementary.text") else ""}
            ${messages(s"$prefix.1.common.text", messages(s"$prefix.1.common.linkText.0"))}
            ${messages(s"$prefix.2.common.text", messages(s"$prefix.2.common.linkText.0"))}
            ${messages(s"$prefix.3.common.text")}
          """
        }

        val expectedTextWithNoMargin = removeLineBreakIfAny(removeNewLinesIfAny(expectedText).trim)
        actualText mustBe expectedTextWithNoMargin
      }

      "display a 'Copy declaration' button" in {
        view.getElementById("submit").text mustBe messages("declaration.copy.title")
      }
    }
  }

  "CopyDeclaration page for invalid input" should {

    val incorrectDUCR = "7GB000000000000-1234512345123451234512345"

    onEveryDeclarationJourney() { implicit request =>
      "display error when DUCR is incorrect and LRN empty" in {
        val view = validate(CopyDeclaration(Ducr(incorrectDUCR), Lrn("")))
        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#ducr_ducr")
        view must containErrorElementWithTagAndHref("a", "#lrn")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.ducr.error.invalid")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.empty")
      }

      "display error when DUCR is incorrect and LRN is longer then 22 characters" in {
        val view = validate(CopyDeclaration(Ducr(incorrectDUCR), Lrn(TestHelper.createRandomAlphanumericString(23))))
        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#ducr_ducr")
        view must containErrorElementWithTagAndHref("a", "#lrn")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.ducr.error.invalid")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.length")
      }

      "display error when DUCR is incorrect and LRN contains special character" in {
        val view = validate(CopyDeclaration(Ducr(incorrectDUCR), Lrn("$$%")))
        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#ducr_ducr")
        view must containErrorElementWithTagAndHref("a", "#lrn")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.ducr.error.invalid")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.specialCharacter")
      }

      "display error for empty LRN" in {
        val view = validate(CopyDeclaration(Ducr(ducr), Lrn("")))
        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#lrn")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.empty")
      }

      "display error when LRN is longer then 22 characters" in {
        val view = validate(CopyDeclaration(Ducr(ducr), Lrn(TestHelper.createRandomAlphanumericString(23))))
        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#lrn")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.length")
      }

      "display error when LRN contains special character" in {
        val view = validate(CopyDeclaration(Ducr(ducr), Lrn("#@#$")))
        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#lrn")
        view must containErrorElementWithMessageKey("declaration.consignmentReferences.lrn.error.specialCharacter")
      }
    }
  }

  "CopyDeclaration page when filled" should {

    onEveryDeclarationJourney() { implicit request =>
      "display data in DUCR input" in {
        val view = fill(CopyDeclaration(Ducr(ducr), Lrn("")))
        view.getElementById("ducr_ducr").attr("value") mustBe ducr
      }

      "display data in LRN input" in {
        val view = fill(CopyDeclaration(Ducr(""), Lrn(lrn)))
        view.getElementById("lrn").attr("value") mustBe lrn
      }
    }
  }
}
