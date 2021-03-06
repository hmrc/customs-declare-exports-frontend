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

import forms.{DeclarationPage, Ducr, Lrn}
import models.viewmodels.TariffContentKey
import models.DeclarationType.{CLEARANCE, DeclarationType}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json

case class ConsignmentReferences(ducr: Ducr, lrn: Lrn)

object ConsignmentReferences extends DeclarationPage {

  val mapping =
    Forms.mapping("ducr" -> Ducr.ducrMapping, "lrn" -> Lrn.mapping("declaration.consignmentReferences.lrn"))(ConsignmentReferences.apply)(
      ConsignmentReferences.unapply
    )

  implicit val format = Json.format[ConsignmentReferences]

  def form(): Form[ConsignmentReferences] = Form(mapping)

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
