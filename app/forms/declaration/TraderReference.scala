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

import forms.DeclarationPage
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator._

case class TraderReference(value: String)

object TraderReference extends DeclarationPage {
  implicit val format: OFormat[TraderReference] = Json.format[TraderReference]

  val formId = "TraderReference"
  val traderReferenceKey = "traderReferenceInput"

  def form2data(traderReference: String): TraderReference = TraderReference(traderReference.trim)

  def form: Form[TraderReference] =
    Form(
      mapping(
        traderReferenceKey -> text()
          .verifying("declaration.traderReference.error.empty", nonEmpty)
          .verifying("declaration.traderReference.error.invalid", isEmpty or isValidTraderReference)
      )(form2data)(TraderReference.unapply)
    )

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.traderReference.common"))
}
