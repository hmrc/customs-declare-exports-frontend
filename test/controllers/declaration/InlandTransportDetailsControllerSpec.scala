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
import controllers.declaration.routes.{DepartureTransportController, ExpressConsignmentController, TransportContainerController}
import controllers.helpers.TransportSectionHelper.postalOrFTIModeOfTransportCodes
import controllers.routes.RootController
import forms.declaration.InlandModeOfTransportCode
import forms.declaration.ModeOfTransportCode._
import models.DeclarationType._
import models.Mode.Normal
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.{GivenWhenThen, OptionValues}
import play.api.data.Form
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.inland_transport_details

class InlandTransportDetailsControllerSpec extends ControllerSpec with GivenWhenThen with OptionValues {

  private val inlandTransportDetails = mock[inland_transport_details]

  private val controller = new InlandTransportDetailsController(
    authenticate = mockAuthAction,
    journeyType = mockJourneyAction,
    navigator = navigator,
    exportsCacheService = mockExportsCacheService,
    mcc = stubMessagesControllerComponents(),
    inlandTransportDetailsPage = inlandTransportDetails
  )

  private val exampleTransportMode = Maritime

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(inlandTransportDetails.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(inlandTransportDetails)
    super.afterEach()
  }

  private def theResponseForm: Form[InlandModeOfTransportCode] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[InlandModeOfTransportCode]])
    verify(inlandTransportDetails).apply(any(), formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Normal)(request))
    theResponseForm
  }

  "Inland Transport Details Controller on GET request" should {

    onJourney(STANDARD, SUPPLEMENTARY) { request =>
      "return 200 OK" in {
        withNewCaching(request.cacheModel)

        val response = controller.displayPage(Normal).apply(getRequest())

        status(response) must be(OK)
      }

      "read item from cache and display it" in {
        withNewCaching(request.cacheModel)

        await(controller.displayPage(Normal)(getRequest()))

        verify(mockExportsCacheService).get(any())(any())
        verify(inlandTransportDetails).apply(any(), any())(any(), any())
      }
    }

    onJourney(SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
      "redirect to start" in {
        withNewCaching(request.cacheModel)

        val response = controller.displayPage(Normal).apply(getRequest())

        status(response) must be(SEE_OTHER)
        redirectLocation(response) mustBe Some(RootController.displayPage().url)
      }
    }
  }

  private val validOtherTransportPagesValues =
    meaningfulModeOfTransportCodes.filterNot(code => postalOrFTIModeOfTransportCodes.contains(Some(code)))

  private val body = Json.obj("inlandModeOfTransportCode" -> JsString(exampleTransportMode.value))

  onJourney(STANDARD, SUPPLEMENTARY) { request =>
    "Inland Transport Details Controller on POST" should {

      "update cache after successful bind" in {
        withNewCaching(request.cacheModel)

        await(controller.submit(Normal).apply(postRequest(body)))

        theCacheModelUpdated.locations.inlandModeOfTransportCode.value.inlandModeOfTransportCode.value mustBe exampleTransportMode
      }

      "return Bad Request if payload is not compatible with model" in {
        withNewCaching(request.cacheModel)

        val body = Json.obj("inlandModeOfTransportCode" -> "A")
        val result = controller.submit(Normal)(postRequest(body))

        status(result) mustBe BAD_REQUEST
      }

      validOtherTransportPagesValues.foreach { transportMode =>
        val expectedRedirect = DepartureTransportController.displayPage()

        s"redirect to ${expectedRedirect.url}" when {
          s"transportMode '$transportMode' is selected" in {
            withNewCaching(request.cacheModel)

            val body = Json.obj("inlandModeOfTransportCode" -> JsString(transportMode.value))
            val result = await(controller.submit(Normal)(postRequest(body)))

            result mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe expectedRedirect
          }
        }
      }

      postalOrFTIModeOfTransportCodes.foreach { transportMode =>
        val expectedRedirect =
          if (request.declarationType == SUPPLEMENTARY) TransportContainerController.displayContainerSummary()
          else ExpressConsignmentController.displayPage()

        s"redirect to ${expectedRedirect.url}" when {
          s"transportMode '$transportMode' is selected" in {
            withNewCaching(request.cacheModel)

            val body = Json.obj("inlandModeOfTransportCode" -> JsString(transportMode.value.value))
            val result = await(controller.submit(Normal)(postRequest(body)))

            result mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe expectedRedirect
          }
        }
      }

      "return an error" when {

        postalOrFTIModeOfTransportCodes.foreach { modeOfTransportCode =>
          s"transportMode '$modeOfTransportCode' is selected on the page at /transport-leaving-the-border" in {

            And("the same option has not been selected on the page at /inland-transport-details page")
            withNewCaching(aDeclaration(withType(request.declarationType), withBorderModeOfTransportCode(modeOfTransportCode)))

            val result = controller.submit(Normal)(postRequest(body))
            status(result) mustBe BAD_REQUEST
          }
        }
      }
    }
  }

  onJourney(SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
    "Inland Transport Details Controller on POST" should {
      "redirect to start" in {
        withNewCaching(request.cacheModel)

        val result = controller.submit(Normal)(postRequest(body))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) mustBe Some(RootController.displayPage().url)
      }
    }
  }
}
