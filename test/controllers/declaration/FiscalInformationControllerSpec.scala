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
import controllers.declaration.routes.{AdditionalFiscalReferencesController, CommodityDetailsController}
import forms.declaration.FiscalInformation
import forms.declaration.FiscalInformation.AllowedFiscalInformationAnswers._
import models.DeclarationType
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request, Result}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.fiscalInformation.fiscal_information

import scala.concurrent.Future

class FiscalInformationControllerSpec extends ControllerSpec with AuditedControllerSpec with OptionValues {

  private val mockFiscalInformationPage = mock[fiscal_information]

  val controller = new FiscalInformationController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    mcc,
    mockFiscalInformationPage
  )(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
    when(mockFiscalInformationPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(auditService, mockFiscalInformationPage)
  }

  def theResponseForm: Form[FiscalInformation] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[FiscalInformation]])
    verify(mockFiscalInformationPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    val item = anItem(withProcedureCodes())
    withNewCaching(aDeclaration(withItem(item)))
    await(controller.displayPage(item.id)(request))
    theResponseForm
  }

  private def verifyPageAccessed: HtmlFormat.Appendable =
    verify(mockFiscalInformationPage, times(1)).apply(any(), any())(any(), any())

  "Fiscal Information controller" should {

    "be redirected to /commodity-details" when {
      val item = anItem(withProcedureCodes(Some("1040")))

      s"on landing on the page, the cached 'Procedure Code' is not for 'Onward Supply Relief'" in {
        withNewCaching(aDeclaration(withItem(item)))

        val result: Future[Result] = controller.displayPage(item.id)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe CommodityDetailsController.displayPage(item.id)
        verifyNoAudit()
      }

      s"on submitting the page, the cached 'Procedure Code' is not for 'Onward Supply Relief'" in {
        withNewCaching(aDeclaration(withItem(item)))

        val correctForm = Json.toJson(fiscalInformation)
        val result: Future[Result] = controller.saveFiscalInformation(item.id)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe CommodityDetailsController.displayPage(item.id)
        verifyNoAudit()
      }
    }

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {
        val item = anItem(withProcedureCodes())
        withNewCaching(aDeclaration(withItems(item)))

        val result = controller.displayPage(item.id)(getRequest())

        status(result) mustBe OK
        verifyPageAccessed

        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" in {
        val item = anItem(withProcedureCodes(), withFiscalInformation())
        withNewCaching(aDeclaration(withItems(item)))

        val result = controller.displayPage(item.id)(getRequest())

        status(result) mustBe OK
        verifyPageAccessed

        theResponseForm.value.value.onwardSupplyRelief mustBe yes
      }
    }

    "return 400 (BAD_REQUEST)" when {
      "form is incorrect" in {
        val item = anItem(withProcedureCodes())
        withNewCaching(aDeclaration(withItems(item)))

        val incorrectForm = Json.toJson(FiscalInformation("IncorrectValue"))
        val result = controller.saveFiscalInformation(item.id)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyPageAccessed
        verifyNoAudit()
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user answer yes with no fiscal references in cache yet" in {
        val item = anItem(withProcedureCodes())
        withNewCaching(aDeclaration(withItems(item)))

        val correctForm = Json.toJson(fiscalInformation)
        val result = controller.saveFiscalInformation(item.id)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalFiscalReferenceAddController.displayPage(item.id)
        verifyAudit()
      }

      "user answer yes with fiscal references already in cache" in {
        val item = anItem(withProcedureCodes(), withFiscalInformation(), withAdditionalFiscalReferenceData())
        withNewCaching(aDeclaration(withItems(item)))

        val correctForm = Json.toJson(fiscalInformation)
        val result = controller.saveFiscalInformation(item.id)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe AdditionalFiscalReferencesController.displayPage(item.id)
        verifyAudit()

        theCacheModelUpdated.itemBy(item.id).flatMap(_.additionalFiscalReferencesData) mustBe Some(fiscalReferences)
      }

      "user answer no" in {
        val item = anItem(withProcedureCodes(), withFiscalInformation(), withAdditionalFiscalReferenceData())
        withNewCaching(aDeclaration(withItems(item)))

        val correctForm = Json.toJson(FiscalInformation("No"))
        val result = controller.saveFiscalInformation(item.id)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe CommodityDetailsController.displayPage(item.id)
        verifyAudit()

        theCacheModelUpdated.itemBy(item.id).flatMap(_.additionalFiscalReferencesData) mustBe None
      }
    }
  }
}
