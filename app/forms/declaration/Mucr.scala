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
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator._

case class Mucr(mucr: String)

object Mucr extends DeclarationPage {

  implicit val format: OFormat[Mucr] = Json.format[Mucr]

  val MUCR = "MUCR"

  def form2Data(mucr: String): Mucr = Mucr(mucr.toUpperCase)

  val mapping = Forms.mapping(
    MUCR -> text()
      .verifying("declaration.mucr.error.empty", nonEmpty)
      .verifying("declaration.mucr.error.invalid", isEmpty or isValidMucr)
      .verifying("declaration.mucr.error.length", isEmpty or noLongerThan(35))
  )(form2Data)(Mucr.unapply)

  def form: Form[Mucr] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.mucr.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
