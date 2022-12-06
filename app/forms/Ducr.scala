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

package forms

import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.viewmodels.TariffContentKey
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class Ducr(ducr: String)

object Ducr extends DeclarationPage {
  implicit val format = Json.format[Ducr]
  val mapping: Mapping[Ducr] =
    Forms.mapping(
      "ducr" ->
        text()
          .verifying("declaration.consignmentReferences.ducr.error.empty", nonEmpty)
          .verifying("declaration.consignmentReferences.ducr.error.invalid", isEmpty or isValidDucr)
    )(form2Data)(Ducr.unapply)
  val form: Form[Ducr] = Form(mapping)

  def form2Data(ducr: String): Ducr = new Ducr(ducr.toUpperCase)

  def model2Form: Ducr => Option[String] =
    model => Some(model.ducr)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    decType match {
      case CLEARANCE => Seq(TariffContentKey("tariff.declaration.ducr.1.clearance"))
      case _ =>
        Seq(
          TariffContentKey("tariff.declaration.consignmentReferences.1.common"),
          TariffContentKey("tariff.declaration.consignmentReferences.3.common")
        )
    }
}
