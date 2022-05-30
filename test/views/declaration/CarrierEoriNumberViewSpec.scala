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
import controllers.declaration.routes.RepresentativeStatusController
import forms.common.Eori
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.carrier.CarrierEoriNumber
import forms.declaration.carrier.CarrierEoriNumber.form
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD}
import models.Mode
import models.Mode.Normal
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.carrier_eori_number
import views.tags.ViewTest

@ViewTest
class CarrierEoriNumberViewSpec extends UnitViewSpec with CommonMessages with ExportsTestData with Stubs with Injector {

  private val page: carrier_eori_number = instanceOf[carrier_eori_number]

  private def createView(form: Form[CarrierEoriNumber], mode: Mode = Mode.Normal)(implicit request: JourneyRequest[_]): Document =
    page(mode, form)(request, messages)

  "Carrier Eori Number View" should {

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
      val view = createView(form)

      "have proper messages for labels" in {
        messages must haveTranslationFor("declaration.carrierEori.hasEori.empty")
        messages must haveTranslationFor("tariff.declaration.locationOfGoods.clearance.text")
      }

      "display 'Back' button that links to 'Exporter Details' page" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage("site.back")
        backButton.getElementById("back-link") must haveHref(RepresentativeStatusController.displayPage(Normal))
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.2")
      }

      "display the expected page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.carrierEori.title")
      }

      "display the expected notification banner" in {
        val banner = view.getElementsByClass("govuk-notification-banner").get(0)

        val title = banner.getElementsByClass("govuk-notification-banner__title").text
        title mustBe messages("declaration.carrierEori.notification.title")

        val content = banner.getElementsByClass("govuk-notification-banner__content").get(0)
        content.text mustBe messages("declaration.carrierEori.notification.body")
      }

      "display inset text" in {
        val expectedInsetText = messages("declaration.carrierEori.inset.1")

        view.getElementsByClass("govuk-inset-text").get(0).text mustBe expectedInsetText
      }

      "display the expected body" in {
        val paragraphs = view.getElementsByClass("govuk-body")
        paragraphs.get(1).text mustBe messages(s"declaration.carrierEori.body.1")
        paragraphs.get(2).text mustBe messages(s"declaration.carrierEori.body.2")
        paragraphs.get(3).text mustBe messages(s"declaration.carrierEori.body.3")
      }

      "display answer input" in {
        val carrierEoriNumber = form.fill(CarrierEoriNumber(Some(Eori("GB123456789")), YesNoAnswers.yes))

        createView(form = carrierEoriNumber)
          .getElementById("Yes")
          .getElementsByAttribute("checked")
          .attr("value") mustBe YesNoAnswers.yes
      }

      "display eori question" in {
        view.getElementsByClass("govuk-label") must containMessageForElements("declaration.carrierEori.eori.label")
        view.getElementsByClass("govuk-hint").text mustBe messages("declaration.carrierEori.eori.hint")
        view.getElementById("eori").attr("value") mustBe empty
      }

      "display the expected tariff details" in {
        val declType = if (request.isType(CLEARANCE)) "clearance" else "common"

        val tariffTitle = view.getElementsByClass("govuk-details__summary-text")
        tariffTitle.text mustBe messages(s"tariff.expander.title.$declType")

        val tariffDetails = view.getElementsByClass("govuk-details__text").first

        val prefix = "tariff.declaration.carrierEoriNumber"
        val expectedText = messages(s"$prefix.$declType.text", messages(s"$prefix.$declType.linkText.0"))

        val actualText = removeBlanksIfAnyBeforeDot(tariffDetails.text)
        actualText mustBe removeLineBreakIfAny(expectedText)
      }

      val createViewWithMode: Mode => Document = mode => createView(form, mode)
      checkAllSaveButtonsAreDisplayed(createViewWithMode)

      "display errors when all inputs are incorrect" in {
        val data = CarrierEoriNumber(Some(Eori("123456789")), YesNoAnswers.yes)
        val form = CarrierEoriNumber.form.fillAndValidate(data)
        val view = createView(form = form)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#eori")
        view must containErrorElementWithMessageKey("declaration.eori.error.format")
      }

      "display errors when eori contains special characters" in {
        val data = CarrierEoriNumber(eori = Some(Eori("12#$%^78")), hasEori = YesNoAnswers.yes)
        val form = CarrierEoriNumber.form.fillAndValidate(data)
        val view = createView(form = form)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#eori")
        view must containErrorElementWithMessageKey("declaration.eori.error.format")
      }
    }
  }
}
