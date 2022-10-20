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
import forms.common.YesNoAnswer
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData}
import mock.{ErrorHandlerMocks, ItemActionMocks}
import models.declaration.ExportItem
import models.{DeclarationType, ExportsDeclaration}
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

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    setupErrorHandler()
    authorizedUser()
    when(additionalFiscalReferencesPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    reset(additionalFiscalReferencesPage)
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(additionalFiscalReferencesPage).apply(any(), any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    val item = anItem(withAdditionalFiscalReferenceData(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("GB", "123124124")))))
    withNewCaching(aDeclaration(withItem(item)))
    await(controller.displayPage(item.id)(request))
    theResponseForm
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1) =
    verify(additionalFiscalReferencesPage, times(numberOfTimes)).apply(any(), any(), any(), any())(any(), any())

  "Additional fiscal references controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with data in cache" in {

        val itemCacheData =
          ExportItem("itemId", additionalFiscalReferencesData = Some(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("PL", "12345")))))
        val cachedData: ExportsDeclaration =
          aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(itemCacheData))
        withNewCaching(cachedData)

        val result: Future[Result] = controller.displayPage(itemCacheData.id)(getRequest())

        status(result) must be(OK)
        verifyPageInvoked()
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user provide wrong action" in {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val wrongAction: Seq[(String, String)] = Seq(("country", "PL"), ("reference", "12345"), ("WrongAction", ""))

        val result: Future[Result] =
          controller.submitForm(item.id)(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user submits valid Yes answer" in {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val requestBody = Json.obj("yesNo" -> "Yes")
        val result = controller.submitForm(item.id)(postRequest(requestBody))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalFiscalReferencesAddController.displayPage(item.id)
      }

      "user submits valid Yes answer in error-fix mode" in {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val requestBody = Json.obj("yesNo" -> "Yes")
        val result = controller.submitForm(Mode.ErrorFix, item.id)(postRequest(requestBody))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalFiscalReferencesAddController.displayPage(Mode.ErrorFix, item.id)
      }

      "user submits valid No answer" in {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val requestBody = Json.obj("yesNo" -> "No")
        val result = controller.submitForm(item.id)(postRequest(requestBody))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.CommodityDetailsController.displayPage(item.id)
      }

    }
  }
}
