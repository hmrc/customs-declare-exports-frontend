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

package unit.forms.declaration

import forms.declaration.{BorderTransport, TransportCodes}
import unit.base.FormSpec

class BorderTransportSpec extends FormSpec {

  val form = BorderTransport.form

  "Transport Details form" should {

    "has no errors" when {

      "only mandatory fields are provided with correct data" in {

        val correctForm = BorderTransport(None, false, "40", "reference", None)

        val result = form.fillAndValidate(correctForm)

        result.hasErrors must be(false)
      }

      "all fields contains correct data" in {

        val correctForm =
          BorderTransport(Some("United Kingdom"), false, "40", "Id.Number", Some(TransportCodes.cash))

        val result = form.fillAndValidate(correctForm)

        result.hasErrors must be(false)
      }
    }

    "has errors" when {
      "sending incorrect nationality" in {
        form.bind(Map("meansOfTransportCrossingTheBorderNationality" -> "fizz")).errors must contain(
          "supplementary.transportInfo.meansOfTransport.crossingTheBorder.nationality.error.incorrect"
        )
      }

      "sending no container info" in {
        form.bind(Map.empty[String, String]).errors must contain("supplementary.transportInfo.container.error.empty")
      }

      "sending no information about transport type" in {
        form.bind(Map.empty[String, String]).errors must contain(
          "supplementary.transportInfo.meansOfTransport.crossingTheBorder.error.empty"
        )
      }

      "sending non existing transport type" in {
        form.bind(Map("meansOfTransportCrossingTheBorderType" -> "donkey")).errors must contain(
          "supplementary.transportInfo.meansOfTransport.crossingTheBorder.error.incorrect"
        )
      }

      "sending no transport type reference" in {
        form.bind(Map.empty[String, String]).errors must contain("error.required")
      }

      "sending empty transport type reference" in {
        form.bind(Map("meansOfTransportCrossingTheBorderIDNumber" -> "")).errors must contain(
          "supplementary.transportInfo.meansOfTransport.CrossingTheBorder.IDNumber.error.empty"
        )
      }

      "sending very long transport type reference" in {
        form.bind(Map("meansOfTransportCrossingTheBorderIDNumber" -> "a" * 128)).errors must contain(
          "supplementary.transportInfo.meansOfTransport.CrossingTheBorder.IDNumber.error.length"
        )
      }

      "sending reference with special characters" in {
        form.bind(Map("meansOfTransportCrossingTheBorderIDNumber" -> "$#@!")).errors must contain(
          "supplementary.transportInfo.meansOfTransport.CrossingTheBorder.IDNumber.error.invalid"
        )
      }

      "sending non existing payment method" in {
        form.bind(Map("paymentMethod" -> "$#@!")).errors must contain("standard.transportDetails.paymentMethod.error")
      }
    }
  }
}
