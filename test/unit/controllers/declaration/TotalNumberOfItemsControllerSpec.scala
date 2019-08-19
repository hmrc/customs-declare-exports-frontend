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

import controllers.declaration.TotalNumberOfItemsController
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.TotalNumberOfItems
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

  val mockTotalNumberOfItemsPage = mock[total_number_of_items]

  val controller = new TotalNumberOfItemsController(
    mockAuthAction,
    mockJourneyAction,
    stubMessagesControllerComponents(),
    mockTotalNumberOfItemsPage,
    mockExportsCacheService
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withChoice(SupplementaryDec)))
    when(mockTotalNumberOfItemsPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockTotalNumberOfItemsPage)
  }

  def checkViewInteractions(noOfInvocations: Int = 1): Unit =
    verify(mockTotalNumberOfItemsPage, times(noOfInvocations)).apply(any())(any(), any())

  def theResponseForm: Form[TotalNumberOfItems] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[TotalNumberOfItems]])
    verify(mockTotalNumberOfItemsPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  "Total Number of Items controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {

        val result = controller.displayForm()(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" in {

        val totalPackage = "12"
        val totalNumberOfItems = TotalNumberOfItems(None, None, totalPackage)
        withNewCaching(aDeclaration(withTotalNumberOfItems(totalNumberOfItems)))

        val result = controller.displayForm()(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value mustNot be(empty)
        theResponseForm.value.value.totalPackage mustBe totalPackage
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {

        val incorrectForm = Json.toJson(TotalNumberOfItems(Some("abc"), None, "12"))

        val result = controller.saveNoOfItems()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
      }
    }

    "return 303 (SEE_OTHER)" when {

      "information provided by user are correct" in {

        val correctForm = Json.toJson(TotalNumberOfItems(None, None, "12"))

        val result = controller.saveNoOfItems()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        checkViewInteractions(0)
      }
    }
  }
}
