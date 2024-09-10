/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.section1.routes.AdditionalDeclarationTypeController
import controllers.section2.routes.EntryIntoDeclarantsRecordsController
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section1.DeclarantEoriConfirmation.form
import forms.section1.DeclarantEoriConfirmation
import models.DeclarationType.{OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestHelper
import tools.Stubs
import views.helpers.CommonMessages
import views.html.section1.declarant_details
import views.common.UnitViewSpec
import views.tags.ViewTest

@ViewTest
class DeclarantDetailsViewSpec extends UnitViewSpec with ExportsTestHelper with CommonMessages with Stubs with Injector {

  private val declarantDetailsPage = instanceOf[declarant_details]
  private def createView(form: Form[DeclarantEoriConfirmation])(implicit request: JourneyRequest[_]): Document =
    declarantDetailsPage(form)(request, messages)

  "Declarant Details View on empty page" should {

    onEveryDeclarationJourney() { implicit request =>
      val view = createView(form)

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.declarant.title", request.eori)
      }

      "display radio button with Yes option" in {
        view.getElementById("code_yes").attr("value") mustBe YesNoAnswers.yes
        view.getElementsByAttributeValue("for", "code_yes") must containMessageForElements("site.yes")
      }

      "display radio button with No option" in {
        view.getElementById("code_no").attr("value") mustBe YesNoAnswers.no
        view.getElementsByAttributeValue("for", "code_no") must containMessageForElements("site.no")
      }

      "display the expected notification banner" in {
        val banner = view.getElementsByClass("govuk-notification-banner").get(0)

        val title = banner.getElementsByClass("govuk-notification-banner__title").text
        title mustBe messages("declaration.declarant.eori.banner.title")

        val content = banner.getElementsByClass("govuk-notification-banner__content").get(0)
        content.text mustBe messages("declaration.declarant.eori.banner.content")
      }

      checkSaveAndContinueButtonIsDisplayed(view)
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display section header" in {
        createView(form).getElementById("section-header") must containMessage("declaration.section.1")
      }

      "display 'Back' button that links to 'Additional Declaration Type' page" in {

        val view = declarantDetailsPage(form)(request, messages)
        val backButton = view.getElementById("back-link")

        backButton must containMessage(backCaption)
        backButton must haveHref(AdditionalDeclarationTypeController.displayPage.url)
      }

      "not display 'Save and return' button on page" in {
        Option(createView(form).getElementById("submit_and_return")).isEmpty
      }
    }

    onClearance { implicit request =>
      "display section header" in {
        createView(form).getElementById("section-header") must containMessage("declaration.section.2")
      }

      "display 'Back' button that links to 'Entry into Declarant's Records' page" in {
        val view = declarantDetailsPage(form)(request, messages)
        val backButton = view.getElementById("back-link")

        backButton must containMessage(backCaption)
        backButton must haveHref(EntryIntoDeclarantsRecordsController.displayPage.url)
      }

      checkExitAndReturnLinkIsDisplayed(createView(form))
    }
  }

  "Declarant Details View with invalid input" should {

    onEveryDeclarationJourney() { implicit request =>
      "display error when answer is empty" in {
        val view = createView(form.fillAndValidate(DeclarantEoriConfirmation("")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#code_yes")

        view must containErrorElementWithMessageKey("declaration.declarant.error")
      }

      "display error when EORI is provided, but is incorrect" in {
        val view = createView(
          form
            .fillAndValidate(DeclarantEoriConfirmation("wrong"))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#code_yes")

        view must containErrorElementWithMessageKey("declaration.declarant.error")
      }
    }

  }

  "Declarant Details View when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display answer input" in {
        val form = DeclarantEoriConfirmation.form.fill(DeclarantEoriConfirmation(YesNoAnswers.yes))
        val view = createView(form)

        view.getElementById("code_yes").attr("value") mustBe YesNoAnswers.yes
        view.getElementById("code_yes").attr("value") mustBe YesNoAnswers.yes
      }
    }
  }
}
