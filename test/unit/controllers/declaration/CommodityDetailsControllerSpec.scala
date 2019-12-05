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
import models.DeclarationType.DeclarationType
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Call
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

        withNewCaching(aDeclaration())

        val result = controller.displayPage(Mode.Normal, itemId)(getRequest())

        status(result) mustBe OK
        verify(mockCommodityDetailsPage, times(1)).apply(any(), any(), any())(any(), any())

        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" in {

        withNewCaching(aDeclaration())

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

        withNewCaching(aDeclaration())

        val incorrectForm = Json.toJson(CommodityDetails(None, "Description"))

        val result = controller.submitForm(Mode.Normal, itemId)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verify(mockCommodityDetailsPage, times(1)).apply(any(), any(), any())(any(), any())
      }
    }

    "return 303 (SEE_OTHER)" when {

      def redirectForDeclarationType(declarationType: DeclarationType, form: CommodityDetails, call: Call): Unit =
        "redirect" in {
          withNewCaching(aDeclaration(withType(declarationType)))

          val correctForm = Json.toJson(form)

          val result = controller.submitForm(Mode.Normal, itemId)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe call
        }

      "submit for Standard declaration" should {

        behave like redirectForDeclarationType(
          DeclarationType.STANDARD,
          CommodityDetails(Some("12345678"), "Description"),
          controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage(Mode.Normal, itemId)
        )
      }

      "submit for Supplementary declaration" should {

        behave like redirectForDeclarationType(
          DeclarationType.SUPPLEMENTARY,
          CommodityDetails(Some("12345678"), "Description"),
          controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage(Mode.Normal, itemId)
        )
      }

      "submit for Simplified declaration" should {

        behave like redirectForDeclarationType(
          DeclarationType.SIMPLIFIED,
          CommodityDetails(None, "Description"),
          controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage(Mode.Normal, itemId)
        )
      }

      "submit for Occasional declaration" should {

        behave like redirectForDeclarationType(
          DeclarationType.OCCASIONAL,
          CommodityDetails(Some("12345678"), "Description"),
          controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage(Mode.Normal, itemId)
        )
      }

      "submit for Clearance declaration" should {

        behave like redirectForDeclarationType(
          DeclarationType.CLEARANCE,
          CommodityDetails(Some("12345678"), "Description"),
          controllers.declaration.routes.CUSCodeController.displayPage(Mode.Normal, itemId)
        )
      }
    }
  }
}
