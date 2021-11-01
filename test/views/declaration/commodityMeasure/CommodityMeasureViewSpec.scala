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
import forms.declaration.IsExs
import forms.declaration.commodityMeasure.CommodityMeasure
import models.DeclarationType._
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.Call
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.commodityMeasure.commodity_measure
import views.tags.ViewTest

@ViewTest
class CommodityMeasureViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  val itemId = "a7sc78"

  private val goodsMeasurePage = instanceOf[commodity_measure]
  private def createView(form: Option[Form[CommodityMeasure]] = None, commodityCode: Option[String] = None)(
    implicit request: JourneyRequest[_]
  ): Document =
    goodsMeasurePage(Mode.Normal, itemId, form.getOrElse(CommodityMeasure.form(request.declarationType)), commodityCode)(request, messages)

  "Commodity Measure" should {

    "have correct message keys" in {
      messages must haveTranslationFor("declaration.commodityMeasure.title")
      messages must haveTranslationFor("declaration.commodityMeasure.netMass")
      messages must haveTranslationFor("declaration.commodityMeasure.netMass.empty")
      messages must haveTranslationFor("declaration.commodityMeasure.netMass.error.format")
      messages must haveTranslationFor("declaration.commodityMeasure.grossMass")
      messages must haveTranslationFor("declaration.commodityMeasure.grossMass.empty")
      messages must haveTranslationFor("declaration.commodityMeasure.grossMass.error")
      messages must haveTranslationFor("declaration.commodityMeasure.global.addOne")
      messages must haveTranslationFor("declaration.commodityMeasure.supplementaryUnits")
      messages must haveTranslationFor("declaration.commodityMeasure.supplementaryUnits.item")
      messages must haveTranslationFor("declaration.commodityMeasure.supplementaryUnits.hint")
      messages must haveTranslationFor("declaration.commodityMeasure.supplementaryUnits.error")
      messages must haveTranslationFor("declaration.commodityMeasure.supplementaryUnitsNotRequired")
      messages must haveTranslationFor("declaration.commodityMeasure.expander.text")
      messages must haveTranslationFor("declaration.commodityMeasure.expander.withCommodityCode.link.text")
      messages must haveTranslationFor("declaration.commodityMeasure.expander.withoutCommodityCode.link.text")
    }
  }

  "Commodity Measure View on empty page" should {
    onEveryDeclarationJourney() { implicit request =>
      "display page title" in {

        createView().getElementById("title").text() mustBe messages("declaration.commodityMeasure.title")
      }

      "display section header" in {

        createView().getElementById("section-header").text() must include(messages("declaration.section.5"))
      }

      "display empty input with label for net mass" in {

        val view = createView()

        view.getElementsByAttributeValue("for", "netMass").text() mustBe messages("declaration.commodityMeasure.netMass")
        view
          .getElementById("netMass-hint")
          .text() mustBe s"${messages("declaration.commodityMeasure.units.hint")} ${messages("declaration.commodityMeasure.netMass.hint")}"
        view.getElementById("netMass").attr("value") mustBe empty
      }

      "display empty input with label for gross mass" in {

        val view = createView()

        view.getElementsByAttributeValue("for", "grossMass").text() mustBe messages("declaration.commodityMeasure.grossMass")
        view
          .getElementById("grossMass-hint")
          .text() mustBe s"${messages("declaration.commodityMeasure.units.hint")} ${messages("declaration.commodityMeasure.grossMass.hint")}"
        view.getElementById("grossMass").attr("value") mustBe empty
      }

      "display 'Save and continue' button on page" in {

        val saveButton = createView().select("#submit")
        saveButton.text() mustBe messages(saveAndContinueCaption)
      }
    }
  }

  "Commodity Measure View on empty page for supplementary units" should {
    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display empty input with label for supplementary units" in {

        val view = createView()

        view.getElementsByAttributeValue("for", "supplementaryUnits").text() mustBe messages("declaration.commodityMeasure.supplementaryUnits")
        view.getElementById("supplementaryUnits-hint").text() mustBe messages("declaration.commodityMeasure.supplementaryUnits.hint")
        view.getElementById("supplementaryUnits").attr("value") mustBe empty
      }

      "display unchecked supplementary units not required checkbox" in {
        val view = createView()

        view.getElementById("supplementaryUnitsNotRequired").attr("checked") mustBe empty
        view.getElementsByAttributeValue("for", "supplementaryUnitsNotRequired").text() mustBe messages(
          "declaration.commodityMeasure.supplementaryUnitsNotRequired"
        )
      }

      "display the expander" when {
        "commodityCode is present" in {
          val commodityCode = "4602191000"
          val hintElement = createView(commodityCode = Some(commodityCode)).getElementById("supplementaryUnits-readMore")

          hintElement must containHtml(messages("declaration.commodityMeasure.expander.title"))
          hintElement
            .getElementsByClass("govuk-hint")
            .get(0) must containText(messages("declaration.commodityMeasure.expander.withCommodityCode.link.text", commodityCode))
        }

        "commodityCode is missing" in {
          val hintElement = createView().getElementById("supplementaryUnits-readMore")

          hintElement must containHtml(messages("declaration.commodityMeasure.expander.title"))
          hintElement
            .getElementsByClass("govuk-hint")
            .get(0) must containText(messages("declaration.commodityMeasure.expander.withoutCommodityCode.link.text"))
        }
      }
    }
  }

  "Commodity Measure View on empty page for supplementary units on clearance request" should {
    onJourney(CLEARANCE) { implicit request =>
      "displaying page not include supplementary units form field" in {
        createView().getElementById("supplementaryUnits") mustBe (null)
      }

      "displaying page not include supplementary units not required form field" in {
        createView().getElementById("supplementaryUnitsNotRequired") mustBe (null)
      }
    }
  }

  "Commodity Measure with invalid input" should {
    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display error when nothing is entered" in {

        val view =
          createView(Some(CommodityMeasure.form(request.declarationType).bind(Map("supplementaryUnits" -> "", "grossMass" -> "", "netMass" -> ""))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#netMass")
        view must containErrorElementWithTagAndHref("a", "#grossMass")

        view must containErrorElementWithMessageKey("declaration.commodityMeasure.supplementaryUnitsNotRequired.error.neither")
        view must containErrorElementWithMessageKey("declaration.commodityMeasure.netMass.empty")
        view must containErrorElementWithMessageKey("declaration.commodityMeasure.grossMass.empty")
      }

      "display error when supplementary units are incorrect" in {

        val view = createView(
          Some(CommodityMeasure.form(request.declarationType).bind(Map("supplementaryUnits" -> "0.0", "grossMass" -> "", "netMass" -> "")))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#supplementaryUnits")

        view must containErrorElementWithMessageKey("declaration.commodityMeasure.supplementaryUnits.error")
      }

      "display error when supplementary units is empty and supplementary units not required is not checked" in {
        val view = createView(
          Some(
            CommodityMeasure
              .form(request.declarationType)
              .bind(Map("supplementaryUnits" -> "", "supplementaryUnitsNotRequired" -> "false", "grossMass" -> "", "netMass" -> ""))
          )
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#supplementaryUnitsNotRequired")

        view must containErrorElementWithMessageKey("declaration.commodityMeasure.supplementaryUnitsNotRequired.error.neither")
      }

      "display error when supplementary units is not empty and supplementary units not required is checked" in {
        val view = createView(
          Some(
            CommodityMeasure
              .form(request.declarationType)
              .bind(Map("supplementaryUnits" -> "123", "supplementaryUnitsNotRequired" -> "true", "grossMass" -> "", "netMass" -> ""))
          )
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#supplementaryUnitsNotRequired")

        view must containErrorElementWithMessageKey("declaration.commodityMeasure.supplementaryUnitsNotRequired.error.both")
      }

      "display error when net mass is empty" in {

        val view =
          createView(
            Some(CommodityMeasure.form(request.declarationType).bind(Map("supplementaryUnits" -> "99.99", "grossMass" -> "10.00", "netMass" -> "")))
          )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#netMass")

        view must containErrorElementWithMessageKey("declaration.commodityMeasure.netMass.empty")
      }

      "display error when net mass is incorrect" in {

        val view =
          createView(
            Some(
              CommodityMeasure
                .form(request.declarationType)
                .bind(Map("supplementaryUnits" -> "99.99", "grossMass" -> "20.99", "netMass" -> "10.0055345"))
            )
          )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#netMass")

        view must containErrorElementWithMessageKey("declaration.commodityMeasure.netMass.error.format")
      }

      "display error when gross mass is empty" in {

        val view =
          createView(
            Some(CommodityMeasure.form(request.declarationType).bind(Map("supplementaryUnits" -> "99.99", "grossMass" -> "", "netMass" -> "10.00")))
          )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#grossMass")

        view must containErrorElementWithMessageKey("declaration.commodityMeasure.grossMass.empty")
      }

      "display error when gross mass is incorrect" in {

        val view = createView(
          Some(
            CommodityMeasure
              .form(request.declarationType)
              .bind(Map("supplementaryUnits" -> "99.99", "grossMass" -> "5.00234ff", "netMass" -> "100.100"))
          )
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#grossMass")

        view must containErrorElementWithMessageKey("declaration.commodityMeasure.grossMass.error")
      }
    }
  }

  "Commodity Measure with no input for clearance request" should {
    onJourney(CLEARANCE) { implicit request =>
      "display no error when nothing is entered" in {

        val view =
          createView(Some(CommodityMeasure.form(request.declarationType).fillAndValidate(CommodityMeasure(Some(""), None, Some(""), Some("")))))

        view must not(haveGovukGlobalErrorSummary)
      }
    }
  }

  "Commodity Measure View when filled" should {
    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display data in supplementary units input" in {
        val form = CommodityMeasure.form(request.declarationType).fill(CommodityMeasure(Some("123"), Some(false), Some(""), Some("")))
        val view = createView(Some(form))

        view.getElementById("supplementaryUnits").attr("value") mustBe "123"
      }

      "display data in supplementary units not required input" in {
        val form = CommodityMeasure.form(request.declarationType).fill(CommodityMeasure(Some(""), Some(true), Some(""), Some("")))
        val view = createView(Some(form))

        view.getElementById("supplementaryUnitsNotRequired").hasAttr("checked") mustBe true
      }

      "display data in net mass input" in {
        val form = CommodityMeasure.form(request.declarationType).fill(CommodityMeasure(Some(""), Some(true), Some(""), Some("123")))
        val view = createView(Some(form))

        view.getElementById("netMass").attr("value") mustBe "123"
      }

      "display data in gross mass input" in {
        val form = CommodityMeasure.form(request.declarationType).fill(CommodityMeasure(Some(""), Some(true), Some("123"), Some("")))
        val view = createView(Some(form))

        view.getElementById("grossMass").attr("value") mustBe "123"
      }

      "display every input filled" in {

        val form = CommodityMeasure.form(request.declarationType).fill(CommodityMeasure(Some("123"), Some(false), Some("123"), Some("123")))
        val view = createView(Some(form))

        view.getElementById("supplementaryUnits").attr("value") mustBe "123"
        view.getElementById("supplementaryUnitsNotRequired").hasAttr("checked") mustBe false
        view.getElementById("netMass").attr("value") mustBe "123"
        view.getElementById("grossMass").attr("value") mustBe "123"
      }
    }
  }

  "Commodity Measure View when filled for clearance request" should {
    onJourney(CLEARANCE) { implicit request =>
      "display data in net mass input" in {

        val form = CommodityMeasure.form(request.declarationType).fill(CommodityMeasure(None, None, Some(""), Some("123")))
        val view = createView(Some(form))

        view.getElementById("netMass").attr("value") mustBe "123"
      }

      "display data in gross mass input" in {

        val form = CommodityMeasure.form(request.declarationType).fill(CommodityMeasure(None, None, Some("123"), Some("")))
        val view = createView(Some(form))

        view.getElementById("grossMass").attr("value") mustBe "123"
      }

      "display every input filled" in {

        val form = CommodityMeasure.form(request.declarationType).fill(CommodityMeasure(None, None, Some("123"), Some("123")))
        val view = createView(Some(form))

        view.getElementById("supplementaryUnits") mustBe (null)
        view.getElementById("supplementaryUnitsNotRequired") mustBe (null)
        view.getElementById("netMass").attr("value") mustBe "123"
        view.getElementById("grossMass").attr("value") mustBe "123"
      }
    }
  }

  "Commodity Measure back links" should {

    onEveryDeclarationJourney() { implicit request =>
      "display 'Back' button that links to 'Package Information' page" in {

        val backButton = createView().getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton must haveHref(controllers.declaration.routes.PackageInformationSummaryController.displayPage(Mode.Normal, itemId).url)
      }
    }

    onClearance { implicit request =>
      def viewHasBackLinkForProcedureCodeAndExsStatus(procedureCode: String, exsStatus: String, call: Call) = {
        val requestWithCache =
          journeyRequest(
            aDeclarationAfter(
              request.cacheModel,
              withIsExs(IsExs(exsStatus)),
              withItem(anItem(withItemId(itemId), withProcedureCodes(Some(procedureCode))))
            )
          )

        val backButton = createView()(requestWithCache).getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton must haveHref(call.url)
      }

      "display 'Back' button that links to 'Commodity Details' " when {
        "procedure code was '0019' and is EXS was 'No" in {

          viewHasBackLinkForProcedureCodeAndExsStatus(
            "0019",
            "No",
            controllers.declaration.routes.CommodityDetailsController.displayPage(Mode.Normal, itemId)
          )
        }
      }
      "display 'Back' button that links to 'UN Dangerous Goods Code' " when {
        "procedure code was '0019' and is EXS was 'Yes" in {

          viewHasBackLinkForProcedureCodeAndExsStatus(
            "0019",
            "Yes",
            controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage(Mode.Normal, itemId)
          )
        }
      }
      "display 'Back' button that links to 'Package Information' " when {
        "procedure code was '1234'" in {

          viewHasBackLinkForProcedureCodeAndExsStatus(
            "1234",
            "ANY",
            controllers.declaration.routes.PackageInformationSummaryController.displayPage(Mode.Normal, itemId)
          )
        }
      }
    }
  }
}
