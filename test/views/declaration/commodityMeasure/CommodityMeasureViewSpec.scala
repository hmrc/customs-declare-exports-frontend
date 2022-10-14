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

package views.declaration.commodityMeasure

import base.Injector
import controllers.declaration.routes.{CommodityDetailsController, PackageInformationSummaryController, UNDangerousGoodsCodeController}
import forms.declaration.IsExs
import forms.declaration.commodityMeasure.CommodityMeasure
import forms.declaration.commodityMeasure.CommodityMeasure.form
import models.DeclarationType._
import models.Mode
import models.Mode.Normal
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.api.data.Form
import play.api.mvc.Call
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.commodityMeasure.commodity_measure
import views.tags.ViewTest

@ViewTest
class CommodityMeasureViewSpec extends PageWithButtonsSpec with Injector {

  val page = instanceOf[commodity_measure]

  override val typeAndViewInstance = (STANDARD, page(Normal, itemId, form)(_, _))

  def createView(frm: Form[CommodityMeasure] = form, mode: Mode = Normal)(implicit request: JourneyRequest[_]): Document =
    page(mode, itemId, frm)(request, messages)

  "Commodity Measure View on empty page" should {
    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1").text mustBe messages("declaration.commodityMeasure.title")
      }

      "display section header" in {
        view.getElementById("section-header").text mustBe messages("declaration.section.5")
      }

      "display empty input with label for gross mass" in {
        val label = view.getElementsByTag("label").get(0)
        label.getElementsByAttributeValue("for", "grossMass").size mustBe 1
        label.text mustBe messages("declaration.commodityMeasure.grossMass.label")

        val hint = s"${messages("declaration.commodityMeasure.grossMass.hint")}${messages("declaration.commodityMeasure.units.hint")}"
        view.getElementsByClass("govuk-hint").get(0).text mustBe hint

        view.getElementById("grossMass").attr("value") mustBe empty
      }

      "display empty input with label for net mass" in {
        val label = view.getElementsByTag("label").get(1)
        label.getElementsByAttributeValue("for", "netMass").size mustBe 1
        label.text mustBe messages("declaration.commodityMeasure.netMass.label")

        val hint = s"${messages("declaration.commodityMeasure.netMass.hint")}${messages("declaration.commodityMeasure.units.hint")}"
        view.getElementsByClass("govuk-hint").get(2).text mustBe hint

        view.getElementById("netMass").attr("value") mustBe empty
      }

      val createViewWithMode: Mode => Document = mode => createView(mode = mode)
      checkAllSaveButtonsAreDisplayed(createViewWithMode)
    }
  }

  "Commodity Measure with invalid input" should {
    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { implicit request =>
      "display error when nothing is entered" in {
        val view = createView(form.bind(Map("grossMass" -> "", "netMass" -> "")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#netMass")
        view must containErrorElementWithTagAndHref("a", "#grossMass")

        view must containErrorElementWithMessageKey("declaration.commodityMeasure.netMass.empty")
        view must containErrorElementWithMessageKey("declaration.commodityMeasure.grossMass.empty")
      }

      "display error when net mass is empty" in {
        val view = createView(form.bind(Map("grossMass" -> "10.00", "netMass" -> "")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#netMass")
        view must containErrorElementWithMessageKey("declaration.commodityMeasure.netMass.empty")
      }

      "display error when net mass is incorrect" in {
        val view = createView(form.bind(Map("grossMass" -> "20.99", "netMass" -> "10.0055345")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#netMass")
        view must containErrorElementWithMessageKey("declaration.commodityMeasure.netMass.error")
      }

      "display error when gross mass is empty" in {
        val view = createView(form.bind(Map("grossMass" -> "", "netMass" -> "10.00")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#grossMass")
        view must containErrorElementWithMessageKey("declaration.commodityMeasure.grossMass.empty")
      }

      "display error when gross mass is incorrect" in {
        val view = createView(form.bind(Map("grossMass" -> "5.00234ff", "netMass" -> "100.100")))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#grossMass")

        view must containErrorElementWithMessageKey("declaration.commodityMeasure.grossMass.error")
      }
    }
  }

  "Commodity Measure View when filled" should {
    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { implicit request =>
      "display data in net mass input" in {
        val form = CommodityMeasure.form.fill(CommodityMeasure(Some(""), Some("123")))
        val view = createView(form)

        view.getElementById("netMass").attr("value") mustBe "123"
      }

      "display data in gross mass input" in {
        val form = CommodityMeasure.form.fill(CommodityMeasure(Some("123"), Some("")))
        val view = createView(form)

        view.getElementById("grossMass").attr("value") mustBe "123"
      }

      "display every input filled" in {
        val form = CommodityMeasure.form.fill(CommodityMeasure(Some("123"), Some("123")))
        val view = createView(form)

        view.getElementById("netMass").attr("value") mustBe "123"
        view.getElementById("grossMass").attr("value") mustBe "123"
      }
    }
  }

  "Commodity Measure back links" should {

    onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
      "display 'Back' button that links to 'Package Information' page" in {
        val backButton = createView().getElementById("back-link")

        backButton must containMessage("site.backToPreviousQuestion")
        backButton must haveHref(PackageInformationSummaryController.displayPage(Normal, itemId).url)
      }
    }

    onClearance { implicit request =>
      def viewHasBackLinkForProcedureCodeAndExsStatus(procedureCode: String, exsStatus: String, call: Call): Assertion = {
        val item = anItem(withItemId(itemId), withProcedureCodes(Some(procedureCode)))
        val declaration = aDeclarationAfter(request.cacheModel, withIsExs(IsExs(exsStatus)), withItem(item))

        val backButton = createView()(journeyRequest(declaration)).getElementById("back-link")

        backButton must containMessage("site.backToPreviousQuestion")
        backButton must haveHref(call.url)
      }

      "display 'Back' button that links to 'Commodity Details' " when {
        "procedure code was '0019' and is EXS was 'No" in {
          viewHasBackLinkForProcedureCodeAndExsStatus("0019", "No", CommodityDetailsController.displayPage(Normal, itemId))
        }
      }

      "display 'Back' button that links to 'UN Dangerous Goods Code' " when {
        "procedure code was '0019' and is EXS was 'Yes" in {
          viewHasBackLinkForProcedureCodeAndExsStatus("0019", "Yes", UNDangerousGoodsCodeController.displayPage(Normal, itemId))
        }
      }

      "display 'Back' button that links to 'Package Information' " when {
        "procedure code was '1234'" in {
          viewHasBackLinkForProcedureCodeAndExsStatus("1234", "ANY", PackageInformationSummaryController.displayPage(Normal, itemId))
        }
      }
    }
  }
}
