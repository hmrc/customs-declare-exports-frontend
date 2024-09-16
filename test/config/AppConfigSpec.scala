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

import base.UnitWithMocksSpec
import com.typesafe.config.{Config, ConfigFactory}
import play.api.{Configuration, Environment}
import tools.Stubs
import tools.Stubs.baseConfig
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

class AppConfigSpec extends UnitWithMocksSpec with Stubs {

  private val environment = Environment.simple()

  private val validConfig: Config =
    ConfigFactory.parseString(baseConfig + """
        |microservice.services.auth.host=localhostauth
        |
        |tracking-consent-frontend.gtm.container=a
        |
        |microservice.services.customs-declare-exports.host=localhost
        |microservice.services.customs-declare-exports.port=9875
        |microservice.services.customs-declare-exports.submit-declaration=/declaration
        |microservice.services.customs-declare-exports.declarations=/v2/declaration
        |microservice.services.customs-declare-exports.cancel-declaration=/cancellations
        |microservice.services.customs-declare-exports.action=/submission/action
        |microservice.services.customs-declare-exports.notifications=/submission/notifications
        |microservice.services.customs-declare-exports.submission=/submission
        |microservice.services.customs-declare-exports.page-of-submissions=/paginated-submissions
        |microservice.services.customs-declare-exports.fetch-ead=/ead
        |microservice.services.customs-declare-exports-movements.host=localhost
        |microservice.services.customs-declare-exports-movements.port=9876
        |microservice.services.customs-declare-exports-movements.save-movement-uri=/save-movement-submission
        |play.frontend.host="self/base-url"
        |
        |secret.tdrHashSalt="SomeSuperSecret"
      """.stripMargin)

  private val validServicesConfiguration = Configuration(validConfig)
  private val missingValuesServicesConfiguration = Configuration(ConfigFactory.parseString(baseConfig))

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

    "have currencyCodes file path" in {
      validAppConfig.currencyCodesFile must be("/code-lists/currencyCodes.json")
    }

    "have countryCodeToAliasesLink file path" in {
      validAppConfig.countryCodeToAliasesLinkFile must be("countryCodeToAliasesLink")
    }

    "have countryCodeToShortNameLink file path" in {
      validAppConfig.countryCodeToShortNameLinkFile must be("countryCodeToShortNameLink")
    }

    "have goodsLocationCode file path" in {
      validAppConfig.glcDep16k must be("/code-lists/goods-locations-codes/dep")
    }

    "have goodsLocationCodeToLocationType file path" in {
      validAppConfig.goodsLocationCodeToLocationTypeFile must be("goodsLocationCodeToLocationTypeLink")
    }

    "have login continue URL" in {
      validAppConfig.loginContinueUrl must be("http://localhost:9000/customs-declare-exports-frontend")
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

    "have single Submission URL" in {
      validAppConfig.submissionPath must be("/submission")
    }

    "have single Action URL" in {
      validAppConfig.actionPath must be("/submission/action")
    }

    "have Notifications URL" in {
      validAppConfig.notificationsPath must be("/submission/notifications")
    }

    "have URL to fetch a page of Submissions" in {
      validAppConfig.pageOfSubmissionsPath must be("/paginated-submissions")
    }

    "have selfBaseUrl" in {
      validAppConfig.selfBaseUrl must be(defined)
      validAppConfig.selfBaseUrl.get must be("self/base-url")
    }

    "empty selfBaseUrl when the key is missing" in {
      missingAppConfig.selfBaseUrl must be(None)
    }

    "have tdrHashSalt" in {
      validAppConfig.maybeTdrHashSalt must be(Some("SomeSuperSecret"))
    }

    "empty tdrHashSalt when the key is missing" in {
      missingAppConfig.maybeTdrHashSalt must be(None)
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
        intercept[Exception](missingAppConfig.declarationsPath).getMessage must be("Missing configuration for CDS Exports declarations URI")
      }

      "cancel declaration uri is missing" in {
        intercept[Exception](missingAppConfig.cancelDeclarationPath).getMessage must be(
          "Missing configuration for CDS Exports cancel declaration URI"
        )
      }

      "fetch mrn status uri is missing" in {
        intercept[Exception](missingAppConfig.fetchMrnStatusPath).getMessage must be("Missing configuration for CDS Exports fetch mrn status URI")
      }

      "fetch page of Submissions uri is missing" in {
        intercept[Exception](missingAppConfig.pageOfSubmissionsPath).getMessage must be(
          "Missing configuration for CDS Exports page of Submissions URI"
        )
      }

      "Single Submission uri is missing" in {
        intercept[Exception](missingAppConfig.submissionPath).getMessage must be("Missing configuration for CDS Exports single Submission URI")
      }

      "Notifications uri is missing" in {
        intercept[Exception](missingAppConfig.notificationsPath).getMessage must be("Missing configuration for CDS Exports Notifications URI")
      }
    }
  }
}
