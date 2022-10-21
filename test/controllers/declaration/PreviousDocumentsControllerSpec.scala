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

import base.ControllerWithoutFormSpec
import controllers.declaration.routes.PreviousDocumentsSummaryController
import forms.declaration.{Document, PreviousDocumentsData}
import models.DeclarationType
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.{Html, HtmlFormat}
import views.html.declaration.previousDocuments.previous_documents

class PreviousDocumentsControllerSpec extends ControllerWithoutFormSpec {

  val mockPreviousDocumentsPage = mock[previous_documents]

  val controller = new PreviousDocumentsController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    stubMessagesControllerComponents(),
    mockPreviousDocumentsPage,
    mockExportsCacheService
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
    when(mockPreviousDocumentsPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockPreviousDocumentsPage)

    super.afterEach()
  }

  def theResponse: Form[Document] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[Document]])
    verify(mockPreviousDocumentsPage).apply(formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  def verifyPage(numberOfTimes: Int = 1): Html =
    verify(mockPreviousDocumentsPage, times(numberOfTimes)).apply(any())(any(), any())

  "Previous Documents controller" should {

    "return 200 (OK)" when {
      "display page method " in {
        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        verifyPage()

        theResponse.value mustBe empty
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user doesn't provide the Document type" in {
        withNewCaching(aDeclaration(withoutPreviousDocuments()))

        val emptyForm = Json.toJson(Document("", "reference", Some("123")))
        val result = controller.submit()(postRequest(emptyForm))

        status(result) mustBe BAD_REQUEST
        verifyPage()
      }

      "user doesn't provide the Document reference" in {
        withNewCaching(aDeclaration(withoutPreviousDocuments()))

        val emptyForm = Json.toJson(Document("355", "", Some("123")))
        val result = controller.submit()(postRequest(emptyForm))

        status(result) mustBe BAD_REQUEST
        verifyPage()
      }

      "user doesn't provide Document type and Document reference" in {
        withNewCaching(aDeclaration(withoutPreviousDocuments()))

        val emptyForm = Json.toJson(Document("", "", Some("123")))
        val result = controller.submit()(postRequest(emptyForm))

        status(result) mustBe BAD_REQUEST
        verifyPage()
      }

      "user put duplicated item" in {
        val document = Document("355", "reference", Some("123"))
        withNewCaching(aDeclaration(withPreviousDocuments(document)))

        val duplicatedForm = Json.toJson(Document("355", "reference", Some("123")))
        val result = controller.submit()(postRequest(duplicatedForm))

        status(result) mustBe BAD_REQUEST
        verifyPage()
      }

      "user reach maximum amount of items" in {
        val document = Document("355", "reference", Some("123"))
        withNewCaching(aDeclaration(withPreviousDocuments(PreviousDocumentsData(Seq.fill(PreviousDocumentsData.maxAmountOfItems)(document)))))

        val correctForm = Json.toJson(Document("355", "reference", None))

        val result = controller.submit()(postRequest(correctForm))

        status(result) mustBe BAD_REQUEST
        verifyPage()
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user fills in Document type and Document reference" in {
        val correctForm = Json.toJson(Document("355", "reference", None))

        val result = controller.submit()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe PreviousDocumentsSummaryController.displayPage()

        verifyPage(0)
      }

      "user fills in all fields" in {
        val correctForm = Json.toJson(Document("355", "reference", Some("123")))

        val result = controller.submit()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe PreviousDocumentsSummaryController.displayPage()

        verifyPage(0)
      }
    }
  }
}
