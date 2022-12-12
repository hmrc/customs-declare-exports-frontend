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

import base.UnitWithMocksSpec
import com.typesafe.config.{Config, ConfigFactory}
import config.AppConfigSpec.configBareMinimum
import forms.Choice
import models.DeclarationType
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

class AppConfigSpec extends UnitWithMocksSpec {

  private val environment = Environment.simple()

  private val validConfig: Config =
    ConfigFactory.parseString(configBareMinimum + """
        |microservice.services.auth.host=localhostauth
        |
        |tracking-consent-frontend.gtm.container=a
        |
        |list-of-available-journeys="CRT,CAN,SUB"
        |list-of-available-declarations="STANDARD,SUPPLEMENTARY"
        |microservice.services.features.use-improved-error-messages=true
        |microservice.services.customs-declare-exports.host=localhost
        |microservice.services.customs-declare-exports.port=9875
        |microservice.services.customs-declare-exports.submit-declaration=/declaration
        |microservice.services.customs-declare-exports.declarations=/v2/declaration
        |microservice.services.customs-declare-exports.cancel-declaration=/cancellations
        |microservice.services.customs-declare-exports.notifications=/notifications
        |microservice.services.customs-declare-exports.submission=/submission
        |microservice.services.customs-declare-exports.page-of-submissions=/paginated-submissions
        |microservice.services.customs-declare-exports.fetch-ead=/ead
        |microservice.services.customs-declare-exports-movements.host=localhost
        |microservice.services.customs-declare-exports-movements.port=9876
        |microservice.services.customs-declare-exports-movements.save-movement-uri=/save-movement-submission
        |play.frontend.host="self/base-url"
      """.stripMargin)

  private val validServicesConfiguration = Configuration(validConfig)
  private val missingValuesServicesConfiguration = Configuration(ConfigFactory.parseString(configBareMinimum))

  private def servicesConfig(conf: Configuration) = new ServicesConfig(conf)
  private def appConfig(conf: Configuration) = new AppConfig(conf, environment, servicesConfig(conf), "AppName")

  val validAppConfig: AppConfig = appConfig(validServicesConfiguration)
  val missingAppConfig: AppConfig = appConfig(missingValuesServicesConfiguration)

  "The config" should {

    "have analytics token" in {
      validAppConfig.analyticsToken must be("N/A")
    }

    "have analytics host" in {
      validAppConfig.analyticsHost must be("localhostGoogle")
    }

    "have gtm container" in {
      validAppConfig.gtmContainer must be("a")
    }

    "have auth URL" in {
      validAppConfig.authUrl must be("http://localhostauth:9988")
    }

    "have login URL" in {
      validAppConfig.loginUrl must be("http://localhost:9949/auth-login-stub/gg-sign-in")
    }

    "have commodityCodes URL" in {
      validAppConfig.commodityCodesUrl must be("https://www.gov.uk/guidance/finding-commodity-codes-for-imports-or-exports")
    }

    "have relevantLicenses URL" in {
      validAppConfig.relevantLicensesUrl must be("https://www.gov.uk/starting-to-export/licences")
    }

    "have serviceAvailability URL" in {
      validAppConfig.serviceAvailabilityUrl must be("https://www.gov.uk/guidance/customs-declaration-service-service-availability-and-issues")
    }

    "have customsMovementsFrontend URL" in {
      validAppConfig.customsMovementsFrontendUrl must be("http://url-to-movements-frontend/start")
    }

    "have exitSurvey URL" in {
      validAppConfig.exitSurveyUrl must be("http://localhost:9514/feedback/customs-declare-exports-frontend")
    }

    "have emailFrontendUrl URL" in {
      validAppConfig.emailFrontendUrl must be("http://localhost:9898/manage-email-cds/service/customs-declare-exports")
    }

    "have tradeTariff URL" in {
      validAppConfig.tradeTariffUrl must be("https://www.gov.uk/trade-tariff")
    }

    "have tariffCommodities URL" in {
      validAppConfig.tariffCommoditiesUrl must be("https://www.trade-tariff.service.gov.uk/commodities/")
    }

    "have tariffBrowse URL" in {
      validAppConfig.tariffBrowseUrl must be("https://www.trade-tariff.service.gov.uk/browse/")
    }

    "have companyInformationRegister URL" in {
      validAppConfig.companyInformationRegister must be("http://companyInformationRegister")
    }

    "have customsDecCompletionRequirements URL" in {
      validAppConfig.customsDecCompletionRequirements must be("http://customsDecCompletionRequirements")
    }

    "have locationCodeForAirports URL" in {
      validAppConfig.locationCodeForAirports must be("http://locationCodeForAirports")
    }

    "have locationCodesForPortsUsingGVMS URL" in {
      validAppConfig.locationCodesForPortsUsingGVMS must be("http://locationCodesForPortsUsingGVMS")
    }

    "have certificateOfAgreementAirports URL" in {
      validAppConfig.certificateOfAgreementAirports must be("http://certificateOfAgreementAirports")
    }

    "have locationCodeForMaritimePorts URL" in {
      validAppConfig.locationCodeForMaritimePorts must be("http://locationCodeForMaritimePorts")
    }

    "have locationCodeForTempStorage URL" in {
      validAppConfig.locationCodeForTempStorage must be("http://locationCodeForTempStorage")
    }

    "have designatedExportPlaceCodes URL" in {
      validAppConfig.designatedExportPlaceCodes must be("http://designatedExportPlaceCodes")
    }

    "have locationCodesForCsePremises URL" in {
      validAppConfig.locationCodesForCsePremises must be("http://locationCodesForCsePremises")
    }

    "have goodsLocationCodesForDataElement URL" in {
      validAppConfig.goodsLocationCodesForDataElement must be("http://goodsLocationCodesForDataElement")
    }

    "have tariffCdsChiefSupplement URL" in {
      validAppConfig.tariffCdsChiefSupplement must be("http://tariffCdsChiefSupplement")
    }

    "have tradeTariffSections URL" in {
      validAppConfig.tradeTariffSections must be("http://tradeTariffSections")
    }

    "have additionalDocumentsLicenceTypes URL" in {
      validAppConfig.additionalDocumentsLicenceTypes must be("http://additionalDocumentsLicenceTypes")
    }

    "have additionalDocumentsUnionCodes URL" in {
      validAppConfig.additionalDocumentsUnionCodes must be("http://additionalDocumentsUnionCodes")
    }

    "have additionalDocumentsReferenceCodes URL" in {
      validAppConfig.additionalDocumentsReferenceCodes must be("http://additionalDocumentsReferenceCodes")
    }

    "have additionalDocumentsUnitCodes URL" in {
      validAppConfig.additionalDocumentsUnitCodes must be("http://additionalDocumentsUnitCodes")
    }

    "have procedureCodeListFile file path" in {
      validAppConfig.procedureCodesListFile must be("procedureCodes")
    }

    "have procedureCodeForC21ListFile file path" in {
      validAppConfig.procedureCodesForC21ListFile must be("procedureCodesC21")
    }

    "have additionalProcedureCodesListFile file path" in {
      validAppConfig.additionalProcedureCodes must be("additionalProcedureCodes")
    }

    "have additionalProcedureCodesForC21ListFile file path" in {
      validAppConfig.additionalProcedureCodesForC21 must be("additionalProcedureCodesC21")
    }

    "have procedureCodeToAdditionalProcedureCodesLinkFile file path" in {
      validAppConfig.procedureCodeToAdditionalProcedureCodesLinkFile must be("procedureCodeToAdditionalProcedureCodesLink")
    }

    "have procedureCodeToAdditionalProcedureCodesC21LinkFile file path" in {
      validAppConfig.procedureCodeToAdditionalProcedureCodesC21LinkFile must be("procedureCodeToAdditionalProcedureCodesC21Link")
    }

    "have countryCodes file path" in {
      validAppConfig.countryCodes must be("/code-lists/countryCodes.json")
    }

    "have countryCodeToAliasesLink file path" in {
      validAppConfig.countryCodeToAliasesLinkFile must be("countryCodeToAliasesLink")
    }

    "have countryCodeToShortNameLink file path" in {
      validAppConfig.countryCodeToShortNameLinkFile must be("countryCodeToShortNameLink")
    }

    "have goodsLocationCode file path" in {
      validAppConfig.goodsLocationCodeFile must be("/code-lists/goods-locations-codes/dep-16k-from-30-11-2022.json")
    }

    "have goodsLocationCodeToLocationType file path" in {
      validAppConfig.goodsLocationCodeToLocationTypeFile must be("goodsLocationCodeToLocationTypeLink")
    }

    "load the Choice options when list-of-available-journeys is defined" in {
      val choices = validAppConfig.availableJourneys()
      choices.size must be(3)

      choices must contain(Choice.AllowedChoiceValues.CreateDec)
      choices must contain(Choice.AllowedChoiceValues.CancelDec)
      choices must contain(Choice.AllowedChoiceValues.Dashboard)
    }

    "load the Declaration options when list-of-available-declarations is defined" in {
      val choices = validAppConfig.availableDeclarations()
      choices.size must be(2)

      choices must contain(DeclarationType.STANDARD.toString)
      choices must contain(DeclarationType.SUPPLEMENTARY.toString)
    }

    "have login continue URL" in {
      validAppConfig.loginContinueUrl must be("http://localhost:9000/customs-declare-exports-frontend")
    }

    "have improved error messages feature toggle set to false if not defined" in {
      missingAppConfig.isUsingImprovedErrorMessages must be(false)
    }

    "have improved error messages feature toggle set to true if defined" in {
      validAppConfig.isUsingImprovedErrorMessages must be(true)
    }

    "have language map with English" in {
      validAppConfig.languageMap.contains("english") must be(true)
    }

    "have language map with Cymraeg" in {
      validAppConfig.languageMap.contains("cymraeg") must be(true)
    }

    "have customs declare exports URL" in {
      validAppConfig.customsDeclareExportsBaseUrl must be("http://localhost:9875")
    }

    "have submit declaration URL" in {
      validAppConfig.declarationsPath must be("/v2/declaration")
    }

    "have cancel declaration URL" in {
      validAppConfig.cancelDeclarationPath must be("/cancellations")
    }

    "have ead URL" in {
      validAppConfig.fetchMrnStatusPath must be("/ead")
    }

    "have Notifications URL" in {
      validAppConfig.notificationsPath must be("/notifications")
    }

    "have single Submission URL" in {
      validAppConfig.submissionPath must be("/submission")
    }

    "have URL to fetch a page of Submissions" in {
      validAppConfig.pageOfSubmissionsPath must be("/paginated-submissions")
    }

    "have selfBaseUrl" in {
      validAppConfig.selfBaseUrl must be(defined)
      validAppConfig.selfBaseUrl.get must be("self/base-url")
    }

    "have additionalProcedureCodesOfCDs URL" in {
      validAppConfig.additionalProcedureCodesOfCDs must be("http://additionalProcedureCodesOfCDs")
    }

    "have unDangerousGoods URL" in {
      validAppConfig.unDangerousGoodsUrl must be("http://unDangerousGoods")
    }

    "have link for 'give feedback'" in {
      validAppConfig.giveFeedbackLink must be("/contact-frontend-url?service=DeclarationServiceId")
    }

    "have ttl lifetime" in {
      validAppConfig.cacheTimeToLive must be(FiniteDuration(24, "h"))
    }

    "have draft lifetime" in {
      validAppConfig.draftTimeToLive must be(FiniteDuration(30, TimeUnit.DAYS))
    }

    "have single Choice options when list-of-available-journeys is not defined" in {
      missingAppConfig.availableJourneys().size must be(1)
      missingAppConfig.availableJourneys() must contain(Choice.AllowedChoiceValues.Dashboard)
    }

    "have single Declaration type options when list-of-available-declarations is not defined" in {
      missingAppConfig.availableDeclarations().size must be(1)
      missingAppConfig.availableDeclarations() must contain(DeclarationType.STANDARD.toString)
    }

    "empty selfBaseUrl when the key is missing" in {
      missingAppConfig.selfBaseUrl must be(None)
    }

    "throw an exception" when {

      "gtm.container is missing" in {
        intercept[Exception](missingAppConfig.gtmContainer).getMessage must be("Could not find config key 'tracking-consent-frontend.gtm.container'")
      }

      "auth.host is missing" in {
        intercept[Exception](missingAppConfig.authUrl).getMessage must be("Could not find config key 'auth.host'")
      }

      "customs-declare-exports.host is missing" in {
        intercept[Exception](missingAppConfig.customsDeclareExportsBaseUrl).getMessage must be(
          "Could not find config key 'customs-declare-exports.host'"
        )
      }

      "submit declaration uri is missing" in {
        intercept[Exception](missingAppConfig.declarationsPath).getMessage must be(
          "Missing configuration for Customs Declarations Exports declarations URI"
        )
      }

      "cancel declaration uri is missing" in {
        intercept[Exception](missingAppConfig.cancelDeclarationPath).getMessage must be(
          "Missing configuration for Customs Declaration Export cancel declaration URI"
        )
      }

      "fetch mrn status uri is missing" in {
        intercept[Exception](missingAppConfig.fetchMrnStatusPath).getMessage must be(
          "Missing configuration for Customs Declaration Export fetch mrn status URI"
        )
      }

      "fetch page of Submissions uri is missing" in {
        intercept[Exception](missingAppConfig.pageOfSubmissionsPath).getMessage must be(
          "Missing configuration for Customs Declaration Exports page of submissions URI"
        )
      }

      "Single Submission uri is missing" in {
        intercept[Exception](missingAppConfig.submissionPath).getMessage must be(
          "Missing configuration for Customs Declaration Exports single submission URI"
        )
      }

      "Notifications uri is missing" in {
        intercept[Exception](missingAppConfig.notificationsPath).getMessage must be(
          "Missing configuration for Customs Declarations Exports notifications URI"
        )
      }
    }
  }
}

object AppConfigSpec {
  val configBareMinimum =
    """
      |urls.login="http://localhost:9949/auth-login-stub/gg-sign-in"
      |urls.loginContinue="http://localhost:9000/customs-declare-exports-frontend"
      |
      |google-analytics.token=N/A
      |google-analytics.host=localhostGoogle
      |
      |urls.govUk = "https://www.gov.uk"
      |urls.cdsUploadDocs = "https://www.gov.uk/guidance/send-documents-to-support-declarations-for-the-customs-declaration-service"
      |urls.commodityCodeTariffPage = "https://www.trade-tariff.service.gov.uk/commodities/NNNNNNNNNN#export"
      |urls.suppUnitsCommodityCodeTariffPage = "https://www.trade-tariff.service.gov.uk/commodities/NNNNNNNNNN"
      |urls.previousProcedureCodes = "https://www.gov.uk/government/publications/appendix-1-de-110-requested-and-previous-procedure-codes"
      |urls.tradeTariffVol3ForCds2 = "https://www.gov.uk/government/collections/uk-trade-tariff-volume-3-for-cds--2"
      |urls.commodityCodeHelp = "https://www.gov.uk/guidance/using-the-trade-tariff-tool-to-find-a-commodity-code"
      |urls.nactCodes = "https://www.gov.uk/guidance/national-additional-codes-for-data-element-617-of-cds"
      |urls.commodityCodes="https://www.gov.uk/guidance/finding-commodity-codes-for-imports-or-exports"
      |urls.relevantLicenses="https://www.gov.uk/starting-to-export/licences"
      |urls.serviceAvailability="https://www.gov.uk/guidance/customs-declaration-service-service-availability-and-issues"
      |urls.customsMovementsFrontend="http://url-to-movements-frontend/start"
      |urls.exitSurveyUrl="http://localhost:9514/feedback/customs-declare-exports-frontend"
      |urls.emailFrontendUrl="http://localhost:9898/manage-email-cds/service/customs-declare-exports"
      |urls.tradeTariff="https://www.gov.uk/trade-tariff"
      |urls.tariffBrowse="https://www.trade-tariff.service.gov.uk/browse/"
      |urls.tariffCommodities="https://www.trade-tariff.service.gov.uk/commodities/"
      |urls.ecicsTool = "https://ec.europa.eu/taxation_customs/dds2/ecics/chemicalsubstance_consultation.jsp"
      |urls.sfusUpload = "http://localhost:6793/cds-file-upload-service/mrn-entry"
      |urls.sfusInbox = "http://localhost:6793/cds-file-upload-service/exports-message-choice"
      |urls.eoriService = "https://www.gov.uk/eori"
      |urls.cdsRegister = "https://www.gov.uk/guidance/get-access-to-the-customs-declaration-service"
      |urls.cdsCheckStatus = "https://www.tax.service.gov.uk/customs/register-for-cds/are-you-based-in-uk"
      |urls.organisationsLink = "https://www.gov.uk/government/organisations/hm-revenue-customs"
      |urls.importExports = "https://www.gov.uk/topic/business-tax/import-export"
      |urls.exitSurveyUrl = "http://localhost:9514/feedback/customs-declare-exports-frontend"
      |urls.emailFrontendUrl = "http://localhost:9898/manage-email-cds/service/customs-declare-exports"
      |urls.companyInformationRegister="http://companyInformationRegister"
      |urls.customsDecCompletionRequirements="http://customsDecCompletionRequirements"
      |urls.locationCodeForAirports="http://locationCodeForAirports"
      |urls.locationCodesForPortsUsingGVMS="http://locationCodesForPortsUsingGVMS"
      |urls.certificateOfAgreementAirports="http://certificateOfAgreementAirports"
      |urls.locationCodeForMaritimePorts="http://locationCodeForMaritimePorts"
      |urls.locationCodeForTempStorage="http://locationCodeForTempStorage"
      |urls.designatedExportPlaceCodes="http://designatedExportPlaceCodes"
      |urls.locationCodesForCsePremises="http://locationCodesForCsePremises"
      |urls.goodsLocationCodesForDataElement="http://goodsLocationCodesForDataElement"
      |urls.tariffCdsChiefSupplement="http://tariffCdsChiefSupplement"
      |urls.notesForMucrConsolidation="http://notesForMucrConsolidation"
      |urls.arriveOrDepartExportsService="http://arriveOrDepartExportsService"
      |urls.customsDeclarationsGoodsTakenOutOfEu="http://customsDeclarationsGoodsTakenOutOfEu"
      |urls.tradeTariffSections = "http://tradeTariffSections"
      |urls.additionalDocumentsLicenceTypes = "http://additionalDocumentsLicenceTypes"
      |urls.additionalDocumentsUnionCodes = "http://additionalDocumentsUnionCodes"
      |urls.additionalDocumentsReferenceCodes = "http://additionalDocumentsReferenceCodes"
      |urls.additionalDocumentsUnitCodes = "http://additionalDocumentsUnitCodes"
      |urls.rollOnRollOffPorts="https://www.gov.uk/government/publications/roll-on-roll-off-ports"
      |urls.railLocationCodes="https://www.gov.uk/government/publications/rail-location-codes"
      |urls.checkCustomsDeclarationService="https://www.gov.uk/check-customs-declaration"
      |urls.nationalExportSystemGuidance="urls.nationalExportSystemGuidance"
      |urls.nationalClearanceHub="urls.nationalClearanceHub"
      |urls.hmrcExchangeRatesFor2021="https://www.gov.uk/government/publications/hmrc-exchange-rates-for-2021-monthly"
      |urls.nationalAdditionalCodes="https://www.gov.uk/government/publications/national-additional-codes-to-declare-with-data-element-617-of-the-customs-declaration-service"
      |urls.commodityCode9306909000="https://www.trade-tariff.service.gov.uk/xi/commodities/9306909000?country=KP#export"
      |urls.simplifiedDeclPreviousDoc="https://www.gov.uk/government/publications/appendix-1-de-110-requested-and-previous-procedure-codes/requested-procedure-10-permanent-export-or-dispatch#simplified-declaration--previous-document-de-21"
      |urls.combinedPackaging="https://www.gov.uk/government/publications/uk-trade-tariff-cds-volume-3-export-declaration-completion-guide/group-6-goods-identification#combined-packaging"
      |urls.getGoodsMovementReference="https://www.gov.uk/guidance/get-a-goods-movement-reference"
      |urls.additionalInformationAppendix4="https://www.gov.uk/guidance/additional-information-ai-statement-codes-for-data-element-22-of-the-customs-declaration-service-cds"
      |
      |urls.procedureCodes.removalOfGoodsFromExciseWarehouse = "https://www.gov.uk/guidance/receive-goods-into-and-remove-goods-from-an-excise-warehouse-excise-notice-197"
      |urls.procedureCodes.onwardSupplyRelief = "https://www.gov.uk/guidance/check-if-you-can-claim-vat-relief-on-goods-imported-into-northern-ireland-for-onward-supply-to-the-eu#onward-supply-relief-osr"
      |urls.procedureCodes.endUseRelief = "https://www.gov.uk/government/publications/uk-trade-tariff-end-use-relief-on-goods-used-for-a-prescribed-use"
      |urls.procedureCodes.inwardProcessing = "https://www.gov.uk/government/publications/appendix-1-de-110-requested-and-previous-procedure-codes/requested-procedure-10-permanent-export-or-dispatch#section"
      |urls.procedureCodes.outwardProcessing = "https://www.gov.uk/guidance/apply-to-pay-less-duty-on-goods-you-export-to-process-or-repair"
      |urls.procedureCodes.temporaryExport = "https://www.gov.uk/guidance/pay-less-import-duty-and-vat-when-re-importing-goods-to-the-uk-and-eu#claiming-relief-for-exporting-goods-using-a-duplicate-list"
      |urls.procedureCodes.reExportFollowingSpecialProcedure = "https://www.gov.uk/guidance/moving-processed-or-repaired-goods-into-free-circulation-or-re-exporting-them"
      |urls.procedureCodes.permanentExportOrDispatch.base = "https://www.gov.uk/government/publications/appendix-1-de-110-requested-and-previous-procedure-codes/requested-procedure-10-permanent-export-or-dispatch"
      |urls.procedureCodes.permanentExportOrDispatch.targetAuthHolder = "holder-of-the-authorisation-identification-number-de-339"
      |urls.procedureCodes.permanentExportOrDispatch.targetConditions = "conditions-for-use"
      |urls.procedureCodes.permanentExportOrDispatch.targetDocuments = "documents-produced-certificates-and-authorisations-additional-references-de-23"
      |urls.procedureCodes.permanentExportOrDispatch.targetSection = "section"
      |
      |urls.additionalProcedureCodesOfCDs = "http://additionalProcedureCodesOfCDs"
      |urls.unDangerousGoods = "http://unDangerousGoods"
      |urls.licensesForExportingGoods = "https://www.gov.uk/export-goods"
      |
      |urls.notDeclarant.eoriContactTeam=eoriContactTeamUrl
      |urls.generalEnquiriesHelp=generalEnquiriesHelpUrl
      |urls.currencyCodesForDataElement410=currencyCodesForDataElement410
      |urls.exchangeRatesForCustoms=exchangeRatesForCustoms
      |
      |urls.additionalDeclarationType = "https://www.gov.uk/government/publications/uk-trade-tariff-cds-volume-3-export-declaration-completion-guide/group-1-message-information-including-procedure-codes#de-12-additional-declaration-type-box-1-declaration-second-subdivision"
      |urls.declareGoodsExported = "https://www.gov.uk/guidance/declare-commercial-goods-youre-taking-out-of-great-britain-in-your-accompanied-baggage-or-small-vehicles"
      |urls.simplifiedDeclarationOccasionalUse = "https://www.gov.uk/government/publications/appendix-2-de-111-additional-procedure-codes/additional-procedure-code-3-series"
      |
      |files.codelists.holderOfAuthorisationCodes="holderOfAuthorisationCodes"
      |files.codelists.procedureCodes="procedureCodes"
      |files.codelists.procedureCodesC21="procedureCodesC21"
      |files.codelists.additionalProcedureCodes="additionalProcedureCodes"
      |files.codelists.additionalProcedureCodesC21="additionalProcedureCodesC21"
      |files.codelists.procedureCodeToAdditionalProcedureCodesLink="procedureCodeToAdditionalProcedureCodesLink"
      |files.codelists.procedureCodeToAdditionalProcedureCodesC21Link="procedureCodeToAdditionalProcedureCodesC21Link"
      |files.codelists.dmsErrorCodes="/code-lists/dmsErrorCodes.json"
      |files.codelists.countryCodes="/code-lists/countryCodes.json"
      |files.codelists.countryCodeToAliasesLink="countryCodeToAliasesLink"
      |files.codelists.countryCodeToShortNameLink="countryCodeToShortNameLink"
      |files.codelists.goodsLocationCode="/code-lists/goods-locations-codes/dep-16k-from-30-11-2022.json"
      |files.codelists.goodsLocationCodeToLocationTypeLink="goodsLocationCodeToLocationTypeLink"
      |files.codelists.packageTypeCode="packageTypeCode"
      |files.codelists.officeOfExits="officeOfExit"
      |files.codelists.customsOffices="customsOffice"
      |
      |draft.timeToLive=30d
      |microservice.services.nrs.host=localhostnrs
      |microservice.services.nrs.port=7654
      |microservice.services.nrs.apikey=cds-exports
      |microservice.services.features.default=disabled
      |
      |microservice.services.auth.port=9988
      |microservice.services.contact-frontend.url=/contact-frontend-url
      |microservice.services.contact-frontend.serviceId=DeclarationServiceId
      |mongodb.timeToLive=24h
      |
      |guidance.addATeamMember = "https://www.gov.uk/guidance/register-for-the-goods-vehicle-movement-service#add-a-team-member"
      |guidance.additionalDocumentsReferenceCodes = "https://www.gov.uk/guidance/data-element-23-documents-and-other-reference-codes-national-of-the-customs-declaration-service-cds#table-2-national-document-codes"
      |guidance.additionalDocumentsUnionCodes = "https://www.gov.uk/government/publications/data-element-23-documents-and-other-reference-codes-union-of-the-customs-declaration-service-cds"
      |guidance.aiCodes = "https://www.gov.uk/guidance/additional-information-ai-statement-codes-for-data-element-22-of-the-customs-declaration-service-cds"
      |guidance.aiCodesForContainers = "https://www.gov.uk/government/publications/appendix-1-de-110-requested-and-previous-procedure-codes/requested-procedure-10-permanent-export-or-dispatch#additional-information-de-22-1"
      |guidance.cdsDeclarationSoftware = "https://www.gov.uk/guidance/list-of-software-developers-providing-customs-declaration-support"
      |guidance.cdsRegister = "https://www.gov.uk/guidance/get-access-to-the-customs-declaration-service"
      |guidance.cdsTariffCompletionGuide = "https://www.gov.uk/government/publications/uk-trade-tariff-cds-volume-3-export-declaration-completion-guide"
      |guidance.clearingGoodsFromToUK = "https://www.gov.uk/guidance/national-clearance-hub-for-goods-entering-leaving-or-transiting-the-eu"
      |guidance.commodityCode0306310010 = "https://www.trade-tariff.service.gov.uk/commodities/0306310010#export"
      |guidance.commodityCode2208303000 = "https://www.trade-tariff.service.gov.uk/commodities/2208303000#import"
      |guidance.commodityCodes = "https://www.gov.uk/guidance/finding-commodity-codes-for-imports-or-exports"
      |guidance.eoriService = "https://www.gov.uk/eori"
      |guidance.exportingByPost = "https://www.gov.uk/government/publications/notice-143-a-guide-for-international-post-users/notice-143-a-guide-for-international-post-users"
      |guidance.manageYourEmailAddress = "https://www.gov.uk/guidance/manage-your-email-address-for-the-customs-declaration-service"
      |guidance.someoneToDealWithCustomsOnYourBehalf = "https://www.gov.uk/guidance/appoint-someone-to-deal-with-customs-on-your-behalf"
      |guidance.specialProcedures = "https://www.gov.uk/government/publications/appendix-1-de-110-requested-and-previous-procedure-codes"
      |guidance.takingCommercialGoodsOnYourPerson = "https://www.gov.uk/guidance/taking-commercial-goods-out-of-great-britain-in-your-baggage"
      |guidance.vatOnGoodsExportedFromUK = "https://www.gov.uk/guidance/vat-on-goods-exported-from-the-uk-notice-703"
      |guidance.vatRatingForStandardExport = "https://www.gov.uk/government/publications/appendix-1-de-110-requested-and-previous-procedure-codes/requested-procedure-10-permanent-export-or-dispatch#vat-1"
      |guidance.moveGoodsThroughPortsUsingGVMS = "https://www.gov.uk/guidance/check-how-to-move-goods-through-ports-that-use-the-goods-vehicle-movement-service"
      |guidance.january2022locations = "https://www.gov.uk/guidance/check-which-locations-need-an-arrived-export-declaration-from-1-january-2022"
      |
      """.stripMargin
}
