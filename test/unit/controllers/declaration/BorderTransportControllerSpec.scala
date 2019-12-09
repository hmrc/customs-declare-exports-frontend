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

import controllers.declaration.BorderTransportController
import forms.declaration.TransportCodes.IMOShipIDNumber
import models.{DeclarationType, ExportsDeclaration, Mode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.Call
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.border_transport

class BorderTransportControllerSpec extends ControllerSpec {

  val borderTransportPage = mock[border_transport]

  val controller = new BorderTransportController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    mockExportsCacheService,
    stubMessagesControllerComponents(),
    borderTransportPage
  )(ec)

  override def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(borderTransportPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  private def formData(transportType: String, reference: String, nationality: String) =
    JsObject(
      Map(
        "borderTransportType" -> JsString(transportType),
        "borderTransportReference_IMOShipIDNumber" -> JsString(reference),
        "borderTransportReference_nameOfVessel" -> JsString(reference),
        "borderTransportReference_wagonNumber" -> JsString(reference),
        "borderTransportReference_vehicleRegistrationNumber" -> JsString(reference),
        "borderTransportReference_IATAFlightNumber" -> JsString(reference),
        "borderTransportReference_aircraftRegistrationNumber" -> JsString(reference),
        "borderTransportReference_europeanVesselIDNumber" -> JsString(reference),
        "borderTransportReference_nameOfInlandWaterwayVessel" -> JsString(reference),
        "borderTransportNationality" -> JsString(nationality)
      )
    )

  def borderTransportController(declarationFactory: () => ExportsDeclaration, nextPage: Call): Unit =
    "Transport Details Controller" should {

      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {
          withNewCaching(declarationFactory())

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }

        "display page method is invoked and cache is not empty" in {
          withNewCaching(aDeclarationAfter(declarationFactory(), withBorderTransport()))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "form contains incorrect values" in {
          withNewCaching(declarationFactory())

          val incorrectForm = formData("incorrect", "", "")

          val result = controller.submitForm(Mode.Normal)(postRequest(incorrectForm))

          status(result) must be(BAD_REQUEST)
        }
      }

      "return 303 (SEE_OTHER)" when {
        "valid options are selected" in {
          withNewCaching(declarationFactory())

          val correctForm = formData(IMOShipIDNumber, "SHIP001", "United Kingdom")

          val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe nextPage
        }
      }
    }

  def borderTransportControllerForInvalidJourney(declarationFactory: () => ExportsDeclaration): Unit =
    "Transport Details Controller" should {

      "return 303 (SEE_OTHER)" when {

        "display page method is invoked" in {
          withNewCaching(aDeclarationAfter(declarationFactory(), withBorderTransport()))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(SEE_OTHER)
          redirectLocation(result) mustBe Some(controllers.routes.StartController.displayStartPage.url)
        }

        "valid options are selected" in {
          withNewCaching(declarationFactory())

          val correctForm = formData(IMOShipIDNumber, "SHIP001", "United Kingdom")

          val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

          status(result) must be(SEE_OTHER)
          redirectLocation(result) mustBe Some(controllers.routes.StartController.displayStartPage.url)
        }
      }

    }

  "Transport Details Controller" when {
    "we are on supplementary declaration journey" should {
      def declarationFactory() = aDeclaration(withType(DeclarationType.SUPPLEMENTARY))
      behave like borderTransportController(declarationFactory, controllers.declaration.routes.TransportContainerController.displayContainerSummary())
    }

    "we are on standard declaration journey" should {
      def declarationFactory() = aDeclaration(withType(DeclarationType.STANDARD))
      behave like borderTransportController(declarationFactory, controllers.declaration.routes.TransportPaymentController.displayPage())
    }

    "we are on simplified declaration journey" should {
      def declarationFactory() = aDeclaration(withType(DeclarationType.SIMPLIFIED))
      behave like borderTransportControllerForInvalidJourney(declarationFactory)
    }

    "we are on occasional declaration journey" should {
      def declarationFactory() = aDeclaration(withType(DeclarationType.OCCASIONAL))
      behave like borderTransportControllerForInvalidJourney(declarationFactory)
    }

    "we are on clearance declaration journey" should {
      def declarationFactory() = aDeclaration(withType(DeclarationType.CLEARANCE))
      behave like borderTransportControllerForInvalidJourney(declarationFactory)
    }
  }
}
