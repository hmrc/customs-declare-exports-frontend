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

package forms.section3

import forms.DeclarationPage
import models.AmendmentRow.{forAddedValue, forAmendedValue, forRemovedValue}
import models.DeclarationType.DeclarationType
import models.ExportsFieldPointer.ExportsFieldPointer
import models.viewmodels.TariffContentKey
import models.{Amendment, FieldMapping}
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator.{hasSpecificLength, nonEmpty, _}

case class OfficeOfExit(officeId: String) extends Ordered[OfficeOfExit] with Amendment {

  def value: String = officeId

  import forms.section3.OfficeOfExit.keyForAmend

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    forAddedValue(pointer, messages(keyForAmend), officeId)

  def valueAmended(newValue: Amendment, pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    forAmendedValue(pointer, messages(keyForAmend), officeId, newValue.value)

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    forRemovedValue(pointer, messages(keyForAmend), officeId)

  override def compare(that: OfficeOfExit): Int =
    officeId.compareTo(that.officeId)
}

object OfficeOfExit extends DeclarationPage with FieldMapping {

  implicit val format: OFormat[OfficeOfExit] = Json.format[OfficeOfExit]

  val pointerBase: String = "officeOfExit"
  val pointer: ExportsFieldPointer = s"$pointerBase.officeId"

  private val keyForAmend = "declaration.summary.locations.officeOfExit"

  def form: Form[OfficeOfExit] = Form(OfficeOfExit.mapping)

  val fieldId = "officeId"

  val formId = "OfficeOfExit"

  private val mapping: Mapping[OfficeOfExit] = Forms.mapping(
    fieldId -> text()
      .verifying("declaration.officeOfExit.empty", nonEmpty)
      .verifying("declaration.officeOfExit.length", isEmpty or hasSpecificLength(8))
      .verifying("declaration.officeOfExit.specialCharacters", isEmpty or isAlphanumeric)
  )(OfficeOfExit.apply)(OfficeOfExit.unapply)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.officeOfExit.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
