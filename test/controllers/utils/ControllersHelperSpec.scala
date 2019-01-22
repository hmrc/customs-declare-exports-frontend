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

package controllers.utils

import controllers.helpers.ControllersHelper._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsArray, JsObject, JsString}
import play.api.test.{FakeHeaders, FakeRequest}

class ControllersHelperSpec extends WordSpec with MustMatchers {

  private val jsInput = JsObject(
    Map(
      "key_1" -> JsString("  value_1    "),
      "key_2" -> JsArray(Seq(JsString("ArrayValue_1   "), JsString("   ArrayValue_2"), JsString("   ArrayValue_3   "))),
      "key_3" -> JsString("value_3")
    )
  )

  private val formUrlEncodedInput: Map[String, Seq[String]] =
    Map(
      "key_1" -> Seq("  value_1    "),
      "key_2" -> Seq("ArrayValue_1   ", "   ArrayValue_2", "   ArrayValue_3   "),
      "key_3" -> Seq("value_3")
    )

  "ControllersHelper on trimFormData" should {
    "trim form data" when {

      "provided with JsValue input" in {
        val request = FakeRequest(method = "POST", uri = "/", headers = FakeHeaders(), body = jsInput)
        trimRequestBody(request).mapValues { values =>
          values.map(!_.contains(" "))
        }
      }

      "provided with FormUrlEncoded input " in {
        val request = FakeRequest(method = "POST", uri = "/", headers = FakeHeaders(), body = formUrlEncodedInput)
        trimRequestBody(request).mapValues { values =>
          values.map(!_.contains(" "))
        }
      }
    }
  }

}
