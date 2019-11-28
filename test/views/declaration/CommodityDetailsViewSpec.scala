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
import models.DeclarationType.DeclarationType
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

  private val page = new commodity_details(mainTemplate, minimalAppConfig)
  private val itemId = "item1"
  private val realMessages = validatedMessages
  private def createView(declarationType: DeclarationType, form: Form[CommodityDetails]): Document =
    page(Mode.Normal, itemId, form)(journeyRequest(declarationType), realMessages)

  def commodityDetailsView(
    declarationType: DeclarationType,
    form: Form[CommodityDetails],
    commodityDetails: Option[CommodityDetails] = None
  ): Unit = {
    val view = createView(declarationType, commodityDetails.fold(form)(form.fill(_)))

    "display page title" in {
      view.getElementById("title").text() mustBe realMessages("declaration.commodityDetails.title")
    }

    "display commodity code input field" in {
      val expectedCode = commodityDetails.flatMap(_.combinedNomenclatureCode).getOrElse("")
      view.getElementById(CommodityDetails.combinedNomenclatureCodeKey).attr("value") mustBe expectedCode
    }

    "display description textarea field" in {
      val expectedDescription = commodityDetails.map(_.descriptionOfGoods).getOrElse("")
      view.getElementById(CommodityDetails.descriptionOfGoodsKey).text() mustBe expectedDescription
    }

    "display 'Back' button that links to 'Package Information' page" in {
      val backButton = view.getElementById("back-link")
      backButton.getElementById("back-link") must haveHref(
        controllers.declaration.routes.FiscalInformationController.displayPage(Mode.Normal, itemId)
      )
    }

    "display 'Save and continue' button on page" in {
      val saveButton = view.select("#submit")
      saveButton.text() mustBe realMessages(saveAndContinueCaption)
    }
  }

  "Commodity Details View on empty page" when {

    for (decType <- DeclarationType.values) {
      s"we are on $decType journey" should {
        behave like commodityDetailsView(decType, CommodityDetails.form(decType))
      }
    }
  }

  "Commodity Details View on populated page" when {

    val details = Some(CommodityDetails(Some("12345678"), "Description"))

    for (decType <- DeclarationType.values) {
      s"we are on $decType journey" should {
        behave like commodityDetailsView(decType, CommodityDetails.form(decType), details)
      }
    }
  }
}
