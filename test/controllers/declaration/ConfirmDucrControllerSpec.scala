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
import forms.Ducr
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.ConsignmentReferences
import mock.ErrorHandlerMocks
import models.DeclarationType.SUPPLEMENTARY
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{verify, when}
import org.mockito.{ArgumentCaptor, Mockito}
import play.api.data.Form
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers.{await, redirectLocation, status}
import play.twirl.api.HtmlFormat
import views.html.declaration.confirm_ducr

class ConfirmDucrControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  private val confirmDucrPage = mock[confirm_ducr]

  private val controller = new ConfirmDucrController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    stubMessagesControllerComponents(),
    mockExportsCacheService,
    confirmDucrPage
  )

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withConsignmentReferences(dummyConRefs)))
    await(controller.displayPage()(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(confirmDucrPage).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(confirmDucrPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    setupErrorHandler()
    authorizedUser()
  }

  override protected def afterEach(): Unit = {
    Mockito.reset(confirmDucrPage)
    super.afterEach()
  }

  private val dummyConRefs = ConsignmentReferences(Some(Ducr("DUCR")))

  "ConfirmDucrController" should {

    "return 200 OK" when {

      "display page method is invoked with DUCR in the cache" in {
        withNewCaching(aDeclaration(withConsignmentReferences(dummyConRefs)))

        val result = controller.displayPage()(getJourneyRequest())

        status(result) mustBe OK
        verify(confirmDucrPage).apply(any(), meq(dummyConRefs.ducr.get))(any(), any())
      }
    }

    "return 400 bad request" when {

      "form was submitted with no data" in {
        withNewCaching(aDeclaration(withConsignmentReferences(dummyConRefs)))

        val body = Json.obj(YesNoAnswer.formId -> "")
        val result = controller.submitForm()(postRequest(body, aDeclaration()))

        status(result) mustBe BAD_REQUEST
        verify(confirmDucrPage).apply(any(), meq(dummyConRefs.ducr.get))(any(), any())
        verifyTheCacheIsUnchanged()
      }
    }

    "return 303 redirect" when {

      "form was submitted with Yes answer" in {
        val declaration = aDeclaration(withConsignmentReferences(dummyConRefs))
        withNewCaching(declaration)

        val body = Json.obj(YesNoAnswer.formId -> YesNoAnswers.yes)
        val result = controller.submitForm()(postRequest(body, aDeclaration()))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.LocalReferenceNumberController.displayPage
        verifyTheCacheIsUnchanged()
      }

      "form was submitted with No answer" in {
        withNewCaching(aDeclaration(withConsignmentReferences(dummyConRefs)))

        val body = Json.obj(YesNoAnswer.formId -> YesNoAnswers.no)
        val result = controller.submitForm()(postRequest(body, aDeclaration()))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.DucrEntryController.displayPage
        theCacheModelUpdated mustBe aDeclaration(withConsignmentReferences(ConsignmentReferences(None, None, None, None)))
      }

      "display page is invoked with no DUCR in cache" in {
        withNewCaching(aDeclaration())

        val result = controller.displayPage()(getJourneyRequest())

        status(result) mustBe 303
        redirectLocation(result) mustBe Some(routes.DucrEntryController.displayPage.url)
        verifyTheCacheIsUnchanged()
      }

      "display page method is invoked on supplementary journey" in {
        withNewCaching(aDeclaration(withType(SUPPLEMENTARY)))

        val result = controller.displayPage()(getJourneyRequest())

        status(result) mustBe 303
        redirectLocation(result) mustBe Some(controllers.routes.RootController.displayPage.url)
      }
    }
  }
}
