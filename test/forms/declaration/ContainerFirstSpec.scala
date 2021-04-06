/*
 * Copyright 2021 HM Revenue & Customs
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

import forms.common.DeclarationPageBaseSpec
import forms.declaration.ContainerFirst._
import models.viewmodels.TariffContentKey
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString}

class ContainerFirstSpec extends DeclarationPageBaseSpec {

  def formData(hasContainer: String, containerId: Option[String]) =
    JsObject(Map(hasContainerKey -> JsString(hasContainer), containerIdKey -> JsString(containerId.getOrElse(""))))

  "ContainerYesNo mapping" should {

    "return form with errors" when {

      "provided with non-alphanumeric id" in {
        val form = ContainerFirst.form.bind(formData("Yes", Some("!2345678")))

        form.errors mustBe Seq(FormError(containerIdKey, "declaration.transportInformation.containerId.error.invalid"))
      }

      "provided with id too long" in {
        val form = ContainerFirst.form.bind(formData("Yes", Some("123456789012345678")))

        form.errors mustBe Seq(FormError(containerIdKey, "declaration.transportInformation.containerId.error.length"))
      }

      "provided with no id when user said yes" in {
        val form = ContainerFirst.form.bind(formData("Yes", None))

        form.errors mustBe Seq(FormError(containerIdKey, "declaration.transportInformation.containerId.empty"))
      }

      "no answer for yes/no" in {
        val form = ContainerFirst.form.bind(formData("", None))

        form.errors mustBe Seq(FormError(hasContainerKey, "declaration.transportInformation.container.answer.empty"))
      }
    }

    "return form without errors" when {
      "provided with valid input when user said Yes" in {
        val form = ContainerFirst.form.bind(formData("Yes", Some("1234ABCD")))

        form.hasErrors must be(false)
      }

      "provided with no input when user said No" in {
        val form = ContainerFirst.form.bind(formData("No", None))

        form.hasErrors must be(false)
      }

    }
  }

  override def getClearanceTariffKeys(messageKey: String): Seq[TariffContentKey] =
    Seq(
      TariffContentKey(s"${messageKey}.1.clearance"),
      TariffContentKey(s"${messageKey}.2.clearance"),
      TariffContentKey(s"${messageKey}.3.clearance")
    )

  "ContainerFirst" when {
    testTariffContentKeys(ContainerFirst, "tariff.declaration.container")
  }
}
