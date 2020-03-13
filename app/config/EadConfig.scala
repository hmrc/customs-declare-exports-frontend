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

import features.Feature.Feature
import features.{Feature, FeatureStatus}
import features.FeatureStatus.FeatureStatus
import javax.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class EadConfig @Inject()(configuration: Configuration) {

  private def loadConfig(key: String): String =
    configuration.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private def feature2Key(feature: Feature): String = s"microservice.services.features.$feature"

  private def str2FeatureStatus(str: String): FeatureStatus = FeatureStatus.withName(str)

  private lazy val defaultFeatureStatus: features.FeatureStatus.Value =
    FeatureStatus.withName(loadConfig(feature2Key(Feature.default)))

  private def getFeatureStatusFromProperties(feature: Feature): Option[FeatureStatus] =
    sys.props
      .get(feature2Key(feature))
      .map(str2FeatureStatus)

  private def getFeatureStatusFromConfig(feature: Feature): FeatureStatus =
    configuration
      .getOptional[String](feature2Key(feature))
      .map(str2FeatureStatus)
      .getOrElse(defaultFeatureStatus)

  private def featureStatus(feature: Feature): FeatureStatus =
    getFeatureStatusFromProperties(feature).getOrElse(getFeatureStatusFromConfig(feature))

  val isEadEnabled = {
    featureStatus(Feature.ead) == FeatureStatus.enabled
  }
}
