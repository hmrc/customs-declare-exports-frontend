/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.declaration.InlandTransportDetailsController
import forms.declaration.InlandModeOfTransportCode
import forms.declaration.ModeOfTransportCode.Maritime
import models.DeclarationType._
import models.Mode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.data.Form
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.inland_transport_details

class InlandTransportDetailsControllerSpec extends ControllerSpec with BeforeAndAfterEach with OptionValues {

  private val inlandTransportDetails = mock[inland_transport_details]

  private val controller = new InlandTransportDetailsController(
    authenticate = mockAuthAction,
    verifyEmail = mockVerifiedEmailAction,
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
    Mockito.reset(inlandTransportDetails)
    super.afterEach()
  }

  private def theResponseForm: Form[InlandModeOfTransportCode] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[InlandModeOfTransportCode]])
    verify(inlandTransportDetails).apply(any(), formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
  }

  "Inland Transport Details Controller on GET request" should {
    onJourney(STANDARD, SIMPLIFIED, SUPPLEMENTARY, OCCASIONAL) { request =>
      "return 200 OK" in {
        withNewCaching(request.cacheModel)

        val response = controller.displayPage(Mode.Normal).apply(getRequest())

        status(response) must be(OK)
      }

      "read item from cache and display it" in {
        withNewCaching(request.cacheModel)

        await(controller.displayPage(Mode.Normal)(getRequest()))

        verify(mockExportsCacheService).get(any())(any())
        verify(inlandTransportDetails).apply(any(), any())(any(), any())
      }
    }
    onClearance { request =>
      "redirect to start" in {
        withNewCaching(request.cacheModel)

        val response = controller.displayPage(Mode.Normal).apply(getRequest())

        status(response) must be(SEE_OTHER)
        redirectLocation(response) mustBe Some(controllers.routes.RootController.displayPage().url)
      }
    }

  }
  "Inland Transport Details Controller on POST" when {

    val body = Json.obj("inlandModeOfTransportCode" -> JsString(exampleTransportMode.value))

    onJourney(STANDARD, SIMPLIFIED, SUPPLEMENTARY, OCCASIONAL) { request =>
      "update cache after successful bind" in {
        withNewCaching(request.cacheModel)

        await(controller.submit(Mode.Normal).apply(postRequest(body)))

        theCacheModelUpdated.locations.inlandModeOfTransportCode.value.inlandModeOfTransportCode.value mustBe exampleTransportMode
      }

      "return Bad Request if payload is not compatible with model" in {
        withNewCaching(request.cacheModel)

        val body = Json.obj("inlandModeOfTransportCode" -> "A")
        val result = controller.submit(Mode.Normal)(postRequest(body))

        status(result) mustBe BAD_REQUEST
      }
    }

    onStandard { request =>
      "redirect to Departure Transport" in {
        withNewCaching(request.cacheModel)

        val result = await(controller.submit(Mode.Normal)(postRequest(body)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DepartureTransportController.displayPage()
      }
    }

    onSupplementary { request =>
      "redirect to Departure Transport" in {
        withNewCaching(request.cacheModel)

        val result = await(controller.submit(Mode.Normal)(postRequest(body)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DepartureTransportController.displayPage()
      }
    }

    onSimplified { request =>
      "redirect to Border Transport" in {
        withNewCaching(request.cacheModel)

        val result = await(controller.submit(Mode.Normal)(postRequest(body)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.BorderTransportController.displayPage()
      }
    }

    onOccasional { request =>
      "redirect to Border Transport" in {
        withNewCaching(request.cacheModel)

        val result = await(controller.submit(Mode.Normal)(postRequest(body)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.BorderTransportController.displayPage()
      }
    }

  }
}
