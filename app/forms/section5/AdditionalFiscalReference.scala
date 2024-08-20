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

import connectors.CodeListConnector
import forms.DeclarationPage
import models.DeclarationType.DeclarationType
import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.ExportItem.itemsPrefix
import models.declaration.{ImplicitlySequencedObject, IsoData}
import models.viewmodels.TariffContentKey
import models.{Amendment, FieldMapping}
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.Countries._
import services.DiffTools.{combinePointers, ExportsDeclarationDiff}
import services.{AlteredField, DiffTools, OriginalAndNewValues}
import utils.validators.forms.FieldValidator._

case class AdditionalFiscalReference(country: String, reference: String)
    extends DiffTools[AdditionalFiscalReference] with ImplicitlySequencedObject with Amendment {

  override def createDiff(
    original: AdditionalFiscalReference,
    pointerString: ExportsFieldPointer,
    sequenceId: Option[Int] = None
  ): ExportsDeclarationDiff =
    // special implementation to ensure AdditionalFiscalReference entity returned as value diff instead of Country and/or reference values
    Seq(
      Option.when(!country.compare(original.country).equals(0) || !reference.compare(original.reference).equals(0))(
        AlteredField(combinePointers(pointerString, sequenceId), OriginalAndNewValues(Some(original), Some(this)))
      )
    ).flatten

  def value: String = country + reference

  def getLeafPointersIfAny(pointer: ExportsFieldPointer): Seq[ExportsFieldPointer] =
    Seq(pointer)
}

object AdditionalFiscalReference extends DeclarationPage with FieldMapping {

  val pointer: ExportsFieldPointer = "references"

  lazy val keyForAmend = s"$itemsPrefix.VATdetails"

  def build(country: String, reference: String): AdditionalFiscalReference = new AdditionalFiscalReference(country, reference.toUpperCase)
  implicit val format: OFormat[AdditionalFiscalReference] = Json.format[AdditionalFiscalReference]

  val countryId = "country"

  def mapping(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[AdditionalFiscalReference] =
    Forms.mapping(
      countryId -> text()
        .verifying("declaration.additionalFiscalReferences.country.empty", _.trim.nonEmpty)
        .verifying("declaration.additionalFiscalReferences.country.error", input => input.isEmpty || isValidCountryCode(input)),
      "reference" -> text()
        .verifying("declaration.additionalFiscalReferences.reference.empty", _.trim.nonEmpty)
        .verifying("declaration.additionalFiscalReferences.reference.error", isEmpty or (isAlphanumeric and noLongerThan(15)))
    )(AdditionalFiscalReference.build)(AdditionalFiscalReference.unapply)

  def form(implicit messages: Messages, codeListConnector: CodeListConnector): Form[AdditionalFiscalReference] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.item.additionalFiscalReferences.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}

case class AdditionalFiscalReferencesData(references: Seq[AdditionalFiscalReference])
    extends DiffTools[AdditionalFiscalReferencesData] with IsoData[AdditionalFiscalReference] {

  override val subPointer: ExportsFieldPointer = AdditionalFiscalReference.pointer
  override val elements: Seq[AdditionalFiscalReference] = references

  override def createDiff(
    original: AdditionalFiscalReferencesData,
    pointerString: ExportsFieldPointer,
    sequenceId: Option[Int] = None
  ): ExportsDeclarationDiff =
    createDiff(original.references, references, combinePointers(pointerString, subPointer, sequenceId))

  def removeReferences(values: Seq[String]): AdditionalFiscalReferencesData = {
    val patterns = values.toSet
    copy(references = references.filterNot(reference => patterns.contains(reference.value)))
  }

  def removeReference(value: String): AdditionalFiscalReferencesData =
    removeReferences(Seq(value))
}

object AdditionalFiscalReferencesData extends FieldMapping {

  val pointer: ExportsFieldPointer = "additionalFiscalReferencesData"

  implicit val format: OFormat[AdditionalFiscalReferencesData] = Json.format[AdditionalFiscalReferencesData]

  def apply(references: Seq[AdditionalFiscalReference]): AdditionalFiscalReferencesData =
    new AdditionalFiscalReferencesData(references)

  def default: AdditionalFiscalReferencesData = AdditionalFiscalReferencesData(Seq.empty)

  val formId = "AdditionalFiscalReferences"

  val limit = 99
}

object AdditionalFiscalReferencesSummary extends DeclarationPage
