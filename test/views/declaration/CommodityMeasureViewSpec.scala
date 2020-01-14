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
import forms.declaration.CommodityMeasure
import helpers.views.declaration.CommonMessages
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.goods_measure
import views.tags.ViewTest

@ViewTest
class CommodityMeasureViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  val itemId = "a7sc78"
  private val form: Form[CommodityMeasure] = CommodityMeasure.form()
  private val goodsMeasurePage = new goods_measure(mainTemplate)
  private def createView(form: Form[CommodityMeasure] = form): Document =
    goodsMeasurePage(Mode.Normal, itemId, form)(request, messages)

  "Commodity Measure" should {

    "have correct message keys" in {

      val messages = instanceOf[MessagesApi].preferred(request)

      messages must haveTranslationFor("supplementary.commodityMeasure.title")
      messages must haveTranslationFor("supplementary.commodityMeasure.netMass")
      messages must haveTranslationFor("supplementary.commodityMeasure.netMass.empty")
      messages must haveTranslationFor("supplementary.commodityMeasure.netMass.error")
      messages must haveTranslationFor("supplementary.commodityMeasure.grossMass")
      messages must haveTranslationFor("supplementary.commodityMeasure.grossMass.empty")
      messages must haveTranslationFor("supplementary.commodityMeasure.grossMass.error")
      messages must haveTranslationFor("supplementary.commodityMeasure.global.addOne")
      messages must haveTranslationFor("supplementary.commodityMeasure.supplementaryUnits")
      messages must haveTranslationFor("supplementary.commodityMeasure.supplementaryUnits.item")
      messages must haveTranslationFor("supplementary.commodityMeasure.supplementaryUnits.hint")
      messages must haveTranslationFor("supplementary.commodityMeasure.supplementaryUnits.error")
    }
  }

  "Commodity Measure View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() mustBe messages("supplementary.commodityMeasure.title")
    }

    "display section header" in {

      createView().getElementById("section-header").text() must include(messages("supplementary.items"))
    }

    "display empty input with label for supplementary units" in {

      val view = createView()

      view.getElementById("supplementaryUnits-label").text() mustBe messages("supplementary.commodityMeasure.supplementaryUnits")
      view.getElementById("supplementaryUnits-hint").text() mustBe messages("supplementary.commodityMeasure.supplementaryUnits.hint")
      view.getElementById("supplementaryUnits").attr("value") mustBe empty
    }

    "display empty input with label for net mass" in {

      val view = createView()

      view.getElementById("netMass-label").text() mustBe messages("supplementary.commodityMeasure.netMass")
      view.getElementById("netMass").attr("value") mustBe empty
    }

    "display empty input with label for gross mass" in {

      val view = createView()

      view.getElementById("grossMass-label").text() mustBe messages("supplementary.commodityMeasure.grossMass")
      view.getElementById("grossMass").attr("value") mustBe empty
    }

    "display 'Back' button that links to 'Package Information' page" in {

      val backButton = createView().getElementById("back-link")

      backButton.text() mustBe messages(backCaption)
      backButton.attr("href") must endWith(s"/items/$itemId/package-information")
    }

    "display 'Save and continue' button on page" in {

      val saveButton = createView().select("#submit")
      saveButton.text() mustBe messages(saveAndContinueCaption)
    }
  }

  "Commodity Measure with invalid input" should {

    "display error when nothing is entered" in {

      val view = createView(CommodityMeasure.form().fillAndValidate(CommodityMeasure(Some(""), "", "")))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("netMass", "#netMass")
      view must haveFieldErrorLink("grossMass", "#grossMass")

      view.select("#error-message-netMass-input").text() mustBe messages("supplementary.commodityMeasure.netMass.empty")
      view.select("#error-message-grossMass-input").text() mustBe messages("supplementary.commodityMeasure.grossMass.empty")
    }

    "display error when supplementary units are incorrect" in {

      val view = createView(CommodityMeasure.form().fillAndValidate(CommodityMeasure(Some("99.123"), "", "")))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("supplementaryUnits", "#supplementaryUnits")

      view.select("#error-message-supplementaryUnits-input").text() mustBe messages("supplementary.commodityMeasure.supplementaryUnits.error")
    }

    "display error when net mass is empty" in {

      val view = createView(CommodityMeasure.form().fillAndValidate(CommodityMeasure(Some("99.99"), "10.00", "")))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("netMass", "#netMass")

      view.select("#error-message-netMass-input").text() mustBe messages("supplementary.commodityMeasure.netMass.empty")
    }

    "display error when net mass is incorrect" in {

      val view =
        createView(CommodityMeasure.form().fillAndValidate(CommodityMeasure(Some("99.99"), "20.99", "10.00345")))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("netMass", "#netMass")

      view.select("#error-message-netMass-input").text() mustBe messages("supplementary.commodityMeasure.netMass.error")
    }

    "display error when gross mass is empty" in {

      val view = createView(CommodityMeasure.form().fillAndValidate(CommodityMeasure(Some("99.99"), "", "10.00")))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("grossMass", "#grossMass")

      view.select("#error-message-grossMass-input").text() mustBe messages("supplementary.commodityMeasure.grossMass.empty")
    }

    "display error when gross mass is incorrect" in {

      val view = createView(CommodityMeasure.form().fillAndValidate(CommodityMeasure(Some("99.99"), "5.00234", "100.100")))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("grossMass", "#grossMass")

      view.select("#error-message-grossMass-input").text() mustBe messages("supplementary.commodityMeasure.grossMass.error")
    }
  }

  "Commodity Measure View when filled" should {

    "display data in supplementary units input" in {

      val form = CommodityMeasure.form().fill(CommodityMeasure(Some("123"), "", ""))
      val view = createView(form)

      view.getElementById("supplementaryUnits").attr("value") mustBe "123"
      view.getElementById("netMass").attr("value") mustBe empty
      view.getElementById("grossMass").attr("value") mustBe empty
    }

    "display data in net mass input" in {

      val form = CommodityMeasure.form().fill(CommodityMeasure(Some(""), "", "123"))
      val view = createView(form)

      view.getElementById("supplementaryUnits").attr("value") mustBe empty
      view.getElementById("grossMass").attr("value") mustBe empty
      view.getElementById("netMass").attr("value") mustBe "123"
    }

    "display data in gross mass input" in {

      val form = CommodityMeasure.form().fill(CommodityMeasure(Some(""), "123", ""))
      val view = createView(form)

      view.getElementById("supplementaryUnits").attr("value") mustBe empty
      view.getElementById("grossMass").attr("value") mustBe "123"
      view.getElementById("netMass").attr("value") mustBe empty
    }

    "display every input filled" in {

      val form = CommodityMeasure.form().fill(CommodityMeasure(Some("123"), "123", "123"))
      val view = createView(form)

      view.getElementById("supplementaryUnits").attr("value") mustBe "123"
      view.getElementById("netMass").attr("value") mustBe "123"
      view.getElementById("grossMass").attr("value") mustBe "123"
    }
  }
}
