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

package unit.controllers.declaration

import controllers.declaration.ItemTypeController
import controllers.util.Remove
import forms.declaration.ItemTypeForm
import models.Mode
import models.declaration.ItemType
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.cache.ExportItemIdGeneratorService
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.item_type

class ItemTypeControllerSpec extends ControllerSpec with ErrorHandlerMocks with OptionValues {

  val mockItemTypePage = mock[item_type]

  val controller = new ItemTypeController(
    mockAuthAction,
    mockJourneyAction,
    mockErrorHandler,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockItemTypePage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(mockItemTypePage.apply(any(), any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  val itemId = new ExportItemIdGeneratorService().generateItemId()

  override protected def afterEach(): Unit = {
    reset(mockItemTypePage)
    super.afterEach()
  }

  def theResponseForm: Form[ItemTypeForm] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ItemTypeForm]])
    verify(mockItemTypePage).apply(any(), any(), captor.capture(), any(), any())(any(), any())
    captor.getValue
  }

  def validateCache(itemType: ItemType) = {
    val cacheItemType = theCacheModelUpdated.items.head.itemType.getOrElse(ItemType.empty)
    cacheItemType mustBe (itemType)
  }

  "Item Type Controller" should {

    "return 200 (OK)" when {

      "item type exists in the cache and item type is defined" in {

        withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId), withItemType()))))

        val result = controller.displayPage(Mode.Normal, itemId)(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustNot be(empty)
      }

      "item type exists in the cache and item type is not defined" in {

        withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId)))))

        val result = controller.displayPage(Mode.Normal, itemId)(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "correct item type is added for add TARIC code and declaration model exist in the cache" in {

        withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId)))))

        val correctForm = Seq(
          ("combinedNomenclatureCode", "nomCode"),
          ("taricAdditionalCode", "1234"),
          ("nationalAdditionalCode", "VATE"),
          ("descriptionOfGoods", "description"),
          ("cusCode", "12345678"),
          ("unDangerousGoodsCode", "4321"),
          ("statisticalValue", "999"),
          addActionUrlEncoded(ItemTypeForm.taricAdditionalCodeKey)
        )

        val result = controller.submitItemType(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) mustBe OK
        verify(mockItemTypePage, times(1)).apply(any(), any(), any(), any(), any())(any(), any())

        validateCache(
          ItemType(
            combinedNomenclatureCode = "nomCode",
            taricAdditionalCodes = Seq("1234"),
            nationalAdditionalCodes = Seq.empty, // NOT added to the cache model after an Add for TARIC codes - see bug CEDS-1094
            descriptionOfGoods = "description",
            cusCode = Some("12345678"),
            unDangerousGoodsCode = Some("4321"),
            statisticalValue = "999"
          )
        )
      }

      "correct item type is added for add National code and declaration model exist in the cache" in {

        withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId)))))

        val correctForm = Seq(
          ("combinedNomenclatureCode", "nomCode2"),
          ("taricAdditionalCode", "5356"),
          ("nationalAdditionalCode", "X611"),
          ("descriptionOfGoods", "description2"),
          ("cusCode", "56353789"),
          ("unDangerousGoodsCode", "6468"),
          ("statisticalValue", "435"),
          addActionUrlEncoded(ItemTypeForm.nationalAdditionalCodeKey)
        )

        val result = controller.submitItemType(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) mustBe OK
        verify(mockItemTypePage, times(1)).apply(any(), any(), any(), any(), any())(any(), any())

        validateCache(
          ItemType(
            combinedNomenclatureCode = "nomCode2",
            taricAdditionalCodes = Seq.empty, // NOT added to the cache model after an Add for TARIC codes - see bug CEDS-1094
            nationalAdditionalCodes = Seq("X611"),
            descriptionOfGoods = "description2",
            cusCode = Some("56353789"),
            unDangerousGoodsCode = Some("6468"),
            statisticalValue = "435"
          )
        )
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "there is not item in the cache during submitting" in {

        withNewCaching(aDeclaration())

        val correctForm =
          Json.toJson(ItemType("code", Seq("code"), Seq("code"), "description", Some("code"), Some("code"), "1234"))

        val result = controller.submitItemType(Mode.Normal, itemId)(postRequest(correctForm))

        status(result) mustBe BAD_REQUEST
        verify(mockItemTypePage, times(0)).apply(any(), any(), any(), any(), any())(any(), any())
      }

      "form action from user is incorrect" in {

        withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId)))))

        val wrongAction = Seq(
          ("combinedNomenclatureCode", "code"),
          ("taricAdditionalCode", ""),
          ("nationalAdditionalCode", ""),
          ("descriptionOfGoods", "description"),
          ("cusCode", ""),
          ("unDangerousGoodsCode", ""),
          ("statisticalValue", "value"),
          ("WrongAction", "")
        )

        val result = controller.submitItemType(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockItemTypePage, times(0)).apply(any(), any(), any(), any(), any())(any(), any())
      }

      "information from user is incorrect during adding" in {

        withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId)))))

        val incorrectForm = Seq(
          ("combinedNomenclatureCode", "!@#$$%"),
          ("taricAdditionalCode", "!@#$$%"),
          ("nationalAdditionalCode", "!@#$$%"),
          ("descriptionOfGoods", "!@#$$%"),
          ("cusCode", "!@#$$%"),
          ("unDangerousGoodsCode", "!@#$$%"),
          ("statisticalValue", "!@#$$%"),
          addActionUrlEncoded(ItemTypeForm.taricAdditionalCodeKey)
        )

        val result = controller.submitItemType(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockItemTypePage, times(1)).apply(any(), any(), any(), any(), any())(any(), any())
      }

      "information from user is incorrect during saving" in {

        withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId)))))

        val incorrectForm = Seq(
          ("combinedNomenclatureCode", ""),
          ("taricAdditionalCode", "!@#$$%"),
          ("nationalAdditionalCode", ""),
          ("descriptionOfGoods", ""),
          ("cusCode", ""),
          ("unDangerousGoodsCode", ""),
          ("statisticalValue", ""),
          saveAndContinueActionUrlEncoded
        )

        val result = controller.submitItemType(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockItemTypePage, times(1)).apply(any(), any(), any(), any(), any())(any(), any())
      }
    }

    "return 303 (SEE_OTHER)" when {

      "item doesn't exists in the cache" in {

        withNewCaching(aDeclaration())

        val result = controller.displayPage(Mode.Normal, itemId)(getRequest())

        status(result) mustBe SEE_OTHER
        verify(mockItemTypePage, times(0)).apply(any(), any(), any(), any(), any())(any(), any())
      }

      "correct item type is added during saving" in {

        withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId)))))

        val correctForm = Seq(
          ("combinedNomenclatureCode", "code"),
          ("taricAdditionalCode", "1234"),
          ("nationalAdditionalCode", "VATR"),
          ("descriptionOfGoods", "description"),
          ("cusCode", ""),
          ("unDangerousGoodsCode", ""),
          ("statisticalValue", "1234"),
          saveAndContinueActionUrlEncoded
        )

        val result = controller.submitItemType(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.PackageInformationController
          .displayPage(Mode.Normal, itemId)
        verify(mockItemTypePage, times(0)).apply(any(), any(), any(), any(), any())(any(), any())
      }

      "item type has been removed and there is exisitng data in cache" in {

        withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId), withItemType(taricAdditionalCodes = Seq("1234"))))))

        val removeAction = (Remove.toString, "taricAdditionalCode_0")

        val result = controller.submitItemType(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(removeAction))

        status(result) mustBe OK
        verify(mockItemTypePage, times(1)).apply(any(), any(), any(), any(), any())(any(), any())
      }
    }
  }
}
