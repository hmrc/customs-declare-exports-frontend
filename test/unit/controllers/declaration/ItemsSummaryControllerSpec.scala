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

import controllers.declaration.ItemsSummaryController
import controllers.util.{Add, SaveAndContinue, SaveAndReturn}
import forms.declaration.FiscalInformation.AllowedFiscalInformationAnswers
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData, FiscalInformation}
import models.declaration.ExportItem
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.cache.ExportItemIdGeneratorService
import unit.base.ControllerSpec
import views.html.declaration.items_summary

class ItemsSummaryControllerSpec extends ControllerSpec with OptionValues {

  val mockItemsSummaryPage = mock[items_summary]
  val mockExportIdGeneratorService = mock[ExportItemIdGeneratorService]

  val controller = new ItemsSummaryController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    mockExportIdGeneratorService,
    stubMessagesControllerComponents(),
    mockItemsSummaryPage
  )(ec)

  val itemId = "ItemId12345"

  def theResponseForm: List[ExportItem] = {
    val captor = ArgumentCaptor.forClass(classOf[List[ExportItem]])
    verify(mockItemsSummaryPage).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
    when(mockItemsSummaryPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockExportIdGeneratorService.generateItemId()).thenReturn(itemId)
  }

  override protected def afterEach(): Unit = {
    reset(mockItemsSummaryPage)
    reset(mockExportIdGeneratorService)
    super.afterEach()
  }

  "Display" should {

    "return 200 (OK)" when {

      "display page method is invoked" in {

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        verify(mockItemsSummaryPage, times(1)).apply(any(), any(), any())(any(), any())

        theResponseForm mustBe empty
      }
    }
  }

  "Submit" when {
    "on Supplementary Journey" should {

      "return 303 (SEE_OTHER) and redirect to Procedure Codes page" when {
        "use add new item" in {
          val result = controller.submit(Mode.Normal)(postRequestAsFormUrlEncoded(Add.toString -> ""))

          status(result) mustBe SEE_OTHER
          verify(mockItemsSummaryPage, times(0)).apply(any(), any(), any())(any(), any())
          redirectLocation(result).value must endWith(s"/items/${itemId}/procedure-codes")
        }
      }

      "return 303 (SEE_OTHER) and continue" when {
        "user save and continues" in {
          val result = controller.submit(Mode.Normal)(postRequestAsFormUrlEncoded(SaveAndContinue.toString -> ""))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.WarehouseIdentificationController
            .displayPage(Mode.Normal)
        }

        "user save and returns" in {
          val result = controller.submit(Mode.Normal)(postRequestAsFormUrlEncoded(SaveAndReturn.toString -> ""))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.WarehouseIdentificationController
            .displayPage(Mode.Normal)
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "there is not completed item in the cache" in {

          val cachedData = aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(anItem(withItemId("id"))))
          withNewCaching(cachedData)

          val result = controller.submit(Mode.Normal)(postRequestAsFormUrlEncoded(SaveAndContinue.toString -> ""))

          status(result) mustBe BAD_REQUEST
          verify(mockItemsSummaryPage).apply(any(), any(), any())(any(), any())
        }
      }
    }

    "on Simplified Journey" should {

      val simplifiedJourneyItem = withItem(
        anItem(
          withItemId("id"),
          withProcedureCodes(),
          withFiscalInformation(FiscalInformation(AllowedFiscalInformationAnswers.yes)),
          withAdditionalFiscalReferenceData(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("GB", "12")))),
          withItemType(),
          withPackageInformation(),
          withAdditionalInformation("code", "description")
        )
      )

      "return 303 (SEE_OTHER) and redirect to Procedure Codes page" when {
        "use add new item" in {
          val cachedData = aDeclaration(withType(DeclarationType.SIMPLIFIED), simplifiedJourneyItem)
          withNewCaching(cachedData)
          val result = controller.submit(Mode.Normal)(postRequestAsFormUrlEncoded(Add.toString -> ""))

          status(result) mustBe SEE_OTHER
          verify(mockItemsSummaryPage, times(0)).apply(any(), any(), any())(any(), any())
          redirectLocation(result).value must endWith(s"/items/${itemId}/procedure-codes")
        }
      }

      "return 303 (SEE_OTHER) and continue" when {
        "user save and continues" in {

          val cachedData = aDeclaration(withType(DeclarationType.SIMPLIFIED), simplifiedJourneyItem)
          withNewCaching(cachedData)
          val result = controller.submit(Mode.Normal)(postRequestAsFormUrlEncoded(SaveAndContinue.toString -> ""))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.WarehouseIdentificationController
            .displayPage(Mode.Normal)
        }

        "user save and returns" in {

          val cachedData = aDeclaration(withType(DeclarationType.SIMPLIFIED), simplifiedJourneyItem)
          withNewCaching(cachedData)
          val result = controller.submit(Mode.Normal)(postRequestAsFormUrlEncoded(SaveAndReturn.toString -> ""))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.WarehouseIdentificationController
            .displayPage(Mode.Normal)
        }
      }
    }
  }
  "Remove" should {

    "return 303 (SEE_OTHER) and redirect to the same page during removing" when {

      "there is no item in declaration with requested Id" in {

        val result = controller.removeItem(Mode.Normal, itemId)(getRequest())

        status(result) mustBe SEE_OTHER
        verify(mockItemsSummaryPage, times(0)).apply(any(), any(), any())(any(), any())
      }

      "user successfully remove item" in {

        val cachedItem = ExportItem(itemId)
        val secondItem = ExportItem("123654")
        withNewCaching(aDeclaration(withItem(cachedItem), withItem(secondItem)))

        val result = controller.removeItem(Mode.Normal, itemId)(getRequest())

        status(result) mustBe SEE_OTHER
        verify(mockItemsSummaryPage, times(0)).apply(any(), any(), any())(any(), any())
        verify(mockExportsCacheService, times(1))
          .update(meq(aDeclaration(withItem(secondItem.copy(sequenceId = secondItem.sequenceId + 1)))))(any())
      }
    }
  }
}
