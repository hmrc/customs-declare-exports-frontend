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

import base.Injector
import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.DeclarantIsExporter
import helpers.views.declaration.CommonMessages
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.declarant_exporter
import views.tags.ViewTest

@ViewTest
class DeclarantExporterViewSpec extends UnitViewSpec with ExportsTestData with CommonMessages with Stubs with Injector {

  private val declarantExporterPage = instanceOf[declarant_exporter]
  private def createView(form: Form[DeclarantIsExporter] = DeclarantIsExporter.form()): Document =
    declarantExporterPage(Mode.Normal, form)(journeyRequest(), messages)

  "Declarant Exporter View on empty page" should {

    "have correct message keys" in {

      val messages = instanceOf[MessagesApi].preferred(request)

      messages must haveTranslationFor("declaration.declarant.exporter.title")
      messages must haveTranslationFor("declaration.summary.parties.header")
      messages must haveTranslationFor("declaration.declarant.exporter.answer.yes")
      messages must haveTranslationFor("declaration.declarant.exporter.answer.no")
      messages must haveTranslationFor("declaration.declarant.exporter.error")
      messages must haveTranslationFor("declaration.declarant.exporter.help-item")
    }
  }

  "Declarant Exporter View on empty page" should {

    onEveryDeclarationJourney() { implicit request =>
      "display page title" in {

        createView().getElementsByTag("h1").text() mustBe messages("declaration.declarant.exporter.title")
      }

      "display section header" in {

        createView().getElementById("section-header").text() must include(messages("declaration.summary.parties.header"))
      }

      "display radio button with Yes option" in {
        val view = createView()
        view.getElementById("answer_yes").attr("value") mustBe YesNoAnswers.yes
        view.getElementsByAttributeValue("for", "answer_yes").text() mustBe "declaration.declarant.exporter.answer.yes"
      }
      "display radio button with No option" in {
        val view = createView()
        view.getElementById("answer_no").attr("value") mustBe YesNoAnswers.no
        view.getElementsByAttributeValue("for", "answer_no").text() mustBe "declaration.declarant.exporter.answer.no"
      }

      "display 'Back' button that links to 'Declarant Details' page" in {

        val backButton = createView().getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton must haveHref(routes.DeclarantDetailsController.displayPage().url)
      }

      "display 'Save and continue' button on page" in {
        val saveButton = createView().getElementById("submit")
        saveButton.text() mustBe messages(saveAndContinueCaption)
      }

      "display 'Save and return' button on page" in {
        val saveButton = createView().getElementById("submit_and_return")
        saveButton.text() mustBe messages(saveAndReturnCaption)
        saveButton.attr("name") mustBe SaveAndReturn.toString
      }
    }
  }

  "Declarant Exporter View with invalid input" should {

    onEveryDeclarationJourney() { implicit request =>
      "display error when answer is empty" in {

        val view = createView(DeclarantIsExporter.form().fillAndValidate(DeclarantIsExporter("")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#answer")

        view.getElementsByClass("govuk-error-message").text() contains messages("declaration.declarant.eori.empty")
      }

      "display error when EORI is provided, but is incorrect" in {

        val view = createView(
          DeclarantIsExporter
            .form()
            .fillAndValidate(DeclarantIsExporter("wrong"))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#answer")

        view.getElementsByClass("govuk-error-message").text() contains messages("declaration.declarant.eori.error.format")
      }
    }

  }

  "Declarant Exporter View when filled" should {

    onEveryDeclarationJourney() { implicit request =>
      "display answer input" in {

        val form = DeclarantIsExporter.form().fill(DeclarantIsExporter(YesNoAnswers.yes))
        val view = createView(form)

        view.getElementById("answer_yes") must beSelected
      }
    }
  }
}
