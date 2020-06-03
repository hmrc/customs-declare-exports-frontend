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

import controllers.declaration.UNDangerousGoodsCodeController
import forms.declaration.UNDangerousGoodsCode
import forms.declaration.UNDangerousGoodsCode.{dangerousGoodsCodeKey, hasDangerousGoodsCodeKey}
import models.DeclarationType.DeclarationType
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.Call
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
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
    withNewCaching(aDeclaration(withType(DeclarationType.STANDARD)))
    when(mockPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockPage)
  }

  val itemId = "itemId"

  def theResponseForm: Form[UNDangerousGoodsCode] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[UNDangerousGoodsCode]])
    verify(mockPage).apply(any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  private def formData(code: String) = JsObject(Map(dangerousGoodsCodeKey -> JsString(code), hasDangerousGoodsCodeKey -> JsString("Yes")))

  "UNDangerousGoodsCode controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {

        val result = controller.displayPage(Mode.Normal, itemId)(getRequest())

        status(result) mustBe OK
        verify(mockPage, times(1)).apply(any(), any(), any())(any(), any())

        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" in {
        val dangerousGoodsCode = UNDangerousGoodsCode(Some("1234"))
        val item = anItem(withUNDangerousGoodsCode(dangerousGoodsCode))
        withNewCaching(aDeclaration(withItems(item)))

        val result = controller.displayPage(Mode.Normal, item.id)(getRequest())

        status(result) mustBe OK
        verify(mockPage, times(1)).apply(any(), any(), any())(any(), any())

        theResponseForm.value mustBe Some(dangerousGoodsCode)
      }

      "with submission errors" in {

        val result = controller.displayPage(Mode.Normal, itemId)(getRequestWithSubmissionErrors)
        status(result) mustBe OK

        theResponseForm.errors mustBe Seq(submissionFormError)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {

        val incorrectForm = formData("Invalid Code")

        val result = controller.submitForm(Mode.Normal, itemId)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verify(mockPage, times(1)).apply(any(), any(), any())(any(), any())
      }
    }

    "return 303 (SEE_OTHER)" when {

      val cusCodePage: Call = controllers.declaration.routes.CusCodeController.displayPage(Mode.Normal, itemId)

      def controllerRedirectsToNextPage(decType: DeclarationType, call: Call = cusCodePage): Unit =
        "accept submission and redirect" in {
          withNewCaching(aDeclaration(withType(decType)))
          val correctForm = formData("1234")

          val result = controller.submitForm(Mode.Normal, itemId)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe call
          verify(mockPage, times(0)).apply(any(), any(), any())(any(), any())
        }

      for (decType <- DeclarationType.values) {
        s"we are on $decType journey" should {
          behave like controllerRedirectsToNextPage(decType)
        }
      }
    }
  }
}
