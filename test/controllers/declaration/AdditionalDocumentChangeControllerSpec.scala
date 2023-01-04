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
import forms.common.YesNoAnswer.Yes
import forms.declaration.additionaldocuments.{AdditionalDocument, DocumentWriteOff}
import mock.ErrorHandlerMocks
import models.declaration.AdditionalDocuments
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import utils.ListItem
import views.html.declaration.additionalDocuments.additional_document_change

class AdditionalDocumentChangeControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  val additionalDocumentChangePage = mock[additional_document_change]

  val controller = new AdditionalDocumentChangeController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    additionalDocumentChangePage
  )(ec)

  val itemId = "itemId"
  val existingDocument1 = AdditionalDocument(Some("1000"), None, None, None, None, None, None)
  val existingDocument2 = AdditionalDocument(Some("2000"), None, None, None, None, None, None)
  val documentId = ListItem.createId(0, existingDocument1)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId), withAdditionalDocuments(Yes, existingDocument1, existingDocument2)))))
    when(additionalDocumentChangePage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(additionalDocumentChangePage)
  }

  def theResponseForm: Form[AdditionalDocument] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[AdditionalDocument]])
    verify(additionalDocumentChangePage).apply(any(), any(), formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    await(controller.displayPage(itemId, documentId)(request))
    theResponseForm
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(additionalDocumentChangePage, times(numberOfTimes)).apply(any(), any(), any())(any(), any())

  "AdditionalDocumentChangeController" should {

    "return 200 (OK)" when {
      "display page method is invoked" in {
        val result = controller.displayPage(itemId, documentId)(getRequest())

        status(result) mustBe OK
        verifyPageInvoked()
      }
    }

    "return 400 (BAD_REQUEST) during adding" when {

      "user put incorrect data" in {
        verifyBadRequest(Seq(("documentTypeCode", "12345")))
      }

      "user entered measurement unit without quantity" in {
        verifyBadRequest(Seq(("documentWriteOff.measurementUnit", "KGM")))
      }

      "user entered quantity without measurement unit" in {
        verifyBadRequest(Seq(("documentWriteOff.documentQuantity", "1000")))
      }

      "user entered qualifier without measurement unit" in {
        verifyBadRequest(Seq(("documentWriteOff.qualifier", "A"), ("documentWriteOff.documentQuantity", "1000")))
      }

      "user put duplicated item" in {
        val duplicatedForm = Json.toJson(existingDocument2)
        val result = controller.submitForm(itemId, documentId)(postRequest(duplicatedForm))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
      }

      "user save empty form without new item" in {
        val result = controller.submitForm(itemId, documentId)(postRequestAsFormUrlEncoded())

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
      }

      def verifyBadRequest(incorrectForm: Seq[(String, String)]): HtmlFormat.Appendable = {
        val result = controller.submitForm(itemId, documentId)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user correctly changed document" in {
        val correctForm =
          Json.obj("documentTypeCode" -> "1001", "documentWriteOff.documentQuantity" -> "123", "documentWriteOff.measurementUnit" -> "KGM")
        val result = controller.submitForm(itemId, documentId)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalDocumentsController.displayPage(itemId)
        verifyPageInvoked(0)

        val savedDocuments = theCacheModelUpdated.itemBy(itemId).flatMap(_.additionalDocuments)
        savedDocuments mustBe Some(
          AdditionalDocuments(
            Yes,
            Seq(
              existingDocument1
                .copy(documentTypeCode = Some("1001"), documentWriteOff = Some(DocumentWriteOff(Some("KGM"), Some(BigDecimal("123"))))),
              existingDocument2
            )
          )
        )
      }

      "user does not change document" in {
        val unchangedForm = Json.toJson(existingDocument1)
        val result = controller.submitForm(itemId, documentId)(postRequest(unchangedForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.AdditionalDocumentsController.displayPage(itemId)
        verifyPageInvoked(0)

        val savedDocuments = theCacheModelUpdated.itemBy(itemId).flatMap(_.additionalDocuments)
        savedDocuments mustBe Some(AdditionalDocuments(Yes, Seq(existingDocument1, existingDocument2)))
      }
    }
  }
}
