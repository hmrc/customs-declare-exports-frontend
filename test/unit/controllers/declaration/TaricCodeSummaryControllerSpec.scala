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

import controllers.declaration.TaricCodeSummaryController
import forms.common.YesNoAnswer
import forms.declaration.TaricCode
import models.Mode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.taric_codes

class TaricCodeSummaryControllerSpec extends ControllerSpec with OptionValues {

  val mockPage = mock[taric_codes]

  val controller =
    new TaricCodeSummaryController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      mockPage
    )

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockPage).apply(any(), any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    val taricCode = TaricCode("1234")
    val item = anItem(withTaricCodes(taricCode))
    withNewCaching(aDeclaration(withItems(item)))
    await(controller.displayPage(Mode.Normal, item.id)(request))
    theResponseForm
  }
  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockPage)
    super.afterEach()
  }

  def theTaricCodes: List[TaricCode] = {
    val captor = ArgumentCaptor.forClass(classOf[List[TaricCode]])
    verify(mockPage).apply(any(), any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1) = verify(mockPage, times(numberOfTimes)).apply(any(), any(), any(), any())(any(), any())

  "Taric Code Summary Controller" should {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" that {
        "display page method is invoked and cache contains data" in {
          val taricCode = TaricCode("1234")
          val item = anItem(withTaricCodes(taricCode))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val result = controller.displayPage(Mode.Normal, item.id)(getRequest())

          status(result) mustBe OK
          verifyPageInvoked()

          theTaricCodes must contain(taricCode)
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "user submits invalid answer" in {
          val taricCode = TaricCode("ABCD")
          val item = anItem(withTaricCodes(taricCode))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("yesNo" -> "invalid")
          val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyPageInvoked()
        }

      }

      "return 303 (SEE_OTHER)" when {

        "there is no taric codes in the cache" in {
          val item = anItem()
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val result = controller.displayPage(Mode.Normal, item.id)(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.TaricCodeAddController.displayPage(Mode.Normal, item.id)
        }

        "user submits valid Yes answer" in {
          val taricCode = TaricCode("ASDF")
          val item = anItem(withTaricCodes(taricCode))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("yesNo" -> "Yes")
          val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.TaricCodeAddController.displayPage(Mode.Normal, item.id)
        }

      }
    }

    onEveryDeclarationJourney() { request =>
      "re-direct to next question" when {

        "user submits valid No answer" in {
          val taricCode = TaricCode("QWER")
          val item = anItem(withTaricCodes(taricCode))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("yesNo" -> "No")
          val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.NactCodeSummaryController.displayPage(Mode.Normal, item.id)
        }

      }
    }

  }
}
