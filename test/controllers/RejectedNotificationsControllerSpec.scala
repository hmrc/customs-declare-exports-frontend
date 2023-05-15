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
import config.AppConfig
import handlers.ErrorHandler
import mock.ErrorHandlerMocks
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.{error_template, rejected_notification_errors}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class RejectedNotificationsControllerSpec extends ControllerWithoutFormSpec with ErrorHandlerMocks with OptionValues {

  private val mockRejectedNotificationPage = mock[rejected_notification_errors]

  private val mcc = stubMessagesControllerComponents()

  private val controller = new RejectedNotificationsController(
    mockAuthAction,
    mockVerifiedEmailAction,
    new ErrorHandler(mcc.messagesApi, instanceOf[error_template])(instanceOf[AppConfig]),
    mockCustomsDeclareExportsConnector,
    mcc,
    mockRejectedNotificationPage
  )(global)

  private val declarationId = "DeclarationId"

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(mockRejectedNotificationPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockRejectedNotificationPage)
    super.afterEach()
  }
  "RejectedNotificationsController.displayPage" should {

    "return 200 (OK)" when {

      "declaration and notifications are found" in {
        fetchDeclaration(declarationId)
        findNotifications(declarationId)

        val result = controller.displayPage(declarationId, false)(getRequest())

        status(result) mustBe OK
        verify(mockRejectedNotificationPage).apply(any(), any(), any(), any())(any(), any())
      }

      "the declaration is found but the Submission has no notifications" in {
        fetchDeclaration(declarationId)

        when(mockCustomsDeclareExportsConnector.findNotifications(any())(any(), any()))
          .thenReturn(Future.successful(List.empty))

        val result = controller.displayPage(declarationId, false)(getRequest())

        status(result) mustBe OK
        verify(mockRejectedNotificationPage).apply(any(), any(), any(), any())(any(), any())
      }
    }

    "return 500 (INTERNAL_SERVER_ERROR)" when {
      "the declaration cannot be found" in {
        declarationNotFound

        val result = controller.displayPage(declarationId, false)(getRequest())

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
