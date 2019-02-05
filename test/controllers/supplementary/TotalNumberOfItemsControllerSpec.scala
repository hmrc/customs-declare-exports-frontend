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
import forms.supplementary.TotalNumberOfItemsSpec._
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
      stringResult must include(messages("supplementary.totalAmountInvoiced"))
      stringResult must include(messages("supplementary.totalAmountInvoiced.hint"))
      stringResult must include(messages("supplementary.exchangeRate"))
      stringResult must include(messages("supplementary.exchangeRate.hint"))
    }

    "validate form - incorrect values - alphabetic" in {
      authorizedUser()
      withCaching[TotalNumberOfItems](None)

      val incorrectTotalNumber: JsValue =
        JsObject(Map("items" -> JsString("as3"), "totalPackage" -> JsString("asd12343")))
      val result = route(app, postRequest(uri, incorrectTotalNumber)).get

      contentAsString(result) must include(messages("supplementary.totalNumberOfItems.error"))
    }

    "validate form - incorrect values - longer than 3" in {
      authorizedUser()
      withCaching[TotalNumberOfItems](None)

      val incorrectTotalNumber: JsValue =
        JsObject(Map("items" -> JsString("1234"), "totalPackage" -> JsString("123456789")))
      val result = route(app, postRequest(uri, incorrectTotalNumber)).get

      contentAsString(result) must include(messages("supplementary.totalNumberOfItems.error"))
    }

    "validate form - incorrect values - zeros" in {
      authorizedUser()
      withCaching[TotalNumberOfItems](None)

      val incorrectTotalNumber: JsValue = JsObject(Map("items" -> JsString("000"), "totalPackage" -> JsString("123")))
      val result = route(app, postRequest(uri, incorrectTotalNumber)).get

      contentAsString(result) must include(messages("supplementary.totalNumberOfItems.error"))
    }

    "validate form - correct value for mandatory field" in {
      authorizedUser()
      withCaching[TotalNumberOfItems](None)

      val correctTotalNumber: JsValue = JsObject(Map("items" -> JsString("100"), "totalPackage" -> JsString("123")))
      val result = route(app, postRequest(uri, correctTotalNumber)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/transaction-type")
      )
    }

    "validate form - correct values for every field using integers for optional ones" in {
      authorizedUser()
      withCaching[TotalNumberOfItems](None)


      val result = route(app, postRequest(uri, correctTotalNumberOfItemsIntegerValuesJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/transaction-type")
      )
    }

    "validate form - correct values for every field using decimals for optional ones" in {
      authorizedUser()
      withCaching[TotalNumberOfItems](None)

      val result = route(app, postRequest(uri, correctTotalNumberOfItemsDecimalValuesJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/transaction-type")
      )
    }

    "validate form - correct mandatory field with incorrect optional due to too many digits after coma" in {
      authorizedUser()
      withCaching[TotalNumberOfItems](None)

      val incorrectOptionalFields: JsValue = JsObject(
        Map(
          "items" -> JsString("100"),
          "totalAmountInvoiced" -> JsString("12312312312312.122"),
          "exchangeRate" -> JsString("1212121.123456"),
          "totalPackage" -> JsString("123")
        )
      )

      val result = route(app, postRequest(uri, incorrectOptionalFields)).get

      contentAsString(result) must include(messages("supplementary.totalAmountInvoiced.error"))
      contentAsString(result) must include(messages("supplementary.exchangeRate.error"))
    }

    "validate form - correct mandatory field with incorrect optional due to too many digits before coma" in {
      authorizedUser()
      withCaching[TotalNumberOfItems](None)

      val incorrectOptionalFields: JsValue = JsObject(
        Map(
          "items" -> JsString("100"),
          "totalAmountInvoiced" -> JsString("12312312312312123.12"),
          "exchangeRate" -> JsString("1212121231.12345"),
          "totalPackage" -> JsString("123")
        )
      )

      val result = route(app, postRequest(uri, incorrectOptionalFields)).get

      contentAsString(result) must include(messages("supplementary.totalAmountInvoiced.error"))
      contentAsString(result) must include(messages("supplementary.exchangeRate.error"))
    }

    "validate form - correct mandatory field with incorrect optional due to too long integers" in {
      authorizedUser()
      withCaching[TotalNumberOfItems](None)

      val incorrectOptionalFields: JsValue = JsObject(
        Map(
          "items" -> JsString("100"),
          "totalAmountInvoiced" -> JsString("12312312312312123"),
          "exchangeRate" -> JsString("1212121231123123"),
          "totalPackage" -> JsString("123")
        )
      )

      val result = route(app, postRequest(uri, incorrectOptionalFields)).get

      contentAsString(result) must include(messages("supplementary.totalAmountInvoiced.error"))
      contentAsString(result) must include(messages("supplementary.exchangeRate.error"))
    }
  }
}
