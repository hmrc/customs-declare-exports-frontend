/*
 * Copyright 2021 HM Revenue & Customs
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

import base.ControllerSpec
import controllers.declaration.routes.{
  DepartureTransportController,
  ExpressConsignmentController,
  InlandOrBorderController,
  InlandTransportDetailsController
}
import forms.declaration.SupervisingCustomsOffice
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.{SUPPLEMENTARY_EIDR, SUPPLEMENTARY_SIMPLIFIED}
import models.{DeclarationType, Mode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.supervising_customs_office

class SupervisingCustomsOfficeControllerSpec extends ControllerSpec with BeforeAndAfterEach with OptionValues {

  private val supervisingCustomsOfficeTemplate: supervising_customs_office = mock[supervising_customs_office]

  private val controller = new SupervisingCustomsOfficeController(
    authenticate = mockAuthAction,
    journeyType = mockJourneyAction,
    navigator = navigator,
    exportsCacheService = mockExportsCacheService,
    mcc = stubMessagesControllerComponents(),
    supervisingCustomsOfficePage = supervisingCustomsOfficeTemplate
  )

  private val exampleCustomsOfficeIdentifier = "A1B2C3D4"
  private val exampleWarehouseIdentificationNumber = "SecretStash"

  private val standardCacheModel = aDeclaration(withType(DeclarationType.STANDARD))

  private val supplementaryCacheModel = aDeclaration(withType(DeclarationType.SUPPLEMENTARY))

  private val simplifiedCacheModel = aDeclaration(withType(DeclarationType.SIMPLIFIED))

  private val clearanceCacheModel = aDeclaration(withType(DeclarationType.CLEARANCE))

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(supervisingCustomsOfficeTemplate.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    Mockito.reset(supervisingCustomsOfficeTemplate)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
  }

  def theResponseForm: Form[SupervisingCustomsOffice] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[SupervisingCustomsOffice]])
    verify(supervisingCustomsOfficeTemplate).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  "Supervising Customs Office Controller on GET request" should {

    "return 200 OK" in {
      withNewCaching(supplementaryCacheModel)

      val response = controller.displayPage(Mode.Normal).apply(getRequest())

      status(response) must be(OK)
    }

    "read item from cache and display it" in {
      withNewCaching(supplementaryCacheModel)

      await(controller.displayPage(Mode.Normal)(getRequest()))

      verify(mockExportsCacheService).get(any())(any())
      verify(supervisingCustomsOfficeTemplate).apply(any(), any())(any(), any())
    }
  }
  "Supervising Customs Office Controller on POST" when {

    val body = Json.obj("supervisingCustomsOffice" -> exampleCustomsOfficeIdentifier, "identificationNumber" -> exampleWarehouseIdentificationNumber)

    "we are on standard declaration journey" should {

      "redirect to the 'Inland or Border' page" in {
        withNewCaching(standardCacheModel)

        val result = await(controller.submit(Mode.Normal)(postRequest(body)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe InlandOrBorderController.displayPage()
      }

      "update cache after successful bind" in {
        withNewCaching(standardCacheModel)

        await(controller.submit(Mode.Normal)(postRequest(body)))

        val supervisingCustomsOffice = theCacheModelUpdated.locations.supervisingCustomsOffice.value
        supervisingCustomsOffice.supervisingCustomsOffice.value mustBe exampleCustomsOfficeIdentifier
      }

      "return Bad Request if payload is not compatible with model" in {
        withNewCaching(standardCacheModel)

        val body = Json.obj("supervisingCustomsOffice" -> "A")
        val result = controller.submit(Mode.Normal)(postRequest(body))

        status(result) mustBe BAD_REQUEST
      }
    }

    "we are on supplementary declaration journey" should {

      "redirect to the 'Inland or Border' page" in {
        withNewCaching(withRequest(SUPPLEMENTARY_SIMPLIFIED).cacheModel)

        val result = await(controller.submit(Mode.Normal)(postRequest(body)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe InlandOrBorderController.displayPage()
      }

      "redirect to the 'Inland Transport' page" in {
        withNewCaching(withRequest(SUPPLEMENTARY_EIDR).cacheModel)

        val result = await(controller.submit(Mode.Normal)(postRequest(body)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe InlandTransportDetailsController.displayPage()
      }

      "update cache after successful bind" in {
        withNewCaching(supplementaryCacheModel)

        await(controller.submit(Mode.Normal)(postRequest(body)))

        val supervisingCustomsOffice = theCacheModelUpdated.locations.supervisingCustomsOffice.value
        supervisingCustomsOffice.supervisingCustomsOffice.value mustBe exampleCustomsOfficeIdentifier
      }

      "return Bad Request if payload is not compatible with model" in {
        withNewCaching(supplementaryCacheModel)

        val body = Json.obj("supervisingCustomsOffice" -> "A")
        val result = controller.submit(Mode.Normal)(postRequest(body))

        status(result) mustBe BAD_REQUEST
      }
    }

    "we are on simplified declaration journey" should {

      "redirect to the 'Express Consignment' page" in {
        withNewCaching(simplifiedCacheModel)

        val result = await(controller.submit(Mode.Normal).apply(postRequest(body)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe ExpressConsignmentController.displayPage()
      }

      "update cache after successful bind" in {
        withNewCaching(simplifiedCacheModel)

        await(controller.submit(Mode.Normal)(postRequest(body)))

        val supervisingCustomsOffice = theCacheModelUpdated.locations.supervisingCustomsOffice.value
        supervisingCustomsOffice.supervisingCustomsOffice.value mustBe exampleCustomsOfficeIdentifier
      }

      "return Bad Request if payload is not compatible with model" in {
        withNewCaching(simplifiedCacheModel)

        val body = Json.obj("supervisingCustomsOffice" -> "A")
        val result = controller.submit(Mode.Normal)(postRequest(body))

        status(result) mustBe BAD_REQUEST
      }
    }
    "we are on clearance declaration journey" should {

      "redirect to Departure Transport" in {
        withNewCaching(clearanceCacheModel)

        val result = await(controller.submit(Mode.Normal)(postRequest(body)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe DepartureTransportController.displayPage()
      }

      "update cache after successful bind" in {
        withNewCaching(clearanceCacheModel)

        await(controller.submit(Mode.Normal)(postRequest(body)))

        val supervisingCustomsOffice = theCacheModelUpdated.locations.supervisingCustomsOffice.value
        supervisingCustomsOffice.supervisingCustomsOffice.value mustBe exampleCustomsOfficeIdentifier
      }

      "return Bad Request if payload is not compatible with model" in {
        withNewCaching(clearanceCacheModel)

        val body = Json.obj("supervisingCustomsOffice" -> "A")
        val result = controller.submit(Mode.Normal)(postRequest(body))

        status(result) mustBe BAD_REQUEST
      }
    }
  }
}
