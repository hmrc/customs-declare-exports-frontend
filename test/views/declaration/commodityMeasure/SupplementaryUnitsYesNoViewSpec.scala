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

package views.declaration.commodityMeasure

import base.Injector
import config.AppConfig
import controllers.declaration.routes.CommodityMeasureController
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.CommodityDetails
import forms.declaration.commodityMeasure.SupplementaryUnits
import forms.declaration.commodityMeasure.SupplementaryUnits.{hasSupplementaryUnits, supplementaryUnits}
import models.DeclarationType.{STANDARD, SUPPLEMENTARY}
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.commodityMeasure.supplementary_units_yes_no
import views.tags.ViewTest

@ViewTest
class SupplementaryUnitsYesNoViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val appConfig = instanceOf[AppConfig]

  private val page = instanceOf[supplementary_units_yes_no]

  private val itemId = "item1"
  private val yesNoPage = true

  private def createView(form: Form[SupplementaryUnits] = SupplementaryUnits.form(yesNoPage))(implicit request: JourneyRequest[_]): Document =
    page(Mode.Normal, itemId, form)(request, messages)

  "SupplementaryUnitsYesNo View" should {
    onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
      val view = createView()

      "display 'Back' button that links to 'Commodity Details' page" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage("site.back")
        backButton must haveHref(CommodityMeasureController.displayPage(Mode.Normal, itemId))
      }

      "display page title" in {
        view.getElementsByTag("h1").text mustBe messages("declaration.supplementaryUnits.yesNo.title")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.5")
      }

      "display body text when a commodity code has been selected" in {
        val commodityCode = "4602191000"
        val item = anItem(withItemId(itemId), withCommodityDetails(CommodityDetails(Some(commodityCode), None)))
        val view = createView()(journeyRequest(aDeclarationAfter(request.cacheModel, withItem(item))))

        val body = view.getElementsByClass("govuk-body").get(0)

        val linkWithCommodityCode = messages("declaration.supplementaryUnits.yesNo.body.link.1", commodityCode)
        val text = messages("declaration.supplementaryUnits.yesNo.body", linkWithCommodityCode)
        body.text mustBe text

        val href = appConfig.commodityCodeTariffPageUrl.replace(CommodityDetails.placeholder, commodityCode)
        body.child(0) must haveHref(href)
      }

      "display body text when no commodity code has been selected" in {
        val body = view.getElementsByClass("govuk-body").get(0)

        val linkWithoutCommodityCode = messages("declaration.supplementaryUnits.yesNo.body.link.2")
        val text = messages("declaration.supplementaryUnits.yesNo.body", linkWithoutCommodityCode)
        body.text mustBe text

        body.child(0) must haveHref(appConfig.tradeTariffSections)
      }

      "display radio button with Yes option" in {
        val label = view.getElementsByTag("label").get(0)
        label.attr("for") mustBe "Yes"
        label.text mustBe messages("site.yes")

        val radio = view.getElementById("Yes")
        radio.attr("type") mustBe "radio"
        radio.attr("name") mustBe hasSupplementaryUnits
        radio.attr("value") mustBe YesNoAnswers.yes
      }

      "include a 'supplementaryUnits' field" in {
        val label = view.getElementsByTag("label").get(1)
        label.attr("for") mustBe supplementaryUnits
        label.text mustBe messages("declaration.supplementaryUnits.quantity.label")

        val hint = view.getElementsByClass("govuk-hint").get(0)
        hint.text mustBe messages("declaration.supplementaryUnits.hint")

        view.getElementById(supplementaryUnits).tag.toString mustBe "input"
      }

      "display radio button with No option" in {
        val label = view.getElementsByTag("label").get(2)
        label.attr("for") mustBe "No"
        label.text mustBe messages("site.no")

        val radio = view.getElementById("No")
        radio.attr("type") mustBe "radio"
        radio.attr("name") mustBe hasSupplementaryUnits
        radio.attr("value") mustBe YesNoAnswers.no
      }

      "display the expected tariff details" in {
        val tariffTitle = view.getElementsByClass("govuk-details__summary-text")
        tariffTitle.first must containMessage("tariff.expander.title.common")

        val tariffDetails = view.getElementsByClass("govuk-details__text").first
        removeBlanksIfAnyBeforeDot(tariffDetails.text) mustBe messages(
          "tariff.declaration.item.supplementaryUnits.common.text",
          messages("tariff.declaration.item.supplementaryUnits.common.linkText.0")
        )
      }

      "display 'Save and continue' button on page" in {
        val saveButton = view.getElementById("submit")
        saveButton must containMessage("site.save_and_continue")
      }

      "display 'Save and return' button on page" in {
        val saveAndReturnButton = view.getElementById("submit_and_return")
        saveAndReturnButton must containMessage("site.save_and_come_back_later")
      }

      "not display any error when the value entered in the 'supplementaryUnits' field is valid" in {
        val view = createView(SupplementaryUnits.form(yesNoPage).fillAndValidate(SupplementaryUnits(Some("100"))))
        view mustNot haveGovukGlobalErrorSummary
      }

      "display an error when the value entered in the 'supplementaryUnits' field is invalid" in {
        val view = createView(SupplementaryUnits.form(yesNoPage).fillAndValidate(SupplementaryUnits(Some("ABC"))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$supplementaryUnits")
        view must containErrorElementWithMessageKey("declaration.supplementaryUnits.quantity.error")
      }

      "display an error when the value entered in the 'supplementaryUnits' field consists of zeroes only" in {
        val view = createView(SupplementaryUnits.form(yesNoPage).fillAndValidate(SupplementaryUnits(Some("0000"))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$supplementaryUnits")
        view must containErrorElementWithMessageKey("declaration.supplementaryUnits.quantity.empty")
      }

      "display an error when the value entered in the 'supplementaryUnits' field is too long" in {
        val view = createView(SupplementaryUnits.form(yesNoPage).fillAndValidate(SupplementaryUnits(Some("12345678901234567"))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$supplementaryUnits")
        view must containErrorElementWithMessageKey("declaration.supplementaryUnits.quantity.length")
      }

      "display error when the 'supplementaryUnits' field is left empty" in {
        val view = createView(SupplementaryUnits.form(yesNoPage).fillAndValidate(SupplementaryUnits(Some(""))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", s"#$supplementaryUnits")
        view must containErrorElementWithMessageKey("declaration.supplementaryUnits.quantity.empty")
      }
    }
  }
}
