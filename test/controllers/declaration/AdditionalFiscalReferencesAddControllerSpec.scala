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
import connectors.CodeListConnector
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData}
import mock.{ErrorHandlerMocks, ItemActionMocks}
import models.DeclarationType
import models.codes.Country
import models.declaration.ExportItem
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request, Result}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.fiscalInformation.additional_fiscal_references_add

import scala.collection.immutable.ListMap
import scala.concurrent.Future

class AdditionalFiscalReferencesAddControllerSpec extends ControllerSpec with ItemActionMocks with ErrorHandlerMocks {

  val mockAddPage = mock[additional_fiscal_references_add]
  val mockCodeListConnector = mock[CodeListConnector]

  val controller = new AdditionalFiscalReferencesAddController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockAddPage
  )(ec, mockCodeListConnector)

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

    "return 200 (OK)" when {

      "display page method is invoked" in {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))
        val result: Future[Result] = controller.displayPage(item.id)(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST) during saving" when {

      "user put incorrect data" in {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val incorrectForm = Seq(("country", "PL"), ("reference", "!@#$"), saveAndContinueActionUrlEncoded)

        val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user adds duplicated item" in {
        val itemCacheData =
          ExportItem("itemId", additionalFiscalReferencesData = Some(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("PL", "12345")))))
        val cachedData = aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(itemCacheData))
        withNewCaching(cachedData)

        val duplicatedForm: Seq[(String, String)] =
          Seq(("country", "PL"), ("reference", "12345"), saveAndContinueActionUrlEncoded)

        val result = controller.submitForm(itemCacheData.id)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) must be(BAD_REQUEST)
      }

      "user reaches maximum amount of items" in {
        val itemCacheData = ExportItem(
          "itemId",
          additionalFiscalReferencesData = Some(AdditionalFiscalReferencesData(Seq.fill(99)(AdditionalFiscalReference("PL", "12345"))))
        )
        val cachedData = aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(itemCacheData))
        withNewCaching(cachedData)

        val form: Seq[(String, String)] =
          Seq(("country", "PL"), ("reference", "54321"), saveAndContinueActionUrlEncoded)

        val result = controller.submitForm(itemCacheData.id)(postRequestAsFormUrlEncoded(form: _*))
        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {
      "user correctly adds new item" in {
        val item = anItem()
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withItem(item)))

        val correctForm: Seq[(String, String)] =
          Seq(("country", "PL"), ("reference", "12345"), saveAndContinueActionUrlEncoded)

        val result: Future[Result] =
          controller.submitForm(item.id)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalFiscalReferencesController.displayPage(item.id)
      }
    }
  }
}
