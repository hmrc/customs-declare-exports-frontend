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

import com.typesafe.config.{Config, ConfigFactory}
import play.api.Configuration
import unit.base.UnitSpec

class SfusConfigSpec extends UnitSpec {

  private val configWithSfusEnabled: Config =
    ConfigFactory.parseString("""
      |microservice.services.features.sfus=enabled
      |urls.sfus=sfusLink
    """.stripMargin)
  private val configWithSfusDisabled: Config =
    ConfigFactory.parseString("""
      |microservice.services.features.sfus=disabled
      |urls.sfus=sfusLink
    """.stripMargin)
  private val notDefinedFeatureFlag: Config =
    ConfigFactory.parseString("""
      |microservice.services.features.default=disabled
      |urls.sfus=sfusLink
    """.stripMargin)
  private val emptyConfig: Config =
    ConfigFactory.parseString("microservice.services.features.default=disabled")

  def featureSwitchConfig(config: Configuration): FeatureSwitchConfig = new FeatureSwitchConfig(config)

  "Change Error Link config" should {

    "return true" when {

      "change error link feature is enabled" in {

        val sfusConfig = new SfusConfig(featureSwitchConfig(Configuration(configWithSfusEnabled)), Configuration(configWithSfusEnabled))

        sfusConfig.isSfusEnabled mustBe true
      }
    }

    "return false" when {

      "ead document feature is diabled" in {

        val sfusConfig = new SfusConfig(featureSwitchConfig(Configuration(configWithSfusDisabled)), Configuration(configWithSfusDisabled))

        sfusConfig.isSfusEnabled mustBe false
      }

      "ead document feature config doesn't exist" in {

        val sfusConfig = new SfusConfig(featureSwitchConfig(Configuration(notDefinedFeatureFlag)), Configuration(notDefinedFeatureFlag))

        sfusConfig.isSfusEnabled mustBe false
      }
    }

    "contains correct sfus url" in {

      val sfusConfig = new SfusConfig(featureSwitchConfig(Configuration(configWithSfusEnabled)), Configuration(configWithSfusEnabled))

      sfusConfig.sfusLink mustBe "sfusLink"
    }

    "throw an exception when url is missing" in {

      intercept[IllegalStateException] {
        new SfusConfig(featureSwitchConfig(Configuration(emptyConfig)), Configuration(emptyConfig))
      }
    }
  }
}
