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
import models.viewmodels.TariffContentKey
import models.DeclarationType.DeclarationType
import play.api.data.{Form, Forms}
import play.api.data.Forms._
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class AdditionalInformation(code: String, description: String) {
  override def toString: String = s"${code}-${description}"
}

object AdditionalInformation extends DeclarationPage {
  implicit val format = Json.format[AdditionalInformation]

  val codeKey = "code"
  val descriptionKey = "description"
  val descriptionMaxLength = 70

  val mapping = Forms.mapping(
    codeKey ->
      text()
        .verifying("declaration.additionalInformation.code.empty", nonEmpty)
        .verifying("declaration.additionalInformation.code.error", isEmpty or (isAlphanumeric and hasSpecificLength(5))),
    descriptionKey ->
      text()
        .verifying("declaration.additionalInformation.description.empty", nonEmpty)
        .verifying(
          "declaration.additionalInformation.description.error",
          isEmpty or (noLongerThan(descriptionMaxLength) and isAlphanumericWithAllowedSpecialCharactersAndNewLine)
        )
  )(AdditionalInformation.apply)(AdditionalInformation.unapply)

  def form(): Form[AdditionalInformation] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.item.additionalInformation.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}

object AdditionalInformationRequired extends DeclarationPage {
  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.item.isAdditionalInformationRequired.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}

object AdditionalInformationSummary extends DeclarationPage
