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
import controllers.declaration.routes.{BorderTransportController, ExpressConsignmentController, TransportCountryController}
import controllers.helpers.TransportSectionHelper.postalOrFTIModeOfTransportCodes
import controllers.routes.RootController
import forms.declaration.DepartureTransport
import forms.declaration.DepartureTransport.radioButtonGroupId
import forms.declaration.InlandOrBorder.Border
import forms.declaration.ModeOfTransportCode.Maritime
import mock.ErrorHandlerMocks
import models.DeclarationType._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.helpers.DepartureTransportHelper
import views.html.declaration.departure_transport

class DepartureTransportControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  val transportCodeService = MockTransportCodeService.transportCodeService

  val departureTransportPage = mock[departure_transport]
  val departureTransportHelper = mock[DepartureTransportHelper]

  val controller = new DepartureTransportController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    transportCodeService,
    departureTransportHelper,
    departureTransportPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    setupErrorHandler()
    authorizedUser()
    when(departureTransportHelper.transportCodes(any())).thenReturn(transportCodeService.transportCodesForV1)
    when(departureTransportPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(departureTransportHelper, departureTransportPage)
    super.afterEach()
  }

  def theResponseForm: Form[DepartureTransport] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[DepartureTransport]])
    verify(departureTransportPage).apply(formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  private def formData(transportCodeValue: String, inputFieldId: String, reference: String): JsObject =
    Json.obj(radioButtonGroupId -> transportCodeValue, inputFieldId -> reference)

  "Departure transport controller" when {
    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE, SIMPLIFIED) { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(getRequest())
          status(result) must be(OK)
        }

        "display page method is invoked and cache contains data" in {
          val departureTransport = withDepartureTransport(Maritime, transportCodeService.WagonNumber.value, "FAA")
          withNewCaching(aDeclarationAfter(request.cacheModel, departureTransport))

          val result = controller.displayPage(getRequest())
          status(result) must be(OK)
        }
      }

      postalOrFTIModeOfTransportCodes.foreach { modeOfTransportCode =>
        s"TransportLeavingTheBorder is $modeOfTransportCode" should {
          "redirect to the starting page on displayOutcomePage" in {
            withNewCaching(aDeclarationAfter(request.cacheModel, withBorderModeOfTransportCode(modeOfTransportCode)))

            val result = controller.displayPage(getRequest())

            status(result) must be(SEE_OTHER)
            redirectLocation(result) mustBe Some(RootController.displayPage.url)
          }
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "no option is selected" in {
          withNewCaching(request.cacheModel)

          val correctForm = formData("", "", "")

          val result = controller.submitForm()(postRequest(correctForm))
          status(result) must be(BAD_REQUEST)
        }

        "form is incorrect" in {
          withNewCaching(request.cacheModel)

          val incorrectForm = formData("wrongValue", transportCodeService.WagonNumber.id, "FAA")

          val result = controller.submitForm()(postRequest(incorrectForm))
          status(result) must be(BAD_REQUEST)
        }
      }

      postalOrFTIModeOfTransportCodes.foreach { modeOfTransportCode =>
        s"TransportLeavingTheBorder is $modeOfTransportCode" should {
          "redirect to the starting page on submitForm" in {
            withNewCaching(aDeclarationAfter(request.cacheModel, withBorderModeOfTransportCode(modeOfTransportCode)))

            val correctForm = formData(transportCodeService.WagonNumber.value, transportCodeService.WagonNumber.id, "FAA")

            val result = controller.submitForm()(postRequest(correctForm))
            redirectLocation(result) mustBe Some(RootController.displayPage.url)
          }
        }
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED) { request =>
      "redirect to the /border-transport page" when {
        "information provided by user are correct" in {
          withNewCaching(request.cacheModel)

          val correctForm = formData(transportCodeService.WagonNumber.value, transportCodeService.WagonNumber.id, "FAA")

          val result = controller.submitForm()(postRequest(correctForm))

          status(result) must be(SEE_OTHER)
          thePageNavigatedTo mustBe BorderTransportController.displayPage
        }
      }

      "redirect to the /transport-country page" when {
        "the user select 'Border' on /inland-or-border" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withInlandOrBorder(Some(Border))))

          val correctForm = formData(transportCodeService.WagonNumber.value, transportCodeService.WagonNumber.id, "FAA")

          val result = controller.submitForm()(postRequest(correctForm))

          status(result) must be(SEE_OTHER)
          thePageNavigatedTo mustBe TransportCountryController.displayPage
        }
      }
    }

    onJourney(CLEARANCE) { request =>
      "redirect to the /express-consignment page" when {

        "information provided by user are correct" in {
          withNewCaching(request.cacheModel)

          val correctForm = formData(transportCodeService.WagonNumber.value, transportCodeService.WagonNumber.id, "FAA")

          val result = controller.submitForm()(postRequest(correctForm))

          status(result) must be(SEE_OTHER)
          thePageNavigatedTo mustBe ExpressConsignmentController.displayPage
        }

        "'0019' has been entered as Procedure Code and" when {
          "the 'NotApplicable' radio element is selected" in {
            when(departureTransportHelper.transportCodes(any())).thenReturn(transportCodeService.transportCodesForV3WhenPC0019)
            withNewCaching(request.cacheModel)

            val correctForm = formData(transportCodeService.NotApplicable.value, "", "")

            val result = controller.submitForm()(postRequest(correctForm))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe ExpressConsignmentController.displayPage
          }
        }
      }
    }

    onJourney(OCCASIONAL) { request =>
      "redirect to the 'start page'" when {

        "displayOutcomePage is invoked" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(getRequest())
          redirectLocation(result) mustBe Some(RootController.displayPage.url)
        }

        "submitForm is invoked" in {
          withNewCaching(request.cacheModel)

          val correctForm = formData(transportCodeService.WagonNumber.value, transportCodeService.WagonNumber.id, "FAA")

          val result = controller.submitForm()(postRequest(correctForm))
          redirectLocation(result) mustBe Some(RootController.displayPage.url)
        }
      }
    }
  }
}
