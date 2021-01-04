/*
 * Copyright 2021 HM Revenue & Customs
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

import base.Injector
import controllers.declaration.ConfirmationController
import play.api.mvc.{AnyContentAsEmpty, Flash, Request, Result}
import play.api.test.Helpers._
import unit.base.ControllerWithoutFormSpec
import views.html.declaration.{draft_confirmation_page, submission_confirmation_page}

import scala.concurrent.Future

class ConfirmationControllerSpec extends ControllerWithoutFormSpec with Injector {

  trait SetUp {
    val submissionConfirmationPage = instanceOf[submission_confirmation_page]
    val draftConfirmationPage = instanceOf[draft_confirmation_page]

    val controller =
      new ConfirmationController(mockAuthAction, stubMessagesControllerComponents(), submissionConfirmationPage, draftConfirmationPage)

    authorizedUser()
  }

  "GET submission confirmation" should {
    "return 200 status code" in new SetUp {

      val request: Request[AnyContentAsEmpty.type] = getRequest()
      val result: Future[Result] = controller.displaySubmissionConfirmation()(request)

      status(result) must be(OK)
      viewOf(result) must be(
        submissionConfirmationPage()(getAuthenticatedRequest(), Flash(), stubMessagesControllerComponents().messagesApi.preferred(request))
      )
    }
  }

  "GET draft confirmation" should {
    "return 200 status code" in new SetUp {

      val request: Request[AnyContentAsEmpty.type] = getRequest()
      val result: Future[Result] = controller.displayDraftConfirmation()(request)

      status(result) must be(OK)
      viewOf(result) must be(
        draftConfirmationPage()(getAuthenticatedRequest(), Flash(), stubMessagesControllerComponents().messagesApi.preferred(request))
      )
    }
  }
}
