/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.declaration.additionaldocuments.AdditionalDocument
import mock.ErrorHandlerMocks
import models.Mode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.additionalDocuments.additional_documents

class AdditionalDocumentsControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  val additionalDocumentsPage = mock[additional_documents]

  val controller = new AdditionalDocumentsController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    additionalDocumentsPage
  )

  val itemId = "itemId"

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration())
    when(additionalDocumentsPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(additionalDocumentsPage)
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(additionalDocumentsPage).apply(any(), any(), formCaptor.capture(), any())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    val item = anItem(withAdditionalDocuments(Some(YesNoAnswer.Yes), additionalDocument))
    withNewCaching(aDeclaration(withItems(item)))
    await(controller.displayPage(Mode.Normal, item.id)(request))
    theResponseForm
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(additionalDocumentsPage, times(numberOfTimes)).apply(any(), any(), any(), any())(any(), any())

  val additionalDocument = AdditionalDocument(Some("1234"), None, None, None, None, None, None)

  "AdditionalDocumentsController" should {

    "return 200 (OK)" when {

      "display page method is invoked with data in cache" in {

        val item = anItem(withAdditionalDocuments(Some(YesNoAnswer.Yes), additionalDocument))
        withNewCaching(aDeclaration(withItems(item)))

        val result = controller.displayPage(Mode.Normal, item.id)(getRequest())

        status(result) mustBe OK
        verifyPageInvoked()
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user provide wrong action" in {

        val requestBody = Seq("yesNo" -> "invalid")
        val result = controller.submitForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(requestBody: _*))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
      }
    }

    "return 303 (SEE_OTHER)" when {

      "there are no documents in the cache" in {

        val result = controller.displayPage(Mode.Normal, itemId)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        // TODO. CEDS-3255.
        // If auth code from List1 thePageNavigatedTo mustBe routes.AdditionalDocumentAddController.displayPage(Mode.Normal, itemId) else
        thePageNavigatedTo mustBe routes.AdditionalDocumentsRequiredController.displayPage(Mode.Normal, itemId)
      }

      "user submits valid Yes answer" in {
        val item = anItem(withAdditionalDocuments(Some(YesNoAnswer.Yes), additionalDocument))
        withNewCaching(aDeclaration(withItems(item)))

        val requestBody = Seq("yesNo" -> "Yes")
        val result = controller.submitForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(requestBody: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalDocumentAddController.displayPage(Mode.Normal, itemId)
      }

      "user submits valid No answer" in {
        val item = anItem(withAdditionalDocuments(Some(YesNoAnswer.Yes), additionalDocument))
        withNewCaching(aDeclaration(withItems(item)))

        val requestBody = Seq("yesNo" -> "No")
        val result = controller.submitForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(requestBody: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.ItemsSummaryController.displayItemsSummaryPage(Mode.Normal)
      }
    }
  }
}
