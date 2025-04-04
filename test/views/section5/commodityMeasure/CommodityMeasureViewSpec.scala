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

package views.section5.commodityMeasure

import base.{Injector, MockAuthAction}
import controllers.section5.routes.{CommodityDetailsController, PackageInformationSummaryController, UNDangerousGoodsCodeController}
import forms.section2.IsExs
import forms.section5.commodityMeasure.CommodityMeasure
import forms.section5.commodityMeasure.CommodityMeasure.form
import models.DeclarationType._
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.api.data.Form
import play.api.mvc.Call
import views.common.PageWithButtonsSpec
import views.html.section5.commodityMeasure.commodity_measure
import views.tags.ViewTest

@ViewTest
class CommodityMeasureViewSpec extends PageWithButtonsSpec with Injector with MockAuthAction {

  private val page = instanceOf[commodity_measure]

  override val typeAndViewInstance = (STANDARD, page(itemId, form(STANDARD))(_, _))

  private val detailsPrefix = "declaration.commodityMeasure.expander.paragraph"
  private val tariffPrefix = "tariff.declaration.item.commodityMeasure"

  def createView(form: Form[CommodityMeasure])(implicit request: JourneyRequest[_]): Document =
    page(itemId, form)(request, messages)

  "Commodity Measure View" should {
    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { implicit request =>
      val view = createView(form(request.declarationType))

      "display section header" in {
        view.getElementById("section-header").text mustBe messages("declaration.section.5")
      }

      "display page title" in {
        view.getElementsByTag("h1").text mustBe messages("declaration.commodityMeasure.title")
      }

      "display the inset text after the title" in {
        view.getElementsByClass("govuk-inset-text").text mustBe messages("declaration.commodityMeasure.inset.text")
      }

      "display empty input with label for net mass" in {
        val label = view.getElementsByTag("label").get(0)
        label.getElementsByAttributeValue("for", "netMass").size mustBe 1
        label.text mustBe messages("declaration.commodityMeasure.netMass.label")

        val hint = s"${messages("declaration.commodityMeasure.netMass.hint")}${messages("declaration.commodityMeasure.units.hint")}"
        view.getElementsByClass("govuk-hint").get(0).text mustBe hint

        view.getElementById("netMass").attr("value") mustBe empty
      }

      "display empty input with label for gross mass" in {
        val label = view.getElementsByTag("label").get(1)
        label.getElementsByAttributeValue("for", "grossMass").size mustBe 1
        label.text mustBe messages("declaration.commodityMeasure.grossMass.label")

        val hint = s"${messages("declaration.commodityMeasure.grossMass.hint")}${messages("declaration.commodityMeasure.units.hint")}"
        view.getElementsByClass("govuk-hint").get(2).text mustBe hint

        view.getElementById("grossMass").attr("value") mustBe empty
      }

      "display a details expander" in {
        val view = createView(form(request.declarationType))
        view.getElementsByClass("govuk-details__summary").first.text mustBe messages("declaration.commodityMeasure.expander.title")
        val details = view.getElementsByClass("govuk-details__text").first

        val expectedText =
          s"""${messages(s"$detailsPrefix.1")}
             |${messages(s"$detailsPrefix.2")}
             |${messages(s"$detailsPrefix.3")}
             |${messages(s"$detailsPrefix.4")}
          """.stripMargin.replace('\n', ' ').trim

        removeBlanksIfAnyBeforeDot(details.text) mustBe expectedText
      }

      checkAllSaveButtonsAreDisplayed(createView(form(request.declarationType)))

      "display error when net mass is incorrect" in {
        val view = createView(form(request.declarationType).bind(Map("grossMass" -> "20.99", "netMass" -> "10.5345")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#netMass")
        view must containErrorElementWithMessageKey("declaration.commodityMeasure.error")
      }

      "display error when gross mass is incorrect" in {
        val view = createView(form(request.declarationType).bind(Map("grossMass" -> "5.0ff", "netMass" -> "100.100")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#grossMass")

        view must containErrorElementWithMessageKey("declaration.commodityMeasure.error")
      }

      "display data in net mass input" in {
        val form = CommodityMeasure.form(request.declarationType).fill(CommodityMeasure(Some(""), Some("123")))
        val view = createView(form)

        view.getElementById("netMass").attr("value") mustBe "123"
      }

      "display data in gross mass input" in {
        val form = CommodityMeasure.form(request.declarationType).fill(CommodityMeasure(Some("123"), Some("")))
        val view = createView(form)

        view.getElementById("grossMass").attr("value") mustBe "123"
      }

      "display every input filled" in {
        val form = CommodityMeasure.form(request.declarationType).fill(CommodityMeasure(Some("123"), Some("123")))
        val view = createView(form)

        view.getElementById("netMass").attr("value") mustBe "123"
        view.getElementById("grossMass").attr("value") mustBe "123"
      }
    }
  }

  "Commodity Measure view" should {

    onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
      val view = createView(form(request.declarationType))

      "display 'Back' button that links to 'Package Information' page" in {
        val backButton = view.getElementById("back-link")

        backButton must containMessage("site.backToPreviousQuestion")
        backButton must haveHref(PackageInformationSummaryController.displayPage(itemId).url)
      }

      "display the expected tariff expander" in {
        view.getElementsByClass("govuk-details__summary").last.text mustBe messages("tariff.expander.title.common")
        val tariffText = view.getElementsByClass("govuk-details__text").last

        val expectedText =
          s"""${messages(s"$tariffPrefix.1.common.text")}
             |${messages(s"$tariffPrefix.2.common.text")}
             |${messages(
            s"$tariffPrefix.3.common.text",
            messages(s"$tariffPrefix.3.common.linkText.0"),
            messages(s"$tariffPrefix.3.common.linkText.1")
          )}
          """.stripMargin.replace('\n', ' ').trim

        removeBlanksIfAnyBeforeDot(tariffText.text) mustBe expectedText

        val links = tariffText.getElementsByClass("govuk-link")
        links.get(0) must haveHref(appConfig.tariffGuideUrl(s"urls.$tariffPrefix.3.common.0"))
      }
    }

    onClearance { implicit request =>
      def viewHasBackLinkForProcedureCodeAndExsStatus(procedureCode: String, exsStatus: String, call: Call): Assertion = {
        val item = anItem(withItemId(itemId), withProcedureCodes(Some(procedureCode)))
        val declaration = aDeclarationAfter(request.cacheModel, withIsExs(IsExs(exsStatus)), withItem(item))

        val backButton = createView(form(request.declarationType))(journeyRequest(declaration)).getElementById("back-link")

        backButton must containMessage("site.backToPreviousQuestion")
        backButton must haveHref(call.url)
      }

      "display 'Back' button that links to 'Commodity Details' " when {
        "procedure code was '0019' and is EXS was 'No" in {
          viewHasBackLinkForProcedureCodeAndExsStatus("0019", "No", CommodityDetailsController.displayPage(itemId))
        }
      }

      "display 'Back' button that links to 'UN Dangerous Goods Code' " when {
        "procedure code was '0019' and is EXS was 'Yes" in {
          viewHasBackLinkForProcedureCodeAndExsStatus("0019", "Yes", UNDangerousGoodsCodeController.displayPage(itemId))
        }
      }

      "display 'Back' button that links to 'Package Information' " when {
        "procedure code was '1234'" in {
          viewHasBackLinkForProcedureCodeAndExsStatus("1234", "ANY", PackageInformationSummaryController.displayPage(itemId))
        }
      }

      "display the expected tariff expander" in {
        val view = createView(form(request.declarationType))
        view.getElementsByClass("govuk-details__summary").last.text mustBe messages("tariff.expander.title.clearance")
        val tariffText = view.getElementsByClass("govuk-details__text").last

        val expectedText =
          messages(s"$tariffPrefix.clearance.text", messages(s"$tariffPrefix.clearance.linkText.0"), messages(s"$tariffPrefix.clearance.linkText.1"))

        removeBlanksIfAnyBeforeDot(tariffText.text) mustBe expectedText

        val links = tariffText.getElementsByClass("govuk-link")
        links.get(0) must haveHref(appConfig.tariffGuideUrl(s"urls.$tariffPrefix.clearance.0"))
        links.get(1) must haveHref(appConfig.tariffGuideUrl(s"urls.$tariffPrefix.clearance.1"))
      }
    }
  }
}
