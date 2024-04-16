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
import connectors.CodeListConnector
import forms.declaration.AdditionalFiscalReference.countryId
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData}
import mock.{ErrorHandlerMocks, ItemActionMocks}
import models.DeclarationType
import models.codes.Country
import models.declaration.ExportItem
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request, Result}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.fiscalInformation.additional_fiscal_references_add

import scala.collection.immutable.ListMap
import scala.concurrent.Future

class AdditionalFiscalReferencesAddControllerSpec extends ControllerSpec with AuditedControllerSpec with ItemActionMocks with ErrorHandlerMocks {

  val mockAddPage = mock[additional_fiscal_references_add]
  val mockCodeListConnector = mock[CodeListConnector]

  val controller = new AdditionalFiscalReferencesAddController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockAddPage
  )(ec, mockCodeListConnector, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    setupErrorHandler()
    authorizedUser()
    when(mockAddPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("PL" -> Country("Poland", "PL")))
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    reset(mockAddPage, mockCodeListConnector)
  }

  def theResponseForm: Form[AdditionalFiscalReference] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[AdditionalFiscalReference]])
    verify(mockAddPage).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    val item = anItem()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))
    await(controller.displayPage(item.id)(request))
    theResponseForm
  }

  "Additional fiscal references controller" should {
    val item = anItem()

    "return 200 (OK)" when {
      "display page method is invoked" in {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))
        val result: Future[Result] = controller.displayPage(item.id)(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "no values are entered" in {
        withNewCaching(aDeclaration(withItem(item)))

        val incorrectForm = Json.obj(fieldIdOnError(countryId) -> "", "reference" -> "")
        val result = controller.submitForm(item.id)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()

        val errors = theResponseForm.errors
        errors(0).messages.head mustBe "declaration.additionalFiscalReferences.country.empty"
        errors(1).messages.head mustBe "declaration.additionalFiscalReferences.reference.empty"
      }

      "user enter incorrect data" in {
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val incorrectForm = Json.obj(fieldIdOnError(countryId) -> "!@#$", "reference" -> "!@#$")

        val result = controller.submitForm(item.id)(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
        verifyNoAudit()

        val errors = theResponseForm.errors
        errors(0).messages.head mustBe "declaration.additionalFiscalReferences.country.error"
        errors(1).messages.head mustBe "declaration.additionalFiscalReferences.reference.error"
      }

      "user adds duplicated item" in {
        val additionalFiscalReferencesData = AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("PL", "12345")))
        val item = ExportItem("itemId", additionalFiscalReferencesData = Some(additionalFiscalReferencesData))
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val duplicatedForm = Json.obj(countryId -> "PL", "reference" -> "12345")

        val result = controller.submitForm(item.id)(postRequest(duplicatedForm))

        status(result) must be(BAD_REQUEST)
        verifyNoAudit()

        theResponseForm.errors.head.messages.head mustBe "declaration.additionalFiscalReferences.error.duplicate"
      }

      "user reaches maximum amount of items" in {
        val additionalFiscalReferencesData = AdditionalFiscalReferencesData(Seq.fill(99)(AdditionalFiscalReference("PL", "12345")))
        val item = ExportItem("itemId", additionalFiscalReferencesData = Some(additionalFiscalReferencesData))
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val correctForm = Json.obj(countryId -> "PL", "reference" -> "54321")

        val result = controller.submitForm(item.id)(postRequest(correctForm))

        status(result) must be(BAD_REQUEST)
        verifyNoAudit()

        theResponseForm.errors.head.messages.head mustBe "supplementary.limit"
      }
    }

    "return 303 (SEE_OTHER)" when {
      "user correctly adds new item" in {
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val correctForm = Json.obj(countryId -> "PL", "reference" -> "12345")

        val result: Future[Result] = controller.submitForm(item.id)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalFiscalReferencesController.displayPage(item.id)
        verifyAudit()
      }
    }
  }
}
