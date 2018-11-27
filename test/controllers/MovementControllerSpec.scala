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
import forms.{ChoiceForm, EnterDucrForm, GoodsDateForm, MovementFormsAndIds}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class MovementControllerSpec extends CustomExportsBaseSpec {

  val choiceUri = uriWithContextPath("/choice")
  val ducrUri = uriWithContextPath("/movement/ducr")
  val goodsDateUri = uriWithContextPath("/movement/goodsDate")
  val locationUri = uriWithContextPath("/movement/location")
  val transportUri = uriWithContextPath("/movement/transport")

  "Movement controller" when {
    "choice screen" should {
      "return http code 200 with success" in {
        authorizedUser()
        withCaching[ChoiceForm](None)

        val result = route(app, getRequest(choiceUri)).get

        status(result) must be(OK)
      }

      "display radio button to choose arrival or departure" in {
        authorizedUser()
        withCaching[ChoiceForm](None)

        val result = route(app, getRequest(choiceUri)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("movement.choice.description"))
        stringResult must include(messages("movement.choice.EAL"))
        stringResult must include(messages("movement.choice.EDL"))
      }

      "validate user choice - no choice" in {
        authorizedUser()
        withCaching[ChoiceForm](None)

        val emptyForm = JsObject(Map("" -> JsString("")))
        val result = route(app, postRequest(choiceUri, emptyForm)).get

        contentAsString(result) must include(messages("error.required"))
      }

      "validate user choice - wrong choice" in {
        authorizedUser()
        withCaching[ChoiceForm](None)

        val wrongForm = JsObject(Map("choice" -> JsString("movement")))
        val result = route(app, postRequest(choiceUri, wrongForm)).get

        contentAsString(result) must include(messages("movement.incorrectValue"))
      }

      "redirect to ducr for arrival page when arrival chosen" in {
        authorizedUser()
        val form = Form(ChoiceForm.choiceMapping).fill(ChoiceForm("EAL"))
        withCaching[ChoiceForm](Some(form))
        withCaching[EnterDucrForm](None)

        val correctForm = JsObject(Map("choice" -> JsString("EAL")))
        val result = route(app, postRequest(choiceUri, correctForm)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/movement/ducr"))
      }

      "redirect to next departure page when departure chosen" in {
        authorizedUser()
        val form = Form(ChoiceForm.choiceMapping).fill(ChoiceForm("EDL"))
        withCaching[ChoiceForm](Some(form))
        withCaching[EnterDucrForm](None)

        val correctForm = JsObject(Map("choice" -> JsString("EDL")))
        val result = route(app, postRequest(choiceUri, correctForm)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/movement/ducr"))
      }
    }

    "ducr screen " should {
      "return http code 200 with success" in {
        authorizedUser()
        withCaching(Some(ChoiceForm("EAL")), MovementFormsAndIds.choiceId)
        withCaching(None, MovementFormsAndIds.enterDucrId)

        val result = route(app, getRequest(choiceUri)).get

        status(result) must be(OK)
      }

      "display form for arrival" in {
        authorizedUser()
        withCaching(Some(ChoiceForm("EAL")), MovementFormsAndIds.choiceId)
        withCaching(None, MovementFormsAndIds.enterDucrId)

        val result = route(app, getRequest(ducrUri)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("movement.ducr"))
        stringResult must include(messages("movement.ducr.label"))
        stringResult must include(messages("movement.ducr.hint"))
      }

      "display form for departure" in {
        authorizedUser()
        withCaching(Some(ChoiceForm("EDL")), MovementFormsAndIds.choiceId)
        withCaching(None, MovementFormsAndIds.enterDucrId)

        val result = route(app, getRequest(ducrUri)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("movement.ducr"))
        stringResult must include(messages("movement.ducr.label"))
        stringResult must include(messages("movement.ducr.hint"))
      }

      "validated submitted form for arrival" in {
        authorizedUser()
        withCaching(None, MovementFormsAndIds.enterDucrId)

        val emptyForm = JsObject(Map("" -> JsString("")))
        val result = route(app, postRequest(ducrUri, emptyForm)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("error.required"))
        stringResult must include(messages("movement.ducr"))
        stringResult must include(messages("movement.ducr.label"))
        stringResult must include(messages("movement.ducr.hint"))
      }

      "validated submitted form for departure" in {
        authorizedUser()
        withCaching(None, MovementFormsAndIds.enterDucrId)

        val emptyForm = JsObject(Map("" -> JsString("")))
        val result = route(app, postRequest(ducrUri, emptyForm)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("error.required"))
        stringResult must include(messages("movement.ducr"))
        stringResult must include(messages("movement.ducr.label"))
        stringResult must include(messages("movement.ducr.hint"))
      }

      "redirect to goods date page" in {
        authorizedUser()
        withCaching(None, MovementFormsAndIds.enterDucrId)

        val result = route(app, postRequest(ducrUri, correctDucrJson)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/movement/goodsDate"))
      }
    }

    "goods date" should {
      "return http code 200 with success" in {
        authorizedUser()
        withCaching(Some(ChoiceForm("EAL")), MovementFormsAndIds.choiceId)
        withCaching(None, MovementFormsAndIds.goodsDateId)

        val result = route(app, getRequest(goodsDateUri)).get

        status(result) must be(OK)
      }

      "display form" in {
        authorizedUser()
        withCaching(Some(ChoiceForm("EAL")), MovementFormsAndIds.choiceId)
        withCaching(None, MovementFormsAndIds.goodsDateId)

        val result = route(app, getRequest(goodsDateUri)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("movement.date.day"))
        stringResult must include(messages("movement.date.month"))
        stringResult must include(messages("movement.date.year"))
        stringResult must include(messages("movement.date.hour"))
        stringResult must include(messages("movement.date.minute"))
      }

      "validate form with minimum values - incorrect values" in {
        authorizedUser()
        withCaching(None, MovementFormsAndIds.goodsDateId)

        val result = route(app, postRequest(goodsDateUri, wrongMinimumGoodsDate)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("movement.date.incorrectDay"))
        stringResult must include(messages("movement.date.incorrectMonth"))
        stringResult must include(messages("movement.date.incorrectYear"))
        stringResult must include(messages("movement.date.incorrectHour"))
        stringResult must include(messages("movement.date.incorrectMinutes"))
      }

      "validate form with maximum values - incorrect values" in {
        authorizedUser()
        withCaching(None, MovementFormsAndIds.goodsDateId)

        val result = route(app, postRequest(goodsDateUri, wrongMaximumGoodsDate)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("movement.date.incorrectDay"))
        stringResult must include(messages("movement.date.incorrectMonth"))
        stringResult must include(messages("movement.date.incorrectHour"))
        stringResult must include(messages("movement.date.incorrectMinutes"))
      }

      "redirect to the next page" in {
        authorizedUser()
        withCaching(None, MovementFormsAndIds.goodsDateId)

        val result = route(app, postRequest(goodsDateUri, goodsDate)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/movement/location"))
      }
    }

    "location" should {
      "return http code 200 with success" in {
        authorizedUser()
        withCaching(Some(ChoiceForm("EAL")), MovementFormsAndIds.choiceId)
        withCaching(None, MovementFormsAndIds.locationId)

        val result = route(app, getRequest(locationUri)).get

        status(result) must be(OK)
      }

      "display form" in {
        authorizedUser()
        withCaching(Some(ChoiceForm("EAL")), MovementFormsAndIds.choiceId)
        withCaching(None, MovementFormsAndIds.locationId)

        val result = route(app, getRequest(locationUri)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("movement.agentLocation"))
        stringResult must include(messages("movement.agentRole"))
        stringResult must include(messages("movement.goodsLocation"))
        stringResult must include(messages("movement.shed"))
      }

      "redirect to the next page with empty input data" in {
        authorizedUser()
        withCaching(Some(ChoiceForm("EAL")), MovementFormsAndIds.choiceId)
        withCaching(None, MovementFormsAndIds.locationId)

        val result = route(app, postRequest(locationUri, emptyLocation)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/movement/transport"))
      }

      "redirect to the next page with correct input data" in {
        authorizedUser()
        withCaching(Some(ChoiceForm("EAL")), MovementFormsAndIds.choiceId)
        withCaching(None, MovementFormsAndIds.locationId)

        val result = route(app, postRequest(locationUri, location)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/movement/transport"))
      }
    }

    "transport" should {
      "return http code 200 with success" in {
        authorizedUser()
        withCaching(Some(ChoiceForm("EAL")), MovementFormsAndIds.choiceId)
        withCaching(None, MovementFormsAndIds.transportId)

        val result = route(app, getRequest(transportUri)).get

        status(result) must be(OK)
      }

      "display form" in {
        authorizedUser()
        withCaching(Some(ChoiceForm("EAL")), MovementFormsAndIds.choiceId)
        withCaching(None, MovementFormsAndIds.transportId)

        val result = route(app, getRequest(transportUri)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("movement.transport.id"))
        stringResult must include(messages("movement.transport.mode"))
        stringResult must include(messages("movement.transport.nationality"))
      }

      "validate input data - incorrect input data" in {
        authorizedUser()
        withCaching(Some(ChoiceForm("EAL")), MovementFormsAndIds.choiceId)
        withCaching(None, MovementFormsAndIds.transportId)

        val result = route(app, postRequest(transportUri, incorrectTransport)).get
        val stringResult = contentAsString(result)

        stringResult must include("Maximum length is 1")
        stringResult must include("Maximum length is 2")
      }

      "redirect to the next page with empty input data" in {
        authorizedUser()
        withCaching(Some(ChoiceForm("EAL")), MovementFormsAndIds.choiceId)
        withCaching(None, MovementFormsAndIds.transportId)

        val result = route(app, postRequest(transportUri, JsObject(Map("" -> JsString(""))))).get
        val stringResult = contentAsString(result) //TODO add correct redirection

        status(result) must be(OK)
        stringResult must include("Done")
      }

      "redirect to the next page with correct input data" in {
        authorizedUser()
        withCaching(Some(ChoiceForm("EAL")), MovementFormsAndIds.choiceId)
        withCaching(None, MovementFormsAndIds.transportId)

        val result = route(app, postRequest(transportUri, correctTransport)).get
        val stringResult = contentAsString(result) //TODO add correct redirection

        status(result) must be(OK)
        stringResult must include("Done")
      }
    }
  }
}
