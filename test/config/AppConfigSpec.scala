/*
 * Copyright 2018 HM Revenue & Customs
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
import play.api.mvc.Call

class AppConfigSpec extends CustomExportsBaseSpec {

  val config = app.injector.instanceOf[AppConfig]

  "The config" should {
    "have analytics token" in {
      config.analyticsToken must be ("N/A")
    }

    "have analytics host" in {
      config.analyticsHost must be ("auto")
    }

    "have auth url" in {
      config.authUrl must be ("http://localhost:8500")
    }

    "have login url" in {
      config.loginUrl must be ("http://localhost:9949/auth-login-stub/gg-sign-in")
    }

    "have login continue url" in {
      config.loginContinueUrl must be ("http://localhost:9000/customs-declare-exports-frontend")
    }

    "have customs declarations url" in {
      config.customsDeclarationsUrl must be ("http://localhost:9820")
    }

    "have language translation enabled field" in {
      config.languageTranslationEnabled must be (false)
    }

    "have language map with English" in {
      config.languageMap.get("english").isDefined must be (true)
    }

    "have language map with Cymraeg" in {
      config.languageMap.get("cymraeg").isDefined must be (true)
    }

    "have default feature status" in {
      config.defaultFeatureStatus must be (FeatureStatus.disabled)
    }

    "return correct status for feature" in {
      config.featureStatus(Feature.default) must be (FeatureStatus.disabled)
    }

    "return correct value for isFeatureOn method" in {
      config.isFeatureOn(Feature.default) must be (false)
    }
    "have a submit import declarations uri" in {
      config.submitImportDeclarationUri must be ("/")
    }

    "have customs declarations endpoint" in {
      config.customsDeclarationsEndpoint must be ("http://localhost:6790")
    }

    "have customs declarations API version" in {
      config.customsDeclarationsApiVersion must be ("2.0")
    }

    "have HMRC Developer Hub Client ID" in {
      config.developerHubClientId must be ("customs-declare-imports-frontend")
    }
  }
}
