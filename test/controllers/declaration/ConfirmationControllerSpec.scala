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

package controllers.declaration

import scala.concurrent.Future

import base.{ControllerWithoutFormSpec, Injector}
import play.api.mvc.{AnyContentAsEmpty, Flash, Request, Result}
import play.api.test.Helpers._
import views.html.declaration.confirmation._

class ConfirmationControllerSpec extends ControllerWithoutFormSpec with Injector {

  trait SetUp {
    val draftConfirmationPage = instanceOf[draft_confirmation_page]
    val holdingConfirmationPage = instanceOf[holding_confirmation_page]
    val submissionConfirmationPage = instanceOf[submission_confirmation_page]

    val controller = new ConfirmationController(
      mockAuthAction,
      stubMessagesControllerComponents(),
      draftConfirmationPage,
      holdingConfirmationPage,
      submissionConfirmationPage
    )

    authorizedUser()
  }

  "GET draft confirmation" should {
    "return 200 status code" in new SetUp {
      val request: Request[AnyContentAsEmpty.type] = getRequest()
      val result: Future[Result] = controller.displayDraftConfirmation(request)

      status(result) mustBe OK
      viewOf(result) mustBe draftConfirmationPage()(
        getAuthenticatedRequest(),
        Flash(),
        stubMessagesControllerComponents().messagesApi.preferred(request)
      )
    }
  }

  "GET holding confirmation" should {
    "return 200 status code" in new SetUp {
      val request: Request[AnyContentAsEmpty.type] = getRequest()
      val result: Future[Result] = controller.displayHoldingConfirmation(request)

      status(result) mustBe OK
      val view = viewOf(result)
      val expectedView = holdingConfirmationPage()(getAuthenticatedRequest(), stubMessagesControllerComponents().messagesApi.preferred(request))
      view mustBe expectedView
    }
  }

  "GET submission confirmation" should {
    "return 200 status code" in new SetUp {
      val request: Request[AnyContentAsEmpty.type] = getRequest()
      val result: Future[Result] = controller.displaySubmissionConfirmation(request)

      status(result) mustBe OK
      viewOf(result) mustBe submissionConfirmationPage()(getAuthenticatedRequest(), stubMessagesControllerComponents().messagesApi.preferred(request))
    }
  }
}
