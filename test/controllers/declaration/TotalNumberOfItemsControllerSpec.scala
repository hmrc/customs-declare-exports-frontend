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

package controllers.declaration

import base.CustomExportsBaseSpec
import forms.Choice
import forms.Choice.choiceId
import forms.declaration.TotalNumberOfItems
import helpers.views.declaration.TotalNumberOfItemsMessages
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class TotalNumberOfItemsControllerSpec extends CustomExportsBaseSpec with TotalNumberOfItemsMessages {

  private val uri = uriWithContextPath("/declaration/total-numbers-of-items")

  before {

    authorizedUser()
    withCaching[TotalNumberOfItems](None)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Total Number Of Items Controller on GET" should {

    "return 200 code" in {

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "read item from cache and display it" in {

      val cachedData = TotalNumberOfItems("163.2", "7987.1", "1.33", " 631.1")
      withCaching[TotalNumberOfItems](Some(cachedData), "TotalNumberOfItems")

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      status(result) must be(OK)
      page must include("163.2")
      page must include("7987.1")
      page must include("1.33")
      page must include("631.1")
    }
  }

  "Total Number Of Items Controller on POST" should {

    "validate request and redirect - correct values for all fields (integers)" in {

      val allFields: JsValue =
        JsObject(
          Map(
            "itemsQuantity" -> JsString("100"),
            "totalAmountInvoiced" -> JsString("456"),
            "exchangeRate" -> JsString("789"),
            "totalPackage" -> JsString("123")
          )
        )
      val result = route(app, postRequest(uri, allFields)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/transaction-type"))
    }

    "validate request and redirect - correct values for all fields (decimals)" in {

      val allFields: JsValue =
        JsObject(
          Map(
            "itemsQuantity" -> JsString("100"),
            "totalAmountInvoiced" -> JsString("456.78"),
            "exchangeRate" -> JsString("789.789"),
            "totalPackage" -> JsString("123")
          )
        )
      val result = route(app, postRequest(uri, allFields)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)

      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/transaction-type"))
    }

    "validate request and redirect - all inputs alphabetic" in {

      val allFields: JsValue =
        JsObject(
          Map(
            "itemsQuantity" -> JsString("test"),
            "totalAmountInvoiced" -> JsString("test"),
            "exchangeRate" -> JsString("test"),
            "totalPackage" -> JsString("test")
          )
        )
      val result = route(app, postRequest(uri, allFields)).get

      status(result) must be(BAD_REQUEST)

      contentAsString(result) must include(messages(tnoiError))
      contentAsString(result) must include(messages(taiError))
      contentAsString(result) must include(messages(erError))
      contentAsString(result) must include(messages(tpqError))
    }

    "validate request and redirect - all inputs too long" in {

      val allFields: JsValue =
        JsObject(
          Map(
            "itemsQuantity" -> JsString("1234"),
            "totalAmountInvoiced" -> JsString("12312312312312123"),
            "exchangeRate" -> JsString("1212121231123123"),
            "totalPackage" -> JsString("123456789")
          )
        )
      val result = route(app, postRequest(uri, allFields)).get

      status(result) must be(BAD_REQUEST)

      contentAsString(result) must include(messages(tnoiError))
      contentAsString(result) must include(messages(taiError))
      contentAsString(result) must include(messages(erError))
      contentAsString(result) must include(messages(tpqError))
    }

    "validate request and redirect - Total Number of Items is 0" in {

      val incorrectTotalNumber: JsValue =
        JsObject(
          Map(
            "itemsQuantity" -> JsString("000"),
            "totalAmountInvoiced" -> JsString("999.99"),
            "exchangeRate" -> JsString("999999.99999"),
            "totalPackage" -> JsString("123")
          )
        )
      val result = route(app, postRequest(uri, incorrectTotalNumber)).get

      status(result) must be(BAD_REQUEST)

      contentAsString(result) must include(messages(tnoiError))
    }

    "validate request and redirect - Total Amount Invoiced / Exchange Rate too long decimal format" in {

      val allFields: JsValue = JsObject(
        Map(
          "itemsQuantity" -> JsString("100"),
          "totalAmountInvoiced" -> JsString("12312312312312.122"),
          "exchangeRate" -> JsString("1212121.123456"),
          "totalPackage" -> JsString("123")
        )
      )

      val result = route(app, postRequest(uri, allFields)).get
      status(result) must be(BAD_REQUEST)

      contentAsString(result) must include(messages(taiError))
      contentAsString(result) must include(messages(erError))
    }

    "validate request and redirect - Total Amount Invoiced / Exchange Rate too long base integer" in {

      val allFields: JsValue = JsObject(
        Map(
          "itemsQuantity" -> JsString("100"),
          "totalAmountInvoiced" -> JsString("12312312312312123.12"),
          "exchangeRate" -> JsString("1212121231.12345"),
          "totalPackage" -> JsString("123")
        )
      )

      val result = route(app, postRequest(uri, allFields)).get
      status(result) must be(BAD_REQUEST)

      contentAsString(result) must include(messages(taiError))
      contentAsString(result) must include(messages(erError))
    }
  }
}
