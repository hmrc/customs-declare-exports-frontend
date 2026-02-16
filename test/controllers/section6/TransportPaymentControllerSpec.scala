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

package controllers.section6

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.general.routes.RootController
import controllers.section6.routes.ContainerController
import forms.section6.TransportPayment
import models.DeclarationType._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section6.transport_payment

class TransportPaymentControllerSpec extends ControllerSpec with AuditedControllerSpec {

  val transportPaymentPage = mock[transport_payment]

  val controller =
    new TransportPaymentController(mockAuthAction, mockJourneyAction, navigator, mockExportsCacheService, mcc, transportPaymentPage)(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration())
    when(transportPaymentPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(transportPaymentPage, auditService)
  }

  def theResponseForm: Form[TransportPayment] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[TransportPayment]])
    verify(transportPaymentPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  private def formData(paymentMethod: String) = JsObject(Map("paymentMethod" -> JsString(paymentMethod)))

  "Transport Payment Controller" should {

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {
          val result = controller.displayPage(getRequest())

          status(result) must be(OK)
          theResponseForm.value mustBe empty
        }

        "display page method is invoked and cache is not empty" in {
          val payment = TransportPayment(TransportPayment.cash)
          withNewCaching(aDeclarationAfter(request.cacheModel, withTransportPayment(Some(payment))))

          val result = controller.displayPage(getRequest())

          status(result) must be(OK)
          theResponseForm.value mustBe Some(payment)
        }
      }

      "return 400 (BAD_REQUEST)" when {
        "form contains incorrect values" in {
          withNewCaching(request.cacheModel)

          val incorrectForm = Json.toJson(TransportPayment("incorrect"))
          val result = controller.submitForm()(postRequest(incorrectForm))

          status(result) must be(BAD_REQUEST)
          verifyNoAudit()
        }
      }

      "return 303 (SEE_OTHER)" when {
        "form contains valid values" in {
          withNewCaching(request.cacheModel)
          val correctForm = formData(TransportPayment.other)

          val result = controller.submitForm()(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe ContainerController.displayContainerSummary()
          verify(transportPaymentPage, times(0)).apply(any())(any(), any())
          verifyAudit()
        }
      }
    }

    onJourney(SUPPLEMENTARY) { request =>
      "return 303 (SEE_OTHER)" when {
        "display page method is invoked" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(getRequest())

          status(result) must be(SEE_OTHER)
          redirectLocation(result) must contain(RootController.displayPage.url)
        }
      }
    }
  }
}
