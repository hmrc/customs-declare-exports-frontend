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

class EadConfigSpec extends UnitSpec {

  private val configWithEnabledEAD: Config =
    ConfigFactory.parseString("microservice.services.features.ead=enabled")
  private val configWithDisabledEAD: Config =
    ConfigFactory.parseString("microservice.services.features.ead=disabled")
  private val emptyConfig: Config =
    ConfigFactory.parseString("microservice.services.features.default=disabled")

  def featureSwitchConfig(config: Configuration): FeatureSwitchConfig = new FeatureSwitchConfig(config)

  "EAD Document config" should {

    "return true" when {

      "ead document feature is enabled" in {

        val eadConfig = new EadConfig(featureSwitchConfig(Configuration(configWithEnabledEAD)))

        eadConfig.isEadEnabled mustBe true
      }
    }

    "return false" when {

      "ead document feature is diabled" in {

        val eadConfig = new EadConfig(featureSwitchConfig(Configuration(configWithDisabledEAD)))

        eadConfig.isEadEnabled mustBe false
      }

      "ead document feature config doesn't exist" in {

        val eadConfig = new EadConfig(featureSwitchConfig(Configuration(emptyConfig)))

        eadConfig.isEadEnabled mustBe false
      }
    }
  }
}
