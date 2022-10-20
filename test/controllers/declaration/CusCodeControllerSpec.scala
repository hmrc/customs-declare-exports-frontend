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
import forms.declaration.CusCode
import forms.declaration.CusCode._
import models.DeclarationType.{apply => _, _}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.cus_code

class CusCodeControllerSpec extends ControllerSpec {

  val mockPage = mock[cus_code]

  val controller =
    new CusCodeController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, stubMessagesControllerComponents(), mockPage)(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockPage)
  }

  val itemId = "itemId"

  def theResponseForm: Form[CusCode] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[CusCode]])
    verify(mockPage).apply(any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(itemId)(request))
    theResponseForm
  }

  private def formData(code: String) = JsObject(Map(cusCodeKey -> JsString(code), hasCusCodeKey -> JsString("Yes")))

  "CUSCode controller" must {

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage(itemId)(getRequest())

          status(result) mustBe OK
          verify(mockPage, times(1)).apply(any(), any(), any())(any(), any())

          theResponseForm.value mustBe empty
        }

        "display page method is invoked and cache contains data" in {
          val cusCode = CusCode(Some("12345678"))
          val item = anItem(withCUSCode(cusCode))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val result = controller.displayPage(item.id)(getRequest())

          status(result) mustBe OK
          verify(mockPage, times(1)).apply(any(), any(), any())(any(), any())

          theResponseForm.value mustBe Some(cusCode)
        }

      }

      "return 400 (BAD_REQUEST)" when {

        "form is incorrect" in {
          withNewCaching(request.cacheModel)

          val incorrectForm = formData("Invalid Code")

          val result = controller.submitForm(itemId)(postRequest(incorrectForm))

          status(result) mustBe BAD_REQUEST
          verify(mockPage, times(1)).apply(any(), any(), any())(any(), any())
        }
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      "return 303 (SEE_OTHER)" when {
        "accept submission and redirect" in {
          withNewCaching(request.cacheModel)
          val correctForm = formData("12345678")

          val result = controller.submitForm(itemId)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.TaricCodeSummaryController.displayPage(itemId)
          verify(mockPage, times(0)).apply(any(), any(), any())(any(), any())
        }
      }
    }

    onClearance { request =>
      "return 303 (SEE_OTHER)" when {
        "invalid journey CLEARANCE" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage("").apply(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.RootController.displayPage().url)
        }
      }
    }
  }
}
