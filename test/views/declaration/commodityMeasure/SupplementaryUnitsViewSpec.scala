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
import controllers.declaration.routes.CommodityMeasureController
import forms.declaration.commodityMeasure.SupplementaryUnits
import forms.declaration.commodityMeasure.SupplementaryUnits.supplementaryUnits
import models.DeclarationType.{STANDARD, SUPPLEMENTARY}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.CommodityInfo
import services.cache.ExportsTestHelper
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.commodityMeasure.supplementary_units
import views.tags.ViewTest

@ViewTest
class SupplementaryUnitsViewSpec extends PageWithButtonsSpec with ExportsTestHelper with Injector {

  val yesNoPage = false
  val form = SupplementaryUnits.form(yesNoPage)

  val commodityInfo = CommodityInfo("2208303000", "number of items", "p/st")

  val page = instanceOf[supplementary_units]

  override val typeAndViewInstance = (STANDARD, page(itemId, form, commodityInfo)(_, _))

  def createView(frm: Form[SupplementaryUnits] = form)(implicit request: JourneyRequest[_]): Document =
    page(itemId, frm, commodityInfo)(request, messages)

  "SupplementaryUnits View" should {

    onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
      val view = createView()

      "display 'Back' button that links to 'Commodity Details' page" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage("site.backToPreviousQuestion")
        backButton must haveHref(CommodityMeasureController.displayPage(itemId))
      }

      "display page title" in {
        val expectedTitle = messages("declaration.supplementaryUnits.title", commodityInfo.description)
        view.getElementsByTag("h1").text mustBe expectedTitle
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.5")
      }

      "display the expected body text" in {
        val expectedBodyText = messages("declaration.supplementaryUnits.body", commodityInfo.code, commodityInfo.description, commodityInfo.units)
        view.getElementsByClass("govuk-body").get(0).text mustBe expectedBodyText
      }

      "display the expected hint text" in {
        view.getElementsByClass("govuk-hint").get(0).text mustBe messages("declaration.supplementaryUnits.hint")
      }

      "include a 'supplementaryUnits' field with suffix" in {
        val wrapperDiv = view.getElementsByClass("govuk-input__wrapper").get(0)
        wrapperDiv.childrenSize mustBe 2

        wrapperDiv.child(0).id mustBe supplementaryUnits
        wrapperDiv.child(0).tag.toString mustBe "input"

        val suffix = wrapperDiv.child(1)
        suffix.tag.toString mustBe "div"
        suffix.className mustBe "govuk-input__suffix"
        suffix.text mustBe commodityInfo.units
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
