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

package controllers.declaration

import base.CSRFUtil._
import base.CustomExportsBaseSpec
import forms.Choice
import forms.Choice.choiceId
import forms.declaration.BorderTransport
import generators.Generators
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary._
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.mvc.Request
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.Helpers._
import play.twirl.api.Html
import services.cache.ExportsCacheModel
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import views.html.declaration.border_transport

import scala.concurrent.Future

class BorderTransportControllerSpec extends CustomExportsBaseSpec with Generators with PropertyChecks {

  val form: Form[BorderTransport] = Form(BorderTransport.formMapping)
  private val uri = uriWithContextPath("/declaration/border-transport")
  private val borderTransportPage = app.injector.instanceOf[border_transport]

  def view(form: Form[BorderTransport], request: Request[_]): Html =
    borderTransportPage(form)(request, messages, appConfig)

  override def beforeEach() {
    authorizedUser()
    withNewCaching(createModel())
    withCaching[BorderTransport](None, BorderTransport.formId)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  override def afterEach() {
    reset(mockCustomsCacheService)
    reset(mockExportsCacheService)
  }

  "GET" should {

    "return 200 code" in {
      val result = route(app, getRequest(uri)).value
      status(result) must be(OK)
    }

    "populate the form fields with data from cache" in {
      val request = addCSRFToken(getRequest(uri))

      forAll(arbitrary[BorderTransport]) { transport =>
        withCaching[BorderTransport](Some(transport), BorderTransport.formId)
        val result = route(app, request).value

        contentAsString(result).replaceCSRF mustBe
          view(form.fill(transport), request).body.replaceCSRF()
      }
    }
  }

  ".onSubmit" should {

    "return UNAUTHORIZED" when {

      "user does not have an EORI" in {
        userWithoutEori()
        val body = Seq(("typesOfPackages", "A1"))
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value

        intercept[InsufficientEnrolments](status(result))
      }
    }

    "return BAD_REQUEST" when {

      "invalid data is submitted" in {
        val body = Seq(
          ("borderModeOfTransportCode", ""),
          ("meansOfTransportOnDepartureType", ""),
          ("meansOfTransportOnDepartureIDNumber", "test1")
        )
        val request = postRequestFormUrlEncoded(uri, body: _*)

        val result = route(app, request).value

        status(result) must be(BAD_REQUEST)
        contentAsString(result).replaceCSRF mustBe view(form.bindFromRequest()(request), request).body.replaceCSRF
      }

    }

    "add input to the cache" when {

      "with valid data and on click of add" in {

        forAll(arbitrary[BorderTransport]) { borderTransport =>
          val body = Seq(
            ("borderModeOfTransportCode", borderTransport.borderModeOfTransportCode),
            ("meansOfTransportOnDepartureType", borderTransport.meansOfTransportOnDepartureType),
            ("meansOfTransportOnDepartureIDNumber", borderTransport.meansOfTransportOnDepartureIDNumber.getOrElse(""))
          )

          val model = createModel("12345")
          when(mockExportsCacheService.get(any())).thenReturn(Future.successful(Some(model)))

          val updatedModel = model.copy(borderTransport = Some(borderTransport))

          when(mockExportsCacheService.update(any(), any[ExportsCacheModel]))
            .thenReturn(Future.successful(Some(updatedModel)))

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value

          status(result) must be(SEE_OTHER)
          result.futureValue.header.headers.get("Location") must be(
            Some("/customs-declare-exports/declaration/transport-details")
          )

          theCacheModelUpdated.borderTransport.get must be(borderTransport)

          verify(mockCustomsCacheService)
            .cache[BorderTransport](
              any(),
              ArgumentMatchers.eq(BorderTransport.formId),
              ArgumentMatchers.eq(borderTransport)
            )(any(), any(), any())

          reset(mockExportsCacheService)
        }
      }
    }

    "navigate to 'transport-details' page" when {

      "on click of continue when a record has already been added" in {
        forAll(arbitrary[BorderTransport]) { borderTransport =>
          withCaching[BorderTransport](Some(borderTransport), BorderTransport.formId)
          val payload = Seq(
            ("borderModeOfTransportCode", borderTransport.borderModeOfTransportCode),
            ("meansOfTransportOnDepartureType", borderTransport.meansOfTransportOnDepartureType),
            ("meansOfTransportOnDepartureIDNumber", borderTransport.meansOfTransportOnDepartureIDNumber.getOrElse(""))
          )
          val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
          status(result) must be(SEE_OTHER)
          result.futureValue.header.headers.get("Location") must be(
            Some("/customs-declare-exports/declaration/transport-details")
          )
        }
      }
    }
  }
}
