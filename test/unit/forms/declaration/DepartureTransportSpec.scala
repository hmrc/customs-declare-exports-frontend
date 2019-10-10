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

import base.TestHelper
import forms.declaration.DepartureTransport
import forms.declaration.TransportCodes._
import org.scalatest.{MustMatchers, WordSpec}

class DepartureTransportSpec extends WordSpec with MustMatchers {

  val form = DepartureTransport.form

  "Border Transport" should {

    "has correct form id" in {

      DepartureTransport.formId must be("BorderTransport")
    }
  }

  "Border Transport form" should {

    "allow all mode transport codes" in {

      val errors = allowedModeOfTransportCodes.map { code =>
        form.fillAndValidate(DepartureTransport(code, IMOShipIDNumber, "rereference")).errors
      }.toSeq.flatten

      errors must be(empty)
    }

    "allow all means of transport type codes" in {

      val errors = allowedMeansOfTransportTypeCodes.map { code =>
        form.fillAndValidate(DepartureTransport(Maritime, code, "reference")).errors
      }.toSeq.flatten

      errors must be(empty)
    }

    "has no errors" when {

      "user filled all mandatory fields with correct data" in {

        val correctForm = DepartureTransport(Maritime, IMOShipIDNumber, "reference")

        val result = form.fillAndValidate(correctForm)

        result.errors must be(empty)
      }
    }

    "has errors" when {

      "mandatory field are empty" in {

        val incorrectForm =
          Map("borderModeOfTransportCode" -> "", "meansOfTransportOnDepartureType" -> "", "meansOfTransportOnDepartureIDNumber" -> "")

        val result = form.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List("borderModeOfTransportCode", "meansOfTransportOnDepartureType", "meansOfTransportOnDepartureIDNumber"))
        errorMessages must be(
          List(
            "supplementary.transportInfo.borderTransportMode.error.empty",
            "supplementary.transportInfo.meansOfTransport.departure.error.empty",
            "supplementary.transportInfo.meansOfTransport.reference.error.empty"
          )
        )
      }

      "fields are incorrect" in {

        val incorrectForm = Map(
          "borderModeOfTransportCode" -> "incorrect",
          "meansOfTransportOnDepartureType" -> "incorrect",
          "meansOfTransportOnDepartureIDNumber" -> "correct"
        )

        val result = form.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List("borderModeOfTransportCode", "meansOfTransportOnDepartureType"))
        errorMessages must be(
          List(
            "supplementary.transportInfo.borderTransportMode.error.incorrect",
            "supplementary.transportInfo.meansOfTransport.departure.error.incorrect"
          )
        )
      }

      "means of transport on departure id number is empty" in {
        val incorrectForm = Map(
          "borderModeOfTransportCode" -> Maritime,
          "meansOfTransportOnDepartureType" -> IMOShipIDNumber,
          "meansOfTransportOnDepartureIDNumber" -> ""
        )

        val result = form.bind(incorrectForm)

        result.errors.length must be(1)

        val error = result.errors.head

        error.key must be("meansOfTransportOnDepartureIDNumber")
        error.message must be("supplementary.transportInfo.meansOfTransport.reference.error.empty")
      }

      "means of transport on departure id number is too long" in {

        val incorrectForm = Map(
          "borderModeOfTransportCode" -> Maritime,
          "meansOfTransportOnDepartureType" -> IMOShipIDNumber,
          "meansOfTransportOnDepartureIDNumber" -> TestHelper.createRandomAlphanumericString(28)
        )

        val result = form.bind(incorrectForm)

        result.errors.length must be(1)

        val error = result.errors.head

        error.key must be("meansOfTransportOnDepartureIDNumber")
        error.message must be("supplementary.transportInfo.meansOfTransport.reference.error.length")
      }

      "means of transport on departure id number contains invalid special characters" in {

        val incorrectForm = Map(
          "borderModeOfTransportCode" -> Maritime,
          "meansOfTransportOnDepartureType" -> IMOShipIDNumber,
          "meansOfTransportOnDepartureIDNumber" -> "!@#$"
        )

        val result = form.bind(incorrectForm)

        result.errors.length must be(1)

        val error = result.errors.head

        error.key must be("meansOfTransportOnDepartureIDNumber")
        error.message must be("supplementary.transportInfo.meansOfTransport.reference.error.invalid")
      }
    }
  }
}
