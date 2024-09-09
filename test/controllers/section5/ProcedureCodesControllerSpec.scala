/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.section5

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.section5.routes.AdditionalProcedureCodesController
import forms.section5.procedurecodes.ProcedureCode
import forms.section5.procedurecodes.ProcedureCode.procedureCodeKey
import forms.section6.WarehouseIdentification
import models.DeclarationType._
import models.declaration.ProcedureCodesData.{osrProcedureCode, warehouseRequiredProcedureCodes}
import models.declaration.{ExportItem, ProcedureCodesData}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section5.procedureCodes.procedure_codes
import views.section5.PackageInformationViewSpec

class ProcedureCodesControllerSpec extends ControllerSpec with AuditedControllerSpec with OptionValues with ScalaFutures {

  private val procedureCodesPage = mock[procedure_codes]

  val controller =
    new ProcedureCodesController(mockAuthAction, mockJourneyAction, navigator, mockExportsCacheService, mcc, procedureCodesPage)(ec, auditService)

  val itemId = "itemId12345"

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
    theResponseForm
  }

  def theResponseForm: Form[ProcedureCode] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ProcedureCode]])
    verify(procedureCodesPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  "ProcedureCodesController on displayOutcomePage" should {

    "return 200 (OK)" when {
      onEveryDeclarationJourney() { request =>
        "display page method is invoked with empty cache" in {
          withNewCaching(aDeclaration(withType(request.declarationType), withItem(ExportItem(itemId))))

          val result = controller.displayPage(itemId)(getRequest())

          status(result) mustBe OK
          verify(procedureCodesPage).apply(any(), any())(any(), any())

          theResponseForm.value mustBe empty
        }

        "display page method is invoked with data in cache" in {
          val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq.empty)))
          withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))

          val result = controller.displayPage(itemId)(getRequest())

          status(result) mustBe OK
          verify(procedureCodesPage).apply(any(), any())(any(), any())

          theResponseForm.value.value.procedureCode mustBe "1234"
        }
      }
    }
  }

  "ProcedureCodesController on submitProcedureCodes" when {
    val packageInformation = PackageInformationViewSpec.packageInformation
    val warehouseIdentification = WarehouseIdentification(Some("WarehouseId"))

    onEveryDeclarationJourney() { request =>
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
              withItem(anItem(withItemId(itemId), withFiscalInformation(), withAdditionalFiscalReferenceData()))
            )
          )
          val correctForm = Seq(("procedureCode", osrProcedureCode))

          controller.submitProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

          val updatedItem = theCacheModelUpdated.itemBy(itemId)
          updatedItem.flatMap(_.fiscalInformation).value mustBe fiscalInformation
          updatedItem.flatMap(_.additionalFiscalReferencesData).value mustBe fiscalReferences
          verifyAudit()
        }
      }

      "provided with non-'1042' code" should {
        "remove FiscalInformation and AdditionalFiscalReferences from cache" in {
          withNewCaching(
            aDeclaration(
              withType(request.declarationType),
              withItem(anItem(withItemId(itemId), withFiscalInformation(), withAdditionalFiscalReferenceData()))
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
          val item = anItem(withItemId(itemId), withPackageInformation(packageInformation))
          withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))
          val correctForm = Seq(("procedureCode", "0019"))

          controller.submitProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

          val updatedItem = theCacheModelUpdated.itemBy(itemId)
          updatedItem.flatMap(_.packageInformation) mustBe Some(Seq(PackageInformationViewSpec.packageInformation))
          verifyAudit()
        }
      }

      onClearance { request =>
        "remove PackageInformation from cache" in {
          val item = anItem(withItemId(itemId), withPackageInformation(packageInformation))
          withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))
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

  "ProcedureCodesController" should {

    "return 400 (BAD_REQUEST)" when {

      "no value is entered" in {
        withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId)))))

        val incorrectForm = Json.obj(fieldIdOnError(procedureCodeKey) -> "")
        val result = controller.submitProcedureCodes(itemId)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()

        theResponseForm.errors.head.messages.head mustBe "declaration.procedureCodes.error.empty"
      }

      "the entered value is incorrect or not a list's option" in {
        withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId)))))

        val incorrectForm = Json.obj(fieldIdOnError(procedureCodeKey) -> "!@#$")
        val result = controller.submitProcedureCodes(itemId)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()

        theResponseForm.errors.head.messages.head mustBe "declaration.procedureCodes.error.invalid"
      }
    }
  }
}
