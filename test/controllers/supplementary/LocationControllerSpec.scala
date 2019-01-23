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

import base.{CustomExportsBaseSpec, TestHelper}
import forms.supplementary.GoodsLocation
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class LocationControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/declaration/supplementary/location-of-goods")

  "Location controller" should {
    "display goods location form" in {
      authorizedUser()
      withCaching[GoodsLocation](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.goodsLocation.title"))
      stringResult must include(messages("supplementary.country"))
      stringResult must include(messages("supplementary.goodsLocation.typeOfLocation"))
      stringResult must include(messages("supplementary.goodsLocation.qualifierOfIdentification"))
      stringResult must include(messages("supplementary.goodsLocation.identificationOfLocation"))
      stringResult must include(messages("supplementary.goodsLocation.additionalIdentifier"))
      stringResult must include(messages("supplementary.goodsLocation.streetAndNumber"))
      stringResult must include(messages("supplementary.goodsLocation.postCode"))
      stringResult must include(messages("supplementary.goodsLocation.city"))
    }

    "validate form - incorrect values" in {
      authorizedUser()
      withCaching[GoodsLocation](None)

      val incorrectGoodsLocation: JsValue = JsObject(
        Map(
          "country" -> JsString(TestHelper.createRandomString(3)),
          "typeOfLocation" -> JsString(TestHelper.createRandomString(2)),
          "qualifierOfIdentification" -> JsString(TestHelper.createRandomString(2)),
          "identificationOfLocation" -> JsString(TestHelper.createRandomString(4)),
          "additionalIdentifier" -> JsString(TestHelper.createRandomString(33)),
          "streetAndNumber" -> JsString(TestHelper.createRandomString(71)),
          "postCode" -> JsString(TestHelper.createRandomString(10)),
          "city" -> JsString(TestHelper.createRandomString(36))
        )
      )
      val result = route(app, postRequest(uri, incorrectGoodsLocation)).get
      val stringResult = contentAsString(result)

      status(result) must be(BAD_REQUEST)
      stringResult must include(messages("supplementary.goodsLocation.typeOfLocation.error"))
      stringResult must include(messages("supplementary.goodsLocation.qualifierOfIdentification.error"))
      stringResult must include(messages("supplementary.goodsLocation.identificationOfLocation.error"))
      stringResult must include(messages("supplementary.goodsLocation.additionalIdentifier.error"))
      stringResult must include(messages("supplementary.goodsLocation.streetAndNumber.error"))
      stringResult must include(messages("supplementary.goodsLocation.postCode.error"))
      stringResult must include(messages("supplementary.goodsLocation.city.error"))
    }

    "validate form - empty form" in {
      authorizedUser()
      withCaching[GoodsLocation](None)

      val emptyGoodsLocation: JsValue = JsObject(
        Map(
          "country" -> JsString(""),
          "typeOfLocation" -> JsString(""),
          "qualifierOfIdentification" -> JsString(""),
          "identificationOfLocation" -> JsString(""),
          "additionalIdentifier" -> JsString(""),
          "streetAndNumber" -> JsString(""),
          "postCode" -> JsString(""),
          "city" -> JsString("")
        )
      )
      val result = route(app, postRequest(uri, emptyGoodsLocation)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/procedure-codes"))
    }

    "validate form - correct values" in {
      authorizedUser()
      withCaching[GoodsLocation](None)

      val emptyGoodsLocation: JsValue = JsObject(
        Map(
          "country" -> JsString("United Kingdom"),
          "typeOfLocation" -> JsString("T"),
          "qualifierOfIdentification" -> JsString("Q"),
          "identificationOfLocation" -> JsString("LOC"),
          "additionalIdentifier" -> JsString("Additional identifier"),
          "streetAndNumber" -> JsString("Street and number"),
          "postCode" -> JsString("Postcode"),
          "city" -> JsString("City")
        )
      )
      val result = route(app, postRequest(uri, emptyGoodsLocation)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/procedure-codes"))
    }
  }
}
