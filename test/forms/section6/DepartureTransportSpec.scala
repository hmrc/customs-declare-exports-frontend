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

package forms.section6

import base.{FormSpec, MockTransportCodeService, TestHelper}
import forms.common.DeclarationPageBaseSpec
import forms.section6.DepartureTransport.radioButtonGroupId

class DepartureTransportSpec extends FormSpec with DeclarationPageBaseSpec {

  val tcs = MockTransportCodeService.transportCodeService

  val allTransportCodes = List(tcs.transportCodesForV1, tcs.transportCodesForV2, tcs.transportCodesForV3, tcs.transportCodesForV3WhenPC0019)
  val transportCodesIds = List("transportCodesForV1", "transportCodesForV2", "transportCodesForV3", "transportCodesForV3WhenPC0019")

  "Departure Transport form" when {

    allTransportCodes.zipWithIndex.foreach { transportCodesAndIndex =>
      val (transportCodes, index) = transportCodesAndIndex

      s"receives as input ${transportCodesIds(index)}" should {
        val form = DepartureTransport.form(transportCodes)(tcs)

        "have no errors" when {
          "user filled all mandatory fields with correct data" in {
            val errors = transportCodes.asList.flatMap { transportCode =>
              val transportId = if (transportCode == tcs.NotApplicable) None else Some("reference")
              val correctForm = DepartureTransport(Some(transportCode.value), transportId)
              form.fillAndValidate(correctForm).errors
            }
            errors must be(empty)
          }
        }

        "have errors" when {

          "transport type not selected" in {
            transportCodes.asList.foreach { transportCode =>
              val incorrectForm = Map(radioButtonGroupId -> "", transportCode.id -> "")

              val result = form.bind(incorrectForm)
              val errorKeys = result.errors.map(_.key)
              val errorMessages = result.errors.map(_.message)

              errorKeys must be(List(radioButtonGroupId))
              val suffix = if (transportCodes == tcs.transportCodesForV3WhenPC0019) ".v3" else ""
              errorMessages must be(List(s"declaration.transportInformation.meansOfTransport.departure.error.empty$suffix"))
            }
          }

          "transport id not provided" in {
            transportCodes.asList.filterNot(_ == tcs.NotApplicable).filterNot(_ == tcs.NotProvided).foreach { transportCode =>
              val incorrectForm = Map(radioButtonGroupId -> transportCode.value, transportCode.id -> "")

              val result = form.bind(incorrectForm)
              val errorKeys = result.errors.map(_.key)
              val errorMessages = result.errors.map(_.message)

              errorKeys must be(List(transportCode.id))
              errorMessages must be(List("declaration.transportInformation.meansOfTransport.departure.error.empty.input"))
            }
          }

          "means of transport on departure type is incorrect" in {
            transportCodes.asList.foreach { transportCode =>
              val incorrectForm = Map(radioButtonGroupId -> "incorrect", transportCode.id -> "correct")

              val result = form.bind(incorrectForm)
              val errorKeys = result.errors.map(_.key)
              val errorMessages = result.errors.map(_.message)

              errorKeys must be(List(radioButtonGroupId))
              errorMessages must be(List("declaration.transportInformation.meansOfTransport.departure.error.incorrect"))
            }
          }

          "means of transport on departure id number is too long" in {
            val tooLong = TestHelper.createRandomAlphanumericString(36)

            transportCodes.asList.filterNot(_ == tcs.NotApplicable).filterNot(_ == tcs.NotProvided).foreach { transportCode =>
              val incorrectForm = Map(radioButtonGroupId -> transportCode.value, transportCode.id -> tooLong)

              val result = form.bind(incorrectForm)
              result.errors.length must be(2)

              val error = result.errors.head
              error.key must be(transportCode.id)
              error.message must be("declaration.transportInformation.meansOfTransport.departure.error.length")
            }
          }

          "means of transport on departure id number contains invalid special characters" in {
            transportCodes.asList.filterNot(_ == tcs.NotApplicable).filterNot(_ == tcs.NotProvided).foreach { transportCode =>
              val incorrectForm = Map(radioButtonGroupId -> transportCode.value, transportCode.id -> "!@#$")

              val result = form.bind(incorrectForm)
              result.errors.length must be(1)

              val error = result.errors.head
              error.key must be(transportCode.id)
              error.message must be("declaration.transportInformation.meansOfTransport.departure.error.invalid")
            }
          }

          "means of transport on departure id number is too long with invalid characters" in {
            val tooLongAndInvalid = TestHelper.createRandomAlphanumericString(36) + "!@#$"

            transportCodes.asList.filterNot(_ == tcs.NotApplicable).filterNot(_ == tcs.NotProvided).foreach { transportCode =>
              val incorrectForm = Map(radioButtonGroupId -> transportCode.value, transportCode.id -> tooLongAndInvalid)

              val result = form.bind(incorrectForm)
              result.errors.length must be(2)

              val error = result.errors.head
              error.key must be(transportCode.id)
              error.message must be("declaration.transportInformation.meansOfTransport.departure.error.length")
            }
          }
        }
      }
    }
  }
}
