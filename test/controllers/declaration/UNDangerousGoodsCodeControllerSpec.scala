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
import controllers.declaration.routes.{CommodityMeasureController, CusCodeController, TaricCodeSummaryController}
import forms.declaration.UNDangerousGoodsCode.{dangerousGoodsCodeKey, hasDangerousGoodsCodeKey}
import forms.declaration.{CommodityDetails, UNDangerousGoodsCode}
import models.DeclarationType._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsEmpty, Call, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.un_dangerous_goods_code

class UNDangerousGoodsCodeControllerSpec extends ControllerSpec with OptionValues {

  val mockPage = mock[un_dangerous_goods_code]

  val controller = new UNDangerousGoodsCodeController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withType(STANDARD)))
    when(mockPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockPage)
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    await(controller.displayPage(itemId)(request))
    theResponseForm
  }

  val itemId = "itemId"

  def theResponseForm: Form[UNDangerousGoodsCode] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[UNDangerousGoodsCode]])
    verify(mockPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  private def formData(code: String): JsObject =
    Json.obj(dangerousGoodsCodeKey -> code, hasDangerousGoodsCodeKey -> "Yes")

  "UNDangerousGoodsCode controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {
        val result = controller.displayPage(itemId)(getRequest())

        status(result) mustBe OK
        verify(mockPage, times(1)).apply(any(), any())(any(), any())

        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" in {
        val dangerousGoodsCode = UNDangerousGoodsCode(Some("1234"))
        val item = anItem(withUNDangerousGoodsCode(dangerousGoodsCode))
        withNewCaching(aDeclaration(withItems(item)))

        val result = controller.displayPage(item.id)(getRequest())

        status(result) mustBe OK
        verify(mockPage, times(1)).apply(any(), any())(any(), any())

        theResponseForm.value mustBe Some(dangerousGoodsCode)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {
        val incorrectForm = formData("Invalid Code")

        val result = controller.submitForm(itemId)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verify(mockPage, times(1)).apply(any(), any())(any(), any())
      }
    }

    "return 303 (SEE_OTHER)" when {

      onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, SUPPLEMENTARY) { request =>
        def controllerRedirectsToNextPageForCommodityCode(commodityCode: String, expectedCall: Call): HtmlFormat.Appendable = {
          val commodityDetails = CommodityDetails(Some(commodityCode), None)
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(anItem(withItemId(itemId), withCommodityDetails(commodityDetails)))))
          val correctForm = formData("1234")

          val result = controller.submitForm(itemId)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe expectedCall
          verify(mockPage, times(0)).apply(any(), any())(any(), any())
        }

        "accept submission and redirect for commodity code 2800000000" in {
          controllerRedirectsToNextPageForCommodityCode("2800000000", CusCodeController.displayPage(itemId))
        }

        "accept submission and redirect for commodity code 2100000000" in {
          controllerRedirectsToNextPageForCommodityCode("2100000000", TaricCodeSummaryController.displayPage(itemId))
        }
      }

      onJourney(CLEARANCE) { request =>
        def controllerRedirectsToNextPageForProcedureCode(procedureCode: String, expectedCall: Call): HtmlFormat.Appendable = {

          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(anItem(withItemId(itemId), withProcedureCodes(Some(procedureCode))))))
          val correctForm = formData("1234")

          val result = controller.submitForm(itemId)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe expectedCall
          verify(mockPage, times(0)).apply(any(), any())(any(), any())
        }

        "accept submission and redirect for procedure code 0019" in {
          controllerRedirectsToNextPageForProcedureCode("0019", CommodityMeasureController.displayPage(itemId))
        }

        "accept submission and redirect for procedure code 1234" in {
          controllerRedirectsToNextPageForProcedureCode("1234", routes.PackageInformationSummaryController.displayPage(itemId))
        }
      }
    }
  }
}
