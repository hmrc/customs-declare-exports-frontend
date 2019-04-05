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

import base.CustomExportsBaseSpec
import com.typesafe.config.{Config, ConfigFactory}
import features.{Feature, FeatureStatus}
import play.api.{Configuration, Environment}

class AppConfigSpec extends CustomExportsBaseSpec {

  val config = app.injector.instanceOf[AppConfig]
  val environment = Environment.simple()

  private val validAppConfig: Config =
    ConfigFactory.parseString(
      """
        |urls.login="http://localhost:9949/auth-login-stub/gg-sign-in"
        |urls.loginContinue="http://localhost:9000/customs-declare-exports-frontend"
        |microservice.services.auth.host=localhostauth
        |google-analytics.token=N/A
        |google-analytics.host=localhostGoogle
        |countryCodesCsvFilename=mdg-country-codes.csv
        |countryCodesJsonFilename=location-autocomplete-canonical-list.json
        |microservice.services.nrs.host=localhostnrs
        |microservice.services.nrs.port=7654
        |microservice.services.nrs.apikey=cds-exports
        |microservice.services.features.default=disabled
        |microservice.services.features.welsh-translation=false
        |microservice.services.auth.port=9988
        |microservice.services.customs-declare-exports.host=localhoste
        |microservice.services.customs-declare-exports.port=9875
        |microservice.services.customs-declare-exports.submit-declaration=/declaration
        |microservice.services.customs-declare-exports.cancel-declaration=/cancel-declaration
        |microservice.services.customs-declare-exports.fetch-notifications=/customs-declare-exports/notifications
        |microservice.services.customs-declare-exports-movements.host=localhostm
        |microservice.services.customs-declare-exports-movements.port=9876
        |microservice.services.customs-declare-exports-movements.save-movement-uri=/save-movement-submission

      """.stripMargin
    )
  private val emptyAppConfig: Config = ConfigFactory.parseString("")
  val validServicesConfiguration = Configuration(validAppConfig)
  private val emptyServicesConfiguration = Configuration(emptyAppConfig)

  private def appConfig(conf: Configuration) = new AppConfig(conf, environment)

  val validConfigService: AppConfig = appConfig(validServicesConfiguration)
  val emptyConfigService: AppConfig = appConfig(emptyServicesConfiguration)

  "The config" should {

    "have analytics token" in {
      validConfigService.analyticsToken must be("N/A")
    }

    "have analytics host" in {
      validConfigService.analyticsHost must be("localhostGoogle")
    }

    "have auth URL" in {
      validConfigService.authUrl must be("http://localhostauth:9988")
    }

    "have login URL" in {
      validConfigService.loginUrl must be("http://localhost:9949/auth-login-stub/gg-sign-in")
    }

    // what is continue URL - redirect ?
    "have login continue URL" in {
      validConfigService.loginContinueUrl must be("http://localhost:9000/customs-declare-exports-frontend")
    }

    "have language translation enabled field" in {
      validConfigService.languageTranslationEnabled must be(false)
    }

    "have language map with English" in {
      validConfigService.languageMap.get("english").isDefined must be(true)
    }

    "have language map with Cymraeg" in {
      validConfigService.languageMap.get("cymraeg").isDefined must be(true)
    }

    "have default feature status" in {
      validConfigService.defaultFeatureStatus must be(FeatureStatus.disabled)
    }

    "return correct value for feature" in {
      validConfigService.featureStatus(Feature.default) must be(FeatureStatus.disabled)
    }

    "return correct value for isFeatureOn method" in {
      validConfigService.isFeatureOn(Feature.default) must be(false)
    }

    "have customs declare exports" in {
      validConfigService.customsDeclareExports must be("http://localhoste:9875")
    }

    "have submit declaration URL" in {
      validConfigService.submitDeclaration must be("/declaration")
    }

    "have cancel declaration URL" in {
      validConfigService.cancelDeclaration must be("/cancel-declaration")
    }

    "have movements backend hostname " in {
      validConfigService.customsDeclareExportsMovements must be("http://localhostm:9876")
    }
    "have movement submission URL" in {
      validConfigService.saveMovementSubmission must be("/save-movement-submission")
    }

    "have fetch notification URL" in {
      validConfigService.fetchNotifications must be("/customs-declare-exports/notifications")
    }

    "have fetch submission notification URL" in {
      config.fetchSubmissionNotifications must be("/customs-declare-exports/submission-notifications")
    }

    "have fetchSubmissions URL" in {
      config.fetchSubmissions must be("/submissions")
    }

    "have countryCodesJsonFilename" in {
      validConfigService.countryCodesJsonFilename must be("location-autocomplete-canonical-list.json")
    }

    "have countriesCsvFilename" in {
      validConfigService.countriesCsvFilename must be("mdg-country-codes.csv")
    }

    "have nrsServiceUrl" in {
      validConfigService.nrsServiceUrl must be("http://localhostnrs:7654")
    }

    "have nrsApiKey" in {
      validConfigService.nrsApiKey must be("cds-exports")
    }

  }

  "throw an exception when google-analytics.host is missing" in {
    intercept[Exception](emptyConfigService.analyticsHost).getMessage must be(
      "Missing configuration key: google-analytics.host"
    )
  }

  "throw an exception when google-analytics.token is missing" in {
    intercept[Exception](emptyConfigService.analyticsToken).getMessage must be(
      "Missing configuration key: google-analytics.token"
    )
  }

  "throw an exception when auth.host is missing" in {
    intercept[Exception](emptyConfigService.authUrl).getMessage must be("Could not find config auth.host")
  }

  "throw an exception when urls.login is missing" in {
    intercept[Exception](emptyConfigService.loginUrl).getMessage must be("Missing configuration key: urls.login")
  }

  "throw an exception when urls.loginContinue is missing" in {
    intercept[Exception](emptyConfigService.loginContinueUrl).getMessage must be(
      "Missing configuration key: urls.loginContinue"
    )
  }

  "throw an exception when microservice.services.features.default is missing" in {
    intercept[Exception](emptyConfigService.defaultFeatureStatus).getMessage must be(
      "Missing configuration key: microservice.services.features.default"
    )
  }

  "throw an exception when customs-declare-exports.host is missing" in {
    intercept[Exception](emptyConfigService.customsDeclareExports).getMessage must be(
      "Could not find config customs-declare-exports.host"
    )
  }

  "throw an exception when submit declaration uri is missing" in {
    intercept[Exception](emptyConfigService.submitDeclaration).getMessage must be(
      "Missing configuration for Customs Declarations Exports submit declaration URI"
    )
  }

  "throw an exception when cancel declaration uri is missing" in {
    intercept[Exception](emptyConfigService.cancelDeclaration).getMessage must be(
      "Missing configuration for Customs Declaration Export cancel declaration URI"
    )
  }

  "throw an exception when fetchSubmissions uri is missing" in {
    intercept[Exception](emptyConfigService.fetchSubmissions).getMessage must be(
      "Missing configuration for Customs Declaration Exports fetch submission URI"
    )
  }


  "throw an exception when customs-declare-exports-movements.host is missing" in {
    intercept[Exception](emptyConfigService.customsDeclareExportsMovements).getMessage must be(
      "Could not find config customs-declare-exports-movements.host"
    )
  }

  "throw an exception when movement submission uri is missing" in {
    intercept[Exception](emptyConfigService.saveMovementSubmission).getMessage must be(
      "Missing configuration for Customs Declarations Exports Movement submission URI"
    )
  }

  "throw an exception when fetch notifications uri is missing" in {
    intercept[Exception](emptyConfigService.fetchNotifications).getMessage must be(
      "Missing configuration for Customs Declarations Exports fetch notification URI"
    )
  }

  "throw an exception when fetch-submission-notifications uri is missing" in {
    intercept[Exception](emptyConfigService.fetchSubmissionNotifications).getMessage must be(
      "Missing configuration for Customs Declaration Export fetch submission notification URI"
    )
  }

  "throw an exception when countryCodesJsonFilename is missing" in {
    intercept[Exception](emptyConfigService.countryCodesJsonFilename).getMessage must be(
      "Missing configuration key: countryCodesJsonFilename"
    )
  }

  "throw an exception when countryCodesCsvFilename is missing" in {
    intercept[Exception](emptyConfigService.countriesCsvFilename).getMessage must be(
      "Missing configuration key: countryCodesCsvFilename"
    )
  }

  "throw an exception when nrs.host is missing" in {
    intercept[Exception](emptyConfigService.nrsServiceUrl).getMessage must be(
      "Could not find config nrs.host"
    )
  }

  "throw an exception when nrs apikey is missing" in {
    intercept[Exception](emptyConfigService.nrsApiKey).getMessage must be(
      "Missing configuration for nrs apikey"
    )
  }


}
