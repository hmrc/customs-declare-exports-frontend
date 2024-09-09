/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.general.routes.RootController
import controllers.section5.routes.{NactCodeSummaryController, ZeroRatedForVatController}
import forms.section4.NatureOfTransaction.{BusinessPurchase, Sale}
import forms.section5.CusCode
import forms.section5.CusCode._
import models.DeclarationType._
import models.ExportsDeclaration
import models.declaration.ProcedureCodesData.lowValueDeclaration
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section5.cus_code

class CusCodeControllerSpec extends ControllerSpec with AuditedControllerSpec {

  val mockPage = mock[cus_code]

  val controller = new CusCodeController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, mockPage)(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockPage)
  }

  def theResponseForm: Form[CusCode] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[CusCode]])
    verify(mockPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
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

  private def formData(code: String) = JsObject(Map(cusCodeKey -> JsString(code), hasCusCodeKey -> JsString("Yes")))

  "CUSCode controller" must {

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {
          val itemId = itemIdOfCachedDeclaration(request.cacheModel)
          val result = controller.displayPage(itemId)(getRequest())

          status(result) mustBe OK
          verify(mockPage, times(1)).apply(any(), any())(any(), any())

          theResponseForm.value mustBe empty
        }

        "display page method is invoked and cache contains data" in {
          val cusCode = CusCode(Some("12345678"))
          val item = anItem(withCUSCode(cusCode))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val result = controller.displayPage(item.id)(getRequest())

          status(result) mustBe OK
          verify(mockPage, times(1)).apply(any(), any())(any(), any())

          theResponseForm.value mustBe Some(cusCode)
        }

      }

      "return 400 (BAD_REQUEST)" when {
        "form is incorrect" in {
          val itemId = itemIdOfCachedDeclaration(request.cacheModel)
          val incorrectForm = formData("Invalid Code")
          val result = controller.submitForm(itemId)(postRequest(incorrectForm))

          status(result) mustBe BAD_REQUEST
          verify(mockPage, times(1)).apply(any(), any())(any(), any())
          verifyNoAudit()
        }
      }
    }

    val correctForm = formData("12345678")

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      "return 303 (SEE_OTHER)" when {
        "accept submission and redirect to the 'Nact Code Summary' page" in {
          val itemId = itemIdOfCachedDeclaration(request.cacheModel)

          val result = controller.submitForm(itemId)(postRequest(correctForm))
          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe NactCodeSummaryController.displayPage(itemId)
          verifyAudit()
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

    onClearance { request =>
      "return 303 (SEE_OTHER)" when {
        "invalid journey CLEARANCE" in {
          val itemId = itemIdOfCachedDeclaration(request.cacheModel)

          val result = controller.displayPage(itemId).apply(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(RootController.displayPage.url)
          verifyNoAudit()
        }
      }
    }
  }
}
