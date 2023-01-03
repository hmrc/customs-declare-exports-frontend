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

import base.ControllerSpec
import controllers.declaration.routes.{CarrierEoriNumberController, ConsigneeDetailsController, ExporterEoriNumberController, IsExsController}
import forms.declaration.DeclarantIsExporter
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
import views.html.declaration.declarant_exporter

class DeclarantExporterControllerSpec extends ControllerSpec with OptionValues {

  val mockPage = mock[declarant_exporter]

  val controller = new DeclarantExporterController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockPage
  )(ec)

  def theResponseForm: Form[DeclarantIsExporter] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[DeclarantIsExporter]])
    verify(mockPage).apply(formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockPage)
    super.afterEach()
  }

  def verifyPage(numberOfTimes: Int): HtmlFormat.Appendable =
    verify(mockPage, times(numberOfTimes)).apply(any())(any(), any())

  "Declarant Exporter controller" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" when {

        "display page method is invoked with empty cache" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(getRequest())

          status(result) mustBe OK
          verifyPage(1)

          theResponseForm.value mustBe empty
        }

        "display page method is invoked with data in cache" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarantIsExporter()))

          val result = controller.displayPage(getRequest())

          status(result) mustBe OK
          verifyPage(1)

          theResponseForm.value.map(_.answer) mustBe Some("Yes")
        }

      }

      "return 400 (BAD_REQUEST)" when {
        "form is incorrect" in {
          withNewCaching(request.cacheModel)

          val incorrectForm = Json.toJson(DeclarantIsExporter("invalid"))

          val result = controller.submitForm()(postRequest(incorrectForm))

          status(result) mustBe BAD_REQUEST
          verifyPage(1)
        }
      }
    }

    onEveryDeclarationJourney() { request =>
      "return 303 (SEE_OTHER) and redirect to exporter page when declarant not exporter" in {
        withNewCaching(request.cacheModel)

        val correctForm = Json.toJson(DeclarantIsExporter("No"))

        val result = controller.submitForm()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe ExporterEoriNumberController.displayPage

        verifyPage(0)
      }
    }

    onJourney(DeclarationType.STANDARD, DeclarationType.SIMPLIFIED, DeclarationType.OCCASIONAL) { request =>
      "return 303 (SEE_OTHER) and redirect to carrier page when declarant is exporter" in {
        withNewCaching(request.cacheModel)

        val correctForm = Json.toJson(DeclarantIsExporter("Yes"))

        val result = controller.submitForm()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe CarrierEoriNumberController.displayPage

        verifyPage(0)
      }
    }

    onJourney(DeclarationType.CLEARANCE) { request =>
      "return 303 (SEE_OTHER) and redirect to isExs page when declarant is exporter" in {
        withNewCaching(request.cacheModel)

        val correctForm = Json.toJson(DeclarantIsExporter("Yes"))

        val result = controller.submitForm()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe IsExsController.displayPage

        verifyPage(0)
      }
    }

    onJourney(DeclarationType.SUPPLEMENTARY) { request =>
      "return 303 (SEE_OTHER) and redirect to consignee page when declarant is exporter" in {
        withNewCaching(request.cacheModel)

        val correctForm = Json.toJson(DeclarantIsExporter("Yes"))

        val result = controller.submitForm()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe ConsigneeDetailsController.displayPage

        verifyPage(0)
      }
    }
  }
}
