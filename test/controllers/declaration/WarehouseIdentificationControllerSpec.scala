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

import base.{CustomExportsBaseSpec, TestHelper}
import forms.Choice
import forms.Choice.choiceId
import forms.declaration.WarehouseIdentification
import forms.declaration.WarehouseIdentificationSpec._
import helpers.views.declaration.WarehouseIdentificationMessages
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class WarehouseIdentificationControllerSpec extends CustomExportsBaseSpec with WarehouseIdentificationMessages {

  private val uri = uriWithContextPath("/declaration/warehouse")

  before {
    authorizedUser()
    withCaching[WarehouseIdentification](None)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Warehouse Identification Controller on GET" should {

    "return 200 code" in {

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "read item from cache and display it" in {

      val cachedData = WarehouseIdentification(Some("SecretStash"))
      withCaching[WarehouseIdentification](Some(cachedData), "IdentificationOfWarehouse")

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      status(result) must be(OK)
      page must include("SecretStash")
    }
  }

  "Warehouse Identification Controller on POST" should {

    "validate request - too many characters" in {

      val incorrectWarehouseIdentification: JsValue =
        JsObject(Map("identificationNumber" -> JsString(TestHelper.createRandomString(37))))
      val result = route(app, postRequest(uri, incorrectWarehouseIdentification)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(winError))
    }

    "validate request - less than two characters" in {

      val incorrectWarehouseIdentification: JsValue =
        JsObject(Map("identificationNumber" -> JsString(TestHelper.createRandomString(1))))
      val result = route(app, postRequest(uri, incorrectWarehouseIdentification)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(winError))
    }

    "validate request - first letter is not capital" in {

      val incorrectWarehouseIdentification: JsValue =
        JsObject(Map("identificationNumber" -> JsString("r1234567GB")))
      val result = route(app, postRequest(uri, incorrectWarehouseIdentification)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(winError))
    }

    "validate request and redirect - no answers" in {

      val result = route(app, postRequest(uri, emptyWarehouseIdentificationJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)

      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/export-items"))
    }

    "validate request and redirect - correct values" in {

      val result = route(app, postRequest(uri, correctWarehouseIdentificationJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)

      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/export-items"))
    }
  }
}
