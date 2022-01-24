/*
 * Copyright 2022 HM Revenue & Customs
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

import base.{FormSpec, TestHelper}
import forms.common.DeclarationPageBaseSpec
import forms.declaration.DepartureTransport.radioButtonGroupId
import forms.declaration.TransportCodes._

class DepartureTransportSpec extends FormSpec with DeclarationPageBaseSpec {

  "Departure Transport form" should {
    val form = DepartureTransport.form(transportCodesForV1)

    "allow all means of transport type codes" in {
      val errors = transportCodesForV1.map { transportCode =>
        form.fillAndValidate(DepartureTransport(Some(transportCode.value), Some("reference"))).errors
      }.flatten

      errors must be(empty)
    }

    "have no errors" when {
      "user filled all mandatory fields with correct data" in {
        val correctForm = DepartureTransport(Some(ShipOrRoroImoNumber.value), Some("reference"))
        val result = form.fillAndValidate(correctForm)
        result.errors must be(empty)
      }
    }

    "have errors" when {

      "transport type not selected" in {
        val incorrectForm = Map(radioButtonGroupId -> "", ShipOrRoroImoNumber.id -> "")

        val result = form.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List(radioButtonGroupId))
        errorMessages must be(List("declaration.transportInformation.meansOfTransport.departure.error.empty"))
      }

      "transport id not provided" in {
        val incorrectForm = Map(radioButtonGroupId -> ShipOrRoroImoNumber.value, ShipOrRoroImoNumber.id -> "")

        val result = form.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List(ShipOrRoroImoNumber.id))
        errorMessages must be(List("declaration.transportInformation.meansOfTransport.departure.error.empty.input"))
      }

      "means of transport on departure type is incorrect" in {
        val incorrectForm = Map(radioButtonGroupId -> "incorrect", ShipOrRoroImoNumber.id -> "correct")

        val result = form.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List(radioButtonGroupId))
        errorMessages must be(List("declaration.transportInformation.meansOfTransport.departure.error.incorrect"))
      }

      "means of transport on departure id number is too long" in {
        val incorrectForm = Map(
          radioButtonGroupId -> ShipOrRoroImoNumber.value,
          ShipOrRoroImoNumber.id -> TestHelper.createRandomAlphanumericString(36)
        )

        val result = form.bind(incorrectForm)
        result.errors.length must be(2)

        val error = result.errors.head
        error.key must be(ShipOrRoroImoNumber.id)
        error.message must be("declaration.transportInformation.meansOfTransport.departure.error.length")
      }

      "means of transport on departure id number contains invalid special characters" in {
        val incorrectForm = Map(radioButtonGroupId -> ShipOrRoroImoNumber.value, ShipOrRoroImoNumber.id -> "!@#$")

        val result = form.bind(incorrectForm)
        result.errors.length must be(1)

        val error = result.errors.head
        error.key must be(ShipOrRoroImoNumber.id)
        error.message must be("declaration.transportInformation.meansOfTransport.departure.error.invalid")
      }

      "means of transport on departure id number is too long with invalid characters" in {
        val incorrectForm = Map(
          radioButtonGroupId -> ShipOrRoroImoNumber.value,
          ShipOrRoroImoNumber.id -> (TestHelper.createRandomAlphanumericString(36) + "!@#$")
        )

        val result = form.bind(incorrectForm)
        result.errors.length must be(2)

        val error = result.errors.head
        error.key must be(ShipOrRoroImoNumber.id)
        error.message must be("declaration.transportInformation.meansOfTransport.departure.error.length")
      }
    }
  }

  "Departure Transport form for clearance request" should {
    val form = DepartureTransport.form(transportCodesForV3WhenPC0019)

    "allow all means of transport type codes" in {
      val errors = transportCodesForV3WhenPC0019.map { transportCode =>
        form.fillAndValidate(DepartureTransport(Some(transportCode.value), Some("reference"))).errors
      }.flatten

      errors must be(empty)
    }

    "have no errors" when {

      "user filled all mandatory fields with correct data" in {
        val correctForm = DepartureTransport(Some(ShipOrRoroImoNumber.value), Some("reference"))
        val result = form.fillAndValidate(correctForm)
        result.errors must be(empty)
      }

      "user selected 'none'" in {
        val correctForm = DepartureTransport(Some(NotApplicable.value), None)
        val result = form.fillAndValidate(correctForm)
        result.errors must be(empty)
      }
    }

    "have errors" when {

      "user provided no selection" in {
        val incorrectForm = Map(radioButtonGroupId -> "", FlightNumber.id -> "")

        val result = DepartureTransport.form(transportCodesForV1).bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List(radioButtonGroupId))
        errorMessages must be(List("declaration.transportInformation.meansOfTransport.departure.error.empty"))
      }

      "'0019' has been entered as Procedure code and" when {
        "user provided no selection" in {
          val incorrectForm = Map(radioButtonGroupId -> "", FlightNumber.id -> "")

          val result = form.bind(incorrectForm)
          val errorKeys = result.errors.map(_.key)
          val errorMessages = result.errors.map(_.message)

          errorKeys must be(List(radioButtonGroupId))
          errorMessages must be(List("declaration.transportInformation.meansOfTransport.departure.error.empty.v3"))
        }
      }

      "transport id not provided" in {
        val incorrectForm = Map(radioButtonGroupId -> FlightNumber.value, FlightNumber.id -> "")

        val result = form.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List(FlightNumber.id))
        errorMessages must be(List("declaration.transportInformation.meansOfTransport.departure.error.empty.input"))
      }

      "means of transport on departure type is incorrect" in {
        val incorrectForm = Map(radioButtonGroupId -> "incorrect", FlightNumber.id -> "correct")

        val result = form.bind(incorrectForm)
        val errorKeys = result.errors.map(_.key)
        val errorMessages = result.errors.map(_.message)

        errorKeys must be(List(radioButtonGroupId))
        errorMessages must be(List("declaration.transportInformation.meansOfTransport.departure.error.incorrect"))
      }

      "means of transport on departure id number is too long" in {
        val incorrectForm = Map(
          radioButtonGroupId -> FlightNumber.value,
          FlightNumber.id -> TestHelper.createRandomAlphanumericString(36)
        )

        val result = form.bind(incorrectForm)
        result.errors.length must be(2)

        val error = result.errors.head
        error.key must be(FlightNumber.id)
        error.message must be("declaration.transportInformation.meansOfTransport.departure.error.length")
      }

      "means of transport on departure id number contains invalid special characters" in {
        val incorrectForm = Map(radioButtonGroupId -> FlightNumber.value, FlightNumber.id -> "!@#$")

        val result = form.bind(incorrectForm)
        result.errors.length must be(1)

        val error = result.errors.head
        error.key must be(FlightNumber.id)
        error.message must be("declaration.transportInformation.meansOfTransport.departure.error.invalid")
      }
    }
  }

  "DepartureTransport" when {
    testTariffContentKeys(DepartureTransport, "tariff.declaration.departureTransport")
  }
}
