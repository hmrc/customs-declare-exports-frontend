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

import controllers.declaration.TransportPaymentController
import forms.declaration.TransportPayment
import models.DeclarationType.DeclarationType
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.transport_payment

class TransportPaymentControllerSpec extends ControllerSpec {

  val transportPaymentPage = mock[transport_payment]

  val controller =
    new TransportPaymentController(
      mockAuthAction,
      mockJourneyAction,
      navigator,
      mockExportsCacheService,
      stubMessagesControllerComponents(),
      transportPaymentPage
    )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.STANDARD)))
    when(transportPaymentPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(transportPaymentPage)
  }

  def theResponseForm: Form[TransportPayment] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[TransportPayment]])
    verify(transportPaymentPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  private def formData(paymentMethod: String) = JsObject(Map("paymentMethod" -> JsString(paymentMethod)))

  "Transport Payment Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(OK)
        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache is not empty" in {

        val payment = TransportPayment(Some(TransportPayment.cash))
        withNewCaching(aDeclaration(withTransportPayment(Some(payment))))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(OK)
        theResponseForm.value mustBe Some(payment)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form contains incorrect values" in {

        val incorrectForm = Json.toJson(TransportPayment(Some("incorrect")))

        val result = controller.submitForm(Mode.Normal)(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      def controllerRedirectsToNextPage(decType: DeclarationType, call: Call): Unit =
        "accept submission and redirect" in {
          withNewCaching(aDeclaration(withType(decType)))
          val correctForm = formData(TransportPayment.other)

          val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe call
          verify(transportPaymentPage, times(0)).apply(any(), any())(any(), any())
        }

      def controllerRedirectsToStartPageForInvalidType(decType: DeclarationType): Unit =
        "accept submission and redirect" in {
          withNewCaching(aDeclaration(withType(decType)))
          val correctForm = formData(TransportPayment.other)

          val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

          status(result) must be(SEE_OTHER)
          redirectLocation(result) mustBe Some(controllers.routes.StartController.displayStartPage.url)
          verify(transportPaymentPage, times(0)).apply(any(), any())(any(), any())
        }

      for (decType <- DeclarationType.values.filter(_ != DeclarationType.CLEARANCE)) {
        s"we are on $decType journey" should {
          behave like controllerRedirectsToNextPage(
            decType,
            controllers.declaration.routes.TransportContainerController.displayContainerSummary(Mode.Normal)
          )
        }
      }

      "we are on a clearance request journey" should {
        behave like controllerRedirectsToStartPageForInvalidType(DeclarationType.CLEARANCE)
      }

    }
  }
}
