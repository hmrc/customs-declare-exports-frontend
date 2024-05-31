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

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.declaration.routes.NactCodeSummaryController
import controllers.routes.RootController
import forms.common.YesNoAnswer
import forms.declaration.NactCode
import models.DeclarationType._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.nact_code_remove

class NactCodeRemoveControllerSpec extends ControllerSpec with AuditedControllerSpec with OptionValues {

  val mockRemovePage = mock[nact_code_remove]

  val controller =
    new NactCodeRemoveController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      navigator,
      mcc,
      mockRemovePage
    )(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockRemovePage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockRemovePage)
    super.afterEach()
  }

  private val nactCode = "VATX"
  private val item = anItem(withNactCodes(NactCode(nactCode)))

  private def theResponseForm: Form[YesNoAnswer] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockRemovePage).apply(any(), any(), formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withItem(item)))
    await(controller.displayPage(item.id, nactCode)(request))
    theResponseForm
  }

  def theNactCode: String = {
    val captor = ArgumentCaptor.forClass(classOf[String])
    verify(mockRemovePage).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def verifyRemovePageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(mockRemovePage, times(numberOfTimes)).apply(any(), any(), any())(any(), any())

  "Nact Code Remove Controller" must {

    onJourney(List(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL), withItem(item)) { request =>
      "return 200 (OK)" that {
        "display page method is invoked and cache is empty" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(item.id, nactCode)(getRequest())

          status(result) mustBe OK
          verifyRemovePageInvoked()

          theNactCode mustBe nactCode
        }
      }

      "return 400 (BAD_REQUEST)" when {
        "user submits an invalid answer" in {
          withNewCaching(request.cacheModel)

          val requestBody = Json.obj("yesNo" -> "invalid")
          val result = controller.submitForm(item.id, nactCode)(postRequest(requestBody))

          status(result) mustBe BAD_REQUEST
          verifyRemovePageInvoked()
        }
      }

      "return 303 (SEE_OTHER)" when {

        "user submits 'Yes' answer" in {
          withNewCaching(request.cacheModel)

          val requestBody = Json.obj("yesNo" -> "Yes")
          val result = controller.submitForm(item.id, nactCode)(postRequest(requestBody))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe NactCodeSummaryController.displayPage(item.id)

          theCacheModelUpdated.itemBy(item.id).flatMap(_.nactCodes) mustBe Some(Seq.empty)
        }

        "user submits 'No' answer" in {
          withNewCaching(request.cacheModel)

          val requestBody = Json.obj("yesNo" -> "No")
          val result = controller.submitForm(item.id, nactCode)(postRequest(requestBody))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe NactCodeSummaryController.displayPage(item.id)

          verifyTheCacheIsUnchanged()
        }
      }

      "redirect to /national-additional-codes-list" when {

        "the 'NACT Remove' page is invoked with invalid code id" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(item.id, "unknown")(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(NactCodeSummaryController.displayPage(item.id).url)
        }

        "the 'submitForm' method is invoked with invalid code id" in {
          withNewCaching(request.cacheModel)

          val body = Json.obj("yesNo" -> "Yes")
          val result = controller.submitForm(item.id, "unknown")(postRequest(body))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(NactCodeSummaryController.displayPage(item.id).url)
        }
      }
    }

    onJourney(CLEARANCE) { request =>
      "return 303 (SEE_OTHER)" when {

        "display page method is invoked" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(item.id, nactCode)(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(RootController.displayPage.url)
        }

        "user submits valid data" in {
          val item = anItem()
          withNewCaching(aDeclarationAfter(request.cacheModel, withItems(item)))

          val requestBody = Json.obj("yesNo" -> "No")
          val result = controller.submitForm(item.id, nactCode)(postRequest(requestBody))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(RootController.displayPage.url)
        }
      }
    }
  }
}
