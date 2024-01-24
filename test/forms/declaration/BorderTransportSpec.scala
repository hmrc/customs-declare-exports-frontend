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

import base.MockTransportCodeService
import forms.common.DeclarationPageBaseSpec
import forms.declaration.BorderTransport.{form, radioButtonGroupId}
import models.viewmodels.TariffContentKey
import play.api.i18n.{Lang, Messages}
import play.api.test.Helpers._
import services.TransportCodeService

import java.util.Locale

class BorderTransportSpec extends DeclarationPageBaseSpec {

  val prefix = "declaration.transportInformation.meansOfTransport.crossingTheBorder"

  implicit val messages: Messages = stubMessagesApi().preferred(Seq(Lang(Locale.ENGLISH)))

  implicit val transportCodeService: TransportCodeService = MockTransportCodeService.transportCodeService

  "Transport Details form" when {

    "have no errors" when {
      "all BorderTransport's fields contain the expected data" in {
        transportCodeService.transportCodesOnBorderTransport.foreach { transportCode =>
          val correctForm = BorderTransport(transportCode.value, "Id.Number")

          val result = form.fillAndValidate(correctForm)
          result.hasErrors must be(false)
        }
      }
    }

    "have errors" when {
      "sending no information about transport type" in {
        val errors = form.bind(Map(radioButtonGroupId -> "")).errors
        errors.map(_.key) must be(List(radioButtonGroupId))
        errors.map(_.message) must be(List(s"$prefix.error.empty"))
      }

      "sending non existing transport type" in {
        val errors = form.bind(Map(radioButtonGroupId -> "abcd123")).errors
        errors.map(_.key) must be(List(radioButtonGroupId))
        errors.map(_.message) must be(List(s"$prefix.error.incorrect"))
      }

      "sending empty transport type reference" in {
        transportCodeService.transportCodesOnBorderTransport.foreach { transportCode =>
          val errors = form.bind(Map(radioButtonGroupId -> transportCode.value, transportCode.id -> "")).errors
          errors.map(_.key) must be(List(transportCode.id))
          errors.map(_.message) must be(List(s"$prefix.IDNumber.error.empty"))
        }
      }

      "sending very long transport type reference" in {
        transportCodeService.transportCodesOnBorderTransport.foreach { transportCode =>
          val errors = form.bind(Map(radioButtonGroupId -> transportCode.value, transportCode.id -> "a" * 128)).errors
          errors.map(_.key) must be(List(transportCode.id))
          errors.map(_.message) must be(List(s"$prefix.IDNumber.error.length"))
        }
      }

      "sending reference with special characters" in {
        transportCodeService.transportCodesOnBorderTransport.foreach { transportCode =>
          val errors = form.bind(Map(radioButtonGroupId -> transportCode.value, transportCode.id -> "$#@!")).errors
          errors.map(_.key) must be(List(transportCode.id))
          errors.map(_.message) must be(List(s"$prefix.IDNumber.error.invalid"))
        }
      }
    }
  }

  override def getCommonTariffKeys(messageKey: String): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"${messageKey}.common"))

  "BorderTransport" when {
    testTariffContentKeysNoSpecialisation(BorderTransport, "tariff.declaration.borderTransport")
  }
}
