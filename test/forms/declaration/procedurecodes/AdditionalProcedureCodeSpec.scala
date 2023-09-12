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

package forms.declaration.procedurecodes

import forms.common.DeclarationPageBaseSpec
import forms.declaration.procedurecodes.AdditionalProcedureCode.additionalProcedureCodeKey
import models.viewmodels.TariffContentKey
import play.api.data.FormError

class AdditionalProcedureCodeSpec extends DeclarationPageBaseSpec {

  "AdditionalProcedureCodes form" should {

    "return form with errors" when {

      "additional code has incorrect length" in {
        val form = AdditionalProcedureCode.form.bind(Map(additionalProcedureCodeKey -> "123456"))

        form.errors.length mustBe 1
        form.errors.head mustBe FormError(additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.invalid")
      }

      "additional code contains special characters" in {

        val form = AdditionalProcedureCode.form.bind(Map(additionalProcedureCodeKey -> "#$%"))

        form.errors.length mustBe 1
        form.errors.head mustBe FormError(additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.invalid")
      }

      "additional code has incorrect length and contains special characters" in {
        val form = AdditionalProcedureCode.form.bind(Map(additionalProcedureCodeKey -> "123456#$"))

        form.errors.length mustBe 1
        form.errors.head mustBe FormError(additionalProcedureCodeKey, "declaration.additionalProcedureCodes.error.invalid")
      }
    }
  }

  override def getCommonTariffKeys(messageKey: String): Seq[TariffContentKey] =
    List(
      TariffContentKey(s"${messageKey}.1.common"),
      TariffContentKey(s"${messageKey}.2.common"),
      TariffContentKey(s"${messageKey}.3.common"),
      TariffContentKey(s"${messageKey}.4.common")
    )

  override def getClearanceTariffKeys(messageKey: String): Seq[TariffContentKey] =
    List(
      TariffContentKey(s"${messageKey}.1.clearance"),
      TariffContentKey(s"${messageKey}.2.common"),
      TariffContentKey(s"${messageKey}.3.common"),
      TariffContentKey(s"${messageKey}.4.common")
    )

  "AdditionalProcedureCodes" when {
    testTariffContentKeys(AdditionalProcedureCode, "tariff.declaration.item.additionalProcedureCodes")
  }
}
