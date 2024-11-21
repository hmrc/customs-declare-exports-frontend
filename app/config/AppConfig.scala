/*
 * Copyright 2024 HM Revenue & Customs
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
import javax.inject.Named
import play.api.i18n.Lang
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.duration.FiniteDuration

@Singleton
class AppConfig @Inject() (
  val runModeConfiguration: Configuration,
  val environment: Environment,
  servicesConfig: ServicesConfig,
  @Named("appName") val appName: String
) {

  private def loadConfig(key: String): String =
    runModeConfiguration.getOptional[String](key).getOrElse(throw new IllegalStateException(s"Missing configuration key: $key"))

  private def loadOptionalConfig(key: String): Option[String] =
    runModeConfiguration.getOptional[String](key)

  val analyticsToken = loadConfig(s"google-analytics.token")
  val analyticsHost = loadConfig(s"google-analytics.host")

  lazy val authUrl = servicesConfig.baseUrl("auth")

  val loginUrl = loadConfig("urls.login")
  val loginContinueUrl = loadConfig("urls.loginContinue")

  val cdsUploadDocs = loadConfig("urls.cdsUploadDocs")

  val commodityCodeTariffPageUrl = loadConfig("urls.commodityCodeTariffPage")
  val suppUnitsCommodityCodeTariffPageUrl = loadConfig("urls.suppUnitsCommodityCodeTariffPage")
  val commodityCodesUrl = loadConfig("urls.commodityCodes")
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
  val simplifiedDeclPreviousDoc = loadConfig("urls.simplifiedDeclPreviousDoc")
  val getGoodsMovementReference = loadConfig("urls.getGoodsMovementReference")

  val standardDeclarationType = loadConfig("urls.standardDeclarationType")
  val declareGoodsExported = loadConfig("urls.declareGoodsExported")

  val errorCodesForCDS = loadConfig("urls.errorCodesForCDS")
  val errorWorkaroundsForCDS = loadConfig("urls.errorWorkaroundsForCDS")
  val reportProblemsByUsingCDS = loadConfig("urls.reportProblemsByUsingCDS")

  val combinedPackaging = loadConfig("urls.combinedPackaging")

  lazy val selfBaseUrl: Option[String] = loadOptionalConfig("play.frontend.host")
  val giveFeedbackLink = {
    val contactFrontendUrl = loadConfig("microservice.services.contact-frontend.url")
    val contactFrontendServiceId = loadConfig("microservice.services.contact-frontend.serviceId")

    s"$contactFrontendUrl?service=$contactFrontendServiceId"
  }

  val guidance = GuidanceConfig(loadConfig)

  lazy val declarationsPath = servicesConfig.getConfString(
    "customs-declare-exports.declarations",
    throw new IllegalStateException("Missing configuration for CDS Exports declarations URI")
  )

  lazy val draftDeclarationsPath = servicesConfig.getConfString(
    "customs-declare-exports.draft-declarations",
    throw new IllegalStateException("Missing configuration for CDS Exports declarations URI")
  )

  lazy val draftAmendmentPath = servicesConfig.getConfString(
    "customs-declare-exports.draft-amendment",
    throw new IllegalStateException("Missing configuration for CDS Exports draft amendment URI")
  )

  lazy val draftRejectionPath = servicesConfig.getConfString(
    "customs-declare-exports.draft-rejection",
    throw new IllegalStateException("Missing configuration for CDS Exports draft rejection URI")
  )

  lazy val draftByParentPath = servicesConfig.getConfString(
    "customs-declare-exports.draft-by-parent",
    throw new IllegalStateException("Missing configuration for CDS Exports draft rejection URI")
  )

  lazy val submissionPath = servicesConfig.getConfString(
    "customs-declare-exports.submission",
    throw new IllegalStateException("Missing configuration for CDS Exports single Submission URI")
  )

  lazy val submissionByActionPath = servicesConfig.getConfString(
    "customs-declare-exports.submission-by-action",
    throw new IllegalStateException("Missing configuration for CDS Exports single Submission by Action URI")
  )

  lazy val actionPath = servicesConfig.getConfString(
    "customs-declare-exports.action",
    throw new IllegalStateException("Missing configuration for CDS Exports single Submission's Action URI")
  )

  lazy val notificationsPath = servicesConfig.getConfString(
    "customs-declare-exports.notifications",
    throw new IllegalStateException("Missing configuration for CDS Exports Notifications URI")
  )

  lazy val latestNotificationPath = servicesConfig.getConfString(
    "customs-declare-exports.latest-notification",
    throw new IllegalStateException("Missing configuration for CDS Exports Single Notification URI")
  )

  lazy val amendmentsPath = servicesConfig.getConfString(
    "customs-declare-exports.amendments",
    throw new IllegalStateException("Missing configuration for CDS Exports amendment submission URI")
  )

  lazy val resubmitAmendmentPath = servicesConfig.getConfString(
    "customs-declare-exports.resubmit-amendment",
    throw new IllegalStateException("Missing configuration for CDS Exports amendment resubmission URI")
  )

  lazy val pageOfSubmissionsPath = servicesConfig.getConfString(
    "customs-declare-exports.page-of-submissions",
    throw new IllegalStateException("Missing configuration for CDS Exports page of Submissions URI")
  )

  lazy val lrnAlreadyUsedPath = servicesConfig.getConfString(
    "customs-declare-exports.lrn-already-used",
    throw new IllegalStateException("Missing configuration for CDS Exports lrn-already-used URI")
  )

  lazy val cancelDeclarationPath = servicesConfig.getConfString(
    "customs-declare-exports.cancel-declaration",
    throw new IllegalStateException("Missing configuration for CDS Exports cancel declaration URI")
  )

  lazy val fetchMrnStatusPath = servicesConfig.getConfString(
    "customs-declare-exports.fetch-ead",
    throw new IllegalStateException("Missing configuration for CDS Exports fetch mrn status URI")
  )

  lazy val fetchVerifiedEmailPath = servicesConfig.getConfString(
    "customs-declare-exports.fetch-verified-email",
    throw new IllegalStateException("Missing configuration for CDS Exports fetch verified email URI")
  )

  // Feature flags
  val isBetaBannerEnabled: Boolean = servicesConfig.getBoolean("features.betaBanner")
  val isTdrVersion: Boolean = servicesConfig.getBoolean("features.tdrVersion")

  lazy val languages: Seq[String] = runModeConfiguration.get[Seq[String]]("play.i18n.langs")

  def languageMap: Map[String, Lang] = Map("english" -> Lang("en"), "cymraeg" -> Lang("cy"))

  lazy val cacheTimeToLive: FiniteDuration =
    servicesConfig.getDuration("mongodb.timeToLive").asInstanceOf[FiniteDuration]

  lazy val draftTimeToLive: FiniteDuration =
    servicesConfig.getDuration("draft.timeToLive").asInstanceOf[FiniteDuration]

  lazy val gtmContainer: String = servicesConfig.getString("tracking-consent-frontend.gtm.container")

  def tariffGuideUrl(key: String): String =
    runModeConfiguration.getOptional[String](key).getOrElse(throw new IllegalStateException(s"Missing tariff guide url key: $key"))

  val additionalProcedureCodes = loadConfig("files.codelists.additionalProcedureCodes")
  val additionalProcedureCodesForC21 = loadConfig("files.codelists.additionalProcedureCodesC21")
  val countryCodes = loadConfig("files.codelists.countryCodes")
  val countryCodeToAliasesLinkFile = loadConfig("files.codelists.countryCodeToAliasesLink")
  val countryCodeToShortNameLinkFile = loadConfig("files.codelists.countryCodeToShortNameLink")
  val dmsErrorCodes = loadConfig("files.codelists.dmsErrorCodes")
  val procedureCodesListFile = loadConfig("files.codelists.procedureCodes")
  val procedureCodesForC21ListFile = loadConfig("files.codelists.procedureCodesC21")
  val procedureCodeToAdditionalProcedureCodesLinkFile = loadConfig("files.codelists.procedureCodeToAdditionalProcedureCodesLink")
  val procedureCodeToAdditionalProcedureCodesC21LinkFile = loadConfig("files.codelists.procedureCodeToAdditionalProcedureCodesC21Link")
  val procedureCodesLinkFile = loadConfig("files.codelists.procedureCodesLink")
  val additionalDocumentCodeLinkFile = loadConfig("files.codelists.additionalDocumentCodeLink")
  val additionalDocumentStatusCodeLinkFile = loadConfig("files.codelists.additionalDocumentStatusCodeLink")
  val goodsLocationCodeToLocationTypeFile = loadConfig("files.codelists.goodsLocationCodeToLocationTypeLink")
  val packageTypeCodeFile = loadConfig("files.codelists.packageTypeCode")
  val officeOfExitsCodeFile = loadConfig("files.codelists.officeOfExits")
  val customsOfficesCodeFile = loadConfig("files.codelists.customsOffices")
  val documentTypeCodeFile = loadConfig("files.codelists.docTypes")
  val documentTypeCodeLinkFile = loadConfig("files.codelists.docTypeLinks")
  val currencyCodesFile = loadConfig("files.codelists.currencyCodes")

  val glcAirports16a = loadConfig("files.codelists.glc.airports")
  val glcCoaAirports16b = loadConfig("files.codelists.glc.coa-airports")
  val glcMaritimeAndWharves16c = loadConfig("files.codelists.glc.maritime-ports-and-wharves")
  val glcItsf16d = loadConfig("files.codelists.glc.itsf")
  val glcRemoteItsf16e = loadConfig("files.codelists.glc.remote-itsf")
  val glcExternalItsf16f = loadConfig("files.codelists.glc.external-itsf")
  val glcBorderInspectionPosts16g = loadConfig("files.codelists.glc.border-inspection-posts")
  val glcApprovedDipositories16h = loadConfig("files.codelists.glc.approved-dipositories")
  val glcPlaceNamesGB16i = loadConfig("files.codelists.glc.gb-place-names")
  val glcOtherLocationCodes16j = loadConfig("files.codelists.glc.other-location-codes")
  val glcDep16k = loadConfig("files.codelists.glc.dep")
  val glcCse16l = loadConfig("files.codelists.glc.cse")
  val glcRail16m = loadConfig("files.codelists.glc.rail")
  val glcActs16n = loadConfig("files.codelists.glc.acts")
  val glcRoro16r = loadConfig("files.codelists.glc.roro")
  val glcGvms16s = loadConfig("files.codelists.glc.gvms")

  val holderOfAuthorisationCodeFile = loadConfig("files.codelists.hoa.hoa-codes")
  val taggedHolderOfAuthorisationCodeFile = loadConfig("files.codelists.hoa.tagged-hoa-codes")

  val taggedTransportCodeFile = loadConfig("files.codelists.tagged-transport-codes")
}
