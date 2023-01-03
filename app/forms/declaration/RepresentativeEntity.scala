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
import forms.common.Eori
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.{Form, Forms}
import play.api.libs.json.Json

case class RepresentativeEntity(details: EntityDetails)

object RepresentativeEntity extends DeclarationPage {
  implicit val format = Json.format[RepresentativeEntity]

  val formId = "RepresentativeEntityDetails"

  private val eoriOnlyMapping =
    Forms.mapping("eori" -> Eori.mapping("declaration.representative.entity.eori.empty"))(eori => EntityDetails(Some(eori), None))(model =>
      model.eori
    )

  val mapping = Forms
    .mapping("details" -> eoriOnlyMapping)(RepresentativeEntity.apply)(RepresentativeEntity.unapply)

  def form: Form[RepresentativeEntity] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.representativesEoriNumber.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
