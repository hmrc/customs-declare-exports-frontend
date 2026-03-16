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
import forms.section5.AdditionalInformation.{codePointer, descriptionPointer}
import models.DeclarationType.DeclarationType
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import models.declaration.ExportItem.itemsPrefix
import models.declaration.ImplicitlySequencedObject
import models.viewmodels.TariffContentKey
import play.api.data.Forms._
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, compareStringDifference, ExportsDeclarationDiff}
import utils.validators.forms.FieldValidator._

case class AdditionalInformation(code: String, description: String) extends DiffTools[AdditionalInformation] with ImplicitlySequencedObject {

  def createDiff(original: AdditionalInformation, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareStringDifference(original.code, code, combinePointers(pointerString, codePointer, sequenceId)),
      compareStringDifference(original.description, description, combinePointers(pointerString, descriptionPointer, sequenceId))
    ).flatten
}

object AdditionalInformation extends DeclarationPage with FieldMapping {

  val pointer: ExportsFieldPointer = "items"
  val codePointer: ExportsFieldPointer = "code"
  val descriptionPointer: ExportsFieldPointer = "description"

  lazy val keyForCode = s"$itemsPrefix.additionalInformation.code"
  lazy val keyForDescription = s"$itemsPrefix.additionalInformation.description"

  implicit val format: OFormat[AdditionalInformation] = Json.format[AdditionalInformation]

  val codeForGVMS = "RRS01"

  val codeKey = "code"
  val descriptionKey = "description"
  val descriptionMaxLength = 70

  val mapping = Forms.mapping(
    codeKey ->
      text
        .verifying("declaration.additionalInformation.code.empty", nonEmpty)
        .verifying("declaration.additionalInformation.code.error", isEmpty or (isAlphanumeric and hasSpecificLength(5)))
        .verifying("declaration.additionalInformation.code.error.rrs01", code => isEmpty(code) or code != codeForGVMS)
        .verifying("declaration.additionalInformation.code.error.lic99", code => isEmpty(code) or code != "LIC99"),
    descriptionKey ->
      text
        .verifying("declaration.additionalInformation.description.empty", nonEmpty)
        .verifying(
          "declaration.additionalInformation.description.error",
          isEmpty or (noLongerThan(descriptionMaxLength) and isAlphanumericWithAllowedSpecialCharactersAndNewLine)
        )
  )(AdditionalInformation.apply)(AdditionalInformation.unapply)

  def form: Form[AdditionalInformation] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.item.additionalInformation.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}

object AdditionalInformationRequired extends DeclarationPage {
  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.item.isAdditionalInformationRequired.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}

object AdditionalInformationSummary extends DeclarationPage
