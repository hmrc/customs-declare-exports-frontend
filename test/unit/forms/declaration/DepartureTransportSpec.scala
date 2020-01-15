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

package unit.forms.declaration

import base.TestHelper
import forms.declaration.DepartureTransport
import forms.declaration.TransportCodes._
import unit.base.FormSpec

class DepartureTransportSpec extends FormSpec {

  val form = DepartureTransport.form

  "Departure Transport form" should {

    "allow all means of transport type codes" in {

      val errors = allowedMeansOfTransportTypeCodes.map { code =>
        form.fillAndValidate(DepartureTransport(code, "reference")).errors
      }.toSeq.flatten

      errors must be(empty)
    }

    "has no errors" when {

      "user filled all mandatory fields with correct data" in {

        val correctForm = DepartureTransport(IMOShipIDNumber, "reference")

        val result = form.fillAndValidate(correctForm)

        result.errors must be(empty)
      }
    }

    "has errors" when {

      "mandatory field are empty" in {

        val incorrectForm =
          Map("meansOfTransportOnDepartureType" -> "", "meansOfTransportOnDepartureIDNumber" -> "")

        val result = form.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List("meansOfTransportOnDepartureType", "meansOfTransportOnDepartureIDNumber"))
        errorMessages must be(
          List(
            "declaration.transportInformation.meansOfTransport.departure.error.empty",
            "declaration.transportInformation.meansOfTransport.reference.error.empty"
          )
        )
      }

      "fields are incorrect" in {

        val incorrectForm = Map("meansOfTransportOnDepartureType" -> "incorrect", "meansOfTransportOnDepartureIDNumber" -> "correct")

        val result = form.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List("meansOfTransportOnDepartureType"))
        errorMessages must be(List("declaration.transportInformation.meansOfTransport.departure.error.incorrect"))
      }

      "means of transport on departure id number is empty" in {
        val incorrectForm = Map("meansOfTransportOnDepartureType" -> IMOShipIDNumber, "meansOfTransportOnDepartureIDNumber" -> "")

        val result = form.bind(incorrectForm)

        result.errors.length must be(1)

        val error = result.errors.head

        error.key must be("meansOfTransportOnDepartureIDNumber")
        error.message must be("declaration.transportInformation.meansOfTransport.reference.error.empty")
      }

      "means of transport on departure id number is too long" in {

        val incorrectForm = Map(
          "meansOfTransportOnDepartureType" -> IMOShipIDNumber,
          "meansOfTransportOnDepartureIDNumber" -> TestHelper.createRandomAlphanumericString(28)
        )

        val result = form.bind(incorrectForm)

        result.errors.length must be(1)

        val error = result.errors.head

        error.key must be("meansOfTransportOnDepartureIDNumber")
        error.message must be("declaration.transportInformation.meansOfTransport.reference.error.invalid")
      }

      "means of transport on departure id number contains invalid special characters" in {

        val incorrectForm = Map("meansOfTransportOnDepartureType" -> IMOShipIDNumber, "meansOfTransportOnDepartureIDNumber" -> "!@#$")

        val result = form.bind(incorrectForm)

        result.errors.length must be(1)

        val error = result.errors.head

        error.key must be("meansOfTransportOnDepartureIDNumber")
        error.message must be("declaration.transportInformation.meansOfTransport.reference.error.invalid")
      }

      "means of transport on departure id number is too long with invalid characters" in {

        val incorrectForm = Map(
          "meansOfTransportOnDepartureType" -> IMOShipIDNumber,
          "meansOfTransportOnDepartureIDNumber" -> (TestHelper.createRandomAlphanumericString(28) + "!@#$")
        )

        val result = form.bind(incorrectForm)

        result.errors.length must be(1)

        val error = result.errors.head

        error.key must be("meansOfTransportOnDepartureIDNumber")
        error.message must be("declaration.transportInformation.meansOfTransport.reference.error.invalid")
      }
    }
  }
}
