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
import controllers.declaration.routes.PreviousDocumentsSummaryController
import forms.declaration.{Document, NatureOfTransaction}
import models.DeclarationType
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.nature_of_transaction

class NatureOfTransactionControllerSpec extends ControllerSpec with AuditedControllerSpec with OptionValues {

  val mockNatureOfTransactionPage = mock[nature_of_transaction]

  val controller =
    new NatureOfTransactionController(mockAuthAction, mockJourneyAction, navigator, mcc, mockNatureOfTransactionPage, mockExportsCacheService)(
      ec,
      auditService
    )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
    when(mockNatureOfTransactionPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockNatureOfTransactionPage)
  }

  def theResponseForm: Form[NatureOfTransaction] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[NatureOfTransaction]])
    verify(mockNatureOfTransactionPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    await(controller.displayPage(request))
    theResponseForm
  }

  "Nature of Transaction controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {
        val result = controller.displayPage(getRequest())

        status(result) mustBe OK
        verify(mockNatureOfTransactionPage, times(1)).apply(any())(any(), any())

        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" in {
        val natureType = "1"
        withNewCaching(aDeclaration(withNatureOfTransaction(natureType)))

        val result = controller.displayPage(getRequest())

        status(result) mustBe OK
        verify(mockNatureOfTransactionPage, times(1)).apply(any())(any(), any())

        theResponseForm.value.value.natureType mustBe natureType
      }
    }

    "return 400 (BAD_REQUEST)" when {
      "form is incorrect" in {
        val incorrectForm = Json.toJson(NatureOfTransaction("incorrect"))

        val result = controller.saveTransactionType()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verify(mockNatureOfTransactionPage, times(1)).apply(any())(any(), any())
        verifyNoAudit()
      }
    }

    "return 303 (SEE_OTHER)" when {
      "user provided correct information" in {
        withNewCaching(aDeclaration(withPreviousDocuments(Document("MCR", "reference", None))))
        val correctForm = Json.toJson(NatureOfTransaction("1"))

        val result = controller.saveTransactionType()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe PreviousDocumentsSummaryController.displayPage

        verify(mockNatureOfTransactionPage, times(0)).apply(any())(any(), any())
        verifyAudit()
      }
    }
  }
}
