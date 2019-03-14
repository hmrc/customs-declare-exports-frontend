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
import forms.supplementary.GoodsLocation
import forms.supplementary.GoodsLocationSpec._
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class LocationControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  val uri = uriWithContextPath("/declaration/supplementary/location-of-goods")

  before {
    authorizedUser()
  }

  "Location Controller on display page" should {

    "display goods location form" in {
      authorizedUser()
      withCaching[GoodsLocation](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.goodsLocation.title"))
      stringResult must include(messages("supplementary.address.country"))
      stringResult must include(messages("supplementary.goodsLocation.typeOfLocation"))
      stringResult must include(messages("supplementary.goodsLocation.qualifierOfIdentification"))
      stringResult must include(messages("supplementary.goodsLocation.identificationOfLocation"))
      stringResult must include(messages("supplementary.goodsLocation.additionalIdentifier"))
      stringResult must include(messages("supplementary.goodsLocation.streetAndNumber"))
      stringResult must include(messages("supplementary.goodsLocation.postCode"))
      stringResult must include(messages("supplementary.goodsLocation.city"))
    }

    "display \"Back\" button that links to \"Destination Countries\" page" in {
      withCaching[GoodsLocation](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("site.back"))
      stringResult must include(messages("/declaration/supplementary/destination-countries"))
    }

    "display \"Save and continue\" button on page" in {

      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      resultAsString must include(messages("site.save_and_continue"))
      resultAsString must include("button id=\"submit\" class=\"button\"")
    }

    "validate form - incorrect values" in {
      withCaching[GoodsLocation](None)

      val result = route(app, postRequest(uri, incorrectGoodsLocationJSON)).get
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

    "validate form and redirect - empty form" in {
      withCaching[GoodsLocation](None)

      val result = route(app, postRequest(uri, emptyGoodsLocationJSON)).get
      val stringResult = contentAsString(result)

      status(result) must be(BAD_REQUEST)
      stringResult must include(messages("supplementary.goodsLocation.identificationOfLocation.empty"))
    }

    "validate form - correct value for mandatory field" in {
      authorizedUser()
      withCaching[GoodsLocation](None)

      val correctGoodsLocation: JsValue =
        JsObject(Map("identificationOfLocation" -> JsString("abc")))
      val result = route(app, postRequest(uri, correctGoodsLocation)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/office-of-exit"))
    }

    "validate form and redirect - correct values" in {
      withCaching[GoodsLocation](None)

      val result = route(app, postRequest(uri, correctGoodsLocationJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/office-of-exit"))
    }
  }
}
