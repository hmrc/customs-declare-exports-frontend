/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.section5

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.section5.routes._
import forms.section4.NatureOfTransaction.{BusinessPurchase, Sale}
import forms.section5.CommodityDetails.commodityCodeChemicalPrefixes
import forms.section5.UNDangerousGoodsCode.{dangerousGoodsCodeKey, hasDangerousGoodsCodeKey}
import forms.section5.{CommodityDetails, UNDangerousGoodsCode}
import models.DeclarationType._
import models.ExportsDeclaration
import models.declaration.ProcedureCodesData.lowValueDeclaration
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.{Assertion, OptionValues}
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsEmpty, Call, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section5.un_dangerous_goods_code

class UNDangerousGoodsCodeControllerSpec extends ControllerSpec with AuditedControllerSpec with OptionValues {

  val mockPage = mock[un_dangerous_goods_code]

  val controller =
    new UNDangerousGoodsCodeController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, mockPage)(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aStandardDeclaration)
    when(mockPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockPage)
  }

  def itemIdOfCachedDeclaration(declaration: ExportsDeclaration): String = {
    val result = declaration.items.headOption.fold(declaration.copy(items = List(anItem())))(_ => declaration)
    withNewCaching(result)
    result.items.head.id
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    val itemId = itemIdOfCachedDeclaration(aDeclaration())
    await(controller.displayPage(itemId)(request))
    theResponseForm
  }

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
        val itemId = itemIdOfCachedDeclaration(aDeclaration())
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
        val itemId = itemIdOfCachedDeclaration(aDeclaration())
        val incorrectForm = formData("Invalid Code")
        val result = controller.submitForm(itemId)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verify(mockPage, times(1)).apply(any(), any())(any(), any())
        verifyNoAudit()
      }
    }

    val correctForm = formData("1234")

    "return 303 (SEE_OTHER)" when {

      onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, SUPPLEMENTARY) { request =>
        def controllerRedirectsToNextPageForCommodityCode(commodityCode: String, expectedCall: String => Call): Assertion = {
          val commodityDetails = CommodityDetails(Some(commodityCode), None)
          val item = anItem(withCommodityDetails(commodityDetails))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(item)))

          val result = controller.submitForm(item.id)(postRequest(correctForm))
          await(result) mustBe aRedirectToTheNextPage
          verifyAudit()
          thePageNavigatedTo mustBe expectedCall(item.id)
        }

        commodityCodeChemicalPrefixes.foreach { prefix =>
          s"accept submission and redirect for commodity code ${prefix}00000000" in {
            controllerRedirectsToNextPageForCommodityCode(s"${prefix}00000000", CusCodeController.displayPage(_))
          }
        }

        "accept submission and redirect for commodity code 2100000000" in {
          controllerRedirectsToNextPageForCommodityCode("2100000000", NactCodeSummaryController.displayPage(_))
        }
      }

      onJourney(CLEARANCE) { request =>
        def controllerRedirectsToNextPageForProcedureCode(procedureCode: String, expectedCall: String => Call): Assertion = {
          val item = anItem(withProcedureCodes(Some(procedureCode)))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(item)))

          val result = controller.submitForm(item.id)(postRequest(correctForm))
          await(result) mustBe aRedirectToTheNextPage
          verifyAudit()
          thePageNavigatedTo mustBe expectedCall(item.id)
        }

        "accept submission and redirect for procedure code 0019" in {
          controllerRedirectsToNextPageForProcedureCode("0019", CommodityMeasureController.displayPage(_))
        }

        "accept submission and redirect for procedure code 1234" in {
          controllerRedirectsToNextPageForProcedureCode("1234", PackageInformationSummaryController.displayPage(_))
        }
      }
    }

    "redirect to the 'Zero-Rated for Vat' page" when {

      "on Standard journey and" when {

        "NatureOfTransaction is 'BusinessPurchase'" in {
          val itemId = itemIdOfCachedDeclaration(aDeclaration(withNatureOfTransaction(BusinessPurchase)))
          val result = controller.submitForm(itemId)(postRequest(correctForm))
          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe ZeroRatedForVatController.displayPage(itemId)
          verifyAudit()
        }

        "NatureOfTransaction is 'Sale'" in {
          val itemId = itemIdOfCachedDeclaration(aDeclaration(withNatureOfTransaction(Sale)))
          val result = controller.submitForm(itemId)(postRequest(correctForm))
          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe ZeroRatedForVatController.displayPage(itemId)
          verifyAudit()
        }
      }

      occasionalAndSimplified.foreach { declarationType =>
        s"on $declarationType journey and" when {
          "the declaration is a 'low value' one " in {
            val item = anItem(withProcedureCodes(additionalProcedureCodes = List(lowValueDeclaration)))
            withNewCaching(aDeclaration(withType(declarationType), withItems(item)))

            val result = controller.submitForm(item.id)(postRequest(correctForm))
            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe ZeroRatedForVatController.displayPage(item.id)
            verifyAudit()
          }
        }
      }
    }
  }
}
