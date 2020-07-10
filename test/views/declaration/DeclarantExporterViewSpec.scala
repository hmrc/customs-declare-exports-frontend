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
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec2
import views.html.declaration.declarant_exporter
import views.tags.ViewTest

@ViewTest
class DeclarantExporterViewSpec extends UnitViewSpec2 with ExportsTestData with CommonMessages with Stubs with Injector {

  private val declarantExporterPage = instanceOf[declarant_exporter]
  private def createView(form: Form[DeclarantIsExporter] = DeclarantIsExporter.form())(implicit request: JourneyRequest[_]): Document =
    declarantExporterPage(Mode.Normal, form)(request, messages)

  "Declarant Exporter View on empty page" should {

    "have correct message keys" in {
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

        createView().getElementsByTag("h1") must containMessageForElements("declaration.declarant.exporter.title")
      }

      "display section header" in {

        createView().getElementById("section-header") must containMessage("supplementary.consignmentReferences.heading")
      }

      "display radio button with Yes option" in {
        val view = createView()
        view.getElementById("answer_yes").attr("value") mustBe YesNoAnswers.yes
        view.getElementsByAttributeValue("for", "answer_yes") must containMessageForElements("declaration.declarant.exporter.answer.yes")
      }
      "display radio button with No option" in {
        val view = createView()
        view.getElementById("answer_no").attr("value") mustBe YesNoAnswers.no
        view.getElementsByAttributeValue("for", "answer_no") must containMessageForElements("declaration.declarant.exporter.answer.no")
      }

      "display 'Back' button that links to 'Declarant Details' page" in {

        val backButton = createView().getElementById("back-link")

        backButton must containMessage(backCaption)
        backButton must haveHref(routes.DeclarantDetailsController.displayPage().url)
      }

      "display 'Save and continue' button on page" in {
        val saveButton = createView().getElementById("submit")
        saveButton must containMessage(saveAndContinueCaption)
      }

      "display 'Save and return' button on page" in {
        val saveButton = createView().getElementById("submit_and_return")
        saveButton must containMessage(saveAndReturnCaption)
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

        view must containErrorElementWithMessageKey("declaration.declarant.exporter.error")
      }

      "display error when EORI is provided, but is incorrect" in {

        val view = createView(
          DeclarantIsExporter
            .form()
            .fillAndValidate(DeclarantIsExporter("wrong"))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#answer")

        view must containErrorElementWithMessageKey("declaration.declarant.exporter.error")
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
