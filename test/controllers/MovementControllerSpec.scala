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
import play.api.libs.json.{JsObject, JsString}
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

        status(result) must be(SEE_OTHER)
      }

      "redirect to next departure page when departure chosen" in {
        authorizedUser()
        val form = Form(ChoiceForm.choiceMapping).fill(ChoiceForm("EDL"))
        withCaching[ChoiceForm](Some(form))
        withCaching[EnterDucrForm](None)

        val correctForm = JsObject(Map("choice" -> JsString("EDL")))
        val result = route(app, postRequest(choiceUri, correctForm)).get

        status(result) must be(SEE_OTHER)
      }
    }

    "ducr screen " should {
      "return http code 303 (redirection) with success" in {
        authorizedUser()

        val correctChoice = JsObject(Map("choice" -> JsString("EAL")))
        val result = route(app, postRequest(choiceUri, correctChoice)).get

        status(result) must be(SEE_OTHER)
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
        withCaching(Some(ChoiceForm("EAL")), MovementFormsAndIds.choiceId)
        withCaching(None, MovementFormsAndIds.enterDucrId)

        val emptyForm = JsObject(Map("" -> JsString("")))
        val result = route(app, postRequest(ducrUri, emptyForm)).get

        contentAsString(result) must include(messages("error.required"))
      }

      "validated submitted form for departure" in {
        authorizedUser()
        withCaching(Some(ChoiceForm("EDL")), MovementFormsAndIds.choiceId)
        withCaching(None, MovementFormsAndIds.enterDucrId)

        val emptyForm = JsObject(Map("" -> JsString("")))
        val result = route(app, postRequest(ducrUri, emptyForm)).get

        // TODO: maybe check if the page is the form page as well?
        contentAsString(result) must include(messages("error.required"))
      }

      "redirect to goods date page" in {
        authorizedUser()
        sendMovementRequest()

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
        withCaching[GoodsDateForm](None)

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
        withCaching[GoodsDateForm](None)

        val result = route(app, postRequest(goodsDateUri, wrongMaximumGoodsDate)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("movement.date.incorrectDay"))
        stringResult must include(messages("movement.date.incorrectMonth"))
        stringResult must include(messages("movement.date.incorrectHour"))
        stringResult must include(messages("movement.date.incorrectMinutes"))
      }

      "redirect to the next page" in {
        authorizedUser()
        withCaching[GoodsDateForm](None)

        val result = route(app, postRequest(goodsDateUri, goodsDate)).get

        status(result) must be(SEE_OTHER)
      }
    }
  }
}
