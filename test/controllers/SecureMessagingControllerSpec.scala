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
import connectors.SecureMessagingFrontendConnector
import controllers.actions.SecureMessagingAction
import models.messaging.{ConversationPartial, InboxPartial, ReplyResultPartial}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.partials.HeaderCarrierForPartialsConverterImpl
import views.html.messaging.{inbox_wrapper, partial_wrapper}

import scala.concurrent.Future

class SecureMessagingControllerSpec extends ControllerWithoutFormSpec {

  val secureMessagingAction = new SecureMessagingAction(mockSecureMessagingConfig)

  val secureMessagingFrontendConnector = mock[SecureMessagingFrontendConnector]

  val inboxWrapperPage = mock[inbox_wrapper]
  val partialWrapperPage = mock[partial_wrapper]

  val controller =
    new SecureMessagingController(
      mockAuthAction,
      mockVerifiedEmailAction,
      secureMessagingAction,
      secureMessagingFrontendConnector,
      mcc,
      inboxWrapperPage,
      partialWrapperPage,
      new HeaderCarrierForPartialsConverterImpl
    )

  override def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()

    reset(inboxWrapperPage, partialWrapperPage, secureMessagingFrontendConnector)

    when(inboxWrapperPage.apply(any[HtmlFormat.Appendable])(any[Request[_]], any[Messages])).thenReturn(HtmlFormat.empty)
    when(partialWrapperPage.apply(any[HtmlFormat.Appendable], any[String], any[String], any[Option[Call]])(any[Request[_]], any[Messages]))
      .thenReturn(HtmlFormat.empty)
    when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(true)
  }

  override def afterEach(): Unit =
    reset(inboxWrapperPage, mockSecureMessagingConfig, secureMessagingFrontendConnector)

  "SecureMessagingController displayInbox" when {

    "the SecureMessaging flag is disabled" should {
      "throw IllegalStateException" in {
        when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(false)

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

  "SecureMessagingController displayConversation is called" when {
    val clientId = "clientId"
    val conversationId = "conversationId"

    "feature flag for SecureMessaging is disabled" should {

      "throw IllegalStateException" in {
        when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(false)

        an[IllegalStateException] mustBe thrownBy {
          await(controller.displayConversation(clientId, conversationId)(getRequest()))
        }
      }
    }

    "feature flag for SecureMessaging is enabled" should {

      "call secure message connector" when {

        "successfully returns a ConversationPartial" should {

          "wrap the partial in the conversation display wrapper" in {
            when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(true)
            when(secureMessagingFrontendConnector.retrieveConversationPartial(any[String], any[String])(any[HeaderCarrier]))
              .thenReturn(Future.successful(ConversationPartial("")))

            val result = controller.displayConversation(clientId, conversationId)(getRequest())
            status(result) mustBe OK

            val expectedUrl = Some(routes.SecureMessagingController.displayInbox)
            verify(partialWrapperPage).apply(any(), any(), any(), eqTo(expectedUrl))(any(), any())
          }
        }

        "unsuccessfully returns a failed Future" should {

          "display the 'Sorry' page to the user" in {
            when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(true)

            when(secureMessagingFrontendConnector.retrieveConversationPartial(any[String], any[String])(any[HeaderCarrier]))
              .thenReturn(Future.failed(new Exception("Whoopse")))

            an[Exception] mustBe thrownBy {
              await(controller.displayConversation(clientId, conversationId)(getRequest()))
            }
          }
        }
      }
    }
  }

  "SecureMessagingController displayReplyResult is called" when {
    val clientId = "clientId"
    val conversationId = "conversationId"

    "feature flag for SecureMessaging is disabled" should {

      "throw IllegalStateException" in {
        when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(false)

        an[IllegalStateException] mustBe thrownBy {
          await(controller.displayReplyResult(clientId, conversationId)(getRequest()))
        }
      }
    }

    "feature flag for SecureMessaging is enabled" should {

      "call secure message connector" that {

        "returns a ReplyResultPartial" should {

          "wrap the partial in the reply_result page" in {
            when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(true)

            when(secureMessagingFrontendConnector.retrieveReplyResult(any[String], any[String])(any[HeaderCarrier]))
              .thenReturn(Future.successful(ReplyResultPartial("")))

            val result = controller.displayReplyResult(clientId, conversationId)(getRequest())

            status(result) mustBe OK
          }
        }

        "returns a failed Future" should {

          "throw an exception" in {
            when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(true)

            when(secureMessagingFrontendConnector.retrieveReplyResult(any[String], any[String])(any[HeaderCarrier]))
              .thenReturn(Future.failed(new Exception("Whoopse")))

            an[Exception] mustBe thrownBy {
              await(controller.displayReplyResult(clientId, conversationId)(getRequest()))
            }
          }
        }
      }
    }
  }

  "SecureMessagingController on submitReply" when {
    val clientId = "clientId"
    val conversationId = "conversationId"

    "feature flag for SecureMessaging is disabled" should {

      "throw IllegalStateException" in {
        when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(false)

        an[IllegalStateException] mustBe thrownBy {
          await(controller.submitReply(clientId, conversationId)(getRequest()))
        }
      }
    }

    "feature flag for SecureMessaging is enabled" should {

      "call secure message connector" in {
        when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(true)
        when(secureMessagingFrontendConnector.submitReply(any[String], any[String], any[Map[String, Seq[String]]])(any[HeaderCarrier]))
          .thenReturn(Future.successful(None))

        val postRequest = postRequestAsFormUrlEncoded("messageReply" -> "BlaBla").withCSRFToken
        controller.submitReply(clientId, conversationId)(postRequest).futureValue

        verify(secureMessagingFrontendConnector).submitReply(any[String], any[String], any[Map[String, Seq[String]]])(any[HeaderCarrier])
      }

      "returns a successful Future of None" that {
        "wraps the returned partial in the partial_wrapper page" in {
          when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(true)

          when(secureMessagingFrontendConnector.submitReply(any[String], any[String], any[Map[String, Seq[String]]])(any[HeaderCarrier]))
            .thenReturn(Future.successful(Some(ConversationPartial("<html></html>"))))

          val postRequest = postRequestAsFormUrlEncoded("messageReply" -> "BlaBla").withCSRFToken
          val result = controller.submitReply(clientId, conversationId)(postRequest)

          status(result) mustBe OK

          val expectedUrl = Some(routes.SecureMessagingController.displayInbox)
          verify(partialWrapperPage).apply(any(), any(), any(), eqTo(expectedUrl))(any(), any())
        }
      }

      "returns a successful Future of Some(ConversationPartial)" that {
        "wraps the returned partial in the partial_wrapper page" in {
          when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(true)

          when(secureMessagingFrontendConnector.submitReply(any[String], any[String], any[Map[String, Seq[String]]])(any[HeaderCarrier]))
            .thenReturn(Future.successful(Some(ConversationPartial("<html></html>"))))

          val postRequest = postRequestAsFormUrlEncoded("messageReply" -> "BlaBla").withCSRFToken
          val result = controller.submitReply(clientId, conversationId)(postRequest)

          status(result) mustBe OK

          val expectedUrl = Some(routes.SecureMessagingController.displayInbox)
          verify(partialWrapperPage).apply(any(), any(), any(), eqTo(expectedUrl))(any(), any())
        }
      }

      "returns a failed Future" should {
        "return a 500 response to the user" in {
          when(mockSecureMessagingConfig.isSecureMessagingEnabled).thenReturn(true)

          when(secureMessagingFrontendConnector.submitReply(any[String], any[String], any[Map[String, Seq[String]]])(any[HeaderCarrier]))
            .thenReturn(Future.failed(new Exception("Whoops!")))

          val postRequest = postRequestAsFormUrlEncoded("messageReply" -> "BlaBla").withCSRFToken

          an[Exception] mustBe thrownBy {
            await(controller.submitReply(clientId, conversationId)(postRequest))
          }

          verify(secureMessagingFrontendConnector).submitReply(any[String], any[String], any[Map[String, Seq[String]]])(any[HeaderCarrier])
        }
      }
    }
  }
}
