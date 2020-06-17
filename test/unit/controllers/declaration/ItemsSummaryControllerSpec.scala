/*
 * Copyright 2020 HM Revenue & Customs
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

import controllers.declaration.ItemsSummaryController
import controllers.util.SaveAndContinue
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.FiscalInformation.AllowedFiscalInformationAnswers
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData, CommodityMeasure, FiscalInformation}
import models.Mode
import models.declaration.ExportItem
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString, eq => meq}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import play.api.data.{Form, FormError}
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.cache.ExportItemIdGeneratorService
import unit.base.ControllerWithoutFormSpec
import views.html.declaration.declarationitems.{items_add_item, items_summary}

class ItemsSummaryControllerSpec extends ControllerWithoutFormSpec with OptionValues with ScalaFutures {

  private val addItemPage = mock[items_add_item]
  private val itemsSummaryPage = mock[items_summary]
  private val mockExportIdGeneratorService = mock[ExportItemIdGeneratorService]

  private val controller = new ItemsSummaryController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    mockExportIdGeneratorService,
    stubMessagesControllerComponents(),
    addItemPage,
    itemsSummaryPage
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
    withCommodityMeasure(CommodityMeasure(None, Some("100"), Some("100")))
  )

  private def formPassedToItemsSummaryView: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(itemsSummaryPage).apply(any(), captor.capture(), any(), any())(any(), any())
    captor.getValue
  }

  private def itemsPassedToItemsSummaryView: List[ExportItem] = {
    val captor = ArgumentCaptor.forClass(classOf[List[ExportItem]])
    verify(itemsSummaryPage).apply(any(), any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def itemsErrorsPassedToItemsSummaryView: Seq[FormError] = {
    val captor = ArgumentCaptor.forClass(classOf[Seq[FormError]])
    verify(itemsSummaryPage).apply(any(), any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(addItemPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(itemsSummaryPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockExportIdGeneratorService.generateItemId()).thenReturn(itemId)
  }

  override protected def afterEach(): Unit = {
    reset(addItemPage, itemsSummaryPage, mockExportIdGeneratorService)
    super.afterEach()
  }

  "displayAddItemPage" should {

    onEveryDeclarationJourney() { request =>
      "call cache" in {

        withNewCaching(aDeclaration(withType(request.declarationType)))

        controller.displayAddItemPage(Mode.Normal)(getRequest()).futureValue

        verify(mockExportsCacheService).get(anyString())(any())
      }

      "return 200 (OK)" when {
        "there is no item in cache" in {

          withNewCaching(aDeclaration(withType(request.declarationType)))

          val result = controller.displayAddItemPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          verify(addItemPage).apply(any())(any(), any())
        }
      }

      "return 303 (SEE_OTHER) and redirect to displayItemsSummaryPage" when {
        "there are items in cache" in {

          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)

          val result = controller.displayAddItemPage(Mode.Normal)(getRequest())

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe controllers.declaration.routes.ItemsSummaryController.displayItemsSummaryPage(Mode.Normal)
        }
      }
    }
  }

  "addFirstItem" should {

    onEveryDeclarationJourney() { request =>

      "call Navigator" in {

        withNewCaching(aDeclaration(withType(request.declarationType)))

        controller.addFirstItem(Mode.Normal)(postRequest(Json.obj())).futureValue

        verify(navigator).continueTo(any[Mode], any(), any[Boolean])(any(), any())
      }

      "return 303 (SEE_OTHER) and redirect to Procedure Codes page" in {

        withNewCaching(aDeclaration(withType(request.declarationType)))

        val result = controller.addFirstItem(Mode.Normal)(postRequest(Json.obj()))

        status(result) mustBe SEE_OTHER
        thePageNavigatedTo mustBe controllers.declaration.routes.ProcedureCodesController.displayPage(Mode.Normal, itemId)
      }
    }
  }

  "displayItemsSummaryPage" should {

    onEveryDeclarationJourney() { request =>
      "call cache" in {

        withNewCaching(aDeclaration(withType(request.declarationType)))

        controller.displayItemsSummaryPage(Mode.Normal)(getRequest()).futureValue

        verify(mockExportsCacheService).get(anyString())(any())
      }

      "return 200 (OK)" when {
        "there are items in cache" in {

          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)

          val result = controller.displayItemsSummaryPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          verify(itemsSummaryPage).apply(any(), any(), any(), any())(any(), any())
          itemsPassedToItemsSummaryView mustBe Seq(exportItem)
        }
      }

      "return 303 (SEE_OTHER) and redirect to displayAddItemPage" when {
        "there is no item in cache" in {

          withNewCaching(aDeclaration(withType(request.declarationType)))

          val result = controller.displayItemsSummaryPage(Mode.Normal)(getRequest())

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe controllers.declaration.routes.ItemsSummaryController.displayAddItemPage(Mode.Normal)
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

          controller.submit(Mode.Normal)(postRequest(answerForm)).futureValue

          verify(navigator).continueTo(any[Mode], any(), any[Boolean])(any(), any())
        }

        "return 303 (SEE_OTHER) and redirect to Procedure Codes page" in {

          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)
          val answerForm = Json.obj("yesNo" -> YesNoAnswers.yes)

          val result = controller.submit(Mode.Normal)(postRequest(answerForm))

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe controllers.declaration.routes.ProcedureCodesController.displayPage(Mode.Normal, itemId)
        }
      }

      "user does not want to add another item" should {

        "call Navigator" in {

          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)
          val answerForm = Json.obj("yesNo" -> YesNoAnswers.no)

          controller.submit(Mode.Normal)(postRequest(answerForm)).futureValue

          verify(navigator).continueTo(any[Mode], any(), any[Boolean])(any(), any())
        }

        "return 303 (SEE_OTHER) and redirect to Warehouse Identification page" in {

          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)
          val answerForm = Json.obj("yesNo" -> YesNoAnswers.no)

          val result = controller.submit(Mode.Normal)(postRequest(answerForm))

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe controllers.declaration.routes.WarehouseIdentificationController.displayPage(Mode.Normal)
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "there is no answer from user" in {

          val cachedData = aDeclaration(withType(request.declarationType), withItem(exportItem))
          withNewCaching(cachedData)

          val result = controller.submit(Mode.Normal)(postRequest(Json.obj()))

          status(result) mustBe BAD_REQUEST
          formPassedToItemsSummaryView.errors mustNot be(empty)
        }

        "there is incomplete item in the cache" in {

          val cachedData = aDeclaration(withType(request.declarationType), withItem(anItem(withItemId("id"))))
          withNewCaching(cachedData)

          val result = controller.submit(Mode.Normal)(postRequestAsFormUrlEncoded(SaveAndContinue.toString -> ""))

          status(result) mustBe BAD_REQUEST
          itemsErrorsPassedToItemsSummaryView mustNot be(empty)
        }
      }
    }
  }

  "remove" should {

    onEveryDeclarationJourney() { request =>
      "return 303 (SEE_OTHER) and redirect to the same page during removing" when {

        "there is no item in declaration with requested Id" in {

          withNewCaching(aDeclaration(withType(request.declarationType)))

          val result = controller.removeItem(Mode.Normal, itemId)(getRequest())

          status(result) mustBe SEE_OTHER
          verify(itemsSummaryPage, times(0)).apply(any(), any(), any(), any())(any(), any())
        }

        "user successfully remove item" in {

          withNewCaching(aDeclaration(withType(request.declarationType)))

          val cachedItem = ExportItem(itemId)
          val secondItem = ExportItem("123654")
          withNewCaching(aDeclaration(withItem(cachedItem), withItem(secondItem)))

          val result = controller.removeItem(Mode.Normal, itemId)(getRequest())

          status(result) mustBe SEE_OTHER
          verify(itemsSummaryPage, times(0)).apply(any(), any(), any(), any())(any(), any())
          verify(mockExportsCacheService, times(1))
            .update(meq(aDeclaration(withItem(secondItem.copy(sequenceId = secondItem.sequenceId + 1)))))(any())
        }
      }
    }
  }
}
