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

import forms.declaration.{TransportCodes, TransportDetails}
import org.scalatest.{MustMatchers, WordSpec}

class TransportDetailsSpec extends WordSpec with MustMatchers {

  val form = TransportDetails.form

  "Transport Details form" should {

    "has no errors" when {

      "only mandatory fields are provided with correct data" in {

        val correctForm = TransportDetails(None, false, "40", None, None)

        val result = form.fillAndValidate(correctForm)

        result.hasErrors must be(false)
      }

      "all fields contains correct data" in {

        val correctForm =
          TransportDetails(Some("United Kingdom"), false, "40", Some("Id.Number"), Some(TransportCodes.cash))

        val result = form.fillAndValidate(correctForm)

        result.hasErrors must be(false)
      }
    }

    "has errors" when {

      "mandatory field is not filled" in {

        val wrongData = Map(
          "meansOfTransportCrossingTheBorderNationality" -> "",
          "container" -> "",
          "meansOfTransportCrossingTheBorderType" -> "",
          "meansOfTransportCrossingTheBorderIDNumber" -> "",
          "paymentMethod" -> ""
        )

        val result = form.bind(wrongData)

        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.messages.head)

        errorKeys must be(List("container", "meansOfTransportCrossingTheBorderType"))
        errorMessages must be(
          List(
            "supplementary.transportInfo.container.error.empty",
            "supplementary.transportInfo.meansOfTransport.crossingTheBorder.error.empty"
          )
        )
      }

      "user input is incorrect" in {

        val incorrectData = Map(
          "meansOfTransportCrossingTheBorderNationality" -> "incorrectCountry",
          "container" -> "false",
          "meansOfTransportCrossingTheBorderType" -> "incorrectTransportTypeCode",
          "meansOfTransportCrossingTheBorderIDNumber" -> "!@#$",
          "paymentMethod" -> "!@#$"
        )

        val result = form.bind(incorrectData)

        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.messages.head)

        errorKeys must be(
          List(
            "meansOfTransportCrossingTheBorderNationality",
            "meansOfTransportCrossingTheBorderType",
            "meansOfTransportCrossingTheBorderIDNumber",
            "paymentMethod"
          )
        )

        errorMessages must be(
          List(
            "supplementary.transportInfo.meansOfTransport.crossingTheBorder.nationality.error.incorrect",
            "supplementary.transportInfo.meansOfTransport.crossingTheBorder.error.incorrect",
            "supplementary.transportInfo.meansOfTransport.idNumber.invalid",
            "standard.transportDetails.paymentMethod.error"
          )
        )
      }
    }
  }
}
