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

package connectors

import com.google.inject.Inject
import config.featureFlags.SecureMessagingConfig
import models.messaging._
import play.api.Logging
import play.api.http.Status
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw

import scala.concurrent.{ExecutionContext, Future}
import models.AuthKey.enrolment
import services.audit.AuditService

class SecureMessagingFrontendConnector @Inject() (httpClient: HttpClient, config: SecureMessagingConfig, auditService: AuditService)(
  implicit ec: ExecutionContext
) extends Logging with Status {

  def retrieveInboxPartial(eori: String)(implicit hc: HeaderCarrier): Future[InboxPartial] =
    fetchPartial(config.fetchInboxEndpoint, "the user's inbox", constructInboxEndpointQueryParams(eori)).map { response =>
      auditService.auditMessageInboxPartialRetrieved(eori, config.notificationType, config.fetchInbox)
      InboxPartial(response.body)
    }

  def retrieveConversationPartial(client: String, conversationId: String)(implicit hc: HeaderCarrier): Future[ConversationPartial] =
    fetchPartial(config.fetchMessageEndpoint(client, conversationId), s"the '$client/$conversationId' conversation", conversationEndpointQueryParams)
      .map(response => ConversationPartial(response.body))

  def submitReply(client: String, conversationId: String, reply: Map[String, Seq[String]])(
    implicit hc: HeaderCarrier
  ): Future[Option[ConversationPartial]] =
    httpClient
      .POSTForm(config.submitReplyEndpoint(client, conversationId), reply)
      .flatMap { response =>
        response.status match {
          case OK          => Future.successful(None)
          case BAD_REQUEST => Future.successful(Some(ConversationPartial(response.body)))
          case statusCode =>
            Future.failed(UpstreamErrorResponse(s"Unhappy response($statusCode) posting reply form to secure-messaging-frontend", statusCode))
        }
      }
      .recoverWith { case exc: UpstreamErrorResponse =>
        logger.warn(
          s"Received a ${exc.statusCode} response from secure-messaging-frontend while submitting a reply for '$client/$conversationId'. ${exc.message}"
        )
        Future.failed(exc)
      }

  def retrieveReplyResult(client: String, conversationId: String)(implicit hc: HeaderCarrier): Future[ReplyResultPartial] =
    fetchPartial(config.replyResultEndpoint(client, conversationId), s"the success result of replying to '$client/$conversationId' conversation")
      .map(response => ReplyResultPartial(response.body))

  private def fetchPartial(url: String, errorInfo: String, queryParams: Seq[(String, String)] = Seq.empty)(
    implicit hc: HeaderCarrier
  ): Future[HttpResponse] =
    httpClient
      .GET[HttpResponse](url, queryParams)
      .flatMap { response =>
        response.status match {
          case OK => Future.successful(response)
          case statusCode =>
            Future.failed(UpstreamErrorResponse(s"Unhappy response($statusCode) fetching $errorInfo", statusCode))
        }
      }
      .recoverWith { case exc: UpstreamErrorResponse =>
        logger.warn(s"Received a ${exc.statusCode} response from secure-messaging-frontend while retrieving $errorInfo. ${exc.message}")
        Future.failed(exc)
      }

  private def constructInboxEndpointQueryParams(eori: String): Seq[(String, String)] = {
    val enrolmentParameter = ("enrolment", s"${enrolment}~EoriNumber~$eori")
    val filterParameter = ("tag", s"notificationType~${config.notificationType}")

    Seq(enrolmentParameter, filterParameter)
  }

  private val conversationEndpointQueryParams: Seq[(String, String)] = Seq(("showReplyForm", "true"))
}
