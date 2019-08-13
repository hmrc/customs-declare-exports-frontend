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

package controllers.declaration

import base.{CustomExportsBaseSpec, TestHelper, ViewValidator}
import controllers.util.{Add, FormAction, Remove, SaveAndContinue}
import forms.declaration.ItemType
import forms.declaration.ItemType.{nationalAdditionalCodesKey, taricAdditionalCodesKey}
import forms.declaration.ItemTypeSpec._
import helpers.views.declaration.{CommonMessages, ItemTypeMessages}
import models.ExportsDeclaration
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import play.api.test.Helpers._
import services.cache.{ExportItem, ExportsItemBuilder}

class ItemTypeControllerSpec
    extends CustomExportsBaseSpec with ViewValidator with ItemTypeMessages with CommonMessages with ExportsItemBuilder {
  import ItemTypeControllerSpec._

  private val exampleItem = anItem()

  private val cacheModel = aDeclaration(withChoice("SMP"), withItem(exampleItem))

  private def uri(item: ExportItem) = uriWithContextPath(s"/declaration/items/${item.id}/item-type")

  override def beforeEach() {
    super.beforeEach()
    authorizedUser()
  }

  override def afterEach() {
    reset(mockAuthConnector, mockExportsCacheService)
    super.afterEach()
  }

  "Item Type Page Controller on GET" should {

    "return 200 status code" in {
      withNewCaching(cacheModel)

      val result = route(app, getRequest(uri(exampleItem), sessionId = cacheModel.sessionId)).get

      status(result) must be(OK)
    }

    "read item from cache and display it" in {

      val exampleItemType =
        ItemType("5555", Seq("6666"), Seq("7777"), "FaultyGoods", Some("CusCus"), Some("12CD"), "900")
      val item = ExportItem("id", itemType = Some(exampleItemType))
      val model = aDeclaration(withItem(item), withChoice("SMP"))

      withNewCaching(model)

      val result = route(app, getRequest(uri(item), sessionId = model.sessionId)).get
      val page = contentAsString(result)

      status(result) must be(OK)
      page must include("5555")
      page must include("6666")
      page must include("7777")
      page must include("FaultyGoods")
      page must include("CusCus")
      page must include("900")
    }

    /*
     * For some reason I cannot create the table
     * in view tests for this form using fill() - for now they are here
     */
    "display the table" when {

      "user added one TARIC" in {

        val cachedData = ItemType("100", Seq("1234"), Seq(), "Description", None, None, "100")
        val item = ExportItem("id", itemType = Some(cachedData))
        val model = aDeclaration(withItem(item), withChoice("SMP"))
        withNewCaching(model)

        val result = route(app, getRequest(uri(item), sessionId = model.sessionId)).get
        val page = contentAsString(result)

        getElementByCss(page, "table>tbody>tr>th:nth-child(1)").text() must be("1234")
        getElementByCss(page, "table>tbody>tr>th:nth-child(2)> button").text() must be(messages(removeCaption))
      }

      "user added one NAC" in {

        val cachedData = ItemType("100", Seq(), Seq("1234"), "Description", None, None, "100")
        val item = ExportItem("id", itemType = Some(cachedData))
        val model = aDeclaration(withItem(item), withChoice("SMP"))
        withNewCaching(model)

        val result = route(app, getRequest(uri(item), sessionId = model.sessionId)).get
        val page = contentAsString(result)

        getElementByCss(page, "table>tbody>tr>th:nth-child(1)").text() must be("1234")
        getElementByCss(page, "table>tbody>tr>th:nth-child(2)> button").text() must be(messages(removeCaption))
      }
    }
  }

  "Item Type Page Controller on POST" when {

    "form action is 'Save and Continue'" should {

      "display form page with error" when {

        "Combined Nomenclature Code is empty" in {
          withNewCaching(cacheModel)

          val form =
            buildItemTypeUrlEncodedInput(SaveAndContinue)(statisticalValue = "100", descriptionOfGoods = "Description")
          val result = route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(form.toSeq: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, cncErrorEmpty, "#combinedNomenclatureCode")

          getElementByCss(page, "#error-message-combinedNomenclatureCode-input").text() must be(messages(cncErrorEmpty))
        }

        "Combined Nomenclature Code is longer than 8 characters" in {
          withNewCaching(cacheModel)

          val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
            combinedNomenclatureCode = "123456789",
            statisticalValue = "100",
            descriptionOfGoods = "Description"
          )
          val result = route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(form.toSeq: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, cncErrorLength, "#combinedNomenclatureCode")

          getElementByCss(page, "#error-message-combinedNomenclatureCode-input").text() must be(
            messages(cncErrorLength)
          )
        }

        "Combined Nomenclature Code contains special characters" in {
          withNewCaching(cacheModel)

          val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
            combinedNomenclatureCode = "123$%^",
            statisticalValue = "100",
            descriptionOfGoods = "Description"
          )
          val result = route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(form.toSeq: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, cncErrorSpecialCharacters, "#combinedNomenclatureCode")

          getElementByCss(page, "#error-message-combinedNomenclatureCode-input").text() must be(
            messages(cncErrorSpecialCharacters)
          )
        }

        "TARIC additional code is not 4 characters long" in {
          withNewCaching(cacheModel)

          val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
            combinedNomenclatureCode = "12345",
            taricAdditionalCodes = Seq("123"),
            statisticalValue = "100",
            descriptionOfGoods = "Description"
          )
          val result = route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(form.toSeq: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, taricErrorLength, "#taricAdditionalCode_")

          getElementByCss(page, "#error-message-taricAdditionalCode_-input").text() must be(messages(taricErrorLength))
        }

        "TARIC additional code contains special characters" in {
          withNewCaching(cacheModel)

          val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
            combinedNomenclatureCode = "12345",
            taricAdditionalCodes = Seq("1$%^"),
            statisticalValue = "100",
            descriptionOfGoods = "Description"
          )
          val result = route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(form.toSeq: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, taricErrorSpecialCharacters, "#taricAdditionalCode_")

          getElementByCss(page, "#error-message-taricAdditionalCode_-input").text() must be(
            messages(taricErrorSpecialCharacters)
          )
        }

        "National additional code is invalid" in {
          withNewCaching(cacheModel)

          val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
            combinedNomenclatureCode = "12345",
            taricAdditionalCodes = Seq("1234"),
            nationalAdditionalCodes = Seq("12345"),
            statisticalValue = "100",
            descriptionOfGoods = "Description"
          )
          val result = route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(form.toSeq: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, nacErrorInvalid, "#nationalAdditionalCode_")

          getElementByCss(page, "#error-message-nationalAdditionalCode_-input").text() must be(
            messages(nacErrorInvalid)
          )
        }

        "Description of goods is empty" in {
          withNewCaching(cacheModel)

          val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
            combinedNomenclatureCode = "12345",
            taricAdditionalCodes = Seq("1234"),
            nationalAdditionalCodes = Seq("VATE"),
            statisticalValue = "100"
          )
          val result = route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(form.toSeq: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, descriptionErrorEmpty, "#descriptionOfGoods")

          getElementByCss(page, "#error-message-descriptionOfGoods-input").text() must be(
            messages(descriptionErrorEmpty)
          )
        }

        "Description of goods is longer than 280 characters" in {
          withNewCaching(cacheModel)

          val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
            combinedNomenclatureCode = "12345",
            taricAdditionalCodes = Seq("1234"),
            nationalAdditionalCodes = Seq("VATE"),
            statisticalValue = "100",
            descriptionOfGoods = TestHelper.createRandomAlphanumericString(281)
          )
          val result = route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(form.toSeq: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, descriptionErrorLength, "#descriptionOfGoods")

          getElementByCss(page, "#error-message-descriptionOfGoods-input").text() must be(
            messages(descriptionErrorLength)
          )
        }

        "CUS code is not 8 characters long" in {
          withNewCaching(cacheModel)

          val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
            combinedNomenclatureCode = "12345",
            taricAdditionalCodes = Seq("1234"),
            nationalAdditionalCodes = Seq("VATE"),
            cusCode = "12345",
            statisticalValue = "100",
            descriptionOfGoods = "Description"
          )
          val result = route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(form.toSeq: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, cusCodeErrorLength, "#cusCode")

          getElementByCss(page, "#error-message-cusCode-input").text() must be(messages(cusCodeErrorLength))
        }

        "CUS code contains special characters" in {
          withNewCaching(cacheModel)

          val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
            combinedNomenclatureCode = "12345",
            taricAdditionalCodes = Seq("1234"),
            nationalAdditionalCodes = Seq("VATE"),
            cusCode = "1234@#$%",
            statisticalValue = "100",
            descriptionOfGoods = "Description"
          )
          val result = route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(form.toSeq: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, cusCodeErrorSpecialCharacters, "#cusCode")

          getElementByCss(page, "#error-message-cusCode-input").text() must be(messages(cusCodeErrorSpecialCharacters))
        }

        "Statistical value is empty" in {
          withNewCaching(cacheModel)

          val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
            combinedNomenclatureCode = "12345",
            descriptionOfGoods = "Description"
          )
          val result = route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(form.toSeq: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, statisticalErrorEmpty, "#statisticalValue")

          getElementByCss(page, "#error-message-statisticalValue-input").text() must be(messages(statisticalErrorEmpty))
        }

        "Statistical value contains more than 15 digits" in {
          withNewCaching(cacheModel)

          val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
            combinedNomenclatureCode = "12345",
            statisticalValue = "12345678901234.56",
            descriptionOfGoods = "Description"
          )
          val result = route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(form.toSeq: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, statisticalErrorLength, "#statisticalValue")

          getElementByCss(page, "#error-message-statisticalValue-input").text() must be(
            messages(statisticalErrorLength)
          )
        }

        "Statistical value contains non-digit characters" in {
          withNewCaching(cacheModel)

          val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
            combinedNomenclatureCode = "12345",
            statisticalValue = "123456Q.78",
            descriptionOfGoods = "Description"
          )
          val result = route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(form.toSeq: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, statisticalErrorWrongFormat, "#statisticalValue")

          getElementByCss(page, "#error-message-statisticalValue-input").text() must be(
            messages(statisticalErrorWrongFormat)
          )
        }

        "user press 'Save and continue' without entering anything" in {
          withNewCaching(cacheModel)

          val form = buildItemTypeUrlEncodedInput(SaveAndContinue)()
          val result = route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(form.toSeq: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, cncErrorEmpty, "#combinedNomenclatureCode")
          checkErrorLink(page, 2, descriptionErrorEmpty, "#descriptionOfGoods")
          checkErrorLink(page, 3, statisticalErrorEmpty, "#statisticalValue")

          getElementByCss(page, "#error-message-combinedNomenclatureCode-input").text() must be(messages(cncErrorEmpty))
          getElementByCss(page, "#error-message-descriptionOfGoods-input").text() must be(
            messages(descriptionErrorEmpty)
          )
          getElementByCss(page, "#error-message-statisticalValue-input").text() must be(messages(statisticalErrorEmpty))
        }

        "user put incorrect/empty values into all fields" in {
          withNewCaching(cacheModel)

          val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
            combinedNomenclatureCode = "123456789",
            taricAdditionalCodes = Seq("12345"),
            nationalAdditionalCodes = Seq("12345"),
            cusCode = "%^&%6789"
          )
          val result = route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(form.toSeq: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, cncErrorLength, "#combinedNomenclatureCode")
          checkErrorLink(page, 2, taricErrorLength, "#taricAdditionalCode_")
          checkErrorLink(page, 3, nacErrorInvalid, "#nationalAdditionalCode_")
          checkErrorLink(page, 4, descriptionErrorEmpty, "#descriptionOfGoods")
          checkErrorLink(page, 5, cusCodeErrorSpecialCharacters, "#cusCode")
          checkErrorLink(page, 6, statisticalErrorEmpty, "#statisticalValue")

          getElementByCss(page, "#error-message-combinedNomenclatureCode-input").text() must be(
            messages(cncErrorLength)
          )
          getElementByCss(page, "#error-message-taricAdditionalCode_-input").text() must be(messages(taricErrorLength))
          getElementByCss(page, "#error-message-nationalAdditionalCode_-input").text() must be(
            messages(nacErrorInvalid)
          )
          getElementByCss(page, "#error-message-descriptionOfGoods-input").text() must be(
            messages(descriptionErrorEmpty)
          )
          getElementByCss(page, "#error-message-cusCode-input").text() must be(messages(cusCodeErrorSpecialCharacters))
          getElementByCss(page, "#error-message-statisticalValue-input").text() must be(messages(statisticalErrorEmpty))
        }

      }

      "save data to the cache" in {
        withNewCaching(cacheModel)

        val userInput = addActionTypeToFormData(SaveAndContinue, correctItemTypeMap)
        route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(userInput.toSeq: _*)).get.futureValue

        verify(mockExportsCacheService).update(any[String], any[ExportsDeclaration])
      }

      "return 303 code" in {
        withNewCaching(cacheModel)

        val userInput = addActionTypeToFormData(SaveAndContinue, correctItemTypeMap)
        val result =
          route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(userInput.toSeq: _*)).get
        status(result) must be(SEE_OTHER)
      }

      "redirect to 'Add Package Information' page" when {

        "provided with mandatory data only" in {
          withNewCaching(cacheModel)

          val userInput = addActionTypeToFormData(SaveAndContinue, mandatoryFieldsOnlyItemTypeMap)
          val result =
            route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(userInput.toSeq: _*)).get

          redirectLocation(result) must be(
            Some(s"/customs-declare-exports/declaration/items/${cacheModel.items.head.id}/package-information")
          )
        }

        "provided with all data" in {
          withNewCaching(cacheModel)

          val userInput = addActionTypeToFormData(SaveAndContinue, correctItemTypeMap)
          val result =
            route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(userInput.toSeq: _*)).get

          redirectLocation(result) must be(
            Some(s"/customs-declare-exports/declaration/items/${cacheModel.items.head.id}/package-information")
          )
        }
      }
    }

    "form action is 'Add'" should {

      "display form page with error" when {

        "user tries to add duplicated TARIC" in {
          withNewCaching(cacheModel)

          val cachedData = ItemType("100", fourDigitsSequence(98), Seq(), "Description", None, None, "100")
          val item = ExportItem("id", itemType = Some(cachedData))
          val model = aDeclaration(withItem(item), withChoice("SMP"))
          withNewCaching(model)

          val form =
            buildItemTypeUrlEncodedInput(Add)(combinedNomenclatureCode = "100", taricAdditionalCodes = Seq("1010"))
          val result = route(app, postRequestFormUrlEncoded(uri(item), model.sessionId)(form.toSeq: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, taricErrorDuplicate, "#taricAdditionalCode_")

          getElementByCss(page, "#error-message-taricAdditionalCode_-input").text() must be(
            messages(taricErrorDuplicate)
          )
        }

        "user tries to add more than 99 TARIC" in {

          val cachedData = ItemType("100", fourDigitsSequence(99), Seq(), "Description", None, None, "100")
          val item = ExportItem("id", itemType = Some(cachedData))
          val model = aDeclaration(withItem(item), withChoice("SMP"))
          withNewCaching(model)

          val form =
            buildItemTypeUrlEncodedInput(Add)(combinedNomenclatureCode = "100", taricAdditionalCodes = Seq("2345"))
          val result = route(app, postRequestFormUrlEncoded(uri(item), model.sessionId)(form.toSeq: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, taricErrorMaxAmount, "#taricAdditionalCode_")

          getElementByCss(page, "#error-message-taricAdditionalCode_-input").text() must be(
            messages(taricErrorMaxAmount)
          )
        }

        "user tries to add duplicated NAC" in {
          withNewCaching(cacheModel)

          val cachedData = ItemType("100", Seq(), Seq("VATE"), "Description", None, None, "100")
          val item = ExportItem("id", itemType = Some(cachedData))
          val model = aDeclaration(withItem(item), withChoice("SMP"))
          withNewCaching(model)

          val form =
            buildItemTypeUrlEncodedInput(Add)(combinedNomenclatureCode = "100", nationalAdditionalCodes = Seq("VATE"))
          val result = route(app, postRequestFormUrlEncoded(uri(item), model.sessionId)(form.toSeq: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, nacErrorDuplicate, "#nationalAdditionalCode_")

          getElementByCss(page, "#error-message-nationalAdditionalCode_-input").text() must be(
            messages(nacErrorDuplicate)
          )
        }
      }

      "save updated data to the cache" when {

        "provided with TARIC" in {
          withNewCaching(cacheModel)

          val cachedItemType =
            ItemType("100", fourDigitsSequence(10), Seq("VATE", "VATR"), "Description", None, None, "100")
          val taricToAdd = "1234"
          val userInput = buildItemTypeUrlEncodedInput(Add)(taricAdditionalCodes = Seq(taricToAdd))
          val item = ExportItem("id", itemType = Some(cachedItemType))
          val model = aDeclaration(withItem(item), withChoice("SMP"))
          withNewCaching(model)

          route(app, postRequestFormUrlEncoded(uri(item), model.sessionId)(userInput.toSeq: _*)).get.futureValue

          val expectedUpdatedItemType =
            cachedItemType.copy(taricAdditionalCodes = cachedItemType.taricAdditionalCodes :+ taricToAdd)
          verify(mockExportsCacheService).update(any[String], any[ExportsDeclaration])
        }

        "provided with NAC" in {
          withNewCaching(cacheModel)

          val cachedItemType =
            ItemType("100", fourDigitsSequence(10), Seq("VATE", "VATR"), "Description", None, None, "100")
          val nacToAdd = "X442"
          val userInput = buildItemTypeUrlEncodedInput(Add)(nationalAdditionalCodes = Seq(nacToAdd))
          val item = ExportItem("id", itemType = Some(cachedItemType))
          val model = aDeclaration(withItem(item), withChoice("SMP"))
          withNewCaching(model)

          route(app, postRequestFormUrlEncoded(uri(item), model.sessionId)(userInput.toSeq: _*)).get.futureValue

          val expectedUpdatedItemType =
            cachedItemType.copy(nationalAdditionalCodes = cachedItemType.nationalAdditionalCodes :+ nacToAdd)
          verify(mockExportsCacheService).update(any[String], any[ExportsDeclaration])
        }

        "provided with both TARIC and NAC" in {
          withNewCaching(cacheModel)

          val cachedItemType =
            ItemType("100", fourDigitsSequence(1), Seq("VATE", "VATR"), "Description", None, None, "100")
          val taricToAdd = "1234"
          val nacToAdd = "X442"
          val userInput = buildItemTypeUrlEncodedInput(Add)(
            taricAdditionalCodes = Seq(taricToAdd),
            nationalAdditionalCodes = Seq(nacToAdd)
          )
          val item = ExportItem("id", itemType = Some(cachedItemType))
          val model = aDeclaration(withItem(item), withChoice("SMP"))
          withNewCaching(model)

          route(app, postRequestFormUrlEncoded(uri(item), model.sessionId)(userInput.toSeq: _*)).get.futureValue

          val expectedUpdatedItemType = cachedItemType.copy(
            taricAdditionalCodes = cachedItemType.taricAdditionalCodes :+ taricToAdd,
            nationalAdditionalCodes = cachedItemType.nationalAdditionalCodes :+ nacToAdd
          )
          verify(mockExportsCacheService).update(any[String], any[ExportsDeclaration])
        }
      }

      "return OK code" in {
        withNewCaching(cacheModel)

        val taricToAdd = "1234"
        val userInput = buildItemTypeUrlEncodedInput(Add)(taricAdditionalCodes = Seq(taricToAdd))

        val result =
          route(app, postRequestFormUrlEncoded(uri(exampleItem), cacheModel.sessionId)(userInput.toSeq: _*)).get

        status(result) must be(OK)
      }

      "refresh Item Type page" in {
        withNewCaching(cacheModel)

        val taricToAdd = "1234"
        val userInput = buildItemTypeUrlEncodedInput(Add)(taricAdditionalCodes = Seq(taricToAdd))

        val pageAddress = uri(exampleItem)
        val result = route(app, postRequestFormUrlEncoded(pageAddress, cacheModel.sessionId)(userInput.toSeq: _*)).get

        val page = contentAsString(result)
        page must include(pageAddress)
      }
    }

    "form action is 'Remove'" should {

      "save data without removed element to the cache" when {

        "removing the first element" in {
          val cachedItemType =
            ItemType("100", fourDigitsSequence(10), Seq.empty, "Description", None, None, "100")
          val userInput = addActionTypeToFormData(Remove(Seq(taricAdditionalCodesKey + "_0")), Map.empty)
          val item = ExportItem("id", itemType = Some(cachedItemType))
          val model = aDeclaration(withItem(item), withChoice("SMP"))
          withNewCaching(model)

          route(app, postRequestFormUrlEncoded(uri(item), model.sessionId)(userInput.toSeq: _*)).get.futureValue

          val expectedUpdatedItemType =
            cachedItemType.copy(taricAdditionalCodes = cachedItemType.taricAdditionalCodes.tail)
          verify(mockExportsCacheService).update(any[String], any[ExportsDeclaration])
        }

        "removing the last element" in {
          val cachedItemType =
            ItemType("100", fourDigitsSequence(10), Seq.empty, "Description", None, None, "100")
          val userInput = addActionTypeToFormData(Remove(Seq(taricAdditionalCodesKey + "_9")), Map.empty)
          val item = ExportItem("id", itemType = Some(cachedItemType))
          val model = aDeclaration(withItem(item), withChoice("SMP"))
          withNewCaching(model)

          route(app, postRequestFormUrlEncoded(uri(item), model.sessionId)(userInput.toSeq: _*)).get.futureValue

          val expectedUpdatedItemType =
            cachedItemType.copy(taricAdditionalCodes = cachedItemType.taricAdditionalCodes.init)
          verify(mockExportsCacheService).update(any[String], any[ExportsDeclaration])
        }

        "removing an element in the middle" in {
          val taricAddCodes = Seq("1111", "2222", "3333", "4444", "5555")
          val cachedItemType =
            ItemType("100", taricAddCodes, Seq.empty, "Description", None, None, "100")
          val userInput = addActionTypeToFormData(Remove(Seq(taricAdditionalCodesKey + "_2")), Map.empty)
          val item = ExportItem("id", itemType = Some(cachedItemType))
          val model = aDeclaration(withItem(item), withChoice("SMP"))
          withNewCaching(model)

          route(app, postRequestFormUrlEncoded(uri(item), model.sessionId)(userInput.toSeq: _*)).get.futureValue

          val expectedUpdatedItemType =
            cachedItemType.copy(taricAdditionalCodes = Seq("1111", "2222", "4444", "5555"))
          verify(mockExportsCacheService).update(any[String], any[ExportsDeclaration])
        }
      }

      "return OK code" in {
        val cachedItemType =
          ItemType("100", fourDigitsSequence(10), fourDigitsSequence(10), "Description", None, None, "100")
        val userInput = addActionTypeToFormData(Remove(Seq(taricAdditionalCodesKey + "_0")), Map.empty)
        val item = ExportItem("id", itemType = Some(cachedItemType))
        val model = aDeclaration(withItem(item), withChoice("SMP"))
        withNewCaching(model)

        val result = route(app, postRequestFormUrlEncoded(uri(item), model.sessionId)(userInput.toSeq: _*)).get

        status(result) must be(OK)
      }

      "refresh Item Type page" in {
        val cachedItemType =
          ItemType("100", fourDigitsSequence(10), fourDigitsSequence(10), "Description", None, None, "100")
        val userInput = addActionTypeToFormData(Remove(Seq(taricAdditionalCodesKey + "_0")), Map.empty)
        val item = ExportItem("id", itemType = Some(cachedItemType))
        val model = aDeclaration(withItem(item), withChoice("SMP"))
        withNewCaching(model)

        val pageAddress = uri(item)
        val result = route(app, postRequestFormUrlEncoded(pageAddress, model.sessionId)(userInput.toSeq: _*)).get

        val page = contentAsString(result)
        page must include(pageAddress)
      }
    }
  }

}

object ItemTypeControllerSpec {

  def fourDigitsSequence(number: Int): Seq[String] = Seq.tabulate(number)(n => (1000 + n).toString)

  def buildItemTypeUrlEncodedInput(formAction: FormAction)(
    combinedNomenclatureCode: String = "",
    taricAdditionalCodes: Seq[String] = Seq.empty,
    nationalAdditionalCodes: Seq[String] = Seq.empty,
    descriptionOfGoods: String = "",
    cusCode: String = "",
    statisticalValue: String = ""
  ): Map[String, String] =
    addActionTypeToFormData(
      formAction,
      Map(
        "combinedNomenclatureCode" -> combinedNomenclatureCode,
        "descriptionOfGoods" -> descriptionOfGoods,
        "cusCode" -> cusCode,
        "statisticalValue" -> statisticalValue
      ) ++ taricAdditionalCodes.zipWithIndex.map {
        case (code, idx) => taricAdditionalCodesKey + "[" + idx + "]" -> code
      } ++ nationalAdditionalCodes.zipWithIndex.map {
        case (code, idx) => nationalAdditionalCodesKey + "[" + idx + "]" -> code
      }
    )

  def addActionTypeToFormData(formAction: FormAction, formData: Map[String, String]): Map[String, String] =
    Map(createKeyValuePair(formAction)) ++ formData

  private def createKeyValuePair(action: FormAction): (String, String) =
    (action.label, action match {
      case Remove(keys) => keys.head
      case _            => ""
    })
}
