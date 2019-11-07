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

import forms.declaration.CommodityDetails
import helpers.views.declaration.CommonMessages
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.commodity_details
import views.tags.ViewTest

@ViewTest
class CommodityDetailsViewSpec extends UnitViewSpec with ExportsTestData with Stubs with CommonMessages {

  private val page = new commodity_details(mainTemplate)
  private val form: Form[CommodityDetails] = CommodityDetails.form(DeclarationType.STANDARD)
  private val itemId = "item1"
  private val realMessages = validatedMessages
  private def createView(mode: Mode = Mode.Normal, itemId: String = itemId, form: Form[CommodityDetails] = form): Document =
    page(mode, itemId, form)(journeyRequest(), realMessages)

  "Commodity Details View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() mustBe realMessages("declaration.commodityDetails.title")
    }

    "display 'Back' button that links to 'Package Information' page" in {
      val backButton = createView().getElementById("link-back")

      backButton.getElementById("link-back") must haveHref(
        controllers.declaration.routes.FiscalInformationController.displayPage(Mode.Normal, itemId)
      )
    }

    "display 'Save and continue' button on page" in {

      val view = createView()

      val saveButton = view.select("#submit")
      saveButton.text() mustBe realMessages(saveAndContinueCaption)
    }
  }
}
