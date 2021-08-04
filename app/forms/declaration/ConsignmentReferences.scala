/*
 * Copyright 2021 HM Revenue & Customs
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

import forms.{DeclarationPage, Ducr, Lrn, Mrn}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import models.viewmodels.TariffContentKey
import models.DeclarationType.{CLEARANCE, DeclarationType, SUPPLEMENTARY}
import play.api.data.{Form, Forms}
import play.api.data.Forms.{optional, text}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.{isMissing, isPresent}

case class ConsignmentReferences(ducr: Ducr, lrn: Lrn, mrn: Option[Mrn] = None)

object ConsignmentReferences extends DeclarationPage {

  implicit val format = Json.format[ConsignmentReferences]

  def form(decType: DeclarationType, additionalDecType: Option[AdditionalDeclarationType]): Form[ConsignmentReferences] = {

    def form2Model: (Ducr, Lrn, Option[Mrn]) => ConsignmentReferences = {
      case (ducr, lrn, mrn) =>
        mrn match {
          case Some(x) => ConsignmentReferences(ducr, lrn, mrn)
          case None    => ConsignmentReferences(ducr, lrn, None)
        }
    }

    def model2Form: ConsignmentReferences => Option[(Ducr, Lrn, Option[Mrn])] =
      model =>
        model.mrn match {
          case Some(code) => Some((model.ducr, model.lrn, Some(code)))
          case None       => Some((model.ducr, model.lrn, None))
      }

    val mrnMapping = (decType, additionalDecType) match {
      case (SUPPLEMENTARY, Some(AdditionalDeclarationType.SUPPLEMENTARY_SIMPLIFIED)) =>
        optional(Mrn.mapping("declaration.consignmentReferences.supplementary.mrn"))
          .verifying("declaration.consignmentReferences.supplementary.mrn.error.empty", isPresent(_))
      case _ =>
        optional(text())
          .verifying("error.notRequired", isMissing(_))
          .transform(_.map(Mrn(_)), (o: Option[Mrn]) => o.map(_.value))
    }

    Form(
      Forms.mapping("ducr" -> Ducr.ducrMapping, "lrn" -> Lrn.mapping("declaration.consignmentReferences.lrn"), "mrn" -> mrnMapping)(form2Model)(
        model2Form
      )
    )
  }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    decType match {
      case CLEARANCE =>
        Seq(
          TariffContentKey("tariff.declaration.consignmentReferences.1.clearance"),
          TariffContentKey("tariff.declaration.consignmentReferences.2.clearance"),
          TariffContentKey("tariff.declaration.consignmentReferences.3.clearance")
        )
      case _ =>
        Seq(
          TariffContentKey("tariff.declaration.consignmentReferences.1.common"),
          TariffContentKey("tariff.declaration.consignmentReferences.2.common"),
          TariffContentKey("tariff.declaration.consignmentReferences.3.common")
        )
    }
}
