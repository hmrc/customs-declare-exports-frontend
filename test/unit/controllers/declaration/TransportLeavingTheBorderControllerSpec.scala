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

import controllers.declaration.TransportLeavingTheBorderController
import forms.declaration.{ModeOfTransportCode, TransportLeavingTheBorder}
import models.DeclarationType.{CLEARANCE, STANDARD, SUPPLEMENTARY}
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.transport_leaving_the_border

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

  def theResponseForm: Form[TransportLeavingTheBorder] = {
    val captor: ArgumentCaptor[Form[TransportLeavingTheBorder]] = ArgumentCaptor.forClass(classOf[Form[TransportLeavingTheBorder]])
    verify(transportLeavingTheBorder).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  "Transport Leaving The Border Controller" must {
    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
          theResponseForm.value mustBe empty
        }

        "display page method is invoked and cache is not empty" in {

          withNewCaching(aDeclarationAfter(request.cacheModel, withDepartureTransport(ModeOfTransportCode.Rail, "", "")))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
          theResponseForm.value mustBe Some(TransportLeavingTheBorder(Some(ModeOfTransportCode.Rail)))
        }

        "with submission errors" in {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal)(getRequestWithSubmissionErrors)
          status(result) mustBe OK

          theResponseForm.errors mustBe Seq(submissionFormError)
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "form contains incorrect values" in {

          withNewCaching(request.cacheModel)

          val result = controller.submitForm(Mode.Normal)(postRequest(Json.obj()))

          status(result) must be(BAD_REQUEST)
        }
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { request =>
      "return 303 (SEE_OTHER)" when {

        "form contains valid values" in {
          withNewCaching(request.cacheModel)
          val correctForm = Json.obj("transportLeavingTheBorder" -> ModeOfTransportCode.Rail.value)

          val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.DepartureTransportController.displayPage(Mode.Normal)
          verify(transportLeavingTheBorder, times(0)).apply(any(), any())(any(), any())
        }
      }
    }
  }
}
