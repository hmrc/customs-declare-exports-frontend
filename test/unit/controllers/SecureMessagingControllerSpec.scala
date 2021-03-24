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

package unit.controllers

import scala.concurrent.Future

import config.SecureMessagingConfig
import connectors.SecureMessagingFrontendConnector
import controllers.SecureMessagingController
import controllers.actions.SecureMessagingAction
import models.messaging.InboxPartial
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HeaderCarrier
import unit.base.ControllerWithoutFormSpec
import views.html.messaging.inbox_wrapper

class SecureMessagingControllerSpec extends ControllerWithoutFormSpec {

  val secureMessagingConfig = mock[SecureMessagingConfig]
  val secureMessagingAction = new SecureMessagingAction(secureMessagingConfig)

  val secureMessagingFrontendConnector = mock[SecureMessagingFrontendConnector]

  val inboxWrapperPage = mock[inbox_wrapper]

  val controller =
    new SecureMessagingController(
      mockAuthAction,
      mockVerifiedEmailAction,
      secureMessagingAction,
      secureMessagingFrontendConnector,
      stubMessagesControllerComponents(),
      inboxWrapperPage
    )

  override def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(inboxWrapperPage.apply(any[HtmlFormat.Appendable])(any[Request[_]], any[Messages])).thenReturn(HtmlFormat.empty)
    when(secureMessagingConfig.isSecureMessagingEnabled).thenReturn(true)
  }

  override def afterEach(): Unit =
    reset(inboxWrapperPage, secureMessagingConfig, secureMessagingFrontendConnector)

  "SecureMessagingController displayInbox" when {

    "the SecureMessaging flag is disabled" should {
      "throw IllegalStateException" in {
        when(secureMessagingConfig.isSecureMessagingEnabled).thenReturn(false)

        an[IllegalStateException] mustBe thrownBy {
          await(controller.displayInbox()(getRequest()))
        }
      }
    }

    "the SecureMessaging flag is is enabled" should {

      "add the conversation inbox, returned from the secure-message service, to the inbox_wrapper" in {
        when(secureMessagingFrontendConnector.retrieveInboxPartial(any[String])(any[HeaderCarrier]))
          .thenReturn(Future.successful(InboxPartial("Some Content")))

        val result = controller.displayInbox()(getRequest())

        status(result) mustBe OK
        verify(inboxWrapperPage).apply(any[HtmlFormat.Appendable])(any(), any())
      }

      "throw an exception" when {
        "the secure-message service returns a failed Future" in {
          when(secureMessagingFrontendConnector.retrieveInboxPartial(any[String])(any[HeaderCarrier]))
            .thenReturn(Future.failed(new Exception("A message error")))

          an[Exception] mustBe thrownBy {
            await(controller.displayInbox()(getRequest()))
          }
        }
      }
    }
  }
}
