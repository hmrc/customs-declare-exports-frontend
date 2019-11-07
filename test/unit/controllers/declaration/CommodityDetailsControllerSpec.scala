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

import controllers.declaration.CommodityDetailsController
import forms.declaration.CommodityDetails
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
import views.html.declaration.commodity_details

class CommodityDetailsControllerSpec extends ControllerSpec with OptionValues {

  val mockCommodityDetailsPage = mock[commodity_details]

  val controller = new CommodityDetailsController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockCommodityDetailsPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.STANDARD)))
    when(mockCommodityDetailsPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockCommodityDetailsPage)
  }

  val itemId = "itemId"

  def theResponseForm: Form[CommodityDetails] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[CommodityDetails]])
    verify(mockCommodityDetailsPage).apply(any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  "Commodity Details controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {

        val result = controller.displayPage(Mode.Normal, itemId)(getRequest())

        status(result) mustBe OK
        verify(mockCommodityDetailsPage, times(1)).apply(any(), any(), any())(any(), any())

        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" in {
        val details = CommodityDetails(Some("12345678"), "Description")
        val item = anItem(withCommodityDetails(details))
        withNewCaching(aDeclaration(withItems(item)))

        val result = controller.displayPage(Mode.Normal, item.id)(getRequest())

        status(result) mustBe OK
        verify(mockCommodityDetailsPage, times(1)).apply(any(), any(), any())(any(), any())

        theResponseForm.value mustBe Some(details)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {

        val incorrectForm = Json.toJson(CommodityDetails(None, "Description"))

        val result = controller.submitForm(Mode.Normal, itemId)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verify(mockCommodityDetailsPage, times(1)).apply(any(), any(), any())(any(), any())
      }
    }

    "return 303 (SEE_OTHER)" when {

      "valid data provided" in {

        val correctForm = Json.toJson(CommodityDetails(Some("12345678"), "Description"))

        val result = controller.submitForm(Mode.Normal, itemId)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.ItemTypeController.displayPage(Mode.Normal, itemId)
        verify(mockCommodityDetailsPage, times(0)).apply(any(), any(), any())(any(), any())
      }

      "valid data provided for simple declaration" in {
        withNewCaching(aDeclaration(withType(DeclarationType.SIMPLIFIED)))
        val correctForm = Json.toJson(CommodityDetails(None, "Description"))

        val result = controller.submitForm(Mode.Normal, itemId)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.ItemTypeController.displayPage(Mode.Normal, itemId)
        verify(mockCommodityDetailsPage, times(0)).apply(any(), any(), any())(any(), any())
      }
    }
  }
}
