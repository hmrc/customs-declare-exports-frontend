/*
 * Copyright 2023 HM Revenue & Customs
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

import features.Feature.Feature
import features.SecureMessagingFeatureStatus.SecureMessagingFeatureStatus
import features.{Feature, SecureMessagingFeatureStatus}
import play.api.Configuration

import javax.inject.{Inject, Singleton}

@Singleton
class SecureMessagingInboxConfig @Inject() (config: Configuration) {

  val sfusInboxLink: String =
    config
      .getOptional[String]("urls.sfusInbox")
      .getOrElse(throw new IllegalStateException("Missing configuration for CDS File Upload frontend inbox page url"))

  val getSecureMessagingStatus: SecureMessagingFeatureStatus = featureStatus(Feature.secureMessagingInbox)

  val isSfusSecureMessagingEnabled: Boolean = getSecureMessagingStatus == SecureMessagingFeatureStatus.sfus
  val isExportsSecureMessagingEnabled: Boolean = getSecureMessagingStatus == SecureMessagingFeatureStatus.exports

  private def featureStatus(feature: Feature): SecureMessagingFeatureStatus =
    getFeatureStatusFromProperties(feature).getOrElse(getFeatureStatusFromConfig(feature))

  private def getFeatureStatusFromProperties(feature: Feature): Option[SecureMessagingFeatureStatus] =
    sys.props
      .get(feature2Key(feature))
      .map(str2FeatureStatus)

  private def getFeatureStatusFromConfig(feature: Feature): SecureMessagingFeatureStatus =
    config
      .getOptional[String](feature2Key(feature))
      .map(str2FeatureStatus)
      .getOrElse(defaultFeatureStatus)

  private def feature2Key(feature: Feature): String = s"microservice.services.features.$feature"

  private def str2FeatureStatus(str: String): SecureMessagingFeatureStatus = SecureMessagingFeatureStatus.withName(str)

  private def defaultFeatureStatus: features.SecureMessagingFeatureStatus.Value =
    SecureMessagingFeatureStatus.withName(loadConfig(feature2Key(Feature.default)))

  private def loadConfig(key: String): String =
    config.getOptional[String](key).getOrElse(throw new IllegalStateException(s"Missing configuration key: $key"))

}
