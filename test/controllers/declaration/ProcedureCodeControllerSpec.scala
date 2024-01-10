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

package controllers.declaration

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.declaration.routes.AdditionalProcedureCodesController
import forms.declaration._
import forms.declaration.procedurecodes.ProcedureCode
import mock.ErrorHandlerMocks
import models.DeclarationType._
import models.declaration.ProcedureCodesData.warehouseRequiredProcedureCodes
import models.declaration.{ExportItem, ProcedureCodesData}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.declaration.PackageInformationViewSpec
import views.html.declaration.procedureCodes.procedure_codes

class ProcedureCodeControllerSpec extends ControllerSpec with AuditedControllerSpec with ErrorHandlerMocks with OptionValues with ScalaFutures {

  private val procedureCodesPage = mock[procedure_codes]

  val controller = new ProcedureCodesController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    mockExportsCacheService,
    stubMessagesControllerComponents(),
    procedureCodesPage
  )(ec, auditService)

  val itemId = "itemId12345"

  def templateParameters: Form[ProcedureCode] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[ProcedureCode]])
    verify(procedureCodesPage).apply(any(), formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(procedureCodesPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(procedureCodesPage, auditService)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(itemId)(request))
    templateParameters
  }

  "ProcedureCodesController on displayOutcomePage" should {

    "return 200 (OK)" when {
      onEveryDeclarationJourney() { request =>
        "display page method is invoked with empty cache" in {
          withNewCaching(aDeclaration(withType(request.declarationType), withItem(ExportItem(itemId))))

          val result = controller.displayPage(itemId)(getRequest())

          status(result) mustBe OK
          verify(procedureCodesPage).apply(any(), any())(any(), any())

          val responseForm = templateParameters
          responseForm.value mustBe empty
        }

        "display page method is invoked with data in cache" in {
          val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq.empty)))
          withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))

          val result = controller.displayPage(itemId)(getRequest())

          status(result) mustBe OK
          verify(procedureCodesPage).apply(any(), any())(any(), any())

          val responseForm = templateParameters
          responseForm.value.value.procedureCode mustBe "1234"
        }
      }
    }
  }

  "ProcedureCodesController on submitProcedureCodes" when {

    val fiscalInformation = FiscalInformation("Yes")
    val fiscalReferencesData = AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("GB", "12")))
    val packageInformation = PackageInformationViewSpec.packageInformation
    val warehouseIdentification = WarehouseIdentification(Some("WarehouseId"))

    onEveryDeclarationJourney() { request =>
      "provided with incorrect code" should {
        "return 400 (BAD_REQUEST)" in {
          withNewCaching(aDeclaration(withType(request.declarationType)))

          val incorrectForm = Seq(("procedureCode", "12345"))

          val result = controller.submitProcedureCodes(itemId)(postRequestAsFormUrlEncoded(incorrectForm: _*))

          status(result) mustBe BAD_REQUEST
          verify(procedureCodesPage).apply(any(), any())(any(), any())
          verifyNoAudit()
        }
      }

      "provided with empty code" should {
        "return 400 (BAD_REQUEST)" in {
          withNewCaching(aDeclaration(withType(request.declarationType)))

          val incorrectForm = Seq(("procedureCode", ""))

          val result = controller.submitProcedureCodes(itemId)(postRequestAsFormUrlEncoded(incorrectForm: _*))

          status(result) mustBe BAD_REQUEST
          verify(procedureCodesPage).apply(any(), any())(any(), any())
          verifyNoAudit()
        }
      }

      "provided with correct code" should {
        "redirect to AdditionalProcedureCodes page" in {
          withNewCaching(aDeclaration(withType(request.declarationType), withItem(anItem(withItemId(itemId)))))
          val correctForm = Seq(("procedureCode", "1234"))

          val result = controller.submitProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe AdditionalProcedureCodesController.displayPage(itemId)
          verify(procedureCodesPage, never()).apply(any(), any())(any(), any())
          verifyAudit()
        }
      }

      "provided with correct code" when {

        "cache was empty" should {

          "update cache with new Procedure Code" in {
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(anItem(withItemId(itemId)))))
            val correctForm = Seq(("procedureCode", "1234"))

            controller.submitProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

            val updatedItem = theCacheModelUpdated.itemBy(itemId)
            updatedItem mustBe defined
            updatedItem.get.procedureCodes mustBe defined
            updatedItem.get.procedureCodes.get.procedureCode mustBe defined
            updatedItem.get.procedureCodes.get.procedureCode.get mustBe "1234"
            verifyAudit()
          }

          "NOT remove Additional Procedure Codes from the cache" in {
            val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(None, Seq("123", "456"))))
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))
            val correctForm = Seq(("procedureCode", "1234"))

            controller.submitProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

            val updatedItem = theCacheModelUpdated.itemBy(itemId)
            updatedItem mustBe defined
            updatedItem.get.procedureCodes mustBe defined
            updatedItem.get.procedureCodes.get.additionalProcedureCodes mustBe Seq("123", "456")
            verifyAudit()
          }
        }

        "cache was NOT empty" should {

          "update cache with new Procedure Code" in {
            val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq.empty)))
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))
            val correctForm = Seq(("procedureCode", "5678"))

            controller.submitProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

            val updatedItem = theCacheModelUpdated.itemBy(itemId)
            updatedItem mustBe defined
            updatedItem.get.procedureCodes mustBe defined
            updatedItem.get.procedureCodes.get.procedureCode mustBe defined
            updatedItem.get.procedureCodes.get.procedureCode.get mustBe "5678"
            verifyAudit()
          }

          "remove all Additional Procedure Codes from the cache" in {
            val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq("123", "456"))))
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))
            val correctForm = Seq(("procedureCode", "5678"))

            controller.submitProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

            val updatedItem = theCacheModelUpdated.itemBy(itemId)
            updatedItem mustBe defined
            updatedItem.get.procedureCodes mustBe defined
            updatedItem.get.procedureCodes.get.additionalProcedureCodes mustBe empty
            verifyAudit()
          }

          "NOT remove Additional Procedure Codes from the cache" when {
            "Procedure Code provided is the same as cached one" in {
              val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq("123", "456"))))
              withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))
              val correctForm = Seq(("procedureCode", "1234"))

              controller.submitProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

              val updatedItem = theCacheModelUpdated.itemBy(itemId)
              updatedItem mustBe defined
              updatedItem.get.procedureCodes mustBe defined
              updatedItem.get.procedureCodes.get.additionalProcedureCodes mustBe Seq("123", "456")
              verifyAudit()
            }
          }
        }
      }

      "provided with '1042' code" should {
        "not make changes to FiscalInformation and AdditionalFiscalReferences in cache" in {
          withNewCaching(
            aDeclaration(
              withType(request.declarationType),
              withItem(anItem(withItemId(itemId), withFiscalInformation(fiscalInformation), withAdditionalFiscalReferenceData(fiscalReferencesData)))
            )
          )
          val correctForm = Seq(("procedureCode", "1042"))

          controller.submitProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

          val updatedItem = theCacheModelUpdated.itemBy(itemId)
          updatedItem.flatMap(_.fiscalInformation).value mustBe fiscalInformation
          updatedItem.flatMap(_.additionalFiscalReferencesData).value mustBe fiscalReferencesData
          verifyAudit()
        }
      }

      "provided with non-'1042' code" should {
        "remove FiscalInformation and AdditionalFiscalReferences from cache" in {
          withNewCaching(
            aDeclaration(
              withType(request.declarationType),
              withItem(anItem(withItemId(itemId), withFiscalInformation(fiscalInformation), withAdditionalFiscalReferenceData(fiscalReferencesData)))
            )
          )
          val correctForm = Seq(("procedureCode", "1234"))

          controller.submitProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

          val updatedItem = theCacheModelUpdated.itemBy(itemId)
          updatedItem.flatMap(_.fiscalInformation) mustBe None
          updatedItem.flatMap(_.additionalFiscalReferencesData) mustBe None
          verifyAudit()
        }
      }
    }

    "provided with '0019' code" when {

      onJourney(STANDARD, SIMPLIFIED, SUPPLEMENTARY, OCCASIONAL) { request =>
        "not make changes to PackageInformation in cache" in {
          withNewCaching(
            aDeclaration(withType(request.declarationType), withItem(anItem(withItemId(itemId), withPackageInformation(packageInformation))))
          )
          val correctForm = Seq(("procedureCode", "0019"))

          controller.submitProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

          val updatedItem = theCacheModelUpdated.itemBy(itemId)
          updatedItem.flatMap(_.packageInformation) mustBe Some(Seq(PackageInformationViewSpec.packageInformation))
          verifyAudit()
        }
      }

      onClearance { request =>
        "remove PackageInformation from cache" in {
          withNewCaching(
            aDeclaration(withType(request.declarationType), withItem(anItem(withItemId(itemId), withPackageInformation(packageInformation))))
          )
          val correctForm = Seq(("procedureCode", "0019"))

          controller.submitProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

          val updatedItem = theCacheModelUpdated.itemBy(itemId)
          updatedItem.flatMap(_.packageInformation) mustBe None
          verifyAudit()
        }
      }
    }

    warehouseRequiredProcedureCodes.foreach { suffix =>
      s"user provides procedure code indicating WarehouseIdentifier is required (ends with $suffix)" when {

        onJourney(STANDARD, SIMPLIFIED, SUPPLEMENTARY, OCCASIONAL) { request =>
          "remove WarehouseIdentification from cache" in {
            withNewCaching(
              aDeclaration(
                withType(request.declarationType),
                withItem(anItem(withItemId(itemId))),
                withWarehouseIdentification(Some(warehouseIdentification))
              )
            )
            val correctForm = Seq(("procedureCode", "1234"))

            controller.submitProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

            val updatedLocations = theCacheModelUpdated.locations
            updatedLocations.warehouseIdentification mustBe None
            verifyAudit()
          }
        }

        onClearance { request =>
          "not make changes to WarehouseIdentification in cache" in {

            withNewCaching(
              aDeclaration(
                withType(request.declarationType),
                withItem(anItem(withItemId(itemId))),
                withWarehouseIdentification(Some(warehouseIdentification))
              )
            )
            val correctForm = Seq(("procedureCode", "1234"))

            controller.submitProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

            val updatedLocations = theCacheModelUpdated.locations
            updatedLocations.warehouseIdentification.value mustBe warehouseIdentification
            verifyAudit()
          }
        }
      }
    }
  }
}
