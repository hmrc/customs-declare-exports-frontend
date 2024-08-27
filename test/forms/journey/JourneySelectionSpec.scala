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

package forms.journey

import base.UnitSpec
import forms.journey.JourneySelection.{nonStandardJourneys, standardOrOtherJourneys}
import play.api.libs.json.{JsString, Json}

class JourneySelectionSpec extends UnitSpec {

  "Validation defined in JourneySelection mapping" should {

    "attach errors to form" when {
      "provided with empty input" in {
        List(standardOrOtherJourneys, nonStandardJourneys).foreach { acceptedJourneys =>
          val form = JourneySelection.form(acceptedJourneys).bind(JsString(""), JsonBindMaxChars)

          form.hasErrors must be(true)
          form.errors.length must equal(1)
          form.errors.head.message must equal("declaration.type.error")
        }
      }

      "provided with a value NOT defined in standardOrOtherJourneys" in {
        nonStandardJourneys.foreach { nonAcceptedJourney =>
          val form = JourneySelection.form(standardOrOtherJourneys).bind(Json.obj("type" -> nonAcceptedJourney), JsonBindMaxChars)

          form.hasErrors must be(true)
          form.errors.length must equal(1)
          form.errors.head.message must equal("declaration.type.error")
        }
      }

      "provided with a value NOT defined in nonStandardJourneys" in {
        standardOrOtherJourneys.foreach { nonAcceptedJourney =>
          val form = JourneySelection.form(nonStandardJourneys).bind(Json.obj("type" -> nonAcceptedJourney), JsonBindMaxChars)

          form.hasErrors must be(true)
          form.errors.length must equal(1)
          form.errors.head.message must equal("declaration.type.error")
        }
      }
    }

    "not attach any error" when {

      "provided with a value defined in standardOrOtherJourneys" in {
        standardOrOtherJourneys.foreach { acceptedJourney =>
          val form = JourneySelection.form(standardOrOtherJourneys).bind(Json.obj("type" -> acceptedJourney), JsonBindMaxChars)
          form.hasErrors must be(false)
        }
      }

      "provided with a value defined in nonStandardJourneys" in {
        nonStandardJourneys.foreach { acceptedJourney =>
          val form = JourneySelection.form(nonStandardJourneys).bind(Json.obj("type" -> acceptedJourney), JsonBindMaxChars)
          form.hasErrors must be(false)
        }
      }
    }
  }
}
