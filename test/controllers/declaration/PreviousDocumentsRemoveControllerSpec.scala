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
import forms.common.YesNoAnswer
import forms.declaration.Document
import models.Mode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import utils.ListItem
import views.html.declaration.previousDocuments.previous_documents_remove

class PreviousDocumentsRemoveControllerSpec extends ControllerWithoutFormSpec {

  private val page = mock[previous_documents_remove]

  private val controller = new PreviousDocumentsRemoveController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    page
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(page.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(page)

    super.afterEach()
  }

  private val document = Document("355", "reference", None)
  private val documentId = ListItem.createId(0, document)

  "Previous Documents Remove Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with document that exists" in {

        withNewCaching(aDeclaration(withPreviousDocuments(document)))

        val result = controller.displayPage(Mode.Normal, documentId)(getRequest())

        status(result) mustBe OK
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user doesn't provide the answer" in {

        withNewCaching(aDeclaration(withPreviousDocuments(document)))

        val incorrectForm = Json.toJson(YesNoAnswer(""))

        val result = controller.submit(Mode.Normal, documentId)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER) and redirect to previous documents summary" when {

      "the display page methid is invoked with non existing document" in {

        withNewCaching(aDeclaration(withoutPreviousDocuments()))

        val result = controller.displayPage(Mode.Normal, documentId)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.PreviousDocumentsSummaryController.displayPage()
      }

      "user answer Yes and there are some documents in cache" in {

        withNewCaching(aDeclaration(withPreviousDocuments(document, Document("355", "reference", None))))

        val correctForm = JsObject(Seq("yesNo" -> JsString("Yes")))

        val result = controller.submit(Mode.Normal, documentId)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.PreviousDocumentsSummaryController.displayPage()
      }

      "user answer No" in {

        withNewCaching(aDeclaration(withPreviousDocuments(document)))

        val correctForm = JsObject(Seq("yesNo" -> JsString("No")))

        val result = controller.submit(Mode.Normal, documentId)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.PreviousDocumentsSummaryController.displayPage()
      }

      "submit method is invoked for incorrect document" in {

        withNewCaching(aDeclaration(withPreviousDocuments(document)))

        val correctForm = JsObject(Seq("yesNo" -> JsString("Yes")))

        val result = controller.submit(Mode.Normal, "incorrectId")(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.PreviousDocumentsSummaryController.displayPage()
      }
    }

    "return 303 (SEE_OTHER) and redirect to documents page" when {

      "user answer Yes and there is no more documents in cache" in {

        withNewCaching(aDeclaration(withPreviousDocuments(document)))

        val correctForm = JsObject(Seq("yesNo" -> JsString("Yes")))

        val result = controller.submit(Mode.Normal, documentId)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.PreviousDocumentsSummaryController.displayPage()
      }
    }
  }
}
