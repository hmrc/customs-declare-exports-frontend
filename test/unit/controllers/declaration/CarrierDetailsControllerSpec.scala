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

import controllers.declaration.CarrierDetailsController
import forms.declaration.{CarrierDetails, EntityDetails}
import models.DeclarationType._
import models.Mode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.carrier_details

class CarrierDetailsControllerSpec extends ControllerSpec {

  val mockCarrierDetailsPage = mock[carrier_details]

  val controller = new CarrierDetailsController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockCarrierDetailsPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(mockCarrierDetailsPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    reset(mockCarrierDetailsPage)
  }

  def theResponseForm: Form[CarrierDetails] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[CarrierDetails]])
    verify(mockCarrierDetailsPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  "Carrier Details Controller display page" should {

    "return OK (200)" when {

      onJourney(STANDARD, SIMPLIFIED, OCCASIONAL)() { declaration =>
        "with valid journey type" in {

          val eori = Some("1234")
          withNewCaching(aDeclarationAfter(declaration, withCarrierDetails(eori)))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          verify(mockCarrierDetailsPage, times(1)).apply(any(), any())(any(), any())

          theResponseForm.value mustBe Some(CarrierDetails(EntityDetails(eori, None)))
        }
      }
    }

    "return 303 (SEE_OTHER)" when {

      "method is invoked and cache is empty" in {

        withNoDeclaration()
        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.StartController.displayStartPage.url)

        verify(mockCarrierDetailsPage, times(0)).apply(any(), any())(any(), any())
      }

      onJourney(SUPPLEMENTARY, CLEARANCE)() { declaration =>
        "with invalid journey type" in {

          withNewCaching(declaration)

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.StartController.displayStartPage.url)

          verify(mockCarrierDetailsPage, times(0)).apply(any(), any())(any(), any())
        }
      }
    }
  }

  "Carrier Details Controller submit page" should {

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {

        withNewCaching(aDeclaration())

        val incorrectForm = Json.toJson(CarrierDetails(EntityDetails(None, None)))

        val result = controller.saveAddress(Mode.Normal)(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      onJourney(STANDARD, SIMPLIFIED, OCCASIONAL)() { declaration =>
        "with valid journey type" in {

          withNewCaching(declaration)
          val correctForm = Json.toJson(CarrierDetails(EntityDetails(Some("12345678"), None)))

          val result = controller.saveAddress(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.DeclarationAdditionalActorsController.displayPage()
        }
      }

      onJourney(SUPPLEMENTARY, CLEARANCE)() { declaration =>
        "with invalid journey type" in {

          withNewCaching(declaration)
          val correctForm = Json.toJson(CarrierDetails(EntityDetails(Some("12345678"), None)))

          val result = controller.saveAddress(Mode.Normal)(postRequest(correctForm))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.StartController.displayStartPage.url)
        }
      }

    }
  }
}
