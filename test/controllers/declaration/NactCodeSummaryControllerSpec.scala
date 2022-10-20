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
import forms.declaration.{NactCode, NactCodeFirst}
import models.{DeclarationType}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.nact_codes

class NactCodeSummaryControllerSpec extends ControllerSpec with OptionValues {

  val mockPage = mock[nact_codes]

  val controller =
    new NactCodeSummaryController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, stubMessagesControllerComponents(), mockPage)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockPage)
    super.afterEach()
  }

  private def theResponseForm: Form[YesNoAnswer] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockPage).apply(any(), any(), formCaptor.capture(), any())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    val item = anItem(withNactCodes(NactCode("VATE")))
    withNewCaching(aDeclaration(withItems(item)))
    await(controller.displayPage(item.id)(request))
    theResponseForm
  }
  def theNactCodes: List[NactCode] = {
    val captor = ArgumentCaptor.forClass(classOf[List[NactCode]])
    verify(mockPage).apply(any(), any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1) = verify(mockPage, times(numberOfTimes)).apply(any(), any(), any(), any())(any(), any())

  "NACT Code Summary Controller" should {

    onJourney(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY, DeclarationType.OCCASIONAL, DeclarationType.SIMPLIFIED) { request =>
      "return 200 (OK)" that {
        "display page method is invoked and cache contains data" in {
          val nactCode = NactCode("VATE")
          val item = anItem(withNactCodes(nactCode))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val result = controller.displayPage(item.id)(getRequest())

          status(result) mustBe OK
          verifyPageInvoked()

          theNactCodes must contain(nactCode)
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "user submits invalid answer" in {
          val nactCode = NactCode("VATE")
          val item = anItem(withNactCodes(nactCode))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("yesNo" -> "invalid")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyPageInvoked()
        }

      }

      "return 303 (SEE_OTHER)" when {

        "there is no nact codes in the cache" in {
          val item = anItem()
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val result = controller.displayPage(item.id)(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.NactCodeAddController.displayPage(item.id)
        }

        "user submits valid Yes answer" in {
          val nactCode = NactCode("VATE")
          val item = anItem(withNactCodes(nactCode))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("yesNo" -> "Yes")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.NactCodeAddController.displayPage(item.id)
        }

      }
    }

    onJourney(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY) { request =>
      "re-direct to next question" when {

        "user submits valid No answer" in {
          val nactCode = NactCode("VATE")
          val item = anItem(withNactCodes(nactCode))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("yesNo" -> "No")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.StatisticalValueController.displayPage(item.id)
        }

      }
    }

    onJourney(DeclarationType.SIMPLIFIED, DeclarationType.OCCASIONAL) { request =>
      "re-direct to next question" when {

        "user submits valid No answer" in {
          val nactCode = NactCode("VATE")
          val item = anItem(withNactCodes(nactCode))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq("yesNo" -> "No")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.PackageInformationSummaryController.displayPage(item.id)
        }

      }
    }

    onJourney(DeclarationType.CLEARANCE) { request =>
      "return 303 (SEE_OTHER)" that {
        "display page method is invoked" in {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage("id")(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.RootController.displayPage().url)
        }

        "user submits valid data" in {
          val item = anItem()
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq(NactCodeFirst.hasNactCodeKey -> "Yes", NactCode.nactCodeKey -> "VATR")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.RootController.displayPage().url)
        }
      }
    }

  }
}
