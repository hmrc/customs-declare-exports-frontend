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
import forms.declaration.procedurecodes.AdditionalProcedureCodes
import mock.ErrorHandlerMocks
import models.Mode
import models.declaration.ProcedureCodesData.limitOfCodes
import models.declaration.{ExportItem, ProcedureCodesData}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers.{await, status, _}
import play.twirl.api.HtmlFormat
import views.html.declaration.procedureCodes.additional_procedure_codes

class AdditionalProcedureCodesControllerSpec extends ControllerSpec with ErrorHandlerMocks with OptionValues with ScalaFutures {

  private val additionalProcedureCodesPage = mock[additional_procedure_codes]

  private val controller = new AdditionalProcedureCodesController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    mockExportsCacheService,
    stubMessagesControllerComponents(),
    additionalProcedureCodesPage
  )(ec)

  private val itemId = "itemId12345"

  private def templateParameters: (Form[AdditionalProcedureCodes], Seq[String]) = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[AdditionalProcedureCodes]])
    val dataCaptor = ArgumentCaptor.forClass(classOf[Seq[String]])
    verify(additionalProcedureCodesPage).apply(any(), any(), formCaptor.capture(), dataCaptor.capture())(any(), any())
    (formCaptor.getValue, dataCaptor.getValue)
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(additionalProcedureCodesPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(additionalProcedureCodesPage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal, itemId)(request))
    templateParameters._1
  }

  "AdditionalProcedureCodesController on displayPage" should {

    "return 200 (OK)" when {

      onEveryDeclarationJourney() { request =>
        "display page method is invoked with empty cache" in {

          withNewCaching(aDeclaration(withType(request.declarationType), withItem(ExportItem(itemId))))

          val result = controller.displayPage(Mode.Normal, itemId)(getRequest())

          status(result) mustBe OK
          verify(additionalProcedureCodesPage).apply(any(), any(), any(), any())(any(), any())

          val (responseForm, responseSeq) = templateParameters
          responseForm.value mustBe empty
          responseSeq mustBe empty
        }

        "display page method is invoked with data in cache" in {

          val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq("123"))))
          withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))

          val result = controller.displayPage(Mode.Normal, itemId)(getRequest())

          status(result) mustBe OK
          verify(additionalProcedureCodesPage).apply(any(), any(), any(), any())(any(), any())

          val (responseForm, responseSeq) = templateParameters
          responseForm.value mustBe empty
          responseSeq mustBe Seq("123")
        }
      }
    }
  }

  "AdditionalProcedureCodesController on submitAdditionalProcedureCodes" when {

    onEveryDeclarationJourney() { request =>
      "provided with incorrect Action" should {
        "return 400 (BAD_REQUEST)" in {
          withNewCaching(aDeclaration(withType(request.declarationType)))

          val formData = Seq(("additionalProcedureCode", "1234"), ("WrongAction", ""))

          val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

          status(result) mustBe BAD_REQUEST
          verify(additionalProcedureCodesPage).apply(any(), any(), any(), any())(any(), any())
        }
      }

      "provided with 'Add' Action" when {

        "AdditionalProcedureCodes cache is empty" when {

          def prepareCache(item: ExportItem = ExportItem(itemId)): Unit =
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))

          "provided with empty code" should {
            "return 400 (BAD_REQUEST)" in {
              prepareCache()
              val formData = Seq(("additionalProcedureCode", ""), addActionUrlEncoded())

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any())(any(), any())
            }
          }

          "provided with incorrect code" should {
            "return 400 (BAD_REQUEST)" in {
              prepareCache()
              val formData = Seq(("additionalProcedureCode", "incorrect"), addActionUrlEncoded())

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any())(any(), any())
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
              updatedItem.get.procedureCodes.get.procedureCode mustBe empty
              updatedItem.get.procedureCodes.get.additionalProcedureCodes mustBe Seq("123")
            }

            "not change previously cached ProcedureCode" in {
              val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq.empty)))
              prepareCache(item)

              controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

              val updatedItem = theCacheModelUpdated.itemBy(itemId)
              updatedItem mustBe defined
              updatedItem.get.procedureCodes mustBe defined
              updatedItem.get.procedureCodes.get.procedureCode mustBe defined
              updatedItem.get.procedureCodes.get.procedureCode.get mustBe "1234"
            }

            "redirect to AdditionalProcedureCodes page" in {
              prepareCache()

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

              status(result) mustBe SEE_OTHER
              thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalProcedureCodesController.displayPage(Mode.Normal, itemId)
            }
          }
        }

        "AdditionalProcedureCodes cache is NOT empty" when {

          def prepareCache(item: ExportItem = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(None, Seq("111"))))): Unit =
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))

          "provided with empty code" should {
            "return 400 (BAD_REQUEST)" in {
              prepareCache()
              val formData = Seq(("additionalProcedureCode", ""), addActionUrlEncoded())

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any())(any(), any())
            }
          }

          "provided with incorrect code" should {
            "return 400 (BAD_REQUEST)" in {
              prepareCache()
              val formData = Seq(("additionalProcedureCode", "incorrect"), addActionUrlEncoded())

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any())(any(), any())
            }
          }

          "provided with duplicated code" should {
            "return 400 (BAD_REQUEST)" in {
              prepareCache()
              val formData = Seq(("additionalProcedureCode", "111"), addActionUrlEncoded())

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any())(any(), any())
            }
          }

          "reached limit on the amount of codes" should {
            "return 400 (BAD_REQUEST)" in {
              val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(None, Seq.fill(limitOfCodes)("111"))))
              prepareCache(item)
              val formData = Seq(("additionalProcedureCode", "123"), addActionUrlEncoded())

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any())(any(), any())
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
              updatedItem.get.procedureCodes.get.procedureCode mustBe empty
              updatedItem.get.procedureCodes.get.additionalProcedureCodes mustBe Seq("111", "123")
            }

            "not change previously cached ProcedureCode" in {
              val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq("111"))))
              prepareCache(item)

              controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

              val updatedItem = theCacheModelUpdated.itemBy(itemId)
              updatedItem mustBe defined
              updatedItem.get.procedureCodes mustBe defined
              updatedItem.get.procedureCodes.get.procedureCode mustBe defined
              updatedItem.get.procedureCodes.get.procedureCode.get mustBe "1234"
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

        "AdditionalProcedureCodes cache is empty" when {

          def prepareCache(item: ExportItem = ExportItem(itemId)): Unit =
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))

          "provided with empty code" should {
            "return 400 (BAD_REQUEST)" in {
              prepareCache()
              val formData = Seq(("additionalProcedureCode", ""), saveAndContinueActionUrlEncoded)

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any())(any(), any())
            }
          }

          "provided with incorrect code" should {
            "return 400 (BAD_REQUEST)" in {
              prepareCache()
              val formData = Seq(("additionalProcedureCode", "incorrect"), saveAndContinueActionUrlEncoded)

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any())(any(), any())
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
              updatedItem.get.procedureCodes.get.procedureCode mustBe empty
              updatedItem.get.procedureCodes.get.additionalProcedureCodes mustBe Seq("123")
            }

            "not change previously cached ProcedureCode" in {
              val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq.empty)))
              prepareCache(item)

              controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

              val updatedItem = theCacheModelUpdated.itemBy(itemId)
              updatedItem mustBe defined
              updatedItem.get.procedureCodes mustBe defined
              updatedItem.get.procedureCodes.get.procedureCode mustBe defined
              updatedItem.get.procedureCodes.get.procedureCode.get mustBe "1234"
            }

            "redirect to FiscalInformation page" when {
              "the ProcedureCode in cache is '1042'" in {
                val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1042"), Seq.empty)))
                prepareCache(item)

                val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

                status(result) mustBe SEE_OTHER
                thePageNavigatedTo mustBe controllers.declaration.routes.FiscalInformationController.displayPage(Mode.Normal, itemId)
              }
            }

            "redirect to CommodityDetails page" when {

              "the ProcedureCode in cache is NOT '1042'" in {
                val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq.empty)))
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

        "AdditionalProcedureCodes cache is NOT empty" when {

          def prepareCache(item: ExportItem = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(None, Seq("111"))))): Unit =
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))

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

              "the ProcedureCode in cache is empty'" in {
                prepareCache()

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
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any())(any(), any())
            }
          }

          "provided with duplicated code" should {
            "return 400 (BAD_REQUEST)" in {
              prepareCache()
              val formData = Seq(("additionalProcedureCode", "111"), saveAndContinueActionUrlEncoded)

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any())(any(), any())
            }
          }

          "reached limit on the amount of codes" should {
            "return 400 (BAD_REQUEST)" in {
              val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(None, Seq.fill(limitOfCodes)("111"))))
              prepareCache(item)
              val formData = Seq(("additionalProcedureCode", "123"), saveAndContinueActionUrlEncoded)

              val result = controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(formData: _*))

              status(result) mustBe BAD_REQUEST
              verify(additionalProcedureCodesPage).apply(any(), any(), any(), any())(any(), any())
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
              val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq("111"))))
              prepareCache(item)

              controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*)).futureValue

              val updatedItem = theCacheModelUpdated.itemBy(itemId)
              updatedItem mustBe defined
              updatedItem.get.procedureCodes mustBe defined
              updatedItem.get.procedureCodes.get.procedureCode mustBe defined
              updatedItem.get.procedureCodes.get.procedureCode.get mustBe "1234"
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

          def prepareCache(): Unit = withNewCaching(aDeclaration(withType(request.declarationType), withItem(ExportItem(itemId))))

          "not change the cache" in {
            prepareCache()

            controller.submitAdditionalProcedureCodes(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(removeAction)).futureValue

            val updatedItem = theCacheModelUpdated.itemBy(itemId)
            updatedItem mustBe defined
            updatedItem.get.procedureCodes mustBe defined
            updatedItem.get.procedureCodes.get.procedureCode mustBe empty
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
            val item = ExportItem(itemId, procedureCodes = Some(ProcedureCodesData(Some("1234"), Seq("111", "123"))))
            withNewCaching(aDeclaration(withType(request.declarationType), withItem(item)))
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
            updatedItem.get.procedureCodes.get.procedureCode.get mustBe "1234"
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
}
