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
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers._

class ArrivalsControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/arrival")

  "Arrivals controller" should {
    "return 200 with a success" in {
      authorizedUser()

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "display arrival form" in {
      authorizedUser()

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      stringResult must include("EORI")
      stringResult must include("Goods arrival date time")
      stringResult must include("Movement reference")
    }

    "validate form submitted" in {
      authorizedUser()

      val emptyForm = JsObject(Map("eori" -> JsString("")))
      val result = route(app, postRequest(uri, emptyForm)).get

      contentAsString(result) must include("Please enter a value")
    }

    "redirect to error page when arrival submission failed" in {
      authorizedUser()
      sendMovementRequest400Response()

      val result = route(app, postRequest(uri, correctArrival)).get
      val stringResult = contentAsString(result)

      status(result) must be(BAD_REQUEST)
      stringResult must include("There is a problem with a service")
      stringResult must include("Please try again later.")
    }

    "redirect to arrival confirmation page" in {
      authorizedUser()
      sendMovementRequest()

      val result = route(app, postRequest(uri, correctArrival)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include("Arrival submitted")
      stringResult must include("Movement reference number")
    }
  }

}
