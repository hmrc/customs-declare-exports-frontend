/*
 * Copyright 2019 HM Revenue & Customs
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
import helpers.views.declaration.{AdditionalFiscalReferencesMessages, CommonMessages}
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportItemIdGeneratorService
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.additional_fiscal_references
import views.tags.ViewTest

@ViewTest
class AdditionalFiscalReferencesViewSpec extends UnitViewSpec with Stubs with AdditionalFiscalReferencesMessages with CommonMessages {

  private val form: Form[AdditionalFiscalReference] = AdditionalFiscalReference.form()

  private val additionalFiscalReferencesPage = new additional_fiscal_references(mainTemplate)

  val itemId = new ExportItemIdGeneratorService().generateItemId()

  private def createView(form: Form[AdditionalFiscalReference] = form, references: Seq[AdditionalFiscalReference] = Seq.empty): Document =
    additionalFiscalReferencesPage(Mode.Normal, itemId, form, references)

  "Additional Fiscal References View on empty page" should {

    val view = createView()

    "display page title" in {
      view.getElementById("title").text() mustBe title
    }

    "display header" in {
      view.getElementById("section-header").text() must include("declaration.fiscalInformation.header")
    }

    "display country input" in {
      view.getElementById("country-label").text() mustBe fiscalReferenceCountry
      view.getElementById("country").attr("value") mustBe empty
    }

    "display VAT number input" in {
      view.getElementById("reference-label").text() mustBe reference
      view.getElementById("reference").attr("value") mustBe empty
    }

    "display 'Back' button to Fiscal Information page" in {

      val backButton = view.getElementById("link-back")

      backButton.text() mustBe backCaption
      backButton must haveHref(controllers.declaration.routes.FiscalInformationController.displayPage(Mode.Normal, itemId))
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

  "Additional Fiscal References" should {

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

  "Additional Fiscal References for invalid input" should {
    import forms.declaration.AdditionalFiscalReferenceSpec._

    "display error" when {

      "country is empty" in {
        val view = createView(form.bind(emptyCountry))

        view must haveGlobalErrorSummary
        view must haveFieldError("country", fiscalReferenceCountryEmpty)
      }

      "country is incorrect" in {
        val view = createView(form.bind(incorrectCountry))

        view must haveGlobalErrorSummary
        view must haveFieldError("country", fiscalReferenceCountryError)
      }

      "reference is empty" in {
        val view = createView(form.bind(emptyReference))

        view must haveGlobalErrorSummary
        view must haveFieldError("reference", referenceEmpty)
      }

      "reference is incorrect" in {
        val view = createView(form.bind(incorrectReference))

        view must haveGlobalErrorSummary
        view must haveFieldError("reference", referenceError)
      }
      "both country and reference are empty" in {
        val view = createView(form.bind(emptyCountryAndRef))

        view must haveGlobalErrorSummary
        view must haveFieldError("country", fiscalReferenceCountryEmpty)
        view must haveFieldError("reference", referenceEmpty)
      }

      "both country and reference are incorrect" in {
        val view = createView(form.bind(incorrectCountryAndRef))

        view must haveGlobalErrorSummary
        view must haveFieldError("country", fiscalReferenceCountryError)
        view must haveFieldError("reference", referenceError)
      }

    }
  }
}
