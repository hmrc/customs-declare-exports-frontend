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
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.Messages
import play.api.libs.json.Json
import services.{DiffTools, DocumentTypeService}
import services.DiffTools.{combinePointers, compareStringDifference, ExportsDeclarationDiff}
import utils.validators.forms.FieldValidator._

case class Document(documentType: String, documentReference: String, goodsItemIdentifier: Option[String]) extends DiffTools[Document] {
  def createDiff(original: Document, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareStringDifference(original.documentType, documentType, combinePointers(pointerString, Document.documentTypePointer, sequenceId)),
      compareStringDifference(
        original.documentReference,
        documentReference,
        combinePointers(pointerString, Document.documentReferencePointer, sequenceId)
      ),
      compareStringDifference(
        original.goodsItemIdentifier,
        goodsItemIdentifier,
        combinePointers(pointerString, Document.goodsItemIdentifierPointer, sequenceId)
      )
    ).flatten
}

object Document extends DeclarationPage with FieldMapping {
  implicit val format = Json.format[Document]

  val pointer: ExportsFieldPointer = "documents"
  val documentTypePointer: ExportsFieldPointer = "documentType"
  val documentReferencePointer: ExportsFieldPointer = "documentReference"
  val goodsItemIdentifierPointer: ExportsFieldPointer = "goodsItemIdentifier"

  val formId = "PreviousDocuments"

  def form(docService: DocumentTypeService)(implicit messages: Messages): Form[Document] = {
    val mapping = Forms.mapping(documentTypeMapping(docService), documentReferenceMapping, goodsIdentifierMapping)(Document.apply)(Document.unapply)

    Form(mapping)
  }

  private def documentTypeMapping(docService: DocumentTypeService)(implicit messages: Messages): (String, Mapping[String]) =
    "documentType" -> text()
      .verifying("declaration.previousDocuments.documentCode.empty", nonEmpty)
      .verifying("declaration.previousDocuments.documentCode.error", isEmpty or isContainedIn(docService.allDocuments().map(_.code)))

  private def documentReferenceMapping: (String, Mapping[String]) =
    "documentReference" -> text()
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

case class PreviousDocumentsData(documents: Seq[Document]) extends DiffTools[PreviousDocumentsData] {
  def createDiff(original: PreviousDocumentsData, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    createDiff(original.documents, documents, combinePointers(pointerString, PreviousDocumentsData.pointer, sequenceId))
}

object PreviousDocumentsData extends FieldMapping {
  implicit val format = Json.format[PreviousDocumentsData]

  val pointer: ExportsFieldPointer = "previousDocuments"

  val maxAmountOfItems = 99

  val isScreenMandatory = false
}

object DocumentChangeOrRemove extends DeclarationPage {
  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.previousDocuments.remove.clearance"))
}

object DocumentSummary extends DeclarationPage
