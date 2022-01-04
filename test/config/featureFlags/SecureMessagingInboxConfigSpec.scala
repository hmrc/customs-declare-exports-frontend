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
import features.SecureMessagingFeatureStatus.{disabled, exports, sfus, SecureMessagingFeatureStatus}
import org.scalatestplus.play.PlaySpec
import play.api.Configuration

class SecureMessagingInboxConfigSpec extends PlaySpec {

  private def buildSecureMessagingConfig(
    secureMessaging: SecureMessagingFeatureStatus = disabled,
    secureMessagingKey: String = "secureMessagingInbox",
    sfusInboxKey: String = "sfusInbox"
  ) = {
    val config = Configuration(ConfigFactory.parseString(s"""
        |microservice.services.features.default=disabled
        |microservice.services.features.${secureMessagingKey}=${secureMessaging}
        |urls.${sfusInboxKey}=${sfusInboxKey}
      """.stripMargin))

    new SecureMessagingInboxConfig(config)
  }

  "SecureMessagingConfig on isSfusSecureMessagingEnabled" should {

    "return true" when {
      "SecureMessaging feature is set to sfus" in {
        buildSecureMessagingConfig(secureMessaging = sfus).isSfusSecureMessagingEnabled mustBe true
      }
    }

    "return false" when {
      "SecureMessaging feature is disabled" in {
        buildSecureMessagingConfig().isSfusSecureMessagingEnabled mustBe false
      }

      "SecureMessaging feature is set to exports" in {
        buildSecureMessagingConfig(secureMessaging = exports).isSfusSecureMessagingEnabled mustBe false
      }

      "SecureMessaging feature config key doesn't exist" in {
        buildSecureMessagingConfig(secureMessaging = sfus, secureMessagingKey = "WRONG").isSfusSecureMessagingEnabled mustBe false
      }
    }
  }

  "SecureMessagingConfig on isExportsSecureMessagingEnabled" should {

    "return true" when {
      "SecureMessaging feature is set to exports" in {
        buildSecureMessagingConfig(secureMessaging = exports).isExportsSecureMessagingEnabled mustBe true
      }
    }

    "return false" when {
      "SecureMessaging feature is disabled" in {
        buildSecureMessagingConfig().isExportsSecureMessagingEnabled mustBe false
      }

      "SecureMessaging feature is set to sfus" in {
        buildSecureMessagingConfig(secureMessaging = sfus).isExportsSecureMessagingEnabled mustBe false
      }

      "SecureMessaging feature config key doesn't exist" in {
        buildSecureMessagingConfig(secureMessaging = exports, secureMessagingKey = "WRONG").isExportsSecureMessagingEnabled mustBe false
      }
    }
  }

  "SecureMessagingConfig on getSecureMessagingStatus" should {

    "return correct SecureMessagingFeatureStatus" when {

      "SecureMessaging feature is set to disabled" in {
        buildSecureMessagingConfig(secureMessaging = disabled).getSecureMessagingStatus mustBe disabled
      }

      "SecureMessaging feature is set to sfus" in {
        buildSecureMessagingConfig(secureMessaging = sfus).getSecureMessagingStatus mustBe sfus
      }

      "SecureMessaging feature is set to exports" in {
        buildSecureMessagingConfig(secureMessaging = exports).getSecureMessagingStatus mustBe exports
      }
    }
  }

  "SecureMessagingConfig on sfusInboxLink" should {

    "return the correct inbox url if present" in {
      buildSecureMessagingConfig().sfusInboxLink mustBe "sfusInbox"
    }

    "throw an exception if url is missing" in {
      intercept[IllegalStateException] {
        buildSecureMessagingConfig(sfusInboxKey = "WRONG")
      }
    }
  }

}
