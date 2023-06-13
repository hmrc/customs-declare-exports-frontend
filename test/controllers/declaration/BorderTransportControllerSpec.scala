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

import base.{ControllerSpec, MockTransportCodeService}
import controllers.routes.RootController
import forms.declaration.BorderTransport
import forms.declaration.BorderTransport.radioButtonGroupId
import models.DeclarationType._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.border_transport

class BorderTransportControllerSpec extends ControllerSpec {

  implicit val transportCodeService = MockTransportCodeService.transportCodeService

  val borderTransportPage = mock[border_transport]

  val controller = new BorderTransportController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    mockExportsCacheService,
    stubMessagesControllerComponents(),
    transportCodeService,
    borderTransportPage
  )(ec)

  def theResponseForm: Form[BorderTransport] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[BorderTransport]])
    verify(borderTransportPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(borderTransportPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(borderTransportPage)
    super.afterEach()
  }

  private def formData(transportType: String, reference: String): JsObject =
    Json.obj(
      radioButtonGroupId -> transportType,
      transportCodeService.ShipOrRoroImoNumber.id -> reference,
      transportCodeService.NameOfVessel.id -> reference,
      transportCodeService.WagonNumber.id -> reference,
      transportCodeService.VehicleRegistrationNumber.id -> reference,
      transportCodeService.FlightNumber.id -> reference,
      transportCodeService.AircraftRegistrationNumber.id -> reference,
      transportCodeService.EuropeanVesselIDNumber.id -> reference,
      transportCodeService.NameOfInlandWaterwayVessel.id -> reference
    )

  "Transport Details Controller" should {

    onJourney(STANDARD, OCCASIONAL, SUPPLEMENTARY, SIMPLIFIED) { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(getRequest())
          status(result) must be(OK)
        }

        "display page method is invoked and cache is not empty" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withBorderTransport()))

          val result = controller.displayPage(getRequest())
          status(result) must be(OK)
        }
      }

      "return 400 (BAD_REQUEST)" when {
        "form contains incorrect values" in {
          withNewCaching(request.cacheModel)

          val incorrectForm = formData("incorrect", "")

          val result = controller.submitForm()(postRequest(incorrectForm))
          status(result) must be(BAD_REQUEST)
        }
      }

      "return 303 (SEE_OTHER)" when {
        "valid options are selected" in {
          withNewCaching(request.cacheModel)

          val correctForm = formData(transportCodeService.ShipOrRoroImoNumber.value, "SHIP001")

          val result = controller.submitForm()(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.TransportCountryController.displayPage
        }
      }
    }

    onJourney(CLEARANCE) { request =>
      "redirect to the start page" when {

        "the 'displayOutcomePage' method is invoked" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(getRequest())

          status(result) must be(SEE_OTHER)
          redirectLocation(result) mustBe Some(RootController.displayPage.url)
        }

        "the 'submitForm' method is invoked" in {
          withNewCaching(request.cacheModel)

          val correctForm = formData(transportCodeService.ShipOrRoroImoNumber.value, "SHIP001")

          val result = controller.submitForm()(postRequest(correctForm))

          status(result) must be(SEE_OTHER)
          redirectLocation(result) mustBe Some(RootController.displayPage.url)
        }
      }
    }
  }
}
