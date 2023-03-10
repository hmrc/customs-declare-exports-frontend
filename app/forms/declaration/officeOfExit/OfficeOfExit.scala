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

package forms.declaration.officeOfExit
import forms.DeclarationPage
import models.viewmodels.TariffContentKey
import models.DeclarationType.DeclarationType
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import play.api.data.{Form, Forms, Mapping}
import play.api.data.Forms.text
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.{hasSpecificLength, isEmpty, nonEmpty, _}

case class OfficeOfExit(officeId: String) extends Ordered[OfficeOfExit] {
  override def compare(that: OfficeOfExit): Int =
    officeId.compareTo(that.officeId)
}

object OfficeOfExit extends DeclarationPage with FieldMapping {
  implicit val format = Json.format[OfficeOfExit]

  val pointer: ExportsFieldPointer = "officeOfExit.officeId"

  private val officeId = "officeId"

  val formId = "OfficeOfExit"

  val mapping: Mapping[OfficeOfExit] = Forms.mapping(
    officeId -> text()
      .verifying("declaration.officeOfExit.empty", nonEmpty)
      .verifying("declaration.officeOfExit.length", isEmpty or hasSpecificLength(8))
      .verifying("declaration.officeOfExit.specialCharacters", isEmpty or isAlphanumeric)
  )(OfficeOfExit.apply)(OfficeOfExit.unapply)

  def form: Form[OfficeOfExit] = Form(OfficeOfExit.mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.officeOfExit.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
