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
import forms.declaration.GoodsLocationSpec._
import helpers.views.declaration.LocationOfGoodsMessages
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class LocationControllerSpec extends CustomExportsBaseSpec with LocationOfGoodsMessages {

  val uri = uriWithContextPath("/declaration/location-of-goods")

  before {
    authorizedUser()
    withCaching[GoodsLocation](None)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Location Controller on display page" should {

    "validate request and redirect - incorrect values" in {

      val result = route(app, postRequest(uri, incorrectGoodsLocationJSON)).get
      val stringResult = contentAsString(result)

      status(result) must be(BAD_REQUEST)
      stringResult must include(messages(typeOfLocationError))
      stringResult must include(messages(qualifierOfIdentError))
      stringResult must include(messages(identOfLocationError))
      stringResult must include(messages(additionalIdentifierError))
      stringResult must include(messages(streetAndNumberError))
      stringResult must include(messages(logPostCodeError))
      stringResult must include(messages(cityError))
    }

    "validate request and redirect - empty form" in {

      val result = route(app, postRequest(uri, emptyGoodsLocationJSON)).get
      val stringResult = contentAsString(result)

      status(result) must be(BAD_REQUEST)
      stringResult must include(messages(identOfLocationEmpty))
    }

    "validate request and redirect - correct value for mandatory field" in {

      val correctGoodsLocation: JsValue =
        JsObject(Map("identificationOfLocation" -> JsString("abc")))
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
