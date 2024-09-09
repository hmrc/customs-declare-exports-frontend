/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.section2

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.general.routes.RootController
import controllers.section2.routes.{CarrierDetailsController, ConsigneeDetailsController}
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.{Address, Eori}
import forms.section2.EntityDetails
import forms.section2.carrier.{CarrierDetails, CarrierEoriNumber}
import models.DeclarationType
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section2.carrier_eori_number

class CarrierEoriNumberControllerSpec extends ControllerSpec with AuditedControllerSpec with OptionValues {

  val mockCarrierEoriNumberPage = mock[carrier_eori_number]

  val controller =
    new CarrierEoriNumberController(mockAuthAction, mockJourneyAction, navigator, mcc, mockCarrierEoriNumberPage, mockExportsCacheService)(
      ec,
      auditService
    )

  def checkViewInteractions(noOfInvocations: Int = 1): Unit =
    verify(mockCarrierEoriNumberPage, times(noOfInvocations)).apply(any())(any(), any())

  def theResponseForm: Form[CarrierEoriNumber] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[CarrierEoriNumber]])
    verify(mockCarrierEoriNumberPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withType(DeclarationType.CLEARANCE)))
    await(controller.displayPage(request))
    theResponseForm
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockCarrierEoriNumberPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockCarrierEoriNumberPage)
  }

  "should return a 200 (OK)" when {
    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
      "display page method is invoked and cache is empty" in {

        withNewCaching(request.cacheModel)

        val result = controller.displayPage(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains Carrier Address details" in {

        withNewCaching(
          aDeclarationAfter(
            request.cacheModel,
            withCarrierDetails(None, Some(Address("John Smith", "1 Export Street", "Leeds", "LS1 2PW", "United Kingdom")))
          )
        )

        val result = controller.displayPage(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value.value.eori mustBe None
        theResponseForm.value.value.hasEori mustBe YesNoAnswers.no
      }

      "display page method is invoked and cache contains Carrier Eori details" in {

        val eori = "GB123456789000"
        val hasEori = YesNoAnswers.yes
        withNewCaching(aDeclarationAfter(request.cacheModel, withCarrierDetails(Some(Eori(eori)), None)))

        val result = controller.displayPage(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value.value.eori mustBe Some(Eori(eori))
        theResponseForm.value.value.hasEori mustBe hasEori
      }

      "display page method is invoked and cache contains no Carrier data" in {

        withNewCaching(aDeclarationAfter(request.cacheModel))

        val result = controller.displayPage(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value mustBe None
      }

    }

    onJourney(SUPPLEMENTARY) { request =>
      "redirect to start" in {
        withNewCaching(request.cacheModel)

        val result = controller.displayPage(getRequest())

        status(result) must be(SEE_OTHER)
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
      }
    }
  }

  "should return a 400 (BAD_REQUEST)" when {
    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
      "EORI is incorrect" in {

        withNewCaching(request.cacheModel)

        val incorrectForm = Json.toJson(CarrierEoriNumber(eori = Some(Eori("!@#$")), hasEori = YesNoAnswers.yes))

        val result = controller.submit()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
        verifyNoAudit()
      }

      "EORI is not provided but trader selected that it has an EORI" in {

        withNewCaching(request.cacheModel)

        val incorrectForm = Json.toJson(CarrierEoriNumber(eori = None, hasEori = YesNoAnswers.yes))

        val result = controller.submit()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
        verifyNoAudit()
      }

      "no choice is selected and no cached CarrierDetails exist" in {

        withNewCaching(request.cacheModel)

        val correctForm = Json.toJson(CarrierEoriNumber(eori = None, hasEori = ""))

        val result = controller.submit()(postRequest(correctForm))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
        verifyNoAudit()
      }
    }
  }

  "should return a 303 (SEE_OTHER)" when {
    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
      "'No' is selected" in {

        withNewCaching(request.cacheModel)

        val correctForm = Json.toJson(CarrierEoriNumber(eori = None, YesNoAnswers.no))

        val result = controller.submit()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe CarrierDetailsController.displayPage
        checkViewInteractions(0)
        theCacheModelUpdated.parties.carrierDetails must be(Some(CarrierDetails(EntityDetails(None, None))))
        verifyAudit()
      }

      "'Yes' is selected" in {

        withNewCaching(request.cacheModel)

        val eoriInput = Some(Eori("GB123456789000"))
        val correctForm = Json.toJson(CarrierEoriNumber(eori = eoriInput, YesNoAnswers.yes))

        val result = controller.submit()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe ConsigneeDetailsController.displayPage
        checkViewInteractions(0)
        theCacheModelUpdated.parties.carrierDetails must be(Some(CarrierDetails(EntityDetails(eoriInput, None))))
        verifyAudit()
      }
    }

    onJourney(SUPPLEMENTARY) { request =>
      "redirect to start" in {
        val eoriCached = Some(Eori("GB123456789000"))
        val eoriInput = Some(Eori("GB123456789000"))

        withNewCaching(aDeclarationAfter(request.cacheModel, withCarrierDetails(eoriCached, None)))

        val correctForm = Json.toJson(CarrierEoriNumber(eori = eoriInput, hasEori = YesNoAnswers.yes))

        val result = controller.submit()(postRequest(correctForm))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
        verifyNoAudit()
      }
    }
  }
}
