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
import forms.common.YesNoAnswer.Yes
import forms.declaration.additionaldocuments.AdditionalDocument
import models.declaration.AdditionalDocuments
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import utils.ListItem
import views.html.declaration.additionalDocuments.additional_document_remove

class AdditionalDocumentRemoveControllerSpec extends ControllerSpec with OptionValues {

  val additionalDocumentRemovePage = mock[additional_document_remove]

  val controller =
    new AdditionalDocumentRemoveController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      additionalDocumentRemovePage
    )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(additionalDocumentRemovePage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(additionalDocumentRemovePage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withItem(itemWithDocument)))
    await(controller.displayPage(itemId, documentId)(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(additionalDocumentRemovePage).apply(any(), any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  def theDocumentProduced: AdditionalDocument = {
    val captor = ArgumentCaptor.forClass(classOf[AdditionalDocument])
    verify(additionalDocumentRemovePage).apply(any(), any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def verifyRemovePageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(additionalDocumentRemovePage, times(numberOfTimes)).apply(any(), any(), any(), any())(any(), any())

  val additionalDocument = AdditionalDocument(Some("1234"), None, None, None, None, None, None)
  val documentId = ListItem.createId(0, additionalDocument)
  val itemId = "itemId"
  val itemWithDocument = anItem(withItemId(itemId), withAdditionalDocuments(Yes, additionalDocument))

  "AdditionalDocumentRemoveController" must {

    onEveryDeclarationJourney() { request =>

      "return 200 (OK)" that {
        "display page method is invoked" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(itemWithDocument)))

          val result = controller.displayPage(itemId, documentId)(getRequest())

          status(result) mustBe OK
          verifyRemovePageInvoked()

          theDocumentProduced mustBe additionalDocument
        }

      }

      "return 400 (BAD_REQUEST)" when {
        "user submits an invalid answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(itemWithDocument)))

          val requestBody = Seq("yesNo" -> "invalid")
          val result = controller.submitForm(itemId, documentId)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyRemovePageInvoked()
        }

      }

      "return 303 (SEE_OTHER)" when {

        "requested document id invalid" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(itemWithDocument)))

          val result = controller.displayPage(itemId, "doc-id")(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.AdditionalDocumentsController.displayPage(itemId)
        }

        "user submits 'Yes' answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(itemWithDocument)))

          val requestBody = Seq("yesNo" -> "Yes")
          val result = controller.submitForm(itemId, documentId)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.AdditionalDocumentsController.displayPage(itemId)

          theCacheModelUpdated.itemBy(itemId).flatMap(_.additionalDocuments) mustBe Some(AdditionalDocuments(None, Seq.empty))
        }

        "user submits 'No' answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(itemWithDocument)))

          val requestBody = Seq("yesNo" -> "No")
          val result = controller.submitForm(itemId, documentId)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.AdditionalDocumentsController.displayPage(itemId)

          verifyTheCacheIsUnchanged()
        }
      }
    }
  }
}
