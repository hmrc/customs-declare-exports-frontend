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
import features.{Feature, FeatureStatus}

class AppConfigSpec extends CustomExportsBaseSpec {

  val config = app.injector.instanceOf[AppConfig]

  "The config" should {
    "have analytics token" in {
      config.analyticsToken must be("N/A")
    }

    "have analytics host" in {
      config.analyticsHost must be("auto")
    }

    "have auth url" in {
      config.authUrl must be("http://localhost:8500")
    }

    "have login url" in {
      config.loginUrl must be("http://localhost:9949/auth-login-stub/gg-sign-in")
    }

    "have login continue url" in {
      config.loginContinueUrl must be("http://localhost:9000/customs-declare-exports-frontend")
    }

    "have customs declarations url" in {
      config.customsDeclarationsUrl must be("http://localhost:9820")
    }

    "have language translation enabled field" in {
      config.languageTranslationEnabled must be(false)
    }

    "have language map with English" in {
      config.languageMap.get("english").isDefined must be(true)
    }

    "have language map with Cymraeg" in {
      config.languageMap.get("cymraeg").isDefined must be(true)
    }

    "have default feature status" in {
      config.defaultFeatureStatus must be(FeatureStatus.disabled)
    }

    "return correct status for feature" in {
      config.featureStatus(Feature.default) must be(FeatureStatus.disabled)
    }

    "return correct value for isFeatureOn method" in {
      config.isFeatureOn(Feature.default) must be(false)
    }
    "have a submit export declarations uri" in {
      config.submitExportDeclarationUri must be("/")
    }
    "have a submit cancellation uri" in {
      config.submitCancellationUri must be("/cancellation-requests")
    }
    "have customs declarations endpoint" in {
      config.customsDeclarationsEndpoint must be("http://localhost:6790")
    }

    "have customs declarations API version" in {
      config.customsDeclarationsApiVersion must be("2.0")
    }

    "have HMRC Developer Hub Client ID" in {
      config.developerHubClientId must be("customs-declare-exports-frontend")
    }

    "have customs declare exports" in {
      config.customsDeclareExports must be("http://localhost:6792")
    }

    "have submission response URL" in {
      config.saveSubmissionResponse must be("/save-submission-response")
    }

    "have movement submission URL" in {
      config.saveMovementSubmission must be("/save-movement-submission")
    }

    "have fetch notification URL" in {
      config.fetchNotifications must be("/customs-declare-exports/notifications")
    }

    "have customs inventory linking exports URL" in {
      config.customsInventoryLinkingExports must be("http://localhost:9823")
    }

    "have submit arrival URI" in {
      config.sendArrival must be("/")
    }

    "have countryCodesJsonFilename" in {
      config.countryCodesJsonFilename must be("location-autocomplete-canonical-list.json")
    }

    "have countriesCsvFilename" in {
      config.countriesCsvFilename must be("mdg-country-codes.csv")
    }

    "have inventory Client Id" in {
      config.clientIdInventory must be("5c68d3b5-d8a7-4212-8688-6b67f18bbce7")
    }

    "have nrsServiceUrl" in {
      config.nrsServiceUrl must be("http://localhost:9479")
    }

    "have nrsApiKey" in {
      config.nrsApiKey must be("cds-exports")
    }

  }
}
