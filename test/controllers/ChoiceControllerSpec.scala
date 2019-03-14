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

package controllers

import base.CustomExportsBaseSpec
import forms.Choice
import forms.Choice._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers._

class ChoiceControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  val choiceUri = uriWithContextPath("/choice")

  before {
    authorizedUser()
    withCaching[Choice](None, Choice.choiceId)
  }

  "Choice Controller on display" should {

    "display \"Save and continue\" button on page" in {
      val result = route(app, getRequest(choiceUri)).get
      val resultAsString = contentAsString(result)

      resultAsString must include(messages("site.save_and_continue"))
      resultAsString must include("button id=\"submit\" class=\"button\"")
    }

    "display \"Back\" button that links to \"Make an export declaration\" page" in {
      val result = route(app, getRequest(choiceUri)).get

      contentAsString(result) must include(messages("site.back"))
      contentAsString(result) must include("start")
    }

    "return http code 200 with success" in {
      val result = route(app, getRequest(choiceUri)).get

      status(result) must be(OK)
    }

    "display radio button to choose simplified declaration or standard declaration or arrival or departure" in {
      val result = route(app, getRequest(choiceUri)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("movement.choice.description"))
      stringResult must include(messages("declaration.choice.SMP"))
      stringResult must include(messages("declaration.choice.STD"))
      stringResult must include(messages("declaration.choice.CAN"))
      stringResult must include(messages("movement.choice.EAL"))
      stringResult must include(messages("movement.choice.EDL"))
      stringResult must include(messages("declaration.choice.SUB"))
    }
  }

  "ChoiceController on submit" should {

    "display the choice page with error" when {

      "no value provided for choice" in {
        val emptyForm = JsObject(Map("" -> JsString("")))
        val result = route(app, postRequest(choiceUri, emptyForm)).get

        contentAsString(result) must include(messages("choicePage.input.error.empty"))
      }

      "wrong value provided for choice" in {
        val wrongForm = JsObject(Map("choice" -> JsString("movement")))
        val result = route(app, postRequest(choiceUri, wrongForm)).get

        contentAsString(result) must include(messages("choicePage.input.error.incorrectValue"))
      }
    }

    "save the choice data to the cache" in {
      reset(mockCustomsCacheService)
      withCaching[Choice](None, Choice.choiceId)

      val validChoiceForm = JsObject(Map("choice" -> JsString("SMP")))
      route(app, postRequest(choiceUri, validChoiceForm)).get.futureValue

      verify(mockCustomsCacheService)
        .cache[Choice](any(), ArgumentMatchers.eq(Choice.choiceId), any())(any(), any(), any())
    }

    "redirect to dispatch location page when simplified declaration chosen" in {
      val correctForm = JsObject(Map("choice" -> JsString(AllowedChoiceValues.SupplementaryDec)))
      val result = route(app, postRequest(choiceUri, correctForm)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/dispatch-location")
      )
    }

    "redirect to choice page when standard declaration chosen" in {
      val correctForm = JsObject(Map("choice" -> JsString(AllowedChoiceValues.StandardDec)))
      val result = route(app, postRequest(choiceUri, correctForm)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/choice"))
    }

    "redirect to ducr for arrival page when arrival chosen" in {
      val correctForm = JsObject(Map("choice" -> JsString(AllowedChoiceValues.Arrival)))
      val result = route(app, postRequest(choiceUri, correctForm)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/movement/ducr"))
    }

    "redirect to ducr for departure page when departure chosen" in {
      val correctForm = JsObject(Map("choice" -> JsString(AllowedChoiceValues.Departure)))
      val result = route(app, postRequest(choiceUri, correctForm)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/movement/ducr"))
    }
  }

  "redirect to submissions page when View recent declarations chosen" in {
    val correctForm = JsObject(Map("choice" -> JsString(AllowedChoiceValues.Submissions)))
    val result = route(app, postRequest(choiceUri, correctForm)).get
    val header = result.futureValue.header

    status(result) must be(SEE_OTHER)
    header.headers.get("Location") must be(Some("/customs-declare-exports/submissions"))
  }
}
