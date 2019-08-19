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
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.cache.{ExportItem, ExportItemIdGeneratorService}
import unit.base.ControllerSpec
import views.html.declaration.items_summary

class ItemsSummaryControllerSpec extends ControllerSpec with OptionValues {

  val mockItemsSummaryPage = mock[items_summary]
  val mockExportIdGeneratorService = mock[ExportItemIdGeneratorService]

  val controller = new ItemsSummaryController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    mockExportIdGeneratorService,
    stubMessagesControllerComponents(),
    mockItemsSummaryPage
  )(ec)

  val itemId = "ItemId12345"

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withChoice(SupplementaryDec)))
    when(mockItemsSummaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockExportIdGeneratorService.generateItemId()).thenReturn(itemId)
  }

  override protected def afterEach(): Unit = {
    reset(mockItemsSummaryPage)
    reset(mockExportIdGeneratorService)
    super.afterEach()
  }

  def checkViewInteractions(noOfInvocations: Int = 1): Unit =
    verify(mockItemsSummaryPage, times(noOfInvocations)).apply(any())(any(), any())

  def theResponseForm: List[ExportItem] = {
    val captor = ArgumentCaptor.forClass(classOf[List[ExportItem]])
    verify(mockItemsSummaryPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  "Items Summary controller" should {

    "return 200 (OK)" when {

      "display page method is invoked" in {

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm mustBe empty
      }
    }

    "return 303 (SEE_OTHER) and redirect to Procedure Codes page" when {

      "use add new item" in {

        val result = controller.addItem()(getRequest())

        status(result) mustBe SEE_OTHER
        checkViewInteractions(0)
        redirectLocation(result).value must include(s"/items/${itemId}/procedure-codes")
      }
    }

    "return 303 (SEE_OTHER) and redirect to the same page during removing" when {

      "there is no item in declaration with requested Id" in {

        val result = controller.removeItem(itemId)(getRequest())

        status(result) mustBe SEE_OTHER
        checkViewInteractions(0)
      }

      "user successfully remove item" in {

        val cachedItem = ExportItem(itemId)
        val secondItem = ExportItem("123654")
        withNewCaching(aDeclaration(withItem(cachedItem), withItem(secondItem)))

        val result = controller.removeItem(itemId)(getRequest())

        status(result) mustBe SEE_OTHER
        checkViewInteractions(0)
        verify(mockExportsCacheService, times(1)).update(any(), meq(aDeclaration(withItem(secondItem.copy(sequenceId = secondItem.sequenceId + 1)))))
      }
    }
  }
}
