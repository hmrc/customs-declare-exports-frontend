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
import forms.declaration.TransportCodes._
import forms.declaration.{DepartureTransport, TransportCodes}
import models.DeclarationType
import unit.base.FormSpec

class DepartureTransportSpec extends FormSpec {

  "Departure Transport form" should {
    val form = DepartureTransport.form(DeclarationType.STANDARD)

    "allow all means of transport type codes" in {

      val errors = allowedMeansOfTransportTypeCodes.map { code =>
        form.fillAndValidate(DepartureTransport(Some(code), Some("reference"))).errors
      }.toSeq.flatten

      errors must be(empty)
    }

    "has no errors" when {

      "user filled all mandatory fields with correct data" in {

        val correctForm = DepartureTransport(Some(IMOShipIDNumber), Some("reference"))

        val result = form.fillAndValidate(correctForm)

        result.errors must be(empty)
      }
    }

    "has errors" when {

      val transportTypeField = DepartureTransport.meansOfTransportOnDepartureTypeKey
      val idNumberField = s"meansOfTransportOnDepartureIDNumber_$IMOShipIDNumber"

      "transport type not selected" in {

        val incorrectForm =
          Map(transportTypeField -> "", idNumberField -> "")

        val result = form.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List(transportTypeField))
        errorMessages must be(List("declaration.transportInformation.meansOfTransport.departure.error.empty"))
      }

      "transport id not provided" in {

        val incorrectForm =
          Map(transportTypeField -> IMOShipIDNumber, idNumberField -> "")

        val result = form.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List(idNumberField))
        errorMessages must be(List("declaration.transportInformation.meansOfTransport.reference.error.empty"))
      }

      "means of transport on departure type is incorrect" in {

        val incorrectForm = Map(transportTypeField -> "incorrect", idNumberField -> "correct")

        val result = form.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List(transportTypeField))
        errorMessages must be(List("declaration.transportInformation.meansOfTransport.departure.error.incorrect"))
      }

      "means of transport on departure id number is too long" in {

        val incorrectForm = Map(transportTypeField -> IMOShipIDNumber, idNumberField -> TestHelper.createRandomAlphanumericString(36))

        val result = form.bind(incorrectForm)

        result.errors.length must be(1)

        val error = result.errors.head

        error.key must be(idNumberField)
        error.message must be("declaration.transportInformation.meansOfTransport.reference.error.invalid")
      }

      "means of transport on departure id number contains invalid special characters" in {

        val incorrectForm = Map(transportTypeField -> IMOShipIDNumber, idNumberField -> "!@#$")

        val result = form.bind(incorrectForm)

        result.errors.length must be(1)

        val error = result.errors.head

        error.key must be(idNumberField)
        error.message must be("declaration.transportInformation.meansOfTransport.reference.error.invalid")
      }

      "means of transport on departure id number is too long with invalid characters" in {

        val incorrectForm = Map(transportTypeField -> IMOShipIDNumber, idNumberField -> (TestHelper.createRandomAlphanumericString(36) + "!@#$"))

        val result = form.bind(incorrectForm)

        result.errors.length must be(1)

        val error = result.errors.head

        error.key must be(idNumberField)
        error.message must be("declaration.transportInformation.meansOfTransport.reference.error.invalid")
      }
    }
  }

  "Departure Transport form for clearance request" should {
    val form = DepartureTransport.form(DeclarationType.CLEARANCE)

    "allow all means of transport type codes" in {

      val errors = (allowedMeansOfTransportTypeCodes + TransportCodes.OptionNone).map { code =>
        form.fillAndValidate(DepartureTransport(Some(code), Some("reference"))).errors
      }.toSeq.flatten

      errors must be(empty)
    }

    "has no errors" when {

      "user filled all mandatory fields with correct data" in {

        val correctForm = DepartureTransport(Some(IMOShipIDNumber), Some("reference"))

        val result = form.fillAndValidate(correctForm)

        result.errors must be(empty)
      }

      "user selected 'none'" in {

        val correctForm = DepartureTransport(Some(OptionNone), None)

        val result = form.fillAndValidate(correctForm)

        result.errors must be(empty)
      }
    }

    "has errors" when {

      val transportTypeField = DepartureTransport.meansOfTransportOnDepartureTypeKey
      val idNumberField = s"meansOfTransportOnDepartureIDNumber_$IATAFlightNumber"

      "user provided no selection" in {

        val incorrectForm =
          Map(transportTypeField -> "", idNumberField -> "")

        val result = form.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List(transportTypeField))
        errorMessages must be(List("declaration.transportInformation.meansOfTransport.departure.error.empty.optional"))
      }

      "transport id not provided" in {

        val incorrectForm =
          Map(transportTypeField -> IATAFlightNumber, idNumberField -> "")

        val result = form.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List(idNumberField))
        errorMessages must be(List("declaration.transportInformation.meansOfTransport.reference.error.empty"))
      }

      "means of transport on departure type is incorrect" in {

        val incorrectForm = Map(transportTypeField -> "incorrect", idNumberField -> "correct")

        val result = form.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List(transportTypeField))
        errorMessages must be(List("declaration.transportInformation.meansOfTransport.departure.error.incorrect"))
      }

      "means of transport on departure id number is too long" in {

        val incorrectForm = Map(transportTypeField -> IATAFlightNumber, idNumberField -> TestHelper.createRandomAlphanumericString(36))

        val result = form.bind(incorrectForm)

        result.errors.length must be(1)

        val error = result.errors.head

        error.key must be(idNumberField)
        error.message must be("declaration.transportInformation.meansOfTransport.reference.error.invalid")
      }

      "means of transport on departure id number contains invalid special characters" in {

        val incorrectForm = Map(transportTypeField -> IATAFlightNumber, idNumberField -> "!@#$")

        val result = form.bind(incorrectForm)

        result.errors.length must be(1)

        val error = result.errors.head

        error.key must be(idNumberField)
        error.message must be("declaration.transportInformation.meansOfTransport.reference.error.invalid")
      }
    }
  }
}
