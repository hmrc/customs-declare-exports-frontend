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

package controllers

import base.ControllerWithoutFormSpec
import config.PaginationConfig
import models.requests.SessionHelper
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declarations.saved_declarations

class SavedDeclarationsControllerSpec extends ControllerWithoutFormSpec {

  private val savedDeclarationsPage = mock[saved_declarations]
  private val paginationConfig = mock[PaginationConfig]

  private val controller = new SavedDeclarationsController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockCustomsDeclareExportsConnector,
    stubMessagesControllerComponents(),
    savedDeclarationsPage,
    paginationConfig
  )(ec)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(savedDeclarationsPage)
    when(savedDeclarationsPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    authorizedUser()
  }

  "Submissions controller" should {

    "return 200 (OK)" when {
      "display declarations method is invoked" in {
        listOfDraftDeclarations()

        val result = controller.displayDeclarations()(getRequest())

        status(result) must be(OK)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "continue declaration found" in {
        fetchDeclaration("123")

        val result = controller.continueDeclaration("123")(getRequest())

        status(result) must be(SEE_OTHER)
        session(result).get(SessionHelper.declarationUuid) must be(Some("123"))
      }

      "continue declaration not found" in {
        declarationNotFound
        val result = controller.continueDeclaration("123")(getRequest())

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.routes.SavedDeclarationsController.displayDeclarations().url))
      }
    }
  }
}
