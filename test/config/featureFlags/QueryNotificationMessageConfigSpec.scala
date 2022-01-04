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

import base.UnitWithMocksSpec
import com.typesafe.config.{Config, ConfigFactory}
import play.api.Configuration

class QueryNotificationMessageConfigSpec extends UnitWithMocksSpec {

  def featureSwitchConfig(config: Config): FeatureSwitchConfig = new FeatureSwitchConfig(Configuration(config))

  "QueryNotificationMessageConfig on isQueryNotificationMessageEnabled" should {

    "return true" when {

      "QueryNotificationMessageConfig feature is enabled" in {

        val configWithEnabledQueryNotificationMessage: Config =
          ConfigFactory.parseString("microservice.services.features.queryNotificationMessage=enabled")

        val queryNotificationMessageConfig = new QueryNotificationMessageConfig(featureSwitchConfig(configWithEnabledQueryNotificationMessage))

        queryNotificationMessageConfig.isQueryNotificationMessageEnabled mustBe true
      }
    }

    "return false" when {

      "QueryNotificationMessageConfig feature is disabled" in {

        val configWithDisabledQueryNotificationMessage: Config =
          ConfigFactory.parseString("microservice.services.features.queryNotificationMessage=disabled")

        val queryNotificationMessageConfig = new QueryNotificationMessageConfig(featureSwitchConfig(configWithDisabledQueryNotificationMessage))

        queryNotificationMessageConfig.isQueryNotificationMessageEnabled mustBe false
      }

      "QueryNotificationMessageConfig feature doesn't exist" in {

        val emptyConfig: Config = ConfigFactory.parseString("microservice.services.features.default=disabled")

        val queryNotificationMessageConfig = new QueryNotificationMessageConfig(featureSwitchConfig(emptyConfig))

        queryNotificationMessageConfig.isQueryNotificationMessageEnabled mustBe false
      }
    }
  }
}
