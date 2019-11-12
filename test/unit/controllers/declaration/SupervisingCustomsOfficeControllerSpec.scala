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

import controllers.declaration.SupervisingCustomsOfficeController
import helpers.views.declaration.WarehouseIdentificationMessages
import models.{DeclarationType, Mode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{verify, when}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.supervising_customs_office

class SupervisingCustomsOfficeControllerSpec extends ControllerSpec with BeforeAndAfterEach with WarehouseIdentificationMessages with OptionValues {

  val supervisingCustomsOfficeTemplate: supervising_customs_office = mock[supervising_customs_office]

  val controller = new SupervisingCustomsOfficeController(
    authenticate = mockAuthAction,
    journeyType = mockJourneyAction,
    navigator = navigator,
    exportsCacheService = mockExportsCacheService,
    mcc = stubMessagesControllerComponents(),
    supervisingCustomsOfficePage = supervisingCustomsOfficeTemplate
  )

  val exampleCustomsOfficeIdentifier = "A1B2C3D4"
  val exampleWarehouseIdentificationNumber = "SecretStash"

  private val standardCacheModel = aDeclaration(
    withType(DeclarationType.STANDARD),
    withWarehouseIdentification(Some(exampleCustomsOfficeIdentifier), None, Some(exampleWarehouseIdentificationNumber), None)
  )

  private val supplementaryCacheModel = aDeclaration(
    withType(DeclarationType.SUPPLEMENTARY),
    withWarehouseIdentification(Some(exampleCustomsOfficeIdentifier), None, Some(exampleWarehouseIdentificationNumber), None)
  )

  private val simplifiedCacheModel = aDeclaration(
    withType(DeclarationType.SIMPLIFIED),
    withWarehouseIdentification(Some(exampleCustomsOfficeIdentifier), None, Some(exampleWarehouseIdentificationNumber), None)
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(supervisingCustomsOfficeTemplate.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    Mockito.reset(supervisingCustomsOfficeTemplate)
    super.afterEach()
  }

  "Supervising Customs Office Controller on GET request" should {

    "return 200 OK" in {
      withNewCaching(supplementaryCacheModel)

      val response = controller.displayPage(Mode.Normal).apply(getRequest())

      status(response) must be(OK)
    }

    "read item from cache and display it" in {
      withNewCaching(supplementaryCacheModel)

      val result = controller.displayPage(Mode.Normal).apply(getRequest())
      await(result)

      verify(mockExportsCacheService).get(any())(any())
      verify(supervisingCustomsOfficeTemplate).apply(any(), any())(any(), any())
    }
  }
  "Supervising Customs Office Controller on POST" when {

    val body = Json.obj("supervisingCustomsOffice" -> exampleCustomsOfficeIdentifier, "identificationNumber" -> exampleWarehouseIdentificationNumber)

    "we are on standard declaration journey" should {

      "redirect to Warehouse Details" in {
        withNewCaching(standardCacheModel)

        val result = controller.submit(Mode.Normal).apply(postRequest(body))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.WarehouseDetailsController.displayPage()
      }

      "update cache after successful bind" in {
        withNewCaching(standardCacheModel)

        val result = controller.submit(Mode.Normal).apply(postRequest(body))
        await(result)

        val updatedWarehouse = theCacheModelUpdated.locations.warehouseIdentification.value

        updatedWarehouse.supervisingCustomsOffice.value mustBe exampleCustomsOfficeIdentifier
        updatedWarehouse.identificationNumber.value mustBe exampleWarehouseIdentificationNumber
      }

      "return Bad Request if payload is not compatible with model" in {
        withNewCaching(standardCacheModel)

        val body = Json.obj("supervisingCustomsOffice" -> "A")
        val result = controller.submit(Mode.Normal).apply(postRequest(body))

        status(result) mustBe BAD_REQUEST
      }
    }

    "we are on supplementary declaration journey" should {

      "redirect to Warehouse Details" in {
        withNewCaching(supplementaryCacheModel)

        val result = controller.submit(Mode.Normal).apply(postRequest(body))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.WarehouseDetailsController.displayPage()
      }

      "update cache after successful bind" in {
        withNewCaching(supplementaryCacheModel)

        val result = controller.submit(Mode.Normal).apply(postRequest(body))
        await(result)

        val updatedWarehouse = theCacheModelUpdated.locations.warehouseIdentification.value

        updatedWarehouse.supervisingCustomsOffice.value mustBe exampleCustomsOfficeIdentifier
        updatedWarehouse.identificationNumber.value mustBe exampleWarehouseIdentificationNumber
      }

      "return Bad Request if payload is not compatible with model" in {
        withNewCaching(supplementaryCacheModel)

        val body = Json.obj("supervisingCustomsOffice" -> "A")
        val result = controller.submit(Mode.Normal).apply(postRequest(body))

        status(result) mustBe BAD_REQUEST
      }
    }

    "we are on simplified declaration journey" should {

      "redirect to Warehouse Details" in {
        withNewCaching(simplifiedCacheModel)

        val result = controller.submit(Mode.Normal).apply(postRequest(body))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.WarehouseDetailsController.displayPage()
      }

      "update cache after successful bind" in {
        withNewCaching(simplifiedCacheModel)

        val result = controller.submit(Mode.Normal).apply(postRequest(body))
        await(result)

        val updatedWarehouse = theCacheModelUpdated.locations.warehouseIdentification.value

        updatedWarehouse.supervisingCustomsOffice.value mustBe exampleCustomsOfficeIdentifier
        updatedWarehouse.identificationNumber.value mustBe exampleWarehouseIdentificationNumber
      }

      "return Bad Request if payload is not compatible with model" in {
        withNewCaching(simplifiedCacheModel)

        val body = Json.obj("supervisingCustomsOffice" -> "A")
        val result = controller.submit(Mode.Normal).apply(postRequest(body))

        status(result) mustBe BAD_REQUEST
      }
    }

  }

}
