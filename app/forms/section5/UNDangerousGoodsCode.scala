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

package forms.section5

import forms.DeclarationPage
import forms.mappings.MappingHelper.requiredRadio
import forms.section5.UNDangerousGoodsCode.keyForAmend
import models.AmendmentRow.{forAddedValue, forAmendedValue, forRemovedValue}
import models.DeclarationType.DeclarationType
import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.ExportItem.itemsPrefix
import models.viewmodels.TariffContentKey
import models.{Amendment, FieldMapping}
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.DiffTools.compareOptionalString
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator._

case class UNDangerousGoodsCode(dangerousGoodsCode: Option[String]) extends Ordered[UNDangerousGoodsCode] with Amendment {

  override def compare(that: UNDangerousGoodsCode): Int =
    compareOptionalString(dangerousGoodsCode, that.dangerousGoodsCode)

  def value: String = dangerousGoodsCode.getOrElse("")

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    dangerousGoodsCode.fold("")(forAddedValue(pointer, messages(keyForAmend), _))

  def valueAmended(newValue: Amendment, pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    forAmendedValue(pointer, messages(keyForAmend), value, newValue.value)

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    dangerousGoodsCode.fold("")(forRemovedValue(pointer, messages(keyForAmend), _))
}

object UNDangerousGoodsCode extends DeclarationPage with FieldMapping {
  implicit val format: OFormat[UNDangerousGoodsCode] = Json.format[UNDangerousGoodsCode]

  val pointer: ExportsFieldPointer = "dangerousGoodsCode.dangerousGoodsCode"

  private lazy val keyForAmend = s"$itemsPrefix.unDangerousGoodsCode"

  val hasDangerousGoodsCodeKey = "hasDangerousGoodsCode"
  val dangerousGoodsCodeKey = "dangerousGoodsCode"
  private val unDangerousGoodsCodeLength = 4

  object AllowedUNDangerousGoodsCodeAnswers {
    val yes = "Yes"
    val no = "No"
  }

  import AllowedUNDangerousGoodsCodeAnswers._

  private def form2Model: (String, Option[String]) => UNDangerousGoodsCode = { case (hasCode, codeValue) =>
    hasCode match {
      case AllowedUNDangerousGoodsCodeAnswers.yes => UNDangerousGoodsCode(codeValue)
      case AllowedUNDangerousGoodsCodeAnswers.no  => UNDangerousGoodsCode(None)
    }
  }

  private def model2Form: UNDangerousGoodsCode => Option[(String, Option[String])] =
    model =>
      model.dangerousGoodsCode match {
        case Some(code) => Some((yes, Some(code)))
        case None       => Some((no, None))
      }

  val mapping =
    Forms.mapping(
      hasDangerousGoodsCodeKey -> requiredRadio("declaration.unDangerousGoodsCode.answer.empty"),
      dangerousGoodsCodeKey -> mandatoryIfEqual(
        hasDangerousGoodsCodeKey,
        yes,
        text()
          .verifying("declaration.unDangerousGoodsCode.error.empty", nonEmpty)
          .verifying("declaration.unDangerousGoodsCode.error.invalid", isEmpty or (hasSpecificLength(unDangerousGoodsCodeLength) and isNumeric))
      )
    )(form2Model)(model2Form)

  def form: Form[UNDangerousGoodsCode] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.item.unDangerousGoodsCode.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
