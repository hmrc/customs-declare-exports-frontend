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

package forms.section5

import forms.DeclarationPage
import forms.mappings.MappingHelper.requiredRadio
import models.DeclarationType.DeclarationType
import models.ExportsFieldPointer.ExportsFieldPointer
import models.viewmodels.TariffContentKey
import models.{Amendment, FieldMapping}
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator._

case class NactCode(nactCode: String) extends Ordered[NactCode] with Amendment {

  override def compare(y: NactCode): Int = nactCode.compareTo(y.nactCode)

  def value: String = nactCode
}

object NactCode extends DeclarationPage with FieldMapping {
  implicit val format: OFormat[NactCode] = Json.format[NactCode]

  val pointer: ExportsFieldPointer = "nactCode"
  val exemptionPointer: ExportsFieldPointer = "nactExemptionCode"

  def keyForAmend(pointer: ExportsFieldPointer): String =
    if (pointer.endsWith(exemptionPointer)) "declaration.summary.item.zeroRatedForVat"
    else "declaration.summary.item.nationalAdditionalCode"

  val nactCodeKey = "nactCode"

  val nactCodeLength = 4
  val nactCodeLimit = 99

  val mapping =
    Forms.mapping(
      nactCodeKey ->
        text()
          .verifying("declaration.nationalAdditionalCode.error.empty", nonEmpty)
          .verifying("declaration.nationalAdditionalCode.error.invalid", isEmpty or (hasSpecificLength(nactCodeLength) and isAlphanumeric))
    )(NactCode.apply)(NactCode.unapply)

  def form: Form[NactCode] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.item.nationalAdditionalCode.common"))
}

object ZeroRatedForVat extends DeclarationPage {

  implicit val format: OFormat[NactCode] = Json.format[NactCode]

  val VatZeroRatedYes = "VATZ"
  val VatZeroRatedReduced = "VATR"
  val VatZeroRatedExempt = "VATE"
  val VatZeroRatedPaid = "VAT_NO"
  val VatReportAfterDeclaration = "VAT_RAD"

  val allowedValues = Seq(VatZeroRatedYes, VatZeroRatedReduced, VatZeroRatedExempt, VatZeroRatedPaid, VatReportAfterDeclaration)

  val mapping: Mapping[NactCode] = Forms.mapping(
    NactCode.nactCodeKey -> requiredRadio("declaration.zeroRatedForVat.error")
      .verifying("declaration.zeroRatedForVat.error", isContainedIn(allowedValues))
  )(NactCode.apply)(NactCode.unapply)

  def form: Form[NactCode] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.item.zeroRatedForVat.common"))
}
