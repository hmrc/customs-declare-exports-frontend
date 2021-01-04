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

package unit.controllers.declaration

import controllers.declaration.DocumentsProducedChangeController
import forms.declaration.additionaldocuments.{DocumentWriteOff, DocumentsProduced}
import models.Mode
import models.declaration.DocumentsProducedData
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import utils.ListItem
import views.html.declaration.documentsProduced.documents_produced_change

class DocumentsProducedChangeControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  val mockDocumentProducedChangePage = mock[documents_produced_change]

  val controller = new DocumentsProducedChangeController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockDocumentProducedChangePage
  )(ec)

  val itemId = "itemId"
  val existingDocument1 = DocumentsProduced(Some("1000"), None, None, None, None, None, None)
  val existingDocument2 = DocumentsProduced(Some("2000"), None, None, None, None, None, None)
  val documentId = ListItem.createId(0, existingDocument1)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId), withDocumentsProduced(existingDocument1, existingDocument2)))))
    when(mockDocumentProducedChangePage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockDocumentProducedChangePage)
  }

  def theResponseForm: Form[DocumentsProduced] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[DocumentsProduced]])
    verify(mockDocumentProducedChangePage).apply(any(), any(), any(), formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    await(controller.displayPage(Mode.Normal, itemId, documentId)(request))
    theResponseForm
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1) =
    verify(mockDocumentProducedChangePage, times(numberOfTimes)).apply(any(), any(), any(), any())(any(), any())

  "Document Produced controller" should {

    "return 200 (OK)" when {

      "display page method is invoked" in {

        val result = controller.displayPage(Mode.Normal, itemId, documentId)(getRequest())

        status(result) mustBe OK
        verifyPageInvoked()
      }
    }

    "return 400 (BAD_REQUEST) during adding" when {

      def verifyBadRequest(incorrectForm: Seq[(String, String)]) = {
        val result = controller.submitForm(Mode.Normal, itemId, documentId)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
      }

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
        val result = controller.submitForm(Mode.Normal, itemId, documentId)(postRequest(duplicatedForm))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
      }

    }

    "return 303 (SEE_OTHER)" when {

      "user correctly changed document" in {

        val correctForm = Seq(("documentTypeCode", "1001"), ("documentWriteOff.documentQuantity", "123"), ("documentWriteOff.measurementUnit", "KGM"))
        val result = controller.submitForm(Mode.Normal, itemId, documentId)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DocumentsProducedController.displayPage(Mode.Normal, itemId)
        verifyPageInvoked(0)

        val savedDocuments = theCacheModelUpdated.itemBy(itemId).flatMap(_.documentsProducedData)
        savedDocuments mustBe Some(
          DocumentsProducedData(
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
        val result = controller.submitForm(Mode.Normal, itemId, documentId)(postRequest(unchangedForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DocumentsProducedController.displayPage(Mode.Normal, itemId)
        verifyPageInvoked(0)

        val savedDocuments = theCacheModelUpdated.itemBy(itemId).flatMap(_.documentsProducedData)
        savedDocuments mustBe Some(DocumentsProducedData(Seq(existingDocument1, existingDocument2)))
      }

      "user save empty form without new item" in {

        val result = controller.submitForm(Mode.Normal, itemId, documentId)(postRequestAsFormUrlEncoded())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DocumentsProducedController.displayPage(Mode.Normal, itemId)
        verifyPageInvoked(0)

        verifyTheCacheIsUnchanged
      }

    }
  }
}
