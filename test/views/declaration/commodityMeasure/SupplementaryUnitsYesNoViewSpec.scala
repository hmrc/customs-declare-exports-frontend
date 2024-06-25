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

package views.declaration.commodityMeasure

import base.Injector
import config.AppConfig
import controllers.declaration.routes.CommodityMeasureController
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.CommodityDetails
import forms.declaration.commodityMeasure.SupplementaryUnits
import forms.declaration.commodityMeasure.SupplementaryUnits.{form, hasSupplementaryUnits, supplementaryUnits}
import models.DeclarationType.{DeclarationType, STANDARD, SUPPLEMENTARY}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import views.declaration.spec.UnitViewSpec
import views.html.declaration.commodityMeasure.supplementary_units_yes_no
import views.tags.ViewTest

@ViewTest
class SupplementaryUnitsYesNoViewSpec extends UnitViewSpec with Injector {

  private val appConfig = instanceOf[AppConfig]

  private def makeRequest(declarationType: DeclarationType, maybeCommodityCode: Option[String]): JourneyRequest[_] =
    maybeCommodityCode.fold(journeyRequest()) { commodityCode =>
      val item = anItem(withItemId(itemId), withCommodityDetails(CommodityDetails(Some(commodityCode), None)))
      withRequestOfType(declarationType, withItem(item))
    }

  private val page = instanceOf[supplementary_units_yes_no]

  private def createView(frm: Form[SupplementaryUnits] = form)(implicit request: JourneyRequest[_]): Document =
    page(itemId, frm)(request, messages)

  "SupplementaryUnitsYesNo View" when {

    onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
      val view = createView()

      s"the declarationType is ${request.declarationType}" should {

        "display 'Back' button that links to 'Commodity Details' page" in {
          val backButton = view.getElementById("back-link")
          backButton must containMessage("site.backToPreviousQuestion")
          backButton must haveHref(CommodityMeasureController.displayPage(itemId))
        }

        "display section header" in {
          view.getElementById("section-header") must containMessage("declaration.section.5")
        }

        "display the expected notification banner" in {
          val banner = view.getElementsByClass("govuk-notification-banner").get(0)

          val title = banner.getElementsByClass("govuk-notification-banner__title").text
          title mustBe messages("declaration.supplementaryUnits.notification.title")

          val content = banner.getElementsByClass("govuk-notification-banner__content").get(0)
          content.text mustBe messages("declaration.supplementaryUnits.notification.body")
        }

        "display page title" in {
          view.getElementsByTag("h1").text mustBe messages("declaration.supplementaryUnits.yesNo.title")
        }

        "display the expected body text when a commodity code of 10-digits has been entered" in {
          val commodityCode = "4602191000"
          val view = createView()(makeRequest(request.declarationType, Some(commodityCode)))

          val body = view.getElementsByClass("govuk-body").get(1)

          val expectedLinkText = messages("declaration.supplementaryUnits.yesNo.body.link.1", commodityCode)
          val expectedHref = appConfig.suppUnitsCommodityCodeTariffPageUrl.replace(CommodityDetails.placeholder, commodityCode)

          body.text mustBe messages("declaration.supplementaryUnits.yesNo.body", expectedLinkText)
          body.child(0) must haveHref(expectedHref)
        }

        "display the expected body text when a commodity code of 8-digits has been entered" in {
          val commodityCode = "46021910"
          val view = createView()(makeRequest(request.declarationType, Some(commodityCode)))

          val body = view.getElementsByClass("govuk-body").get(1)

          val expectedLinkText = messages("declaration.supplementaryUnits.yesNo.body.link.1", commodityCode)
          val expectedHref = appConfig.suppUnitsCommodityCodeTariffPageUrl.replace(CommodityDetails.placeholder, s"${commodityCode}00")

          body.text mustBe messages("declaration.supplementaryUnits.yesNo.body", expectedLinkText)
          body.child(0) must haveHref(expectedHref)
        }

        "display the expected body text when no commodity code has been entered" in {
          val body = view.getElementsByClass("govuk-body").get(1)

          val expectedLinkText = messages("declaration.supplementaryUnits.yesNo.body.link.2")

          body.text mustBe messages("declaration.supplementaryUnits.yesNo.body", expectedLinkText)
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
            "tariff.declaration.text",
            messages("tariff.declaration.item.supplementaryUnits.common.linkText.0")
          )
        }

        checkAllSaveButtonsAreDisplayed(createView())

        "not display any error when the value entered in the 'supplementaryUnits' field is valid" in {
          val view = createView(form.fillAndValidate(SupplementaryUnits(Some("100"))))
          view mustNot haveGovukGlobalErrorSummary
        }

        "display an error when the value entered in the 'supplementaryUnits' field is invalid" in {
          val view = createView(form.fillAndValidate(SupplementaryUnits(Some("ABC"))))
          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#$supplementaryUnits")
          view must containErrorElementWithMessageKey("declaration.supplementaryUnits.quantity.error")
        }

        "display an error when the value entered in the 'supplementaryUnits' field consists of zeroes only" in {
          val view = createView(form.fillAndValidate(SupplementaryUnits(Some("0000"))))
          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#$supplementaryUnits")
          view must containErrorElementWithMessageKey("declaration.supplementaryUnits.quantity.empty")
        }

        "display an error when the value entered in the 'supplementaryUnits' field is too long" in {
          val view = createView(form.fillAndValidate(SupplementaryUnits(Some("12345678901234567"))))
          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#$supplementaryUnits")
          view must containErrorElementWithMessageKey("declaration.supplementaryUnits.quantity.length")
        }

        "display error when the 'supplementaryUnits' field is left empty" in {
          val view = createView(form.fillAndValidate(SupplementaryUnits(Some(""))))
          view must haveGovukGlobalErrorSummary
          view must containErrorElementWithTagAndHref("a", s"#$supplementaryUnits")
          view must containErrorElementWithMessageKey("declaration.supplementaryUnits.quantity.empty")
        }
      }
    }
  }
}
