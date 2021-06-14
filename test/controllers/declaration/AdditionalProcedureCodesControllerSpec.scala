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
import controllers.util.Remove
import forms.declaration.procedurecodes.AdditionalProcedureCode
import forms.declaration.procedurecodes.AdditionalProcedureCode.additionalProcedureCodeKey
import mock.ErrorHandlerMocks
import models.{DeclarationType, Mode}
import models.codes.{ProcedureCode, AdditionalProcedureCode => AdditionalProcedureCodeModel}
import models.declaration.{ExportItem, ProcedureCodesData}
import models.declaration.ProcedureCodesData.limitOfCodes
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import play.api.data.{Form, FormError}
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers.{await, status, _}
import play.twirl.api.HtmlFormat
import services.ProcedureCodeService
import views.html.declaration.procedureCodes.additional_procedure_codes

import java.util.Locale

class AdditionalProcedureCodesControllerSpec extends ControllerSpec with ErrorHandlerMocks with OptionValues with ScalaFutures {

  private val additionalProcedureCodesPage = mock[additional_procedure_codes]
  private val procedureCodeService = mock[ProcedureCodeService]
  private val messages = mock[Messages]
  private val locale = new Locale("en", "gb")

  private val controller = new AdditionalProcedureCodesController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    mockExportsCacheService,
    stubMessagesControllerComponents(),
    procedureCodeService,
    additionalProcedureCodesPage
  )(ec)

  private val itemId = "itemId12345"
  private val sampleProcedureCode = "1040"
  private val sampleProcedureCodeItem = ProcedureCode(sampleProcedureCode, "Blah blah blah")
  private val procedureCodeDataValid = ProcedureCodesData(Some(sampleProcedureCode), Seq.empty[String])
  private val procedureCodeDataInvalid = ProcedureCodesData(Some("0000"), Seq.empty[String])
  private val validAdditionalProcedureCodes = Seq(AdditionalProcedureCodeModel("000", "None"))

  private def templateParameters: (Form[AdditionalProcedureCode], Seq[String]) = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[AdditionalProcedureCode]])
    val dataCaptor = ArgumentCaptor.forClass(classOf[Seq[String]])
    verify(additionalProcedureCodesPage).apply(any(), any(), formCaptor.capture(), any(), any(), dataCaptor.capture())(any(), any())
    (formCaptor.getValue, dataCaptor.getValue)
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(additionalProcedureCodesPage.apply(any(), any(), any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(procedureCodeService.getAdditionalProcedureCodesFor(any(), any())).thenReturn(Seq.empty[AdditionalProcedureCodeModel])
    when(procedureCodeService.getAdditionalProcedureCodesFor(meq(sampleProcedureCode), any())).thenReturn(validAdditionalProcedureCodes)
    when(procedureCodeService.getProcedureCodeFor(any(), any(), any(), any())).thenReturn(None)
    when(procedureCodeService.getProcedureCodeFor(meq(sampleProcedureCode), any(), any(), any())).thenReturn(Some(sampleProcedureCodeItem))
    when(messages.lang).thenReturn(Lang(locale))
  }

  override protected def afterEach(): Unit = {
    reset(additionalProcedureCodesPage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withItem(ExportItem(itemId, procedureCodes = Some(procedureCodeDataValid)))))
    await(controller.displayPage(Mode.Normal, itemId)(request))
    templateParameters._1
  }

  "AdditionalProcedureCodesController on displayPage" should {

    "return 200 (OK)" when {

      onEveryDeclarationJourney() { request =>
        "display page method is invoked with empty additionalProcedureCodes in cache" in {

          withNewCaching(aDeclaration(withType(request.declarationType), withItem(ExportItem(itemId, procedureCodes = Some(procedureCodeDataValid)))))

          val result = controller.displayPage(Mode.Normal, itemId)(getRequest())

          status(result) mustBe OK
          verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())

          val (responseForm, responseSeq) = templateParameters
          responseForm.value mustBe empty
          responseSeq mustBe empty
        }

        "display page method is invoked with data in cache" in {

          val item = ExportItem(itemId, procedureCodes = Some(procedureCodeDataValid))
          withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))

          val result = controller.displayPage(Mode.Normal, itemId)(getRequest())

          status(result) mustBe OK
          verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())

          val (responseForm, responseSeq) = templateParameters
          responseForm.value mustBe empty
          responseSeq mustBe Seq.empty[String]
        }
      }
    }

    "return 303 (SEE_OTHER)" when {
      onEveryDeclarationJourney() { request =>
        "display page method is invoked with missing Procedure Code in cache" in {
          withNewCaching(aDeclaration(withType(request.declarationType), withItem(ExportItem(itemId))))

          val result = controller.displayPage(Mode.Normal, itemId)(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.declaration.routes.ProcedureCodesController.displayPage(Mode.Normal, itemId).url)
        }

        "display page method is invoked with invalid Procedure Code in cache" in {
          withNewCaching(
            aDeclaration(withType(request.declarationType), withItem(ExportItem(itemId, procedureCodes = Some(procedureCodeDataInvalid))))
          )

          val result = controller.displayPage(Mode.Normal, itemId)(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.declaration.routes.ProcedureCodesController.displayPage(Mode.Normal, itemId).url)
        }
      }
    }
  }

  "AdditionalProcedureCodesController on submitAdditionalProcedureCodes" when {

    val validExportItem = ExportItem(itemId, procedureCodes = Some(procedureCodeDataValid))
    onEveryDeclarationJourney() { request =>
      "provided with incorrect Action" should {
        "return 400 (BAD_REQUEST)" in {
          withNewCaching(aDeclaration(withType(request.declarationType), withItem(validExportItem)))

          val formData = Seq(("additionalProcedureCode", sampleProcedureCode), ("WrongAction", ""))

          val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

          status(result) mustBe BAD_REQUEST
          verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())
        }
      }

      "provided with 'Add' Action" when {

        "cache currently contains no AdditionalProcedureCodes for this item" when {

          def prepareCache(item: ExportItem = validExportItem): Unit = {
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))
            prepareMocks(item)
          }

          "provided with empty code" should {
            "return 400 (BAD_REQUEST)" in {
              prepareCache()
              val formData = Seq(("additionalProcedureCode", ""), addActionUrlEncoded())

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())
            }
          }

          "provided with incorrect code" should {
            "return 400 (BAD_REQUEST)" in {
              prepareCache()
              val formData = Seq(("additionalProcedureCode", "incorrect"), addActionUrlEncoded())

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())
            }
          }

          "provided with correct code" should {
            val correctForm = Seq(("additionalProcedureCode", "123"), addActionUrlEncoded())

            "add the code to the cache" in {
              prepareCache()

              controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

              val updatedItem = theCacheModelUpdated.itemBy(itemId)
              updatedItem mustBe defined
              updatedItem.get.procedureCodes mustBe defined
              updatedItem.get.procedureCodes.get.procedureCode mustBe Some(sampleProcedureCode)
              updatedItem.get.procedureCodes.get.additionalProcedureCodes mustBe Seq("123")
            }

            "not change previously cached ProcedureCode" in {
              val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq.empty)))
              prepareCache(item)

              controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

              val updatedItem = theCacheModelUpdated.itemBy(itemId)
              updatedItem mustBe defined
              updatedItem.get.procedureCodes mustBe defined
              updatedItem.get.procedureCodes.get.procedureCode mustBe defined
              updatedItem.get.procedureCodes.get.procedureCode.get mustBe sampleProcedureCode
            }

            "redirect to AdditionalProcedureCodes page" in {
              prepareCache()

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

              status(result) mustBe SEE_OTHER
              thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalProcedureCodesController.displayPage(Mode.Normal, itemId)
            }
          }
        }

        "cache already contains some AdditionalProcedureCodes for this item" when {

          def prepareCache(
            item: ExportItem = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq("111"))))
          ): Unit = {
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))
            prepareMocks(item)
          }

          "provided with empty code" should {
            "return 400 (BAD_REQUEST)" in {
              prepareCache()
              val formData = Seq(("additionalProcedureCode", ""), addActionUrlEncoded())

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())
            }
          }

          "provided with incorrect code" should {
            "return 400 (BAD_REQUEST)" in {
              prepareCache()
              val formData = Seq(("additionalProcedureCode", "incorrect"), addActionUrlEncoded())

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())
            }
          }

          "provided with duplicated code" should {
            "return 400 (BAD_REQUEST)" in {
              prepareCache()
              val formData = Seq(("additionalProcedureCode", "111"), addActionUrlEncoded())

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())
            }
          }

          "reached limit on the amount of codes" should {
            "return 400 (BAD_REQUEST)" in {
              val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq.fill(limitOfCodes)("111"))))
              prepareCache(item)
              val formData = Seq(("additionalProcedureCode", "123"), addActionUrlEncoded())

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())
            }
          }

          "adding '000' when another code is already present" should {
            "return 400 with form validation error" in {
              val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq("111"))))
              prepareCache(item)
              val formData = Seq(("additionalProcedureCode", "000"), addActionUrlEncoded())

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST

              val (form, responseSeq) = templateParameters
              form.errors.length mustBe 1
              form.errors.head mustBe FormError(additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.tripleZero.notFirstCode")
              responseSeq mustBe Seq("111")
            }
          }

          "adding another code when '000' is already present" should {
            "return 400 with form validation error" in {
              val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq("000"))))
              prepareCache(item)
              val formData = Seq(("additionalProcedureCode", "111"), addActionUrlEncoded())

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST

              val (form, responseSeq) = templateParameters
              form.errors.length mustBe 1
              form.errors.head mustBe FormError(additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.tripleZero.alreadyPresent")
              responseSeq mustBe Seq("000")
            }
          }

          "provided with correct code" should {
            val correctForm = Seq(("additionalProcedureCode", "123"), addActionUrlEncoded())

            "add the code to the cache" in {
              prepareCache()

              controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

              val updatedItem = theCacheModelUpdated.itemBy(itemId)
              updatedItem mustBe defined
              updatedItem.get.procedureCodes mustBe defined
              updatedItem.get.procedureCodes.get.procedureCode mustBe Some(sampleProcedureCode)
              updatedItem.get.procedureCodes.get.additionalProcedureCodes mustBe Seq("111", "123")
            }

            "not change previously cached ProcedureCode" in {
              val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq("111"))))
              prepareCache(item)

              controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

              val updatedItem = theCacheModelUpdated.itemBy(itemId)
              updatedItem mustBe defined
              updatedItem.get.procedureCodes mustBe defined
              updatedItem.get.procedureCodes.get.procedureCode mustBe defined
              updatedItem.get.procedureCodes.get.procedureCode.get mustBe sampleProcedureCode
            }

            "redirect to AdditionalProcedureCodes page" in {
              prepareCache()

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

              status(result) mustBe SEE_OTHER
              thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalProcedureCodesController.displayPage(Mode.Normal, itemId)
            }
          }
        }
      }

      "provided with 'Save and Continue' Action" when {

        "cache currently contains no AdditionalProcedureCodes for this item" when {

          def prepareCache(item: ExportItem = validExportItem): Unit = {
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))
            prepareMocks(item)
          }

          "provided with empty code" should {
            "return 400 (BAD_REQUEST)" in {
              prepareCache()
              val formData = Seq(("additionalProcedureCode", ""), saveAndContinueActionUrlEncoded)

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())
            }
          }

          "provided with incorrect code" should {
            "return 400 (BAD_REQUEST)" in {
              prepareCache()
              val formData = Seq(("additionalProcedureCode", "incorrect"), saveAndContinueActionUrlEncoded)

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())
            }
          }

          "provided with correct code" should {
            val correctForm = Seq(("additionalProcedureCode", "123"), saveAndContinueActionUrlEncoded)

            "add the code to the cache" in {
              prepareCache()

              controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

              val updatedItem = theCacheModelUpdated.itemBy(itemId)
              updatedItem mustBe defined
              updatedItem.get.procedureCodes mustBe defined
              updatedItem.get.procedureCodes.get.procedureCode mustBe Some(sampleProcedureCode)
              updatedItem.get.procedureCodes.get.additionalProcedureCodes mustBe Seq("123")
            }

            "not change previously cached ProcedureCode" in {
              val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq.empty)))
              prepareCache(item)

              controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

              val updatedItem = theCacheModelUpdated.itemBy(itemId)
              updatedItem mustBe defined
              updatedItem.get.procedureCodes mustBe defined
              updatedItem.get.procedureCodes.get.procedureCode mustBe defined
              updatedItem.get.procedureCodes.get.procedureCode.get mustBe sampleProcedureCode
            }

            "redirect to FiscalInformation page" when {
              "the ProcedureCode in cache is '1042'" in {
                val procedureCode = "1042"
                val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(procedureCode), Seq.empty)))
                prepareCache(item)

                val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

                status(result) mustBe SEE_OTHER
                thePageNavigatedTo mustBe controllers.declaration.routes.FiscalInformationController.displayPage(Mode.Normal, itemId)
              }
            }

            "redirect to CommodityDetails page" when {

              "the ProcedureCode in cache is NOT '1042'" in {
                val procedureCode = "1234"
                val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(procedureCode), Seq.empty)))
                prepareCache(item)

                val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

                status(result) mustBe SEE_OTHER
                thePageNavigatedTo mustBe controllers.declaration.routes.CommodityDetailsController.displayPage(Mode.Normal, itemId)
              }

              "the ProcedureCode in cache is empty'" in {
                prepareCache()

                val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

                status(result) mustBe SEE_OTHER
                thePageNavigatedTo mustBe controllers.declaration.routes.CommodityDetailsController.displayPage(Mode.Normal, itemId)
              }
            }
          }
        }

        "cache contains some AdditionalProcedureCodes for this item" when {

          def prepareCache(
            item: ExportItem = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq("111"))))
          ): Unit = {
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))
            prepareMocks(item)
          }

          "provided with empty code" should {
            val formData = Seq(("additionalProcedureCode", ""), saveAndContinueActionUrlEncoded)

            "redirect to FiscalInformation page" when {
              "the ProcedureCode in cache is '1042'" in {
                val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1042"), Seq("111"))))
                prepareCache(item)

                val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

                status(result) mustBe SEE_OTHER
                thePageNavigatedTo mustBe controllers.declaration.routes.FiscalInformationController.displayPage(Mode.Normal, itemId)
              }
            }

            "redirect to CommodityDetails page" when {

              "the ProcedureCode in cache is NOT '1042'" in {
                val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq("111"))))
                prepareCache(item)

                val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

                status(result) mustBe SEE_OTHER
                thePageNavigatedTo mustBe controllers.declaration.routes.CommodityDetailsController.displayPage(Mode.Normal, itemId)
              }

            }
          }

          "provided with incorrect code" should {
            "return 400 (BAD_REQUEST)" in {
              prepareCache()
              val formData = Seq(("additionalProcedureCode", "incorrect"), saveAndContinueActionUrlEncoded)

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())
            }
          }

          "provided with duplicated code" should {
            "return 400 (BAD_REQUEST)" in {
              prepareCache()
              val formData = Seq(("additionalProcedureCode", "111"), saveAndContinueActionUrlEncoded)

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())
            }
          }

          "reached limit on the amount of codes" should {
            "return 400 (BAD_REQUEST)" in {
              val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq.fill(limitOfCodes)("111"))))
              prepareCache(item)
              val formData = Seq(("additionalProcedureCode", "123"), saveAndContinueActionUrlEncoded)

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())
            }
          }

          "provided with correct code" should {
            val correctForm = Seq(("additionalProcedureCode", "123"), saveAndContinueActionUrlEncoded)

            "add the code to the cache" in {
              prepareCache()

              controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

              val updatedItem = theCacheModelUpdated.itemBy(itemId)
              updatedItem mustBe defined
              updatedItem.get.procedureCodes mustBe defined
              updatedItem.get.procedureCodes.get.additionalProcedureCodes mustBe Seq("111", "123")
            }

            "not change previously cached ProcedureCode" in {
              val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq("111"))))
              prepareCache(item)

              controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

              val updatedItem = theCacheModelUpdated.itemBy(itemId)
              updatedItem mustBe defined
              updatedItem.get.procedureCodes mustBe defined
              updatedItem.get.procedureCodes.get.procedureCode mustBe defined
              updatedItem.get.procedureCodes.get.procedureCode.get mustBe sampleProcedureCode
            }

            "redirect to FiscalInformation page" when {
              "the ProcedureCode in cache is '1042'" in {
                val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1042"), Seq("111"))))
                prepareCache(item)

                val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

                status(result) mustBe SEE_OTHER
                thePageNavigatedTo mustBe controllers.declaration.routes.FiscalInformationController.displayPage(Mode.Normal, itemId)
              }
            }

            "redirect to CommodityDetails page" when {

              "the ProcedureCode in cache is NOT '1042'" in {
                val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq("111"))))
                prepareCache(item)

                val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

                status(result) mustBe SEE_OTHER
                thePageNavigatedTo mustBe controllers.declaration.routes.CommodityDetailsController.displayPage(Mode.Normal, itemId)
              }

              "the ProcedureCode in cache is empty'" in {
                prepareCache()

                val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

                status(result) mustBe SEE_OTHER
                thePageNavigatedTo mustBe controllers.declaration.routes.CommodityDetailsController.displayPage(Mode.Normal, itemId)
              }
            }
          }
        }
      }

      "provided with 'Remove' Action" when {

        val removeAction = (Remove.toString, "123")

        "AdditionalProcedureCodes cache is empty" should {

          def prepareCache(): Unit = {
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(validExportItem)))
            prepareMocks(validExportItem)
          }

          "not change the cache" in {
            prepareCache()

            controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(removeAction)).futureValue

            val updatedItem = theCacheModelUpdated.itemBy(itemId)
            updatedItem mustBe defined
            updatedItem.get.procedureCodes mustBe defined
            updatedItem.get.procedureCodes.get.procedureCode mustBe Some(sampleProcedureCode)
            updatedItem.get.procedureCodes.get.additionalProcedureCodes mustBe empty
          }

          "redirect to AdditionalProcedureCodes page" in {
            prepareCache()

            val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(removeAction))

            status(result) mustBe SEE_OTHER
            thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalProcedureCodesController.displayPage(Mode.Normal, itemId)
          }
        }

        "AdditionalProcedureCodes cache is NOT empty" should {

          def prepareCache(): Unit = {
            val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq("111", "123"))))
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))
            prepareMocks(item)
          }

          "remove the code from cache" in {
            prepareCache()

            controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(removeAction)).futureValue

            val updatedItem = theCacheModelUpdated.itemBy(itemId)
            updatedItem mustBe defined
            updatedItem.get.procedureCodes mustBe defined
            updatedItem.get.procedureCodes.get.additionalProcedureCodes mustBe Seq("111")
          }

          "not change previously cached ProcedureCode" in {
            prepareCache()

            controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(removeAction)).futureValue

            val updatedItem = theCacheModelUpdated.itemBy(itemId)
            updatedItem mustBe defined
            updatedItem.get.procedureCodes mustBe defined
            updatedItem.get.procedureCodes.get.procedureCode mustBe defined
            updatedItem.get.procedureCodes.get.procedureCode.get mustBe sampleProcedureCode
          }

          "redirect to AdditionalProcedureCodes page" in {
            prepareCache()

            val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(removeAction))

            status(result) mustBe SEE_OTHER
            thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalProcedureCodesController.displayPage(Mode.Normal, itemId)
          }
        }
      }
    }
  }

  private def prepareMocks(item: ExportItem) =
    for {
      procedureCodeData <- item.procedureCodes
      procedureCode <- procedureCodeData.procedureCode
    } yield {
      when(procedureCodeService.getAdditionalProcedureCodesFor(meq(procedureCode), any()))
        .thenReturn(validAdditionalProcedureCodes)
      when(procedureCodeService.getProcedureCodeFor(meq(procedureCode), any(), any(), any()))
        .thenReturn(Some(ProcedureCode(procedureCode, "Blah blah blah")))
    }
}
