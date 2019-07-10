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

import java.time.LocalDateTime

import base.{CustomExportsBaseSpec, TestHelper}
import forms.Choice
import forms.Choice.choiceId
import forms.declaration.TransportCodes._
import forms.declaration.WarehouseIdentification
import forms.declaration.WarehouseIdentificationSpec._
import helpers.views.declaration.WarehouseIdentificationMessages
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.verify
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import services.cache.ExportsCacheModel

class WarehouseIdentificationControllerSpec extends CustomExportsBaseSpec with WarehouseIdentificationMessages {

  private val uri = uriWithContextPath("/declaration/warehouse")

  override def beforeEach() {
    authorizedUser()
    withNewCaching(createModelWithNoItems())
    withCaching[WarehouseIdentification](None)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  override def afterEach()  {
    Mockito.reset(mockExportsCacheService)
  }

  "Warehouse Identification Controller on GET" should {

    "return 200 code" in {

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
      verify(mockExportsCacheService).get(any())
    }

    "read item from cache and display it" in {

      val cachedData = WarehouseIdentification(Some("Office"), Some("R"), Some("SecretStash"), Some(Maritime))
      withNewCaching(createModelWithNoItems().copy(warehouseIdentification = Some(cachedData)))

      val Some(result) = route(app, getRequest(uri))
      val page = contentAsString(result)

      status(result) must be(OK)
      page must include("Office")
      page must include(messages("supplementary.warehouse.identificationType.r"))
      page must include("SecretStash")
      page must include("Sea transport")
      verify(mockExportsCacheService).get(any())
    }
  }

  "Warehouse Identification Controller on POST" should {

    "validate identification type" in {

      val incorrectWarehouseIdentification: JsValue =
        JsObject(Map("identificationType" -> JsString(TestHelper.createRandomAlphanumericString(2))))

      val result = route(app, postRequest(uri, incorrectWarehouseIdentification)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(identificationTypeError))
      verifyTheCacheIsUnchanged()
    }

    "validate identification number - more than 35 characters" in {

      val incorrectWarehouseIdentification: JsValue =
        JsObject(Map("identificationNumber" -> JsString(TestHelper.createRandomAlphanumericString(36))))

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
      theCacheModelUpdated.warehouseIdentification must be(Some(emptyWarehouseIdentification))
    }

    "validate request and redirect - correct values" in {

      val result = route(app, postRequest(uri, correctWarehouseIdentificationJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)

      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/border-transport"))
      theCacheModelUpdated.warehouseIdentification.get.identificationNumber must be(Some("1234567GB"))
    }
  }
}
