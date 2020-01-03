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

import controllers.declaration.{routes, TransportLeavingTheBorderController}
import forms.declaration.{ModeOfTransportCodes, TransportPayment}
import models.{DeclarationType, Mode}
import models.DeclarationType.{CLEARANCE, STANDARD, SUPPLEMENTARY}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.test.Helpers._
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers.{await, status, BAD_REQUEST, OK}
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.transport_leaving_the_border

import scala.concurrent.ExecutionContext

class TransportLeavingTheBorderControllerSpec extends ControllerSpec {

  val transportLeavingTheBorder = mock[transport_leaving_the_border]

  val controller = new TransportLeavingTheBorderController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    transportLeavingTheBorder
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.STANDARD)))
    when(transportLeavingTheBorder.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(transportLeavingTheBorder)
    super.afterEach()
  }

  def theResponseForm: Form[ModeOfTransportCodes] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ModeOfTransportCodes]])
    verify(transportLeavingTheBorder).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  "Transport Leaving The Border Controller" must {
    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE)() { declaration =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
          theResponseForm.value mustBe empty
        }

        "display page method is invoked and cache is not empty" in {

          withNewCaching(aDeclarationAfter(declaration, withDepartureTransport(ModeOfTransportCodes.Rail, "", "")))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
          theResponseForm.value mustBe Some(ModeOfTransportCodes.Rail)
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "form contains incorrect values" in {

          withNewCaching(declaration)

          val result = controller.submitForm(Mode.Normal)(postRequest(Json.obj()))

          status(result) must be(BAD_REQUEST)
        }
      }

    }

    onJourney(STANDARD, SUPPLEMENTARY)() { declaration =>
      "return 303 (SEE_OTHER)" when {

        "form contains valid values" in {
          withNewCaching(declaration)
          val correctForm = Json.obj("code" -> ModeOfTransportCodes.Rail.value)

          val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.DepartureTransportController.displayPage()
          verify(transportLeavingTheBorder, times(0)).apply(any(), any())(any(), any())
        }
      }
    }

    onClearance { declaration =>
      "return 303 (SEE_OTHER)" when {

        "form contains valid values" in {
          withNewCaching(declaration)
          val correctForm = Json.obj("code" -> ModeOfTransportCodes.Rail.value)

          val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.TransportContainerController.displayContainerSummary()
          verify(transportLeavingTheBorder, times(0)).apply(any(), any())(any(), any())
        }
      }
    }
  }
}
