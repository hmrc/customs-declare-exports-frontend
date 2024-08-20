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

package forms.section4

import forms.DeclarationPage
import models.DeclarationType.DeclarationType
import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.{ImplicitlySequencedObject, IsoData}
import models.viewmodels.TariffContentKey
import models.{AmendmentOp, FieldMapping}
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.DiffTools.{combinePointers, compareStringDifference, ExportsDeclarationDiff}
import services.{DiffTools, DocumentTypeService}
import utils.validators.forms.FieldValidator._

case class Document(documentType: String, documentReference: String, goodsItemIdentifier: Option[String])
    extends DiffTools[Document] with ImplicitlySequencedObject with AmendmentOp {

  import forms.section4.Document.{documentReferencePointer, documentTypePointer, goodsItemIdentifierPointer}

  def createDiff(original: Document, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareStringDifference(original.documentType, documentType, combinePointers(pointerString, documentTypePointer, sequenceId)),
      compareStringDifference(original.documentReference, documentReference, combinePointers(pointerString, documentReferencePointer, sequenceId)),
      compareStringDifference(
        original.goodsItemIdentifier,
        goodsItemIdentifier,
        combinePointers(pointerString, goodsItemIdentifierPointer, sequenceId)
      )
    ).flatten

  def getLeafPointersIfAny(pointer: ExportsFieldPointer): Seq[ExportsFieldPointer] =
    Seq(pointer)
}

object Document extends DeclarationPage with FieldMapping {
  implicit val format: OFormat[Document] = Json.format[Document]

  val pointer: ExportsFieldPointer = "documents"
  val documentTypePointer: ExportsFieldPointer = "documentType"
  val documentReferencePointer: ExportsFieldPointer = "documentReference"
  val goodsItemIdentifierPointer: ExportsFieldPointer = "goodsItemIdentifier"

  lazy val keyForItemNumber = "declaration.summary.transaction.previousDocuments.goodsItemIdentifier"
  lazy val keyForReference = "declaration.summary.transaction.previousDocuments.reference"
  lazy val keyForType = "declaration.summary.transaction.previousDocuments.type"

  val documentTypeId = "documentType"
  val documentRefId = "documentReference"

  val formId = "PreviousDocuments"

  def form(docService: DocumentTypeService)(implicit messages: Messages): Form[Document] = {
    val mapping = Forms.mapping(documentTypeMapping(docService), documentReferenceMapping, goodsIdentifierMapping)(Document.apply)(Document.unapply)

    Form(mapping)
  }

  private def documentTypeMapping(docService: DocumentTypeService)(implicit messages: Messages): (String, Mapping[String]) =
    documentTypeId -> text()
      .verifying("declaration.previousDocuments.documentCode.empty", nonEmpty)
      .verifying("declaration.previousDocuments.documentCode.error", isEmpty or isContainedIn(docService.allDocuments().map(_.code)))

  private def documentReferenceMapping: (String, Mapping[String]) =
    documentRefId -> text()
      .transform(_.trim, (s: String) => s)
      .verifying("declaration.previousDocuments.documentReference.empty", nonEmpty)
      .verifying("declaration.previousDocuments.documentReference.error", isEmpty or isAlphanumericWithSpecialCharacters(Set(' ', '-', '/', ':')))
      .verifying("declaration.previousDocuments.documentReference.error.spaces", isEmpty or notContainsConsecutiveSpaces)
      .verifying("declaration.previousDocuments.documentReference.error.length", isEmpty or noLongerThan(35))

  private def goodsIdentifierMapping: (String, Mapping[Option[String]]) =
    "goodsItemIdentifier" ->
      optional(text.verifying("declaration.previousDocuments.goodsItemIdentifier.error", isNumeric and noLongerThan(3)))

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.addPreviousDocument.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}

case class PreviousDocumentsData(documents: Seq[Document]) extends DiffTools[PreviousDocumentsData] with IsoData[Document] {

  override val subPointer: ExportsFieldPointer = Document.pointer
  override val elements: Seq[Document] = documents

  def createDiff(original: PreviousDocumentsData, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    createDiff(original.documents, documents, combinePointers(pointerString, subPointer, sequenceId))
}

object PreviousDocumentsData extends FieldMapping {
  implicit val format: OFormat[PreviousDocumentsData] = Json.format[PreviousDocumentsData]

  val pointer: ExportsFieldPointer = "previousDocuments"

  val maxAmountOfItems = 99
}

object DocumentChangeOrRemove extends DeclarationPage {
  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.previousDocuments.remove.clearance"))
}

object DocumentSummary extends DeclarationPage
