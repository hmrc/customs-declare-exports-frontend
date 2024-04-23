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

import base.ControllerSpec
import controllers.declaration.routes.{CommodityDetailsController, FiscalInformationController}
import forms.common.YesNoAnswer
import forms.declaration.FiscalInformation
import forms.declaration.FiscalInformation.AllowedFiscalInformationAnswers
import mock.{ErrorHandlerMocks, ItemActionMocks}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request, Result}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.fiscalInformation.additional_fiscal_references

import scala.concurrent.Future

class AdditionalFiscalReferencesControllerSpec extends ControllerSpec with ItemActionMocks with ErrorHandlerMocks {

  val additionalFiscalReferencesPage = mock[additional_fiscal_references]

  val controller = new AdditionalFiscalReferencesController(
    mockItemAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    additionalFiscalReferencesPage
  )

  private val item = anItem(withFiscalInformation(), withAdditionalFiscalReferenceData())

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    setupErrorHandler()
    authorizedUser()
    when(additionalFiscalReferencesPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    reset(additionalFiscalReferencesPage)
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(additionalFiscalReferencesPage).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withItem(item)))
    await(controller.displayPage(item.id)(request))
    theResponseForm
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(additionalFiscalReferencesPage, times(numberOfTimes)).apply(any(), any(), any())(any(), any())

  "Additional fiscal references controller" should {

    "be redirected to /commodity-details" when {

      List(None, Some(FiscalInformation(AllowedFiscalInformationAnswers.no))).foreach { fiscalInfo =>
        val infoToPrint = fiscalInfo.fold("None")(_.onwardSupplyRelief)

        s"on landing on the page, the cached value for /fiscal-information is '$infoToPrint'" in {
          withNewCaching(aDeclaration(withItem(item.copy(fiscalInformation = fiscalInfo))))

          val result: Future[Result] = controller.displayPage(item.id)(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe CommodityDetailsController.displayPage(item.id)
        }
      }
    }

    "be redirected to /fiscal-information" when {
      "the declaration has no 'Fiscal References'" in {
        withNewCaching(aDeclaration(withItem(item.copy(additionalFiscalReferencesData = None))))

        val result: Future[Result] = controller.displayPage(item.id)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe FiscalInformationController.displayPage(item.id)
      }
    }

    "return 200 (OK)" when {
      "display page method is invoked with data in cache" in {
        withNewCaching(aDeclaration(withItem(item)))

        val result: Future[Result] = controller.displayPage(item.id)(getRequest())

        status(result) must be(OK)
        verifyPageInvoked()
      }
    }

    "return 400 (BAD_REQUEST)" when {
      "user provide wrong action" in {
        withNewCaching(aDeclaration(withItem(item)))

        val incorrectForm = Json.obj("yesNo" -> "wrong")

        val result = controller.submitForm(item.id)(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user submits valid Yes answer" in {
        withNewCaching(aDeclaration(withItem(item)))

        val requestBody = Json.obj("yesNo" -> "Yes")
        val result = controller.submitForm(item.id)(postRequest(requestBody))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalFiscalReferenceAddController.displayPage(item.id)
      }

      "user submits valid No answer" in {
        withNewCaching(aDeclaration(withItem(item)))

        val requestBody = Json.obj("yesNo" -> "No")
        val result = controller.submitForm(item.id)(postRequest(requestBody))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe CommodityDetailsController.displayPage(item.id)
      }
    }
  }
}
