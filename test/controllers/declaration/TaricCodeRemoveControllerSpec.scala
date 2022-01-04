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
import views.html.declaration.taric_code_remove

class TaricCodeRemoveControllerSpec extends ControllerSpec with OptionValues {

  val mockRemovePage = mock[taric_code_remove]

  val controller =
    new TaricCodeRemoveController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      mockRemovePage
    )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockRemovePage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockRemovePage)
    super.afterEach()
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockRemovePage).apply(any(), any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal, item.id, "ABCD")(request))
    theResponseForm
  }

  def theTaricCode: String = {
    val captor = ArgumentCaptor.forClass(classOf[String])
    verify(mockRemovePage).apply(any(), any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def verifyRemovePageInvoked(numberOfTimes: Int = 1) =
    verify(mockRemovePage, times(numberOfTimes)).apply(any(), any(), any(), any())(any(), any())

  val item = anItem()

  "Taric Code Remove Controller" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" that {
        "display page method is invoked and cache is empty" in {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal, item.id, "ABCD")(getRequest())

          status(result) mustBe OK
          verifyRemovePageInvoked()

          theTaricCode mustBe "ABCD"
        }

      }

      "return 400 (BAD_REQUEST)" when {
        "user submits an invalid answer" in {
          withNewCaching(request.cacheModel)

          val requestBody = Seq("yesNo" -> "invalid")
          val result = controller.submitForm(Mode.Normal, item.id, "ABCD")(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyRemovePageInvoked()
        }

      }
      "return 303 (SEE_OTHER)" when {
        "user submits 'Yes' answer" in {
          val taricCode = TaricCode("ABCD")
          val item = anItem(withTaricCodes(taricCode))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("yesNo" -> "Yes")
          val result = controller.submitForm(Mode.Normal, item.id, "ABCD")(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.TaricCodeSummaryController.displayPage(Mode.Normal, item.id)

          theCacheModelUpdated.itemBy(item.id).flatMap(_.taricCodes) mustBe Some(Seq.empty)
        }

        "user submits 'No' answer" in {
          val taricCode = TaricCode("ABCD")
          val item = anItem(withTaricCodes(taricCode))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("yesNo" -> "No")
          val result = controller.submitForm(Mode.Normal, item.id, "ABCD")(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.TaricCodeSummaryController.displayPage(Mode.Normal, item.id)

          verifyTheCacheIsUnchanged()
        }
      }
    }

  }
}
