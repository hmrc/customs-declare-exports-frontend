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
import base.ExportsTestData.lrn
import forms.{Ducr, Lrn}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.{ConsignmentReferences, TraderReference}
import mock.ErrorHandlerMocks
import models.DeclarationType.SUPPLEMENTARY
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.{ArgumentCaptor, Mockito}
import org.mockito.Mockito.{verify, when}
import play.api.data.Form
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers.{await, redirectLocation, status}
import play.twirl.api.HtmlFormat
import views.html.declaration.confirm_ducr

import java.time.{LocalDateTime, ZonedDateTime}

class ConfirmDucrControllerSpec extends ControllerSpec with ErrorHandlerMocks {

  private val confirmDucrPage = mock[confirm_ducr]

  private val controller = new ConfirmDucrController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    mockErrorHandler,
    stubMessagesControllerComponents(),
    mockExportsCacheService,
    confirmDucrPage
  )

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withTraderReference(dummyTraderRef)))
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

  private val dummyTraderRef = TraderReference("dummyRef")
  private val lastDigitOfYear = ZonedDateTime.now().getYear.toString.last
  private val dummyDucr = Ducr(lastDigitOfYear + "GB" + authEori + "-" + dummyTraderRef.value)

  "ConfirmDucrController" should {

    "return 200 OK" when {

      "display page method is invoked with trader reference in the cache" in {
        withNewCaching(aDeclaration(withTraderReference(dummyTraderRef), withCreatedDate(LocalDateTime.now())))

        val result = controller.displayPage()(getJourneyRequest())

        status(result) mustBe OK
        verify(confirmDucrPage).apply(any(), meq(dummyDucr))(any(), any())
      }
    }

    "return 400 bad request" when {

      "display page is invoked with no trader ref in cache" in {
        withNewCaching(aDeclaration())

        val result = controller.displayPage()(getJourneyRequest())

        status(result) mustBe BAD_REQUEST
        verify(mockErrorHandler).displayErrorPage()(any())
        verifyTheCacheIsUnchanged()
      }

      "form was submitted with no data" in {
        withNewCaching(aDeclaration(withTraderReference(dummyTraderRef), withCreatedDate(LocalDateTime.now())))

        val body = Json.obj(YesNoAnswer.formId -> "")
        val result = controller.submitForm()(postRequest(body, aDeclaration()))

        status(result) mustBe BAD_REQUEST
        verify(confirmDucrPage).apply(any(), meq(dummyDucr))(any(), any())
        verifyTheCacheIsUnchanged()
      }
    }

    "return 303 redirect" when {

      "form was submitted with Yes answer with no existing ConsignmentReferences" in {
        val declaration = aDeclaration(withTraderReference(dummyTraderRef), withCreatedDate(LocalDateTime.now()))
        withNewCaching(declaration)

        val body = Json.obj(YesNoAnswer.formId -> YesNoAnswers.yes)
        val result = controller.submitForm()(postRequest(body, aDeclaration()))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.LocalReferenceNumberController.displayPage

        val cacheModifier: ExportsDeclarationModifier =
          _.copy(intermediaryConsignmentReferences = Some(IntermediaryConsignmentReferences(Some(dummyDucr), Some(dummyTraderRef))))
        theCacheModelUpdated mustBe aDeclarationAfter(declaration, cacheModifier)
      }

      "form was submitted with Yes answer with existing ConsignmentReferences" in {
        val declaration = aDeclaration(withTraderReference(dummyTraderRef), withConsignmentReferences(), withCreatedDate(LocalDateTime.now()))
        withNewCaching(declaration)

        val body = Json.obj(YesNoAnswer.formId -> YesNoAnswers.yes)
        val result = controller.submitForm()(postRequest(body, aDeclaration()))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.LocalReferenceNumberController.displayPage

        val cacheModifier: ExportsDeclarationModifier =
          _.copy(consignmentReferences = Some(ConsignmentReferences(dummyDucr, Some(Lrn(lrn)))))
        theCacheModelUpdated mustBe aDeclarationAfter(declaration, cacheModifier)
      }

      "form was submitted with No answer" in {
        withNewCaching(aDeclaration(withTraderReference(dummyTraderRef), withCreatedDate(LocalDateTime.now())))

        val body = Json.obj(YesNoAnswer.formId -> YesNoAnswers.no)
        val result = controller.submitForm()(postRequest(body, aDeclaration()))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.DucrEntryController.displayPage
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
