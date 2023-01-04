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
import forms.MappingHelper.requiredRadio
import forms.common.YesNoAnswer.YesNoAnswers
import models.viewmodels.TariffContentKey
import models.DeclarationType.DeclarationType
import play.api.data.{Form, Forms}
import play.api.data.Forms.text
import play.api.libs.json.Json
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator._

case class WarehouseIdentification(identificationNumber: Option[String] = None)

object WarehouseIdentification extends DeclarationPage {
  implicit val format = Json.format[WarehouseIdentification]

  val formId = "WarehouseIdentification"

  val inWarehouseKey = "inWarehouse"
  val warehouseIdKey = "identificationNumber"

  private def form2ModelYesNo: (String, Option[String]) => WarehouseIdentification = { case (inWarehouse, warehouseId) =>
    inWarehouse match {
      case YesNoAnswers.yes => WarehouseIdentification(warehouseId.map(_.toUpperCase))
      case YesNoAnswers.no  => WarehouseIdentification(None)
    }
  }

  private def model2FormYesNo: WarehouseIdentification => Option[(String, Option[String])] =
    model =>
      model.identificationNumber match {
        case Some(id) => Some((YesNoAnswers.yes, Some(id)))
        case None     => Some((YesNoAnswers.no, None))
      }

  private def form2Model: (String) => WarehouseIdentification = id => WarehouseIdentification(Some(id.toUpperCase))

  private def model2Form: WarehouseIdentification => Option[String] = model => model.identificationNumber

  val validWarehouseTypes = Set('R', 'S', 'T', 'U', 'Y', 'Z')

  private val mappingYesNo = Forms
    .mapping(
      inWarehouseKey -> requiredRadio("declaration.warehouse.identification.answer.error"),
      warehouseIdKey ->
        mandatoryIfEqual(
          inWarehouseKey,
          YesNoAnswers.yes,
          text().verifying(
            "declaration.warehouse.identification.identificationNumber.error",
            startsWithIgnoreCase(validWarehouseTypes) and noShorterThan(2) and noLongerThan(36) and isAlphanumeric
          )
        )
    )(form2ModelYesNo)(model2FormYesNo)

  private val mapping = Forms
    .mapping(
      warehouseIdKey ->
        text()
          .verifying("declaration.warehouse.identification.identificationNumber.empty", nonEmpty)
          .verifying("declaration.warehouse.identification.identificationNumber.length", noLongerThan(36))
          .verifying(
            "declaration.warehouse.identification.identificationNumber.format",
            isEmpty or (
              startsWithIgnoreCase(validWarehouseTypes) and noShorterThan(2) and noLongerThan(36)
            )
          )
          .verifying("declaration.warehouse.identification.identificationNumber.invalid", isAlphanumeric)
    )(form2Model)(model2Form)

  def form(yesNo: Boolean): Form[WarehouseIdentification] = Form(if (yesNo) mappingYesNo else mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.warehouseIdentification.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
