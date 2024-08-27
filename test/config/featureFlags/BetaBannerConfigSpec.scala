/*
 * Copyright 2024 HM Revenue & Customs
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

import base.UnitWithMocksSpec
import com.typesafe.config.ConfigFactory
import play.api.Configuration

class BetaBannerConfigSpec extends UnitWithMocksSpec {

  private val configWithBetaBannerEnabled: Configuration =
    Configuration(ConfigFactory.parseString("microservice.services.features.betaBanner=enabled"))

  private val configWithBetaBannerDisabled: Configuration =
    Configuration(ConfigFactory.parseString("microservice.services.features.betaBanner=disabled"))

  private val emptyConfig: Configuration =
    Configuration(ConfigFactory.parseString("microservice.services.features.default=disabled"))

  private def betaBannerConfig(configuration: Configuration) = new BetaBannerConfig(new FeatureSwitchConfig(configuration))

  "BetaBannerConfig on isBetaBannerEnabled" should {

    "return true" when {
      "the feature is enabled" in {
        betaBannerConfig(configWithBetaBannerEnabled).isBetaBannerEnabled mustBe true
      }
    }

    "return false" when {

      "the feature is disabled" in {
        betaBannerConfig(configWithBetaBannerDisabled).isBetaBannerEnabled mustBe false
      }

      "there is no config for the feature" in {
        betaBannerConfig(emptyConfig).isBetaBannerEnabled mustBe false
      }
    }
  }
}
