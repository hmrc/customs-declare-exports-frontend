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

import base.{AuditedControllerSpec, ControllerSpec, TestHelper}
import controllers.declaration.routes.StatisticalValueController
import controllers.routes.RootController
import forms.declaration.{NactCode, NactCodeFirst}
import models.DeclarationType._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.{nact_code_add, nact_code_add_first}

class NactCodeAddControllerSpec extends ControllerSpec with AuditedControllerSpec with OptionValues {

  val mockAddPage = mock[nact_code_add]
  val mockAddFirstPage = mock[nact_code_add_first]

  val controller =
    new NactCodeAddController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      navigator,
      mcc,
      mockAddFirstPage,
      mockAddPage
    )(ec, auditService)

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
    theNactCodeFirst
  }

  def theNactCode: Form[NactCode] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[NactCode]])
    verify(mockAddPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  def theNactCodeFirst: Form[NactCodeFirst] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[NactCodeFirst]])
    verify(mockAddFirstPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  private def verifyAddPageInvoked(numberOfTimes: Int = 1) = verify(mockAddPage, times(numberOfTimes)).apply(any(), any())(any(), any())
  private def verifyAddPageFirstInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(mockAddFirstPage, times(numberOfTimes)).apply(any(), any())(any(), any())

  val item = anItem()

  "Nact Code Add Controller" must {

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      "return 200 (OK)" that {

        "display page method is invoked and cache is empty" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(item.id)(getRequest())

          status(result) mustBe OK
          verifyAddPageFirstInvoked()
          verifyAddPageInvoked(0)

          theNactCodeFirst.value mustBe empty
        }

        "display page method is invoked and user said no" in {
          val item = anItem(withNactCodes(List.empty))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val result = controller.displayPage(item.id)(getRequest())

          status(result) mustBe OK
          verifyAddPageFirstInvoked()
          verifyAddPageInvoked(0)

          theNactCodeFirst.value mustBe Some(NactCodeFirst(None))
        }

        "display page method is invoked and cache contains data" in {
          val nactCode = NactCode("1234")
          val item = anItem(withNactCodes(nactCode))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val result = controller.displayPage(item.id)(getRequest())

          status(result) mustBe OK
          verifyAddPageFirstInvoked(0)
          verifyAddPageInvoked()

          theNactCode.value mustBe empty
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "user adds invalid code" in {
          withNewCaching(request.cacheModel)

          val requestBody = Seq(NactCodeFirst.hasNactCodeKey -> "Yes", NactCode.nactCodeKey -> "invalidCode")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageFirstInvoked()
          verifyNoAudit()
        }

        "user adds duplicate code" in {
          val nactCode = NactCode("VATR")
          val item = anItem(withNactCodes(nactCode))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq(NactCodeFirst.hasNactCodeKey -> "Yes", NactCode.nactCodeKey -> "VATR")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
          verifyNoAudit()
        }

        "user adds too many codes" in {
          val nactCodes = List.fill(99)(NactCode(TestHelper.createRandomAlphanumericString(4)))
          val item = anItem(withNactCodes(nactCodes))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq(NactCodeFirst.hasNactCodeKey -> "Yes", NactCode.nactCodeKey -> "VATR")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
          verifyNoAudit()
        }
      }

      "return 303 (SEE_OTHER)" when {

        "user submits valid first code" in {
          val item = anItem()
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq(NactCodeFirst.hasNactCodeKey -> "Yes", NactCode.nactCodeKey -> "VATR")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.NactCodeSummaryController.displayPage(item.id)

          theCacheModelUpdated.itemBy(item.id).flatMap(_.nactCodes) mustBe Some(Seq(NactCode("VATR")))
          verifyAudit()
        }

        "user submits valid additional code" in {
          val nactCode = NactCode("1234")
          val item = anItem(withNactCodes(nactCode))
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq(NactCode.nactCodeKey -> "VATR")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.NactCodeSummaryController.displayPage(item.id)

          theCacheModelUpdated.itemBy(item.id).flatMap(_.nactCodes) mustBe Some(Seq(nactCode, NactCode("VATR")))
          verifyAudit()
        }
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY) { request =>
      "re-direct to next question" when {
        "user submits valid No answer" in {
          val item = anItem()
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq(NactCodeFirst.hasNactCodeKey -> "No")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe StatisticalValueController.displayPage(item.id)
        }
      }
    }

    onJourney(SIMPLIFIED, OCCASIONAL) { request =>
      "re-direct to next question" when {
        "user submits valid No answer" in {
          val item = anItem()
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq(NactCodeFirst.hasNactCodeKey -> "No")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.PackageInformationSummaryController.displayPage(item.id)
        }
      }
    }

    onJourney(CLEARANCE) { request =>
      "return 303 (SEE_OTHER)" that {

        "display page method is invoked" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(item.id)(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(RootController.displayPage.url)
        }

        "user submits valid data" in {
          val item = anItem()
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Seq(NactCodeFirst.hasNactCodeKey -> "Yes", NactCode.nactCodeKey -> "VATR")
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(RootController.displayPage.url)
        }
      }
    }
  }
}
