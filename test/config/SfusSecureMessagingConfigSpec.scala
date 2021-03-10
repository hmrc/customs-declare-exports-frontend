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

class SfusSecureMessagingConfigSpec extends UnitSpec {

  private val configWithSfusSecureMessagingEnabled = ConfigFactory.parseString("microservice.services.features.sfusSecureMessaging=enabled")
  private val configWithSfusSecureMessagingDisabled = ConfigFactory.parseString("microservice.services.features.sfusSecureMessaging=disabled")
  private val emptyConfig = ConfigFactory.parseString("microservice.services.features.default=disabled")

  def featureSwitchConfig(config: Configuration): FeatureSwitchConfig = new FeatureSwitchConfig(config)

  "SFUS Secure Messaging config" should {

    "return true" when {

      "the SFUS Secure Messaging feature is enabled" in {
        val sfusSecureMessagingConfig = new SfusSecureMessagingConfig(featureSwitchConfig(Configuration(configWithSfusSecureMessagingEnabled)))
        assert(sfusSecureMessagingConfig.isEnabled)
      }
    }

    "return false" when {

      "the SFUS Secure Messaging feature is disabled" in {
        val sfusSecureMessagingConfig = new SfusSecureMessagingConfig(featureSwitchConfig(Configuration(configWithSfusSecureMessagingDisabled)))
        assert(sfusSecureMessagingConfig.isDisabled)
      }

      "the SFUS Secure Messaging feature was not configure" in {
        val sfusSecureMessagingConfig = new SfusSecureMessagingConfig(featureSwitchConfig(Configuration(emptyConfig)))
        assert(sfusSecureMessagingConfig.isDisabled)
      }
    }
  }
}
