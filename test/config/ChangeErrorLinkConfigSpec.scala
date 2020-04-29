/*
 * Copyright 2020 HM Revenue & Customs
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

class ChangeErrorLinkConfigSpec extends UnitSpec {

  private val configWithChangeLinkEnabled: Config =
    ConfigFactory.parseString("microservice.services.features.changeErrorLink=enabled")
  private val configWithChangeLinkDisabled: Config =
    ConfigFactory.parseString("microservice.services.features.changeErrorLink=disabled")
  private val emptyConfig: Config =
    ConfigFactory.parseString("microservice.services.features.default=disabled")

  def featureSwitchConfig(config: Configuration): FeatureSwitchConfig = new FeatureSwitchConfig(config)

  "Change Error Link config" should {

    "return true" when {

      "change error link feature is enabled" in {

        val changeErrorLinkConfig = new ChangeErrorLinkConfig(featureSwitchConfig(Configuration(configWithChangeLinkEnabled)))

        changeErrorLinkConfig.isEnabled mustBe true
      }
    }

    "return false" when {

      "ead document feature is diabled" in {

        val changeErrorLinkConfig = new ChangeErrorLinkConfig(featureSwitchConfig(Configuration(configWithChangeLinkDisabled)))

        changeErrorLinkConfig.isEnabled mustBe false
      }

      "ead document feature config doesn't exist" in {

        val changeErrorLinkConfig = new ChangeErrorLinkConfig(featureSwitchConfig(Configuration(emptyConfig)))

        changeErrorLinkConfig.isEnabled mustBe false
      }
    }
  }
}
