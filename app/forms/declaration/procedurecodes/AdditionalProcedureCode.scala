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

import forms.DeclarationPage
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.viewmodels.TariffContentKey
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.{hasSpecificLength, isEmpty, _}

case class AdditionalProcedureCode(additionalProcedureCode: Option[String])

object AdditionalProcedureCode extends DeclarationPage {
  implicit val format = Json.format[AdditionalProcedureCode]

  val additionalProcedureCodeKey = "additionalProcedureCode"
  private val additionalProcedureCodeLength = 3

  val mapping = Forms.mapping(
    additionalProcedureCodeKey -> optional(
      text()
        .verifying(
          "declaration.additionalProcedureCodes.error.invalid",
          isEmpty or (hasSpecificLength(additionalProcedureCodeLength) and isAlphanumeric)
        )
    )
  )(AdditionalProcedureCode.apply)(AdditionalProcedureCode.unapply)

  def form: Form[AdditionalProcedureCode] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    decType match {
      case CLEARANCE =>
        Seq(TariffContentKey("tariff.declaration.item.additionalProcedureCodes.clearance"))
      case _ =>
        Seq(TariffContentKey("tariff.declaration.item.additionalProcedureCodes.common"))
    }
}
