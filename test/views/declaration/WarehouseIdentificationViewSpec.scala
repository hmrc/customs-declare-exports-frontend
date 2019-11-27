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

import base.Injector
import forms.declaration.WarehouseIdentification
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.warehouse_identification
import views.tags.ViewTest

@ViewTest
class WarehouseIdentificationViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = new warehouse_identification(mainTemplate)
  private val form: Form[WarehouseIdentification] = WarehouseIdentification.form()

  private def createView(mode: Mode = Mode.Normal, form: Form[WarehouseIdentification] = form, messages: Messages = stubMessages()): Document =
    page(mode, form)(journeyRequest(), messages)

  "Warehouse Identification Number View" should {
    val view = createView()

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("declaration.warehouse.identification.sectionHeader")
      messages must haveTranslationFor("declaration.warehouse.identification.title")
      messages must haveTranslationFor("declaration.warehouse.identification.hint")
      messages must haveTranslationFor("declaration.warehouse.identification.identificationNumber.error")
      messages must haveTranslationFor("declaration.warehouse.identification.identificationNumber.empty")
    }

    "display same page title as header" in {
      val viewWithMessage = createView(messages = realMessagesApi.preferred(request))
      viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
    }

    "have the correct section header" in {
      view.getElementById("section-header").text() must include("declaration.warehouse.identification.sectionHeader")
    }

    "have the correct page title" in {
      view.getElementById("title").text() mustBe "declaration.warehouse.identification.title"
    }

    "have the correct hint" in {
      view.getElementById("hint").text() mustBe "declaration.warehouse.identification.hint"
    }

    "display 'Back' button that links to 'Items Summary' page" in {
      val backButton = view.getElementById("back-link")

      backButton.text() mustBe "site.back"
      backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.ItemsSummaryController.displayPage(Mode.Normal))
    }

    "display 'Save and continue' button on page" in {
      view.getElementById("submit").text() mustBe "site.save_and_continue"
    }

    "display 'Save and return' button on page" in {
      view.getElementById("submit_and_return").text() mustBe "site.save_and_come_back_later"
    }
  }
}
