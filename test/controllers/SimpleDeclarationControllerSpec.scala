/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers

import base.CustomExportsBaseSpec
import base.ExportsTestData._
import forms.SimpleDeclarationForm
import org.joda.time.DateTime
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization

class SimpleDeclarationControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/simple-declaration")
  implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(randomString(255))), nsStamp = DateTime.now().getMillis)

  "SimpleDeclaration" should {
    "return 200 with a success" in {
      authorizedUser()
      withCaching[SimpleDeclarationForm](None)

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "display Simple-declaration" in {
      authorizedUser()
      withCaching(None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      stringResult must include("Do you have a representative?")
      stringResult must include("Is consolidate DUCR to wider shipment?")
      stringResult must include("Building and street")
    }

    "validate form submitted" in {
      authorizedUser()
      withCaching(None)
      successfulCustomsDeclarationResponse()
      withNrsSubmission()

      val result = route(app, postRequest(uri, wrongJson)).get
      contentAsString(result) must include("Incorrect DUCR")
    }

    "redirect to error page when submission failed in customs declarations" in {
      authorizedUser()
      withCaching(None)
      customsDeclaration400Response()

      val result = route(app, postRequest(uri, jsonBody)).get
      val stringResult = contentAsString(result)

      status(result) must be(BAD_REQUEST)
      stringResult must include("There is a problem with a service")
      stringResult must include("Please try again later.")
    }

    "redirect to next page" in {
      authorizedUser()
      withCaching(None)
      successfulCustomsDeclarationResponse()
      withNrsSubmission()

      val result = route(app, postRequest(uri, jsonBody)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include("Confirmation page")
      stringResult must include("Your reference number is")
    }
  }
}
