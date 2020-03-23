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

import controllers.declaration.{routes, DepartureTransportController}
import forms.declaration.DepartureTransport
import forms.declaration.ModeOfTransportCodes.Maritime
import forms.declaration.TransportCodes.WagonNumber
import models.DeclarationType._
import models.Mode
import models.declaration.Transport
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.departure_transport

import scala.concurrent.Future

class DepartureTransportControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  val borderTransportPage = mock[departure_transport]

  val controller = new DepartureTransportController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    borderTransportPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    setupErrorHandler()
    authorizedUser()
    when(borderTransportPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  private def nextPage(decType: DeclarationType) = decType match {
    case SUPPLEMENTARY => routes.BorderTransportController.displayPage()
    case STANDARD      => routes.BorderTransportController.displayPage()
    case CLEARANCE     => routes.TransportContainerController.displayContainerSummary()
  }

  private def formData(transportType: String, reference: String) =
    JsObject(
      Map(
        DepartureTransport.meansOfTransportOnDepartureTypeKey -> JsString(transportType),
        s"${DepartureTransport.meansOfTransportOnDepartureIDNumberKey}_$transportType" -> JsString(reference)
      )
    )

  "Border transport controller" when {
    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE)() { declaration =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {
          withNewCaching(declaration)

          val result: Future[Result] = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }

        "display page method is invoked and cache contains data" in {

          withNewCaching(aDeclarationAfter(declaration, withDepartureTransport(Maritime, WagonNumber, "FAA")))

          val result: Future[Result] = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "no option is selected" in {
          withNewCaching(declaration)

          val correctForm: JsValue = formData("", "")

          val result: Future[Result] = controller.submitForm(Mode.Normal)(postRequest(correctForm))

          status(result) must be(BAD_REQUEST)
        }

        "form is incorrect" in {
          withNewCaching(declaration)

          val incorrectForm: JsValue = formData("wrongValue", "FAA")

          val result: Future[Result] = controller.submitForm(Mode.Normal)(postRequest(incorrectForm))

          status(result) must be(BAD_REQUEST)
        }
      }

      "return 303 (SEE_OTHER)" when {

        "information provided by user are correct" in {
          withNewCaching(declaration)

          val correctForm: JsValue = formData(WagonNumber, "FAA")

          val result: Future[Result] = controller.submitForm(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe nextPage(declaration.`type`)
        }
      }
    }

    onJourney(CLEARANCE)() { declaration =>
      "return 303 (SEE_OTHER)" when {

        "'none' option is selected" in {
          withNewCaching(declaration)

          val correctForm: JsValue = formData(Transport.optionNone, "")

          val result: Future[Result] = controller.submitForm(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe nextPage(declaration.`type`)
        }
      }
    }

    onJourney(SIMPLIFIED, OCCASIONAL)() { declaration =>
      "return 303 (SEE_OTHER)" when {

        "display page method is invoked" in {
          withNewCaching(aDeclarationAfter(declaration, withBorderTransport()))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(SEE_OTHER)
          redirectLocation(result) mustBe Some(controllers.routes.StartController.displayStartPage.url)
        }

        "page is submitted" in {
          withNewCaching(declaration)

          val correctForm: JsValue = formData(WagonNumber, "FAA")

          val result: Future[Result] = controller.submitForm(Mode.Normal)(postRequest(correctForm))

          status(result) must be(SEE_OTHER)
          redirectLocation(result) mustBe Some(controllers.routes.StartController.displayStartPage.url)
        }
      }

    }

  }

}
