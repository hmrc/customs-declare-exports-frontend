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
import features.Feature
import play.api.Configuration

class TdrFeatureFlagsSpec extends UnitWithMocksSpec {

  private def buildTdrConfig(enabled: Boolean = false, key: Feature.Value = Feature.tdrUnauthorisedMessage): TdrFeatureFlags = {
    val config = Configuration(ConfigFactory.parseString(s"""
        |microservice.services.features.default=disabled
        |microservice.services.features.${key}=${asConfigVal(enabled)}
      """.stripMargin))

    new TdrFeatureFlags(new FeatureSwitchConfig(config))
  }

  private def asConfigVal(bool: Boolean): String = if (bool) "enabled" else "disabled"

  "TdrFeatureFlags.isTdrUnauthorisedMessageEnabled" should {

    "return true" when {
      "the related feature flag is enabled" in {
        buildTdrConfig(true).isTdrUnauthorisedMessageEnabled mustBe true
      }
    }

    "return false" when {
      "the related feature flag is disabled" in {
        buildTdrConfig().isTdrUnauthorisedMessageEnabled mustBe false
      }

      "the related feature config key doesn't exist" in {
        buildTdrConfig(true, Feature.betaBanner).isTdrUnauthorisedMessageEnabled mustBe false
      }
    }
  }

  "TdrFeatureFlags.showErrorPageVersionForTdr" should {

    "return true" when {
      "the related feature flag is enabled" in {
        buildTdrConfig(true, Feature.showErrorPageVersionForTdr).showErrorPageVersionForTdr mustBe true
      }
    }

    "return false" when {
      "the related feature flag is disabled" in {
        buildTdrConfig(false, Feature.showErrorPageVersionForTdr).showErrorPageVersionForTdr mustBe false
      }

      "the related feature config key doesn't exist" in {
        buildTdrConfig(true, Feature.betaBanner).showErrorPageVersionForTdr mustBe false
      }
    }
  }
}
