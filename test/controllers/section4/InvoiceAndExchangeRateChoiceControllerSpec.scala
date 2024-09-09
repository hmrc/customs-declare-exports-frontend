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

package controllers.section4

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.general.routes.RootController
import controllers.section4.routes.{InvoiceAndExchangeRateController, TotalPackageQuantityController}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.DeclarationType
import models.DeclarationType._
import models.requests.JourneyRequest
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.Assertion
import play.api.data.Form
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{AnyContentAsEmpty, Call, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section4.invoice_and_exchange_rate_choice

class InvoiceAndExchangeRateChoiceControllerSpec extends ControllerSpec with AuditedControllerSpec {

  private val invoiceAndExchangeRateChoicePage = mock[invoice_and_exchange_rate_choice]

  val controller = new InvoiceAndExchangeRateChoiceController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    mcc,
    invoiceAndExchangeRateChoicePage,
    mockExportsCacheService
  )(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.STANDARD)))
    when(invoiceAndExchangeRateChoicePage.apply(any[Form[YesNoAnswer]])(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(invoiceAndExchangeRateChoicePage)
    super.afterEach()
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(invoiceAndExchangeRateChoicePage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    await(controller.displayPage(request))
    theResponseForm
  }

  "InvoiceAndExchangeRateChoiceController on displayOutcomePage" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {
        val result = controller.displayPage(getRequest())
        status(result) must be(OK)
        verifyPageInvoked
      }

      "display page method is invoked and cache is not empty" in {
        withNewCaching(aDeclaration(withTotalNumberOfItems(Some("100000"))))

        val result = controller.displayPage(getRequest())
        status(result) must be(OK)
        verifyPageInvoked
      }
    }

    onJourney(CLEARANCE, SIMPLIFIED, OCCASIONAL) { request =>
      "redirect to the starting page" in {
        withNewCaching(request.cacheModel)

        val result = controller.displayPage(getRequest())
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
      }
    }
  }

  "InvoiceAndExchangeRateChoiceController on submitForm" should {

    onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
      "return 303 (SEE_OTHER) and redirect to the /total-package-quantity page" when {
        "the answer is 'yes'" in {
          verifyRedirect(YesNoAnswers.yes, TotalPackageQuantityController.displayPage)
          verifyAudit()
        }
      }

      "return 303 (SEE_OTHER) and redirect to the /invoices-and-exchange-rate page" when {
        "the answer is 'no'" in {
          verifyRedirect(YesNoAnswers.no, InvoiceAndExchangeRateController.displayPage)
          verifyNoAudit()
        }
      }
    }

    onJourney(CLEARANCE, SIMPLIFIED, OCCASIONAL) { request =>
      "redirect to the starting page" in {
        withNewCaching(request.cacheModel)

        val result = controller.submitForm()(postRequest(JsString("")))
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
        verifyNoAudit()
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form contains incorrect values" in {
        val incorrectForm = Json.obj("yesNo" -> "wrong")

        val result = controller.submitForm()(postRequest(incorrectForm))
        status(result) must be(BAD_REQUEST)
        verifyPageInvoked
        verifyNoAudit()
      }

      "neither Yes or No have been selected on the page" in {
        val incorrectForm = Json.obj("yesNo" -> "")

        val result = controller.submitForm()(postRequest(incorrectForm))
        status(result) must be(BAD_REQUEST)
        verifyPageInvoked
        verifyNoAudit()
      }
    }
  }

  private def verifyPageInvoked: HtmlFormat.Appendable =
    verify(invoiceAndExchangeRateChoicePage).apply(any[Form[YesNoAnswer]])(any(), any())

  private def verifyRedirect(yesOrNo: String, call: Call)(implicit request: JourneyRequest[_]): Assertion = {
    withNewCaching(request.cacheModel)
    val correctForm = Json.obj("yesNo" -> yesOrNo)

    val result = controller.submitForm()(postRequest(correctForm))

    status(result) mustBe SEE_OTHER
    thePageNavigatedTo mustBe call
  }
}
