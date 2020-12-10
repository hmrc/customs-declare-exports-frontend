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

import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigFactory}
import forms.Choice
import models.DeclarationType
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import unit.base.UnitSpec

import scala.concurrent.duration.FiniteDuration

class AppConfigSpec extends UnitSpec {

  private val environment = Environment.simple()

  private val validConfig: Config =
    ConfigFactory.parseString(
      """
        |urls.login="http://localhost:9949/auth-login-stub/gg-sign-in"
        |urls.loginContinue="http://localhost:9000/customs-declare-exports-frontend"
        |
        |urls.customsDeclarationsGoodsTakenOutOfEu="https://www.gov.uk/guidance/customs-declarations-for-goods-taken-out-of-the-eu"
        |urls.commodityCodes="https://www.gov.uk/guidance/finding-commodity-codes-for-imports-or-exports"
        |urls.relevantLicenses="https://www.gov.uk/starting-to-export/licences"
        |urls.serviceAvailability="https://www.gov.uk/guidance/customs-declaration-service-service-availability-and-issues"
        |urls.customsMovementsFrontend="http://url-to-movements-frontend/start"
        |urls.exitSurveyUrl="http://localhost:9514/feedback/customs-declare-exports-frontend"
        |
        |microservice.services.auth.host=localhostauth
        |google-analytics.token=N/A
        |google-analytics.host=localhostGoogle
        |
        |tracking-consent-frontend.gtm.container=a
        |
        |countryCodesCsvFilename=code-lists/mdg-country-codes.csv
        |countryCodesJsonFilename=code-lists/location-autocomplete-canonical-list.json
        |list-of-available-journeys="CRT,CAN,SUB"
        |list-of-available-declarations="STANDARD,SUPPLEMENTARY"
        |draft.timeToLive=30d
        |microservice.services.nrs.host=localhostnrs
        |microservice.services.nrs.port=7654
        |microservice.services.nrs.apikey=cds-exports
        |microservice.services.features.default=disabled
        |microservice.services.features.welsh-translation=false
        |microservice.services.features.use-improved-error-messages=true
        |microservice.services.auth.port=9988
        |microservice.services.customs-declare-exports.host=localhost
        |microservice.services.customs-declare-exports.port=9875
        |microservice.services.customs-declare-exports.submit-declaration=/declaration
        |microservice.services.customs-declare-exports.declarations=/v2/declaration
        |microservice.services.customs-declare-exports.cancel-declaration=/cancellations
        |microservice.services.customs-declare-exports.fetch-notifications=/notifications
        |microservice.services.customs-declare-exports.fetch-submissions=/submissions
        |microservice.services.customs-declare-exports.fetch-submission-notifications=/submission-notifications
        |microservice.services.customs-declare-exports.fetch-ead=/ead
        |microservice.services.customs-declare-exports-movements.host=localhostm
        |microservice.services.customs-declare-exports-movements.port=9876
        |microservice.services.customs-declare-exports-movements.save-movement-uri=/save-movement-submission
        |microservice.services.contact-frontend.url=/contact-frontend-url
        |microservice.services.contact-frontend.serviceId=DeclarationServiceId
        |mongodb.timeToLive=24h
        |platform.frontend.host="self/base-url"
      """.stripMargin
    )
  private val emptyConfig: Config = ConfigFactory.empty()
  val validServicesConfiguration = Configuration(validConfig)
  private val emptyServicesConfiguration = Configuration(emptyConfig)

  private def servicesConfig(conf: Configuration) = new ServicesConfig(conf)
  private def appConfig(conf: Configuration) = new AppConfig(conf, environment, servicesConfig(conf), "AppName")

  val validAppConfig: AppConfig = appConfig(validServicesConfiguration)
  val emptyAppConfig: AppConfig = appConfig(emptyServicesConfiguration)

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

    "have customsDeclarationsGoodsTakenOutOfEu URL" in {
      validAppConfig.customsDeclarationsGoodsTakenOutOfEuUrl must be("https://www.gov.uk/guidance/customs-declarations-for-goods-taken-out-of-the-eu")
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

    "load the Choice options when list-of-available-journeys is defined" in {
      val choices = validAppConfig.availableJourneys()
      choices.size must be(3)

      choices must contain(Choice.AllowedChoiceValues.CreateDec)
      choices must contain(Choice.AllowedChoiceValues.CancelDec)
      choices must contain(Choice.AllowedChoiceValues.Submissions)
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

    "have language translation enabled field" in {
      validAppConfig.languageTranslationEnabled must be(false)
    }

    "have improved error messages feature toggle set to false if not defined" in {
      emptyAppConfig.isUsingImprovedErrorMessages must be(false)
    }

    "have improved error messages feature toggle set to true if defined" in {
      validAppConfig.isUsingImprovedErrorMessages must be(true)
    }

    "have language map with English" in {
      validAppConfig.languageMap.get("english").isDefined must be(true)
    }

    "have language map with Cymraeg" in {
      validAppConfig.languageMap.get("cymraeg").isDefined must be(true)
    }

    "have customs declare exports URL" in {
      validAppConfig.customsDeclareExportsBaseUrl must be("http://localhost:9875")
    }

    "have submit declaration URL" in {
      validAppConfig.declarations must be("/v2/declaration")
    }

    "have cancel declaration URL" in {
      validAppConfig.cancelDeclaration must be("/cancellations")
    }

    "have ead URL" in {
      validAppConfig.fetchMrnStatus must be("/ead")
    }

    "have fetch notification URL" in {
      validAppConfig.fetchNotifications must be("/notifications")
    }

    "have fetchSubmissions URL" in {
      validAppConfig.fetchSubmissions must be("/submissions")
    }

    "have selfBaseUrl" in {
      validAppConfig.selfBaseUrl must be(defined)
      validAppConfig.selfBaseUrl.get must be("self/base-url")
    }

    "have link for 'give feedback'" in {
      validAppConfig.giveFeedbackLink must be("/contact-frontend-url?service=DeclarationServiceId")
    }

    "have countryCodesJsonFilename" in {
      validAppConfig.countryCodesJsonFilename must be("code-lists/location-autocomplete-canonical-list.json")
    }

    "have countriesCsvFilename" in {
      validAppConfig.countriesCsvFilename must be("code-lists/mdg-country-codes.csv")
    }

    "have ttl lifetime" in {
      validAppConfig.cacheTimeToLive must be(FiniteDuration(24, "h"))
    }

    "have draft lifetime" in {
      validAppConfig.draftTimeToLive must be(FiniteDuration(30, TimeUnit.DAYS))
    }

    "have single Choice options when list-of-available-journeys is not defined" in {
      emptyAppConfig.availableJourneys().size must be(1)
      emptyAppConfig.availableJourneys() must contain(Choice.AllowedChoiceValues.Submissions)
    }

    "have single Declaration type options when list-of-available-declarations is not defined" in {
      emptyAppConfig.availableDeclarations().size must be(1)
      emptyAppConfig.availableDeclarations() must contain(DeclarationType.STANDARD.toString)
    }

    "empty selfBaseUrl when the key is missing" in {
      emptyAppConfig.selfBaseUrl must be(None)
    }

    "throw an exception" when {

      "gtm.container is missing" in {
        intercept[Exception](emptyAppConfig.gtmContainer).getMessage must be("Could not find config key 'tracking-consent-frontend.gtm.container'")
      }

      "google-analytics.host is missing" in {
        intercept[Exception](emptyAppConfig.analyticsHost).getMessage must be("Missing configuration key: google-analytics.host")
      }

      "google-analytics.token is missing" in {
        intercept[Exception](emptyAppConfig.analyticsToken).getMessage must be("Missing configuration key: google-analytics.token")
      }

      "auth.host is missing" in {
        intercept[Exception](emptyAppConfig.authUrl).getMessage must be("Could not find config key 'auth.host'")
      }

      "urls.login is missing" in {
        intercept[Exception](emptyAppConfig.loginUrl).getMessage must be("Missing configuration key: urls.login")
      }

      "urls.loginContinue is missing" in {
        intercept[Exception](emptyAppConfig.loginContinueUrl).getMessage must be("Missing configuration key: urls.loginContinue")
      }

      "customs-declare-exports.host is missing" in {
        intercept[Exception](emptyAppConfig.customsDeclareExportsBaseUrl).getMessage must be(
          "Could not find config key 'customs-declare-exports.host'"
        )
      }

      "submit declaration uri is missing" in {
        intercept[Exception](emptyAppConfig.declarations).getMessage must be(
          "Missing configuration for Customs Declarations Exports submit declaration URI"
        )
      }

      "cancel declaration uri is missing" in {
        intercept[Exception](emptyAppConfig.cancelDeclaration).getMessage must be(
          "Missing configuration for Customs Declaration Export cancel declaration URI"
        )
      }

      "fetch mrn status uri is missing" in {
        intercept[Exception](emptyAppConfig.fetchMrnStatus).getMessage must be(
          "Missing configuration for Customs Declaration Export fetch mrn status URI"
        )
      }

      "fetchSubmissions uri is missing" in {
        intercept[Exception](emptyAppConfig.fetchSubmissions).getMessage must be(
          "Missing configuration for Customs Declaration Exports fetch submission URI"
        )
      }

      "fetch notifications uri is missing" in {
        intercept[Exception](emptyAppConfig.fetchNotifications).getMessage must be(
          "Missing configuration for Customs Declarations Exports fetch notification URI"
        )
      }

      "link for 'give feedback' is missing" in {
        intercept[Exception](emptyAppConfig.giveFeedbackLink).getMessage must be(
          "Missing configuration key: microservice.services.contact-frontend.url"
        )
      }

      "countryCodesJsonFilename is missing" in {
        intercept[Exception](emptyAppConfig.countryCodesJsonFilename).getMessage must be("Missing configuration key: countryCodesJsonFilename")
      }

      "countryCodesCsvFilename is missing" in {
        intercept[Exception](emptyAppConfig.countriesCsvFilename).getMessage must be("Missing configuration key: countryCodesCsvFilename")
      }
    }
  }

}
