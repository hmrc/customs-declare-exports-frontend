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

package unit.controllers.declaration

import controllers.declaration.ProcedureCodesController
import controllers.util.Remove
import forms.declaration._
import models.declaration.ProcedureCodesData.limitOfCodes
import models.declaration.{ExportItem, ProcedureCodesData}
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.declaration.PackageInformationViewSpec
import views.html.declaration.procedure_codes

class ProcedureCodesControllerSpec extends ControllerSpec with ErrorHandlerMocks with OptionValues {

  private val mockProcedureCodesPage = mock[procedure_codes]

  val controller = new ProcedureCodesController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    mockExportsCacheService,
    stubMessagesControllerComponents(),
    mockProcedureCodesPage
  )(ec)

  val itemId = "itemId12345"

  def theResponse: (Form[ProcedureCodes], Seq[String]) = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[ProcedureCodes]])
    val dataCaptor = ArgumentCaptor.forClass(classOf[Seq[String]])
    verify(mockProcedureCodesPage).apply(any(), any(), formCaptor.capture(), dataCaptor.capture())(any(), any())
    (formCaptor.getValue, dataCaptor.getValue)
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(mockProcedureCodesPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockProcedureCodesPage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal, itemId)(request))
    theResponse._1
  }

  "Procedure Codes controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in {

        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))

        val result = controller.displayPage(Mode.Normal, itemId)(getRequest())

        status(result) mustBe OK
        verify(mockProcedureCodesPage, times(1)).apply(any(), any(), any(), any())(any(), any())

        val (responseForm, responseSeq) = theResponse
        responseForm.value mustBe empty
        responseSeq mustBe empty
      }

      "display page method is invoked with data in cache" in {

        val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq("123"))))
        withNewCaching(aDeclaration(withItem(item)))

        val result = controller.displayPage(Mode.Normal, itemId)(getRequest())

        status(result) mustBe OK
        verify(mockProcedureCodesPage, times(1)).apply(any(), any(), any(), any())(any(), any())

        val (responseForm, responseSeq) = theResponse
        responseForm.value.value.procedureCode.value mustBe "1234"
        responseSeq mustBe Seq("123")
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user provide wrong action" in {

        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))

        val wrongAction = Seq(("procedureCode", "1234"), ("additionalProcedureCode", "123"), ("WrongAction", ""))

        val result = controller.submitProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockProcedureCodesPage, times(1)).apply(any(), any(), any(), any())(any(), any())
      }
    }

    "return 400 (BAD_REQUEST) during adding" when {

      "user put incorrect data" in {

        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))

        val incorrectForm =
          Seq(("procedureCode", "1234"), ("additionalProcedureCode", "incorrect"), addActionUrlEncoded())

        val result =
          controller.submitProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockProcedureCodesPage, times(1)).apply(any(), any(), any(), any())(any(), any())
      }

      "user put duplicated item" in {

        val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq("123"))))
        withNewCaching(aDeclaration(withItem(item)))

        val duplicatedForm =
          Seq(("procedureCode", "1234"), ("additionalProcedureCode", "123"), addActionUrlEncoded())

        val result =
          controller.submitProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockProcedureCodesPage, times(1)).apply(any(), any(), any(), any())(any(), any())
      }

      "user reach maximum amount of items" in {

        val item =
          ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq.fill(limitOfCodes)("123"))))
        withNewCaching(aDeclaration(withItem(item)))

        val correctForm =
          Seq(("procedureCode", "1234"), ("additionalProcedureCode", "321"), addActionUrlEncoded())

        val result = controller.submitProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockProcedureCodesPage, times(1)).apply(any(), any(), any(), any())(any(), any())
      }
    }

    "return 400 (BAD_REQUEST) during saving" when {

      "user put incorrect data" in {

        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))

        val incorrectForm =
          Seq(("procedureCode", "1234"), ("additionalProcedureCode", "incorrect"), saveAndContinueActionUrlEncoded)

        val result =
          controller.submitProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockProcedureCodesPage, times(1)).apply(any(), any(), any(), any())(any(), any())
      }

      "user put duplicated item" in {

        val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq("123"))))
        withNewCaching(aDeclaration(withItem(item)))

        val duplicatedForm =
          Seq(("procedureCode", "1234"), ("additionalProcedureCode", "123"), saveAndContinueActionUrlEncoded)

        val result =
          controller.submitProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockProcedureCodesPage, times(1)).apply(any(), any(), any(), any())(any(), any())
      }

      "user reach maximum amount of items" in {

        val item =
          ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq.fill(limitOfCodes)("123"))))
        withNewCaching(aDeclaration(withItem(item)))

        val correctForm =
          Seq(("procedureCode", "1234"), ("additionalProcedureCode", "321"), saveAndContinueActionUrlEncoded)

        val result = controller.submitProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockProcedureCodesPage, times(1)).apply(any(), any(), any(), any())(any(), any())
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user correctly add new item" in {

        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))

        val correctForm =
          Seq(("procedureCode", "1234"), ("additionalProcedureCode", "321"), addActionUrlEncoded())

        val result = controller.submitProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.ProcedureCodesController
          .displayPage(Mode.Normal, "itemId12345")

        verify(mockProcedureCodesPage, times(0)).apply(any(), any(), any(), any())(any(), any())
      }

      "user save correct data with '1042' procedure code" in {

        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(anItem(withItemId(itemId)))))

        val correctForm =
          Seq(("procedureCode", "1042"), ("additionalProcedureCode", "321"), saveAndContinueActionUrlEncoded)

        val result = controller.submitProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.FiscalInformationController
          .displayPage(Mode.Normal, "itemId12345")

        verify(mockProcedureCodesPage, times(0)).apply(any(), any(), any(), any())(any(), any())
      }

      "user save correct data with non-'1042' procedure code" in {

        withNewCaching(
          aDeclaration(
            withType(DeclarationType.SUPPLEMENTARY),
            withItem(
              anItem(
                withItemId(itemId),
                withFiscalInformation(FiscalInformation("Yes")),
                withAdditionalFiscalReferenceData(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("GB", "12"))))
              )
            )
          )
        )

        val correctForm =
          Seq(("procedureCode", "1234"), ("additionalProcedureCode", "321"), saveAndContinueActionUrlEncoded)

        val result = controller.submitProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.CommodityDetailsController
          .displayPage(Mode.Normal, "itemId12345")

        verify(mockProcedureCodesPage, times(0)).apply(any(), any(), any(), any())(any(), any())

        val updatedItem = theCacheModelUpdated.itemBy(itemId)
        updatedItem.flatMap(_.fiscalInformation) mustBe None
        updatedItem.flatMap(_.additionalInformation) mustBe None
      }

      "user save correct data with '0019' procedure code on standard journey" in {

        withNewCaching(
          aDeclaration(
            withType(DeclarationType.SUPPLEMENTARY),
            withItem(anItem(withItemId(itemId), withPackageInformation(PackageInformationViewSpec.packageInformation)))
          )
        )

        val correctForm =
          Seq(("procedureCode", "0019"), ("additionalProcedureCode", "321"), saveAndContinueActionUrlEncoded)

        val result = controller.submitProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.CommodityDetailsController
          .displayPage(Mode.Normal, "itemId12345")

        verify(mockProcedureCodesPage, times(0)).apply(any(), any(), any(), any())(any(), any())

        val updatedItem = theCacheModelUpdated.itemBy(itemId)
        updatedItem.flatMap(_.packageInformation) mustBe Some(Seq(PackageInformationViewSpec.packageInformation))
      }

      "user save correct data with procedure code indicating warehouse identifier not required" in {

        val warehouseIdentification = WarehouseIdentification(Some("WarehouseId"))
        withNewCaching(
          aDeclaration(
            withType(DeclarationType.SUPPLEMENTARY),
            withItem(anItem(withItemId(itemId))),
            withWarehouseIdentification(Some(warehouseIdentification))
          )
        )

        val correctForm =
          Seq(("procedureCode", "1234"), ("additionalProcedureCode", "000"), saveAndContinueActionUrlEncoded)

        val result = controller.submitProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.CommodityDetailsController
          .displayPage(Mode.Normal, "itemId12345")

        verify(mockProcedureCodesPage, times(0)).apply(any(), any(), any(), any())(any(), any())

        val updatedLocations = theCacheModelUpdated.locations
        updatedLocations.warehouseIdentification mustBe None
      }

      "user changed procedure code indicating warehouse identifier no longer required" in {

        val warehouseIdentification = WarehouseIdentification(Some("WarehouseId"))
        withNewCaching(
          aDeclaration(
            withType(DeclarationType.SUPPLEMENTARY),
            withItem(anItem(withItemId(itemId), withProcedureCodes(Some("1007"), Seq("0000")))),
            withWarehouseIdentification(Some(warehouseIdentification))
          )
        )

        val correctForm =
          Seq(("procedureCode", "1234"), saveAndContinueActionUrlEncoded)

        val result = controller.submitProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.CommodityDetailsController
          .displayPage(Mode.Normal, "itemId12345")

        verify(mockProcedureCodesPage, times(0)).apply(any(), any(), any(), any())(any(), any())

        val updatedLocations = theCacheModelUpdated.locations
        updatedLocations.warehouseIdentification mustBe None
      }

      "user save correct data with '0019' procedure code on clearance journey" in {

        withNewCaching(
          aDeclaration(
            withType(DeclarationType.CLEARANCE),
            withItem(
              anItem(
                withItemId(itemId),
                withFiscalInformation(FiscalInformation("No")),
                withPackageInformation(PackageInformationViewSpec.packageInformation)
              )
            )
          )
        )

        val correctForm =
          Seq(("procedureCode", "0019"), ("additionalProcedureCode", "321"), saveAndContinueActionUrlEncoded)

        val result = controller.submitProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.CommodityDetailsController
          .displayPage(Mode.Normal, "itemId12345")

        verify(mockProcedureCodesPage, times(0)).apply(any(), any(), any(), any())(any(), any())

        val updatedItem = theCacheModelUpdated.itemBy(itemId)
        updatedItem.flatMap(_.packageInformation) mustBe None
        updatedItem.flatMap(_.fiscalInformation) mustBe None
      }

      "user save correct data without new item" in {

        val item =
          ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq("123"))))
        withNewCaching(aDeclaration(withItem(item)))

        val correctForm =
          Seq(("procedureCode", "1042"), saveAndContinueActionUrlEncoded)

        val result = controller.submitProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.FiscalInformationController
          .displayPage(Mode.Normal, "itemId12345")

        verify(mockProcedureCodesPage, times(0)).apply(any(), any(), any(), any())(any(), any())
      }

      "user remove existing item" in {

        val item =
          ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq("123"))))
        withNewCaching(aDeclaration(withItem(item)))

        val removeAction = (Remove.toString, "123")

        val result = controller.submitProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(removeAction))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.ProcedureCodesController.displayPage(Mode.Normal, itemId)
      }
    }
  }
}
