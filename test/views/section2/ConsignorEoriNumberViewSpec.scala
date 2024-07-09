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

package views.section2

import base.Injector
import controllers.section2.routes.IsExsController
import forms.common.Eori
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section2.consignor.ConsignorEoriNumber.form
import forms.section2.consignor.ConsignorEoriNumber
import models.DeclarationType.CLEARANCE
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestHelper
import views.components.gds.Styles
import views.html.section2.consignor_eori_number
import views.common.PageWithButtonsSpec
import views.tags.ViewTest

@ViewTest
class ConsignorEoriNumberViewSpec extends PageWithButtonsSpec with ExportsTestHelper with Injector {

  private val page: consignor_eori_number = instanceOf[consignor_eori_number]

  override val typeAndViewInstance = (CLEARANCE, page(form)(_, _))

  private def createView(frm: Form[ConsignorEoriNumber] = form)(implicit request: JourneyRequest[_]): Document =
    page(frm)(request, messages)

  "Consignor Eori Number View" should {

    onJourney(CLEARANCE) { implicit request =>
      val view = createView()

      "display answer input" in {
        val view = createView(form.fill(ConsignorEoriNumber(Some(Eori("GB123456789")), YesNoAnswers.yes)))

        view
          .getElementById("Yes")
          .getElementsByAttribute("checked")
          .attr("value") mustBe YesNoAnswers.yes
      }

      "have proper messages for labels" in {
        messages must haveTranslationFor("declaration.consignorEori.title")
        messages must haveTranslationFor("declaration.consignorEori.eori.label")
        messages must haveTranslationFor("declaration.consignorEori.hasEori.empty")
      }

      "display page title" in {
        view.getElementsByClass(Styles.gdsPageLegend) must containMessageForElements("declaration.consignorEori.title")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.2")
      }

      "display eori question" in {
        view.getElementsByClass("govuk-label") must containMessageForElements("declaration.consignorEori.eori.label")
        view.getElementById("eori").attr("value") mustBe empty
      }

      "display 'Back' button that links to 'Exporter Details' page" in {
        val backButton = view.getElementById("back-link")

        backButton must containMessage("site.backToPreviousQuestion")
        backButton.getElementById("back-link") must haveHref(IsExsController.displayPage)
      }

      checkAllSaveButtonsAreDisplayed(createView())

      "handle invalid input" should {

        "display errors when all inputs are incorrect" in {
          val data = ConsignorEoriNumber(Some(Eori("123456789")), YesNoAnswers.yes)
          val view = createView(form.fillAndValidate(data))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#eori")
          view must containErrorElementWithMessageKey("declaration.eori.error.format")
        }

        "display errors when eori contains special characters" in {
          val data = ConsignorEoriNumber(eori = Some(Eori("12#$%^78")), hasEori = YesNoAnswers.yes)
          val view = createView(form.fillAndValidate(data))

          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", "#eori")
          view must containErrorElementWithMessageKey("declaration.eori.error.format")
        }
      }
    }
  }
}
