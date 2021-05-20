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

package forms.declaration.procedurecodes

import forms.common.DeclarationPageBaseSpec
import forms.declaration.procedurecodes.AdditionalProcedureCodesSpec._
import models.viewmodels.TariffContentKey
import play.api.data.FormError

class AdditionalProcedureCodesSpec extends DeclarationPageBaseSpec {

  "AdditionalProcedureCodes form" should {

    "return form with errors" when {

      "additional code has incorrect length" in {
        val form = AdditionalProcedureCodes.form().bind(tooLongAdditionalProcedureCode)

        form.errors.length must be(1)
        form.errors.head must be(FormError("additionalProcedureCode", "declaration.additionalProcedureCodes.error.invalid"))
      }

      "additional code has special characters" in {

        val form = AdditionalProcedureCodes.form().bind(additionalProcedureCodeSpecialChars)

        form.errors.length must be(1)
        form.errors.head must be(FormError("additionalProcedureCode", "declaration.additionalProcedureCodes.error.invalid"))
      }

      "additional code has incorrect length with special characters" in {
        val form = AdditionalProcedureCodes.form().bind(tooLongAdditionalProcedureCodeWithSpecialCharacters)

        form.errors.length must be(1)
        form.errors.head must be(FormError("additionalProcedureCode", "declaration.additionalProcedureCodes.error.invalid"))
      }
    }
  }

  override def getCommonTariffKeys(messageKey: String): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"${messageKey}.common"))

  override def getClearanceTariffKeys(messageKey: String): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"${messageKey}.1.clearance"), TariffContentKey(s"${messageKey}.2.clearance"))

  "AdditionalProcedureCodes" when {
    testTariffContentKeys(AdditionalProcedureCodes, "tariff.declaration.item.additionalProcedureCodes")
  }

}

object AdditionalProcedureCodesSpec {

  val tooLongAdditionalProcedureCode: Map[String, String] =
    Map("additionalProcedureCode" -> "123456")

  val additionalProcedureCodeSpecialChars: Map[String, String] =
    Map("additionalProcedureCode" -> "#$%")

  val tooLongAdditionalProcedureCodeWithSpecialCharacters: Map[String, String] =
    Map("additionalProcedureCode" -> "123456#$")
}
