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

package forms.section5.procedurecodes

import forms.DeclarationPage
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.viewmodels.TariffContentKey
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator.{hasSpecificLength, isEmpty, _}

case class AdditionalProcedureCode(additionalProcedureCode: Option[String])

object AdditionalProcedureCode extends DeclarationPage {
  implicit val format: OFormat[AdditionalProcedureCode] = Json.format[AdditionalProcedureCode]

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

  override def defineTariffContentKeys(declarationType: DeclarationType): Seq[TariffContentKey] =
    declarationType match {
      case CLEARANCE =>
        List(
          TariffContentKey("tariff.declaration.item.additionalProcedureCodes.1.clearance"),
          TariffContentKey("tariff.declaration.item.additionalProcedureCodes.2.common"),
          TariffContentKey("tariff.declaration.item.additionalProcedureCodes.3.common"),
          TariffContentKey("tariff.declaration.item.additionalProcedureCodes.4.common")
        )
      case _ =>
        List(
          TariffContentKey("tariff.declaration.item.additionalProcedureCodes.1.common"),
          TariffContentKey("tariff.declaration.item.additionalProcedureCodes.2.common"),
          TariffContentKey("tariff.declaration.item.additionalProcedureCodes.3.common"),
          TariffContentKey("tariff.declaration.item.additionalProcedureCodes.4.common")
        )
    }
}
