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

import controllers.declaration.ExporterDetailsController
import forms.common.{Address, Eori}
import forms.declaration.ExporterDetails
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.exporter_details

import scala.concurrent.ExecutionContext

class ExporterDetailsControllerSpec extends ControllerSpec with OptionValues {

  val exporter_details = mock[exporter_details]

  val controller = new ExporterDetailsController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    exporter_details
  )(ExecutionContext.global)

  def theResponseForm: Form[ExporterDetails] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ExporterDetails]])
    verify(exporter_details).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(exporter_details.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(exporter_details)
    super.afterEach()
  }

  "Exporter Details Controller" should {
    onJourney(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY, DeclarationType.OCCASIONAL, DeclarationType.SIMPLIFIED) { request =>
      "return 200 OK" when {
        "details are empty" in {
          withNewCaching(request.cacheModel)
          val response = controller.displayPage(Mode.Normal)(getRequest())
          status(response) mustBe OK
        }
        "details are filled" in {
          withNewCaching(
            aDeclarationAfter(
              request.cacheModel,
              withExporterDetails(eori = Some(Eori("99980")), address = Some(Address("CaptainAmerica", "Test Street", "Leeds", "LS18BN", "Portugal")))
            )
          )
          val response = controller.displayPage(Mode.Normal)(getRequest())
          status(response) mustBe OK
          val details = theResponseForm.value.value.details
          details.eori mustBe defined
          details.address mustBe defined
        }

      }
      "return 400 bad request" when {
        "form contains errors" in {
          withNewCaching(request.cacheModel)
          val body = Json.obj("details" -> Json.obj("eori" -> "!nva!id"))
          val response = controller.saveAddress(Mode.Normal)(postRequest(body))
          status(response) mustBe BAD_REQUEST
        }
      }
      "return 303 (SEE_OTHER) and redirect to representative details page" when {
        "correct form is submitted" in {
          withNewCaching(request.cacheModel)
          val body = Json.obj("details" -> Json.obj("eori" -> "GB213472539481923"))
          val response = controller.saveAddress(Mode.Normal)(postRequest(body))

          await(response) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.RepresentativeAgentController.displayPage()
        }
      }
    }

    onJourney(DeclarationType.CLEARANCE) { request =>
      "return 303 (SEE_OTHER) and redirect to Is Exs page" when {
        "correct form is submitted" in {
          withNewCaching(request.cacheModel)
          val body = Json.obj("details" -> Json.obj("eori" -> "GB213472539481923"))
          val response = controller.saveAddress(Mode.Normal)(postRequest(body))

          await(response) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.IsExsController.displayPage()
        }
      }
    }
  }
}
