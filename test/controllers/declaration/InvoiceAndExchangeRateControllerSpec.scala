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
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.InvoiceAndExchangeRate
import models.DeclarationType._
import models.Mode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.{Html, HtmlFormat}
import views.html.declaration.invoice_and_exchange_rate

class InvoiceAndExchangeRateControllerSpec extends ControllerSpec with OptionValues {

  val mockInvoiceAndExchangeRatePage = mock[invoice_and_exchange_rate]

  def theResponseForm: Form[InvoiceAndExchangeRate] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[InvoiceAndExchangeRate]])
    verify(mockInvoiceAndExchangeRatePage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(mockInvoiceAndExchangeRatePage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    authorizedUser()
  }

  override protected def afterEach(): Unit = {
    Mockito.reset(mockInvoiceAndExchangeRatePage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
  }

  val controller = new InvoiceAndExchangeRateController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    stubMessagesControllerComponents(),
    mockInvoiceAndExchangeRatePage,
    mockExportsCacheService
  )(ec)

  val withoutExchange = InvoiceAndExchangeRate(
    totalAmountInvoiced = "100",
    totalAmountInvoicedCurrency = Some("GBP"),
    agreedExchangeRate = YesNoAnswers.no,
    exchangeRate = None
  )

  val withExchange = InvoiceAndExchangeRate(
    totalAmountInvoiced = "100",
    totalAmountInvoicedCurrency = Some("GBP"),
    agreedExchangeRate = YesNoAnswers.yes,
    exchangeRate = Some("1")
  )

  def verifyPage(numberOfTimes: Int = 1): Html =
    verify(mockInvoiceAndExchangeRatePage, times(numberOfTimes)).apply(any(), any())(any(), any())

  "Total Number of Items controller" should {

    implicit val format = Json.format[InvoiceAndExchangeRate]

    onJourney(STANDARD, SUPPLEMENTARY) { request =>
      "display page method is invoked and cache is empty" in {
        withNewCaching(request.cacheModel)
        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        verifyPage()

        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" in {
        withNewCaching(aDeclaration(withType(request.declarationType), withTotalNumberOfItems(withoutExchange)))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        verifyPage()

        theResponseForm.value mustNot be(empty)
      }

      "return 400 (BAD_REQUEST) when form is incorrect" in {
        withNewCaching(request.cacheModel)
        val incorrectForm = Json.toJson(InvoiceAndExchangeRate("", None, "", Some("abc")))
        val result = controller.saveNoOfItems(Mode.Normal)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyPage()
      }

      "return 303 (SEE_OTHER) when information provided by user are correct" in {
        withNewCaching(request.cacheModel)
        val correctForm = Json.toJson(withoutExchange)
        val result = controller.saveNoOfItems(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.TotalPackageQuantityController.displayPage()
        verifyPage(0)
      }

      "empty fixed rate of exchange value from cache when 'No' is submitted" in {
        withNewCaching(aDeclaration(withType(request.declarationType), withTotalNumberOfItems(withExchange)))
        val correctForm = Json.toJson(withoutExchange)
        await(controller.saveNoOfItems(Mode.Normal)(postRequest(correctForm)))

        theCacheModelUpdated.totalNumberOfItems.get.exchangeRate mustBe None
      }
    }

    onJourney(SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
      "redirect 303 (See Other) to start" in {
        withNewCaching(request.cacheModel)

        val result = controller.displayPage(Mode.Normal).apply(getRequest(request.cacheModel))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) must contain(controllers.routes.RootController.displayPage().url)
      }
    }
  }
}