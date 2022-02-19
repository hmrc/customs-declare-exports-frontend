/*
 * Copyright 2022 HM Revenue & Customs
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
import base.ExportsTestData.allValuesRequiringToSkipInlandOrBorder
import controllers.declaration.routes.{
  DepartureTransportController,
  ExpressConsignmentController,
  InlandOrBorderController,
  InlandTransportDetailsController
}
import controllers.helpers.{InlandOrBorderHelper, SupervisingCustomsOfficeHelper}
import controllers.helpers.TransportSectionHelper.additionalDeclTypesAllowedOnInlandOrBorder
import forms.declaration.InlandOrBorder.{Border, Inland}
import forms.declaration.ModeOfTransportCode.{FixedTransportInstallations, PostalConsignment}
import forms.declaration.SupervisingCustomsOffice
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import models.DeclarationType
import models.Mode.Normal
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

  private val inlandOrBorderHelper = instanceOf[InlandOrBorderHelper]
  private val supervisingCustomsOfficeHelper = instanceOf[SupervisingCustomsOfficeHelper]
  private val supervisingCustomsOfficeTemplate = mock[supervising_customs_office]

  private val controller = new SupervisingCustomsOfficeController(
    authenticate = mockAuthAction,
    journeyType = mockJourneyAction,
    navigator = navigator,
    exportsCacheService = mockExportsCacheService,
    mcc = stubMessagesControllerComponents(),
    supervisingCustomsOfficePage = supervisingCustomsOfficeTemplate,
    inlandOrBorderHelper = inlandOrBorderHelper,
    supervisingCustomsOfficeHelper = supervisingCustomsOfficeHelper
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
    await(controller.displayPage(Normal)(request))
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

      val response = controller.displayPage(Normal).apply(getRequest())

      status(response) must be(OK)
    }

    "read item from cache and display it" in {
      withNewCaching(supplementaryCacheModel)

      await(controller.displayPage(Normal)(getRequest()))

      verify(mockExportsCacheService).get(any())(any())
      verify(supervisingCustomsOfficeTemplate).apply(any(), any())(any(), any())
    }
  }

  "Supervising Customs Office Controller on POST" when {

    val body = Json.obj("supervisingCustomsOffice" -> exampleCustomsOfficeIdentifier, "identificationNumber" -> exampleWarehouseIdentificationNumber)

    "we are on standard declaration journey" should {

      "redirect to /inland-or-border after a successful bind" in {
        withNewCaching(standardCacheModel)

        val result = await(controller.submit(Normal)(postRequest(body)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe InlandOrBorderController.displayPage()
      }

      "update cache after successful bind" in {
        withNewCaching(standardCacheModel)

        await(controller.submit(Normal)(postRequest(body)))

        val supervisingCustomsOffice = theCacheModelUpdated.locations.supervisingCustomsOffice.value
        supervisingCustomsOffice.supervisingCustomsOffice.value mustBe exampleCustomsOfficeIdentifier
      }

      "return Bad Request if payload is not compatible with model" in {
        withNewCaching(standardCacheModel)

        val body = Json.obj("supervisingCustomsOffice" -> "A")
        val result = controller.submit(Normal)(postRequest(body))

        status(result) mustBe BAD_REQUEST
      }
    }

    "we are on supplementary declaration journey" should {

      "redirect to /inland-or-border after a successful bind" in {
        withNewCaching(withRequest(SUPPLEMENTARY_SIMPLIFIED).cacheModel)

        val result = await(controller.submit(Normal)(postRequest(body)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe InlandOrBorderController.displayPage()
      }

      "redirect to /inland-transport-details after a successful bind" in {
        withNewCaching(withRequest(SUPPLEMENTARY_EIDR).cacheModel)

        val result = await(controller.submit(Normal)(postRequest(body)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe InlandTransportDetailsController.displayPage()
      }

      "update cache after successful bind" in {
        withNewCaching(supplementaryCacheModel)

        await(controller.submit(Normal)(postRequest(body)))

        val supervisingCustomsOffice = theCacheModelUpdated.locations.supervisingCustomsOffice.value
        supervisingCustomsOffice.supervisingCustomsOffice.value mustBe exampleCustomsOfficeIdentifier
      }

      "return Bad Request if payload is not compatible with model" in {
        withNewCaching(supplementaryCacheModel)

        val body = Json.obj("supervisingCustomsOffice" -> "A")
        val result = controller.submit(Normal)(postRequest(body))

        status(result) mustBe BAD_REQUEST
      }
    }

    additionalDeclTypesAllowedOnInlandOrBorder.foreach { additionalType =>
      s"AdditionalDeclarationType is $additionalType" should {
        "redirect to /inland-transport-details after a successful bind" when {
          "the user has previously entered a value which requires to skip the /inland-or-border page" in {
            allValuesRequiringToSkipInlandOrBorder.foreach { modifier =>
              initMockNavigatorForMultipleCallsInTheSameTest
              withNewCaching(withRequest(additionalType, modifier).cacheModel)

              val result = controller.submit(Normal)(postRequest(body))

              await(result) mustBe aRedirectToTheNextPage
              thePageNavigatedTo mustBe InlandTransportDetailsController.displayPage()
            }
          }
        }
      }
    }

    "we are on simplified declaration journey" should {

      "redirect to /express-consignment after a successful bind" in {
        withNewCaching(simplifiedCacheModel)

        val result = await(controller.submit(Normal).apply(postRequest(body)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe ExpressConsignmentController.displayPage()
      }

      "update cache after successful bind" in {
        withNewCaching(simplifiedCacheModel)

        await(controller.submit(Normal)(postRequest(body)))

        val supervisingCustomsOffice = theCacheModelUpdated.locations.supervisingCustomsOffice.value
        supervisingCustomsOffice.supervisingCustomsOffice.value mustBe exampleCustomsOfficeIdentifier
      }

      "return Bad Request if payload is not compatible with model" in {
        withNewCaching(simplifiedCacheModel)

        val body = Json.obj("supervisingCustomsOffice" -> "A")
        val result = controller.submit(Normal)(postRequest(body))

        status(result) mustBe BAD_REQUEST
      }
    }

    "we are on clearance declaration journey" should {

      "redirect to /departure-transport after a successful bind" in {
        withNewCaching(clearanceCacheModel)

        val result = await(controller.submit(Normal)(postRequest(body)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe DepartureTransportController.displayPage()
      }

      "redirect to /express-consignment after a successful bind" when {
        List(FixedTransportInstallations, PostalConsignment).foreach { modeOfTransport =>
          s"'$modeOfTransport' has been selected on /transport-leaving-the-border" in {
            val borderModeOfTransportCode = withBorderModeOfTransportCode(Some(modeOfTransport))
            withNewCaching(aDeclaration(withType(DeclarationType.CLEARANCE), borderModeOfTransportCode))

            val result = await(controller.submit(Normal)(postRequest(body)))

            result mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe ExpressConsignmentController.displayPage()
          }
        }
      }

      "update cache after successful bind" in {
        withNewCaching(clearanceCacheModel)

        await(controller.submit(Normal)(postRequest(body)))

        val supervisingCustomsOffice = theCacheModelUpdated.locations.supervisingCustomsOffice.value
        supervisingCustomsOffice.supervisingCustomsOffice.value mustBe exampleCustomsOfficeIdentifier
      }

      "return Bad Request if payload is not compatible with model" in {
        withNewCaching(clearanceCacheModel)

        val body = Json.obj("supervisingCustomsOffice" -> "A")
        val result = controller.submit(Normal)(postRequest(body))

        status(result) mustBe BAD_REQUEST
      }
    }

    val inlandOrBorderIfOrNotToReset = List(
      (STANDARD_FRONTIER, Border, Some(Border)),
      (STANDARD_PRE_LODGED, Border, Some(Border)),
      (SUPPLEMENTARY_SIMPLIFIED, Inland, Some(Inland)),
      (SUPPLEMENTARY_EIDR, Border, None),
      (SIMPLIFIED_FRONTIER, Border, None),
      (SIMPLIFIED_PRE_LODGED, Border, None),
      (OCCASIONAL_FRONTIER, Border, None),
      (OCCASIONAL_PRE_LODGED, Border, None),
      (CLEARANCE_FRONTIER, Border, None),
      (CLEARANCE_PRE_LODGED, Inland, None)
    )

    inlandOrBorderIfOrNotToReset.foreach { data =>
      val additionalType = data._1
      val actualCachedInlandOrBorder = data._2
      val expectedCachedInlandOrBorder = data._3

      s"AdditionalDeclarationType is $additionalType and" when {
        s"the cached InlandOrBorder is $actualCachedInlandOrBorder" should {
          s"${if (expectedCachedInlandOrBorder.isEmpty) "" else "not "} reset InlandOrBorder after a successful bind" in {
            withNewCaching(withRequest(additionalType, withInlandOrBorder(Some(actualCachedInlandOrBorder))).cacheModel)

            await(controller.submit(Normal)(postRequest(body)))

            theCacheModelUpdated.locations.inlandOrBorder mustBe expectedCachedInlandOrBorder
          }
        }
      }
    }
  }
}
