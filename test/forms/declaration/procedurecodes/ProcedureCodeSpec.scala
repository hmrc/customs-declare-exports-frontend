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
import forms.declaration.procedurecodes.ProcedureCode.procedureCodeKey
import models.viewmodels.TariffContentKey
import play.api.data.FormError

class ProcedureCodeSpec extends DeclarationPageBaseSpec {

  "Procedure Code form" should {

    "return form with errors" when {

      "procedure code is empty" in {
        val form = ProcedureCode.form.bind(Map(procedureCodeKey -> ""))

        form.errors mustBe Seq(FormError(procedureCodeKey, "declaration.procedureCodes.error.empty"))
      }

      "procedure code is incorrect" in {
        val form = ProcedureCode.form.bind(Map(procedureCodeKey -> "21"))

        form.errors mustBe Seq(FormError(procedureCodeKey, "declaration.procedureCodes.error.invalid"))
      }

      "procedure code has special chars" in {
        val form = ProcedureCode.form.bind(Map(procedureCodeKey -> "21##"))

        form.errors mustBe Seq(FormError(procedureCodeKey, "declaration.procedureCodes.error.invalid"))
      }

      "procedure code is incorrect with special chars" in {
        val form = ProcedureCode.form.bind(Map(procedureCodeKey -> "12321##"))

        form.errors mustBe Seq(FormError(procedureCodeKey, "declaration.procedureCodes.error.invalid"))
      }
    }
  }

  override def getCommonTariffKeys(messageKey: String): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"${messageKey}.common"))

  override def getClearanceTariffKeys(messageKey: String): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"${messageKey}.clearance"))

  "ProcedureCodes" when {
    testTariffContentKeys(ProcedureCode, "tariff.declaration.item.procedureCodes")
  }
}
