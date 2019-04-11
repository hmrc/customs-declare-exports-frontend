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
import forms.declaration.TransportDetails
import generators.Generators
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import org.scalacheck.Arbitrary._
import org.scalatest.prop.PropertyChecks
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.Countries
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import views.html.declaration.transport_details

class TransportDetailsControllerSpec extends CustomExportsBaseSpec with Generators with PropertyChecks {

  private val uri = uriWithContextPath("/declaration/transport-details")

  val form: Form[TransportDetails] = Form(TransportDetails.formMapping)

  def view(form: Form[TransportDetails], request: FakeRequest[_]): Html =
    transport_details(form)(request, appConfig, messages, countries = Countries.allCountries)

  before {
    authorizedUser()
    withCaching[TransportDetails](None, TransportDetails.formId)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

  }

  after {
    reset(mockCustomsCacheService)
  }

  "GET" should {

    "return 200 code" in {
      val result = route(app, getRequest(uri)).value
      status(result) must be(OK)
    }

    "populate the form fields with data from cache" in {
      authorizedUser()
      val request = getRequest(uri)

      forAll(arbitrary[TransportDetails]) { transport =>
        withCaching[TransportDetails](Some(transport), TransportDetails.formId)
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
        withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
        val body = Seq(("typesOfPackages", "A1"))
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value

        intercept[InsufficientEnrolments](status(result))
      }
    }

    "return BAD_REQUEST" when {

      "invalid data is submitted" in {
        authorizedUser()
        withCaching[TransportDetails](None, TransportDetails.formId)
        withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

        val body = Seq(("container", ""), ("meansOfTransportCrossingTheBorderNationality", ""))
        val request = postRequestFormUrlEncoded(uri, body: _*)

        val result = route(app, request).value

        status(result) must be(BAD_REQUEST)
        contentAsString(result).replaceCSRF mustBe view(form.bindFromRequest()(request), request).body.replaceCSRF
      }

    }

    "add input to the cache" when {

      "with valid data and on click of add" in {

        forAll(arbitrary[TransportDetails]) { transportDetails =>
          authorizedUser()
          withCaching[TransportDetails](None, TransportDetails.formId)
          withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
          val body = Seq(
            ("container", transportDetails.container.toString),
            (
              "meansOfTransportCrossingTheBorderNationality",
              transportDetails.meansOfTransportCrossingTheBorderNationality.getOrElse("UK")
            ),
            ("meansOfTransportCrossingTheBorderType", transportDetails.meansOfTransportCrossingTheBorderType),
            (
              "meansOfTransportCrossingTheBorderIDNumber",
              transportDetails.meansOfTransportCrossingTheBorderIDNumber.getOrElse("")
            )
          )

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).value

          status(result) must be(SEE_OTHER)

          verify(mockCustomsCacheService)
            .cache[TransportDetails](
              any(),
              ArgumentMatchers.eq(TransportDetails.formId),
              ArgumentMatchers.eq(transportDetails)
            )(any(), any(), any())
        }
      }
    }

    "navigate to respective page" when {

      "on click of continue" in {
        forAll(arbitrary[TransportDetails]) { transportDetails =>
          authorizedUser()
          withCaching[TransportDetails](Some(transportDetails), TransportDetails.formId)
          withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
          val payload = Seq(
            ("container", transportDetails.container.toString),
            (
              "meansOfTransportCrossingTheBorderNationality",
              transportDetails.meansOfTransportCrossingTheBorderNationality.getOrElse("UK")
            ),
            ("meansOfTransportCrossingTheBorderType", transportDetails.meansOfTransportCrossingTheBorderType),
            (
              "meansOfTransportCrossingTheBorderIDNumber",
              transportDetails.meansOfTransportCrossingTheBorderIDNumber.getOrElse("")
            )
          )
          val nextPage = transportDetails.container match {
            case true => Some("/customs-declare-exports/declaration/add-transport-containers")
            case _    => Some("/customs-declare-exports/declaration/summary")
          }
          val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
          status(result) must be(SEE_OTHER)
          result.futureValue.header.headers.get("Location") must be(nextPage)
        }
      }
      "navigate to add-seal page if full dec and user selected no for containers" in {
        val transportDetails = TransportDetails(Some("Poland"), false, "10", Some("test"))
        authorizedUser()
        withCaching[TransportDetails](None, TransportDetails.formId)
        withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.StandardDec)), choiceId)
        val payload = Seq(
          ("container", transportDetails.container.toString),
          (
            "meansOfTransportCrossingTheBorderNationality",
            transportDetails.meansOfTransportCrossingTheBorderNationality.getOrElse("UK")
          ),
          ("meansOfTransportCrossingTheBorderType", transportDetails.meansOfTransportCrossingTheBorderType),
          (
            "meansOfTransportCrossingTheBorderIDNumber",
            transportDetails.meansOfTransportCrossingTheBorderIDNumber.getOrElse("")
          )
        )

        val result = route(app, postRequestFormUrlEncoded(uri, payload: _*)).value
        status(result) must be(SEE_OTHER)
        result.futureValue.header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/add-seal"))
      }
    }
  }

}
