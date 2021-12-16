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

package views.declaration.additionaldeclarationtype

import base.Injector
import controllers.declaration.routes
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import forms.declaration.additionaldeclarationtype._
import models.DeclarationType.DeclarationType
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.additionaldeclarationtype.declaration_type
import views.tags.ViewTest

@ViewTest
class DeclarationTypeViewSpec extends UnitViewSpec with ExportsTestData with CommonMessages with Stubs with Injector {

  private val formStandard: Form[AdditionalDeclarationType] = AdditionalDeclarationTypeStandardDec.form()
  private val formSupplementary: Form[AdditionalDeclarationType] = AdditionalDeclarationTypeSupplementaryDec.form()
  private val formSimplified: Form[AdditionalDeclarationType] = AdditionalDeclarationTypeSimplifiedDec.form()
  private val formOccasional: Form[AdditionalDeclarationType] = AdditionalDeclarationTypeOccasionalDec.form()
  private val formClearance: Form[AdditionalDeclarationType] = AdditionalDeclarationTypeClearanceDec.form()
  private val declarationTypePage = instanceOf[declaration_type]
  private def createView(form: Form[AdditionalDeclarationType], journeyType: DeclarationType): Document =
    declarationTypePage(Mode.Normal, form)(journeyRequest(journeyType), messages)

  "Declaration Type View on empty page" should {

    "display page title" when {

      "used for Standard Declaration journey" in {
        val viewWithMessage = createView(formStandard, DeclarationType.STANDARD)
        viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
      }

      "used for Supplementary Declaration journey" in {
        val viewWithMessage = createView(formSupplementary, DeclarationType.SUPPLEMENTARY)
        viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
      }

      "used for Simplified Declaration journey" in {
        val viewWithMessage = createView(formSimplified, DeclarationType.SIMPLIFIED)
        viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
      }

      "used for Occasional Declaration journey" in {
        val viewWithMessage = createView(formOccasional, DeclarationType.OCCASIONAL)
        viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
      }

      "used for Clearance Request journey" in {
        val viewWithMessage = createView(formClearance, DeclarationType.CLEARANCE)
        viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
      }
    }

    "display inset text" when {

      "used for Standard Declaration journey" in {
        val viewWithInset = createView(formStandard, DeclarationType.STANDARD)
        val inset = viewWithInset.getElementsByClass("govuk-inset-text")
        inset.size mustBe 1
      }

      "used for Supplementary Declaration journey" in {
        val viewWithInset = createView(formSupplementary, DeclarationType.SUPPLEMENTARY)
        val inset = viewWithInset.getElementsByClass("govuk-inset-text")
        inset.size mustBe 0
      }

      "used for Simplified Declaration journey" in {
        val viewWithInset = createView(formSimplified, DeclarationType.SIMPLIFIED)
        val inset = viewWithInset.getElementsByClass("govuk-inset-text")
        inset.size mustBe 1
      }

      "used for Occasional Declaration journey" in {
        val viewWithInset = createView(formOccasional, DeclarationType.OCCASIONAL)
        val inset = viewWithInset.getElementsByClass("govuk-inset-text")
        inset.size mustBe 0
      }

      "used for Clearance Request journey" in {
        val viewWithInset = createView(formClearance, DeclarationType.CLEARANCE)
        val inset = viewWithInset.getElementsByClass("govuk-inset-text")
        inset.size mustBe 1
      }
    }

    "display 'Back' button that links to 'Declaration Choice' page" when {

      "used for Standard Declaration journey" in {
        val backButton = createView(formStandard, DeclarationType.STANDARD).getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.DeclarationChoiceController.displayPage().url
      }

      "used for Supplementary Declaration journey" in {

        val backButton = createView(formSupplementary, DeclarationType.SUPPLEMENTARY).getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.DeclarationChoiceController.displayPage().url
      }

      "used for Simplified Declaration journey" in {
        val backButton = createView(formSimplified, DeclarationType.SIMPLIFIED).getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.DeclarationChoiceController.displayPage().url
      }

      "used for Occasional Declaration journey" in {
        val backButton = createView(formOccasional, DeclarationType.OCCASIONAL).getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.DeclarationChoiceController.displayPage().url
      }

      "used for Clearance Request journey" in {
        val backButton = createView(formClearance, DeclarationType.CLEARANCE).getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.DeclarationChoiceController.displayPage().url
      }
    }

    "display 'Continue' button" when {

      "used for Standard Declaration journey" in {
        val view: Document = createView(formStandard, DeclarationType.STANDARD)
        view.getElementById("submit").text() mustBe messages(continueCaption)
      }

      "used for Supplementary Declaration journey" in {
        val view: Document = createView(formSupplementary, DeclarationType.SUPPLEMENTARY)
        view.getElementById("submit").text() mustBe messages(continueCaption)
      }

      "used for Simplified Declaration journey" in {
        val view: Document = createView(formSimplified, DeclarationType.SIMPLIFIED)
        view.getElementById("submit").text() mustBe messages(continueCaption)
      }

      "used for Occasional Declaration journey" in {
        val view: Document = createView(formOccasional, DeclarationType.OCCASIONAL)
        view.getElementById("submit").text() mustBe messages(continueCaption)
      }

      "used for Clearance Request journey" in {
        val view: Document = createView(formClearance, DeclarationType.CLEARANCE)
        view.getElementById("submit").text() mustBe messages(continueCaption)
      }
    }

    "not display 'Save and return' button" when {

      "used for Standard Declaration journey" in {
        val view: Document = createView(formStandard, DeclarationType.STANDARD)
        val button = view.getElementById("submit_and_return")
        button mustBe null
      }

      "used for Supplementary Declaration journey" in {
        val view: Document = createView(formSupplementary, DeclarationType.SUPPLEMENTARY)
        val button = view.getElementById("submit_and_return")
        button mustBe null
      }

      "used for Simplified Declaration journey" in {
        val view: Document = createView(formSimplified, DeclarationType.SIMPLIFIED)
        val button = view.getElementById("submit_and_return")
        button mustBe null
      }

      "used for Occasional Declaration journey" in {
        val view: Document = createView(formOccasional, DeclarationType.OCCASIONAL)
        val button = view.getElementById("submit_and_return")
        button mustBe null
      }

      "used for Clearance Request journey" in {
        val view: Document = createView(formClearance, DeclarationType.CLEARANCE)
        val button = view.getElementById("submit_and_return")
        button mustBe null
      }
    }

    "display header" when {

      "used for Standard Declaration journey" in {

        val view = createView(formStandard, DeclarationType.STANDARD)

        view.title() must include(messages("declaration.declarationType.header.standard"))
      }

      "used for Supplementary Declaration journey" in {

        val view = createView(formSupplementary, DeclarationType.SUPPLEMENTARY)

        view.title() must include(messages("declaration.declarationType.header.supplementary"))
      }

      "used for Simplified Declaration journey" in {

        val view = createView(formSimplified, DeclarationType.SIMPLIFIED)

        view.title() must include(messages("declaration.declarationType.header.simplified"))
      }

      "used for Occasional Declaration journey" in {

        val view = createView(formOccasional, DeclarationType.OCCASIONAL)

        view.title() must include(messages("declaration.declarationType.header.occasional"))
      }

      "used for Clearance Request journey" in {

        val view = createView(formClearance, DeclarationType.CLEARANCE)

        view.title() must include(messages("declaration.declarationType.header.clearance"))
      }
    }

    "display two radio buttons with description (not selected)" when {

      "used for Standard Declaration journey" in {

        val view = createView(formStandard, DeclarationType.STANDARD)

        val optionOne = view.getElementsByAttributeValue("for", "PreLodged")
        optionOne.attr("checked") mustBe empty
        optionOne.text() mustBe messages("declaration.declarationType.inputText.standard.preLodged")

        val optionTwo = view.getElementsByAttributeValue("for", "Frontier")
        optionTwo.attr("checked") mustBe empty
        optionTwo.text() mustBe messages("declaration.declarationType.inputText.standard.frontier")
      }

      "used for Supplementary Declaration journey" in {

        val view = createView(formSupplementary, DeclarationType.SUPPLEMENTARY)

        val optionOne = view.getElementsByAttributeValue("for", "Simplified")
        optionOne.attr("checked") mustBe empty
        optionOne.text() mustBe messages("declaration.declarationType.inputText.supplementary.simplified")

        val optionTwo = view.getElementsByAttributeValue("for", "Standard")
        optionTwo.attr("checked") mustBe empty
        optionTwo.text() mustBe messages("declaration.declarationType.inputText.supplementary.standard")
      }

      "used for Simplified Declaration journey" in {

        val view = createView(formSimplified, DeclarationType.SIMPLIFIED)

        val optionOne = view.getElementsByAttributeValue("for", "PreLodged")
        optionOne.attr("checked") mustBe empty
        optionOne.text() mustBe messages("declaration.declarationType.inputText.simplified.preLodged")

        val optionTwo = view.getElementsByAttributeValue("for", "Frontier")
        optionTwo.attr("checked") mustBe empty
        optionTwo.text() mustBe messages("declaration.declarationType.inputText.simplified.frontier")
      }

      "used for Occasional Declaration journey" in {

        val view = createView(formOccasional, DeclarationType.OCCASIONAL)

        val optionOne = view.getElementsByAttributeValue("for", "PreLodged")
        optionOne.attr("checked") mustBe empty
        optionOne.text() mustBe messages("declaration.declarationType.inputText.occasional.preLodged")

        val optionTwo = view.getElementsByAttributeValue("for", "Frontier")
        optionTwo.attr("checked") mustBe empty
        optionTwo.text() mustBe messages("declaration.declarationType.inputText.occasional.frontier")
      }

      "used for Clearance Request journey" in {

        val view = createView(formClearance, DeclarationType.CLEARANCE)

        val optionOne = view.getElementsByAttributeValue("for", "PreLodged")
        optionOne.attr("checked") mustBe empty
        optionOne.text() mustBe messages("declaration.declarationType.inputText.clearance.preLodged")

        val optionTwo = view.getElementsByAttributeValue("for", "Frontier")
        optionTwo.attr("checked") mustBe empty
        optionTwo.text() mustBe messages("declaration.declarationType.inputText.clearance.frontier")
      }
    }

  }

  "Declaration Type View with invalid input" should {

    "display error if nothing is selected" when {

      "used for Standard Declaration journey" in {

        val view = createView(formStandard.bind(Map[String, String]()), DeclarationType.STANDARD)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#PreLodged")

        view must containErrorElementWithMessageKey("declaration.declarationType.inputText.error.empty")
      }

      "used for Supplementary Declaration journey" in {

        val view = createView(formSupplementary.bind(Map[String, String]()), DeclarationType.SUPPLEMENTARY)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#Simplified")

        view must containErrorElementWithMessageKey("declaration.declarationType.inputText.error.empty")
      }

      "used for Simplified Declaration journey" in {

        val view = createView(formSimplified.bind(Map[String, String]()), DeclarationType.SIMPLIFIED)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#PreLodged")

        view must containErrorElementWithMessageKey("declaration.declarationType.inputText.error.empty")
      }

      "used for Occasional Declaration journey" in {

        val view = createView(formOccasional.bind(Map[String, String]()), DeclarationType.OCCASIONAL)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#PreLodged")

        view must containErrorElementWithMessageKey("declaration.declarationType.inputText.error.empty")
      }

      "used for Clearance Request journey" in {

        val view = createView(formClearance.bind(Map[String, String]()), DeclarationType.CLEARANCE)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#PreLodged")

        view must containErrorElementWithMessageKey("declaration.declarationType.inputText.error.empty")
      }
    }

    "display error if incorrect declaration is selected" when {

      "used for Standard Declaration journey" in {

        val view = createView(formStandard.bind(Map("additionalDeclarationType" -> "#")), DeclarationType.STANDARD)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#PreLodged")

        view must containErrorElementWithMessageKey("declaration.declarationType.inputText.error.incorrect")
      }

      "used for Supplementary Declaration journey" in {

        val view = createView(formSupplementary.bind(Map("additionalDeclarationType" -> "#")), DeclarationType.SUPPLEMENTARY)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#Simplified")

        view must containErrorElementWithMessageKey("declaration.declarationType.inputText.error.incorrect")
      }

      "used for Simplified Declaration journey" in {

        val view = createView(formSimplified.bind(Map("additionalDeclarationType" -> "#")), DeclarationType.SIMPLIFIED)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#PreLodged")

        view must containErrorElementWithMessageKey("declaration.declarationType.inputText.error.incorrect")
      }

      "used for Occasional Declaration journey" in {

        val view = createView(formOccasional.bind(Map("additionalDeclarationType" -> "#")), DeclarationType.OCCASIONAL)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#PreLodged")

        view must containErrorElementWithMessageKey("declaration.declarationType.inputText.error.incorrect")
      }

      "used for Clearance Request journey" in {

        val view = createView(formClearance.bind(Map("additionalDeclarationType" -> "#")), DeclarationType.CLEARANCE)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#PreLodged")

        view must containErrorElementWithMessageKey("declaration.declarationType.inputText.error.incorrect")
      }
    }

  }

  "Declaration Type View with entered data" should {

    "display selected first radio button" when {

      "used for Standard Declaration journey - Pre-lodged (D)" in {

        val view = createView(formStandard.fill(AdditionalDeclarationType.STANDARD_PRE_LODGED), DeclarationType.STANDARD)

        val optionOne = view.getElementById("PreLodged")
        optionOne.getElementsByAttribute("checked").size() mustBe 1

        val optionTwo = view.getElementById("Frontier")
        optionTwo.attr("checked") mustBe empty
      }

      "used for Supplementary Declaration journey - Simplified (Y)" in {

        val view = createView(formSupplementary.fill(AdditionalDeclarationType.SUPPLEMENTARY_SIMPLIFIED), DeclarationType.SUPPLEMENTARY)

        val optionOne = view.getElementById("Simplified")
        optionOne.getElementsByAttribute("checked").size() mustBe 1

        val optionTwo = view.getElementById("Standard")
        optionTwo.attr("checked") mustBe empty
      }

      "used for Simplified Declaration journey - Pre-lodged (F)" in {

        val view = createView(formSimplified.fill(AdditionalDeclarationType.SIMPLIFIED_PRE_LODGED), DeclarationType.SIMPLIFIED)

        val optionOne = view.getElementById("PreLodged")
        optionOne.getElementsByAttribute("checked").size() mustBe 1

        val optionTwo = view.getElementById("Frontier")
        optionTwo.attr("checked") mustBe empty
      }

      "used for Occasional Declaration journey - Pre-lodged (E)" in {

        val view = createView(formOccasional.fill(AdditionalDeclarationType.OCCASIONAL_PRE_LODGED), DeclarationType.OCCASIONAL)

        val optionOne = view.getElementById("PreLodged")
        optionOne.getElementsByAttribute("checked").size() mustBe 1

        val optionTwo = view.getElementById("Frontier")
        optionTwo.attr("checked") mustBe empty
      }

      "used for Clearance Request journey - Pre-lodged (K)" in {

        val view = createView(formClearance.fill(AdditionalDeclarationType.CLEARANCE_PRE_LODGED), DeclarationType.CLEARANCE)

        val optionOne = view.getElementById("PreLodged")
        optionOne.getElementsByAttribute("checked").size() mustBe 1

        val optionTwo = view.getElementById("Frontier")
        optionTwo.attr("checked") mustBe empty
      }
    }

    "display selected second radio button" when {

      "used for Standard Declaration journey - Frontier (A)" in {

        val view = createView(formStandard.fill(AdditionalDeclarationType.STANDARD_FRONTIER), DeclarationType.STANDARD)

        val optionOne = view.getElementById("PreLodged")
        optionOne.attr("checked") mustBe empty

        val optionTwo = view.getElementById("Frontier")
        optionTwo.getElementsByAttribute("checked").size() mustBe 1
      }

      "used for Supplementary Declaration journey - Standard (Z)" in {

        val view = createView(formSupplementary.fill(AdditionalDeclarationType.SUPPLEMENTARY_EIDR), DeclarationType.SUPPLEMENTARY)

        val optionOne = view.getElementById("Simplified")
        optionOne.attr("checked") mustBe empty

        val optionTwo = view.getElementById("Standard")
        optionTwo.getElementsByAttribute("checked").size() mustBe 1
      }

      "used for Simplified Declaration journey - Frontier (C)" in {

        val view = createView(formSimplified.fill(AdditionalDeclarationType.SIMPLIFIED_FRONTIER), DeclarationType.SIMPLIFIED)

        val optionOne = view.getElementById("PreLodged")
        optionOne.attr("checked") mustBe empty

        val optionTwo = view.getElementById("Frontier")
        optionTwo.getElementsByAttribute("checked").size() mustBe 1
      }

      "used for Occasional Declaration journey - Frontier (B)" in {

        val view = createView(formOccasional.fill(AdditionalDeclarationType.OCCASIONAL_FRONTIER), DeclarationType.OCCASIONAL)

        val optionOne = view.getElementById("PreLodged")
        optionOne.attr("checked") mustBe empty

        val optionTwo = view.getElementById("Frontier")
        optionTwo.getElementsByAttribute("checked").size() mustBe 1
      }

      "used for Clearance Request journey - Frontier (L)" in {

        val view = createView(formClearance.fill(AdditionalDeclarationType.CLEARANCE_FRONTIER), DeclarationType.CLEARANCE)

        val optionOne = view.getElementById("PreLodged")
        optionOne.attr("checked") mustBe empty

        val optionTwo = view.getElementById("Frontier")
        optionTwo.getElementsByAttribute("checked").size() mustBe 1
      }
    }

  }

}
