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
import forms.declaration.{CommodityDetails, CusCode, IsExs}
import models.DeclarationType._
import models.Mode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Call, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
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

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal, itemId)(request))
    theResponseForm
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

        val details = CommodityDetails(Some("1234567809"), Some("Description"))
        val item = anItem(withCommodityDetails(details))
        withNewCaching(aDeclaration(withItems(item)))

        val result = controller.displayPage(Mode.Normal, item.id)(getRequest())

        status(result) mustBe OK
        verify(mockCommodityDetailsPage, times(1)).apply(any(), any(), any())(any(), any())
        println(theResponseForm.value)
        theResponseForm.value mustBe Some(details)
      }

    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {

        withNewCaching(aDeclaration())

        val incorrectForm = Json.toJson(CommodityDetails(None, Some("Description")))

        val result = controller.submitForm(Mode.Normal, itemId)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verify(mockCommodityDetailsPage, times(1)).apply(any(), any(), any())(any(), any())
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, OCCASIONAL) { request =>
      "return 303 (SEE_OTHER) and redirect to UN Dangerous Goods Code page" in {

        withNewCaching(request.cacheModel)
        val correctForm = Json.toJson(CommodityDetails(Some("1234567809"), Some("Description")))

        val result = controller.submitForm(Mode.Normal, itemId)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage(Mode.Normal, itemId)
      }
    }

    onJourney(SIMPLIFIED) { request =>
      "return 303 (SEE_OTHER) and redirect to UN Dangerous Goods Code page" in {

        withNewCaching(request.cacheModel)
        val correctForm = Json.toJson(CommodityDetails(None, Some("Description")))

        val result = controller.submitForm(Mode.Normal, itemId)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage(Mode.Normal, itemId)
      }
    }

    onJourney(CLEARANCE) { request =>
      def controllerRedirectsToNextPageForProcedureCodeAndExsStatus(procedureCode: String, exsStatus: String, expectedCall: Call) = {

        withNewCaching(
          aDeclarationAfter(
            request.cacheModel,
            withIsExs(IsExs(exsStatus)),
            withItem(anItem(withItemId(itemId), withProcedureCodes(Some(procedureCode))))
          )
        )
        val correctForm = Json.toJson(CommodityDetails(Some("1234567809"), Some("Description")))

        val result = controller.submitForm(Mode.Normal, itemId)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe expectedCall
      }

      "return 303 (SEE_OTHER) and redirects when Exs No and Procedure Code 1234" in {

        controllerRedirectsToNextPageForProcedureCodeAndExsStatus(
          "1234",
          "No",
          controllers.declaration.routes.PackageInformationSummaryController.displayPage(Mode.Normal, itemId)
        )
      }

      "return 303 (SEE_OTHER) and redirects when Exs No and Procedure Code 0019" in {

        controllerRedirectsToNextPageForProcedureCodeAndExsStatus(
          "0019",
          "No",
          controllers.declaration.routes.CommodityMeasureController.displayPage(Mode.Normal, itemId)
        )
      }

      "return 303 (SEE_OTHER) and redirects when Exs Yes" in {

        controllerRedirectsToNextPageForProcedureCodeAndExsStatus(
          "0000",
          "Yes",
          controllers.declaration.routes.UNDangerousGoodsCodeController.displayPage(Mode.Normal, itemId)
        )
      }
    }

    "trim potential spaces entered by the user in the commodity code" in {
      withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId)))))

      val commodityCode = "1234567809"
      val correctForm = Json.toJson(CommodityDetails(Some(s"  ${commodityCode}  "), Some("Description")))
      val result = controller.submitForm(Mode.Normal, itemId)(postRequest(correctForm))

      await(result) mustBe aRedirectToTheNextPage

      val declarations = theCacheModelUpdated()
      declarations.head.commodityCodeOfItem(itemId).value mustBe commodityCode
    }

    "clear the cus-code field when commodity code entered does not have a chemical code prefix" in {
      val cusCode = CusCode(Some("11111111"))
      withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId), withCUSCode(cusCode)))))

      val correctForm = Json.toJson(CommodityDetails(Some("1234567809"), Some("Description")))
      val result = controller.submitForm(Mode.Normal, itemId)(postRequest(correctForm))

      await(result) mustBe aRedirectToTheNextPage

      val updatedModel = theCacheModelUpdated()
      updatedModel.head.itemBy(itemId).head.cusCode mustBe None
    }

    "do not clear the cus-code field when commodity code entered does have a chemical code prefix" in {
      val cusCode = CusCode(Some("11111111"))
      withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId), withCUSCode(cusCode)))))

      val correctForm = Json.toJson(CommodityDetails(Some("2800000000"), Some("Description")))
      val result = controller.submitForm(Mode.Normal, itemId)(postRequest(correctForm))

      await(result) mustBe aRedirectToTheNextPage

      val updatedModel = theCacheModelUpdated()
      updatedModel.head.itemBy(itemId).head.cusCode mustBe Some(cusCode)
    }
  }
}
