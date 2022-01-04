/*
 * Copyright 2022 HM Revenue & Customs
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

import base.Injector
import controllers.declaration.routes
import controllers.helpers.SaveAndReturn
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.DeclarantEoriConfirmation
import forms.declaration.DeclarantEoriConfirmation.form
import models.DeclarationType.{OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.declarant_details
import views.tags.ViewTest

@ViewTest
class DeclarantDetailsViewSpec extends UnitViewSpec with ExportsTestData with CommonMessages with Stubs with Injector {

  private val declarantDetailsPage = instanceOf[declarant_details]
  private def createView(form: Form[DeclarantEoriConfirmation])(implicit request: JourneyRequest[_]): Document =
    declarantDetailsPage(Mode.Normal, form)(request, messages)

  "Declarant Details View on empty page" should {

    "have correct message keys" in {
      messages must haveTranslationFor("declaration.declarant.title")
      messages must haveTranslationFor("declaration.eori.error.format")
      messages must haveTranslationFor("declaration.eori.empty")
    }
  }

  "Declarant Details View on empty page" should {

    onEveryDeclarationJourney() { implicit request =>
      "display page title" in {

        createView(form())
          .getElementsByTag("h1") must containMessageForElements("declaration.declarant.title", request.eori)
      }

      "display radio button with Yes option" in {

        val view = createView(form())
        view.getElementById("code_yes").attr("value") mustBe YesNoAnswers.yes
        view.getElementsByAttributeValue("for", "code_yes") must containMessageForElements("site.yes")
      }
      "display radio button with No option" in {

        val view = createView(form())
        view.getElementById("code_no").attr("value") mustBe YesNoAnswers.no
        view.getElementsByAttributeValue("for", "code_no") must containMessageForElements("site.no")
      }

      "display 'Save and continue' button on page" in {

        val saveButton = createView(form()).getElementById("submit")
        saveButton must containMessage(saveAndContinueCaption)
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display section header" in {

        createView(form()).getElementById("section-header") must containMessage("declaration.section.1")
      }

      "display 'Back' button that links to 'Additional Declaration Type' page" in {

        val view = declarantDetailsPage(Mode.Normal, form())(request, messages)
        val backButton = view.getElementById("back-link")

        backButton must containMessage(backCaption)
        backButton must haveHref(routes.AdditionalDeclarationTypeController.displayPage().url)
      }

      "not display 'Save and return' button on page" in {

        createView(form()).getElementById("submit_and_return") mustBe null
      }
    }

    onClearance { implicit request =>
      "display section header" in {

        createView(form()).getElementById("section-header") must containMessage("declaration.section.2")
      }

      "display 'Back' button that links to 'Entry into Declarant's Records' page" in {

        val view = declarantDetailsPage(Mode.Normal, form())(request, messages)
        val backButton = view.getElementById("back-link")

        backButton must containMessage(backCaption)
        backButton must haveHref(routes.EntryIntoDeclarantsRecordsController.displayPage().url)
      }

      "display 'Save and return' button on page" in {

        val saveButton = createView(form()).getElementById("submit_and_return")
        saveButton must containMessage(saveAndReturnCaption)
        saveButton.attr("name") mustBe SaveAndReturn.toString
      }
    }
  }

  "Declarant Details View with invalid input" should {

    onEveryDeclarationJourney() { implicit request =>
      "display error when answer is empty" in {

        val view = createView(form().fillAndValidate(DeclarantEoriConfirmation("")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#code_yes")

        view must containErrorElementWithMessageKey("declaration.declarant.error")
      }

      "display error when EORI is provided, but is incorrect" in {

        val view = createView(
          form()
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

        val form = DeclarantEoriConfirmation.form().fill(DeclarantEoriConfirmation(YesNoAnswers.yes))
        val view = createView(form)

        view.getElementById("code_yes").attr("value") mustBe YesNoAnswers.yes
        view.getElementById("code_yes").attr("value") mustBe YesNoAnswers.yes
      }
    }
  }
}
