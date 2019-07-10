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
import helpers.views.declaration.ChoiceMessages
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers._
import services.cache.ExportsCacheModel

class ChoiceControllerSpec extends CustomExportsBaseSpec with ChoiceMessages {

  private val choiceUri = uriWithContextPath("/choice")
  private val sessionId = "12345"

  override def beforeEach() {
    authorizedUser()
    withNewCaching(createModelWithNoItems())
    withCaching[Choice](None, Choice.choiceId)
  }

  override def afterEach() {
    reset(mockCustomsCacheService)
    reset(mockExportsCacheService)
  }

  "Choice Controller on GET" should {

    "return 200 status code" in {
      val result = route(app, getRequest(choiceUri)).get

      status(result) must be(OK)
      verify(mockExportsCacheService).get(any())
    }

    "read item from cache and display it" in {

      val cachedData = Choice("SMP")
      withCaching[Choice](Some(cachedData), Choice.choiceId)

      val result = route(app, getRequest(choiceUri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include("value=\"SMP\" checked=\"checked\"")
      verify(mockExportsCacheService).get(any())
    }
  }

  "ChoiceController on POST" should {

    "display the choice page with error" when {

      "no value provided for choice" in {

        val emptyForm = JsObject(Map("" -> JsString("")))
        val result = route(app, postRequest(choiceUri, emptyForm)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(choiceEmpty))
        verifyTheCacheIsUnchanged()
      }

      "wrong value provided for choice" in {

        val wrongForm = JsObject(Map("value" -> JsString("test")))
        val result = route(app, postRequest(choiceUri, wrongForm)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(choiceError))
        verifyTheCacheIsUnchanged()
      }
    }

    "save the choice data to the cache" in {

      val validChoiceForm = JsObject(Map("value" -> JsString("SMP")))
      route(app, postRequest(choiceUri, validChoiceForm)).get.futureValue

      verify(mockCustomsCacheService)
        .cache[Choice](any(), ArgumentMatchers.eq(Choice.choiceId), any())(any(), any(), any())

      verify(mockExportsCacheService).update(any(), any[ExportsCacheModel])
    }

    "redirect to dispatch location page when 'Supplementary declaration' is selected" in {

      val correctForm = JsObject(Map("value" -> JsString(AllowedChoiceValues.SupplementaryDec)))
      val result = route(app, postRequest(choiceUri, correctForm)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/dispatch-location"))
      theCacheModelUpdated.choice must be("SMP")
    }

    "redirect to dispatch location page when 'Standard declaration' is selected" in {

      val correctForm = JsObject(Map("value" -> JsString(AllowedChoiceValues.StandardDec)))
      val result = route(app, postRequest(choiceUri, correctForm)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/dispatch-location"))
      theCacheModelUpdated.choice must be("STD")
    }

    "redirect to cancel declaration page when 'Cancel declaration' is selected" in {

      val correctForm = JsObject(Map("value" -> JsString(AllowedChoiceValues.CancelDec)))
      val result = route(app, postRequest(choiceUri, correctForm)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/cancel-declaration"))
      theCacheModelUpdated.choice must be("CAN")
    }

    "redirect to submissions page when 'View recent declarations' is selected" in {

      val correctForm = JsObject(Map("value" -> JsString(AllowedChoiceValues.Submissions)))
      val result = route(app, postRequest(choiceUri, correctForm)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/submissions"))
      theCacheModelUpdated.choice must be("SUB")
    }
  }
}
