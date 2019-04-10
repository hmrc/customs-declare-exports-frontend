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
import forms.declaration.TransportCodes._


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

    "display correct hints and questions" in {

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      page must include(messages(title))
      page must include(messages(titleHint))
      page must include(messages(identificationNumber))
      page must include(messages(supervisingCustomsOffice))
      page must include(messages(inlandTransportMode))
      page must include(messages(inlandTransportModeHint))
      page must include(messages(sea))
      page must include(messages(rail))
      page must include(messages(road))
      page must include(messages(air))
      page must include(messages(postalOrMail))
      page must include(messages(fixedTransportInstallations))
      page must include(messages(inlandWaterway))
      page must include(messages(unknown))
    }

    "read item from cache and display it" in {

      val cachedData = WarehouseIdentification(Some("Office"), Some("SecretStash"), Some(Maritime))
      withCaching[WarehouseIdentification](Some(cachedData), "IdentificationOfWarehouse")

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      status(result) must be(OK)
      page must include("Office")
      page must include("SecretStash")
      page must include("Sea transport")
    }
  }

  "Warehouse Identification Controller on POST" should {

    "validate request - incorrect values" in {

      val incorrectWarehouseIdentification: JsValue =
        JsObject(
          Map(
            "supervisingCustomsOffice" -> JsString(TestHelper.createRandomAlphanumericString(5)),
            "identificationNumber" -> JsString(TestHelper.createRandomAlphanumericString(37)),
            "inlandModeOfTransportCode" -> JsString("Incorrect mode of transport")
          )
        )
      val result = route(app, postRequest(uri, incorrectWarehouseIdentification)).get
      val page = contentAsString(result)

      status(result) must be(BAD_REQUEST)

      page must include(messages(supervisingCustomsOfficeError))
      page must include(messages(identificationNumberError))
    }

    "validate identification number - less than two characters" in {

      val incorrectWarehouseIdentification: JsValue =
        JsObject(Map("identificationNumber" -> JsString(TestHelper.createRandomAlphanumericString(1))))

      val result = route(app, postRequest(uri, incorrectWarehouseIdentification)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(identificationNumberError))
    }

    "validate identification number - more than 36 characters" in {

      val incorrectWarehouseIdentification: JsValue =
        JsObject(Map("identificationNumber" -> JsString(TestHelper.createRandomAlphanumericString(37))))

      val result = route(app, postRequest(uri, incorrectWarehouseIdentification)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(identificationNumberError))
    }

    "validate identification number - first letter is not capital" in {

      val incorrectWarehouseIdentification: JsValue =
        JsObject(Map("identificationNumber" -> JsString("r1234567GB")))

      val result = route(app, postRequest(uri, incorrectWarehouseIdentification)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(identificationNumberError))
    }

    "validate supervising customs office - less than 8 characters" in {

      val incorrectWarehouseOffice: JsValue =
        JsObject(Map("supervisingCustomsOffice" -> JsString(TestHelper.createRandomAlphanumericString(7))))

      val result = route(app, postRequest(uri, incorrectWarehouseOffice)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(supervisingCustomsOfficeError))
    }

    "validate supervising customs office - more than 8 characters" in {

      val incorrectWarehouseOffice: JsValue =
        JsObject(Map("supervisingCustomsOffice" -> JsString(TestHelper.createRandomAlphanumericString(9))))

      val result = route(app, postRequest(uri, incorrectWarehouseOffice)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(supervisingCustomsOfficeError))
    }

    "validate supervising customs office - 8 characters with special characters" in {

      val incorrectWarehouseOffice: JsValue =
        JsObject(Map("supervisingCustomsOffice" -> JsString("123 ,.78")))

      val result = route(app, postRequest(uri, incorrectWarehouseOffice)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(supervisingCustomsOfficeError))
    }

    "validate inland mode transport code - wrong choice" in {

      val incorrectTransportCode: JsValue =
        JsObject(Map("inlandModeOfTransportCode" -> JsString("Incorrect more")))

      val result = route(app, postRequest(uri, incorrectTransportCode)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(inlandTransportModeError))
    }

    "validate request and redirect - no answers" in {

      val result = route(app, postRequest(uri, emptyWarehouseIdentificationJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)

      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/border-transport"))
    }

    "validate request and redirect - correct values" in {

      val result = route(app, postRequest(uri, correctWarehouseIdentificationJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)

      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/border-transport"))
    }
  }
}
