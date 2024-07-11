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

package views.section4

import base.Injector
import controllers.summary.routes.SectionSummaryController
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.form
import models.DeclarationType.{STANDARD, SUPPLEMENTARY}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import views.common.PageWithButtonsSpec
import views.html.section4.invoice_and_exchange_rate_choice
import views.tags.ViewTest

@ViewTest
class InvoiceAndExchangeRateChoiceViewSpec extends PageWithButtonsSpec with Injector {

  val page = instanceOf[invoice_and_exchange_rate_choice]

  override val typeAndViewInstance = (STANDARD, page(form())(_, _))

  def createView(frm: Form[YesNoAnswer] = form())(implicit request: JourneyRequest[_]): Document =
    page(frm)(request, messages)

  "'Invoice And Exchange Rate Choice' view" should {

    onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
      val view = createView()

      "display 'Back' button to the /office-of-exit page" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage(backCaption)
        backButton must haveHref(SectionSummaryController.displayPage(3))
      }

      "display header" in {
        view.getElementById("section-header") must containMessage("declaration.section.4")
      }

      "display page title" in {
        view.getElementsByTag("h1").first() must containMessage("declaration.invoice.amount.choice.title")
      }

      "display two Yes/No radio buttons" in {
        val radios = view.getElementsByClass("govuk-radios").first.children
        radios.size mustBe 2
        Option(radios.first.getElementById("code_yes")) mustBe defined
        Option(radios.last.getElementById("code_no")) mustBe defined

        radios.last.text mustBe messages("declaration.invoice.amount.choice.answer.no")
      }

      "select the 'Yes' radio when clicked" in {
        val view = createView(form().bind(Map("yesNo" -> "Yes")))
        view.getElementById("code_yes") must beSelected
      }

      "select the 'No' radio when clicked" in {
        val view = createView(form().bind(Map("yesNo" -> "No")))
        view.getElementById("code_no") must beSelected
      }

      "display error when neither 'yes' and 'no' are selected" in {
        val errorKey = "declaration.invoice.amount.choice.answer.empty"
        val view: Document = createView(YesNoAnswer.form(errorKey = errorKey).fillAndValidate(YesNoAnswer("")))
        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithMessageKey(errorKey)
      }

      checkAllSaveButtonsAreDisplayed(createView())

      "display the expected tariff details" in {
        val tariffTitle = view.getElementsByClass("govuk-details__summary-text")
        tariffTitle.text mustBe messages("tariff.expander.title.common")

        val tariffDetails = view.getElementsByClass("govuk-details__text").first

        val prefix = "tariff.declaration.totalNumbersOfItems"
        val expectedText = messages("tariff.declaration.text", messages(s"$prefix.common.linkText.0"), messages(s"$prefix.common.linkText.1"))

        val actualText = removeBlanksIfAnyBeforeDot(tariffDetails.text)
        actualText contains removeLineBreakIfAny(expectedText)
      }
    }
  }
}
