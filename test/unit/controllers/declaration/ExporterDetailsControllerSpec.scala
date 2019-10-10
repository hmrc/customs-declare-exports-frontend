/*
 * Copyright 2019 HM Revenue & Customs
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
import forms.common.Address
import forms.declaration.ExporterDetails
import models.Mode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
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
    "return 200 OK" when {
      "details are empty" in {
        val declaration = aDeclaration()
        withNewCaching(declaration)
        val response = controller.displayPage(Mode.Normal)(getRequest(declaration))
        status(response) mustBe OK
      }
      "details are filled" in {
        val declaration = aDeclaration(
          withExporterDetails(eori = Some("99980"), address = Some(Address("CaptainAmerica", "Test Street", "Leeds", "LS18BN", "Portugal")))
        )
        withNewCaching(declaration)
        val response = controller.displayPage(Mode.Normal)(getRequest(declaration))
        status(response) mustBe OK
        val details = theResponseForm.value.value.details
        details.eori mustBe defined
        details.address mustBe defined
      }
    }
    "return 400 bad request" when {
      "form contains erros" in {
        val declaration = aDeclaration()
        withNewCaching(declaration)
        val response = controller.saveAddress(Mode.Normal)(postRequest(Json.obj(), declaration))
        status(response) mustBe BAD_REQUEST
      }
    }
    "return 303 See other when" when {
      "correct form is submitted" in {
        val declaration = aDeclaration()
        withNewCaching(declaration)
        val body = Json.obj("details" -> Json.obj("eori" -> "PL213472539481923"))
        val response = controller.saveAddress(Mode.Normal)(postRequest(body, declaration))

        await(response) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.ConsigneeDetailsController.displayPage()
      }
    }
  }
}
