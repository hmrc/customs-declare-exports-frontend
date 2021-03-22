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

package config

import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class SecureMessagingConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {

  lazy val baseUrl = servicesConfig.baseUrl("secure-messaging")

  lazy val fetchInbox: String = getValue("microservice.services.secure-messaging.fetch-inbox")
  lazy val fetchMessage: String = getValue("microservice.services.secure-messaging.fetch-message")
  lazy val submitReply: String = getValue("microservice.services.secure-messaging.submit-reply")
  lazy val replyResult: String = getValue("microservice.services.secure-messaging.reply-result")

  lazy val fetchInboxEndpoint: String = s"${baseUrl}$fetchInbox"

  def fetchMessageEndpoint(client: String, conversationId: String): String =
    s"${baseUrl}$fetchMessage/$client/$conversationId"

  def submitReplyEndpoint(client: String, conversationId: String): String =
    s"${baseUrl}$submitReply/$client/$conversationId"

  def replyResultEndpoint(client: String, conversationId: String): String =
    s"${baseUrl}$replyResult"
      .replace("CLIENT_ID/CONVERSATION_ID", s"$client/$conversationId")

  private def getValue(path: String) =
    config
      .getOptional[String](path)
      .getOrElse(throw new IllegalStateException(s"Customs Declaration Export frontend, missing configuration key for $path"))
}
