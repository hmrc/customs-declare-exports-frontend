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

import java.util.Base64

import com.google.inject.{Inject, Singleton}
import forms.Choice
import javax.inject.Named
import models.DeclarationType
import play.api.i18n.Lang
import play.api.mvc.Call
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.duration.FiniteDuration

@Singleton
class AppConfig @Inject()(
  val runModeConfiguration: Configuration,
  val environment: Environment,
  servicesConfig: ServicesConfig,
  @Named("appName") val appName: String
) {

  private def loadConfig(key: String): String =
    runModeConfiguration.getOptional[String](key).getOrElse(throw new IllegalStateException(s"Missing configuration key: $key"))

  lazy val analyticsToken = loadConfig(s"google-analytics.token")
  lazy val analyticsHost = loadConfig(s"google-analytics.host")

  lazy val authUrl = servicesConfig.baseUrl("auth")
  lazy val loginUrl = loadConfig("urls.login")
  lazy val loginContinueUrl = loadConfig("urls.loginContinue")

  lazy val customsDeclarationsGoodsTakenOutOfEuUrl = loadConfig("urls.customsDeclarationsGoodsTakenOutOfEu")
  lazy val commodityCodeForTaricPageUrl = loadConfig("urls.commodityCodeForTaricPage")
  lazy val commodityCodesUrl = loadConfig("urls.commodityCodes")
  lazy val nactCodesUrl = loadConfig("urls.nactCodes")
  lazy val relevantLicensesUrl = loadConfig("urls.relevantLicenses")
  lazy val serviceAvailabilityUrl = loadConfig("urls.serviceAvailability")
  lazy val customsMovementsFrontendUrl = loadConfig("urls.customsMovementsFrontend")
  lazy val exitSurveyUrl = loadConfig("urls.exitSurveyUrl")

  lazy val notesForMucrConsolidation = loadConfig("urls.notesForMucrConsolidation")
  lazy val arriveOrDepartExportsService = loadConfig("urls.arriveOrDepartExportsService")

  lazy val govUkUrl = loadConfig("urls.govUk")
  lazy val tradeTariffUrl = loadConfig("urls.tradeTariff")
  lazy val tariffCommoditiesUrl = loadConfig("urls.tariffCommodities")
  lazy val previousProcedureCodesUrl = loadConfig("urls.previousProcedureCodes")
  lazy val tradeTariffVol3ForCds2Url = loadConfig("urls.tradeTariffVol3ForCds2")
  lazy val commodityCodeHelpUrl = loadConfig("urls.commodityCodeHelp")
  lazy val ecicsToolUrl = loadConfig("urls.ecicsTool")
  lazy val companyInformationRegister = loadConfig("urls.companyInformationRegister")

  lazy val customsDeclareExportsBaseUrl = servicesConfig.baseUrl("customs-declare-exports")

  lazy val emailFrontendUrl: String = loadConfig("urls.emailFrontendUrl")

  lazy val govUkPageForTypeCO = loadConfig("urls.govUkPageForTypeCO")

  lazy val customsDecCompletionRequirements = loadConfig("urls.customsDecCompletionRequirements")
  lazy val locationCodeForAirports = loadConfig("urls.locationCodeForAirports")
  lazy val certificateOfAgreementAirports = loadConfig("urls.certificateOfAgreementAirports")
  lazy val locationCodeForMaritimePorts = loadConfig("urls.locationCodeForMaritimePorts")
  lazy val locationCodeForTempStorage = loadConfig("urls.locationCodeForTempStorage")
  lazy val designatedExportPlaceCodes = loadConfig("urls.designatedExportPlaceCodes")
  lazy val locationCodesForCsePremises = loadConfig("urls.locationCodesForCsePremises")
  lazy val goodsLocationCodesForDataElement = loadConfig("urls.goodsLocationCodesForDataElement")
  lazy val tariffCdsChiefSupplement = loadConfig("urls.tariffCdsChiefSupplement")

  lazy val selfBaseUrl: Option[String] = runModeConfiguration.getOptional[String]("platform.frontend.host")
  lazy val giveFeedbackLink = {
    val contactFrontendUrl = loadConfig("microservice.services.contact-frontend.url")
    val contactFrontendServiceId = loadConfig("microservice.services.contact-frontend.serviceId")

    s"$contactFrontendUrl?service=$contactFrontendServiceId"
  }

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

  lazy val fetchMrnStatus = servicesConfig.getConfString(
    "customs-declare-exports.fetch-ead",
    throw new IllegalStateException("Missing configuration for Customs Declaration Export fetch mrn status URI")
  )

  lazy val fetchVerifiedEmail = servicesConfig.getConfString(
    "customs-declare-exports.fetch-verified-email",
    throw new IllegalStateException("Missing configuration for Customs Declaration Exports fetch verified email URI")
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

  def availableJourneys(): Seq[String] =
    runModeConfiguration
      .getOptional[String]("list-of-available-journeys")
      .map(_.split(","))
      .getOrElse(Array(Choice.AllowedChoiceValues.Submissions))
      .toSeq

  def availableDeclarations(): Seq[String] =
    runModeConfiguration
      .getOptional[String]("list-of-available-declarations")
      .map(_.split(","))
      .getOrElse(Array(DeclarationType.STANDARD.toString))
      .toSeq

  private def allowListConfig(key: String): Seq[String] =
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

  val shutterPageToAllowList: String = "allowList.shutterPage"
  val allowListedIps: String = "allowList.ips"
  val allowListExcludedPathsDefined: String = "allowList.excludedPaths"
  val allowListed: String = "allowList.enabled"

  lazy val shutterPage: String = servicesConfig.getString(shutterPageToAllowList)
  lazy val allowListIps: Seq[String] = allowListConfig(allowListedIps)
  lazy val allowListExcludedPaths: Seq[Call] =
    allowListConfig(allowListExcludedPathsDefined).map(path => Call("GET", path))
  lazy val allowListEnabled: Boolean = servicesConfig.getBoolean(allowListed)

  lazy val gtmContainer: String = servicesConfig.getString("tracking-consent-frontend.gtm.container")

  def tariffGuideUrl(key: String): String =
    runModeConfiguration.getOptional[String](key).getOrElse(throw new IllegalStateException(s"Missing tariff guide url key: $key"))
}
