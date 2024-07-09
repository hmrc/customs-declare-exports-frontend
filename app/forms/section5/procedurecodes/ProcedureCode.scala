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

package forms.section5.procedurecodes

import forms.DeclarationPage
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.viewmodels.TariffContentKey
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator._

case class ProcedureCode(procedureCode: String)

object ProcedureCode extends DeclarationPage {
  implicit val format: OFormat[ProcedureCode] = Json.format[ProcedureCode]

  val procedureCodeKey = "procedureCode"
  private val procedureCodeLength = 4

  val mapping =
    Forms.mapping(
      procedureCodeKey ->
        text()
          .verifying("declaration.procedureCodes.error.empty", nonEmpty)
          .verifying("declaration.procedureCodes.error.invalid", isEmpty or (hasSpecificLength(procedureCodeLength) and isAlphanumeric))
    )(ProcedureCode.apply)(ProcedureCode.unapply)

  def form: Form[ProcedureCode] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    decType match {
      case CLEARANCE =>
        Seq(TariffContentKey("tariff.declaration.item.procedureCodes.clearance"))
      case _ =>
        Seq(TariffContentKey("tariff.declaration.item.procedureCodes.common"))
    }
}
