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

import connectors.CodeListConnector
import forms.DeclarationPage
import models.DeclarationType.DeclarationType
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import models.declaration.{ImplicitlySequencedObject, IsoData}
import models.viewmodels.TariffContentKey
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.i18n.Messages
import play.api.libs.json.Json
import services.view.Countries._
import services.DiffTools
import services.DiffTools.{combinePointers, compareStringDifference, ExportsDeclarationDiff}
import utils.validators.forms.FieldValidator._

case class AdditionalFiscalReference(country: String, reference: String) extends DiffTools[AdditionalFiscalReference] with ImplicitlySequencedObject {
  override def createDiff(
    original: AdditionalFiscalReference,
    pointerString: ExportsFieldPointer,
    sequenceId: Option[Int] = None
  ): ExportsDeclarationDiff =
    Seq(
      compareStringDifference(original.country, country, combinePointers(pointerString, sequenceId)),
      compareStringDifference(original.reference, reference, combinePointers(pointerString, sequenceId))
    ).flatten

  val asString: String = country + reference
}

object AdditionalFiscalReference extends DeclarationPage with FieldMapping {

  val pointer: ExportsFieldPointer = "references"

  def build(country: String, reference: String): AdditionalFiscalReference = new AdditionalFiscalReference(country, reference.toUpperCase)
  implicit val format = Json.format[AdditionalFiscalReference]

  def mapping(implicit messages: Messages, codeListConnector: CodeListConnector) =
    Forms.mapping(
      "country" -> text()
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
    copy(references = references.filterNot(reference => patterns.contains(reference.asString)))
  }

  def removeReference(value: String): AdditionalFiscalReferencesData =
    removeReferences(Seq(value))
}

object AdditionalFiscalReferencesData extends FieldMapping {
  val pointer: ExportsFieldPointer = "additionalFiscalReferencesData"

  implicit val format = Json.format[AdditionalFiscalReferencesData]

  def apply(references: Seq[AdditionalFiscalReference]): AdditionalFiscalReferencesData =
    new AdditionalFiscalReferencesData(references)

  def default: AdditionalFiscalReferencesData = AdditionalFiscalReferencesData(Seq.empty)

  val formId = "AdditionalFiscalReferences"

  val limit = 99
}

object AdditionalFiscalReferencesSummary extends DeclarationPage
