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
import play.api.Configuration
import unit.base.UnitSpec

class SfusConfigSpec extends UnitSpec {

  private def asConfigVal(bool: Boolean) = if (bool) "enabled" else "disabled"

  def featureSwitchConfig(config: Configuration): FeatureSwitchConfig = new FeatureSwitchConfig(config)

  private def generateFeaturesConfig(
    sfusEnabled: Boolean = false,
    sfusSecureMessagingEnabled: Boolean = false,
    sfusKey: String = "sfus",
    sfusSecureMessagingKey: String = "sfusSecureMessaging"
  ) = {
    val config = Configuration(ConfigFactory.parseString(s"""
        |microservice.services.features.default=disabled
        |microservice.services.features.${sfusKey}=${asConfigVal(sfusEnabled)}
        |microservice.services.features.${sfusSecureMessagingKey}=${asConfigVal(sfusSecureMessagingEnabled)}
        |urls.sfusUpload=sfusLink
        |urls.sfusInbox=sfusLink
      """.stripMargin))

    new SfusConfig(featureSwitchConfig(config), config)
  }

  private def generateUrlConfig(sfusUploadKey: String = "sfusUpload", sfusInboxKey: String = "sfusInbox") = {
    val config = Configuration(ConfigFactory.parseString(s"""
       |microservice.services.features.default=disabled
       |urls.${sfusUploadKey}=sfusLink
       |urls.${sfusInboxKey}=sfusLink
      """.stripMargin))

    new SfusConfig(featureSwitchConfig(config), config)
  }

  "SFUS config" when {
    "retrieving the value for sfus feature flag" should {
      "return true" when {
        "sfus feature is enabled" in {
          generateFeaturesConfig(sfusEnabled = true).isSfusUploadEnabled mustBe true
        }
      }

      "return false" when {
        "sfus feature is disabled" in {
          generateFeaturesConfig().isSfusUploadEnabled mustBe false
        }

        "sfus feature config key doesn't exist" in {
          generateFeaturesConfig(sfusEnabled = true, sfusKey = "WRONG").isSfusUploadEnabled mustBe false
        }
      }
    }

    "retrieving the url for sfusUpload link it" should {
      "return the correct sfusUpload url if present" in {
        generateUrlConfig().sfusUploadLink mustBe "sfusLink"
      }

      "throw an exception if url is missing" in {
        intercept[IllegalStateException] {
          generateUrlConfig(sfusUploadKey = "WRONG")
        }
      }
    }

    "retrieving the value for sfusSecureMessaging feature flag" should {
      "return true" when {
        "sfus feature is enabled" in {
          generateFeaturesConfig(sfusSecureMessagingEnabled = true).isSfusSecureMessagingEnabled mustBe true
        }
      }

      "return false" when {
        "sfus feature is disabled" in {
          generateFeaturesConfig().isSfusSecureMessagingEnabled mustBe false
        }

        "sfus feature config key doesn't exist" in {
          generateFeaturesConfig(sfusSecureMessagingEnabled = true, sfusSecureMessagingKey = "WRONG").isSfusSecureMessagingEnabled mustBe false
        }
      }
    }

    "retrieving the url for sfusInbox link it" should {
      "return the correct sfusInbox url if present" in {
        generateUrlConfig().sfusInboxLink mustBe "sfusLink"
      }

      "throw an exception if url is missing" in {
        intercept[IllegalStateException] {
          generateUrlConfig(sfusInboxKey = "WRONG")
        }
      }
    }
  }
}
