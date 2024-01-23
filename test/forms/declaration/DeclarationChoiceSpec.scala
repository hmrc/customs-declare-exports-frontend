/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.declaration

import base.UnitSpec
import forms.declaration.DeclarationChoice.{nonStandardJourneys, standardOrOtherJourneys}
import play.api.libs.json.{JsString, Json}

class DeclarationChoiceSpec extends UnitSpec {

  "Validation defined in DeclarationChoice mapping" should {

    "attach errors to form" when {
      "provided with empty input" in {
        List(standardOrOtherJourneys, nonStandardJourneys).foreach { acceptedJourneys =>
          val form = DeclarationChoice.form(acceptedJourneys).bind(JsString(""), JsonBindMaxChars)

          form.hasErrors must be(true)
          form.errors.length must equal(1)
          form.errors.head.message must equal("declaration.type.error")
        }
      }

      "provided with a value NOT defined in standardOrOtherJourneys" in {
        nonStandardJourneys.foreach { nonAcceptedJourney =>
          val form = DeclarationChoice.form(standardOrOtherJourneys).bind(Json.obj("type" -> nonAcceptedJourney), JsonBindMaxChars)

          form.hasErrors must be(true)
          form.errors.length must equal(1)
          form.errors.head.message must equal("declaration.type.error")
        }
      }

      "provided with a value NOT defined in nonStandardJourneys" in {
        standardOrOtherJourneys.foreach { nonAcceptedJourney =>
          val form = DeclarationChoice.form(nonStandardJourneys).bind(Json.obj("type" -> nonAcceptedJourney), JsonBindMaxChars)

          form.hasErrors must be(true)
          form.errors.length must equal(1)
          form.errors.head.message must equal("declaration.type.error")
        }
      }
    }

    "not attach any error" when {

      "provided with a value defined in standardOrOtherJourneys" in {
        standardOrOtherJourneys.foreach { acceptedJourney =>
          val form = DeclarationChoice.form(standardOrOtherJourneys).bind(Json.obj("type" -> acceptedJourney), JsonBindMaxChars)
          form.hasErrors must be(false)
        }
      }

      "provided with a value defined in nonStandardJourneys" in {
        nonStandardJourneys.foreach { acceptedJourney =>
          val form = DeclarationChoice.form(nonStandardJourneys).bind(Json.obj("type" -> acceptedJourney), JsonBindMaxChars)
          form.hasErrors must be(false)
        }
      }
    }
  }
}
