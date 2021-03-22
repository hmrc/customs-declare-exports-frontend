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
import forms.declaration.{CommodityMeasure, IsExs}
import models.DeclarationType._
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.Call
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.commodityMeasure.commodity_measure
import views.tags.ViewTest

@ViewTest
class CommodityMeasureViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  val itemId = "a7sc78"

  private val goodsMeasurePage = instanceOf[commodity_measure]
  private def createView(form: Option[Form[CommodityMeasure]] = None)(implicit request: JourneyRequest[_]): Document =
    goodsMeasurePage(Mode.Normal, itemId, form.getOrElse(CommodityMeasure.form(request.declarationType)))(request, messages)

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
    }
  }

  "Commodity Measure View on empty page for supplementary units on clearance request" should {
    onJourney(CLEARANCE) { implicit request =>
      "no display supplementary units" in {

        createView().getElementById("supplementaryUnits") mustBe (null)
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

        val view = createView(Some(CommodityMeasure.form(request.declarationType).fillAndValidate(CommodityMeasure(Some(""), Some(""), Some("")))))

        view must not(haveGovukGlobalErrorSummary)
      }
    }
  }

  "Commodity Measure View when filled" should {
    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display data in supplementary units input" in {

        val form = CommodityMeasure.form(request.declarationType).fill(CommodityMeasure(Some("123"), Some(""), Some("")))
        val view = createView(Some(form))

        view.getElementById("supplementaryUnits").attr("value") mustBe "123"
        view.getElementById("netMass").attr("value") mustBe empty
        view.getElementById("grossMass").attr("value") mustBe empty
      }

      "display data in net mass input" in {

        val form = CommodityMeasure.form(request.declarationType).fill(CommodityMeasure(Some(""), Some(""), Some("123")))
        val view = createView(Some(form))

        view.getElementById("supplementaryUnits").attr("value") mustBe empty
        view.getElementById("grossMass").attr("value") mustBe empty
        view.getElementById("netMass").attr("value") mustBe "123"
      }

      "display data in gross mass input" in {

        val form = CommodityMeasure.form(request.declarationType).fill(CommodityMeasure(Some(""), Some("123"), Some("")))
        val view = createView(Some(form))

        view.getElementById("supplementaryUnits").attr("value") mustBe empty
        view.getElementById("grossMass").attr("value") mustBe "123"
        view.getElementById("netMass").attr("value") mustBe empty
      }

      "display every input filled" in {

        val form = CommodityMeasure.form(request.declarationType).fill(CommodityMeasure(Some("123"), Some("123"), Some("123")))
        val view = createView(Some(form))

        view.getElementById("supplementaryUnits").attr("value") mustBe "123"
        view.getElementById("netMass").attr("value") mustBe "123"
        view.getElementById("grossMass").attr("value") mustBe "123"
      }
    }
  }

  "Commodity Measure View when filled for clearance request" should {
    onJourney(CLEARANCE) { implicit request =>
      "display data in net mass input" in {

        val form = CommodityMeasure.form(request.declarationType).fill(CommodityMeasure(None, Some(""), Some("123")))
        val view = createView(Some(form))

        view.getElementById("supplementaryUnits") mustBe (null)
        view.getElementById("grossMass").attr("value") mustBe empty
        view.getElementById("netMass").attr("value") mustBe "123"
      }

      "display data in gross mass input" in {

        val form = CommodityMeasure.form(request.declarationType).fill(CommodityMeasure(None, Some("123"), Some("")))
        val view = createView(Some(form))

        view.getElementById("supplementaryUnits") mustBe (null)
        view.getElementById("grossMass").attr("value") mustBe "123"
        view.getElementById("netMass").attr("value") mustBe empty
      }

      "display every input filled" in {

        val form = CommodityMeasure.form(request.declarationType).fill(CommodityMeasure(None, Some("123"), Some("123")))
        val view = createView(Some(form))

        view.getElementById("supplementaryUnits") mustBe (null)
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
