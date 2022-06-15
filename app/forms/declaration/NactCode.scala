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

package forms.declaration
import forms.DeclarationPage
import forms.MappingHelper.requiredRadio
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class NactCode(nactCode: String)

object NactCode extends DeclarationPage {

  implicit val format = Json.format[NactCode]

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

  def form(): Form[NactCode] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.item.nationalAdditionalCode.common"))
}

object ZeroRatedForVat extends DeclarationPage {

  implicit val format = Json.format[NactCode]

  val VatZeroRatedYes = "VATZ"
  val VatZeroRatedReduced = "VATR"
  val VatZeroRatedExempt = "VATE"
  val VatZeroRatedPaid = "VAT_NO"

  val allowedValues = Seq(VatZeroRatedYes, VatZeroRatedReduced, VatZeroRatedExempt, VatZeroRatedPaid)

  val mapping: Mapping[NactCode] = Forms.mapping(
    NactCode.nactCodeKey -> requiredRadio("declaration.zeroRatedForVat.empty")
      .verifying("declaration.zeroRatedForVat.error", isContainedIn(allowedValues))
  )(NactCode.apply)(NactCode.unapply)

  def form(): Form[NactCode] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.item.zeroRatedForVat.common"))
}
