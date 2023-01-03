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

package controllers.declaration

import base.{ControllerWithoutFormSpec, Injector}
import models.requests.ExportsSessionKeys
import play.api.mvc.Result
import play.api.test.Helpers._
import views.html.declaration.draft_declaration_page

import scala.concurrent.Future

class DraftDeclarationControllerSpec extends ControllerWithoutFormSpec with Injector {

  trait SetUp {
    val draftDeclarationPage = instanceOf[draft_declaration_page]

    val controller =
      new DraftDeclarationController(mockAuthAction, appConfig, stubMessagesControllerComponents(), draftDeclarationPage, mockJourneyAction)

    authorizedUser()
  }

  "GET draft declaration" should {
    "return 200 status code" in new SetUp {
      withNewCaching(aDeclaration())
      val result: Future[Result] = controller.displayPage(getRequest())

      status(result) mustBe OK
      session(result).get(ExportsSessionKeys.declarationId) mustBe None
    }
  }
}
