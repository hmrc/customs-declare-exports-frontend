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
import controllers.declaration.routes.{ConsignorDetailsController, RepresentativeAgentController}
import controllers.routes.RootController
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.{Address, Eori}
import forms.declaration.EntityDetails
import forms.declaration.consignor.{ConsignorDetails, ConsignorEoriNumber}
import models.DeclarationType.{OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.DeclarationType
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.consignor_eori_number

class ConsignorEoriNumberControllerSpec extends ControllerSpec with OptionValues {

  val mockConsignorEoriNumberPage = mock[consignor_eori_number]

  val controller = new ConsignorEoriNumberController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    stubMessagesControllerComponents(),
    mockConsignorEoriNumberPage,
    mockExportsCacheService
  )(ec)

  def checkViewInteractions(noOfInvocations: Int = 1): Unit =
    verify(mockConsignorEoriNumberPage, times(noOfInvocations)).apply(any())(any(), any())

  def theResponseForm: Form[ConsignorEoriNumber] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ConsignorEoriNumber]])
    verify(mockConsignorEoriNumberPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withType(DeclarationType.CLEARANCE)))
    await(controller.displayPage()(request))
    theResponseForm
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockConsignorEoriNumberPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockConsignorEoriNumberPage)
  }

  "should return a 200 (OK)" when {
    onJourney(DeclarationType.CLEARANCE) { request =>
      "display page method is invoked and cache is empty" in {
        withNewCaching(request.cacheModel)

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains Consignor Address details" in {
        withNewCaching(
          aDeclarationAfter(
            request.cacheModel,
            withConsignorDetails(None, Some(Address("John Smith", "1 Export Street", "Leeds", "LS1 2PW", "United Kingdom")))
          )
        )

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value.value.eori mustBe None
        theResponseForm.value.value.hasEori mustBe YesNoAnswers.no
      }

      "display page method is invoked and cache contains Consignor Eori details" in {
        val eori = "GB123456789000"
        val hasEori = YesNoAnswers.yes
        withNewCaching(aDeclarationAfter(request.cacheModel, withConsignorDetails(Some(Eori(eori)), None)))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value.value.eori mustBe Some(Eori(eori))
        theResponseForm.value.value.hasEori mustBe hasEori
      }

      "display page method is invoked and cache contains no Consignor data" in {
        withNewCaching(aDeclarationAfter(request.cacheModel))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value mustBe None
      }

    }

    onJourney(STANDARD, SUPPLEMENTARY, OCCASIONAL, SIMPLIFIED) { request =>
      "redirect to start" in {
        withNewCaching(request.cacheModel)

        val result = controller.displayPage()(getRequest())

        status(result) must be(SEE_OTHER)
        redirectLocation(result) mustBe Some(RootController.displayPage().url)
      }
    }
  }

  "should return a 400 (BAD_REQUEST)" when {
    onJourney(DeclarationType.CLEARANCE) { request =>
      "EORI is incorrect" in {
        withNewCaching(request.cacheModel)

        val incorrectForm = Json.toJson(ConsignorEoriNumber(eori = Some(Eori("!@#$")), hasEori = YesNoAnswers.yes))

        val result = controller.submit()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
      }

      "EORI is not provided but trader selected that it has an EORI" in {
        withNewCaching(request.cacheModel)

        val incorrectForm = Json.toJson(ConsignorEoriNumber(eori = None, hasEori = YesNoAnswers.yes))

        val result = controller.submit()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
      }

      "no choice is selected and no cached ConsignorDetails exist" in {
        withNewCaching(request.cacheModel)

        val correctForm = Json.toJson(ConsignorEoriNumber(eori = None, hasEori = ""))

        val result = controller.submit()(postRequest(correctForm))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
      }
    }
  }

  "should return a 303 (SEE_OTHER)" when {
    onJourney(DeclarationType.CLEARANCE) { request =>
      "'No' is selected" in {
        withNewCaching(request.cacheModel)

        val correctForm = Json.toJson(ConsignorEoriNumber(eori = None, YesNoAnswers.no))

        val result = controller.submit()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe ConsignorDetailsController.displayPage()
        checkViewInteractions(0)
        theCacheModelUpdated.parties.consignorDetails must be(Some(ConsignorDetails(EntityDetails(None, None))))
      }

      "'Yes' is selected" in {
        withNewCaching(request.cacheModel)

        val eoriInput = Some(Eori("GB123456789000"))
        val correctForm = Json.toJson(ConsignorEoriNumber(eori = eoriInput, YesNoAnswers.yes))

        val result = controller.submit()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe RepresentativeAgentController.displayPage()
        checkViewInteractions(0)
        theCacheModelUpdated.parties.consignorDetails must be(Some(ConsignorDetails(EntityDetails(eoriInput, None))))
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, OCCASIONAL, SIMPLIFIED) { request =>
      "redirect to start" in {
        val eoriCached = Some(Eori("GB123456789000"))
        val eoriInput = Some(Eori("GB123456789000"))

        withNewCaching(aDeclarationAfter(request.cacheModel, withConsignorDetails(eoriCached, None)))

        val correctForm = Json.toJson(ConsignorEoriNumber(eori = eoriInput, hasEori = YesNoAnswers.yes))

        val result = controller.submit()(postRequest(correctForm))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) mustBe Some(RootController.displayPage().url)
      }
    }
  }
}
