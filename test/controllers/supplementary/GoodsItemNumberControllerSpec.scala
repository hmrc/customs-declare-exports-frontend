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
import forms.supplementary.GoodsItemNumber
import forms.supplementary.GoodsItemNumberSpec._
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class GoodsItemNumberControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  val uri = uriWithContextPath("/declaration/supplementary/good-item-number")

  before {
    authorizedUser()
  }

  "Good item number controller" should {

    "display good item number declaration form" in {
      withCaching[GoodsItemNumber](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.goodItemNumber.title"))
      stringResult must include(messages("supplementary.goodItemNumber"))
      stringResult must include(messages("supplementary.goodItemNumber.hint"))
    }

    "display \"Back\" button that links to \"Previous Documents\" page" in {
      withCaching[GoodsItemNumber](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("site.back"))
      stringResult must include(messages("/declaration/supplementary/previous-documents"))
    }

    "display \"Save and continue\" button on page" in {
      withCaching[GoodsItemNumber](None)

      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      resultAsString must include(messages("site.save_and_continue"))
      resultAsString must include("button id=\"submit\" class=\"button\"")
    }
  }

  "validate form - too many characters" in {
    withCaching[GoodsItemNumber](None)

    val incorrectGoodItemNumber: JsValue =
      JsObject(Map("goodItemNumber" -> JsString("4563")))
    val result = route(app, postRequest(uri, incorrectGoodItemNumber)).get
    val stringResult = contentAsString(result)

    stringResult must include(messages("supplementary.goodItemNumber.error"))
  }

  "validate form - cannot contain only zeros" in {
    withCaching[GoodsItemNumber](None)

    //TODO: not sure why we have . in here before
    val incorrectGoodItemNumber: JsValue =
      JsObject(Map("goodItemNumber" -> JsString("000")))
    val result = route(app, postRequest(uri, incorrectGoodItemNumber)).get
    val stringResult = contentAsString(result)

    stringResult must include(messages("supplementary.goodItemNumber.error"))
  }

  "validate form - contains alphabetic" in {
    withCaching[GoodsItemNumber](None)

    val alphabeticGoodItemNumber: JsValue =
      JsObject(Map("goodItemNumber" -> JsString("RGB")))
    val result = route(app, postRequest(uri, alphabeticGoodItemNumber)).get
    val stringResult = contentAsString(result)

    stringResult must include(messages("supplementary.goodItemNumber.error"))
  }

  "validate form and redirect - correct answer" in {
    withCaching[GoodsItemNumber](None)

    val result = route(app, postRequest(uri, correctGoodsItemNumberJSON)).get
    val header = result.futureValue.header

    status(result) must be(SEE_OTHER)

    header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/procedure-codes"))
  }
}
