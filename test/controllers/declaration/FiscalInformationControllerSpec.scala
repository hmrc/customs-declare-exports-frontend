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
import forms.declaration.FiscalInformation.AllowedFiscalInformationAnswers._
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData, FiscalInformation}
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.fiscalInformation.fiscal_information

class FiscalInformationControllerSpec extends ControllerSpec with OptionValues {

  private val mockFiscalInformationPage = mock[fiscal_information]

  val controller = new FiscalInformationController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockFiscalInformationPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
    when(mockFiscalInformationPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockFiscalInformationPage)
  }

  val itemId = "itemId"

  def theResponseForm: Form[FiscalInformation] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[FiscalInformation]])
    verify(mockFiscalInformationPage).apply(any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    await(controller.displayPage(Mode.Normal, itemId, fastForward = false)(request))
    theResponseForm
  }

  private def verifyPageAccessed(numberOfTimes: Int) =
    verify(mockFiscalInformationPage, times(numberOfTimes)).apply(any(), any(), any())(any(), any())

  "Fiscal Information controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {

        val result = controller.displayPage(Mode.Normal, itemId, fastForward = false)(getRequest())

        status(result) mustBe OK
        verify(mockFiscalInformationPage, times(1)).apply(any(), any(), any())(any(), any())

        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" in {
        val item = anItem(withFiscalInformation(FiscalInformation(yes)))
        withNewCaching(aDeclaration(withItems(item)))

        val result = controller.displayPage(Mode.Normal, item.id, fastForward = false)(getRequest())

        status(result) mustBe OK
        verifyPageAccessed(1)

        theResponseForm.value.value.onwardSupplyRelief mustBe yes
      }

      "mode is Change" in {
        val item = anItem(
          withFiscalInformation(FiscalInformation(yes)),
          withAdditionalFiscalReferenceData(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("GB", "12345"))))
        )
        withNewCaching(aDeclaration(withItems(item)))

        val result = controller.displayPage(Mode.Change, item.id, fastForward = false)(getRequest())

        status(result) mustBe OK
        verifyPageAccessed(1)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {

        val incorrectForm = Json.toJson(FiscalInformation("IncorrectValue"))

        val result = controller.saveFiscalInformation(Mode.Normal, itemId)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyPageAccessed(1)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user answer yes" in {

        val correctForm = Json.toJson(FiscalInformation("Yes"))

        val result = controller.saveFiscalInformation(Mode.Normal, itemId)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalFiscalReferencesAddController
          .displayPage(Mode.Normal, itemId)
        verifyPageAccessed(0)
      }

      "user answer no" in {

        val correctForm = Json.toJson(FiscalInformation("No"))

        val result = controller.saveFiscalInformation(Mode.Normal, itemId)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.CommodityDetailsController.displayPage(Mode.Normal, itemId)
        verifyPageAccessed(0)
      }

      "user comes back from Commodity details and is 'fast-forwarded' to see existing additional fiscal references page" in {

        val item = anItem(
          withFiscalInformation(FiscalInformation(yes)),
          withAdditionalFiscalReferenceData(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("GB", "12"))))
        )
        withNewCaching(aDeclaration(withItems(item)))

        val result = controller.displayPage(Mode.Normal, item.id, fastForward = true)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalFiscalReferencesController.displayPage(Mode.Normal, item.id)
        verifyPageAccessed(0)
      }

      "user comes back from Commodity details and is 'fast-forwarded' to see Procedure Code page due to ineligible procedure code" in {

        val item = anItem(withProcedureCodes(Some("1000"), Seq("000")))
        withNewCaching(aDeclaration(withItems(item)))

        val result = controller.displayPage(Mode.Normal, item.id, fastForward = true)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalProcedureCodesController.displayPage(Mode.Normal, item.id)
        verifyPageAccessed(0)
      }

      "user navigates to Fiscal Information page with additionalFiscalReferencesData in cache and mode is Normal" in {
        val item = anItem(
          withFiscalInformation(FiscalInformation(yes)),
          withAdditionalFiscalReferenceData(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("GB", "12345"))))
        )
        withNewCaching(aDeclaration(withItems(item)))

        val result = controller.displayPage(Mode.Normal, item.id, fastForward = false)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalFiscalReferencesController.displayPage(Mode.Normal, item.id)
        verifyPageAccessed(0)
      }
    }
  }
}
