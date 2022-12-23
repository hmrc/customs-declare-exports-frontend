/*
 * Copyright 2022 HM Revenue & Customs
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

import base.ControllerWithoutFormSpec
import base.ExportsTestData.pc1040
import controllers.helpers.SupervisingCustomsOfficeHelper
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.FiscalInformation.AllowedFiscalInformationAnswers
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData, FiscalInformation, WarehouseIdentification}
import models.DeclarationType._
import models.declaration.{CommodityMeasure, ExportItem, ProcedureCodesData}
import models.{DeclarationType, ExportsDeclaration}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import play.api.data.{Form, FormError}
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.cache.ExportItemIdGeneratorService
import views.html.declaration.declarationitems.{items_add_item, items_remove_item, items_summary}

import scala.concurrent.Await
import scala.concurrent.duration._

class ItemsSummaryControllerSpec extends ControllerWithoutFormSpec with OptionValues with ScalaFutures {

  private val addItemPage = mock[items_add_item]
  private val itemsSummaryPage = mock[items_summary]
  private val removeItemPage = mock[items_remove_item]
  private val mockExportIdGeneratorService = mock[ExportItemIdGeneratorService]
  private val supervisingCustomsOfficeHelper = instanceOf[SupervisingCustomsOfficeHelper]

  private val controller = new ItemsSummaryController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    mockExportIdGeneratorService,
    stubMessagesControllerComponents(),
    addItemPage,
    itemsSummaryPage,
    removeItemPage,
    supervisingCustomsOfficeHelper
  )(ec)

  private val itemId = "ItemId12345"
  private val exportItem: ExportItem = anItem(
    withItemId(itemId),
    withProcedureCodes(),
    withFiscalInformation(FiscalInformation(AllowedFiscalInformationAnswers.yes)),
    withAdditionalFiscalReferenceData(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("GB", "12")))),
    withStatisticalValue(),
    withPackageInformation(),
    withAdditionalInformation("code", "description"),
    withCommodityMeasure(CommodityMeasure(None, Some(true), Some("100"), Some("100")))
  )

  private def formPassedToItemsSummaryView: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(itemsSummaryPage).apply(captor.capture(), any(), any())(any(), any())
    captor.getValue
  }

  private def itemsPassedToItemsSummaryView: List[ExportItem] = {
    val captor = ArgumentCaptor.forClass(classOf[List[ExportItem]])
    verify(itemsSummaryPage).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def itemsErrorsPassedToItemsSummaryView: Seq[FormError] = {
    val captor = ArgumentCaptor.forClass(classOf[Seq[FormError]])
    verify(itemsSummaryPage).apply(any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  private def itemPassedToRemoveItemView: ExportItem = {
    val captor = ArgumentCaptor.forClass(classOf[ExportItem])
    verify(removeItemPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(addItemPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
    when(itemsSummaryPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(removeItemPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockExportIdGeneratorService.generateItemId()).thenReturn(itemId)
  }

  override protected def afterEach(): Unit = {
    reset(addItemPage, itemsSummaryPage, removeItemPage, mockExportIdGeneratorService, mockExportsCacheService)
    super.afterEach()
  }

  "displayAddItemPage" should {

    onEveryDeclarationJourney() { request =>
      "call cache" in {
        withNewCaching(aDeclaration(withType(request.declarationType)))

        val result = controller.displayAddItemPage()(getRequest())
        status(result) mustBe OK
        verify(mockExportsCacheService).get(anyString())(any())
      }

      "return 200 (OK)" when {
        "there is no item in cache" in {
          withNewCaching(aDeclaration(withType(request.declarationType)))

          val result = controller.displayAddItemPage()(getRequest())
          status(result) mustBe OK
          verify(addItemPage).apply()(any(), any())
        }
      }

      "return 303 (SEE_OTHER) and redirect to displayItemsSummaryPage" when {
        "there are items in cache" in {
          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)

          val result = controller.displayAddItemPage()(getRequest())

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe routes.ItemsSummaryController.displayItemsSummaryPage
        }
      }
    }
  }

  "addFirstItem" should {

    onEveryDeclarationJourney() { request =>
      "call Navigator" in {
        withNewCaching(aDeclaration(withType(request.declarationType)))

        val result = controller.addFirstItem()(postRequest(Json.obj()))
        status(result) mustBe SEE_OTHER

        verify(navigator).continueTo(any())(any())
      }

      "return 303 (SEE_OTHER) and redirect to Procedure Codes page" in {
        withNewCaching(aDeclaration(withType(request.declarationType)))

        val result = controller.addFirstItem()(postRequest(Json.obj()))
        status(result) mustBe SEE_OTHER
        thePageNavigatedTo mustBe routes.ProcedureCodesController.displayPage(itemId)

        theCacheModelUpdated.items.size mustBe 1
      }
    }
  }

  "displayItemsSummaryPage" should {
    onEveryDeclarationJourney() { request =>
      "call cache" in {
        withNewCaching(aDeclaration(withType(request.declarationType)))

        val result = controller.displayItemsSummaryPage()(getRequest())
        status(result) mustBe SEE_OTHER

        verify(mockExportsCacheService).get(anyString())(any())
      }

      "return 200 (OK)" when {
        "there are items in cache" in {
          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)

          val result = controller.displayItemsSummaryPage()(getRequest())
          status(result) mustBe OK

          verify(itemsSummaryPage).apply(any(), any(), any())(any(), any())
          itemsPassedToItemsSummaryView mustBe Seq(exportItem)
        }
      }

      "return 303 (SEE_OTHER) and redirect to displayAddItemPage" when {
        "there is no item in cache" in {
          withNewCaching(aDeclaration(withType(request.declarationType)))

          val result = controller.displayItemsSummaryPage()(getRequest())

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe routes.ItemsSummaryController.displayAddItemPage
        }
      }

      "remove un-used item" when {
        "there is unused item in cache" in {
          val emptyItem = anItem()
          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem), withItem(emptyItem))
          withNewCaching(cachedData)

          val result = controller.displayItemsSummaryPage()(getRequest())

          status(result) mustBe OK
          verify(itemsSummaryPage).apply(any(), any(), any())(any(), any())
          itemsPassedToItemsSummaryView mustBe Seq(exportItem)
        }
      }
    }
  }

  "submit" when {

    onEveryDeclarationJourney() { request =>
      "user wants to add another item" should {

        "call Navigator" in {
          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)
          val answerForm = Json.obj("yesNo" -> YesNoAnswers.yes)

          val result = controller.submit()(postRequest(answerForm))
          status(result) mustBe SEE_OTHER

          verify(navigator).continueTo(any())(any())
        }

        "return 303 (SEE_OTHER) and redirect to Procedure Codes page" in {
          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)
          val answerForm = Json.obj("yesNo" -> YesNoAnswers.yes)

          val result = controller.submit()(postRequest(answerForm))
          status(result) mustBe SEE_OTHER

          thePageNavigatedTo mustBe routes.ProcedureCodesController.displayPage(itemId)

          verify(navigator).continueTo(any())(any())
        }
      }

      "user does not want to add another item" should {
        "return 303 (SEE_OTHER) and redirect to next page" in {
          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)
          val answerForm = Json.obj("yesNo" -> YesNoAnswers.no)

          val result = controller.submit()(postRequest(answerForm))

          status(result) mustBe SEE_OTHER
          request.declarationType match {
            case DeclarationType.SIMPLIFIED | DeclarationType.OCCASIONAL =>
              thePageNavigatedTo mustBe routes.SupervisingCustomsOfficeController.displayPage
            case _ => thePageNavigatedTo mustBe routes.TransportLeavingTheBorderController.displayPage
          }

          verify(navigator).continueTo(any())(any())
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "there is no answer from user" in {
          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)

          val result = controller.submit()(postRequest(Json.obj()))

          status(result) mustBe BAD_REQUEST
          formPassedToItemsSummaryView.errors mustNot be(empty)
        }

        "there is incomplete item in the cache" in {
          val cachedData = aDeclaration(withType(request.declarationType), withItem(anItem(withItemId("id"))))
          withNewCaching(cachedData)
          val answerForm = Json.obj("yesNo" -> YesNoAnswers.no)

          val result = controller.submit()(postRequest(answerForm))

          status(result) mustBe BAD_REQUEST
          itemsErrorsPassedToItemsSummaryView mustNot be(empty)
        }
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { request =>
      "user does not want to add another item" should {
        "return 303 (SEE_OTHER) and redirect to Transport Leaving the Border page" in {
          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)
          val answerForm = Json.obj("yesNo" -> YesNoAnswers.no)

          val result = controller.submit()(postRequest(answerForm))

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe routes.TransportLeavingTheBorderController.displayPage
        }
      }
    }

    onJourney(SIMPLIFIED, OCCASIONAL) { request =>
      "return 303 (SEE_OTHER)" in {
        val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
        withNewCaching(cachedData)
        val answerForm = Json.obj("yesNo" -> YesNoAnswers.no)

        val result = controller.submit()(postRequest(answerForm))

        status(result) mustBe SEE_OTHER
        thePageNavigatedTo mustBe routes.SupervisingCustomsOfficeController.displayPage
      }

      "return 303 (SEE_OTHER) and redirect to Warehouse Identification page when procedure code requires warehouse id" in {
        val cachedData = aDeclaration(
          withType(request.declarationType),
          withItem(exportItem.copy(procedureCodes = Some(ProcedureCodesData(Some("0007"), Seq("123")))))
        )
        withNewCaching(cachedData)
        val answerForm = Json.obj("yesNo" -> YesNoAnswers.no)

        val result = controller.submit()(postRequest(answerForm))

        status(result) mustBe SEE_OTHER
        thePageNavigatedTo mustBe routes.WarehouseIdentificationController.displayPage
      }

      "return 303 (SEE_OTHER) and redirect to Express Consignment page" when {
        "cache contains '1040' as procedure code and '000' as APC" in {
          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem.copy(procedureCodes = pc1040)))
          withNewCaching(cachedData)
          val answerForm = Json.obj("yesNo" -> YesNoAnswers.no)

          val result = controller.submit()(postRequest(answerForm))

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe routes.ExpressConsignmentController.displayPage
        }
      }
    }
  }

  "displayRemoveItemConfirmationPage" should {
    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" in {
        val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
        withNewCaching(cachedData)

        val result = controller.displayRemoveItemConfirmationPage(itemId)(getRequest())

        status(result) mustBe OK
        verify(removeItemPage).apply(any(), any())(any(), any())
        itemPassedToRemoveItemView mustBe exportItem
      }

      "return 303 (SEE_OTHER) and redirect to Items Summary page" when {
        "provided with itemId not matching any Item in cache" in {
          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)

          val result = controller.displayRemoveItemConfirmationPage("someItemId")(getRequest())

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe routes.ItemsSummaryController.displayItemsSummaryPage
        }
      }
    }
  }

  "removeItem" when {
    val cachedItem = ExportItem(itemId)
    val secondItem = ExportItem("123654")

    def declarationPassedToUpdateCache: ExportsDeclaration = {
      val captor = ArgumentCaptor.forClass(classOf[ExportsDeclaration])
      verify(mockExportsCacheService).update(captor.capture())(any())
      captor.getValue
    }

    onEveryDeclarationJourney() { request =>
      "user wants to remove an Item" when {
        val removeItemForm = Json.obj("yesNo" -> YesNoAnswers.yes)

        "there is no Item in declaration with requested Id" should {

          "not call ExportsCacheService update method" in {
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(cachedItem), withItem(secondItem)))

            val result = controller.removeItem("someId123")(postRequest(removeItemForm))
            status(result) mustBe SEE_OTHER

            verifyTheCacheIsUnchanged()
          }

          "return 303 (SEE_OTHER) and redirect to Items Summary page" in {
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(cachedItem), withItem(secondItem)))

            val result = controller.removeItem("someId123")(postRequest(removeItemForm))

            status(result) mustBe SEE_OTHER
            thePageNavigatedTo mustBe routes.ItemsSummaryController.displayItemsSummaryPage
          }
        }

        "there is Item in declaration with requested Id" should {

          "remove the Item from cache" in {
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(cachedItem), withItem(secondItem)))

            val result = controller.removeItem(itemId)(postRequest(removeItemForm))
            status(result) mustBe SEE_OTHER

            val items = declarationPassedToUpdateCache.items
            items.size mustBe 1
            items must contain(secondItem.copy(sequenceId = secondItem.sequenceId + 1))
          }

          "return 303 (SEE_OTHER) and redirect to Items Summary page" in {
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(cachedItem), withItem(secondItem)))

            val result = controller.removeItem(itemId)(postRequest(removeItemForm))

            status(result) mustBe SEE_OTHER
            thePageNavigatedTo mustBe routes.ItemsSummaryController.displayItemsSummaryPage

            val items = declarationPassedToUpdateCache.items
            items.size mustBe 1
            items must contain(secondItem.copy(sequenceId = secondItem.sequenceId + 1))
          }
        }
      }

      "user does not want to remove an Item" should {
        val removeItemForm = Json.obj("yesNo" -> YesNoAnswers.no)

        "redirect to Items Summary page" in {
          withNewCaching(aDeclaration(withType(request.declarationType), withItem(cachedItem), withItem(secondItem)))

          val result = controller.removeItem(itemId)(postRequest(removeItemForm))

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe routes.ItemsSummaryController.displayItemsSummaryPage

          verifyTheCacheIsUnchanged()
        }
      }

      "provided with empty form" should {

        "return 400 (BAD_REQUEST)" in {
          withNewCaching(aDeclaration(withType(request.declarationType), withItem(cachedItem), withItem(secondItem)))
          val incorrectRemoveItemForm = Json.obj("yesNo" -> "")

          val result = controller.removeItem(itemId)(postRequest(incorrectRemoveItemForm))

          status(result) mustBe BAD_REQUEST
          verify(removeItemPage).apply(any(), any())(any(), any())
        }

        "throw IllegalStateException if the Item has already been removed" in {
          withNewCaching(aDeclaration(withType(request.declarationType)))
          val incorrectRemoveItemForm = Json.obj("yesNo" -> "")

          intercept[IllegalStateException] {
            Await.result(controller.removeItem(itemId)(postRequest(incorrectRemoveItemForm)), 5.seconds)
          }
        }
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      "warehouse identification answer is updated" when {
        val removeItemForm = Json.obj("yesNo" -> YesNoAnswers.yes)

        val warehouseItem = anItem(withItemId("warehouseItem"), withProcedureCodes(Some("0007"), Seq("000")))
        val declaration = aDeclaration(
          withType(request.declarationType),
          withItem(cachedItem),
          withItem(warehouseItem),
          withWarehouseIdentification(Some(WarehouseIdentification(Some("id"))))
        )

        "user removes item contain 'warehouse procedure code'" should {
          "remove the Item from cache" in {
            withNewCaching(declaration)

            val result = controller.removeItem("warehouseItem")(postRequest(removeItemForm))
            status(result) mustBe SEE_OTHER

            val items = declarationPassedToUpdateCache.items
            items.size mustBe 1

            declarationPassedToUpdateCache.locations.warehouseIdentification mustBe None
          }
        }
      }
    }

    onJourney(CLEARANCE) { request =>
      "warehouse identification answer is retained" when {
        val removeItemForm = Json.obj("yesNo" -> YesNoAnswers.yes)

        val warehouseItem = anItem(withItemId("warehouseItem"), withProcedureCodes(Some("0007"), Seq("000")))
        val declaration = aDeclaration(
          withType(request.declarationType),
          withItem(cachedItem),
          withItem(warehouseItem),
          withWarehouseIdentification(Some(WarehouseIdentification(Some("id"))))
        )

        "user removes item contain 'warehouse procedure code'" should {
          "remove the Item from cache" in {
            withNewCaching(declaration)

            val result = controller.removeItem("warehouseItem")(postRequest(removeItemForm))
            status(result) mustBe SEE_OTHER

            val items = declarationPassedToUpdateCache.items
            items.size mustBe 1

            declarationPassedToUpdateCache.locations.warehouseIdentification mustBe Some(WarehouseIdentification(Some("id")))
          }
        }
      }
    }
  }
}
