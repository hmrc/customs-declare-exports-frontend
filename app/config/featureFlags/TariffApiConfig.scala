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

import features.Feature
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class TariffApiConfig @Inject() (featureSwitchConfig: FeatureSwitchConfig, servicesConfig: ServicesConfig) {

  val tariffCommoditiesUri: String = s"${servicesConfig.baseUrl("tariff-api")}/api/v2/commodities"

  val isCommoditiesEnabled: Boolean = featureSwitchConfig.isFeatureOn(Feature.commodities)
}
