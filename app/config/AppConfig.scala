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

  val analyticsToken = loadConfig(s"google-analytics.token")
  val analyticsHost = loadConfig(s"google-analytics.host")

  lazy val authUrl = servicesConfig.baseUrl("auth")

  val loginUrl = loadConfig("urls.login")
  val loginContinueUrl = loadConfig("urls.loginContinue")

  val commodityCodeTariffPageUrl = loadConfig("urls.commodityCodeTariffPage")
  val suppUnitsCommodityCodeTariffPageUrl = loadConfig("urls.suppUnitsCommodityCodeTariffPage")
  val commodityCodesUrl = loadConfig("urls.commodityCodes")
  val nactCodesUrl = loadConfig("urls.nactCodes")
  val relevantLicensesUrl = loadConfig("urls.relevantLicenses")
  val serviceAvailabilityUrl = loadConfig("urls.serviceAvailability")
  val customsMovementsFrontendUrl = loadConfig("urls.customsMovementsFrontend")
  val exitSurveyUrl = loadConfig("urls.exitSurveyUrl")

  val notesForMucrConsolidationUrl = loadConfig("urls.notesForMucrConsolidation")

  val govUkUrl = loadConfig("urls.govUk")
  val tradeTariffUrl = loadConfig("urls.tradeTariff")
  val tariffCommoditiesUrl = loadConfig("urls.tariffCommodities")
  val tariffBrowseUrl = loadConfig("urls.tariffBrowse")
  val previousProcedureCodes = loadConfig("urls.previousProcedureCodes")
  val commodityCodeHelpUrl = loadConfig("urls.commodityCodeHelp")
  val ecicsToolUrl = loadConfig("urls.ecicsTool")
  val companyInformationRegister = loadConfig("urls.companyInformationRegister")

  val procedureCodesRemovalOfGoodsFromExciseWarehouse = loadConfig("urls.procedureCodes.removalOfGoodsFromExciseWarehouse")
  val procedureCodesOnwardSupplyRelief = loadConfig("urls.procedureCodes.onwardSupplyRelief")
  val procedureCodesEndUseRelief = loadConfig("urls.procedureCodes.endUseRelief")
  val procedureCodesInwardProcessing = loadConfig("urls.procedureCodes.inwardProcessing")
  val procedureCodesOutwardProcessing = loadConfig("urls.procedureCodes.outwardProcessing")
  val procedureCodesTemporaryExport = loadConfig("urls.procedureCodes.temporaryExport")
  val procedureCodesReExportFollowingSpecialProcedure = loadConfig("urls.procedureCodes.reExportFollowingSpecialProcedure")
  val permanentExportOrDispatch = PermanentExportOrDispatch(loadConfig)

  val additionalProcedureCodesOfCDs = loadConfig("urls.additionalProcedureCodesOfCDs")

  val notDeclarantEoriContactTeamUrl = loadConfig("urls.notDeclarant.eoriContactTeam")
  val generalEnquiriesHelpUrl = loadConfig("urls.generalEnquiriesHelp")
  val currencyCodesForDataElement410 = loadConfig("urls.currencyCodesForDataElement410")
  val exchangeRatesForCustoms = loadConfig("urls.exchangeRatesForCustoms")

  lazy val customsDeclareExportsBaseUrl = servicesConfig.baseUrl("customs-declare-exports")

  val emailFrontendUrl: String = loadConfig("urls.emailFrontendUrl")

  val rollOnRollOffPorts = loadConfig("urls.rollOnRollOffPorts")
  val railLocationCodes = loadConfig("urls.railLocationCodes")
  val customsDecCompletionRequirements = loadConfig("urls.customsDecCompletionRequirements")
  val locationCodeForAirports = loadConfig("urls.locationCodeForAirports")
  val certificateOfAgreementAirports = loadConfig("urls.certificateOfAgreementAirports")
  val locationCodeForMaritimePorts = loadConfig("urls.locationCodeForMaritimePorts")
  val locationCodeForTempStorage = loadConfig("urls.locationCodeForTempStorage")
  val designatedExportPlaceCodes = loadConfig("urls.designatedExportPlaceCodes")
  val locationCodesForCsePremises = loadConfig("urls.locationCodesForCsePremises")
  val goodsLocationCodesForDataElement = loadConfig("urls.goodsLocationCodesForDataElement")
  val tariffCdsChiefSupplement = loadConfig("urls.tariffCdsChiefSupplement")
  val tradeTariffSections = loadConfig("urls.tradeTariffSections")
  val additionalDocumentsLicenceTypes = loadConfig("urls.additionalDocumentsLicenceTypes")
  val additionalDocumentsUnionCodes = loadConfig("urls.additionalDocumentsUnionCodes")
  val additionalDocumentsReferenceCodes = loadConfig("urls.additionalDocumentsReferenceCodes")
  val additionalDocumentsUnitCodes = loadConfig("urls.additionalDocumentsUnitCodes")
  val unDangerousGoodsUrl = loadConfig("urls.unDangerousGoods")
  val licensesForExportingGoods = loadConfig("urls.licensesForExportingGoods")
  val locationCodesForPortsUsingGVMS = loadConfig("urls.locationCodesForPortsUsingGVMS")
  val additionalInformationAppendix4 = loadConfig("urls.additionalInformationAppendix4")

  val eoriService = loadConfig("urls.eoriService")
  val cdsRegister = loadConfig("urls.cdsRegister")
  val checkCustomsDeclarationService = loadConfig("urls.checkCustomsDeclarationService")
  val nationalExportSystemGuidance = loadConfig("urls.nationalExportSystemGuidance")
  val nationalClearanceHub = loadConfig("urls.nationalClearanceHub")
  val hmrcExchangeRatesFor2021 = loadConfig("urls.hmrcExchangeRatesFor2021")
  val nationalAdditionalCodes = loadConfig("urls.nationalAdditionalCodes")
  val commodityCode9306909000 = loadConfig("urls.commodityCode9306909000")
  val simplifiedDeclPreviousDoc = loadConfig("urls.simplifiedDeclPreviousDoc")
  val getGoodsMovementReference = loadConfig("urls.getGoodsMovementReference")

  val additionalDeclarationType = loadConfig("urls.additionalDeclarationType")
  val declareGoodsExported = loadConfig("urls.declareGoodsExported")
  val simplifiedDeclarationOccasionalUse = loadConfig("urls.simplifiedDeclarationOccasionalUse")

  val combinedPackaging = loadConfig("urls.combinedPackaging")

  lazy val selfBaseUrl: Option[String] = runModeConfiguration.getOptional[String]("platform.frontend.host")
  val giveFeedbackLink = {
    val contactFrontendUrl = loadConfig("microservice.services.contact-frontend.url")
    val contactFrontendServiceId = loadConfig("microservice.services.contact-frontend.serviceId")

    s"$contactFrontendUrl?service=$contactFrontendServiceId"
  }

  val guidance = Guidance(loadConfig)

  lazy val declarationsPath = servicesConfig.getConfString(
    "customs-declare-exports.declarations",
    throw new IllegalStateException("Missing configuration for Customs Declarations Exports declaration URI")
  )

  lazy val singleSubmissionPath = servicesConfig.getConfString(
    "customs-declare-exports.submission",
    throw new IllegalStateException("Missing configuration for Customs Declaration Exports single submission URI")
  )

  lazy val submissionsPath = servicesConfig.getConfString(
    "customs-declare-exports.submissions",
    throw new IllegalStateException("Missing configuration for Customs Declaration Exports submissions URI")
  )

  lazy val notificationsPath = servicesConfig.getConfString(
    "customs-declare-exports.notifications",
    throw new IllegalStateException("Missing configuration for Customs Declarations Exports notifications URI")
  )

  lazy val latestNotificationPath = servicesConfig.getConfString(
    "customs-declare-exports.latest-notification",
    throw new IllegalStateException("Missing configuration for Customs Declarations Exports latest-notification URI")
  )

  lazy val cancelDeclarationPath = servicesConfig.getConfString(
    "customs-declare-exports.cancel-declaration",
    throw new IllegalStateException("Missing configuration for Customs Declaration Export cancel declaration URI")
  )

  lazy val fetchMrnStatusPath = servicesConfig.getConfString(
    "customs-declare-exports.fetch-ead",
    throw new IllegalStateException("Missing configuration for Customs Declaration Export fetch mrn status URI")
  )

  lazy val fetchVerifiedEmailPath = servicesConfig.getConfString(
    "customs-declare-exports.fetch-verified-email",
    throw new IllegalStateException("Missing configuration for Customs Declaration Exports fetch verified email URI")
  )

  lazy val isUsingImprovedErrorMessages =
    runModeConfiguration.getOptional[Boolean]("microservice.services.features.use-improved-error-messages").getOrElse(false)

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
  lazy val allowListExcludedPaths: Seq[Call] = allowListConfig(allowListExcludedPathsDefined).map(path => Call("GET", path))
  lazy val allowListEnabled: Boolean = servicesConfig.getBoolean(allowListed)

  lazy val gtmContainer: String = servicesConfig.getString("tracking-consent-frontend.gtm.container")

  def tariffGuideUrl(key: String): String =
    runModeConfiguration.getOptional[String](key).getOrElse(throw new IllegalStateException(s"Missing tariff guide url key: $key"))

  val additionalProcedureCodes = loadConfig("files.codelists.additionalProcedureCodes")
  val additionalProcedureCodesForC21 = loadConfig("files.codelists.additionalProcedureCodesC21")
  val countryCodes = loadConfig("files.codelists.countryCodes")
  val countryCodeToAliasesLinkFile = loadConfig("files.codelists.countryCodeToAliasesLink")
  val countryCodeToShortNameLinkFile = loadConfig("files.codelists.countryCodeToShortNameLink")
  val dmsErrorCodes = loadConfig("files.codelists.dmsErrorCodes")
  val holderOfAuthorisationCodes = loadConfig("files.codelists.holderOfAuthorisationCodes")
  val procedureCodesListFile = loadConfig("files.codelists.procedureCodes")
  val procedureCodesForC21ListFile = loadConfig("files.codelists.procedureCodesC21")
  val procedureCodeToAdditionalProcedureCodesLinkFile = loadConfig("files.codelists.procedureCodeToAdditionalProcedureCodesLink")
  val procedureCodeToAdditionalProcedureCodesC21LinkFile = loadConfig("files.codelists.procedureCodeToAdditionalProcedureCodesC21Link")
  val goodsLocationCodeFile = loadConfig("files.codelists.goodsLocationCode")
  val goodsLocationCodeToLocationTypeFile = loadConfig("files.codelists.goodsLocationCodeToLocationTypeLink")
}
