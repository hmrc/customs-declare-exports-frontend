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
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.TransportCodes._
import forms.declaration.WarehouseIdentification
import forms.declaration.WarehouseIdentificationSpec._
import helpers.views.declaration.WarehouseIdentificationMessages
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify}
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class WarehouseIdentificationControllerSpec extends CustomExportsBaseSpec with WarehouseIdentificationMessages {

  private val uri = uriWithContextPath("/declaration/warehouse")

  override def beforeEach() {
    authorizedUser()
    withNewCaching(aCacheModel(withChoice(SupplementaryDec)))
    withCaching[WarehouseIdentification](None)
  }

  override def afterEach() {
    Mockito.reset(mockExportsCacheService)
  }

  "Warehouse Identification Controller on POST" should {

    "validate identification type and number" in {

      val incorrectWarehouseIdentification: JsValue =
        JsObject(Map(
          "identificationType" -> JsString(WarehouseIdentification.IdentifierType.PUBLIC_CUSTOMS_1),
          "identificationNumber" -> JsString("")
        ))

      val result = route(app, postRequest(uri, incorrectWarehouseIdentification)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(identificationNumberError))
      verifyTheCacheIsUnchanged()
    }

    "validate supervising customs office - invalid" in {

      val incorrectWarehouseOffice: JsValue =
        JsObject(Map("supervisingCustomsOffice" -> JsString("SOMEWRONGCODE")))

      val Some(result) = route(app, postRequest(uri, incorrectWarehouseOffice))

      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include(messages(supervisingCustomsOfficeError))
      verifyTheCacheIsUnchanged()
    }

    "validate inland mode transport code - wrong choice" in {

      val incorrectTransportCode: JsValue =
        JsObject(Map("inlandModeOfTransportCode" -> JsString("Incorrect more")))

      val result = route(app, postRequest(uri, incorrectTransportCode)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(inlandTransportModeError))
      verifyTheCacheIsUnchanged()
    }

    "validate request and redirect - no answers" in {

      val result = route(app, postRequest(uri, emptyWarehouseIdentificationJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)

      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/border-transport"))
      theCacheModelUpdated.locations.warehouseIdentification must be(Some(emptyWarehouseIdentification))
    }

    "validate request and redirect - correct values" in {

      val result = route(app, postRequest(uri, correctWarehouseIdentificationJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)

      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/border-transport"))
      theCacheModelUpdated.locations.warehouseIdentification.get.identificationNumber must be(Some("1234567GB"))
    }
  }
}
