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
  private def createView(form: Form[ItemType] = form, journeyType: String): Html =
    item_type(appConfig, form)(fakeJourneyRequest(journeyType), messages)

  /*
   * Validation for the errors is done in ItemTypePageController
   */
  "Item Type View" should {

    "have proper labels for messages" in {

      assertMessage(title, "Item type")
      assertMessage(cncHeader, "6/14 What is the Combined Nomenclature Commodity Code for this item?")
      assertMessage(cncHeaderHint, "Up to 8 digits")
      assertMessage(taricHeader, "6/16 Do you need to enter any TARIC additional codes?")
      assertMessage(taricHeaderHint, "Up to 4 digits. If no additional code is required, leave blank")
      assertMessage(nacHeader, "6/17 Enter the National Additional Code")
      assertMessage(nacHeaderHint, "Up to 4 digits. If no additional code is required, leave blank")
      assertMessage(descriptionHeader, "6/8 Enter the trade description of the goods")
      assertMessage(
        descriptionHeaderHint,
        "Include information on size, weight or other physical criteria where this is required by the commodity code"
      )
      assertMessage(cusCodeHeader, "6/13 What is the CUS Code for this item?")
      assertMessage(cusCodeHeaderHint, "Up to 8 digits")
      assertMessage(unDangerousGoodsCodeHeader, "6/12 Does the item have a UN Dangerous Goods Code?")
      assertMessage(unDangerousGoodsCodeHeaderHint, "A 4 digit code.")
      assertMessage(statisticalHeader, "8/6 What is the statistical value of the items?")
      assertMessage(
        statisticalHeaderHint,
        "The approximate value of the goods at the time they leave the EU. It should be entered in GBP only."
      )
    }

    "have proper labels for error messages" in {

      assertMessage(cncErrorEmpty, "Combined Nomenclature Commodity Code cannot be empty")
      assertMessage(cncErrorLength, "Combined Nomenclature Commodity Code cannot be longer than 8 characters")
      assertMessage(cncErrorSpecialCharacters, "Combined Nomenclature Commodity Code cannot contain special characters")
      assertMessage(taricErrorLength, "TARIC additional code must be exactly 4 characters long")
      assertMessage(taricErrorSpecialCharacters, "TARIC additional code cannot contain specialCharacters")
      assertMessage(taricErrorMaxAmount, "You cannot add more codes")
      assertMessage(taricErrorDuplicate, "TARIC additional codes cannot contain duplicates")
      assertMessage(nacErrorLength, "National Additional Code cannot be longer than 4 characters")
      assertMessage(nacErrorSpecialCharacters, "National Additional Code cannot contain special Characters")
      assertMessage(nacErrorMaxAmount, "You cannot add more codes")
      assertMessage(nacErrorDuplicate, "National Additional Codes cannot contain duplicates")
      assertMessage(descriptionErrorEmpty, "Description cannot be empty")
      assertMessage(descriptionErrorLength, "Description cannot be longer than 280 characters")
      assertMessage(cusCodeErrorLength, "CUS Code must be exactly 8 characters long")
      assertMessage(cusCodeErrorSpecialCharacters, "CUS Code cannot contain special characters")
      assertMessage(unDangerousGoodsCodeErrorLength, "The code must be 4 characters")
      assertMessage(unDangerousGoodsCodeErrorSpecialCharacters, "Enter a UN Dangerous Goods Code in the correct format")
      assertMessage(statisticalErrorEmpty, "The statistical value cannot be empty")
      assertMessage(statisticalErrorLength, "Entered statistical value is too long")
      assertMessage(statisticalErrorWrongFormat, "Format of this value is incorrect")
    }
  }

  "Item Type View on empty page" when {

    "used for Standard Declaration journey" should {

      "display page title" in {

        getElementByCss(createView(journeyType = AllowedChoiceValues.StandardDec), "title").text() must be(
          messages(title)
        )
      }

      "display section header" in {

        getElementById(createView(journeyType = AllowedChoiceValues.StandardDec), "section-header").text() must be("Items")
      }

      "display header" in {

        getElementByCss(createView(journeyType = AllowedChoiceValues.StandardDec), "legend>h1").text() must be(
          messages(title)
        )
      }

      "display empty input with label for CNC" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        getElementByCss(view, "form>div:nth-child(4)>label>span:nth-child(1)").text() must be(messages(cncHeader))
        getElementByCss(view, "form>div:nth-child(4)>label>span.form-hint").text() must be(messages(cncHeaderHint))
        getElementById(view, "combinedNomenclatureCode").attr("value") must be("")
      }

      "display empty input with label for TARIC" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        getElementByCss(view, "form>div:nth-child(5)>label>span:nth-child(1)").text() must be(messages(taricHeader))
        getElementByCss(view, "form>div:nth-child(5)>label>span.form-hint").text() must be(messages(taricHeaderHint))
        getElementById(view, "taricAdditionalCode_").attr("value") must be("")
      }

      "display empty input with label for NAC" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        getElementByCss(view, "form>div:nth-child(6)>label>span:nth-child(1)").text() must be(messages(nacHeader))
        getElementByCss(view, "form>div:nth-child(6)>label>span.form-hint").text() must be(messages(nacHeaderHint))
        getElementById(view, "nationalAdditionalCode_").attr("value") must be("")
      }

      "display empty input with label for Statistical Value" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        getElementByCss(view, "form>div:nth-child(7)>label>span:nth-child(1)").text() must be(
          messages(statisticalHeader)
        )
        getElementByCss(view, "form>div:nth-child(7)>label>span.form-hint").text() must be(
          messages(statisticalHeaderHint)
        )
        getElementById(view, "statisticalValue").attr("value") must be("")
      }

      "display empty input with label for Description" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        getElementByCss(view, "form>div:nth-child(8)>label>span:nth-child(1)").text() must be(
          messages(descriptionHeader)
        )
        getElementByCss(view, "form>div:nth-child(8)>label>span.form-hint").text() must be(
          messages(descriptionHeaderHint)
        )
        getElementById(view, "descriptionOfGoods").text() must be("")
      }

      "display empty input with label for CUS" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        getElementByCss(view, "form>div:nth-child(9)>label>span:nth-child(1)").text() must be(messages(cusCodeHeader))
        getElementByCss(view, "form>div:nth-child(9)>label>span.form-hint").text() must be(messages(cusCodeHeaderHint))
        getElementById(view, "cusCode").attr("value") must be("")
      }

      "display empty input with label for UN Dangerous Goods Code" in {

        val view = createView(journeyType = AllowedChoiceValues.StandardDec)

        getElementByCss(view, "form>div:nth-child(10)>label>span:nth-child(1)").text() must be(
          messages(unDangerousGoodsCodeHeader)
        )
        getElementByCss(view, "form>div:nth-child(10)>label>span.form-hint").text() must be(
          messages(unDangerousGoodsCodeHeaderHint)
        )
        getElementById(view, "unDangerousGoodsCode").attr("value") must be("")
      }

      "display two 'Add' buttons" in {

        getElementsByCss(createView(journeyType = AllowedChoiceValues.StandardDec), "#add").size() must be(2)
      }

      "display 'Back' button that links to 'procedure-codes' page" in {

        val backButton = getElementById(createView(journeyType = AllowedChoiceValues.StandardDec), "link-back")

        backButton.text() must be(messages(backCaption))
        backButton.attr("href") must be("/customs-declare-exports/declaration/procedure-codes")
      }

      "display 'Save and continue' button" in {

        val saveButton = getElementByCss(createView(journeyType = AllowedChoiceValues.StandardDec), "#submit")
        saveButton.text() must be(messages(saveAndContinueCaption))
      }
    }

    "used for Supplementary Declaration journey" should {

      "display page title" in {

        getElementByCss(createView(journeyType = AllowedChoiceValues.SupplementaryDec), "title").text() must be(
          messages(title)
        )
      }

      "display header" in {

        getElementByCss(createView(journeyType = AllowedChoiceValues.SupplementaryDec), "legend>h1").text() must be(
          messages(title)
        )
      }

      "display empty input with label for CNC" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        getElementByCss(view, "form>div:nth-child(4)>label>span:nth-child(1)").text() must be(messages(cncHeader))
        getElementByCss(view, "form>div:nth-child(4)>label>span.form-hint").text() must be(messages(cncHeaderHint))
        getElementById(view, "combinedNomenclatureCode").attr("value") must be("")
      }

      "display empty input with label for TARIC" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        getElementByCss(view, "form>div:nth-child(5)>label>span:nth-child(1)").text() must be(messages(taricHeader))
        getElementByCss(view, "form>div:nth-child(5)>label>span.form-hint").text() must be(messages(taricHeaderHint))
        getElementById(view, "taricAdditionalCode_").attr("value") must be("")
      }

      "display empty input with label for NAC" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        getElementByCss(view, "form>div:nth-child(6)>label>span:nth-child(1)").text() must be(messages(nacHeader))
        getElementByCss(view, "form>div:nth-child(6)>label>span.form-hint").text() must be(messages(nacHeaderHint))
        getElementById(view, "nationalAdditionalCode_").attr("value") must be("")
      }

      "display empty input with label for Description" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        getElementByCss(view, "form>div:nth-child(8)>label>span:nth-child(1)").text() must be(
          messages(descriptionHeader)
        )
        getElementByCss(view, "form>div:nth-child(8)>label>span.form-hint").text() must be(
          messages(descriptionHeaderHint)
        )
        getElementById(view, "descriptionOfGoods").text() must be("")
      }

      "display empty input with label for CUS" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        getElementByCss(view, "form>div:nth-child(9)>label>span:nth-child(1)").text() must be(messages(cusCodeHeader))
        getElementByCss(view, "form>div:nth-child(9)>label>span.form-hint").text() must be(messages(cusCodeHeaderHint))
        getElementById(view, "cusCode").attr("value") must be("")
      }

      "display empty input with label for Statistical Value" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        getElementByCss(view, "form>div:nth-child(7)>label>span:nth-child(1)").text() must be(
          messages(statisticalHeader)
        )
        getElementByCss(view, "form>div:nth-child(7)>label>span.form-hint").text() must be(
          messages(statisticalHeaderHint)
        )
        getElementById(view, "statisticalValue").attr("value") must be("")
      }

      "display two 'Add' buttons" in {

        getElementsByCss(createView(journeyType = AllowedChoiceValues.SupplementaryDec), "#add").size() must be(2)
      }

      "display 'Back' button that links to 'procedure-codes' page" in {

        val backButton = getElementById(createView(journeyType = AllowedChoiceValues.SupplementaryDec), "link-back")

        backButton.text() must be(messages(backCaption))
        backButton.attr("href") must be("/customs-declare-exports/declaration/procedure-codes")
      }

      "display 'Save and continue' button" in {

        val saveButton = getElementByCss(createView(journeyType = AllowedChoiceValues.SupplementaryDec), "#submit")
        saveButton.text() must be(messages(saveAndContinueCaption))
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
