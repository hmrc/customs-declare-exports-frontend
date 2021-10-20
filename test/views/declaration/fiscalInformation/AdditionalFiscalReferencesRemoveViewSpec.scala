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
import forms.common.YesNoAnswer
import forms.declaration.AdditionalFiscalReference
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.fiscalInformation.additional_fiscal_references_remove
import views.tags.ViewTest

@ViewTest
class AdditionalFiscalReferencesRemoveViewSpec extends UnitViewSpec with ExportsTestData with Stubs with CommonMessages with Injector {

  private val itemId = "74fd3906"
  private val referenceId = "0.200378103"
  private val additionalReference = AdditionalFiscalReference("FR", "12345")

  private val page = instanceOf[additional_fiscal_references_remove]

  private def createView(form: Form[YesNoAnswer] = YesNoAnswer.form(), reference: AdditionalFiscalReference = additionalReference): Document =
    page(Mode.Normal, itemId, referenceId, reference, form)(request, messages)

  "AdditionalFiscalReferences Remove View" should {
    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.additionalFiscalReferences.remove.title")
    }

    "display reference to remove" in {
      view.getElementsByClass("govuk-summary-list__value").get(1).text() mustBe additionalReference.reference
    }

    "display 'Back' button that links to 'AdditionalFiscalReferences summary' page" in {
      val backLink = view.getElementById("back-link")

      backLink must containMessage(backCaption)
      backLink must haveHref(controllers.declaration.routes.AdditionalFiscalReferencesController.displayPage(Mode.Normal, itemId))
    }

    "display 'Save and continue' button on page" in {
      val saveButton = view.getElementById("submit")
      saveButton must containMessage(saveAndContinueCaption)
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton must containMessage(saveAndReturnCaption)
    }
  }

  "AdditionalFiscalReferences Remove View for invalid input" should {

    "display error if nothing is entered" in {
      val view = createView(YesNoAnswer.form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#code_yes")

      view must containErrorElementWithMessageKey("error.yesNo.required")
    }
  }
}
