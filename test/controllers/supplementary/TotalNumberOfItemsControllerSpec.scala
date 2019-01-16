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

package controllers.supplementary

import base.CustomExportsBaseSpec
import forms.supplementary.TotalNumberOfItems
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class TotalNumberOfItemsControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/declaration/supplementary/total-numbers-of-items")

  "Total number of items controller" should {
    "display total number of items form" in {
      authorizedUser()
      withCaching[TotalNumberOfItems](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.totalNumberOfItems.title"))
      stringResult must include(messages("supplementary.totalNumberOfItems"))
      stringResult must include(messages("supplementary.totalNumberOfItems.hint"))
    }

    "validate form - incorrect values - alphabetic" in {
      authorizedUser()
      withCaching[TotalNumberOfItems](None)

      val incorrectTotalNumber: JsValue = JsObject(Map("items" -> JsString("as3")))
      val result = route(app, postRequest(uri, incorrectTotalNumber)).get

      contentAsString(result) must include(messages("supplementary.totalNumberOfItems.error"))
    }

    "validate form - incorrect values - longer than 3" in {
      authorizedUser()
      withCaching[TotalNumberOfItems](None)

      val incorrectTotalNumber: JsValue = JsObject(Map("items" -> JsString("1234")))
      val result = route(app, postRequest(uri, incorrectTotalNumber)).get

      contentAsString(result) must include(messages("supplementary.totalNumberOfItems.error"))
    }

    "validate form - incorrect values - zeros" in {
      authorizedUser()
      withCaching[TotalNumberOfItems](None)

      val incorrectTotalNumber: JsValue = JsObject(Map("items" -> JsString("000")))
      val result = route(app, postRequest(uri, incorrectTotalNumber)).get

      contentAsString(result) must include(messages("supplementary.totalNumberOfItems.error"))
    }

    "validate form - correct values" in {
      pending
      authorizedUser()
      withCaching[TotalNumberOfItems](None)

      val correctTotalNumber: JsValue = JsObject(Map("items" -> JsString("100")))
      val result = route(app, postRequest(uri, correctTotalNumber)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/nature-of-transaction")
      )
    }
  }
}
