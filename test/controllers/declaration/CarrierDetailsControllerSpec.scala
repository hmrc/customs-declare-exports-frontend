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
import connectors.CodeListConnector
import forms.common.Address
import forms.declaration.EntityDetails
import forms.declaration.carrier.CarrierDetails
import models.DeclarationType._
import models.Mode
import models.codes.Country
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.carrier_details

import scala.collection.immutable.ListMap

class CarrierDetailsControllerSpec extends ControllerSpec {

  val mockCarrierDetailsPage = mock[carrier_details]
  val mockCodeListConnector = mock[CodeListConnector]

  val controller = new CarrierDetailsController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockCarrierDetailsPage
  )(ec, mockCodeListConnector)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(mockCarrierDetailsPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> Country("United Kingdom, Great Britain, Northern Ireland", "GB")))
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    reset(mockCarrierDetailsPage, mockCodeListConnector)
  }

  def theResponseForm: Form[CarrierDetails] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[CarrierDetails]])
    verify(mockCarrierDetailsPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  def verifyPageInvocations(numberOfInvocations: Int) =
    verify(mockCarrierDetailsPage, times(numberOfInvocations)).apply(any(), any())(any(), any())

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
  }

  "Carrier Details Controller display page" should {

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }

        "display page method is invoked and cache contains data" in {

          withNewCaching(
            aDeclarationAfter(
              request.cacheModel,
              withCarrierDetails(None, Some(Address("John Smith", "1 Export Street", "Leeds", "LS1 2PW", "United Kingdom")))
            )
          )

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "form is incorrect" in {

          withNewCaching(request.cacheModel)

          val incorrectForm = Json.toJson(CarrierDetails(EntityDetails(None, Some(Address("", "", "Leeds", "LS1 2PW", "United Kingdom")))))

          val result = controller.saveAddress(Mode.Normal)(postRequest(incorrectForm))

          status(result) must be(BAD_REQUEST)
        }
      }
    }

    "return 303 (SEE_OTHER)" when {

      onJourney(CLEARANCE) { request =>
        "form is empty" in {

          withNewCaching(request.cacheModel)

          val incorrectForm = Json.toJson(CarrierDetails(EntityDetails(None, None)))

          val result = controller.saveAddress(Mode.Normal)(postRequest(incorrectForm))

          status(result) must be(SEE_OTHER)
        }
      }

      onJourney(STANDARD, SIMPLIFIED, OCCASIONAL) { request =>
        "method is invoked and cache is empty" in {
          withNoDeclaration()
          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.RootController.displayPage().url)

          verifyPageInvocations(0)
        }
      }

      onJourney(SUPPLEMENTARY) { request =>
        "with invalid journey type" in {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.RootController.displayPage().url)

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
          val correctForm =
            Json.toJson(
              CarrierDetails(
                EntityDetails(
                  None,
                  Some(Address("John Smith", "1 Export Street", "Leeds", "LS1 2PW", "United Kingdom, Great Britain, Northern Ireland"))
                )
              )
            )

          val result = controller.saveAddress(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.ConsigneeDetailsController.displayPage()
        }
      }

      onJourney(SUPPLEMENTARY) { request =>
        "with invalid journey type" in {

          withNewCaching(request.cacheModel)
          val correctForm =
            Json.toJson(
              CarrierDetails(
                EntityDetails(
                  None,
                  Some(Address("John Smith", "1 Export Street", "Leeds", "LS1 2PW", "United Kingdom, Great Britain, Northern Ireland"))
                )
              )
            )

          val result = controller.saveAddress(Mode.Normal)(postRequest(correctForm))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.RootController.displayPage().url)
        }
      }

    }
  }
}
