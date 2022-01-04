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
import forms.declaration.Document
import models.Mode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.previousDocuments.previous_documents_summary

class PreviousDocumentsSummaryControllerSpec extends ControllerSpec {

  private val page = mock[previous_documents_summary]

  private val controller = new PreviousDocumentsSummaryController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    mockExportsCacheService,
    stubMessagesControllerComponents(),
    page
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(page.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(page)

    super.afterEach()
  }

  private val document = Document("355", "reference", None)

  private def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(page).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withPreviousDocuments(document)))
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
  }

  "Previous Documents Summary Controller" should {

    "return 200 (OK)" when {

      "display page is invoked with documents in cache" in {

        withNewCaching(aDeclaration(withPreviousDocuments(document)))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user doesn't answer on the question" in {

        withNewCaching(aDeclaration(withPreviousDocuments(document)))

        val incorrectForm = JsObject(Seq("yesNo" -> JsString("")))

        val result = controller.submit(Mode.Normal)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER) and redirect to Previous Documents page" when {

      "display page method is invoked without documents in cache" in {

        withNewCaching(aDeclaration(withoutPreviousDocuments()))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.PreviousDocumentsController.displayPage()
      }

      "user answer Yes to add additional document" in {

        withNewCaching(aDeclaration(withPreviousDocuments(document)))

        val correctForm = JsObject(Seq("yesNo" -> JsString("Yes")))

        val result = controller.submit(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.PreviousDocumentsController.displayPage()
      }
    }

    "return 303 (SEE_OTHER) and redirect to Items summary page" when {

      "user answer No" in {

        withNewCaching(aDeclaration(withPreviousDocuments(document)))

        val correctForm = JsObject(Seq("yesNo" -> JsString("No")))

        val result = controller.submit(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.ItemsSummaryController.displayAddItemPage()
      }
    }
  }
}
