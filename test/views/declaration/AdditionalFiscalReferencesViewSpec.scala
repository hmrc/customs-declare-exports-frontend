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

import controllers.util.{Add, SaveAndContinue, SaveAndReturn}
import forms.declaration.AdditionalFiscalReference
import helpers.views.declaration.CommonMessages
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportItemIdGeneratorService
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.additional_fiscal_references
import views.tags.ViewTest

@ViewTest
class AdditionalFiscalReferencesViewSpec extends UnitViewSpec with Stubs with CommonMessages {

  private val form: Form[AdditionalFiscalReference] = AdditionalFiscalReference.form()

  private val additionalFiscalReferencesPage = new additional_fiscal_references(mainTemplate)

  val itemId = new ExportItemIdGeneratorService().generateItemId()

  private def createView(form: Form[AdditionalFiscalReference] = form, references: Seq[AdditionalFiscalReference] = Seq.empty)(
    implicit request: JourneyRequest[_]
  ): Document =
    additionalFiscalReferencesPage(Mode.Normal, itemId, form, references)

  "Additional Fiscal References View on empty page" should {
    onEveryDeclarationJourney { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementById("title").text() mustBe "declaration.additionalFiscalReferences.title"
      }

      "display header" in {
        view.getElementById("section-header").text() must include("declaration.fiscalInformation.header")
      }

      "display country input" in {
        view.getElementById("country-label").text() mustBe "declaration.additionalFiscalReferences.country"
        view.getElementById("country").attr("value") mustBe empty
      }

      "display VAT number input" in {
        view.getElementById("reference-label").text() mustBe "declaration.additionalFiscalReferences.reference"
        view.getElementById("reference").attr("value") mustBe empty
      }

      "display 'Back' button to Fiscal Information page" in {

        val backButton = view.getElementById("back-link")

        backButton.text() mustBe backCaption
        backButton must haveHref(controllers.declaration.routes.FiscalInformationController.displayPage(Mode.Normal, itemId, fastForward = false))
      }

      "display 'For more information about this' summary text" in {
        val detailsSummaryText = view.getElementsByClass("govuk-details__summary-text").first().text()
        detailsSummaryText.text() mustBe "site.details.summary_text_this"
      }

      "display 'Save and continue' button" in {
        view must containElement("button").withName(SaveAndContinue.toString)
      }

      "display 'Save and return' button" in {
        view must containElement("button").withName(SaveAndReturn.toString)
      }

      "display 'Add' button" in {
        view must containElement("button").withName(Add.toString)
      }
    }
  }

  "Additional Fiscal References" should {
    onEveryDeclarationJourney { implicit request =>
      val view = createView(references = Seq(AdditionalFiscalReference("FR", "12345")))

      "display references" in {
        view.text() must include("FR12345")

      }

      "display remove button" in {
        view.getElementsByAttributeValue("class", "remove button--secondary").first() must submitTo(
          controllers.declaration.routes.AdditionalFiscalReferencesController
            .removeReference(Mode.Normal, itemId, "FR12345")
        )
      }
    }
  }

  "Additional Fiscal References for invalid input" should {
    import forms.declaration.AdditionalFiscalReferenceSpec._

    onEveryDeclarationJourney { implicit request =>
      "display error" when {

        "country is empty" in {
          val view = createView(form.bind(emptyCountry))

          view must haveGlobalErrorSummary
          view must haveFieldError("country", "declaration.additionalFiscalReferences.country.empty")
        }

        "country is incorrect" in {
          val view = createView(form.bind(incorrectCountry))

          view must haveGlobalErrorSummary
          view must haveFieldError("country", "declaration.additionalFiscalReferences.country.error")
        }

        "reference is empty" in {
          val view = createView(form.bind(emptyReference))

          view must haveGlobalErrorSummary
          view must haveFieldError("reference", "declaration.additionalFiscalReferences.reference.empty")
        }

        "reference is incorrect" in {
          val view = createView(form.bind(incorrectReference))

          view must haveGlobalErrorSummary
          view must haveFieldError("reference", "declaration.additionalFiscalReferences.reference.error")
        }
        "both country and reference are empty" in {
          val view = createView(form.bind(emptyCountryAndRef))

          view must haveGlobalErrorSummary
          view must haveFieldError("country", "declaration.additionalFiscalReferences.country.empty")
          view must haveFieldError("reference", "declaration.additionalFiscalReferences.reference.empty")
        }

        "both country and reference are incorrect" in {
          val view = createView(form.bind(incorrectCountryAndRef))

          view must haveGlobalErrorSummary
          view must haveFieldError("country", "declaration.additionalFiscalReferences.country.error")
          view must haveFieldError("reference", "declaration.additionalFiscalReferences.reference.error")
        }
      }
    }
  }
}
