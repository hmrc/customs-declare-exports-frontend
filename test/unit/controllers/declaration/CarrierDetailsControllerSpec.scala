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
import forms.common.Eori
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

  def verifyPageInvocations(numberOfInvocations: Int) =
    verify(mockCarrierDetailsPage, times(numberOfInvocations)).apply(any(), any())(any(), any())

  "Carrier Details Controller display page" should {

    "return OK (200)" when {

      onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
        "with valid journey type" in {

          val eori = Some(Eori("1234"))
          withNewCaching(aDeclarationAfter(request.cacheModel, withCarrierDetails(eori)))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          verifyPageInvocations(1)

          theResponseForm.value mustBe Some(CarrierDetails(EntityDetails(eori, None)))
          theResponseForm.errors mustBe Seq.empty
        }

        "with submission errors" in {

          withNewCaching(aDeclaration())

          val result = controller.displayPage(Mode.Normal)(getRequestWithSubmissionErrors)
          status(result) mustBe OK

          theResponseForm.errors mustBe Seq(submissionFormError)
        }
      }
    }

    "return 303 (SEE_OTHER)" when {

      "method is invoked and cache is empty" in {

        withNoDeclaration()
        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.StartController.displayStartPage.url)

        verifyPageInvocations(0)
      }

      onJourney(SUPPLEMENTARY) { request =>
        "with invalid journey type" in {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.StartController.displayStartPage.url)

          verifyPageInvocations(0)
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

      onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
        "with valid journey type" in {

          withNewCaching(request.cacheModel)
          val correctForm = Json.toJson(CarrierDetails(EntityDetails(Some(Eori("GB12345678912345")), None)))

          val result = controller.saveAddress(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.ConsigneeDetailsController.displayPage()
        }
      }

      onJourney(SUPPLEMENTARY) { request =>
        "with invalid journey type" in {

          withNewCaching(request.cacheModel)
          val correctForm = Json.toJson(CarrierDetails(EntityDetails(Some(Eori("12345678")), None)))

          val result = controller.saveAddress(Mode.Normal)(postRequest(correctForm))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.StartController.displayStartPage.url)
        }
      }

    }
  }
}
