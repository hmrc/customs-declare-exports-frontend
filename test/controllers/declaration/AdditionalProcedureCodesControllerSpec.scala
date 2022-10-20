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
import base.ExportsTestData.pc1040
import controllers.helpers.{Remove, SupervisingCustomsOfficeHelper}
import forms.declaration.SupervisingCustomsOffice
import forms.declaration.procedurecodes.AdditionalProcedureCode
import forms.declaration.procedurecodes.AdditionalProcedureCode.additionalProcedureCodeKey
import mock.ErrorHandlerMocks
import models.codes.AdditionalProcedureCode.NO_APC_APPLIES_CODE
import models.codes.{AdditionalProcedureCode => AdditionalProcedureCodeModel, ProcedureCode}
import models.declaration.ProcedureCodesData.limitOfCodes
import models.declaration.{ExportItem, ProcedureCodesData}
import models.requests.JourneyRequest
import models.{DeclarationType}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import play.api.data.{Form, FormError}
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers.{await, status, _}
import play.twirl.api.HtmlFormat
import services.ProcedureCodeService
import views.html.declaration.procedureCodes.additional_procedure_codes

import java.util.{Locale, UUID}

class AdditionalProcedureCodesControllerSpec extends ControllerSpec with ErrorHandlerMocks with OptionValues with ScalaFutures {

  private val additionalProcedureCodesPage = mock[additional_procedure_codes]
  private val procedureCodeService = mock[ProcedureCodeService]
  private val messages = mock[Messages]
  private val locale = new Locale("en", "gb")
  private val supervisingCustomsOfficeHelper = instanceOf[SupervisingCustomsOfficeHelper]

  private val controller = new AdditionalProcedureCodesController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    mockExportsCacheService,
    stubMessagesControllerComponents(),
    procedureCodeService,
    additionalProcedureCodesPage,
    supervisingCustomsOfficeHelper
  )(ec)

  private val itemId = "itemId12345"
  private val sampleProcedureCode = "1040"
  private val sampleProcedureCodeItem = ProcedureCode(sampleProcedureCode, "Blah blah blah")
  private val procedureCodeDataValid = ProcedureCodesData(Some(sampleProcedureCode), Seq.empty[String])
  private val procedureCodeDataInvalid = ProcedureCodesData(Some("0000"), Seq.empty[String])
  private val validAdditionalProcedureCodes = Seq(AdditionalProcedureCodeModel(NO_APC_APPLIES_CODE, "None"))

  private def templateParameters: (Form[AdditionalProcedureCode], Seq[String]) = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[AdditionalProcedureCode]])
    val dataCaptor = ArgumentCaptor.forClass(classOf[Seq[String]])
    verify(additionalProcedureCodesPage).apply(any(), formCaptor.capture(), any(), any(), dataCaptor.capture())(any(), any())
    (formCaptor.getValue, dataCaptor.getValue)
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(additionalProcedureCodesPage.apply(any(), any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
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
    await(controller.displayPage(itemId)(request))
    templateParameters._1
  }

  "AdditionalProcedureCodesController on displayPage" should {

    "return 200 (OK)" when {

      onEveryDeclarationJourney() { request =>
        "display page method is invoked with empty additionalProcedureCodes in cache" in {

          withNewCaching(aDeclaration(withType(request.declarationType), withItem(ExportItem(itemId, procedureCodes = Some(procedureCodeDataValid)))))

          val result = controller.displayPage(itemId)(getRequest())

          status(result) mustBe OK
          verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())

          val (responseForm, responseSeq) = templateParameters
          responseForm.value mustBe empty
          responseSeq mustBe empty
        }

        "display page method is invoked with data in cache" in {

          val item = ExportItem(itemId, procedureCodes = Some(procedureCodeDataValid))
          withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))

          val result = controller.displayPage(itemId)(getRequest())

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

          val result = controller.displayPage(itemId)(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.ProcedureCodesController.displayPage(itemId).url)
        }

        "display page method is invoked with invalid Procedure Code in cache" in {
          withNewCaching(
            aDeclaration(withType(request.declarationType), withItem(ExportItem(itemId, procedureCodes = Some(procedureCodeDataInvalid))))
          )

          val result = controller.displayPage(itemId)(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.ProcedureCodesController.displayPage(itemId).url)
        }
      }
    }
  }

  "AdditionalProcedureCodesController on submitAdditionalProcedureCodes" when {

    val validExportItem = ExportItem(itemId, procedureCodes = Some(procedureCodeDataValid))

    onEveryDeclarationJourney() { implicit request =>
      "provided with incorrect Action" should {
        "return 400 (BAD_REQUEST)" in {
          withNewCaching(aDeclaration(withType(request.declarationType), withItem(validExportItem)))

          val formData = Seq(("additionalProcedureCode", sampleProcedureCode), ("WrongAction", ""))

          val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(formData: _*))

          status(result) mustBe BAD_REQUEST
          verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())
        }
      }

      "provided with 'Add' Action" when {
        "cache currently contains no AdditionalProcedureCodes for this item" when {

          testBadCodes(validExportItem, addActionUrlEncoded())

          "provided with correct code" should {

            testAddCodeSuccess(validExportItem, addActionUrlEncoded())

            "redirect to AdditionalProcedureCodes page" in {
              prepareCache(validExportItem)

              val correctForm = Seq(("additionalProcedureCode", "123"), addActionUrlEncoded())
              val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

              status(result) mustBe SEE_OTHER
              thePageNavigatedTo mustBe routes.AdditionalProcedureCodesController.displayPage(itemId)
            }
          }
        }

        "cache already contains some AdditionalProcedureCodes for this item" when {
          val exportItem = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq("111"))))

          testBadCodes(exportItem, addActionUrlEncoded())

          testCodesWithCachePopulated(
            ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq("111")))),
            addActionUrlEncoded()
          )

          "provided with correct code" should {
            testAddCodeSuccess(exportItem, addActionUrlEncoded())

            "redirect to AdditionalProcedureCodes page" in {
              prepareCache(exportItem)

              val correctForm = Seq(("additionalProcedureCode", "123"), addActionUrlEncoded())
              val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

              status(result) mustBe SEE_OTHER
              thePageNavigatedTo mustBe routes.AdditionalProcedureCodesController.displayPage(itemId)
            }
          }
        }
      }

      "provided with 'Save and Continue' Action" when {
        "cache currently contains no AdditionalProcedureCodes for this item" when {

          testBadCodes(validExportItem, saveAndContinueActionUrlEncoded)

          "provided with correct code" should {
            testAddCodeSuccess(
              ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq("111")))),
              saveAndContinueActionUrlEncoded
            )

            val correctForm = Seq(("additionalProcedureCode", "123"), saveAndContinueActionUrlEncoded)

            "redirect to FiscalInformation page" when {
              "the ProcedureCode in cache is '1042'" in {
                val procedureCode = "1042"
                val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(procedureCode), Seq.empty)))
                prepareCache(item)

                val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

                status(result) mustBe SEE_OTHER
                thePageNavigatedTo mustBe routes.FiscalInformationController.displayPage(itemId)
              }
            }

            "redirect to CommodityDetails page" when {
              "the ProcedureCode in cache is NOT '1042'" in {
                val procedureCode = "1234"
                val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(procedureCode), Seq.empty)))
                prepareCache(item)

                val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

                status(result) mustBe SEE_OTHER
                thePageNavigatedTo mustBe routes.CommodityDetailsController.displayPage(itemId)
              }

              "the ProcedureCode in cache is empty'" in {
                prepareCache(validExportItem)

                val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

                status(result) mustBe SEE_OTHER
                thePageNavigatedTo mustBe routes.CommodityDetailsController.displayPage(itemId)
              }
            }
          }

          "provided with procedureCode '1040' and additionalProcedureCode '000'" should {
            "reset (to None) 'supervisingCustomsOffice'" in {
              val item = ExportItem(itemId, procedureCodes = pc1040)
              val someOffice = withSupervisingCustomsOffice(Some(SupervisingCustomsOffice(Some("Some office"))))

              withNewCaching(aDeclaration(withType(request.declarationType), withItem(item), someOffice))

              val correctForm = List(saveAndContinueActionUrlEncoded)

              controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

              val updatedModel = theCacheModelUpdated
              updatedModel.locations.supervisingCustomsOffice mustBe None

              val procedureCodesData = updatedModel.itemBy(itemId).get.procedureCodes.get
              procedureCodesData.procedureCode.get mustBe sampleProcedureCode
              procedureCodesData.additionalProcedureCodes mustBe List(NO_APC_APPLIES_CODE)
            }
          }
        }

        "cache contains some AdditionalProcedureCodes for this item" when {

          "provided with empty code" should {
            val formData = Seq(("additionalProcedureCode", ""), saveAndContinueActionUrlEncoded)

            "redirect to FiscalInformation page" when {
              "the ProcedureCode in cache is '1042'" in {
                val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1042"), Seq("111"))))
                prepareCache(item)

                val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(formData: _*))

                status(result) mustBe SEE_OTHER
                thePageNavigatedTo mustBe routes.FiscalInformationController.displayPage(itemId)
              }
            }

            "redirect to CommodityDetails page" when {

              "the ProcedureCode in cache is NOT '1042'" in {
                val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq("111"))))
                prepareCache(item)

                val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(formData: _*))

                status(result) mustBe SEE_OTHER
                thePageNavigatedTo mustBe routes.CommodityDetailsController.displayPage(itemId)
              }

            }
          }

          val exportItem = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq("111"))))

          "provided with incorrect code" should {
            "return 400 (BAD_REQUEST)" in {
              prepareCache(exportItem)
              val formData = Seq(("additionalProcedureCode", "incorrect"), saveAndContinueActionUrlEncoded)

              val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())
            }
          }

          testCodesWithCachePopulated(exportItem, saveAndContinueActionUrlEncoded)

          "provided with correct code" should {

            testAddCodeSuccess(exportItem, saveAndContinueActionUrlEncoded)

            val correctForm = Seq(("additionalProcedureCode", "123"), saveAndContinueActionUrlEncoded)

            "redirect to FiscalInformation page" when {
              "the ProcedureCode in cache is '1042'" in {
                val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1042"), Seq("111"))))
                prepareCache(item)

                val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

                status(result) mustBe SEE_OTHER
                thePageNavigatedTo mustBe routes.FiscalInformationController.displayPage(itemId)
              }
            }

            "redirect to CommodityDetails page" when {

              "the ProcedureCode in cache is NOT '1042'" in {
                val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq("111"))))
                prepareCache(item)

                val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

                status(result) mustBe SEE_OTHER
                thePageNavigatedTo mustBe routes.CommodityDetailsController.displayPage(itemId)
              }

              "the ProcedureCode in cache is empty'" in {
                prepareCache(exportItem)

                val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

                status(result) mustBe SEE_OTHER
                thePageNavigatedTo mustBe routes.CommodityDetailsController.displayPage(itemId)
              }
            }
          }
        }

        "multiple items are provided with procedureCode '1040' and additionalProcedureCode '000'" should {
          "reset (to None) 'supervisingCustomsOffice'" in {
            val itemId1 = UUID.randomUUID.toString
            val item1 = ExportItem(itemId1, procedureCodes = pc1040)
            val item2 = ExportItem(itemId, procedureCodes = pc1040)
            val someOffice = withSupervisingCustomsOffice(Some(SupervisingCustomsOffice(Some("Some office"))))
            withNewCaching(aDeclaration(withType(request.declarationType), withItems(item1, item2), someOffice))

            val correctForm = List(saveAndContinueActionUrlEncoded)

            controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

            val updatedModel = theCacheModelUpdated
            updatedModel.locations.supervisingCustomsOffice mustBe None

            val procedureCodesData1 = updatedModel.itemBy(itemId1).get.procedureCodes.get
            procedureCodesData1.procedureCode.get mustBe sampleProcedureCode
            procedureCodesData1.additionalProcedureCodes mustBe List(NO_APC_APPLIES_CODE)

            val procedureCodesData2 = updatedModel.itemBy(itemId).get.procedureCodes.get
            procedureCodesData2.procedureCode.get mustBe sampleProcedureCode
            procedureCodesData2.additionalProcedureCodes mustBe List(NO_APC_APPLIES_CODE)
          }
        }
      }

      "provided with 'Remove' Action" when {

        val removeAction = (Remove.toString, "123")

        "AdditionalProcedureCodes cache is empty" should {
          "not change the cache" in {
            prepareCache(validExportItem)

            controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(removeAction)).futureValue

            val updatedItem = theCacheModelUpdated.itemBy(itemId)
            updatedItem mustBe defined
            updatedItem.get.procedureCodes mustBe defined
            updatedItem.get.procedureCodes.get.procedureCode mustBe Some(sampleProcedureCode)
            updatedItem.get.procedureCodes.get.additionalProcedureCodes mustBe empty
          }

          "redirect to AdditionalProcedureCodes page" in {
            prepareCache(validExportItem)

            val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(removeAction))

            status(result) mustBe SEE_OTHER
            thePageNavigatedTo mustBe routes.AdditionalProcedureCodesController.displayPage(itemId)
          }
        }

        "AdditionalProcedureCodes cache is NOT empty" should {

          val exportItem = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq("111", "123"))))

          "remove the code from cache" in {
            prepareCache(exportItem)

            controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(removeAction)).futureValue

            val updatedItem = theCacheModelUpdated.itemBy(itemId)
            updatedItem mustBe defined
            updatedItem.get.procedureCodes mustBe defined
            updatedItem.get.procedureCodes.get.additionalProcedureCodes mustBe Seq("111")
          }

          "not change previously cached ProcedureCode" in {
            prepareCache(exportItem)

            controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(removeAction)).futureValue

            val updatedItem = theCacheModelUpdated.itemBy(itemId)
            updatedItem mustBe defined
            updatedItem.get.procedureCodes mustBe defined
            updatedItem.get.procedureCodes.get.procedureCode mustBe defined
            updatedItem.get.procedureCodes.get.procedureCode.get mustBe sampleProcedureCode
          }

          "redirect to AdditionalProcedureCodes page" in {
            prepareCache(exportItem)

            val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(removeAction))

            status(result) mustBe SEE_OTHER
            thePageNavigatedTo mustBe routes.AdditionalProcedureCodesController.displayPage(itemId)
          }
        }
      }
    }
  }

  private def prepareCache(item: ExportItem)(implicit request: JourneyRequest[_]): Unit = {
    withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))
    prepareMocks(item)
  }

  private def testBadCodes(exportItem: ExportItem, formAction: (String, String))(implicit request: JourneyRequest[_]) = {
    "provided with empty code" should {
      "return 400 (BAD_REQUEST)" in {
        prepareCache(exportItem)
        val formData = Seq(("additionalProcedureCode", ""), formAction)

        val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(formData: _*))

        status(result) mustBe BAD_REQUEST
        verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())
      }
    }

    "provided with incorrect code" should {
      "return 400 (BAD_REQUEST)" in {
        prepareCache(exportItem)
        val formData = Seq(("additionalProcedureCode", "incorrect"), addActionUrlEncoded())

        val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(formData: _*))

        status(result) mustBe BAD_REQUEST
        verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())
      }
    }
  }

  private def testCodesWithCachePopulated(defaultExportItem: ExportItem, formAction: (String, String))(implicit request: JourneyRequest[_]) = {
    "provided with duplicated code" should {
      "return 400 (BAD_REQUEST)" in {
        prepareCache(defaultExportItem)
        val formData = Seq(("additionalProcedureCode", "111"), formAction)

        val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(formData: _*))

        status(result) mustBe BAD_REQUEST
        verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())
      }
    }

    "reached limit on the amount of codes" should {
      "return 400 (BAD_REQUEST)" in {
        val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq.fill(limitOfCodes)("111"))))
        prepareCache(item)
        val formData = Seq(("additionalProcedureCode", "123"), formAction)

        val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(formData: _*))

        status(result) mustBe BAD_REQUEST
        verify(additionalProcedureCodesPage).apply(any(), any(), any(), any(), any(), any())(any(), any())
      }
    }

    "adding '000' when another code is already present" should {
      "return 400 with form validation error" in {
        val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq("111"))))
        prepareCache(item)
        val formData = Seq(("additionalProcedureCode", NO_APC_APPLIES_CODE), formAction)

        val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(formData: _*))

        status(result) mustBe BAD_REQUEST

        val (form, responseSeq) = templateParameters
        form.errors.length mustBe 1
        form.errors.head mustBe FormError(additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.tripleZero.notFirstCode")
        responseSeq mustBe Seq("111")
      }
    }

    "adding another code when '000' is already present" should {
      "return 400 with form validation error" in {
        val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq(NO_APC_APPLIES_CODE))))
        prepareCache(item)
        val formData = Seq(("additionalProcedureCode", "111"), formAction)

        val result = controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(formData: _*))

        status(result) mustBe BAD_REQUEST

        val (form, responseSeq) = templateParameters
        form.errors.length mustBe 1
        form.errors.head mustBe FormError(additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.tripleZero.alreadyPresent")
        responseSeq mustBe Seq(NO_APC_APPLIES_CODE)
      }
    }
  }

  private def testAddCodeSuccess(defaultExportItem: ExportItem, formAction: (String, String))(implicit request: JourneyRequest[_]) = {
    val correctForm = Seq(("additionalProcedureCode", "123"), formAction)

    "add the code to the cache" in {
      prepareCache(defaultExportItem)

      controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

      val updatedItem = theCacheModelUpdated.itemBy(itemId)
      updatedItem mustBe defined
      updatedItem.get.procedureCodes mustBe defined
      updatedItem.get.procedureCodes.get.procedureCode mustBe Some(sampleProcedureCode)

      val preexistingCache = defaultExportItem.procedureCodes.map(_.additionalProcedureCodes).getOrElse(Seq.empty[String])
      updatedItem.get.procedureCodes.get.additionalProcedureCodes mustBe preexistingCache ++ Seq("123")
    }

    "not change previously cached ProcedureCode" in {
      val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some(sampleProcedureCode), Seq.empty)))
      prepareCache(item)

      controller.submitAdditionalProcedureCodes(itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

      val updatedItem = theCacheModelUpdated.itemBy(itemId)
      updatedItem mustBe defined
      updatedItem.get.procedureCodes mustBe defined
      updatedItem.get.procedureCodes.get.procedureCode mustBe defined
      updatedItem.get.procedureCodes.get.procedureCode.get mustBe sampleProcedureCode
    }
  }

  private def prepareMocks(item: ExportItem): Option[OngoingStubbing[Option[ProcedureCode]]] =
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
