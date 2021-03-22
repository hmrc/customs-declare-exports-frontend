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

import com.typesafe.config.ConfigFactory
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class SecureMessagingConfigSpec extends PlaySpec {

  private def generateConfig(obfusticateKey: String = "") = {
    val config = Configuration(ConfigFactory.parseString(s"""microservice.services.secure-messaging {
        |      protocol = http
        |      host = localhost
        |      port = 9055
        |      fetch-inbox${obfusticateKey} = /secure-message-frontend/cds-file-upload-service/messages
        |      fetch-message${obfusticateKey} = /secure-message-frontend/cds-file-upload-service/conversation
        |      submit-reply${obfusticateKey} = /secure-message-frontend/cds-file-upload-service/conversation
        |      reply-result${obfusticateKey} = /secure-message-frontend/cds-file-upload-service/conversation/CLIENT_ID/CONVERSATION_ID/result
        |}
      """.stripMargin))

    new SecureMessagingConfig(config, new ServicesConfig(config))
  }

  "SecureMessagingConfig" when {

    "fetchInboxEndpoint val is referenced" which {
      "has a value defined in the config" should {
        "return the correct value" in {
          generateConfig().fetchInboxEndpoint mustBe "http://localhost:9055/secure-message-frontend/cds-file-upload-service/messages"
        }
      }

      "has no value defined in the config" should {
        "throw an exception" in {
          assertThrows[IllegalStateException] {
            generateConfig("wrong").fetchInboxEndpoint
          }
        }
      }
    }

    "fetchMessageEndpoint is called" which {
      "has a value defined in the config" should {
        "return the correct value" in {
          generateConfig()
            .fetchMessageEndpoint("client", "converationId") mustBe "http://localhost:9055/secure-message-frontend/cds-file-upload-service/conversation/client/converationId"
        }
      }

      "has no value defined in the config" should {
        "throw an exception" in {
          assertThrows[IllegalStateException] {
            generateConfig("wrong").fetchMessageEndpoint("client", "converationId")
          }
        }
      }
    }

    "submitReplyEndpoint is called" which {
      "has a value defined in the config" should {
        "return the correct value" in {
          generateConfig()
            .submitReplyEndpoint("client", "converationId") mustBe "http://localhost:9055/secure-message-frontend/cds-file-upload-service/conversation/client/converationId"
        }
      }

      "has no value defined in the config" should {
        "throw an exception" in {
          assertThrows[IllegalStateException] {
            generateConfig("wrong").submitReplyEndpoint("client", "converationId")
          }
        }
      }
    }

    "replyResultEndpoint is called" which {
      "has a value defined in the config" should {
        "return the correct value" in {
          generateConfig()
            .replyResultEndpoint("client", "converationId") mustBe "http://localhost:9055/secure-message-frontend/cds-file-upload-service/conversation/client/converationId/result"
        }
      }

      "has no value defined in the config" should {
        "throw an exception" in {
          assertThrows[IllegalStateException] {
            generateConfig("wrong").replyResultEndpoint("client", "converationId")
          }
        }
      }
    }
  }
}
