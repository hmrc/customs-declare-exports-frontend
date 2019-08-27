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

import controllers.declaration.WarehouseIdentificationController
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.declaration.TransportCodes.Maritime
import helpers.views.declaration.WarehouseIdentificationMessages
import models.Mode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.warehouse_identification

class WarehouseIdentificationControllerSpec
    extends ControllerSpec with BeforeAndAfterEach with WarehouseIdentificationMessages with OptionValues {

  val warehouseIdentificationTemplate: warehouse_identification = mock[warehouse_identification]

  val controller = new WarehouseIdentificationController(
    authenticate = mockAuthAction,
    journeyType = mockJourneyAction,
    navigator,
    exportsCacheService = mockExportsCacheService,
    mcc = stubMessagesControllerComponents(),
    warehouseIdentificationPage = warehouseIdentificationTemplate
  )

  val exampleCustomsOfficeIdentifier = "A1B2C3D4"
  val exampleWarehauseIdentificationType = "R"
  val exampleWarehauseIdentificationNumber = "SecretStash"
  val exampleTransportMode = Maritime

  val cacheModel = aDeclaration(
    withChoice(SupplementaryDec),
    withWarehouseIdentification(
      Some(exampleCustomsOfficeIdentifier),
      Some(exampleWarehauseIdentificationType),
      Some(exampleWarehauseIdentificationNumber),
      Some(exampleTransportMode)
    )
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(cacheModel)
    when(warehouseIdentificationTemplate.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    Mockito.reset(warehouseIdentificationTemplate)
    super.afterEach()
  }

  "WerehouseIdentificationController on GET request" should {
    "return 200 OK" in {
      val response = controller.displayForm(Mode.Normal).apply(getRequest())
      status(response) must be(OK)
    }

    "read item from cache and display it" in {
      val result = controller.displayForm(Mode.Normal).apply(getRequest())
      await(result)
      verify(mockExportsCacheService).get(any())(any())
      verify(warehouseIdentificationTemplate).apply(any(), any())(any(), any())
    }
  }
  "Warehouse Identification Controller on POST" should {
    "update cache after successful bind" in {
      val body = Json.obj(
        "supervisingCustomsOffice" -> exampleCustomsOfficeIdentifier,
        "identificationType" -> exampleWarehauseIdentificationType,
        "identificationNumber" -> exampleWarehauseIdentificationNumber,
        "inlandModeOfTransportCode" -> exampleTransportMode
      )

      val result = controller.saveWarehouse(Mode.Normal).apply(postRequest(body))

      await(result) mustBe aRedirectToTheNextPage
      thePageNavigatedTo mustBe controllers.declaration.routes.BorderTransportController.displayForm()

      val updatedWarehouse = theCacheModelUpdated.locations.warehouseIdentification.value
      updatedWarehouse.supervisingCustomsOffice.value mustBe exampleCustomsOfficeIdentifier
      updatedWarehouse.identificationType.value mustBe exampleWarehauseIdentificationType
      updatedWarehouse.identificationNumber.value mustBe exampleWarehauseIdentificationNumber
      updatedWarehouse.inlandModeOfTransportCode.value mustBe exampleTransportMode
    }

    "return Bad Request if payload is not compatibile with model" in {
      val body = Json.obj("supervisingCustomsOffice" -> "A")
      val result = controller.saveWarehouse(Mode.Normal).apply(postRequest(body))

      status(result) mustBe BAD_REQUEST
    }
  }

}
