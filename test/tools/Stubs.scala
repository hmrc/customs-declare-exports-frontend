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

package tools

import com.typesafe.config.ConfigFactory
import config.AppConfig
import org.apache.pekko.stream.testkit.NoMaterializer
import play.api.http.{DefaultFileMimeTypes, FileMimeTypes, FileMimeTypesConfiguration}
import play.api.i18n.{Lang, Langs, MessagesApi}
import play.api.mvc._
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import tools.Stubs.minimalConfig
import uk.gov.hmrc.govukfrontend.views.html.components.{Footer => _}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.ExecutionContext

trait Stubs {

  def stubMessagesControllerComponents(
    bodyParser: BodyParser[AnyContent] = stubBodyParser(AnyContentAsEmpty),
    playBodyParsers: PlayBodyParsers = stubPlayBodyParsers(NoMaterializer),
    messagesApi: MessagesApi = stubMessagesApi(),
    langs: Langs = stubLangs(List(Lang("en"), Lang("cy"))),
    fileMimeTypes: FileMimeTypes = new DefaultFileMimeTypes(FileMimeTypesConfiguration()),
    executionContext: ExecutionContext = ExecutionContext.global
  ): MessagesControllerComponents =
    DefaultMessagesControllerComponents(
      new DefaultMessagesActionBuilderImpl(bodyParser, messagesApi)(executionContext),
      DefaultActionBuilder(bodyParser)(executionContext),
      playBodyParsers,
      messagesApi,
      langs,
      fileMimeTypes,
      executionContext
    )

  private val minimalConfiguration: Configuration = Configuration(ConfigFactory.parseString(minimalConfig))

  private val environment = Environment.simple()

  private val servicesConfig = new ServicesConfig(minimalConfiguration)
  private val appConfig = new AppConfig(minimalConfiguration, environment, servicesConfig, "AppName")

  val minimalAppConfig: AppConfig = appConfig
}

object Stubs {

  val baseConfig: String =
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
      |urls.standardDeclarationType="https://www.gov.uk/guidance/making-a-full-export-declaration"
      |urls.combinedPackaging="https://www.gov.uk/government/publications/uk-trade-tariff-cds-volume-3-export-declaration-completion-guide/group-6-goods-identification#combined-packaging"
      |urls.getGoodsMovementReference="https://www.gov.uk/guidance/get-a-goods-movement-reference"
      |urls.additionalInformationAppendix4="https://www.gov.uk/guidance/additional-information-ai-statement-codes-for-data-element-22-of-the-customs-declaration-service-cds"
      |
      |urls.errorCodesForCDS="https://www.gov.uk/government/publications/customs-declaration-service-error-codes"
      |urls.errorWorkaroundsForCDS="https://www.gov.uk/government/publications/known-error-workarounds-for-the-customs-declaration-service-cds"
      |urls.reportProblemsByUsingCDS="https://www.gov.uk/guidance/report-a-problem-using-the-customs-declaration-service"
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
      |urls.declareGoodsExported = "https://www.gov.uk/guidance/declare-commercial-goods-youre-taking-out-of-great-britain-in-your-accompanied-baggage-or-small-vehicles"
      |
      |files.codelists.procedureCodes="procedureCodes"
      |files.codelists.procedureCodesC21="procedureCodesC21"
      |files.codelists.additionalProcedureCodes="additionalProcedureCodes"
      |files.codelists.additionalProcedureCodesC21="additionalProcedureCodesC21"
      |files.codelists.procedureCodeToAdditionalProcedureCodesLink="procedureCodeToAdditionalProcedureCodesLink"
      |files.codelists.procedureCodeToAdditionalProcedureCodesC21Link="procedureCodeToAdditionalProcedureCodesC21Link"
      |files.codelists.procedureCodesLink="procedureCodesLink"
      |files.codelists.additionalDocumentStatusCodeLink="additionalDocumentStatusCodesLink"
      |files.codelists.additionalDocumentCodeLink="additionalDocumentCodesLink"
      |files.codelists.dmsErrorCodes="/code-lists/dmsErrorCodes.json"
      |files.codelists.countryCodes="/code-lists/countryCodes.json"
      |files.codelists.currencyCodes="/code-lists/currencyCodes.json"
      |files.codelists.countryCodeToAliasesLink="countryCodeToAliasesLink"
      |files.codelists.countryCodeToShortNameLink="countryCodeToShortNameLink"
      |files.codelists.goodsLocationCodeToLocationTypeLink="goodsLocationCodeToLocationTypeLink"
      |files.codelists.packageTypeCode="packageTypeCode"
      |files.codelists.officeOfExits="officeOfExit"
      |files.codelists.customsOffices="customsOffice"
      |
      |files.codelists.glc.airports="/code-lists/goods-locations-codes/airports"
      |files.codelists.glc.coa-airports="/code-lists/goods-locations-codes/coa-airports"
      |files.codelists.glc.maritime-ports-and-wharves="/code-lists/goods-locations-codes/maritime-ports-and-wharves"
      |files.codelists.glc.itsf="/code-lists/goods-locations-codes/itsf"
      |files.codelists.glc.remote-itsf="/code-lists/goods-locations-codes/remote-itsf"
      |files.codelists.glc.external-itsf="/code-lists/goods-locations-codes/external-itsf"
      |files.codelists.glc.border-inspection-posts="/code-lists/goods-locations-codes/border-inspection-posts"
      |files.codelists.glc.approved-dipositories="/code-lists/goods-locations-codes/approved-dipositories"
      |files.codelists.glc.gb-place-names="/code-lists/goods-locations-codes/gb-place-names"
      |files.codelists.glc.other-location-codes="/code-lists/goods-locations-codes/other-location-codes"
      |files.codelists.glc.dep="/code-lists/goods-locations-codes/dep"
      |files.codelists.glc.cse="/code-lists/goods-locations-codes/cse"
      |files.codelists.glc.rail="/code-lists/goods-locations-codes/rail"
      |files.codelists.glc.acts="/code-lists/goods-locations-codes/acts"
      |files.codelists.glc.roro="/code-lists/goods-locations-codes/roro"
      |files.codelists.glc.gvms="/code-lists/goods-locations-codes/gvms"
      |
      |files.codelists.hoa.hoa-codes="/code-lists/holder-of-authorisation-codes/holder-of-authorisation-codes"
      |files.codelists.hoa.tagged-hoa-codes="/code-lists/holder-of-authorisation-codes/tagged-holder-of-authorisation-codes"
      |
      |files.codelists.tagged-transport-codes="/code-lists/tagged-transport-codes"
      |files.codelists.docTypes="/code-lists/document-type.json"
      |files.codelists.docTypeLinks="/code-lists/linkDocumentTypes.json"
      |
      |microservice.services.nrs.host=localhostnrs
      |microservice.services.nrs.port=7654
      |microservice.services.nrs.apikey=cds-exports
      |
      |microservice.services.auth.port=9988
      |microservice.services.contact-frontend.url=/contact-frontend-url
      |microservice.services.contact-frontend.serviceId=DeclarationServiceId
      |
      |features.betaBanner=true
      |features.tdrVersion=false
      |
      |draft.timeToLive=30d
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
      |guidance.gvms = "https://www.gov.uk/guidance/register-for-the-goods-vehicle-movement-service"
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

  def minimalConfig: String =
    baseConfig + """
      |assets.version="version"
      |tracking-consent-frontend.gtm.container=a
      |metrics.name=""
      |metrics.rateUnit="SECONDS"
      |metrics.durationUnit="SECONDS"
      |metrics.showSamples=false
      |metrics.jvm=false
      |metrics.logback=false
      |draft.timeToLive=1d
      |timeoutDialog.timeout=13min
      |timeoutDialog.countdown=3min
      |urls.tradeTariff=tradeTariff
      |urls.classificationHelp=classificationHelp
      |urls.ecicsTool=ecicsTool
      |urls.notDeclarant.eoriContactTeam=eoriContactTeamUrl
      |urls.generalEnquiriesHelp=generalEnquiriesHelpUrl
      |play.i18n.langs = ["en", "cy"]
      |language.fallback.url=""
      |files.codelists.doc-type="/code-lists/document-type.json"
    """.stripMargin

  def updateConfig(configuration: String, replacements: Map[String, String]): String =
    if (replacements.isEmpty) configuration
    else {
      val values = configuration
        .split("[\n\r]")
        .map(_.trim.split("="))
        .filter(_.length == 2)
        .map(arr => arr(0).trim -> arr(1).trim)
        .toList

      replacements
        .foldLeft(Map(values: _*)) { case (map, (key, value)) =>
          map.updated(key, value)
        }
        .map(pair => s"${pair._1}=${pair._2}")
        .mkString("\n")
    }
}
