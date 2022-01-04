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

package config.featureFlags

import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class SecureMessagingConfig @Inject()(servicesConfig: ServicesConfig, secureMessagingInboxConfig: SecureMessagingInboxConfig) {

  lazy val isSecureMessagingEnabled: Boolean = secureMessagingInboxConfig.isExportsSecureMessagingEnabled

  val notificationType = "CDS-EXPORTS"

  val baseUrl = servicesConfig.baseUrl("secure-messaging")

  val fetchInbox: String = servicesConfig.getString("microservice.services.secure-messaging.fetch-inbox")
  val fetchMessage: String = servicesConfig.getString("microservice.services.secure-messaging.fetch-message")
  val submitReply: String = servicesConfig.getString("microservice.services.secure-messaging.submit-reply")
  val replyResult: String = servicesConfig.getString("microservice.services.secure-messaging.reply-result")

  lazy val fetchInboxEndpoint: String = s"${baseUrl}$fetchInbox"

  def fetchMessageEndpoint(client: String, conversationId: String): String =
    s"${baseUrl}$fetchMessage/$client/$conversationId"

  def submitReplyEndpoint(client: String, conversationId: String): String =
    s"${baseUrl}$submitReply/$client/$conversationId"

  def replyResultEndpoint(client: String, conversationId: String): String =
    s"${baseUrl}$replyResult"
      .replace("CLIENT_ID/CONVERSATION_ID", s"$client/$conversationId")
}
