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

package views.section1

import base.Injector
import controllers.section1.routes.{AdditionalDeclarationTypeController, DeclarantDetailsController}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.form
import models.DeclarationType._
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import views.html.section1.ducr_choice
import views.common.PageWithButtonsSpec
import views.tags.ViewTest

@ViewTest
class DucrChoiceViewSpec extends PageWithButtonsSpec with Injector {

  val page = instanceOf[ducr_choice]

  override val typeAndViewInstance = (STANDARD, page(form())(_, _))

  def createView(frm: Form[YesNoAnswer] = form())(implicit request: JourneyRequest[_]): Document =
    page(frm)(request, messages)

  "'Ducr Choice' view" should {

    onClearance { implicit request =>
      val view = createView()

      "display the expected notification banner" in {
        val banner = view.getElementsByClass("govuk-notification-banner").get(0)

        val title = banner.getElementsByClass("govuk-notification-banner__title").text
        title mustBe messages("declaration.ducr.banner.title")

        val content = banner.getElementsByClass("govuk-notification-banner__content").get(0)
        content.text mustBe messages("declaration.ducr.banner.content")
      }

      "display 'Back' button to the /type page" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(AdditionalDeclarationTypeController.displayPage)
      }
    }

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL) { implicit request =>
      val view = createView()

      "display 'Back' button to the /declarant-details page" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(DeclarantDetailsController.displayPage)
      }
    }

    onJourney(STANDARD, CLEARANCE, SIMPLIFIED, OCCASIONAL) { implicit request =>
      val view = createView()

      "display header" in {
        view.getElementById("section-header") must containMessage("declaration.section.1")
      }

      "display page title" in {
        view.getElementsByTag("h1").first() must containMessage("declaration.ducr.choice.title")
      }

      "display two Yes/No radio buttons" in {
        val radios = view.getElementsByClass("govuk-radios").first.children
        radios.size mustBe 2
        Option(radios.first.getElementById("code_yes")) mustBe defined
        Option(radios.last.getElementById("code_no")) mustBe defined

        radios.last.text mustBe messages("declaration.ducr.choice.answer.no")
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
        val errorKey = "declaration.ducr.choice.answer.empty"
        val view: Document = createView(YesNoAnswer.form(errorKey = errorKey).fillAndValidate(YesNoAnswer("")))
        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithMessageKey(errorKey)
      }

      checkAllSaveButtonsAreDisplayed(createView())

      "display the expected tariff details" in {
        val expectedKey = if (request.isType(CLEARANCE)) "clearance" else "common"

        val tariffTitle = view.getElementsByClass("govuk-details__summary-text")
        tariffTitle.text mustBe messages(s"tariff.expander.title.$expectedKey")

        val tariffDetails = view.getElementsByClass("govuk-details__text").first

        val prefix = "tariff.declaration"

        val expectedText = messages(s"$prefix.text", messages(s"$prefix.ducr.common.linkText.0"))

        val actualText = removeBlanksIfAnyBeforeDot(tariffDetails.text)
        actualText mustBe removeLineBreakIfAny(expectedText)
      }
    }
  }
}
