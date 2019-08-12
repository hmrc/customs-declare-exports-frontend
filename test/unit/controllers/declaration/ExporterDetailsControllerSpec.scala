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
import forms.declaration.ExporterDetails
import org.mockito.{ArgumentCaptor, Mockito}
import org.mockito.ArgumentMatchers._
import play.api.data.Form
import play.api.libs.json.Json
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.exporter_details
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext

class ExporterDetailsControllerSpec extends ControllerSpec {

  val exporter_details = mock[exporter_details]

  val controller = new ExporterDetailsController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    stubMessagesControllerComponents(),
    exporter_details
  )(ExecutionContext.global)

  def templateArgument: Form[ExporterDetails] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ExporterDetails]])
    Mockito.verify(exporter_details).apply(captor.capture())(any(), any() )
    captor.getValue
  }


  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    Mockito.when(exporter_details.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    Mockito.reset(exporter_details)
    super.afterEach()
  }

  "Exporter Details Controller" should {
    "return 200 OK" when {
      "details are empty" in {
        val declaration = aDeclaration()
        withNewCaching(declaration)
        val response = controller.displayForm().apply(getRequest(declaration))
        status(response) mustBe OK
      }
      "details are filled" in {
        val declaration = aDeclaration(withExporterDetails(eori = Some(exampleUser.eori)))
        withNewCaching(declaration)
        val response = controller.displayForm().apply(getRequest(declaration))
        status(response) mustBe OK
      }
    }
    "return 400 bad request" when {
      "form contains erros" in {
        val declaration = aDeclaration()
        withNewCaching(declaration)
        val response = controller.saveAddress().apply(postRequest(Json.obj(), declaration))
        status(response) mustBe BAD_REQUEST
      }
    }
    "return 303 See other when" when {
      "correct form is submitted" in {
        val declaration = aDeclaration()
        withNewCaching(declaration)
        val body = Json.obj(
          "details" -> Json.obj(
            "eori" -> "PL213472539481923",
            "address" -> Json.obj(
              "fullname" -> "Example Subject",
              "addressLine" -> "Test Street",
              "townOrCity" -> "Test",
              "postCode" -> "AB12 34CD",
              "country" -> "GB"
            )
          )
        )
        val response = controller.saveAddress().apply(postRequest(body, declaration))
        println(templateArgument)
        status(response) mustBe SEE_OTHER
      }
    }
  }
}
