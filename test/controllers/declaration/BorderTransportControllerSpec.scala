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
import connectors.CodeListConnector
import forms.declaration.BorderTransport
import forms.declaration.TransportCodes.shipOrRoroImoNumber
import models.DeclarationType._
import models.Mode
import models.codes.Country
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.border_transport

import scala.collection.immutable.ListMap

class BorderTransportControllerSpec extends ControllerSpec {

  val borderTransportPage = mock[border_transport]
  val mockCodeListConnector = mock[CodeListConnector]

  val controller = new BorderTransportController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    mockExportsCacheService,
    stubMessagesControllerComponents(),
    borderTransportPage
  )(ec, mockCodeListConnector)

  def theResponseForm: Form[BorderTransport] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[BorderTransport]])
    verify(borderTransportPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(borderTransportPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> Country("United Kingdom, Great Britain, Northern Ireland", "GB")))
  }

  override protected def afterEach(): Unit = {
    reset(borderTransportPage, mockCodeListConnector)
    super.afterEach()
  }

  private def nextPage(decType: DeclarationType) = decType match {
    case STANDARD      => routes.ExpressConsignmentController.displayPage()
    case SUPPLEMENTARY => routes.TransportContainerController.displayContainerSummary()
  }

  private def formData(transportType: String, reference: String, nationality: String) =
    JsObject(
      Map(
        "borderTransportType" -> JsString(transportType),
        "borderTransportReference_shipOrRoroImoNumber" -> JsString(reference),
        "borderTransportReference_nameOfVessel" -> JsString(reference),
        "borderTransportReference_wagonNumber" -> JsString(reference),
        "borderTransportReference_vehicleRegistrationNumber" -> JsString(reference),
        "borderTransportReference_flightNumber" -> JsString(reference),
        "borderTransportReference_aircraftRegistrationNumber" -> JsString(reference),
        "borderTransportReference_europeanVesselIDNumber" -> JsString(reference),
        "borderTransportReference_nameOfInlandWaterwayVessel" -> JsString(reference),
        "borderTransportNationality" -> JsString(nationality)
      )
    )

  "Transport Details Controller" when {
    onJourney(STANDARD, SUPPLEMENTARY) { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }

        "display page method is invoked and cache is not empty" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withBorderTransport()))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "form contains incorrect values" in {
          withNewCaching(request.cacheModel)

          val incorrectForm = formData("incorrect", "", "")

          val result = controller.submitForm(Mode.Normal)(postRequest(incorrectForm))

          status(result) must be(BAD_REQUEST)
        }
      }

      "return 303 (SEE_OTHER)" when {
        "valid options are selected" in {
          withNewCaching(request.cacheModel)

          val correctForm = formData(shipOrRoroImoNumber, "SHIP001", "United Kingdom, Great Britain, Northern Ireland")

          val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe nextPage(request.declarationType)
        }
      }
    }

    onJourney(SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
      "display page method is invoked" in {
        withNewCaching(aDeclarationAfter(request.cacheModel, withBorderTransport()))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(SEE_OTHER)
        redirectLocation(result) mustBe Some(controllers.routes.RootController.displayPage().url)
      }

      "valid options are selected" in {
        withNewCaching(request.cacheModel)

        val correctForm = formData(shipOrRoroImoNumber, "SHIP001", "United Kingdom, Great Britain, Northern Ireland")

        val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) mustBe Some(controllers.routes.RootController.displayPage().url)
      }
    }
  }
}
