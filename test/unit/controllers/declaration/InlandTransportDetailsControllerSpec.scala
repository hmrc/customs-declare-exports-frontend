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

import controllers.declaration.InlandTransportDetailsController
import forms.declaration.ModeOfTransportCodes.Maritime
import models.{DeclarationType, Mode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{verify, when}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.inland_transport_details

class InlandTransportDetailsControllerSpec extends ControllerSpec with BeforeAndAfterEach with OptionValues {

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
    Mockito.reset(inlandTransportDetails)
    super.afterEach()
  }

  "Inland Transport Details Controller on GET request" should {
    onJourney(DeclarationType.STANDARD, DeclarationType.SIMPLIFIED, DeclarationType.SUPPLEMENTARY, DeclarationType.OCCASIONAL)() { declaration =>
      "return 200 OK" in {
        withNewCaching(declaration)

        val response = controller.displayPage(Mode.Normal).apply(getRequest())

        status(response) must be(OK)
      }

      "read item from cache and display it" in {
        withNewCaching(declaration)

        await(controller.displayPage(Mode.Normal)(getRequest()))

        verify(mockExportsCacheService).get(any())(any())
        verify(inlandTransportDetails).apply(any(), any())(any(), any())
      }
    }
    onClearance { declaration =>
      "redirect to start" in {
        withNewCaching(declaration)

        val response = controller.displayPage(Mode.Normal).apply(getRequest())

        status(response) must be(SEE_OTHER)
        redirectLocation(response) mustBe Some(controllers.routes.StartController.displayStartPage.url)
      }
    }

  }
  "Inland Transport Details Controller on POST" when {

    val body = Json.obj("inlandModeOfTransportCode" -> exampleTransportMode)

    onJourney(DeclarationType.STANDARD, DeclarationType.SIMPLIFIED, DeclarationType.SUPPLEMENTARY, DeclarationType.OCCASIONAL)() { declaration =>
      "update cache after successful bind" in {
        withNewCaching(declaration)

        await(controller.submit(Mode.Normal).apply(postRequest(body)))

        theCacheModelUpdated.locations.inlandModeOfTransportCode.value.inlandModeOfTransportCode.value mustBe exampleTransportMode
      }

      "return Bad Request if payload is not compatible with model" in {
        withNewCaching(declaration)

        val body = Json.obj("inlandModeOfTransportCode" -> "A")
        val result = controller.submit(Mode.Normal)(postRequest(body))

        status(result) mustBe BAD_REQUEST
      }
    }

    onStandard { declaration =>
      "redirect to Departure Transport" in {
        withNewCaching(declaration)

        val result = await(controller.submit(Mode.Normal)(postRequest(body)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.TransportLeavingTheBorderController.displayPage()
      }
    }

    onSupplementary { declaration =>
      "redirect to Departure Transport" in {
        withNewCaching(declaration)

        val result = await(controller.submit(Mode.Normal)(postRequest(body)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.TransportLeavingTheBorderController.displayPage()
      }
    }

    onSimplified { declaration =>
      "redirect to Border Transport" in {
        withNewCaching(declaration)

        val result = await(controller.submit(Mode.Normal)(postRequest(body)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.BorderTransportController.displayPage()
      }
    }

    onOccasional { declaration =>
      "redirect to Border Transport" in {
        withNewCaching(declaration)

        val result = await(controller.submit(Mode.Normal)(postRequest(body)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.BorderTransportController.displayPage()
      }
    }

  }
}
