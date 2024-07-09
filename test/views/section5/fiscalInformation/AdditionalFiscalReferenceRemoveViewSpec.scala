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

package views.section5.fiscalInformation

import base.Injector
import controllers.section5.routes.AdditionalFiscalReferencesController
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.form
import forms.section5.AdditionalFiscalReference
import models.DeclarationType.STANDARD
import org.jsoup.nodes.Document
import play.api.data.Form
import views.common.PageWithButtonsSpec
import views.html.section5.fiscalInformation.additional_fiscal_reference_remove
import views.tags.ViewTest

@ViewTest
class AdditionalFiscalReferenceRemoveViewSpec extends PageWithButtonsSpec with Injector {

  val referenceId = "0.200378103"
  val additionalReference = AdditionalFiscalReference("FR", "12345")

  val page = instanceOf[additional_fiscal_reference_remove]

  override val typeAndViewInstance = (STANDARD, page(itemId, referenceId, additionalReference, form())(_, _))

  def createView(frm: Form[YesNoAnswer] = form()): Document =
    page(itemId, referenceId, additionalReference, frm)(request, messages)

  "AdditionalFiscalReferences Remove View" should {
    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.additionalFiscalReferences.remove.title")
    }

    "display reference to remove" in {
      view.getElementsByClass(summaryValueClassName).get(1).text() mustBe additionalReference.reference
    }

    "display 'Back' button that links to 'AdditionalFiscalReferences summary' page" in {
      val backLink = view.getElementById("back-link")

      backLink must containMessage(backToPreviousQuestionCaption)
      backLink must haveHref(AdditionalFiscalReferencesController.displayPage(itemId))
    }

    checkAllSaveButtonsAreDisplayed(createView())
  }

  "AdditionalFiscalReferences Remove View for invalid input" should {
    "display error if nothing is entered" in {
      val view = createView(form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#code_yes")

      view must containErrorElementWithMessageKey("error.yesNo.required")
    }
  }
}
