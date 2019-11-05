/*
 * Copyright 2019 HM Revenue & Customs
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

import controllers.declaration.DocumentsProducedController
import controllers.util.Remove
import forms.declaration.additionaldocuments.DocumentsProduced
import models.{DeclarationType, Mode}
import models.declaration.DocumentsProducedData
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import unit.mock.ErrorHandlerMocks
import views.html.declaration.documents_produced

class DocumentProducedControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  val mockDocumentProducedPage = mock[documents_produced]

  val controller = new DocumentsProducedController(
    mockAuthAction,
    mockJourneyAction,
    mockErrorHandler,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockDocumentProducedPage
  )(ec)

  val itemId = "itemId"

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
    when(mockDocumentProducedPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockDocumentProducedPage)
  }

  val documentsProduced = DocumentsProduced(Some("1234"), None, None, None, None, None, None)

  "Document Produced controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in {

        val result = controller.displayPage(Mode.Normal, itemId)(getRequest())

        status(result) mustBe OK
        verify(mockDocumentProducedPage, times(1)).apply(refEq(Mode.Normal), any(), any(), any())(any(), any())
      }

      "display page method is invoked with data in cache" in {

        val item = anItem(withDocumentsProduced(documentsProduced))
        withNewCaching(aDeclaration(withItems(item)))

        val result = controller.displayPage(Mode.Normal, item.id)(getRequest())

        status(result) mustBe OK
        verify(mockDocumentProducedPage, times(1)).apply(refEq(Mode.Normal), any(), any(), any())(any(), any())
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user provide wrong action" in {

        val wrongAction = ("WrongAction", "")

        val result = controller.saveForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(wrongAction))

        status(result) mustBe BAD_REQUEST
        verify(mockDocumentProducedPage, times(1)).apply(refEq(Mode.Normal), any(), any(), any())(any(), any())
      }
    }

    "return 400 (BAD_REQUEST) during adding" when {

      "user put incorrect data" in {

        val incorrectForm = Seq(("documentTypeCode", "12345"), addActionUrlEncoded())

        val result = controller.saveForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockDocumentProducedPage, times(1)).apply(refEq(Mode.Normal), any(), any(), any())(any(), any())
      }

      "user put duplicated item" in {

        withNewCaching(aDeclaration(withItems(anItem(withItemId("itemId"), withDocumentsProduced(documentsProduced)))))

        val duplicatedForm = Seq(("documentTypeCode", "1234"), addActionUrlEncoded())

        val result = controller.saveForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockDocumentProducedPage, times(1)).apply(refEq(Mode.Normal), any(), any(), any())(any(), any())
      }

      "user reach maximum amount of items" in {

        withNewCaching(
          aDeclaration(
            withItems(
              anItem(
                withItemId("itemId"),
                withDocumentsProducedData(DocumentsProducedData(Seq.fill(DocumentsProducedData.maxNumberOfItems)(documentsProduced)))
              )
            )
          )
        )

        val correctForm = Seq(("documentTypeCode", "4321"), addActionUrlEncoded())

        val result = controller.saveForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockDocumentProducedPage, times(1)).apply(refEq(Mode.Normal), any(), any(), any())(any(), any())
      }
    }

    "return 400 (BAD_REQUEST) during saving" when {

      "user put incorrect data" in {

        val incorrectForm = Seq(("documentTypeCode", "12345"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockDocumentProducedPage, times(1)).apply(refEq(Mode.Normal), any(), any(), any())(any(), any())
      }

      "user put duplicated item" in {

        withNewCaching(aDeclaration(withItems(anItem(withItemId("itemId"), withDocumentsProduced(documentsProduced)))))

        val duplicatedForm = Seq(("documentTypeCode", "1234"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockDocumentProducedPage, times(1)).apply(refEq(Mode.Normal), any(), any(), any())(any(), any())
      }

      "user reach maximum amount of items" in {

        withNewCaching(
          aDeclaration(
            withItems(
              anItem(
                withItemId("itemId"),
                withDocumentsProducedData(DocumentsProducedData(Seq.fill(DocumentsProducedData.maxNumberOfItems)(documentsProduced)))
              )
            )
          )
        )

        val correctForm = Seq(("documentTypeCode", "4321"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockDocumentProducedPage, times(1)).apply(refEq(Mode.Normal), any(), any(), any())(any(), any())
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user correctly add new item" in {

        val correctForm = Seq(("documentTypeCode", "1234"), addActionUrlEncoded())

        val result = controller.saveForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) mustBe SEE_OTHER
        verify(mockDocumentProducedPage, times(0)).apply(refEq(Mode.Normal), any(), any(), any())(any(), any())
      }

      "user save correct data" in {

        val correctForm = Seq(("documentTypeCode", "1234"), saveAndContinueActionUrlEncoded)

        val result = controller.saveForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.ItemsSummaryController.displayPage()
        verify(mockDocumentProducedPage, times(0)).apply(refEq(Mode.Normal), any(), any(), any())(any(), any())
      }

      "user save empty form without new item" in {

        val result =
          controller.saveForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(saveAndContinueActionUrlEncoded))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.ItemsSummaryController.displayPage()
        verify(mockDocumentProducedPage, times(0)).apply(refEq(Mode.Normal), any(), any(), any())(any(), any())
      }

      "user remove existing item" in {

        withNewCaching(aDeclaration(withItems(anItem(withDocumentsProduced(documentsProduced)))))

        val removeAction = (Remove.toString, "0")

        val result = controller.saveForm(Mode.Normal, itemId)(postRequestAsFormUrlEncoded(removeAction))

        status(result) mustBe OK
        verify(mockDocumentProducedPage, times(1)).apply(refEq(Mode.Normal), any(), any(), any())(any(), any())
      }
    }
  }
}
