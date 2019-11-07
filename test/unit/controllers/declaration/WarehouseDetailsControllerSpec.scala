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

import controllers.declaration.WarehouseDetailsController
import forms.declaration.TransportCodes.Maritime
import helpers.views.declaration.WarehouseIdentificationMessages
import models.{DeclarationType, Mode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.warehouse_details

class WarehouseDetailsControllerSpec extends ControllerSpec with BeforeAndAfterEach with WarehouseIdentificationMessages with OptionValues {

  val warehouseIdentificationTemplate: warehouse_details = mock[warehouse_details]

  val controller = new WarehouseDetailsController(
    authenticate = mockAuthAction,
    journeyType = mockJourneyAction,
    navigator,
    exportsCacheService = mockExportsCacheService,
    mcc = stubMessagesControllerComponents(),
    warehouseDetailsPage = warehouseIdentificationTemplate
  )

  val exampleCustomsOfficeIdentifier = "A1B2C3D4"
  val exampleWarehouseIdentificationType = "R"
  val exampleWarehouseIdentificationNumber = "SecretStash"
  val exampleTransportMode = Maritime

  private val standardCacheModel = aDeclaration(
    withType(DeclarationType.STANDARD),
    withWarehouseIdentification(
      Some(exampleCustomsOfficeIdentifier),
      Some(exampleWarehouseIdentificationType),
      Some(exampleWarehouseIdentificationNumber),
      Some(exampleTransportMode)
    )
  )

  private val suplementaryCacheModel = aDeclaration(
    withType(DeclarationType.SUPPLEMENTARY),
    withWarehouseIdentification(
      Some(exampleCustomsOfficeIdentifier),
      Some(exampleWarehouseIdentificationType),
      Some(exampleWarehouseIdentificationNumber),
      Some(exampleTransportMode)
    )
  )

  private val simplifiedCacheModel = aDeclaration(
    withType(DeclarationType.SIMPLIFIED),
    withWarehouseIdentification(
      Some(exampleCustomsOfficeIdentifier),
      Some(exampleWarehouseIdentificationType),
      Some(exampleWarehouseIdentificationNumber),
      Some(exampleTransportMode)
    )
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(warehouseIdentificationTemplate.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    Mockito.reset(warehouseIdentificationTemplate)
    super.afterEach()
  }

  "WarehouseIdentificationController on GET request" should {
    "return 200 OK" in {
      withNewCaching(suplementaryCacheModel)
      val response = controller.displayPage(Mode.Normal).apply(getRequest())
      status(response) must be(OK)
    }

    "read item from cache and display it" in {
      withNewCaching(suplementaryCacheModel)
      val result = controller.displayPage(Mode.Normal).apply(getRequest())
      await(result)
      verify(mockExportsCacheService).get(any())(any())
      verify(warehouseIdentificationTemplate).apply(any(), any())(any(), any())
    }
  }
  "Warehouse Identification Controller on POST" when {

    val body = Json.obj(
      "supervisingCustomsOffice" -> exampleCustomsOfficeIdentifier,
      "identificationType" -> exampleWarehouseIdentificationType,
      "identificationNumber" -> exampleWarehouseIdentificationNumber,
      "inlandModeOfTransportCode" -> exampleTransportMode
    )

    "we are on standard declaration journey" should {

      "redirect to Departure Transport" in {
        withNewCaching(standardCacheModel)
        val result = controller.saveWarehouse(Mode.Normal).apply(postRequest(body))
        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DepartureTransportController.displayPage()
      }

      "update cache after successful bind" in {
        withNewCaching(standardCacheModel)
        val result = controller.saveWarehouse(Mode.Normal).apply(postRequest(body))
        await(result)
        val updatedWarehouse = theCacheModelUpdated.locations.warehouseIdentification.value
        updatedWarehouse.supervisingCustomsOffice.value mustBe exampleCustomsOfficeIdentifier
        updatedWarehouse.identificationType.value mustBe exampleWarehouseIdentificationType
        updatedWarehouse.identificationNumber.value mustBe exampleWarehouseIdentificationNumber
        updatedWarehouse.inlandModeOfTransportCode.value mustBe exampleTransportMode
      }

      "return Bad Request if payload is not compatibile with model" in {
        withNewCaching(standardCacheModel)
        val body = Json.obj("supervisingCustomsOffice" -> "A")
        val result = controller.saveWarehouse(Mode.Normal).apply(postRequest(body))

        status(result) mustBe BAD_REQUEST
      }
    }

    "we are on supplementary declaration journey" should {
      "redirect to Departure Transport" in {
        withNewCaching(suplementaryCacheModel)
        val result = controller.saveWarehouse(Mode.Normal).apply(postRequest(body))
        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DepartureTransportController.displayPage()
      }

      "update cache after successful bind" in {
        withNewCaching(suplementaryCacheModel)
        val result = controller.saveWarehouse(Mode.Normal).apply(postRequest(body))
        await(result)
        val updatedWarehouse = theCacheModelUpdated.locations.warehouseIdentification.value
        updatedWarehouse.supervisingCustomsOffice.value mustBe exampleCustomsOfficeIdentifier
        updatedWarehouse.identificationType.value mustBe exampleWarehouseIdentificationType
        updatedWarehouse.identificationNumber.value mustBe exampleWarehouseIdentificationNumber
        updatedWarehouse.inlandModeOfTransportCode.value mustBe exampleTransportMode
      }

      "return Bad Request if payload is not compatible with model" in {
        withNewCaching(suplementaryCacheModel)
        val body = Json.obj("supervisingCustomsOffice" -> "A")
        val result = controller.saveWarehouse(Mode.Normal).apply(postRequest(body))

        status(result) mustBe BAD_REQUEST
      }
    }

    "we are on simplified declaration journey" should {
      "redirect to Border Transport" in {
        withNewCaching(simplifiedCacheModel)
        val result = controller.saveWarehouse(Mode.Normal).apply(postRequest(body))
        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.BorderTransportController.displayPage()
      }

      "update cache after successful bind" in {
        withNewCaching(simplifiedCacheModel)
        val result = controller.saveWarehouse(Mode.Normal).apply(postRequest(body))
        await(result)
        val updatedWarehouse = theCacheModelUpdated.locations.warehouseIdentification.value
        updatedWarehouse.supervisingCustomsOffice.value mustBe exampleCustomsOfficeIdentifier
        updatedWarehouse.identificationType.value mustBe exampleWarehouseIdentificationType
        updatedWarehouse.identificationNumber.value mustBe exampleWarehouseIdentificationNumber
        updatedWarehouse.inlandModeOfTransportCode.value mustBe exampleTransportMode
      }

      "return Bad Request if payload is not compatible with model" in {
        withNewCaching(simplifiedCacheModel)
        val body = Json.obj("supervisingCustomsOffice" -> "A")
        val result = controller.saveWarehouse(Mode.Normal).apply(postRequest(body))

        status(result) mustBe BAD_REQUEST
      }
    }

  }

}
