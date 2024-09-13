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
import connectors.CodeListConnector
import controllers.section5.routes.{AdditionalFiscalReferencesController, CommodityDetailsController}
import forms.section5.AdditionalFiscalReference.countryId
import forms.section5.FiscalInformation.AllowedFiscalInformationAnswers
import forms.section5.{AdditionalFiscalReference, AdditionalFiscalReferencesData, FiscalInformation}
import models.DeclarationType.SUPPLEMENTARY
import models.codes.Country
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request, Result}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section5.fiscalInformation.additional_fiscal_reference_add

import scala.collection.immutable.ListMap
import scala.concurrent.Future

class AdditionalFiscalReferencesAddControllerSpec extends ControllerSpec with AuditedControllerSpec {

  val mockAddPage = mock[additional_fiscal_reference_add]
  val mockCodeListConnector = mock[CodeListConnector]

  val controller =
    new AdditionalFiscalReferenceAddController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, mockAddPage)(
      ec,
      mockCodeListConnector,
      auditService
    )

  private val item = anItem(withFiscalInformation())

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    setupErrorHandler()
    authorizedUser()
    when(mockAddPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("PL" -> Country("Poland", "PL")))
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    reset(auditService, mockAddPage, mockCodeListConnector)
  }

  def theResponseForm: Form[AdditionalFiscalReference] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[AdditionalFiscalReference]])
    verify(mockAddPage).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withItem(item)))
    await(controller.displayPage(item.id)(request))
    theResponseForm
  }

  "Additional fiscal references controller" should {

    "be redirected to /commodity-details" when {

      List(None, Some(FiscalInformation(AllowedFiscalInformationAnswers.no))).foreach { fiscalInfo =>
        val infoToPrint = fiscalInfo.fold("None")(_.onwardSupplyRelief)

        s"on landing on the page, the cached value for /fiscal-information is '$infoToPrint'" in {
          withNewCaching(aDeclaration(withItem(item.copy(fiscalInformation = fiscalInfo))))

          val result: Future[Result] = controller.displayPage(item.id)(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe CommodityDetailsController.displayPage(item.id)
          verifyNoAudit()
        }

        s"on submitting the page, the cached value for /fiscal-information is '$infoToPrint'" in {
          withNewCaching(aDeclaration(withItem(item.copy(fiscalInformation = fiscalInfo))))

          val correctForm = Json.obj(countryId -> "PL", "reference" -> "12345")
          val result: Future[Result] = controller.submitForm(item.id)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe CommodityDetailsController.displayPage(item.id)
          verifyNoAudit()
        }
      }
    }

    "return 200 (OK)" when {
      "display page method is invoked" in {
        withNewCaching(aDeclaration(withItem(item)))
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
        withNewCaching(aDeclaration(withItem(item)))

        val incorrectForm = Json.obj(fieldIdOnError(countryId) -> "!@#$", "reference" -> "!@#$")

        val result = controller.submitForm(item.id)(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
        verifyNoAudit()

        val errors = theResponseForm.errors
        errors(0).messages.head mustBe "declaration.additionalFiscalReferences.country.error"
        errors(1).messages.head mustBe "declaration.additionalFiscalReferences.reference.error"
      }

      "user adds duplicated item" in {
        val item = anItem(withFiscalInformation(), withAdditionalFiscalReferenceData())
        withNewCaching(aDeclaration(withItem(item)))

        val duplicatedForm = Json.obj(countryId -> fiscalReference.country, "reference" -> fiscalReference.reference)

        val result = controller.submitForm(item.id)(postRequest(duplicatedForm))

        status(result) must be(BAD_REQUEST)
        verifyNoAudit()

        theResponseForm.errors.head.messages.head mustBe "declaration.additionalFiscalReferences.error.duplicate"
      }

      "user reaches maximum amount of items" in {
        val additionalFiscalReferencesData = AdditionalFiscalReferencesData(Seq.fill(99)(fiscalReference))
        val item = anItem(withFiscalInformation(), withAdditionalFiscalReferenceData(additionalFiscalReferencesData))
        withNewCaching(aDeclaration(withType(SUPPLEMENTARY), withItem(item)))

        val correctForm = Json.obj(countryId -> "PL", "reference" -> "54321")

        val result = controller.submitForm(item.id)(postRequest(correctForm))

        status(result) must be(BAD_REQUEST)
        verifyNoAudit()

        theResponseForm.errors.head.messages.head mustBe "supplementary.limit"
      }
    }

    "return 303 (SEE_OTHER)" when {
      "user correctly adds new item" in {
        withNewCaching(aDeclaration(withItem(item)))

        val correctForm = Json.obj(countryId -> "PL", "reference" -> "12345")

        val result: Future[Result] = controller.submitForm(item.id)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe AdditionalFiscalReferencesController.displayPage(item.id)
        verifyAudit()
      }
    }
  }
}
