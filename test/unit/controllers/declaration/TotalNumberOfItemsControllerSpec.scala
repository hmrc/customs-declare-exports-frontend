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

import controllers.declaration.TotalNumberOfItemsController
import forms.declaration.TotalNumberOfItems
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.total_number_of_items

class TotalNumberOfItemsControllerSpec extends ControllerSpec with OptionValues {

  def theResponseForm(mockTotalNumberOfItemsPage: total_number_of_items): Form[TotalNumberOfItems] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[TotalNumberOfItems]])
    verify(mockTotalNumberOfItemsPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  trait SetUp {
    val mockTotalNumberOfItemsPage: total_number_of_items = mock[total_number_of_items]

    val controller = new TotalNumberOfItemsController(
      mockAuthAction,
      mockJourneyAction,
      navigator,
      stubMessagesControllerComponents(),
      mockTotalNumberOfItemsPage,
      mockExportsCacheService
    )(ec)
  }

  trait StandardSetUp extends SetUp {
    reset(mockTotalNumberOfItemsPage)
    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.STANDARD)))
    when(mockTotalNumberOfItemsPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  trait SimplifiedSetUp extends SetUp {
    reset(mockTotalNumberOfItemsPage)
    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.SIMPLIFIED)))
    when(mockTotalNumberOfItemsPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  "Total Number of Items controller" should {

    "return 200 (OK)" when {
      "during Standard journey" when {

        "display page method is invoked and cache is empty" in new StandardSetUp {

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          verify(mockTotalNumberOfItemsPage, times(1)).apply(any(), any())(any(), any())

          theResponseForm(mockTotalNumberOfItemsPage).value mustBe empty
        }

        "display page method is invoked and cache contains data" in new StandardSetUp {

          val totalPackage = "12"
          val totalNumberOfItems = TotalNumberOfItems(None, None, totalPackage)
          withNewCaching(aDeclaration(withTotalNumberOfItems(totalNumberOfItems)))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          verify(mockTotalNumberOfItemsPage, times(1)).apply(any(), any())(any(), any())

          theResponseForm(mockTotalNumberOfItemsPage).value mustNot be(empty)
          theResponseForm(mockTotalNumberOfItemsPage).value.value.totalPackage mustBe totalPackage
        }
      }

      "during Simplified journey" when {

        "display page method is invoked and cache is empty" in new SimplifiedSetUp {

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          verify(mockTotalNumberOfItemsPage, times(1)).apply(any(), any())(any(), any())

          theResponseForm(mockTotalNumberOfItemsPage).value mustBe empty
        }

        "display page method is invoked and cache contains data" in new SimplifiedSetUp {

          val totalPackage = "12"
          val totalNumberOfItems = TotalNumberOfItems(None, None, totalPackage)
          withNewCaching(aDeclaration(withTotalNumberOfItems(totalNumberOfItems), withType(DeclarationType.SIMPLIFIED)))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          verify(mockTotalNumberOfItemsPage, times(1)).apply(any(), any())(any(), any())

          theResponseForm(mockTotalNumberOfItemsPage).value mustNot be(empty)
          theResponseForm(mockTotalNumberOfItemsPage).value.value.totalPackage mustBe totalPackage
        }
      }
    }

    "return 400 (BAD_REQUEST)" when {
      "standard journey" when {
        "form is incorrect" in new StandardSetUp {

          val incorrectForm = Json.toJson(TotalNumberOfItems(Some("abc"), None, "12"))

          val result = controller.saveNoOfItems(Mode.Normal)(postRequest(incorrectForm))

          status(result) mustBe BAD_REQUEST
          verify(mockTotalNumberOfItemsPage, times(1)).apply(any(), any())(any(), any())
        }
      }
      "simplified journey" when {
        "form is incorrect" in new SimplifiedSetUp {

          val incorrectForm = Json.toJson(TotalNumberOfItems(Some("abc"), None, "12"))

          val result = controller.saveNoOfItems(Mode.Normal)(postRequest(incorrectForm))

          status(result) mustBe BAD_REQUEST
          verify(mockTotalNumberOfItemsPage, times(1)).apply(any(), any())(any(), any())
        }
      }
    }

    "return 303 (SEE_OTHER)" when {
      "Standard journey" when {

        "information provided by user are correct" in new StandardSetUp {

          val correctForm = Json.toJson(TotalNumberOfItems(None, None, "12"))

          val result = controller.saveNoOfItems(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.NatureOfTransactionController.displayPage()
          verify(mockTotalNumberOfItemsPage, times(0)).apply(any(), any())(any(), any())
        }
      }
      "Simplified journey" when {

        "information provided by user are correct" in new SimplifiedSetUp {

          val correctForm = Json.toJson(TotalNumberOfItems(None, None, "12"))

          val result = controller.saveNoOfItems(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.PreviousDocumentsController.displayPage()
          verify(mockTotalNumberOfItemsPage, times(0)).apply(any(), any())(any(), any())
        }
      }
    }
  }
}
