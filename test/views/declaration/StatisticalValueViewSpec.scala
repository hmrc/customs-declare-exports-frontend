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
import forms.declaration.StatisticalValue
import models.Mode
import models.declaration.ExportItem
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
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
    form: Form[StatisticalValue] = form,
    messages: Messages = stubMessages()
  ): Document =
    page(mode, item.id, form)(journeyRequest(), messages)

  "Item Type View on empty page" when {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("declaration.statisticalValue.header")
      messages must haveTranslationFor("supplementary.items")
      messages must haveTranslationFor("declaration.statisticalValue.header.hint")
    }

    val view = createView()

    "used for Standard Declaration journey" should {

      "display same page title as header" in {
        val viewWithMessage = createView(messages = realMessagesApi.preferred(request))
        viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
      }

      "display section header" in {
        view.getElementById("section-header").text() must include("supplementary.items")
      }

      "display empty input with label for Statistical Value" in {
        view.getElementById("statisticalValue-hint").text() mustBe "declaration.statisticalValue.header.hint"
        view.getElementById("statisticalValue").attr("value") mustBe empty
      }

      "display 'Back' button that links to 'TARIC Codes' page" in {

        val backButton = createView().getElementById("back-link")

        backButton.text() mustBe "site.back"
        backButton.getElementById("back-link") must haveHref(
          controllers.declaration.routes.NactCodeSummaryController.displayPage(Mode.Normal, itemId = "itemId")
        )
      }

      "display 'Save and continue' button" in {
        val view = createView()
        val saveButton = view.getElementById("submit")
        saveButton.text() mustBe "site.save_and_continue"
      }

      "display 'Save and return' button" in {
        val view = createView()
        val saveButton = view.getElementById("submit_and_return")
        saveButton.text() mustBe "site.save_and_come_back_later"
      }
    }

    "used for Simplified Declaration journey" should {

      "display same page title as header" in {
        val viewWithMessage = createView(messages = realMessagesApi.preferred(request))
        viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
      }

      val view = createView()

      "display section header" in {
        view.getElementById("section-header").text() must include("supplementary.items")
      }

      "display empty input with label for Statistical Value" in {
        view.getElementById("statisticalValue-hint").text() mustBe "declaration.statisticalValue.header.hint"
        view.getElementById("statisticalValue").attr("value") mustBe empty
      }

      "display 'Back' button that links to 'TARIC Codes' page" in {

        val backButton = createView().getElementById("back-link")

        backButton.text() mustBe "site.back"
        backButton.getElementById("back-link") must haveHref(
          controllers.declaration.routes.NactCodeSummaryController.displayPage(Mode.Normal, itemId = "itemId")
        )
      }

      "display 'Save and continue' button" in {
        val view = createView()
        val saveButton = view.getElementById("submit")
        saveButton.text() mustBe "site.save_and_continue"
      }

      "display 'Save and return' button" in {
        val view = createView()
        val saveButton = view.getElementById("submit_and_return")
        saveButton.text() mustBe "site.save_and_come_back_later"
      }
    }

    "used for Supplementary Declaration journey" should {

      "display page title" in {

        createView()
          .getElementsByTag("h1")
          .text() mustBe "declaration.statisticalValue.header"
      }

      "display empty input with label for Statistical Value" in {

        val view = createView()

        view.getElementById("statisticalValue-hint").text() mustBe "declaration.statisticalValue.header.hint"
        view.getElementById("statisticalValue").attr("value") mustBe empty
      }

      "display 'Back' button that links to 'TARIC Codes' page" in {

        val backButton =
          createView().getElementById("back-link")

        backButton.text() mustBe "site.back"
        backButton.getElementById("back-link") must haveHref(
          controllers.declaration.routes.NactCodeSummaryController.displayPage(Mode.Normal, itemId = "itemId")
        )
      }

      "display 'Save and continue' button" in {
        val view = createView()
        val saveButton = view.getElementById("submit")
        saveButton.text() mustBe "site.save_and_continue"
      }

      "display 'Save and return' button" in {
        val view = createView()
        val saveButton = view.getElementById("submit_and_return")
        saveButton.text() mustBe "site.save_and_come_back_later"
      }
    }
  }

  "Item Type View with entered data" should {

    "used for Standard Declaration journey" should {

      "display data in Statistical Value input" in {

        val itemType = StatisticalValue("12345")
        val view = createView(form = StatisticalValue.form().fill(itemType))

        assertViewDataEntered(view, itemType)
      }

      def assertViewDataEntered(view: Document, itemType: StatisticalValue): Unit =
        view.getElementById("statisticalValue").attr("value") must equal(itemType.statisticalValue)
    }

    "used for Simplified Declaration journey" should {

      "display data in Statistical Value input" in {

        val itemType = StatisticalValue("12345")
        val view = createView(form = StatisticalValue.form().fill(itemType))

        assertViewDataEntered(view, itemType)
      }

      def assertViewDataEntered(view: Document, itemType: StatisticalValue): Unit =
        view.getElementById("statisticalValue").attr("value") must equal(itemType.statisticalValue)
    }

    "used for Supplementary Declaration journey" should {

      "display data in Statistical Value input" in {

        val itemType = StatisticalValue("12345")
        val view = createView(form = StatisticalValue.form().fill(itemType))

        assertViewDataEntered(view, itemType)
      }

      def assertViewDataEntered(view: Document, itemType: StatisticalValue): Unit =
        view.getElementById("statisticalValue").attr("value") must equal(itemType.statisticalValue)
    }

  }

}
