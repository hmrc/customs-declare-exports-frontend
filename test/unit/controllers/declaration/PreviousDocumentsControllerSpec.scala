/*
 * Copyright 2020 HM Revenue & Customs
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

import controllers.declaration.PreviousDocumentsController
import controllers.util.Remove
import forms.declaration.{Document, PreviousDocumentsData}
import models.{DeclarationType, Mode}
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
import views.html.declaration.previous_documents

class PreviousDocumentsControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  val mockPreviousDocumentsPage = mock[previous_documents]

  val controller = new PreviousDocumentsController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    mockErrorHandler,
    stubMessagesControllerComponents(),
    mockPreviousDocumentsPage,
    mockExportsCacheService
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
    when(mockPreviousDocumentsPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockPreviousDocumentsPage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    await(controller.displayPage(Mode.Normal)(request))
    theResponse._1
  }

  def theResponse: (Form[Document], Seq[Document]) = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[Document]])
    val dataCaptor = ArgumentCaptor.forClass(classOf[Seq[Document]])
    verify(mockPreviousDocumentsPage).apply(any(), any(), formCaptor.capture(), dataCaptor.capture())(any(), any())
    (formCaptor.getValue, dataCaptor.getValue)
  }

  "Previous Documents controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in {

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        verify(mockPreviousDocumentsPage, times(1)).apply(any(), any(), any(), any())(any(), any())

        val (responseForm, responseSeq) = theResponse
        responseForm.value mustBe empty
        responseSeq mustBe empty
      }

      "display page method is invoked with data in cache" in {

        val document = Document("X", "355", "reference", Some("123"))
        withNewCaching(aDeclaration(withPreviousDocuments(document)))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        verify(mockPreviousDocumentsPage, times(1)).apply(any(), any(), any(), any())(any(), any())

        val (responseForm, responseSeq) = theResponse
        responseForm.value mustBe empty
        responseSeq mustBe Seq(document)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user provide wrong action" in {

        val wrongAction = Seq(
          ("documentCategory", "X"),
          ("documentType", "355"),
          ("documentReference", "reference"),
          ("goodsItemIdentifier", ""),
          ("WrongAction", "")
        )

        val result = controller.savePreviousDocuments(Mode.Normal)(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockPreviousDocumentsPage, times(1)).apply(any(), any(), any(), any())(any(), any())
      }
    }

    "return 400 (BAD_REQUEST) during adding" when {

      "user put incorrect data" in {

        val incorrectForm = Seq(
          ("documentCategory", "incorrect"),
          ("documentType", "355"),
          ("documentReference", "reference"),
          ("goodsItemIdentifier", ""),
          addActionUrlEncoded()
        )

        val result = controller.savePreviousDocuments(Mode.Normal)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockPreviousDocumentsPage, times(1)).apply(any(), any(), any(), any())(any(), any())
      }

      "user put duplicated item" in {

        val document = Document("X", "355", "reference", Some("123"))
        withNewCaching(aDeclaration(withPreviousDocuments(document)))

        val duplicatedForm = Seq(
          ("documentCategory", "X"),
          ("documentType", "355"),
          ("documentReference", "reference"),
          ("goodsItemIdentifier", "123"),
          addActionUrlEncoded()
        )

        val result = controller.savePreviousDocuments(Mode.Normal)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockPreviousDocumentsPage, times(1)).apply(any(), any(), any(), any())(any(), any())
      }

      "user reach maximum amount of items" in {

        val document = Document("X", "355", "reference", Some("123"))
        withNewCaching(aDeclaration(withPreviousDocuments(PreviousDocumentsData(Seq.fill(PreviousDocumentsData.maxAmountOfItems)(document)))))

        val correctForm = Seq(
          ("documentCategory", "X"),
          ("documentType", "355"),
          ("documentReference", "reference"),
          ("goodsItemIdentifier", ""),
          addActionUrlEncoded()
        )

        val result = controller.savePreviousDocuments(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockPreviousDocumentsPage, times(1)).apply(any(), any(), any(), any())(any(), any())
      }
    }

    "return 400 (BAD_REQUEST) during saving" when {

      "user put incorrect data" in {

        val incorrectForm = Seq(
          ("documentCategory", "incorrect"),
          ("documentType", "355"),
          ("documentReference", "reference"),
          ("goodsItemIdentifier", ""),
          saveAndContinueActionUrlEncoded
        )

        val result = controller.savePreviousDocuments(Mode.Normal)(postRequestAsFormUrlEncoded(incorrectForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockPreviousDocumentsPage, times(1)).apply(any(), any(), any(), any())(any(), any())
      }

      "user put duplicated item" in {

        val document = Document("X", "355", "reference", Some("123"))
        withNewCaching(aDeclaration(withPreviousDocuments(document)))

        val duplicatedForm = Seq(
          ("documentCategory", "X"),
          ("documentType", "355"),
          ("documentReference", "reference"),
          ("goodsItemIdentifier", "123"),
          saveAndContinueActionUrlEncoded
        )

        val result = controller.savePreviousDocuments(Mode.Normal)(postRequestAsFormUrlEncoded(duplicatedForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockPreviousDocumentsPage, times(1)).apply(any(), any(), any(), any())(any(), any())
      }

      "user reach maximum amount of items" in {

        val document = Document("X", "355", "reference", Some("123"))
        withNewCaching(aDeclaration(withPreviousDocuments(PreviousDocumentsData(Seq.fill(PreviousDocumentsData.maxAmountOfItems)(document)))))

        val correctForm = Seq(
          ("documentCategory", "X"),
          ("documentType", "355"),
          ("documentReference", "reference"),
          ("goodsItemIdentifier", ""),
          saveAndContinueActionUrlEncoded
        )

        val result = controller.savePreviousDocuments(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        status(result) mustBe BAD_REQUEST
        verify(mockPreviousDocumentsPage, times(1)).apply(any(), any(), any(), any())(any(), any())
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user correctly add new item" in {

        val correctForm = Seq(
          ("documentCategory", "X"),
          ("documentType", "355"),
          ("documentReference", "reference"),
          ("goodsItemIdentifier", ""),
          addActionUrlEncoded()
        )

        val result = controller.savePreviousDocuments(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.PreviousDocumentsController.displayPage()

        verify(mockPreviousDocumentsPage, times(0)).apply(any(), any(), any(), any())(any(), any())
      }

      "user save correct data" in {

        val correctForm = Seq(
          ("documentCategory", "X"),
          ("documentType", "355"),
          ("documentReference", "reference"),
          ("goodsItemIdentifier", ""),
          saveAndContinueActionUrlEncoded
        )

        val result = controller.savePreviousDocuments(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.ItemsSummaryController.displayPage()

        verify(mockPreviousDocumentsPage, times(0)).apply(any(), any(), any(), any())(any(), any())
      }

      "user save correct data without new item" in {

        val document = Document("X", "355", "reference", Some("123"))
        withNewCaching(aDeclaration(withPreviousDocuments(document)))

        val result =
          controller.savePreviousDocuments(Mode.Normal)(postRequestAsFormUrlEncoded(saveAndContinueActionUrlEncoded))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.ItemsSummaryController.displayPage()
        verify(mockPreviousDocumentsPage, times(0)).apply(any(), any(), any(), any())(any(), any())
      }

      "user remove existing item" in {

        val document = Document("X", "355", "reference", Some("123"))
        withNewCaching(aDeclaration(withPreviousDocuments(document)))

        val removeAction = (Remove.toString, Json.toJson(document).toString)

        val result = controller.savePreviousDocuments(Mode.Normal)(postRequestAsFormUrlEncoded(removeAction))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.PreviousDocumentsController.displayPage(Mode.Normal)
      }
    }
  }
}
