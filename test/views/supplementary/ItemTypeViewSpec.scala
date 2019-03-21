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

package views.supplementary
import forms.supplementary.ItemType
import helpers.views.supplementary.{CommonMessages, ItemTypeMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.html.supplementary.item_type
import views.supplementary.spec.ViewSpec

class ItemTypeViewSpec extends ViewSpec with ItemTypeMessages with CommonMessages {

  private val form: Form[ItemType] = ItemType.form()
  private def createView(form: Form[ItemType] = form): Html = item_type(appConfig, form)(fakeRequest, messages)

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
      assertMessage(descriptionHeaderHint, "Include information on size, weight or other physical criteria where this is required by the commodity code")
      assertMessage(cusCodeHeader, "6/13 What is the CUS Code for this item?")
      assertMessage(cusCodeHeaderHint, "Up to 8 digits")
      assertMessage(statisticalHeader, "8/6 What is the statistical value of the items?")
      assertMessage(statisticalHeaderHint, "The approximate value of the goods at the time they leave the EU. It should be entered in GBP only.")
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
      assertMessage(statisticalErrorEmpty, "The statistical value cannot be empty")
      assertMessage(statisticalErrorLength, "Entered statistical value is too long")
      assertMessage(statisticalErrorWrongFormat, "Format of this value is incorrect")
    }
  }

  "Item Type View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title))
    }

    "display header" in {

      val view = createView()

      getElementByCss(view, "legend>h1").text() must be(messages(title))
    }

    "display empty input with label for CNC" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(3)>label>span:nth-child(1)").text() must be(messages(cncHeader))
      getElementByCss(view, "form>div:nth-child(3)>label>span.form-hint").text() must be(messages(cncHeaderHint))
      getElementById(view, "combinedNomenclatureCode").attr("value") must be("")
    }

    "display empty input with label for TARIC" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(4)>label>span:nth-child(1)").text() must be(messages(taricHeader))
      getElementByCss(view, "form>div:nth-child(4)>label>span.form-hint").text() must be(messages(taricHeaderHint))
      getElementById(view, "taricAdditionalCode_").attr("value") must be("")
    }

    "display two \"Add\" buttons" in {

      val view = createView()

      getElementsByCss(view, "#add").size() must be(2)
    }

    "display empty input with label for NAC" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(5)>label>span:nth-child(1)").text() must be(messages(nacHeader))
      getElementByCss(view, "form>div:nth-child(5)>label>span.form-hint").text() must be(messages(nacHeaderHint))
      getElementById(view, "nationalAdditionalCode_").attr("value") must be("")
    }

    "display empty input with label for CUS" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(6)>label>span:nth-child(1)").text() must be(messages(cusCodeHeader))
      getElementByCss(view, "form>div:nth-child(6)>label>span.form-hint").text() must be(messages(cusCodeHeaderHint))
      getElementById(view, "cusCode").attr("value") must be("")
    }

    "display empty input with label for Statistical Value" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(7)>label>span:nth-child(1)").text() must be(messages(statisticalHeader))
      getElementByCss(view, "form>div:nth-child(7)>label>span.form-hint").text() must be(messages(statisticalHeaderHint))
      getElementById(view, "statisticalValue").attr("value") must be("")
    }

    "display empty input with label for Description" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(8)>label>span:nth-child(1)").text() must be(messages(descriptionHeader))
      getElementByCss(view, "form>div:nth-child(8)>label>span.form-hint").text() must be(messages(descriptionHeaderHint))
      getElementById(view, "descriptionOfGoods").text() must be("")
    }

    "display \"Back\" button that links to \"procedure-codes\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/supplementary/procedure-codes")
    }

    "display \"Save and continue\" button" in {

      val saveButton = getElementByCss(createView(), "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Item Type View when filled" should {

    "display data in CNC input" in {

      val form = ItemType.form().fill(ItemType("12345", Seq(), Seq(), "", Some(""), ""))
      val view = createView(form)

      getElementById(view, "combinedNomenclatureCode").attr("value") must be("12345")
    }

    "display data in CUS input" in {

      val form = ItemType.form().fill(ItemType("", Seq(), Seq(""), "", Some("1234"), ""))
      val view = createView(form)

      getElementById(view, "cusCode").attr("value") must be("1234")
    }

    "display data in Statistical Value input" in {

      val form = ItemType.form().fill(ItemType("", Seq(), Seq(""), "", Some(""), "12345"))
      val view = createView(form)

      getElementById(view, "statisticalValue").attr("value") must be("12345")
    }

    "display data in Description input" in {

      val form = ItemType.form().fill(ItemType("", Seq(), Seq(""), "Description", Some(""), ""))
      val view = createView(form)

      getElementById(view, "descriptionOfGoods").text() must be("Description")
    }
  }
}
