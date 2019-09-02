/*
 * Copyright 2019 HM Revenue & Customs
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

import forms.declaration.CommodityMeasure
import helpers.views.declaration.{CommodityMeasureMessages, CommonMessages}
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import views.declaration.spec.AppViewSpec
import views.html.declaration.goods_measure
import views.tags.ViewTest

@ViewTest
class CommodityMeasureViewSpec extends AppViewSpec with CommodityMeasureMessages with CommonMessages {

  private val form: Form[CommodityMeasure] = CommodityMeasure.form()
  private val goodsMeasurePage = app.injector.instanceOf[goods_measure]
  private def createView(form: Form[CommodityMeasure] = form): Document =
    goodsMeasurePage(Mode.Normal, itemId, form)(fakeRequest, messages)

  "Commodity Measure View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() must be(messages(title))
    }

    "display section header" in {

      createView().getElementById("section-header").text() must be("Items")
    }

    "display empty input with label for supplementary units" in {

      val view = createView()

      view.getElementById("supplementaryUnits-label").text() must be(messages(supplementaryUnits))
      view.getElementById("supplementaryUnits-hint").text() must be(messages(supplementaryUnitsHint))
      view.getElementById("supplementaryUnits").attr("value") must be("")
    }

    "display empty input with label for net mass" in {

      val view = createView()

      view.getElementById("netMass-label").text() must be(messages(netMass))
      view.getElementById("netMass-hint").text() must be(messages(netMassHint))
      view.getElementById("netMass").attr("value") must be("")
    }

    "display empty input with label for gross mass" in {

      val view = createView()

      view.getElementById("grossMass-label").text() must be(messages(grossMass))
      view.getElementById("grossMass-hint").text() must be(messages(grossMassHint))
      view.getElementById("grossMass").attr("value") must be("")
    }

    "display 'Back' button that links to 'Package Information' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be(s"/customs-declare-exports/declaration/items/$itemId/package-information")
    }

    "display 'Save and continue' button on page" in {

      val saveButton = createView().select("#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Commodity Measure with invalid input" should {

    "display error when nothing is entered" in {

      val view = createView(CommodityMeasure.form().fillAndValidate(CommodityMeasure(Some(""), "", "")))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("netMass", "#netMass")
      view must haveFieldErrorLink("grossMass", "#grossMass")

      view.select("#error-message-netMass-input").text() must be(messages(netMassEmpty))
      view.select("#error-message-grossMass-input").text() must be(messages(grossMassEmpty))
    }

    "display error when supplementary units are incorrect" in {

      val view = createView(CommodityMeasure.form().fillAndValidate(CommodityMeasure(Some("99.123"), "", "")))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("supplementaryUnits", "#supplementaryUnits")

      view.select("#error-message-supplementaryUnits-input").text() must be(messages(supplementaryUnitsError))
    }

    "display error when net mass is empty" in {

      val view = createView(CommodityMeasure.form().fillAndValidate(CommodityMeasure(Some("99.99"), "", "10.00")))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("netMass", "#netMass")

      view.select("#error-message-netMass-input").text() must be(messages(netMassEmpty))
    }

    "display error when net mass is incorrect" in {

      val view =
        createView(CommodityMeasure.form().fillAndValidate(CommodityMeasure(Some("99.99"), "20.9999", "10.00")))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("netMass", "#netMass")

      view.select("#error-message-netMass-input").text() must be(messages(netMassError))
    }

    "display error when gross mass is empty" in {

      val view = createView(CommodityMeasure.form().fillAndValidate(CommodityMeasure(Some("99.99"), "10.00", "")))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("grossMass", "#grossMass")

      view.select("#error-message-grossMass-input").text() must be(messages(grossMassEmpty))
    }

    "display error when gross mass is incorrect" in {

      val view = createView(CommodityMeasure.form().fillAndValidate(CommodityMeasure(Some("99.99"), "5.00", "100.100")))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("grossMass", "#grossMass")

      view.select("#error-message-grossMass-input").text() must be(messages(grossMassError))
    }
  }

  "Commodity Measure View when filled" should {

    "display data in supplementary units input" in {

      val form = CommodityMeasure.form().fill(CommodityMeasure(Some("123"), "", ""))
      val view = createView(form)

      view.getElementById("supplementaryUnits").attr("value") must be("123")
      view.getElementById("netMass").attr("value") must be("")
      view.getElementById("grossMass").attr("value") must be("")
    }

    "display data in net mass input" in {

      val form = CommodityMeasure.form().fill(CommodityMeasure(Some(""), "123", ""))
      val view = createView(form)

      view.getElementById("supplementaryUnits").attr("value") must be("")
      view.getElementById("netMass").attr("value") must be("123")
      view.getElementById("grossMass").attr("value") must be("")
    }

    "display data in gross mass input" in {

      val form = CommodityMeasure.form().fill(CommodityMeasure(Some(""), "", "123"))
      val view = createView(form)

      view.getElementById("supplementaryUnits").attr("value") must be("")
      view.getElementById("netMass").attr("value") must be("")
      view.getElementById("grossMass").attr("value") must be("123")
    }

    "display every input filled" in {

      val form = CommodityMeasure.form().fill(CommodityMeasure(Some("123"), "123", "123"))
      val view = createView(form)

      view.getElementById("supplementaryUnits").attr("value") must be("123")
      view.getElementById("netMass").attr("value") must be("123")
      view.getElementById("grossMass").attr("value") must be("123")
    }
  }
}
