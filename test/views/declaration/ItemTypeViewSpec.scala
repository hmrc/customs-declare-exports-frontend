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
    itemTypePage(itemId, form, false)(fakeJourneyRequest(journeyType), messages)

  "Item Type View on empty page" when {

    "used for Standard Declaration journey" should {

      "display page title" in {

        getElementByCss(createView(journeyType = AllowedChoiceValues.StandardDec), "title").text() must be(
          messages(title)
        )
      }

      "display section header" in {

        getElementById(createView(journeyType = AllowedChoiceValues.StandardDec), "section-header").text() must be(
          "Items"
        )
      }

      "display header" in {

        getElementByCss(createView(journeyType = AllowedChoiceValues.StandardDec), "legend>h1").text() must be(
          messages(title)
        )
      }

      "display empty input with label for CNC" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        getElementById(view, "combinedNomenclatureCode-label").text() must be(messages(cncHeader))
        getElementById(view, "combinedNomenclatureCode-hint").text() must be(messages(cncHeaderHint))
        getElementById(view, "combinedNomenclatureCode").attr("value") must be("")
      }

      "display empty input with label for TARIC" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        getElementById(view, "taricAdditionalCode_-label").text() must be(messages(taricHeader))
        getElementById(view, "taricAdditionalCode_-hint").text() must be(messages(taricHeaderHint))
        getElementById(view, "taricAdditionalCode_").attr("value") must be("")
      }

      "display empty input with label for NAC" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        getElementById(view, "nationalAdditionalCode_-label").text() must be(messages(nacHeader))
        getElementById(view, "nationalAdditionalCode_-hint").text() must be(messages(nacHeaderHint))
        getElementById(view, "nationalAdditionalCode").attr("value") must be("")
      }

      "display empty input with label for Statistical Value" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        getElementById(view, "statisticalValue-label").text() must be(messages(statisticalHeader))
        getElementById(view, "statisticalValue-hint").text() must be(messages(statisticalHeaderHint))
        getElementById(view, "statisticalValue").attr("value") must be("")
      }

      "display empty input with label for Description" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        getElementById(view, "descriptionOfGoods-label").ownText() must be(messages(descriptionHeader))
        getElementById(view, "descriptionOfGoods-hint").text() must be(messages(descriptionHeaderHint))
        getElementById(view, "descriptionOfGoods").text() must be("")
      }

      "display empty input with label for CUS" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        getElementById(view, "cusCode-label").text() must be(messages(cusCodeHeader))
        getElementById(view, "cusCode-hint").text() must be(messages(cusCodeHeaderHint))
        getElementById(view, "cusCode").attr("value") must be("")
      }

      "display empty input with label for UN Dangerous Goods Code" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        getElementById(view, "unDangerousGoodsCode-label").text() must be(messages(unDangerousGoodsCodeHeader))
        getElementById(view, "unDangerousGoodsCode-hint").text() must be(messages(unDangerousGoodsCodeHeaderHint))
        getElementById(view, "unDangerousGoodsCode").attr("value") must be("")
      }

      "display two 'Add' buttons" in {

        getElementsByCss(createView(journeyType = AllowedChoiceValues.StandardDec), "#add").size() must be(2)
      }

      "display 'Back' button that links to 'fiscal-information' page" in {

        val backButton = getElementById(createView(journeyType = AllowedChoiceValues.StandardDec), "link-back")

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

        getElementById(createView(journeyType = AllowedChoiceValues.SupplementaryDec), "title").text() must be(
          messages(title)
        )
      }

      "display empty input with label for CNC" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        getElementById(view, "combinedNomenclatureCode-label").text() must be(messages(cncHeader))
        getElementById(view, "combinedNomenclatureCode-hint").text() must be(messages(cncHeaderHint))
        getElementById(view, "combinedNomenclatureCode").attr("value") must be("")
      }

      "display empty input with label for TARIC" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        getElementById(view, "taricAdditionalCode_-label").text() must be(messages(taricHeader))
        getElementById(view, "taricAdditionalCode_-hint").text() must be(messages(taricHeaderHint))
        getElementById(view, "taricAdditionalCode_").attr("value") must be("")
      }

      "display empty input with label for NAC" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        getElementById(view, "nationalAdditionalCode_-label").text() must be(messages(nacHeader))
        getElementById(view, "nationalAdditionalCode_-hint").text() must be(messages(nacHeaderHint))
        getElementById(view, "nationalAdditionalCode").attr("value") must be("")
      }

      "display empty input with label for Description" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        getElementById(view, "descriptionOfGoods-label").text() must be(messages(descriptionHeader))
        getElementById(view, "descriptionOfGoods-hint").text() must be(messages(descriptionHeaderHint))
        getElementById(view, "descriptionOfGoods").text() must be("")
      }

      "display empty input with label for CUS" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        getElementById(view, "cusCode-label").text() must be(messages(cusCodeHeader))
        getElementById(view, "cusCode-hint").text() must be(messages(cusCodeHeaderHint))
        getElementById(view, "cusCode").attr("value") must be("")
      }

      "display empty input with label for Statistical Value" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        getElementById(view, "statisticalValue-label").text() must be(messages(statisticalHeader))
        getElementById(view, "statisticalValue-hint").text() must be(messages(statisticalHeaderHint))
        getElementById(view, "statisticalValue").attr("value") must be("")
      }

      "display two 'Add' buttons" in {

        getElementsByCss(createView(journeyType = AllowedChoiceValues.SupplementaryDec), "#add").size() must be(2)
      }

      "display 'Back' button that links to 'fiscal-information' page" in {

        val backButton = getElementById(createView(journeyType = AllowedChoiceValues.SupplementaryDec), "link-back")

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
        getElementById(view, "combinedNomenclatureCode").attr("value") must equal(itemType.combinedNomenclatureCode)
        getElementById(view, "descriptionOfGoods").text() must equal(itemType.descriptionOfGoods)
        getElementById(view, "cusCode").attr("value") must equal(itemType.cusCode.getOrElse(""))
        getElementById(view, "unDangerousGoodsCode").attr("value") must equal(
          itemType.unDangerousGoodsCode.getOrElse("")
        )
        getElementById(view, "statisticalValue").attr("value") must equal(itemType.statisticalValue)
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
        getElementById(view, "combinedNomenclatureCode").attr("value") must equal(itemType.combinedNomenclatureCode)
        getElementById(view, "descriptionOfGoods").text() must equal(itemType.descriptionOfGoods)
        getElementById(view, "cusCode").attr("value") must equal(itemType.cusCode.getOrElse(""))
        getElementById(view, "statisticalValue").attr("value") must equal(itemType.statisticalValue)
      }
    }

  }

}
