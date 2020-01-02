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

import controllers.declaration.LocationController
import forms.declaration.GoodsLocation
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
import views.html.declaration.goods_location

class LocationControllerSpec extends ControllerSpec with OptionValues {

  val mockGoodsLocationPage = mock[goods_location]

  val controller = new LocationController(
    mockAuthAction,
    mockJourneyAction,
    stubMessagesControllerComponents(),
    mockGoodsLocationPage,
    mockExportsCacheService,
    navigator
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
    when(mockGoodsLocationPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockGoodsLocationPage)
  }

  def theResponseForm: Form[GoodsLocation] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[GoodsLocation]])
    verify(mockGoodsLocationPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  "Location controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        verify(mockGoodsLocationPage, times(1)).apply(any(), any())(any(), any())

        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" in {

        val goodsLocation = GoodsLocation("PL", "A", "B", None, None, None, None, None)
        withNewCaching(aDeclaration(withGoodsLocation(goodsLocation)))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        verify(mockGoodsLocationPage, times(1)).apply(any(), any())(any(), any())

        theResponseForm.value mustNot be(empty)
        theResponseForm.value.value.country mustBe "PL"
        theResponseForm.value.value.typeOfLocation mustBe "A"
        theResponseForm.value.value.qualifierOfIdentification mustBe "B"
        theResponseForm.value.value.identificationOfLocation mustBe empty
        theResponseForm.value.value.additionalIdentifier mustBe empty
        theResponseForm.value.value.addressLine mustBe empty
        theResponseForm.value.value.postCode mustBe empty
        theResponseForm.value.value.city mustBe empty
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {

        val incorrectForm =
          Json.toJson(GoodsLocation("incorrect", "incorrect", "incorrect", None, None, None, None, None))

        val result = controller.saveLocation(Mode.Normal)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verify(mockGoodsLocationPage, times(1)).apply(any(), any())(any(), any())
      }
    }

    "return 303 (SEE_OTHER)" when {

      "information provided by user are correct" in {

        val correctForm = Json.toJson(GoodsLocation("Poland", "A", "B", None, None, None, None, None))

        val result = controller.saveLocation(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.OfficeOfExitController.displayPage()
        verify(mockGoodsLocationPage, times(0)).apply(any(), any())(any(), any())
      }
    }
  }
}
