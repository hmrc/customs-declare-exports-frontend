/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers

import base.ControllerWithoutFormSpec
import config.PaginationConfig
import controllers.routes.SavedDeclarationsController
import controllers.summary.routes.SummaryController
import models.{DraftDeclarationData, Page, Paginated}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, verify, when}
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.test.Helpers.{redirectLocation, status}
import play.twirl.api.HtmlFormat
import views.html.drafts.saved_declarations

import scala.concurrent.Future

class SavedDeclarationsControllerSpec extends ControllerWithoutFormSpec {

  private val savedDeclarations = mock[saved_declarations]
  private val paginationConfig = mock[PaginationConfig]

  private val controller = new SavedDeclarationsController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockCustomsDeclareExportsConnector,
    mcc,
    savedDeclarations,
    paginationConfig
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(savedDeclarations.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(paginationConfig.itemsPerPage).thenReturn(Page.MAX_DOCUMENT_PER_PAGE)
  }

  override protected def afterEach(): Unit = reset(mockCustomsDeclareExportsConnector, savedDeclarations, paginationConfig)

  "SavedDeclarationsController.displayDeclarations" should {
    "return 200 (OK)" in {
      val page = Paginated(listOfDraftDeclarationData, Page(), 1)
      when(mockCustomsDeclareExportsConnector.fetchDraftDeclarations(any[Page])(any(), any()))
        .thenReturn(Future.successful(page))

      val result = controller.displayDeclarations()(getRequest())

      status(result) mustBe OK

      val captor: ArgumentCaptor[Paginated[DraftDeclarationData]] = ArgumentCaptor.forClass(classOf[Paginated[DraftDeclarationData]])
      verify(savedDeclarations).apply(captor.capture())(any(), any())
      captor.getValue mustBe page
    }
  }

  "SavedDeclarationsController.displayDeclaration" should {

    "redirect to '/saved-summary'" when {
      "the declaration is found" in {
        val declaration = aDeclaration()
        when(mockCustomsDeclareExportsConnector.findDeclaration(anyString())(any(), any()))
          .thenReturn(Future.successful(Some(declaration)))

        val result = controller.displayDeclaration(declaration.id)(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(SummaryController.displayPage.url)
      }
    }

    "redirect to '/saved-declarations'" when {
      "the declaration is NOT found" in {
        when(mockCustomsDeclareExportsConnector.findDeclaration(anyString())(any(), any()))
          .thenReturn(Future.successful(None))

        val result = controller.displayDeclaration("some id")(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(SavedDeclarationsController.displayDeclarations().url)
      }
    }
  }
}
