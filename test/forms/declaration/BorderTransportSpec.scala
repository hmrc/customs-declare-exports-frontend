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

import play.api.test.Helpers._
import base.FormSpec
import connectors.CodeListConnector
import forms.common.DeclarationPageBaseSpec
import forms.declaration.BorderTransport.{form, nationalityId, radioButtonGroupId}
import forms.declaration.TransportCodes._
import models.codes.Country
import models.viewmodels.TariffContentKey
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang

import java.util.Locale
import scala.collection.immutable.ListMap

class BorderTransportSpec extends FormSpec with DeclarationPageBaseSpec with MockitoSugar with BeforeAndAfterEach {

  val nationality = "United Kingdom, Great Britain, Northern Ireland"

  implicit val mockCodeListConnector = mock[CodeListConnector]
  implicit val messages = stubMessagesApi().preferred(Seq(Lang(Locale.ENGLISH)))

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockCodeListConnector.getCountryCodes(any()))
      .thenReturn(ListMap("GB" -> Country(nationality, "GB")))
  }

  override protected def afterEach(): Unit = {
    reset(mockCodeListConnector)
    super.afterEach()
  }

  "Transport Details form" should {

    "has no errors" when {

      "only mandatory fields are provided with correct data" in {
        val correctForm = BorderTransport(None, FlightNumber.value, "reference")

        val result = form.fillAndValidate(correctForm)
        result.hasErrors must be(false)
      }

      "all fields contains correct data" in {
        val correctForm = BorderTransport(Some(nationality), FlightNumber.value, "Id.Number")

        val result = form.fillAndValidate(correctForm)
        result.hasErrors must be(false)
      }
    }

    "has errors" when {
      "sending incorrect nationality" in {
        form.bind(Map(nationalityId -> "fizz")).errors must contain(
          "declaration.transportInformation.meansOfTransport.crossingTheBorder.nationality.error.incorrect"
        )
      }

      "sending no information about transport type" in {
        form.bind(Map.empty[String, String]).errors must contain("declaration.transportInformation.meansOfTransport.crossingTheBorder.error.empty")
      }

      "sending non existing transport type" in {
        form.bind(Map(radioButtonGroupId -> "donkey")).errors must contain(
          "declaration.transportInformation.meansOfTransport.crossingTheBorder.error.incorrect"
        )
      }

      "sending empty transport type reference" in {
        form.bind(Map(radioButtonGroupId -> ShipOrRoroImoNumber.value, ShipOrRoroImoNumber.id -> "")).errors must contain(
          "declaration.transportInformation.meansOfTransport.crossingTheBorder.IDNumber.error.empty"
        )
      }

      "sending very long transport type reference" in {
        form
          .bind(
            Map(
              radioButtonGroupId -> AircraftRegistrationNumber.value,
              AircraftRegistrationNumber.id -> "a" * 128
            )
          )
          .errors must contain("declaration.transportInformation.meansOfTransport.crossingTheBorder.IDNumber.error.length")
      }

      "sending reference with special characters" in {
        form
          .bind(
            Map(radioButtonGroupId -> VehicleRegistrationNumber.value, VehicleRegistrationNumber.id -> "$#@!")
          )
          .errors must contain("declaration.transportInformation.meansOfTransport.crossingTheBorder.IDNumber.error.invalid")
      }

    }
  }

  override def getCommonTariffKeys(messageKey: String): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"${messageKey}.1.common"), TariffContentKey(s"${messageKey}.2.common"))

  "BorderTransport" when {
    testTariffContentKeysNoSpecialisation(BorderTransport, "tariff.declaration.borderTransport")
  }
}
