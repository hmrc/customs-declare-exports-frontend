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

package views.declaration.fiscalInformation

import base.Injector
import controllers.util.{SaveAndContinue, SaveAndReturn}
import forms.declaration.AdditionalFiscalReference
import models.Mode
import models.requests.JourneyRequest
import models.DeclarationType._
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.{ExportItemIdGeneratorService, ExportsTestData}
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.fiscalInformation.additional_fiscal_references_add
import views.tags.ViewTest

@ViewTest
class AdditionalFiscalReferencesAddViewSpec extends UnitViewSpec with ExportsTestData with Stubs with CommonMessages with Injector {

  private val form: Form[AdditionalFiscalReference] = AdditionalFiscalReference.form()

  private val additionalFiscalReferencesPage = instanceOf[additional_fiscal_references_add]

  val itemId = new ExportItemIdGeneratorService().generateItemId()

  private def createView(form: Form[AdditionalFiscalReference] = form, references: Seq[AdditionalFiscalReference] = Seq.empty)(
    implicit request: JourneyRequest[_]
  ): Document =
    additionalFiscalReferencesPage(Mode.Normal, itemId, form, references)

  "Additional Fiscal References View on empty page" should {
    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1").text() mustBe messages("declaration.additionalFiscalReferences.title")
      }

      "display header" in {
        view.getElementById("section-header").text() must include(messages("declaration.section.5"))
      }

      "display country input" in {
        view.getElementsByAttributeValue("for", "country").text() mustBe messages("declaration.additionalFiscalReferences.country")
        view.getElementById("country").attr("value") mustBe empty
      }

      "display VAT number input" in {
        view.getElementsByAttributeValue("for", "reference").text() mustBe messages("declaration.additionalFiscalReferences.reference")
        view.getElementById("reference").attr("value") mustBe empty
      }

      "display 'Back' button to Fiscal Information page" in {

        val backButton = view.getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton must haveHref(controllers.declaration.routes.FiscalInformationController.displayPage(Mode.Normal, itemId, fastForward = false))
      }

      "display 'For more information about this' summary text" in {
        val detailsSummaryText = view.getElementsByClass("govuk-details__summary-text").first().text()
        val titleKey = request.declarationType match {
          case CLEARANCE => "tariff.expander.title.clearance"
          case _         => "tariff.expander.title.common"
        }
        detailsSummaryText.text() mustBe messages(titleKey)
      }

      "display 'Save and continue' button" in {
        view must containElement("button").withName(SaveAndContinue.toString)
      }

      "display 'Save and return' button" in {
        view must containElement("button").withName(SaveAndReturn.toString)
      }
    }
  }

  "Additional Fiscal References for invalid input" should {
    import forms.declaration.AdditionalFiscalReferenceSpec._

    onEveryDeclarationJourney() { implicit request =>
      "display error" when {

        "country is empty" in {
          val view = createView(form.bind(emptyCountry))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#country")

          view must containErrorElementWithMessage(messages("declaration.additionalFiscalReferences.country.empty"))
        }

        "country is incorrect" in {
          val view = createView(form.bind(incorrectCountry))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#country")

          view must containErrorElementWithMessage(messages("declaration.additionalFiscalReferences.country.error"))
        }

        "reference is empty" in {
          val view = createView(form.bind(emptyReference))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#reference")

          view must containErrorElementWithMessage(messages("declaration.additionalFiscalReferences.reference.empty"))
        }

        "reference is incorrect" in {
          val view = createView(form.bind(incorrectReference))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#reference")

          view must containErrorElementWithMessage(messages("declaration.additionalFiscalReferences.reference.error"))
        }
        "both country and reference are empty" in {
          val view = createView(form.bind(emptyCountryAndRef))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#country")
          view must containErrorElementWithTagAndHref("a", "#reference")

          view must containErrorElementWithMessage(messages("declaration.additionalFiscalReferences.country.empty"))
          view must containErrorElementWithMessage(messages("declaration.additionalFiscalReferences.reference.empty"))
        }

        "both country and reference are incorrect" in {
          val view = createView(form.bind(incorrectCountryAndRef))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#country")
          view must containErrorElementWithTagAndHref("a", "#reference")

          view must containErrorElementWithMessage(messages("declaration.additionalFiscalReferences.country.error"))
          view must containErrorElementWithMessage(messages("declaration.additionalFiscalReferences.reference.error"))
        }
      }
    }
  }
}
