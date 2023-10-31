/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.helpers.SequenceIdHelper.valueOfEso
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.FiscalInformation.AllowedFiscalInformationAnswers
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData, FiscalInformation, WarehouseIdentification}
import mock.ErrorHandlerMocks
import models.DeclarationType._
import models.declaration.{CommodityMeasure, DeclarationStatus, ExportItem}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{GivenWhenThen, OptionValues}
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.cache.SubmissionBuilder
import views.html.declaration.declarationitems.{items_cannot_remove, items_remove_item}

import scala.concurrent.Future

class RemoveItemsSummaryControllerSpec
    extends ControllerWithoutFormSpec with OptionValues with ScalaFutures with GivenWhenThen with ErrorHandlerMocks with SubmissionBuilder {

  private val removeItemPage = mock[items_remove_item]
  private val cannotRemoveItemPage = mock[items_cannot_remove]

  private val controller = new RemoveItemsSummaryController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    mockCustomsDeclareExportsConnector,
    navigator,
    mockErrorHandler,
    stubMessagesControllerComponents(),
    cannotRemoveItemPage,
    removeItemPage
  )(ec)

  private val parentDeclarationId = "parentDecId"
  private val parentDeclaration = aDeclaration(withId(parentDeclarationId))
  private val submission = emptySubmission(parentDeclaration, "eori")

  private val itemId = "ItemId12345"
  private val exportItem: ExportItem = anItem(
    withSequenceId(1),
    withItemId(itemId),
    withProcedureCodes(),
    withFiscalInformation(FiscalInformation(AllowedFiscalInformationAnswers.yes)),
    withAdditionalFiscalReferenceData(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("GB", "12")))),
    withStatisticalValue(),
    withPackageInformation(),
    withAdditionalInformation("code", "description"),
    withCommodityMeasure(CommodityMeasure(None, Some(true), Some("100"), Some("100")))
  )
  private def itemPassedToRemoveItemView: ExportItem = {
    val captor = ArgumentCaptor.forClass(classOf[ExportItem])
    verify(removeItemPage).apply(any(), captor.capture(), any(), any())(any(), any())
    captor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    setupErrorHandler()
    authorizedUser()

    when(removeItemPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(cannotRemoveItemPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any())).thenReturn(Future.successful(Some(parentDeclaration)))
  }

  override protected def afterEach(): Unit = {
    reset(removeItemPage, cannotRemoveItemPage, mockExportsCacheService, mockCustomsDeclareExportsConnector)
    super.afterEach()
  }

  "displayRemoveItemConfirmationPage" should {
    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" when {

        "item can be removed" in {
          when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
            .thenReturn(Future.successful(Some(parentDeclaration)))

          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem), withParentDeclarationId(parentDeclarationId))
          withNewCaching(cachedData)

          val result = controller.displayRemoveItemConfirmationPage(itemId)(getRequest())

          status(result) mustBe OK
          verify(removeItemPage).apply(any(), any(), any(), any())(any(), any())
          itemPassedToRemoveItemView mustBe exportItem
        }

        "item cannot be removed" in {
          when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
            .thenReturn(Future.successful(Some(parentDeclaration)))

          when(mockCustomsDeclareExportsConnector.findSubmissionByLatestDecId(any())(any(), any()))
            .thenReturn(Future.successful(Some(submission)))

          val cachedData = aDeclaration(
            withType(request.declarationType),
            withItem(exportItem),
            withParentDeclarationId(parentDeclarationId),
            withStatus(DeclarationStatus.AMENDMENT_DRAFT)
          )
          withNewCaching(cachedData)

          val result = controller.displayRemoveItemConfirmationPage(itemId)(getRequest())

          status(result) mustBe OK
          verify(cannotRemoveItemPage)(any(), any(), any(), any())(any(), any())
          itemPassedToRemoveItemView mustBe exportItem
        }
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

      "return INTERNAL_SERVER_ERROR" when {

        "parentDecId does not exist" in {
          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem), withStatus(DeclarationStatus.AMENDMENT_DRAFT))
          withNewCaching(cachedData)

          val result = controller.displayRemoveItemConfirmationPage(itemId)(getRequest())

          status(result) mustBe INTERNAL_SERVER_ERROR
        }

        "parentDec cannot be found" in {
          when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
            .thenReturn(Future.successful(None))

          val cachedData = aDeclaration(
            withType(request.declarationType),
            withItem(exportItem),
            withParentDeclarationId(parentDeclarationId),
            withStatus(DeclarationStatus.AMENDMENT_DRAFT)
          )
          withNewCaching(cachedData)

          val result = controller.displayRemoveItemConfirmationPage(itemId)(getRequest())

          status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "removeItem" when {
    val cachedItem = ExportItem(sequenceId = 1, id = itemId)
    val secondItem = ExportItem(sequenceId = 2, id = "123654")

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
            val cachedDeclaration = aDeclaration(withType(request.declarationType), withItem(cachedItem), withItem(secondItem))
            valueOfEso[ExportItem](cachedDeclaration).value mustBe 2
            withNewCaching(cachedDeclaration)

            val result = controller.removeItem(itemId)(postRequest(removeItemForm))
            status(result) mustBe SEE_OTHER

            val declaration = theCacheModelUpdated
            declaration.items.size mustBe 1
            declaration.items must contain(secondItem)

            And("max sequence id value is unchanged")
            valueOfEso[ExportItem](declaration).value mustBe 2
          }

          "return 303 (SEE_OTHER) and redirect to Items Summary page" in {
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(cachedItem), withItem(secondItem)))

            val result = controller.removeItem(itemId)(postRequest(removeItemForm))

            status(result) mustBe SEE_OTHER
            thePageNavigatedTo mustBe routes.ItemsSummaryController.displayItemsSummaryPage

            val items = theCacheModelUpdated.items
            items.size mustBe 1
            items must contain(secondItem)
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
          withNewCaching(
            aDeclaration(withType(request.declarationType), withItem(cachedItem), withItem(secondItem), withParentDeclarationId(parentDeclarationId))
          )

          val incorrectRemoveItemForm = Json.obj("yesNo" -> "")

          val result = controller.removeItem(itemId)(postRequest(incorrectRemoveItemForm))

          status(result) mustBe BAD_REQUEST
          verify(removeItemPage).apply(any(), any(), any(), any())(any(), any())
        }

        "throw IllegalStateException if the Item has already been removed" in {
          withNewCaching(aDeclaration(withType(request.declarationType)))
          val incorrectRemoveItemForm = Json.obj("yesNo" -> "")

          val result = controller.removeItem(itemId)(postRequest(incorrectRemoveItemForm))

          status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }

    onJourney(STANDARD, OCCASIONAL, SUPPLEMENTARY, SIMPLIFIED) { request =>
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

            val model = theCacheModelUpdated
            model.items.size mustBe 1
            model.locations.warehouseIdentification mustBe None
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

            val model = theCacheModelUpdated
            model.items.size mustBe 1
            model.locations.warehouseIdentification mustBe Some(WarehouseIdentification(Some("id")))
          }
        }
      }
    }
  }
}
