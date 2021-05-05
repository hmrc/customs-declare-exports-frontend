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

package views.declaration

import base.Injector
import forms.declaration.StatisticalValue
import models.Mode
import models.declaration.ExportItem
import org.jsoup.nodes.Document
import org.scalatest.Inspectors.forAll
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.statistical_value
import views.tags.ViewTest

@ViewTest
class StatisticalValueViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = instanceOf[statistical_value]
  private val form: Form[StatisticalValue] = StatisticalValue.form()
  private def createView(
    mode: Mode = Mode.Normal,
    item: ExportItem = ExportItem(id = "itemId", sequenceId = 1),
    form: Form[StatisticalValue] = form
  ): Document =
    page(mode, item.id, form)(journeyRequest(), messages)

  "Item Type View on empty page" should {

    val view: Document = createView()

    "have proper messages for labels" in {
      messages must haveTranslationFor("declaration.statisticalValue.header")
      messages must haveTranslationFor("declaration.statisticalValue.header.hint")
      messages must haveTranslationFor("declaration.statisticalValue.header.hint.i1")
      messages must haveTranslationFor("declaration.statisticalValue.header.hint.i2")
      messages must haveTranslationFor("declaration.statisticalValue.header.hint.i3")
      messages must haveTranslationFor("declaration.statisticalValue.inset.text")
      messages must haveTranslationFor("tariff.declaration.item.statisticalValue.common.text")
      messages must haveTranslationFor("tariff.declaration.item.statisticalValue.common.linkText.0")
    }

    "display same page title as header" in {
      view.title() must include(view.getElementsByTag("h1").text())
    }

    "display section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.5")
    }

    "display page title" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.statisticalValue.header")
    }

    "display the expected hint paragraphs" in {
      val hints = view.getElementsByClass("govuk-hint")
      hints.get(0) must containMessage("declaration.statisticalValue.header.hint")

      val indexedListOfMessages = List(
        "declaration.statisticalValue.header.hint.i1",
        "declaration.statisticalValue.header.hint.i2",
        "declaration.statisticalValue.header.hint.i3"
      ).zipWithIndex

      val bulletPoints = hints.get(1).children
      forAll(indexedListOfMessages)(t => bulletPoints.get(t._2) must containMessage(t._1))
    }

    "display the expected inset paragraph" in {
      val insetElement = view.getElementsByClass("govuk-inset-text").first
      insetElement must containMessage("declaration.statisticalValue.inset.text")
    }

    "display empty input for Statistical Value" in {
      view.getElementById("statisticalValue").attr("value") mustBe empty
    }

    "display 'Back' button that links to 'TARIC Codes' page" in {
      val backButton = view.getElementById("back-link")
      backButton must containMessage("site.back")
      backButton.getElementById("back-link") must haveHref(
        controllers.declaration.routes.NactCodeSummaryController.displayPage(Mode.Normal, itemId = "itemId")
      )
    }

    "display 'Save and continue' button" in {
      val saveButton = view.getElementById("submit")
      saveButton must containMessage("site.save_and_continue")
    }

    "display 'Save and return' button" in {
      val saveButton = view.getElementById("submit_and_return")
      saveButton must containMessage("site.save_and_come_back_later")
    }
  }

  "Item Type View with entered data" should {
    "display data in Statistical Value input" in {
      val itemType = StatisticalValue("12345")
      val view = createView(form = StatisticalValue.form().fill(itemType))

      assertViewDataEntered(view, itemType)
    }
  }

  def assertViewDataEntered(view: Document, itemType: StatisticalValue): Unit =
    view.getElementById("statisticalValue").attr("value") must equal(itemType.statisticalValue)
}
