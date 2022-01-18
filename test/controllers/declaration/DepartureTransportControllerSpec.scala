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
import forms.declaration.ModeOfTransportCode.Maritime
import forms.declaration.TransportCodes.wagonNumber
import forms.declaration.{DepartureTransport, TransportCodes}
import mock.ErrorHandlerMocks
import models.DeclarationType._
import models.Mode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.mvc.{AnyContentAsEmpty, Request, Result}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
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

  override protected def afterEach(): Unit = {
    reset(borderTransportPage)
    super.afterEach()
  }

  def theResponseForm: Form[DepartureTransport] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[DepartureTransport]])
    verify(borderTransportPage).apply(any(), formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
  }

  private def nextPage(decType: DeclarationType) = decType match {
    case STANDARD | SUPPLEMENTARY => routes.BorderTransportController.displayPage()
    case CLEARANCE                => routes.ExpressConsignmentController.displayPage()
  }

  private def formData(transportType: String, reference: String) =
    JsObject(
      Map(
        DepartureTransport.meansOfTransportOnDepartureTypeKey -> JsString(transportType),
        s"${DepartureTransport.meansOfTransportOnDepartureIDNumberKey}_$transportType" -> JsString(reference)
      )
    )

  "Departure transport controller" when {
    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {
          withNewCaching(request.cacheModel)

          val result: Future[Result] = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }

        "display page method is invoked and cache contains data" in {

          withNewCaching(aDeclarationAfter(request.cacheModel, withDepartureTransport(Maritime, wagonNumber, "FAA")))

          val result: Future[Result] = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "no option is selected" in {
          withNewCaching(request.cacheModel)

          val correctForm: JsValue = formData("", "")

          val result: Future[Result] = controller.submitForm(Mode.Normal)(postRequest(correctForm))

          status(result) must be(BAD_REQUEST)
        }

        "form is incorrect" in {
          withNewCaching(request.cacheModel)

          val incorrectForm: JsValue = formData("wrongValue", "FAA")

          val result: Future[Result] = controller.submitForm(Mode.Normal)(postRequest(incorrectForm))

          status(result) must be(BAD_REQUEST)
        }
      }

      "return 303 (SEE_OTHER)" when {

        "information provided by user are correct" in {
          withNewCaching(request.cacheModel)

          val correctForm: JsValue = formData(wagonNumber, "FAA")

          val result: Future[Result] = controller.submitForm(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe nextPage(request.declarationType)
        }
      }
    }

    onJourney(CLEARANCE) { request =>
      "return 303 (SEE_OTHER)" when {

        "'none' option is selected" in {
          withNewCaching(request.cacheModel)

          val correctForm: JsValue = formData(TransportCodes.notApplicable, "")

          val result: Future[Result] = controller.submitForm(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe nextPage(request.declarationType)
        }
      }
    }

    onJourney(SIMPLIFIED, OCCASIONAL) { request =>
      "return 303 (SEE_OTHER)" when {

        "display page method is invoked" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withBorderTransport()))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(SEE_OTHER)
          redirectLocation(result) mustBe Some(controllers.routes.RootController.displayPage().url)
        }

        "page is submitted" in {
          withNewCaching(request.cacheModel)

          val correctForm: JsValue = formData(wagonNumber, "FAA")

          val result: Future[Result] = controller.submitForm(Mode.Normal)(postRequest(correctForm))

          status(result) must be(SEE_OTHER)
          redirectLocation(result) mustBe Some(controllers.routes.RootController.displayPage().url)
        }
      }

    }

  }

}
