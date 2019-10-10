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
import forms.Choice.AllowedChoiceValues
import forms.declaration.ItemType
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.item_type
import views.tags.ViewTest

@ViewTest
class ItemTypeViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = new item_type(mainTemplate)
  private val form: Form[ItemType] = ItemType.form()
  private def createView(
    journeyType: String = AllowedChoiceValues.StandardDec,
    mode: Mode = Mode.Normal,
    itemId: String = "itemId",
    form: Form[ItemType] = form,
    hasAdditionalFiscalReferences: Boolean = false,
    taricAdditionalCodes: Seq[String] = Seq.empty,
    nationalAdditionalCodes: Seq[String] = Seq.empty,
    messages: Messages = stubMessages()
  ): Document =
    page(mode, itemId, form, hasAdditionalFiscalReferences, taricAdditionalCodes, nationalAdditionalCodes)(journeyRequest(), messages)

  "Item Type View on empty page" when {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("declaration.itemType.title")
      messages must haveTranslationFor("supplementary.items")
      messages must haveTranslationFor("declaration.itemType.title")
      messages must haveTranslationFor("declaration.itemType.combinedNomenclatureCode.header")
      messages must haveTranslationFor("declaration.itemType.combinedNomenclatureCode.header.hint")
      messages must haveTranslationFor("declaration.itemType.taricAdditionalCodes.header")
      messages must haveTranslationFor("declaration.itemType.taricAdditionalCodes.header.hint")
      messages must haveTranslationFor("declaration.itemType.nationalAdditionalCode.header")
      messages must haveTranslationFor("declaration.itemType.nationalAdditionalCode.header.hint")
      messages must haveTranslationFor("declaration.itemType.statisticalValue.header")
      messages must haveTranslationFor("declaration.itemType.statisticalValue.header.hint")
      messages must haveTranslationFor("declaration.itemType.description.header")
      messages must haveTranslationFor("declaration.itemType.description.header.hint")
      messages must haveTranslationFor("declaration.itemType.cusCode.header")
      messages must haveTranslationFor("declaration.itemType.unDangerousGoodsCode.header")
      messages must haveTranslationFor("declaration.itemType.unDangerousGoodsCode.header.hint")
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

      "display empty input with label for CNC" in {
        view
          .getElementById("combinedNomenclatureCode-label")
          .text() mustBe "declaration.itemType.combinedNomenclatureCode.header"
        view
          .getElementById("combinedNomenclatureCode-hint")
          .text() mustBe "declaration.itemType.combinedNomenclatureCode.header.hint"
        view.getElementById("combinedNomenclatureCode").attr("value") mustBe empty
      }

      "display empty input with label for TARIC" in {
        view
          .getElementById("taricAdditionalCode_-label")
          .text() mustBe "declaration.itemType.taricAdditionalCodes.header"
        view
          .getElementById("taricAdditionalCode_-hint")
          .text() mustBe "declaration.itemType.taricAdditionalCodes.header.hint"
        view.getElementById("taricAdditionalCode_").attr("value") mustBe empty
      }

      "display empty input with label for NAC" in {
        view
          .getElementById("nationalAdditionalCode_-label")
          .text() mustBe "declaration.itemType.nationalAdditionalCode.header"
        view
          .getElementById("nationalAdditionalCode_-hint")
          .text() mustBe "declaration.itemType.nationalAdditionalCode.header.hint"
        view.getElementById("nationalAdditionalCode").attr("value") mustBe empty
      }

      "display empty input with label for Statistical Value" in {
        view.getElementById("statisticalValue-label").text() mustBe "declaration.itemType.statisticalValue.header"
        view.getElementById("statisticalValue-hint").text() mustBe "declaration.itemType.statisticalValue.header.hint"
        view.getElementById("statisticalValue").attr("value") mustBe empty
      }

      "display empty input with label for Description" in {
        view.getElementById("descriptionOfGoods-label").ownText() mustBe "declaration.itemType.description.header"
        view.getElementById("descriptionOfGoods-hint").text() mustBe "declaration.itemType.description.header.hint"
        view.getElementById("descriptionOfGoods").text() mustBe empty
      }

      "display empty input with label for CUS" in {
        view.getElementById("cusCode-label").text() mustBe "declaration.itemType.cusCode.header"
        view.getElementById("cusCode-hint").text() mustBe "declaration.itemType.cusCode.header.hint"
        view.getElementById("cusCode").attr("value") mustBe empty
      }

      "display empty input with label for UN Dangerous Goods Code" in {
        view
          .getElementById("unDangerousGoodsCode-label")
          .text() mustBe "declaration.itemType.unDangerousGoodsCode.header"
        view
          .getElementById("unDangerousGoodsCode-hint")
          .text() mustBe "declaration.itemType.unDangerousGoodsCode.header.hint"
        view.getElementById("unDangerousGoodsCode").attr("value") mustBe empty
      }

      "display two 'Add' buttons" in {

        view.select("#add").size() must be(2)
      }

      "display 'Back' button that links to 'fiscal-information' page" in {

        val backButton = createView(journeyType = AllowedChoiceValues.StandardDec).getElementById("link-back")

        backButton.text() mustBe "site.back"
        backButton.getElementById("link-back") must haveHref(
          controllers.declaration.routes.FiscalInformationController.displayPage(Mode.Normal, itemId = "itemId")
        )
      }

      "display 'Save and continue' button" in {
        val view = createView(journeyType = AllowedChoiceValues.StandardDec)
        val saveButton = view.getElementById("submit")
        saveButton.text() mustBe "site.save_and_continue"
      }

      "display 'Save and return' button" in {
        val view = createView(journeyType = AllowedChoiceValues.StandardDec)
        val saveButton = view.getElementById("submit_and_return")
        saveButton.text() mustBe "site.save_and_come_back_later"
      }
    }

    "used for Supplementary Declaration journey" should {

      "display page title" in {

        createView(journeyType = AllowedChoiceValues.SupplementaryDec)
          .getElementById("title")
          .text() mustBe "declaration.itemType.title"
      }

      "display empty input with label for CNC" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        view
          .getElementById("combinedNomenclatureCode-label")
          .text() mustBe "declaration.itemType.combinedNomenclatureCode.header"
        view
          .getElementById("combinedNomenclatureCode-hint")
          .text() mustBe "declaration.itemType.combinedNomenclatureCode.header.hint"
        view.getElementById("combinedNomenclatureCode").attr("value") mustBe empty
      }

      "display empty input with label for TARIC" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        view
          .getElementById("taricAdditionalCode_-label")
          .text() mustBe "declaration.itemType.taricAdditionalCodes.header"
        view
          .getElementById("taricAdditionalCode_-hint")
          .text() mustBe "declaration.itemType.taricAdditionalCodes.header.hint"
        view.getElementById("taricAdditionalCode_").attr("value") mustBe empty
      }

      "display empty input with label for NAC" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        view
          .getElementById("nationalAdditionalCode_-label")
          .text() mustBe "declaration.itemType.nationalAdditionalCode.header"
        view
          .getElementById("nationalAdditionalCode_-hint")
          .text() mustBe "declaration.itemType.nationalAdditionalCode.header.hint"
        view.getElementById("nationalAdditionalCode").attr("value") mustBe empty
      }

      "display empty input with label for Description" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        view.getElementById("descriptionOfGoods-label").text() mustBe "declaration.itemType.description.header"
        view.getElementById("descriptionOfGoods-hint").text() mustBe "declaration.itemType.description.header.hint"
        view.getElementById("descriptionOfGoods").text() mustBe empty
      }

      "display empty input with label for CUS" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        view.getElementById("cusCode-label").text() mustBe "declaration.itemType.cusCode.header"
        view.getElementById("cusCode-hint").text() mustBe "declaration.itemType.cusCode.header.hint"
        view.getElementById("cusCode").attr("value") mustBe empty
      }

      "display empty input with label for Statistical Value" in {

        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)

        view.getElementById("statisticalValue-label").text() mustBe "declaration.itemType.statisticalValue.header"
        view.getElementById("statisticalValue-hint").text() mustBe "declaration.itemType.statisticalValue.header.hint"
        view.getElementById("statisticalValue").attr("value") mustBe empty
      }

      "display two 'Add' buttons" in {

        createView(journeyType = AllowedChoiceValues.SupplementaryDec).select("#add").size() must be(2)
      }

      "display 'Back' button that links to 'fiscal-information' page" in {

        val backButton =
          createView(journeyType = AllowedChoiceValues.SupplementaryDec).getElementById("link-back")

        backButton.text() mustBe "site.back"
        backButton.getElementById("link-back") must haveHref(
          controllers.declaration.routes.FiscalInformationController.displayPage(Mode.Normal, itemId = "itemId")
        )
      }

      "display 'Save and continue' button" in {
        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)
        val saveButton = view.getElementById("submit")
        saveButton.text() mustBe "site.save_and_continue"
      }

      "display 'Save and return' button" in {
        val view = createView(journeyType = AllowedChoiceValues.SupplementaryDec)
        val saveButton = view.getElementById("submit_and_return")
        saveButton.text() mustBe "site.save_and_come_back_later"
      }
    }
  }

  "Item Type View with entered data" should {

    "used for Standard Declaration journey" should {

      "display data in CNC input" in {

        val itemType = ItemType("12345", Seq(), Seq(), "", None, None, "")
        val view = createView(form = ItemType.form().fill(itemType))

        assertViewDataEntered(view, itemType)
      }

      "display data in Description input" in {

        val itemType = ItemType("", Seq(), Seq(""), "Description", None, None, "")
        val view = createView(form = ItemType.form().fill(itemType))

        assertViewDataEntered(view, itemType)
      }

      "display data in CUS input" in {

        val itemType = ItemType("", Seq(), Seq(""), "", Some("1234"), None, "")
        val view = createView(form = ItemType.form().fill(itemType))

        assertViewDataEntered(view, itemType)
      }

      "display data in UN Dangerous Goods Code input" in {

        val itemType = ItemType("", Seq(), Seq(""), "", None, Some("1234"), "12345")
        val view = createView(form = ItemType.form().fill(itemType))

        assertViewDataEntered(view, itemType)
      }

      "display data in Statistical Value input" in {

        val itemType = ItemType("", Seq(), Seq(""), "", None, None, "12345")
        val view = createView(form = ItemType.form().fill(itemType))

        assertViewDataEntered(view, itemType)
      }

      def assertViewDataEntered(view: Document, itemType: ItemType): Unit = {
        view.getElementById("combinedNomenclatureCode").attr("value") must equal(itemType.combinedNomenclatureCode)
        view.getElementById("descriptionOfGoods").text() must equal(itemType.descriptionOfGoods)
        view.getElementById("cusCode").attr("value") must equal(itemType.cusCode.getOrElse(""))
        view.getElementById("unDangerousGoodsCode").attr("value") must equal(itemType.unDangerousGoodsCode.getOrElse(""))
        view.getElementById("statisticalValue").attr("value") must equal(itemType.statisticalValue)
      }
    }

    "used for Supplementary Declaration journey" should {

      "display data in CNC input" in {

        val itemType = ItemType("12345", Seq(), Seq(), "", None, None, "")
        val view = createView(form = ItemType.form().fill(itemType), journeyType = AllowedChoiceValues.SupplementaryDec)

        assertViewDataEntered(view, itemType)
      }

      "display data in Description input" in {

        val itemType = ItemType("", Seq(), Seq(""), "Description", None, None, "")
        val view = createView(form = ItemType.form().fill(itemType), journeyType = AllowedChoiceValues.SupplementaryDec)

        assertViewDataEntered(view, itemType)
      }

      "display data in CUS input" in {

        val itemType = ItemType("", Seq(), Seq(""), "", Some("1234"), None, "")
        val view = createView(form = ItemType.form().fill(itemType), journeyType = AllowedChoiceValues.SupplementaryDec)

        assertViewDataEntered(view, itemType)
      }

      "display data in Statistical Value input" in {

        val itemType = ItemType("", Seq(), Seq(""), "", None, None, "12345")
        val view = createView(form = ItemType.form().fill(itemType), journeyType = AllowedChoiceValues.SupplementaryDec)

        assertViewDataEntered(view, itemType)
      }

      def assertViewDataEntered(view: Document, itemType: ItemType): Unit = {
        view.getElementById("combinedNomenclatureCode").attr("value") must equal(itemType.combinedNomenclatureCode)
        view.getElementById("descriptionOfGoods").text() must equal(itemType.descriptionOfGoods)
        view.getElementById("cusCode").attr("value") must equal(itemType.cusCode.getOrElse(""))
        view.getElementById("statisticalValue").attr("value") must equal(itemType.statisticalValue)
      }
    }

  }

}
