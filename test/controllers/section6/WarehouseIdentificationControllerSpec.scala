/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.section6

import base.ExportsTestData.modifierForPC1040
import base.{AuditedControllerSpec, ControllerSpec}
import controllers.helpers.SupervisingCustomsOfficeHelper
import controllers.helpers.TransportSectionHelper.additionalDeclTypesAllowedOnInlandOrBorder
import controllers.section6.routes.{InlandOrBorderController, InlandTransportDetailsController, SupervisingCustomsOfficeController}
import forms.section1.AdditionalDeclarationType._
import forms.section6.InlandOrBorder.{Border, Inland}
import forms.section6.WarehouseIdentification
import models.DeclarationType._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section6.{warehouse_identification, warehouse_identification_yesno}

class WarehouseIdentificationControllerSpec extends ControllerSpec with AuditedControllerSpec {

  private val pageYesNo = mock[warehouse_identification_yesno]
  private val pageIdentification = mock[warehouse_identification]

  private val supervisingCustomsOfficeHelper = instanceOf[SupervisingCustomsOfficeHelper]

  val controller = new WarehouseIdentificationController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    mockExportsCacheService,
    mcc,
    pageYesNo,
    pageIdentification,
    supervisingCustomsOfficeHelper
  )(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aStandardDeclaration)
    when(pageYesNo.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(pageIdentification.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(pageYesNo, pageIdentification)
  }

  def theResponseForm: Form[WarehouseIdentification] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[WarehouseIdentification]])
    verify(pageIdentification).apply(captor.capture())(any(), any())
    captor.getValue
  }

  def theResponseFormYesNo: Form[WarehouseIdentification] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[WarehouseIdentification]])
    verify(pageYesNo).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  private val identification = WarehouseIdentification(Some("R12345678GB"))

  "Warehouse Identification Controller" should {

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {
          val result = controller.displayPage(getRequest())
          status(result) must be(OK)
        }

        "display page method is invoked and cache is not empty" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withWarehouseIdentification(Some(identification))))

          val result = controller.displayPage(getRequest())

          status(result) must be(OK)
          theResponseForm.value mustBe Some(identification)
        }
      }

      "return 400 (BAD_REQUEST) on submit" when {
        "form contains incorrect values" in {
          withNewCaching(request.cacheModel)

          val incorrectForm = Json.obj(WarehouseIdentification.warehouseIdKey -> "incorrect")
          val result = controller.saveIdentificationNumber()(postRequest(incorrectForm))

          status(result) must be(BAD_REQUEST)
          verifyNoAudit()
        }
      }

      "return 303 (SEE_OTHER) on submit" when {
        "form contains valid response" in {
          withNewCaching(request.cacheModel)
          val correctForm = Json.obj(WarehouseIdentification.warehouseIdKey -> "R12341234")

          val result = controller.saveIdentificationNumber()(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe SupervisingCustomsOfficeController.displayPage
          verifyAudit()
        }
      }
    }

    "redirect to the 'Inland Transport Details' page" when {
      "AdditionalDeclarationType is SUPPLEMENTARY_EIDR and" when {
        "all declaration's items have '1040' as Procedure code and '000' as unique Additional Procedure code and" in {
          withNewCaching(withRequest(SUPPLEMENTARY_EIDR, modifierForPC1040).cacheModel)
          val correctForm = Json.obj(WarehouseIdentification.warehouseIdKey -> "R12341234")

          val result = controller.saveIdentificationNumber()(postRequest(correctForm))

          status(result) mustBe SEE_OTHER

          thePageNavigatedTo mustBe InlandTransportDetailsController.displayPage
          verifyAudit()
        }
      }
    }

    "redirect to the 'Inland or Border' page" when {
      "all declaration's items have '1040' as Procedure code and '000' as unique Additional Procedure code and" when {
        additionalDeclTypesAllowedOnInlandOrBorder.foreach { additionalType =>
          s"AdditionalDeclarationType is $additionalType" in {
            withNewCaching(withRequest(additionalType, modifierForPC1040).cacheModel)
            val correctForm = Json.obj(WarehouseIdentification.warehouseIdKey -> "R12341234")

            val result = controller.saveIdentificationNumber()(postRequest(correctForm))

            status(result) mustBe SEE_OTHER
            thePageNavigatedTo mustBe InlandOrBorderController.displayPage
            verifyAudit()
          }
        }
      }
    }

    onJourney(CLEARANCE) { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {
          val result = controller.displayPage(getRequest())
          status(result) must be(OK)
        }

        "display page method is invoked and cache is not empty" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withWarehouseIdentification(Some(identification))))

          val result = controller.displayPage(getRequest())

          status(result) must be(OK)
          theResponseFormYesNo.value mustBe Some(identification)
        }
      }

      "return 400 (BAD_REQUEST)" when {
        "form contains incorrect values" in {
          withNewCaching(request.cacheModel)

          val incorrectForm = Json.obj(WarehouseIdentification.inWarehouseKey -> "Yes", WarehouseIdentification.warehouseIdKey -> "incorrect")

          val result = controller.saveIdentificationNumber()(postRequest(incorrectForm))

          status(result) must be(BAD_REQUEST)
          verifyNoAudit()
        }
      }

      "return 303 (SEE_OTHER)" when {
        "form contains valid response" in {
          withNewCaching(request.cacheModel)

          val correctForm = Json.obj(WarehouseIdentification.inWarehouseKey -> "Yes", WarehouseIdentification.warehouseIdKey -> "R12341234")

          val result = controller.saveIdentificationNumber()(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe SupervisingCustomsOfficeController.displayPage
          verifyAudit()
        }
      }

      "display SupervisingCustomsOffice page on submit" when {
        "declaration is EIDR and all declaration's items have '1040' as PC and '000' as unique APC" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withEntryIntoDeclarantsRecords(), modifierForPC1040))

          val correctForm = Json.obj(WarehouseIdentification.inWarehouseKey -> "Yes", WarehouseIdentification.warehouseIdKey -> "R12341234")

          val result = controller.saveIdentificationNumber()(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe SupervisingCustomsOfficeController.displayPage
          verifyAudit()
        }
      }
    }

    val bodyNonClearance = Json.obj(WarehouseIdentification.warehouseIdKey -> "R12341234")
    val bodyForClearance = Json.obj(WarehouseIdentification.inWarehouseKey -> "No", WarehouseIdentification.warehouseIdKey -> None)

    val resetInlandOrBorderConditions = List(
      (STANDARD_FRONTIER, Border, bodyNonClearance),
      (STANDARD_PRE_LODGED, Border, bodyNonClearance),
      (SUPPLEMENTARY_SIMPLIFIED, Inland, bodyNonClearance),
      (SUPPLEMENTARY_EIDR, Border, bodyNonClearance),
      (SIMPLIFIED_FRONTIER, Border, bodyNonClearance),
      (SIMPLIFIED_PRE_LODGED, Border, bodyNonClearance),
      (OCCASIONAL_FRONTIER, Border, bodyNonClearance),
      (OCCASIONAL_PRE_LODGED, Border, bodyNonClearance),
      (CLEARANCE_FRONTIER, Border, bodyForClearance),
      (CLEARANCE_PRE_LODGED, Inland, bodyForClearance)
    )

    resetInlandOrBorderConditions.foreach { data =>
      val additionalType = data._1
      val actualCachedInlandOrBorder = data._2
      val body = data._3

      s"AdditionalDeclarationType is $additionalType and" when {
        "the cached InlandOrBorder is NOT None" should {
          "reset InlandOrBorder after a successful bind" in {
            val inlandOrBorder = withInlandOrBorder(Some(actualCachedInlandOrBorder))
            withNewCaching(withRequest(additionalType, inlandOrBorder).cacheModel)

            await(controller.saveIdentificationNumber()(postRequest(body)))

            theCacheModelUpdated.locations.inlandOrBorder mustBe None
          }
        }
      }
    }
  }
}
