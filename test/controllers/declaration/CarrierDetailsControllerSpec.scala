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

import base.{AuditedControllerSpec, ControllerSpec}
import connectors.CodeListConnector
import forms.common.Address
import forms.declaration.EntityDetails
import forms.declaration.carrier.CarrierDetails
import models.DeclarationType._
import models.codes.Country
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import play.twirl.api.HtmlFormat.Appendable
import views.html.declaration.carrier_details

import scala.collection.immutable.ListMap

class CarrierDetailsControllerSpec extends ControllerSpec with AuditedControllerSpec {

  val mockCarrierDetailsPage = mock[carrier_details]
  val mockCodeListConnector = mock[CodeListConnector]

  val controller = new CarrierDetailsController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    mcc,
    mockCarrierDetailsPage
  )(ec, mockCodeListConnector, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(mockCarrierDetailsPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> Country("United Kingdom, Great Britain, Northern Ireland", "GB")))
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    reset(mockCarrierDetailsPage, mockCodeListConnector)
  }

  def theResponseForm: Form[CarrierDetails] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[CarrierDetails]])
    verify(mockCarrierDetailsPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  def verifyPageInvocations(numberOfInvocations: Int): Appendable =
    verify(mockCarrierDetailsPage, times(numberOfInvocations)).apply(any())(any(), any())

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  "Carrier Details Controller display page" should {

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(getRequest())

          status(result) must be(OK)
        }

        "display page method is invoked and cache contains data" in {
          val address = Address("John Smith", "1 Export Street", "Leeds", "LS1 2PW", "United Kingdom")
          withNewCaching(aDeclarationAfter(request.cacheModel, withCarrierDetails(None, Some(address))))

          val result = controller.displayPage(getRequest())

          status(result) must be(OK)
        }
      }
    }

    "return 303 (SEE_OTHER)" when {

      onJourney(CLEARANCE) { request =>
        "form is empty" in {
          withNewCaching(request.cacheModel)

          val incorrectForm = Json.toJson(CarrierDetails(EntityDetails(None, None)))

          val result = controller.saveAddress(postRequest(incorrectForm))

          status(result) must be(SEE_OTHER)
          verifyAudit()
        }
      }

      "method is invoked and cache is empty" in {
        withNoDeclaration()
        val result = controller.displayPage(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.RootController.displayPage.url)

        verifyPageInvocations(0)
      }

      onJourney(SUPPLEMENTARY) { request =>
        "with invalid journey type" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.RootController.displayPage.url)

          verifyPageInvocations(0)
        }
      }
    }
  }

  "Carrier Details Controller submit page" should {

    "return 303 (SEE_OTHER)" when {

      onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
        "with valid journey type" in {
          withNewCaching(request.cacheModel)

          val address = Address("John Smith", "1 Export Street", "Leeds", "LS1 2PW", "GB")
          val correctForm = Json.toJson(CarrierDetails(EntityDetails(None, Some(address))))

          val result = controller.saveAddress(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.ConsigneeDetailsController.displayPage
          verifyAudit()
        }
      }

      onJourney(SUPPLEMENTARY) { request =>
        "with invalid journey type" in {
          withNewCaching(request.cacheModel)

          val address = Address("John Smith", "1 Export Street", "Leeds", "LS1 2PW", "United Kingdom, Great Britain, Northern Ireland")
          val correctForm = Json.toJson(CarrierDetails(EntityDetails(None, Some(address))))

          val result = controller.saveAddress(postRequest(correctForm))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.RootController.displayPage.url)
          verifyNoAudit()
        }
      }
    }

    "return 400 (BAD_REQUEST)" when {
      val prefix = "details.address"

      "no value is entered" in {
        withNewCaching(aStandardDeclaration)

        val incorrectForm = Json.obj(s"$prefix.fullName" -> "", s"$prefix.addressLine" -> "", s"$prefix.townOrCity" -> "", s"$prefix.postCode" -> "")
        val result = controller.saveAddress(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()

        val errors = theResponseForm.errors
        errors(0).messages.head mustBe "declaration.address.fullName.empty"
        errors(1).messages.head mustBe "declaration.address.addressLine.empty"
        errors(2).messages.head mustBe "declaration.address.townOrCity.empty"
        errors(3).messages.head mustBe "declaration.address.postCode.empty"
        errors(4).messages.head mustBe "declaration.address.country.empty"
      }

      "the entered values are incorrect" in {
        withNewCaching(aStandardDeclaration)

        val incorrectForm = Json.obj(
          s"$prefix.fullName" -> "$".repeat(36),
          s"$prefix.addressLine" -> "$".repeat(71),
          s"$prefix.townOrCity" -> "$".repeat(36),
          s"$prefix.postCode" -> "$".repeat(10),
          s"$prefix.country" -> "TTTT"
        )
        val result = controller.saveAddress(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()

        val errors = theResponseForm.errors
        errors(0).messages.head mustBe "declaration.address.fullName.error"
        errors(1).messages.head mustBe "declaration.address.fullName.length"
        errors(2).messages.head mustBe "declaration.address.addressLine.error"
        errors(3).messages.head mustBe "declaration.address.addressLine.length"
        errors(4).messages.head mustBe "declaration.address.townOrCity.error"
        errors(5).messages.head mustBe "declaration.address.townOrCity.length"
        errors(6).messages.head mustBe "declaration.address.postCode.error"
        errors(7).messages.head mustBe "declaration.address.postCode.length"
        errors(8).messages.head mustBe "declaration.address.country.error"
      }
    }
  }
}
