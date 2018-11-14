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

class DeparturesControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/departure")

  "Departures controller" should {
    "return http code 200 with success" in {
      authorizedUser()

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "display departure form" in {
      authorizedUser()

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("movement.departures.title"))
      stringResult must include(messages("movement.eori"))
      stringResult must include(messages("movement.ucr"))
      stringResult must include(messages("movement.ucrType"))
      stringResult must include(messages("movement.goodsLocation"))
    }

    "validate submitted form" in {
      authorizedUser()

      val emptyForm = JsObject(Map("eori" -> JsString("")))
      val result = route(app, postRequest(uri, emptyForm)).get

      contentAsString(result) must include("Please enter a value")
    }

    "redirect to departure confirmation page" in {
      authorizedUser()
      sendMovementRequest()

      val result = route(app, postRequest(uri, correctDeparture)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include("Departure has been submitted")
      stringResult must include("GB/NLA-0YH06GF0V3CUPJC9393")
    }

    "redirect to error page on failed submission" in {
      authorizedUser()
      sendMovementRequest400Response()

      val result = route(app, postRequest(uri, correctDeparture)).get
      val stringResult = contentAsString(result)

      status(result) must be(BAD_REQUEST)
      stringResult must include("There is a problem with a service")
      stringResult must include("Please try again later.")
    }

  }
}
