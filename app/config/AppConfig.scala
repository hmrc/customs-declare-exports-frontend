/*
 * Copyright 2018 HM Revenue & Customs
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

import com.google.inject.{Inject, Singleton}
import features.Feature.Feature
import features.FeatureStatus.FeatureStatus
import features.{Feature, FeatureStatus}
import play.api.Mode.Mode
import play.api.i18n.Lang
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.config.{AppName, ServicesConfig}

@Singleton
class AppConfig @Inject() (override val runModeConfiguration: Configuration, val environment: Environment)
  extends ServicesConfig with AppName {

  override protected def mode: Mode = environment.mode
  override protected def appNameConfiguration: Configuration = runModeConfiguration

  private def loadConfig(key: String): String =
    runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  lazy val keyStoreSource: String = appName
  lazy val keyStoreUrl: String = baseUrl("keystore")
  lazy val sessionCacheDomain: String = getConfString(
    "cachable.session-cache.domain",
    throw new Exception(s"Could not find config 'cachable.session-cache.domain'")
  )

  lazy val analyticsToken = loadConfig(s"google-analytics.token")
  lazy val analyticsHost = loadConfig(s"google-analytics.host")

  lazy val authUrl = baseUrl("auth")
  lazy val loginUrl = loadConfig("urls.login")
  lazy val loginContinueUrl = loadConfig("urls.loginContinue")
  lazy val customsDeclarationsUrl = loadConfig("urls.customsDeclarations")

  lazy val customsDeclarationsEndpoint = baseUrl("customs-declarations")
  lazy val submitImportDeclarationUri = getConfString("customs-declarations.submit-uri",
    throw new IllegalStateException("Missing configuration for Customs Declarations submission URI"))
  lazy val developerHubClientId: String = loadConfig("hmrc-developers-hub.client-id")

  lazy val customsDeclareExports = baseUrl("customs-declare-exports")
  lazy val saveSubmissionResponse = getConfString("customs-declare-exports.save-response-uri",
    throw new IllegalStateException("Missing configuration for Customs Declarations Exports submission URI"))
  lazy val fetchNotifications = getConfString("customs-declare-exports.fetch-notifications",
    throw new IllegalStateException("Missing configuration for Customs Declarations Exports fetch notification URI"))

  lazy val customsDeclarationsApiVersion = getConfString("customs-declarations.api-version",
    throw new IllegalStateException("Missing configuration for Customs Declarations API version"))

  lazy val languageTranslationEnabled =
    runModeConfiguration.getBoolean("microservice.services.features.welsh-translation").getOrElse(true)

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  lazy val defaultFeatureStatus: features.FeatureStatus.Value =
    FeatureStatus.withName(loadConfig(feature2Key(Feature.default)))

  def featureStatus(feature: Feature): FeatureStatus =
    sys.props.get(feature2Key(feature)).map(str2FeatureStatus).getOrElse(
      runModeConfiguration.getString(feature2Key(feature)).map(str2FeatureStatus).getOrElse(
        defaultFeatureStatus
      )
    )

  def isFeatureOn(feature: Feature): Boolean = featureStatus(feature) == FeatureStatus.enabled

  def setFeatureStatus(feature: Feature, status: FeatureStatus): Unit =
    sys.props += (feature2Key(feature) -> status.toString)

  private def feature2Key(feature: Feature): String = s"microservice.services.features.$feature"

  private def str2FeatureStatus(str: String): FeatureStatus = FeatureStatus.withName(str)
}
