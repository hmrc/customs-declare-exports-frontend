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

package controllers.declaration

import base.{ControllerSpec, TestHelper}
import controllers.declaration.routes.{TaricCodeSummaryController, ZeroRatedForVatController}
import forms.declaration.NatureOfTransaction.{BusinessPurchase, NationalPurposes, Sale}
import forms.declaration.{TaricCode, TaricCodeFirst}
import models.DeclarationType
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.{taric_code_add, taric_code_add_first}

class TaricCodeAddControllerSpec extends ControllerSpec with OptionValues {

  val mockAddPage = mock[taric_code_add]
  val mockAddFirstPage = mock[taric_code_add_first]

  val controller =
    new TaricCodeAddController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      mockAddFirstPage,
      mockAddPage
    )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockAddFirstPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockAddPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockAddFirstPage, mockAddPage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(item.id)(request))
    theTaricCodeFirst
  }

  def theTaricCode: Form[TaricCode] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[TaricCode]])
    verify(mockAddPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  def theTaricCodeFirst: Form[TaricCodeFirst] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[TaricCodeFirst]])
    verify(mockAddFirstPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  private def verifyAddPageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(mockAddPage, times(numberOfTimes)).apply(any(), any())(any(), any())

  private def verifyAddPageFirstInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(mockAddFirstPage, times(numberOfTimes)).apply(any(), any())(any(), any())

  val item = anItem()

  "Taric Code Add Controller" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" that {

        "display page method is invoked and cache is empty" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(item.id)(getRequest())

          status(result) mustBe OK
          verifyAddPageFirstInvoked()
          verifyAddPageInvoked(0)

          theTaricCodeFirst.value mustBe empty
        }

        "display page method is invoked and user said no" in {
          val item = anItem(withTaricCodes(List.empty))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val result = controller.displayPage(item.id)(getRequest())

          status(result) mustBe OK
          verifyAddPageFirstInvoked()
          verifyAddPageInvoked(0)

          theTaricCodeFirst.value mustBe Some(TaricCodeFirst.none)
        }

        "display page method is invoked and cache contains data" in {
          val taricCode = TaricCode("1234")
          val item = anItem(withTaricCodes(taricCode))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val result = controller.displayPage(item.id)(getRequest())

          status(result) mustBe OK
          verifyAddPageFirstInvoked(0)
          verifyAddPageInvoked()

          theTaricCode.value mustBe empty
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "user adds invalid code" in {
          withNewCaching(request.cacheModel)

          val requestBody = Seq(TaricCodeFirst.hasTaricCodeKey -> "Yes", TaricCode.taricCodeKey -> "invalidCode")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageFirstInvoked()
        }

        "user adds duplicate code" in {
          val taricCode = TaricCode("4321")
          val item = anItem(withTaricCodes(taricCode))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq(TaricCodeFirst.hasTaricCodeKey -> "Yes", TaricCode.taricCodeKey -> "4321")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
        }

        "user adds too many codes" in {
          val taricCodes = List.fill(99)(TaricCode(TestHelper.createRandomAlphanumericString(4)))
          val item = anItem(withTaricCodes(taricCodes))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq(TaricCodeFirst.hasTaricCodeKey -> "Yes", TaricCode.taricCodeKey -> "1234")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
        }
      }

      "return 303 (SEE_OTHER)" when {
        "user submits valid first code" in {
          val item = anItem()
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq(TaricCodeFirst.hasTaricCodeKey -> "Yes", TaricCode.taricCodeKey -> "1234")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe TaricCodeSummaryController.displayPage(item.id)

          theCacheModelUpdated.itemBy(item.id).flatMap(_.taricCodes) mustBe Some(Seq(TaricCode("1234")))
        }

        "user submits valid additional code" in {
          val taricCode = TaricCode("1234")
          val item = anItem(withTaricCodes(taricCode))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq(TaricCode.taricCodeKey -> "4321")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe TaricCodeSummaryController.displayPage(item.id)

          theCacheModelUpdated.itemBy(item.id).flatMap(_.taricCodes) mustBe Some(Seq(taricCode, TaricCode("4321")))
        }
      }
    }

    onEveryDeclarationJourney() { request =>
      "re-direct to next question" when {
        "user submits valid No answer" in {
          val item = anItem()
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq(TaricCodeFirst.hasTaricCodeKey -> "No")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.NactCodeSummaryController.displayPage(item.id)
        }
      }
    }

    onJourney(DeclarationType.STANDARD) { request =>
      "re-direct to next question and" when {
        "user submits valid No answer and" when {

          "user has answered sale transaction" in {
            val item = anItem()
            withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item), withNatureOfTransaction(Sale)))

            val requestBody = Seq(TaricCodeFirst.hasTaricCodeKey -> "No")
            val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe ZeroRatedForVatController.displayPage(item.id)
          }

          "user has answered business purchase transaction" in {
            val item = anItem()
            withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item), withNatureOfTransaction(BusinessPurchase)))

            val requestBody = Seq(TaricCodeFirst.hasTaricCodeKey -> "No")
            val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe ZeroRatedForVatController.displayPage(item.id)
          }

          "user has not answered nature of transaction" in {
            val item = anItem()
            withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

            val requestBody = Seq(TaricCodeFirst.hasTaricCodeKey -> "No")
            val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe routes.NactCodeSummaryController.displayPage(item.id)
          }

          "user has answered other nature of transaction" in {
            val item = anItem()
            withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item), withNatureOfTransaction(NationalPurposes)))

            val requestBody = Seq(TaricCodeFirst.hasTaricCodeKey -> "No")
            val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe routes.NactCodeSummaryController.displayPage(item.id)
          }
        }
      }
    }
  }
}
