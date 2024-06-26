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

package controllers.section2

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.section2.routes.{ExporterDetailsController, IsExsController, RepresentativeAgentController}
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.{Address, Eori}
import forms.declaration.EntityDetails
import forms.section2.exporter.{ExporterDetails, ExporterEoriNumber}
import models.DeclarationType._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section2.exporter_eori_number

class ExporterEoriNumberControllerSpec extends ControllerSpec with AuditedControllerSpec with OptionValues {

  val mockExporterEoriNumberPage = mock[exporter_eori_number]

  val controller =
    new ExporterEoriNumberController(mockAuthAction, mockJourneyAction, navigator, mcc, mockExporterEoriNumberPage, mockExportsCacheService)(
      ec,
      auditService
    )

  def checkViewInteractions(noOfInvocations: Int = 1): Unit =
    verify(mockExporterEoriNumberPage, times(noOfInvocations)).apply(any())(any(), any())

  def theResponseForm: Form[ExporterEoriNumber] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ExporterEoriNumber]])
    verify(mockExporterEoriNumberPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withType(CLEARANCE)))
    await(controller.displayPage(request))
    theResponseForm
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockExporterEoriNumberPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockExporterEoriNumberPage)
  }

  onEveryDeclarationJourney() { request =>
    "should return a 200 (OK)" when {

      "display page method is invoked and cache is empty" in {
        withNewCaching(request.cacheModel)

        val result = controller.displayPage(getRequest())

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

        val result = controller.displayPage(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value.value.eori mustBe None
        theResponseForm.value.value.hasEori mustBe YesNoAnswers.no
      }

      "display page method is invoked and cache contains Exporter Eori details" in {
        val eori = "GB123456789000"
        val hasEori = YesNoAnswers.yes
        withNewCaching(aDeclarationAfter(request.cacheModel, withExporterDetails(Some(Eori(eori)), None)))

        val result = controller.displayPage(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value.value.eori mustBe Some(Eori(eori))
        theResponseForm.value.value.hasEori mustBe hasEori
      }

      "display page method is invoked and cache contains no Exporter data" in {
        withNewCaching(aDeclarationAfter(request.cacheModel))

        val result = controller.displayPage(getRequest())

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
        verifyNoAudit()
      }

      "EORI is not provided but trader selected that it has an EORI" in {
        withNewCaching(request.cacheModel)

        val incorrectForm = Json.toJson(ExporterEoriNumber(eori = None, hasEori = YesNoAnswers.yes))

        val result = controller.submit()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
        verifyNoAudit()
      }

      "no choice is selected and no cached ExporterDetails exist" in {
        withNewCaching(request.cacheModel)

        val correctForm = Json.toJson(ExporterEoriNumber(eori = None, hasEori = ""))

        val result = controller.submit()(postRequest(correctForm))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
        verifyNoAudit()
      }
    }

    "should return a 303 (SEE_OTHER)" when {
      "'No' is selected" in {
        withNewCaching(request.cacheModel)

        val correctForm = Json.toJson(ExporterEoriNumber(eori = None, YesNoAnswers.no))

        val result = controller.submit()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe ExporterDetailsController.displayPage
        checkViewInteractions(0)
        theCacheModelUpdated.parties.exporterDetails must be(Some(ExporterDetails(EntityDetails(None, None))))
        verifyAudit()
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
        thePageNavigatedTo mustBe RepresentativeAgentController.displayPage
        checkViewInteractions(0)
        theCacheModelUpdated.parties.exporterDetails must be(Some(ExporterDetails(EntityDetails(eoriInput, None))))
        verifyAudit()
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
        thePageNavigatedTo mustBe IsExsController.displayPage
        checkViewInteractions(0)
        theCacheModelUpdated.parties.exporterDetails must be(Some(ExporterDetails(EntityDetails(eoriInput, None))))
        verifyAudit()
      }
    }
  }
}
