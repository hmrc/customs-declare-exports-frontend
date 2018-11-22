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
import forms.{ChoiceForm, EnterDucrForm}
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers._

class MovementControllerSpec extends CustomExportsBaseSpec {

  val chooseMovementUri = uriWithContextPath("/choice")
  val arrivalUri = uriWithContextPath("/movement/arrivals")
  val departureUri = uriWithContextPath("/movement/departures")
  val ducrUri = uriWithContextPath("/movement/ducr")

  "Movement controller" when {
    "choice screen" should {
      "return http code 200 with success" in {
        authorizedUser()
        withCaching[ChoiceForm](None)

        val result = route(app, getRequest(chooseMovementUri)).get

        status(result) must be(OK)
      }

      "display radio button to choose arrival or departure" in {
        authorizedUser()
        withCaching[ChoiceForm](None)

        val result = route(app, getRequest(chooseMovementUri)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("movement.choice.description"))
        stringResult must include(messages("movement.choice.EAL"))
        stringResult must include(messages("movement.choice.EDL"))
      }

      "validate user choice - no choice" in {
        authorizedUser()
        withCaching[ChoiceForm](None)

        val emptyForm = JsObject(Map("" -> JsString("")))
        val result = route(app, postRequest(chooseMovementUri, emptyForm)).get

        contentAsString(result) must include(messages("error.required"))
      }

      "validate user choice - wrong choice" in {
        authorizedUser()
        withCaching[ChoiceForm](None)

        val wrongForm = JsObject(Map("choice" -> JsString("movement")))
        val result = route(app, postRequest(chooseMovementUri, wrongForm)).get

        contentAsString(result) must include(messages("movement.incorrectValue"))
      }

      "redirect to arrival when arrival chosen" in {
        authorizedUser()
        withCaching[ChoiceForm](None)

        val correctForm = JsObject(Map("choice" -> JsString("EAL")))
        val result = route(app, postRequest(chooseMovementUri, correctForm)).get
        val stringResult = contentAsString(result)

        status(result) must be(OK)
        stringResult must include("Arrival")
      }

      "redirect to departure when departure chosen" in {
        authorizedUser()
        withCaching[ChoiceForm](None)

        val correctForm = JsObject(Map("choice" -> JsString("EDL")))
        val result = route(app, postRequest(chooseMovementUri, correctForm)).get
        val stringResult = contentAsString(result)

        status(result) must be(OK)
        stringResult must include("Departure")
      }
    }

    "arrival" should {
      "return http code 200 with success" in {
        authorizedUser()

        val correctChoice = JsObject(Map("choice" -> JsString("EAL")))
        val result = route(app, postRequest(chooseMovementUri, correctChoice)).get

        status(result) must be(OK)
      }

      "display form" in {
        authorizedUser()

        val correctChoice = JsObject(Map("choice" -> JsString("EAL")))
        val result = route(app, postRequest(chooseMovementUri, correctChoice)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("movement.EAL.title"))
        stringResult must include(messages("movement.eori"))
        stringResult must include(messages("movement.agentLocation"))
      }

      "validated submitted form" in {
        authorizedUser()

        val emptyForm = JsObject(Map("" -> JsString("")))
        val result = route(app, postRequest(arrivalUri, emptyForm)).get

        contentAsString(result) must include(messages("error.required"))
      }

      "redirect to confirmation page" in {
        authorizedUser()
        sendMovementRequest()

        val result = route(app, postRequest(arrivalUri, correctArrival)).get
        val stringResult = contentAsString(result)

        status(result) must be(OK)
        //stringResult must include("Arrival has been submitted") //TODO fix problems with string - different one for arrival
        stringResult must include("GB/NLA-0YH06GF0V3CUPJC9393")
      }

      "redirect to error page on failed submission" in {
        authorizedUser()
        sendMovementRequest400Response()

        val result = route(app, postRequest(arrivalUri, correctArrival)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages("global.error.heading"))
        stringResult must include(messages("global.error.message"))
      }
    }

    "departure" should {
      "return http code 200 with success" in {
        authorizedUser()

        val correctChoice = JsObject(Map("choice" -> JsString("EDL")))
        val result = route(app, postRequest(chooseMovementUri, correctChoice)).get

        status(result) must be(OK)
      }

      "display form" in {
        authorizedUser()

        val correctChoice = JsObject(Map("choice" -> JsString("EDL")))
        val result = route(app, postRequest(departureUri, correctChoice)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("movement.EDL.title"))
        stringResult must include(messages("movement.eori"))
        stringResult must include(messages("movement.agentLocation"))
      }

      "validated submitted form" in {
        authorizedUser()

        val emptyForm = JsObject(Map("" -> JsString("")))
        val result = route(app, postRequest(departureUri, emptyForm)).get

        contentAsString(result) must include(messages("error.required"))
      }

      "redirect to confirmation page" in {
        authorizedUser()
        sendMovementRequest()

        val result = route(app, postRequest(departureUri, correctDeparture)).get
        val stringResult = contentAsString(result)

        status(result) must be(OK)
        //stringResult must include("Departure has been submitted") //TODO fix problems with string - different one for arrival
        stringResult must include("GB/NLA-0YH06GF0V3CUPJC9393")
      }

      "redirect to error page on failed submission" in {
        authorizedUser()
        sendMovementRequest400Response()

        val result = route(app, postRequest(departureUri, correctDeparture)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages("global.error.heading"))
        stringResult must include(messages("global.error.message"))
      }
    }

    "ducr" should {
      "return http code 200 with success" in {
        authorizedUser()
        withCaching[EnterDucrForm](None)

        val result = route(app, getRequest(ducrUri)).get

        status(result) must be(OK)
      }

      "display form" in {
        authorizedUser()
        withCaching[EnterDucrForm](None)

        val result = route(app, getRequest(ducrUri)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("movement.ducr"))
        stringResult must include(messages("movement.ducr.label"))
        stringResult must include(messages("movement.ducr.hint"))
      }

      "validate user choice - no choice" in {
        authorizedUser()
        withCaching[EnterDucrForm](None)

        val emptyForm = JsObject(Map("" -> JsString("")))
        val result = route(app, postRequest(ducrUri, emptyForm)).get

        contentAsString(result) must include(messages("error.required"))
      }

      "validate user choice - wrong choice" in {
        authorizedUser()
        withCaching[EnterDucrForm](None)

        val wrongForm = JsObject(Map("ducr" -> JsString("ducr")))
        val result = route(app, postRequest(ducrUri, wrongForm)).get

        contentAsString(result) must include(messages("error.ducr"))
      }

      "redirect to the next page" in {
        authorizedUser()
        withCaching[EnterDucrForm](None)

        val correctForm = JsObject(Map("ducr" -> JsString("5GB123456789000-123ABC456DEFIIIII")))
        val result = route(app, postRequest(ducrUri, correctForm)).get

        status(result) must be(OK)
        contentAsString(result) must include("DONE")
      }
    }
  }
}
