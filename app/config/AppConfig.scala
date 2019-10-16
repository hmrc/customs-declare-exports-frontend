/*
 * Copyright 2019 HM Revenue & Customs
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

import java.util.Base64

import com.google.inject.{Inject, Singleton}
import features.Feature.Feature
import features.FeatureStatus.FeatureStatus
import features.{Feature, FeatureStatus}
import forms.Choice
import javax.inject.Named
import play.api.i18n.Lang
import play.api.mvc.Call
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.duration.FiniteDuration

@Singleton
class AppConfig @Inject()(
  val runModeConfiguration: Configuration,
  val environment: Environment,
  servicesConfig: ServicesConfig,
  @Named("appName") val appName: String
) {

  private val logger = Logger(this.getClass())

  private def loadConfig(key: String): String =
    runModeConfiguration.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  lazy val analyticsToken = loadConfig(s"google-analytics.token")
  lazy val analyticsHost = loadConfig(s"google-analytics.host")

  lazy val authUrl = servicesConfig.baseUrl("auth")
  lazy val loginUrl = loadConfig("urls.login")
  lazy val loginContinueUrl = loadConfig("urls.loginContinue")

  lazy val customsDeclarationsGoodsTakenOutOfEuUrl = loadConfig("urls.customsDeclarationsGoodsTakenOutOfEu")
  lazy val commodityCodesUrl = loadConfig("urls.commodityCodes")
  lazy val relevantLicensesUrl = loadConfig("urls.relevantLicenses")
  lazy val serviceAvailabilityUrl = loadConfig("urls.serviceAvailability")
  lazy val customsMovementsFrontendUrl = loadConfig("urls.customsMovementsFrontend")

  lazy val customsDeclareExports = servicesConfig.baseUrl("customs-declare-exports")

  lazy val declarations = servicesConfig.getConfString(
    "customs-declare-exports.declarations",
    throw new IllegalStateException("Missing configuration for Customs Declarations Exports submit declaration URI")
  )

  lazy val fetchSubmissions = servicesConfig.getConfString(
    "customs-declare-exports.fetch-submissions",
    throw new IllegalStateException("Missing configuration for Customs Declaration Exports fetch submission URI")
  )

  lazy val fetchNotifications = servicesConfig.getConfString(
    "customs-declare-exports.fetch-notifications",
    throw new IllegalStateException("Missing configuration for Customs Declarations Exports fetch notification URI")
  )

  lazy val cancelDeclaration = servicesConfig.getConfString(
    "customs-declare-exports.cancel-declaration",
    throw new IllegalStateException("Missing configuration for Customs Declaration Export cancel declaration URI")
  )

  lazy val languageTranslationEnabled =
    runModeConfiguration.getOptional[Boolean]("microservice.services.features.welsh-translation").getOrElse(true)

  lazy val isUsingImprovedErrorMessages =
    runModeConfiguration.getOptional[Boolean]("microservice.services.features.use-improved-error-messages").getOrElse(false)

  lazy val countriesCsvFilename: String = loadConfig("countryCodesCsvFilename")

  lazy val countryCodesJsonFilename: String = loadConfig("countryCodesJsonFilename")

  def languageMap: Map[String, Lang] = Map("english" -> Lang("en"), "cymraeg" -> Lang("cy"))

  lazy val cacheTimeToLive: FiniteDuration =
    servicesConfig.getDuration("mongodb.timeToLive").asInstanceOf[FiniteDuration]

  lazy val draftTimeToLive: FiniteDuration =
    servicesConfig.getDuration("draft.timeToLive").asInstanceOf[FiniteDuration]

  lazy val paginationItemsPerPage: Int = servicesConfig.getInt("pagination.itemsPerPage")

  lazy val defaultFeatureStatus: features.FeatureStatus.Value =
    FeatureStatus.withName(loadConfig(feature2Key(Feature.default)))

  def availableJourneys(): Seq[String] =
    runModeConfiguration
      .getOptional[String]("list-of-available-journeys")
      .map(_.split(","))
      .getOrElse(Array(Choice.AllowedChoiceValues.SupplementaryDec))
      .toSeq

  def featureStatus(feature: Feature): FeatureStatus =
    sys.props
      .get(feature2Key(feature))
      .map(str2FeatureStatus)
      .getOrElse(
        runModeConfiguration
          .getOptional[String](feature2Key(feature))
          .map(str2FeatureStatus)
          .getOrElse(defaultFeatureStatus)
      )

  def isFeatureOn(feature: Feature): Boolean = featureStatus(feature) == FeatureStatus.enabled

  def setFeatureStatus(feature: Feature, status: FeatureStatus): Unit =
    sys.props += (feature2Key(feature) -> status.toString)

  private def feature2Key(feature: Feature): String = s"microservice.services.features.$feature"

  private def str2FeatureStatus(str: String): FeatureStatus = FeatureStatus.withName(str)

  private def whitelistConfig(key: String): Seq[String] =
    Some(
      new String(
        Base64.getDecoder.decode(
          runModeConfiguration
            .getOptional[String](key)
            .getOrElse("")
        ),
        "UTF-8"
      )
    ).map(_.split(",")).getOrElse(Array.empty).toSeq

  val shutterPageToWhitelist: String = "whitelist.shutterPage"
  val whitelistedIps: String = "whitelist.ips"
  val whitelistExcludedPathsDefined: String = "whitelist.excludedPaths"
  val whitelisted: String = "whitelist.enabled"

  lazy val shutterPage: String = servicesConfig.getString(shutterPageToWhitelist)
  lazy val whitelistIps: Seq[String] = whitelistConfig(whitelistedIps)
  lazy val whitelistExcludedPaths: Seq[Call] =
    whitelistConfig(whitelistExcludedPathsDefined).map(path => Call("GET", path))
  lazy val whiteListEnabled: Boolean = servicesConfig.getBoolean(whitelisted)

}
