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

import base.{AuditedControllerSpec, ControllerSpec, MockTransportCodeService}
import controllers.declaration.routes.{BorderTransportController, ExpressConsignmentController, TransportCountryController}
import controllers.helpers.TransportSectionHelper.{postalOrFTIModeOfTransportCodes, Guernsey, Jersey}
import forms.declaration.DepartureTransport
import forms.declaration.DepartureTransport.radioButtonGroupId
import forms.declaration.InlandOrBorder.Border
import forms.declaration.ModeOfTransportCode.Maritime
import forms.declaration.countries.Country
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

class DepartureTransportControllerSpec extends ControllerSpec with AuditedControllerSpec {

  val transportCodeService = MockTransportCodeService.transportCodeService

  val departureTransportPage = mock[departure_transport]
  val departureTransportHelper = mock[DepartureTransportHelper]

  val controller = new DepartureTransportController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    mcc,
    transportCodeService,
    departureTransportHelper,
    departureTransportPage
  )(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    setupErrorHandler()
    authorizedUser()
    when(departureTransportHelper.transportCodes(any())).thenReturn(transportCodeService.transportCodesForV1)
    when(departureTransportPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(departureTransportHelper, departureTransportPage, auditService)
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

  private val departureNumber = "FAA"
  private val departureType = transportCodeService.WagonNumber.value
  private val departureTransport = withDepartureTransport(Maritime, departureType, departureNumber)

  "Departure transport controller" should {

    onEveryDeclarationJourney() { request =>
      "reset the value, if any, and redirect to the 'next' page" when {
        postalOrFTIModeOfTransportCodes.foreach { modeOfTransportCode =>
          s"TransportLeavingTheBorder is $modeOfTransportCode" should {

            "the 'displayPage' is invoked" in {
              val departureTransport = withDepartureTransport(modeOfTransportCode.value, departureType, departureNumber)
              withNewCaching(aDeclarationAfter(request.cacheModel, departureTransport))

              val result = controller.displayPage(getRequest())

              status(result) must be(SEE_OTHER)

              if (request.declarationType == CLEARANCE) thePageNavigatedTo mustBe ExpressConsignmentController.displayPage
              else thePageNavigatedTo mustBe BorderTransportController.displayPage

              val transport = theCacheModelUpdated.transport
              transport.meansOfTransportOnDepartureType mustBe None
              transport.meansOfTransportOnDepartureIDNumber mustBe None
            }

            "the 'submitForm' is invoked" in {
              val departureTransport = withDepartureTransport(modeOfTransportCode.value, departureType, departureNumber)
              withNewCaching(aDeclarationAfter(request.cacheModel, departureTransport))

              val correctForm = formData(transportCodeService.WagonNumber.value, transportCodeService.WagonNumber.id, departureNumber)

              val result = controller.submitForm()(postRequest(correctForm))

              status(result) mustBe SEE_OTHER
              if (request.declarationType == CLEARANCE) thePageNavigatedTo mustBe ExpressConsignmentController.displayPage
              else thePageNavigatedTo mustBe BorderTransportController.displayPage

              val transport = theCacheModelUpdated.transport
              transport.meansOfTransportOnDepartureType mustBe None
              transport.meansOfTransportOnDepartureIDNumber mustBe None
              verifyAudit()
            }
          }
        }
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(getRequest())
          status(result) must be(OK)
        }

        "display page method is invoked and cache contains data" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, departureTransport))

          val result = controller.displayPage(getRequest())
          status(result) must be(OK)
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "no option is selected" in {
          withNewCaching(request.cacheModel)

          val correctForm = formData("", "", "")

          val result = controller.submitForm()(postRequest(correctForm))
          status(result) must be(BAD_REQUEST)
          verifyNoAudit()
        }

        "form is incorrect" in {
          withNewCaching(request.cacheModel)

          val incorrectForm = formData("wrongValue", transportCodeService.WagonNumber.id, departureNumber)

          val result = controller.submitForm()(postRequest(incorrectForm))
          status(result) must be(BAD_REQUEST)
          verifyNoAudit()
        }
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY) { request =>
      "return 303 (SEE_OTHER) and redirect to the 'next page'" when {
        List(Guernsey, Jersey).foreach { country =>
          s"the 'displayPage' method is invoked and Destination country is '$country'" in {
            val destinationCountry = withDestinationCountry(Country(Some(country)))
            withNewCaching(aDeclarationAfter(request.cacheModel, destinationCountry, departureTransport))

            val result = controller.displayPage(getRequest())

            status(result) mustBe SEE_OTHER
            thePageNavigatedTo mustBe BorderTransportController.displayPage

            val transport = theCacheModelUpdated.transport
            transport.meansOfTransportOnDepartureType mustBe None
            transport.meansOfTransportOnDepartureIDNumber mustBe None
          }

          s"the 'submitForm' method is invoked and Destination country is '$country'" in {
            val destinationCountry = withDestinationCountry(Country(Some(country)))
            withNewCaching(aDeclarationAfter(request.cacheModel, destinationCountry, departureTransport))

            val correctForm = formData(transportCodeService.WagonNumber.value, transportCodeService.WagonNumber.id, departureNumber)
            val result = controller.submitForm(postRequest(correctForm))

            status(result) mustBe SEE_OTHER
            thePageNavigatedTo mustBe BorderTransportController.displayPage

            val transport = theCacheModelUpdated.transport
            transport.meansOfTransportOnDepartureType mustBe None
            transport.meansOfTransportOnDepartureIDNumber mustBe None
          }
        }
      }

      "redirect to the /border-transport page" when {
        "information provided by user are correct" in {
          withNewCaching(request.cacheModel)

          val correctForm = formData(transportCodeService.WagonNumber.value, transportCodeService.WagonNumber.id, departureNumber)
          val result = controller.submitForm()(postRequest(correctForm))

          status(result) must be(SEE_OTHER)
          thePageNavigatedTo mustBe BorderTransportController.displayPage
          verifyAudit()
        }
      }

      "redirect to the /transport-country page" when {
        "the user select 'Border' on /inland-or-border" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withInlandOrBorder(Some(Border))))

          val correctForm = formData(transportCodeService.WagonNumber.value, transportCodeService.WagonNumber.id, departureNumber)

          val result = controller.submitForm()(postRequest(correctForm))

          status(result) must be(SEE_OTHER)
          thePageNavigatedTo mustBe TransportCountryController.displayPage
          verifyAudit()
        }
      }
    }

    onJourney(OCCASIONAL, SIMPLIFIED) { request =>
      "redirect to /border-country" when {
        "the 'displayPage' is invoked" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(getRequest())

          status(result) must be(SEE_OTHER)

          thePageNavigatedTo mustBe BorderTransportController.displayPage

          val transport = theCacheModelUpdated.transport
          transport.meansOfTransportOnDepartureType mustBe None
          transport.meansOfTransportOnDepartureIDNumber mustBe None
        }

        "the 'submitForm' is invoked" in {
          withNewCaching(request.cacheModel)

          val correctForm = formData(transportCodeService.WagonNumber.value, transportCodeService.WagonNumber.id, departureNumber)

          val result = controller.submitForm()(postRequest(correctForm))

          status(result) mustBe SEE_OTHER

          thePageNavigatedTo mustBe BorderTransportController.displayPage

          val transport = theCacheModelUpdated.transport
          transport.meansOfTransportOnDepartureType mustBe None
          transport.meansOfTransportOnDepartureIDNumber mustBe None
        }
      }
    }

    onJourney(CLEARANCE) { request =>
      "redirect to the /express-consignment page" when {

        "information provided by user are correct" in {
          withNewCaching(request.cacheModel)

          val correctForm = formData(transportCodeService.WagonNumber.value, transportCodeService.WagonNumber.id, departureNumber)

          val result = controller.submitForm()(postRequest(correctForm))

          status(result) must be(SEE_OTHER)
          thePageNavigatedTo mustBe ExpressConsignmentController.displayPage
          verifyAudit()
        }

        "'0019' has been entered as Procedure Code and" when {
          "the 'NotApplicable' radio element is selected" in {
            when(departureTransportHelper.transportCodes(any())).thenReturn(transportCodeService.transportCodesForV3WhenPC0019)
            withNewCaching(request.cacheModel)

            val correctForm = formData(transportCodeService.NotApplicable.value, "", "")

            val result = controller.submitForm()(postRequest(correctForm))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe ExpressConsignmentController.displayPage
            verifyAudit()
          }
        }
      }
    }
  }
}
