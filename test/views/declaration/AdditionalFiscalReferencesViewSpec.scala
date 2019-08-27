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
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.additional_fiscal_references
import views.tags.ViewTest

@ViewTest
class AdditionalFiscalReferencesViewSpec extends ViewSpec with AdditionalFiscalReferencesMessages with CommonMessages {

  private val form: Form[AdditionalFiscalReference] = AdditionalFiscalReference.form()
  private val additionalFiscalReferencesPage = app.injector.instanceOf[additional_fiscal_references]

  private def createView(
    form: Form[AdditionalFiscalReference] = form,
    references: Seq[AdditionalFiscalReference] = Seq.empty
  ): Html =
    additionalFiscalReferencesPage(Mode.Normal, itemId, form, references)(fakeRequest, messages)

  "Additional Fiscal References View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() must be(messages(title))

    }

    "display header" in {

      createView().getElementById("section-header").text() must be(messages("declaration.fiscalInformation.header"))

    }

    "display country input" in {

      createView().getElementById("country-label").text() must be(messages(fiscalReferenceCountry))
      createView().getElementById("country").attr("value") must be("")
    }

    "display VAT number input" in {

      createView().getElementById("reference-label").text() must be(messages(reference))
      createView().getElementById("reference").attr("value") must be("")

    }

    "display 'Back' button to Fiscal Information page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be(s"/customs-declare-exports/declaration/items/$itemId/fiscal-information")

    }

    "display 'Save and continue' button" in {
      val view: Document = createView()
      view must containElement("button").withName(SaveAndContinue.toString)
    }

    "display 'Save and return' button" in {
      val view: Document = createView()
      view must containElement("button").withName(SaveAndReturn.toString)
    }

    "display 'Add' button" in {
      val view: Document = createView()
      view must containElement("button").withName(Add.toString)
    }

  }

  "Additional Fiscal References" should {

    "display references" in {
      val view = createView(references = Seq(AdditionalFiscalReference("FR", "12345")))

      view.body must include("FR12345")
    } 
  }

  "Additional Fiscal References for invalid input" should {
    import forms.declaration.AdditionalFiscalReferenceSpec._

    "display error" when {

      "country is empty" in {
        val view = createView(form.bind(emptyCountry))

        checkErrorsSummary(view)
        checkErrorLink(view, "country-error", messages(fiscalReferenceCountryEmpty), "#country")
      }

      "country is incorrect" in {
        val view = createView(form.bind(incorrectCountry))

        checkErrorsSummary(view)
        checkErrorLink(view, "country-error", messages(fiscalReferenceCountryError), "#country")
      }

      "reference is empty" in {
        val view = createView(form.bind(emptyReference))

        checkErrorsSummary(view)
        checkErrorLink(view, "reference-error", messages(referenceEmpty), "#reference")
      }

      "reference is incorrect" in {
        val view = createView(form.bind(incorrectReference))

        checkErrorsSummary(view)
        checkErrorLink(view, "reference-error", messages(referenceError), "#reference")
      }
      "both country and reference are empty" in {
        val view = createView(form.bind(emptyCountryAndRef))

        checkErrorsSummary(view)
        checkErrorLink(view, "country-error", messages(fiscalReferenceCountryEmpty), "#country")
        checkErrorLink(view, "reference-error", messages(referenceEmpty), "#reference")
      }
      "both country and reference are incorrect" in {
        val view = createView(form.bind(incorrectCountryAndRef))

        checkErrorsSummary(view)
        checkErrorLink(view, "country-error", messages(fiscalReferenceCountryError), "#country")
        checkErrorLink(view, "reference-error", messages(referenceError), "#reference")
      }

    }
  }

}
