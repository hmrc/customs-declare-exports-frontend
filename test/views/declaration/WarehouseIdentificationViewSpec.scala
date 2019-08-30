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
import play.api.i18n.MessagesApi
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.warehouse_identification
import views.tags.ViewTest

@ViewTest
class WarehouseIdentificationViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val form: Form[WarehouseIdentification] = WarehouseIdentification.form()

  private def createView(mode: Mode = Mode.Normal, form: Form[WarehouseIdentification] = form): Document =
    new warehouse_identification(mainTemplate)(mode, form)(journeyRequest, stubMessages())

  "Warehouse Identification View" should {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest)
      messages("supplementary.warehouse.title") mustBe "Enter more details about the warehouse"
    }

    "display page title" in {
      createView().select("title").text() must be("supplementary.warehouse.title")
    }

    "display header" in {
      createView().select("legend>h1").text() must be("supplementary.warehouse.title")
    }

    "display 'Back' button that links to 'Supervising Office' page" in {
      val backButton = createView().getElementById("link-back")

      backButton.text() mustBe "site.back"
      backButton.getElementById("link-back") must haveHref(
        controllers.declaration.routes.ItemsSummaryController.displayPage(Mode.Normal)
      )
    }

    "display 'Save and continue' button on page" in {
      createView().getElementById("submit").text() mustBe "site.save_and_continue"
    }

    "display 'Save and return' button on page" in {
      createView().getElementById("submit_and_return").text() mustBe "site.save_and_come_back_later"
    }
  }
}
