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

package connectors

import base.{ExportsTestData, Injector}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{verify, _}
import config.featureFlags.SecureMessagingConfig
import models.AuthKey.enrolment
import models.messaging._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.mvc.Http.Status.{BAD_GATEWAY, BAD_REQUEST, OK}
import uk.gov.hmrc.http._

class SecureMessagingFrontendConnectorISpec extends ConnectorISpec with Injector with ScalaFutures with IntegrationPatience {

  val secureMessagingConfig = app.injector.instanceOf[SecureMessagingConfig]
  val connector = app.injector.instanceOf[SecureMessagingFrontendConnector]

  val clientId = "clientId"
  val conversationId = "conversationId"
  val partialContent = "<div>Some Content</div>"
  val inboxUrl = "/secure-message-frontend/customs-declare-exports/messages"
  val conversationUrl = s"/secure-message-frontend/customs-declare-exports/conversation/$clientId/$conversationId?showReplyForm=true"
  val submitReplyUrl = s"/secure-message-frontend/customs-declare-exports/conversation/$clientId/$conversationId"
  val resultUrl = s"/secure-message-frontend/customs-declare-exports/conversation/$clientId/$conversationId/result"

  override def beforeAll(): Unit = {
    super.beforeAll
    auditingWireMockServer.start
    secureMessagingWireMockServer.start
    WireMock.configureFor(wireHost, secureMessagingWirePort)
  }

  override def beforeEach(): Unit = {
    super.beforeEach
    auditingWireMockServer.resetRequests
  }

  override def afterAll(): Unit = {
    auditingWireMockServer.stop
    secureMessagingWireMockServer.stop
    super.afterAll
  }

  private def constructQueryParams(eori: String): String =
    s"?enrolment=${enrolment}%7EEoriNumber%7E${eori}&tag=notificationType%7E${secureMessagingConfig.notificationType}"

  "SecureMessageFrontend" when {
    "retrieveInboxPartial is called" which {

      val url = s"${inboxUrl}${constructQueryParams(ExportsTestData.eori)}"

      "receives a 200 response" should {
        "return a populated InboxPartial" in {
          val response = aResponse.withStatus(OK).withBody(partialContent)
          stubForSecureMessaging(get(url).willReturn(response))

          val result = connector.retrieveInboxPartial(ExportsTestData.eori).futureValue
          result mustBe InboxPartial(partialContent)

          verifyForAuditing()
        }
      }

      "receives a non 200 response" should {
        "return a failed Future" in {
          val response = aResponse().withStatus(BAD_REQUEST)
          stubForSecureMessaging(get(url).willReturn(response))

          val result = connector.retrieveInboxPartial(ExportsTestData.eori)
          assert(result.failed.futureValue.isInstanceOf[UpstreamErrorResponse])

          verifyForAuditing(0)
        }
      }

      "is passed the user's eori number" should {
        "include the Enrolment tag as a query string parameter with the correct eori value" in {
          val response = aResponse().withStatus(OK).withBody(partialContent)
          stubForSecureMessaging(get(url).willReturn(response))

          connector.retrieveInboxPartial(ExportsTestData.eori).futureValue

          verify(getRequestedFor(urlEqualTo(url)))
          verifyForAuditing()
        }

        "include the ExportMessages tag as a query string parameter" in {
          val response = aResponse().withStatus(OK).withBody(partialContent)
          stubForSecureMessaging(get(url).willReturn(response))

          connector.retrieveInboxPartial(ExportsTestData.eori).futureValue

          verify(getRequestedFor(urlEqualTo(url)))
          verifyForAuditing()
        }
      }
    }

    "retrieveConversationPartial is called" which {
      "receives a 200 response" should {
        "return a populated InboxPartial" in {
          stubForSecureMessaging(
            get(conversationUrl)
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withBody(partialContent)
              )
          )

          val result = connector.retrieveConversationPartial(clientId, conversationId).futureValue
          result mustBe ConversationPartial(partialContent)
        }
      }

      "receives a non 200 response" should {
        "return a failed Future" in {
          stubForSecureMessaging(
            get(conversationUrl)
              .willReturn(
                aResponse()
                  .withStatus(BAD_REQUEST)
              )
          )

          val result = connector.retrieveConversationPartial(clientId, conversationId)
          assert(result.failed.futureValue.isInstanceOf[UpstreamErrorResponse])
        }
      }
    }

    "submitReply is called" which {
      "receives a 200 response" should {
        "return a None" in {
          stubForSecureMessaging(
            post(submitReplyUrl)
              .willReturn(
                aResponse()
                  .withStatus(OK)
              )
          )

          val result = connector.submitReply(clientId, conversationId, Map("field" -> Seq("value"))).futureValue

          result.isEmpty mustBe true
        }
      }

      "receives a 400 response" should {
        "return a Some ConversationPartial" in {
          stubForSecureMessaging(
            post(submitReplyUrl)
              .willReturn(
                aResponse()
                  .withStatus(BAD_REQUEST)
                  .withBody(partialContent)
              )
          )

          val result = connector.submitReply(clientId, conversationId, Map("field" -> Seq("value"))).futureValue

          result.isDefined mustBe true
          result.get mustBe ConversationPartial(partialContent)
        }
      }

      "receives a response that is not a 200 or 400" should {
        "return a failed Future" in {
          stubForSecureMessaging(
            post(submitReplyUrl)
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
              )
          )

          val result = connector.submitReply(clientId, conversationId, Map("field" -> Seq("value")))
          assert(result.failed.futureValue.isInstanceOf[UpstreamErrorResponse])
        }
      }
    }

    "retrieveReplyResult is called" which {
      "receives a 200 response" should {
        "return a populated ReplyResultPartial" in {
          stubForSecureMessaging(
            get(resultUrl)
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withBody(partialContent)
              )
          )

          val result = connector.retrieveReplyResult(clientId, conversationId).futureValue

          result mustBe ReplyResultPartial(partialContent)
        }
      }

      "receives a non 200 response" should {
        "return a failed Future" in {
          stubForSecureMessaging(
            get(resultUrl)
              .willReturn(
                aResponse()
                  .withStatus(BAD_REQUEST)
              )
          )

          val result = connector.retrieveReplyResult(clientId, conversationId)
          assert(result.failed.futureValue.isInstanceOf[UpstreamErrorResponse])
        }
      }
    }
  }

  "secure-message-frontend service is unavailable" when {
    "retrieveInboxPartial is called" should {
      "return a failed Future" in {
        secureMessagingWireMockServer.stop()

        val result = connector.retrieveInboxPartial(ExportsTestData.eori)
        assert(result.failed.futureValue.isInstanceOf[BadGatewayException])
      }
    }

    "retrieveConversationPartial is called" should {
      "return a failed Future" in {
        secureMessagingWireMockServer.stop()

        val result = connector.retrieveConversationPartial(clientId, conversationId)
        assert(result.failed.futureValue.isInstanceOf[BadGatewayException])
      }
    }

    "submitReply is called" should {
      "return a failed Future" in {
        secureMessagingWireMockServer.stop()

        val result = connector.submitReply(clientId, conversationId, Map("field" -> Seq("value")))
        assert(result.failed.futureValue.isInstanceOf[BadGatewayException])
      }
    }

    "retrieveReplyResult is called" should {
      "return a failed Future" in {
        secureMessagingWireMockServer.stop()

        val result = connector.retrieveReplyResult(clientId, conversationId)
        assert(result.failed.futureValue.isInstanceOf[BadGatewayException])
      }
    }
  }
}
