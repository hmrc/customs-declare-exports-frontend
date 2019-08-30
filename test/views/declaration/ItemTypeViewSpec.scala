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

import forms.Choice.AllowedChoiceValues
import forms.declaration.ItemType
import helpers.views.declaration.{CommonMessages, ItemTypeMessages}
import models.Mode
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.item_type
import views.tags.ViewTest

@ViewTest
class ItemTypeViewSpec extends ViewSpec with ItemTypeMessages with CommonMessages {

  private val form: Form[ItemType] = ItemType.form()
  private val itemTypePage = app.injector.instanceOf[item_type]
  private def createView(form: Form[ItemType] = form, journeyType: String): Html =
    itemTypePage(Mode.Normal, itemId, form, false)(fakeJourneyRequest(journeyType), messages)

  "Item Type View on empty page" when {

    "used for Standard Declaration journey" should {

      "display page title" in {

        createView(journeyType = AllowedChoiceValues.StandardDec).select("title").text() must be(messages(title))
      }

      "display section header" in {

        createView(journeyType = AllowedChoiceValues.StandardDec).getElementById("section-header").text() must be(
          "Items"
        )
      }

      "display header" in {

        createView(journeyType = AllowedChoiceValues.StandardDec).select("legend>h1").text() must be(messages(title))
      }

      "display empty input with label for CNC" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        view.getElementById("combinedNomenclatureCode-label").text() must be(messages(cncHeader))
        view.getElementById("combinedNomenclatureCode-hint").text() must be(messages(cncHeaderHint))
        view.getElementById("combinedNomenclatureCode").attr("value") must be("")
      }

      "display empty input with label for TARIC" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        view.getElementById("taricAdditionalCode_-label").text() must be(messages(taricHeader))
        view.getElementById("taricAdditionalCode_-hint").text() must be(messages(taricHeaderHint))
        view.getElementById("taricAdditionalCode_").attr("value") must be("")
      }

      "display empty input with label for NAC" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        view.getElementById("nationalAdditionalCode_-label").text() must be(messages(nacHeader))
        view.getElementById("nationalAdditionalCode_-hint").text() must be(messages(nacHeaderHint))
        view.getElementById("nationalAdditionalCode").attr("value") must be("")
      }

      "display empty input with label for Statistical Value" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        view.getElementById("statisticalValue-label").text() must be(messages(statisticalHeader))
        view.getElementById("statisticalValue-hint").text() must be(messages(statisticalHeaderHint))
        view.getElementById("statisticalValue").attr("value") must be("")
      }

      "display empty input with label for Description" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        view.getElementById("descriptionOfGoods-label").ownText() must be(messages(descriptionHeader))
        view.getElementById("descriptionOfGoods-hint").text() must be(messages(descriptionHeaderHint))
        view.getElementById("descriptionOfGoods").text() must be("")
      }

      "display empty input with label for CUS" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        view.getElementById("cusCode-label").text() must be(messages(cusCodeHeader))
        view.getElementById("cusCode-hint").text() must be(messages(cusCodeHeaderHint))
        view.getElementById("cusCode").attr("value") must be("")
      }

      "display empty input with label for UN Dangerous Goods Code" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        view.getElementById("unDangerousGoodsCode-label").text() must be(messages(unDangerousGoodsCodeHeader))
        view.getElementById("unDangerousGoodsCode-hint").text() must be(messages(unDangerousGoodsCodeHeaderHint))
        view.getElementById("unDangerousGoodsCode").attr("value") must be("")
      }

      "display two 'Add' buttons" in {

        createView(journeyType = AllowedChoiceValues.StandardDec).select("#add").size() must be(2)
      }

      "display 'Back' button that links to 'fiscal-information' page" in {

        val backButton = createView(journeyType = AllowedChoiceValues.StandardDec).getElementById("link-back")

        backButton.text() must be(messages(backCaption))
        backButton.attr("href") must be(s"/customs-declare-exports/declaration/items/$itemId/fiscal-information")
      }

      "display 'Save and continue' button" in {
        val view = createView(journeyType = AllowedChoiceValues.StandardDec)
        val saveButton = view.getElementById("submit")
        saveButton.text() must be(messages(saveAndContinueCaption))
      }

      "display 'Save and return' button" in {
        val view = createView(journeyType = AllowedChoiceValues.StandardDec)
        val saveButton = view.getElementById("submit_and_return")
        saveButton.text() must be(messages(saveAndReturnCaption))
      }
    }

    "used for Supplementary Declaration journey" should {

      "display page title" in {

        createView(journeyType = AllowedChoiceValues.SupplementaryDec).getElementById("title").text() must be(
          messages(title)
        )
      }

      "display empty input with label for CNC" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        view.getElementById("combinedNomenclatureCode-label").text() must be(messages(cncHeader))
        view.getElementById("combinedNomenclatureCode-hint").text() must be(messages(cncHeaderHint))
        view.getElementById("combinedNomenclatureCode").attr("value") must be("")
      }

      "display empty input with label for TARIC" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        view.getElementById("taricAdditionalCode_-label").text() must be(messages(taricHeader))
        view.getElementById("taricAdditionalCode_-hint").text() must be(messages(taricHeaderHint))
        view.getElementById("taricAdditionalCode_").attr("value") must be("")
      }

      "display empty input with label for NAC" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        view.getElementById("nationalAdditionalCode_-label").text() must be(messages(nacHeader))
        view.getElementById("nationalAdditionalCode_-hint").text() must be(messages(nacHeaderHint))
        view.getElementById("nationalAdditionalCode").attr("value") must be("")
      }

      "display empty input with label for Description" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        view.getElementById("descriptionOfGoods-label").text() must be(messages(descriptionHeader))
        view.getElementById("descriptionOfGoods-hint").text() must be(messages(descriptionHeaderHint))
        view.getElementById("descriptionOfGoods").text() must be("")
      }

      "display empty input with label for CUS" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        view.getElementById("cusCode-label").text() must be(messages(cusCodeHeader))
        view.getElementById("cusCode-hint").text() must be(messages(cusCodeHeaderHint))
        view.getElementById("cusCode").attr("value") must be("")
      }

      "display empty input with label for Statistical Value" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        view.getElementById("statisticalValue-label").text() must be(messages(statisticalHeader))
        view.getElementById("statisticalValue-hint").text() must be(messages(statisticalHeaderHint))
        view.getElementById("statisticalValue").attr("value") must be("")
      }

      "display two 'Add' buttons" in {

        createView(journeyType = AllowedChoiceValues.SupplementaryDec).select("#add").size() must be(2)
      }

      "display 'Back' button that links to 'fiscal-information' page" in {

        val backButton = createView(journeyType = AllowedChoiceValues.SupplementaryDec).getElementById("link-back")

        backButton.text() must be(messages(backCaption))
        backButton.attr("href") must be(s"/customs-declare-exports/declaration/items/$itemId/fiscal-information")
      }

      "display 'Save and continue' button" in {
        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)
        val saveButton = view.getElementById("submit")
        saveButton.text() must be(messages(saveAndContinueCaption))
      }

      "display 'Save and return' button" in {
        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)
        val saveButton = view.getElementById("submit_and_return")
        saveButton.text() must be(messages(saveAndReturnCaption))
      }
    }

  }

  "Item Type View with entered data" should {

    "used for Standard Declaration journey" should {

      "display data in CNC input" in {

        val itemType = ItemType("12345", Seq(), Seq(), "", None, None, "")
        val view = createView(ItemType.form().fill(itemType), AllowedChoiceValues.StandardDec)

        assertViewDataEntered(view, itemType)
      }

      "display data in Description input" in {

        val itemType = ItemType("", Seq(), Seq(""), "Description", None, None, "")
        val view = createView(ItemType.form().fill(itemType), AllowedChoiceValues.StandardDec)

        assertViewDataEntered(view, itemType)
      }

      "display data in CUS input" in {

        val itemType = ItemType("", Seq(), Seq(""), "", Some("1234"), None, "")
        val view = createView(ItemType.form().fill(itemType), AllowedChoiceValues.StandardDec)

        assertViewDataEntered(view, itemType)
      }

      "display data in UN Dangerous Goods Code input" in {

        val itemType = ItemType("", Seq(), Seq(""), "", None, Some("1234"), "12345")
        val view = createView(ItemType.form().fill(itemType), AllowedChoiceValues.StandardDec)

        assertViewDataEntered(view, itemType)
      }

      "display data in Statistical Value input" in {

        val itemType = ItemType("", Seq(), Seq(""), "", None, None, "12345")
        val view = createView(ItemType.form().fill(itemType), AllowedChoiceValues.StandardDec)

        assertViewDataEntered(view, itemType)
      }

      def assertViewDataEntered(view: Html, itemType: ItemType): Unit = {
        view.getElementById("combinedNomenclatureCode").attr("value") must equal(itemType.combinedNomenclatureCode)
        view.getElementById("descriptionOfGoods").text() must equal(itemType.descriptionOfGoods)
        view.getElementById("cusCode").attr("value") must equal(itemType.cusCode.getOrElse(""))
        view.getElementById("unDangerousGoodsCode").attr("value") must equal(
          itemType.unDangerousGoodsCode.getOrElse("")
        )
        view.getElementById("statisticalValue").attr("value") must equal(itemType.statisticalValue)
      }
    }

    "used for Supplementary Declaration journey" should {

      "display data in CNC input" in {

        val itemType = ItemType("12345", Seq(), Seq(), "", None, None, "")
        val view = createView(ItemType.form().fill(itemType), AllowedChoiceValues.SupplementaryDec)

        assertViewDataEntered(view, itemType)
      }

      "display data in Description input" in {

        val itemType = ItemType("", Seq(), Seq(""), "Description", None, None, "")
        val view = createView(ItemType.form().fill(itemType), AllowedChoiceValues.SupplementaryDec)

        assertViewDataEntered(view, itemType)
      }

      "display data in CUS input" in {

        val itemType = ItemType("", Seq(), Seq(""), "", Some("1234"), None, "")
        val view = createView(ItemType.form().fill(itemType), AllowedChoiceValues.SupplementaryDec)

        assertViewDataEntered(view, itemType)
      }

      "display data in Statistical Value input" in {

        val itemType = ItemType("", Seq(), Seq(""), "", None, None, "12345")
        val view = createView(ItemType.form().fill(itemType), AllowedChoiceValues.SupplementaryDec)

        assertViewDataEntered(view, itemType)
      }

      def assertViewDataEntered(view: Html, itemType: ItemType): Unit = {
        view.getElementById("combinedNomenclatureCode").attr("value") must equal(itemType.combinedNomenclatureCode)
        view.getElementById("descriptionOfGoods").text() must equal(itemType.descriptionOfGoods)
        view.getElementById("cusCode").attr("value") must equal(itemType.cusCode.getOrElse(""))
        view.getElementById("statisticalValue").attr("value") must equal(itemType.statisticalValue)
      }
    }

  }

}
