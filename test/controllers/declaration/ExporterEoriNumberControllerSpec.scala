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
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.{Address, Eori}
import forms.declaration.EntityDetails
import forms.declaration.exporter.{ExporterDetails, ExporterEoriNumber}
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.{DeclarationType}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.exporter_eori_number

class ExporterEoriNumberControllerSpec extends ControllerSpec with OptionValues {

  val mockExporterEoriNumberPage = mock[exporter_eori_number]

  val controller = new ExporterEoriNumberController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    stubMessagesControllerComponents(),
    mockExporterEoriNumberPage,
    mockExportsCacheService
  )(ec)

  def checkViewInteractions(noOfInvocations: Int = 1): Unit =
    verify(mockExporterEoriNumberPage, times(noOfInvocations)).apply(any(), any())(any(), any())

  def theResponseForm: Form[ExporterEoriNumber] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ExporterEoriNumber]])
    verify(mockExporterEoriNumberPage).apply(any(), captor.capture())(any(), any())
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
    when(mockExporterEoriNumberPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockExporterEoriNumberPage)
  }

  onEveryDeclarationJourney() { request =>
    "should return a 200 (OK)" when {
      "display page method is invoked and cache is empty" in {

        withNewCaching(request.cacheModel)

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains Exporter Address details" in {

        withNewCaching(
          aDeclarationAfter(
            request.cacheModel,
            withExporterDetails(None, Some(Address("John Smith", "1 Export Street", "Leeds", "LS1 2PW", "United Kingdom")))
          )
        )

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value.value.eori mustBe None
        theResponseForm.value.value.hasEori mustBe YesNoAnswers.no
      }

      "display page method is invoked and cache contains Exporter Eori details" in {

        val eori = "GB123456789000"
        val hasEori = YesNoAnswers.yes
        withNewCaching(aDeclarationAfter(request.cacheModel, withExporterDetails(Some(Eori(eori)), None)))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value.value.eori mustBe Some(Eori(eori))
        theResponseForm.value.value.hasEori mustBe hasEori
      }

      "display page method is invoked and cache contains no Exporter data" in {

        withNewCaching(aDeclarationAfter(request.cacheModel))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value mustBe None
      }
    }

    "should return a 400 (BAD_REQUEST)" when {
      "EORI is incorrect" in {

        withNewCaching(request.cacheModel)

        val incorrectForm = Json.toJson(ExporterEoriNumber(eori = Some(Eori("!@#$")), hasEori = YesNoAnswers.yes))

        val result = controller.submit()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
      }

      "EORI is not provided but trader selected that it has an EORI" in {

        withNewCaching(request.cacheModel)

        val incorrectForm = Json.toJson(ExporterEoriNumber(eori = None, hasEori = YesNoAnswers.yes))

        val result = controller.submit()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
      }

      "no choice is selected and no cached ExporterDetails exist" in {

        withNewCaching(request.cacheModel)

        val correctForm = Json.toJson(ExporterEoriNumber(eori = None, hasEori = ""))

        val result = controller.submit()(postRequest(correctForm))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
      }
    }
    "should return a 303 (SEE_OTHER)" when {
      "'No' is selected" in {

        withNewCaching(request.cacheModel)

        val correctForm = Json.toJson(ExporterEoriNumber(eori = None, YesNoAnswers.no))

        val result = controller.submit()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.ExporterDetailsController.displayPage()
        checkViewInteractions(0)
        theCacheModelUpdated.parties.exporterDetails must be(Some(ExporterDetails(EntityDetails(None, None))))
      }
    }
  }

  onJourney(STANDARD, SUPPLEMENTARY, OCCASIONAL, SIMPLIFIED) { request =>
    "should return a 303 (SEE_OTHER)" when {
      "'Yes' is selected" in {

        withNewCaching(request.cacheModel)

        val eoriInput = Some(Eori("GB123456789000"))
        val correctForm = Json.toJson(ExporterEoriNumber(eori = eoriInput, YesNoAnswers.yes))

        val result = controller.submit()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.RepresentativeAgentController.displayPage()
        checkViewInteractions(0)
        theCacheModelUpdated.parties.exporterDetails must be(Some(ExporterDetails(EntityDetails(eoriInput, None))))
      }
    }
  }

  onJourney(CLEARANCE) { request =>
    "should return a 303 (SEE_OTHER)" when {
      "'Yes' is selected" in {

        withNewCaching(request.cacheModel)

        val eoriInput = Some(Eori("GB123456789000"))
        val correctForm = Json.toJson(ExporterEoriNumber(eori = eoriInput, YesNoAnswers.yes))

        val result = controller.submit()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.IsExsController.displayPage()
        checkViewInteractions(0)
        theCacheModelUpdated.parties.exporterDetails must be(Some(ExporterDetails(EntityDetails(eoriInput, None))))
      }
    }
  }
}
