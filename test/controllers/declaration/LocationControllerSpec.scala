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

import base.ControllerSpec
import connectors.CodeListConnector
import forms.declaration.GoodsLocationForm
import models.{DeclarationType, Mode}
import models.codes.Country
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.goods_location

import scala.collection.immutable.ListMap

class LocationControllerSpec extends ControllerSpec with OptionValues {

  val mockGoodsLocationPage = mock[goods_location]
  val mockCodeListConnector = mock[CodeListConnector]

  val controller = new LocationController(
    mockAuthAction,
    mockJourneyAction,
    stubMessagesControllerComponents(),
    mockGoodsLocationPage,
    mockExportsCacheService,
    navigator
  )(ec, mockCodeListConnector)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
    when(mockGoodsLocationPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("PL" -> Country("Poland", "PL")))
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    reset(mockGoodsLocationPage, mockCodeListConnector)
  }

  def theResponseForm: Form[GoodsLocationForm] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[GoodsLocationForm]])
    verify(mockGoodsLocationPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
  }

  "Location controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        verify(mockGoodsLocationPage).apply(any(), any())(any(), any())

        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" in {

        val goodsLocation = GoodsLocationForm("GBAUEMAEMAEMA")
        withNewCaching(aDeclaration(withGoodsLocation(goodsLocation)))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        verify(mockGoodsLocationPage).apply(any(), any())(any(), any())

        theResponseForm.value mustNot be(empty)
        theResponseForm.value.value.code mustBe "GBAUEMAEMAEMA"
      }

    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {

        val incorrectForm = Json.toJson(GoodsLocationForm("incorrect"))

        val result = controller.saveLocation(Mode.Normal)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verify(mockGoodsLocationPage).apply(any(), any())(any(), any())
      }
    }

    "return 303 (SEE_OTHER)" when {

      "information provided by user are correct" in {

        val correctForm = Json.toJson(GoodsLocationForm("PLAUEMAEMAEMA"))

        val result = controller.saveLocation(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.OfficeOfExitController.displayPage()
        verify(mockGoodsLocationPage, times(0)).apply(any(), any())(any(), any())
      }
    }
  }
}
