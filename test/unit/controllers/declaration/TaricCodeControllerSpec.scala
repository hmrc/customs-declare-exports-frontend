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

import base.TestHelper
import controllers.declaration.TaricCodeController
import controllers.util.{Add, SaveAndContinue}
import forms.declaration.TaricCode
import forms.declaration.TaricCode.taricCodeKey
import models.DeclarationType.DeclarationType
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.taric_codes

class TaricCodeControllerSpec extends ControllerSpec with ErrorHandlerMocks with OptionValues {

  val mockPage = mock[taric_codes]

  val controller =
    new TaricCodeController(
      mockAuthAction,
      mockJourneyAction,
      mockErrorHandler,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      mockPage
    )(ec)

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

  val item = anItem()

  "TARIC Code controller" must {

    onEveryDeclarationJourney(withItems(item)){ declaration =>
      "return 200 (OK)" that {
        "display page method is invoked and cache is empty" in {

          withNewCaching(declaration)

          val result = controller.displayPage(Mode.Normal, item.id)(getRequest())

          status(result) mustBe OK
          verifyPageInvoked()

          theTaricCodes mustBe empty
        }

        "display page method is invoked and cache contains data" in {
          val taricCode = TaricCode("1234")
          val item = anItem(withTaricCodes(taricCode))
          withNewCaching(aDeclarationAfter(declaration, withItems(item)))

          val result = controller.displayPage(Mode.Normal, item.id)(getRequest())

          status(result) mustBe OK
          verifyPageInvoked()

          theTaricCodes must contain(taricCode)
        }
      }

      "user adds invalid code" in {
        withNewCaching(declaration)

        val body = Seq((taricCodeKey, "invalidCode"), (Add.toString, ""))

        val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(body: _*))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
      }

      "return 400 (BAD_REQUEST)" when {
        "user adds duplicate code" in {
          val taricCode = TaricCode("1234")
          val item = anItem(withTaricCodes(taricCode))
          withNewCaching(aDeclarationAfter(declaration, withItems(item)))

          val body = Seq((taricCodeKey, "1234"), (Add.toString, ""))

          val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(body: _*))

          status(result) mustBe BAD_REQUEST
          verifyPageInvoked()
        }

        "user adds too many codes" in {
          val taricCodes = List.fill(99)(TaricCode(TestHelper.createRandomAlphanumericString(4)))
          val item = anItem(withTaricCodes(taricCodes))
          withNewCaching(aDeclarationAfter(declaration, withItems(item)))

          val body = Seq((taricCodeKey, "1234"), (Add.toString, ""))

          val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(body: _*))

          status(result) mustBe BAD_REQUEST
          verifyPageInvoked()
        }
      }
      "return 303 (SEE_OTHER)" when {
        "user submits valid code" in {
          val item = anItem(withItemId("itemId"))
          withNewCaching(aDeclarationAfter(declaration, withItems(item)))

          val body = Seq((taricCodeKey, "1234"), (SaveAndContinue.toString, ""))

          val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(body: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.NactCodeController.displayPage(Mode.Normal, "itemId")
          verifyPageInvoked(0)
        }
      }
    }
  }
}
