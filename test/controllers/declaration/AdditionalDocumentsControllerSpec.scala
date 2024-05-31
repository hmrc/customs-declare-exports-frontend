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

import base.{ControllerSpec, MockTaggedCodes}
import forms.common.YesNoAnswer.Yes
import forms.common.{Eori, YesNoAnswer}
import forms.declaration.additionaldocuments.AdditionalDocument
import forms.declaration.authorisationHolder.AuthorisationHolder
import forms.declaration.authorisationHolder.AuthorizationTypeCodes.EXRR
import models.declaration.EoriSource
import models.requests.SessionHelper.errorFixModeSessionKey
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.additionalDocuments.additional_documents

class AdditionalDocumentsControllerSpec extends ControllerSpec with MockTaggedCodes {

  val additionalDocumentsPage = mock[additional_documents]

  val controller = new AdditionalDocumentsController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    mcc,
    taggedAuthCodes,
    additionalDocumentsPage
  )

  val itemId = "itemId"

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration())
    when(additionalDocumentsPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(additionalDocumentsPage)
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(additionalDocumentsPage).apply(any(), formCaptor.capture(), any())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    val item = anItem(withAdditionalDocuments(Yes, additionalDocument))
    withNewCaching(aDeclaration(withItems(item)))
    await(controller.displayPage(item.id)(request))
    theResponseForm
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(additionalDocumentsPage, times(numberOfTimes)).apply(any(), any(), any())(any(), any())

  val additionalDocument = AdditionalDocument(Some("1234"), None, None, None, None, None, None)

  "AdditionalDocumentsController" should {

    "return 200 (OK)" when {
      "display page method is invoked with data in cache" in {
        val item = anItem(withAdditionalDocuments(Yes, additionalDocument))
        withNewCaching(aDeclaration(withItems(item)))

        val result = controller.displayPage(item.id)(getRequest())

        status(result) mustBe OK
        verifyPageInvoked()
      }
    }

    "return 400 (BAD_REQUEST)" when {
      "user provide wrong action" in {
        val requestBody = Json.obj("yesNo" -> "invalid")
        val result = controller.submitForm(itemId)(postRequest(requestBody))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
      }
    }

    "return 303 (SEE_OTHER)" when {

      "there are no documents in the cache" when {

        "the authorisation code does not require additional documents" in {
          val result = controller.displayPage(itemId)(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.AdditionalDocumentsRequiredController.displayPage(itemId)
        }

        "the authorisation code requires additional documents" in {
          val authorisationHolder = AuthorisationHolder(Some("OPO"), Some(Eori("GB123456789012")), Some(EoriSource.OtherEori))
          withNewCaching(aDeclaration(withAuthorisationHolders(authorisationHolder)))

          val result = controller.displayPage(itemId)(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.AdditionalDocumentAddController.displayPage(itemId)
        }

        "the authorisation code does not require additional documents and in error-fix mode" in {
          val authorisationHolder = AuthorisationHolder(Some(EXRR), Some(Eori("GB123456789012")), Some(EoriSource.OtherEori))
          withNewCaching(aDeclaration(withAuthorisationHolders(authorisationHolder)))

          val request = getRequestWithSession(errorFixModeSessionKey -> "true")
          val result = controller.displayPage(itemId)(request)

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.AdditionalDocumentAddController.displayPage(itemId)
        }
      }

      "user submits valid Yes answer" in {
        val item = anItem(withAdditionalDocuments(Yes, additionalDocument))
        withNewCaching(aDeclaration(withItems(item)))

        val requestBody = Json.obj("yesNo" -> "Yes")
        val result = controller.submitForm(itemId)(postRequest(requestBody))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalDocumentAddController.displayPage(itemId)
      }

      "user submits valid Yes answer in error-fix mode" in {
        val item = anItem(withAdditionalDocuments(Yes, additionalDocument))
        withNewCaching(aDeclaration(withItems(item)))

        val requestBody = Json.obj("yesNo" -> "Yes")
        val result = controller.submitForm(itemId)(postRequest(requestBody))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalDocumentAddController.displayPage(itemId)
      }

      "user submits valid No answer" in {
        val item = anItem(withAdditionalDocuments(Yes, additionalDocument))
        withNewCaching(aDeclaration(withItems(item)))

        val requestBody = Json.obj("yesNo" -> "No")
        val result = controller.submitForm(itemId)(postRequest(requestBody))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.ItemsSummaryController.displayItemsSummaryPage
      }
    }
  }
}
