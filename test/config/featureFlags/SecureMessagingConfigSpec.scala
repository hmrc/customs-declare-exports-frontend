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

import com.typesafe.config.ConfigFactory
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class SecureMessagingConfigSpec extends PlaySpec with MockitoSugar {

  private def generateConfig(obfuscated: String = "", secureMessagingEnabled: Boolean = true) = {
    val config = Configuration(ConfigFactory.parseString(s"""microservice.services.secure-messaging {
        |      protocol = http
        |      host = localhost
        |      port = 9055
        |      fetch-inbox${obfuscated} = /secure-message-frontend/customs-declare-exports/messages
        |      fetch-message${obfuscated} = /secure-message-frontend/customs-declare-exports/conversation
        |      submit-reply${obfuscated} = /secure-message-frontend/customs-declare-exports/conversation
        |      reply-result${obfuscated} = /secure-message-frontend/customs-declare-exports/conversation/CLIENT_ID/CONVERSATION_ID/result
        |}
      """.stripMargin))

    val secureMessagingInboxConfig = mock[SecureMessagingInboxConfig]
    when(secureMessagingInboxConfig.isExportsSecureMessagingEnabled).thenReturn(secureMessagingEnabled)

    new SecureMessagingConfig(new ServicesConfig(config), secureMessagingInboxConfig)
  }

  "SecureMessagingConfig" when {

    "fetchInboxEndpoint val is referenced" which {
      "has a value defined in the config" should {
        "return the correct value" in {
          generateConfig().fetchInboxEndpoint mustBe
            "http://localhost:9055/secure-message-frontend/customs-declare-exports/messages"
        }
      }

      "has no value defined in the config" should {
        "throw an exception" in {
          assertThrows[RuntimeException] {
            generateConfig("wrong").fetchInboxEndpoint
          }
        }
      }
    }

    "fetchMessageEndpoint is called" which {
      "has a value defined in the config" should {
        "return the correct value" in {
          generateConfig().fetchMessageEndpoint("client", "conversationId") mustBe
            "http://localhost:9055/secure-message-frontend/customs-declare-exports/conversation/client/conversationId"
        }
      }

      "has no value defined in the config" should {
        "throw an exception" in {
          assertThrows[RuntimeException] {
            generateConfig("wrong").fetchMessageEndpoint("client", "conversationId")
          }
        }
      }
    }

    "submitReplyEndpoint is called" which {
      "has a value defined in the config" should {
        "return the correct value" in {
          generateConfig().submitReplyEndpoint("client", "conversationId") mustBe
            "http://localhost:9055/secure-message-frontend/customs-declare-exports/conversation/client/conversationId"
        }
      }

      "has no value defined in the config" should {
        "throw an exception" in {
          assertThrows[RuntimeException] {
            generateConfig("wrong").submitReplyEndpoint("client", "conversationId")
          }
        }
      }
    }

    "replyResultEndpoint is called" which {
      "has a value defined in the config" should {
        "return the correct value" in {
          generateConfig().replyResultEndpoint("client", "conversationId") mustBe
            "http://localhost:9055/secure-message-frontend/customs-declare-exports/conversation/client/conversationId/result"
        }
      }

      "has no value defined in the config" should {
        "throw an exception" in {
          assertThrows[RuntimeException] {
            generateConfig("wrong").replyResultEndpoint("client", "converationId")
          }
        }
      }
    }
  }
}
