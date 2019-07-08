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
import forms.declaration.GoodsLocation
import forms.declaration.GoodsLocationTestData._
import helpers.views.declaration.LocationOfGoodsMessages
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class LocationControllerSpec extends CustomExportsBaseSpec with LocationOfGoodsMessages {

  private val uri = uriWithContextPath("/declaration/location-of-goods")

  override def beforeEach() {
    authorizedUser()
    withCaching[GoodsLocation](None)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Location Controller on GET" should {

    "return 200 status code" in {
      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "read item from cache and display it" in {

      val cachedData =
        GoodsLocation("Spain", "1", "1", Some("1"), Some("1"), Some("BAFTA Street"), Some("LS37BH"), Some("SecretCity"))
      
      withCaching[GoodsLocation](Some(cachedData), "GoodsLocation")

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      status(result) must be(OK)
      page must include("Spain")
      page must include("BAFTA Street")
      page must include("LS37BH")
      page must include("SecretCity")
    }
  }

  "Location Controller on POST" should {

    "validate request and redirect - incorrect values" in {

      val result = route(app, postRequest(uri, incorrectGoodsLocationJSON)).get
      val stringResult = contentAsString(result)

      status(result) must be(BAD_REQUEST)
      stringResult must include(messages(typeOfLocationError))
      stringResult must include(messages(qualifierOfIdentError))
      stringResult must include(messages(identOfLocationError))
      stringResult must include(messages(additionalQualifierError))
      stringResult must include(messages(locationAddressError))
      stringResult must include(messages(logPostCodeError))
      stringResult must include(messages(cityError))
    }

    "validate request and redirect - empty form" in {

      val result = route(app, postRequest(uri, emptyGoodsLocationJSON)).get
      val stringResult = contentAsString(result)

      status(result) must be(BAD_REQUEST)
      stringResult must include(messages(typeOfLocationEmpty))
      stringResult must include(messages(qualifierOfIdentEmpty))
    }

    "validate request and redirect - correct value for mandatory field" in {

      val correctGoodsLocation: JsValue =
        JsObject(
          Map(
            "country" -> JsString("Poland"),
            "typeOfLocation" -> JsString("t"),
            "qualifierOfIdentification" -> JsString("t"),
            "identificationOfLocation" -> JsString("TST"),
            "additionalQualifier" -> JsString("TST")
          )
        )
      val result = route(app, postRequest(uri, correctGoodsLocation)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/office-of-exit"))
    }

    "validate request and redirect - correct values" in {

      val result = route(app, postRequest(uri, correctGoodsLocationJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/office-of-exit"))
    }
  }
}
