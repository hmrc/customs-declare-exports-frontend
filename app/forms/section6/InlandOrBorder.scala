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

package forms.section6

import forms.DeclarationPage
import forms.mappings.MappingHelper.requiredRadio
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}

case class InlandOrBorder(location: String)

object InlandOrBorder extends DeclarationPage {

  implicit val format: OFormat[InlandOrBorder] = Json.format[InlandOrBorder]

  val Border = new InlandOrBorder("Border")
  val Inland = new InlandOrBorder("Inland")

  def apply(location: String): InlandOrBorder = if (location == "Border") Border else Inland

  val fieldId = "location"

  val mapping: Mapping[InlandOrBorder] =
    Forms
      .mapping(fieldId -> requiredRadio("declaration.inlandOrBorder.answer.empty"))(apply)(InlandOrBorder.unapply)

  def form: Form[InlandOrBorder] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.inlandOrBorder.common"))
}
