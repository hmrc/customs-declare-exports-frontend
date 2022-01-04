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

import scala.concurrent.Future

import base.{ControllerWithoutFormSpec, Injector}
import play.api.mvc.{AnyContentAsEmpty, Flash, Request, Result}
import play.api.test.Helpers._
import views.html.declaration.draft_declaration_page

class DraftDeclarationControllerSpec extends ControllerWithoutFormSpec with Injector {

  trait SetUp {
    val draftDeclarationPage = instanceOf[draft_declaration_page]

    val controller = new DraftDeclarationController(mockAuthAction, stubMessagesControllerComponents(), draftDeclarationPage)
    authorizedUser()
  }

  "GET draft declaration" should {
    "return 200 status code" in new SetUp {
      val request: Request[AnyContentAsEmpty.type] = getRequest()
      val result: Future[Result] = controller.displayPage(request)

      status(result) mustBe OK
      viewOf(result) mustBe draftDeclarationPage()(
        getAuthenticatedRequest(),
        Flash(),
        stubMessagesControllerComponents().messagesApi.preferred(request)
      )
    }
  }
}
