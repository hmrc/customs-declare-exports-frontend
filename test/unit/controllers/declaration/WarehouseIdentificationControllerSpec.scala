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

package unit.controllers.declaration

import base.TestHelper
import controllers.declaration.WarehouseIdentificationController
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.TransportCodes.Maritime
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify}
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import unit.base.ControllerSpec
import views.html.declaration.warehouse_identification

class WarehouseIdentificationControllerSpec extends ControllerSpec with BeforeAndAfterEach {

  val controller = new WarehouseIdentificationController(
    appConfig = minimalAppConfig,
    authenticate = mockAuthAction,
    journeyType = mockJourneyAction,
    customsCacheService = mockCustomsCacheService,
    exportsCacheService = mockExportsCacheService,
    mcc = stubMessagesControllerComponents(),
    warehouseIdentificationPage = new warehouse_identification(mainTemplate)
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aCacheModel(withChoice(SupplementaryDec), withoutWarehouseIdentification()))
  }

  "WerehouseIdentificationController on GET request" should {
    "return 200 OK" in {
      val response = controller.displayForm().apply(getRequest())
      status(response) must be(OK)
      verify(mockExportsCacheService, times(2)).get(any())
    }

    "read item from cache and display it" in {
      val customsOfficeIdentifier = "Office"
      val warehauseIdentificationType = "R"
      val warehauseIdentificationNumber = "SecretStash"
      val transportMode = Maritime
      withNewCaching(
        aCacheModel(
          withChoice(SupplementaryDec),
          withWarehouseIdentification(
            customsOfficeIdentifier,
            warehauseIdentificationType,
            warehauseIdentificationNumber,
            transportMode
          )
        )
      )

      val result = controller.displayForm().apply(getRequest())
      val page = contentAsString(result)

      status(result) must be(OK)
      page must include(customsOfficeIdentifier)
      page must include("supplementary.warehouse.identificationType.r") // determinate by identification type
      page must include(warehauseIdentificationNumber)
      page must include("supplementary.transportInfo.transportMode.sea") // determinate by transportMode
      verify(mockExportsCacheService, times(2)).get(any())
    }
  }
  "Warehouse Identification Controller on POST" should {

    "validate identification type" in {
      val incorrectWarehouseIdentification: JsValue =
        JsObject(Map("identificationType" -> JsString(TestHelper.createRandomAlphanumericString(2))))

      val result = controller.saveWarehouse().apply(postRequest(incorrectWarehouseIdentification))

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include("supplementary.warehouse.identificationType.error")
      verifyTheCacheIsUnchanged()
    }

    "validate identification number - more than 35 characters" in {
      val incorrectWarehouseIdentification: JsValue =
        JsObject(Map("identificationNumber" -> JsString(TestHelper.createRandomAlphanumericString(36))))

      val result = controller.saveWarehouse().apply(postRequest(incorrectWarehouseIdentification))

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include("supplementary.warehouse.identificationNumber.error")
      verifyTheCacheIsUnchanged()
    }

    "validate supervising customs office - invalid" in {

      val incorrectWarehouseOffice: JsValue =
        JsObject(Map("supervisingCustomsOffice" -> JsString("SOMEWRONGCODE")))

      val result = controller.saveWarehouse().apply(postRequest(incorrectWarehouseOffice))

      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include("supplementary.warehouse.supervisingCustomsOffice.error")
      verifyTheCacheIsUnchanged()
    }

    "validate inland mode transport code - wrong choice" in {

      val incorrectTransportCode: JsValue =
        JsObject(Map("inlandModeOfTransportCode" -> JsString("Incorrect more")))

      val result = controller.saveWarehouse().apply(postRequest(incorrectTransportCode))

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include("supplementary.warehouse.inlandTransportMode.error.incorrect")
      verifyTheCacheIsUnchanged()
    }
  }
}
