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

import forms.DeclarationPage
import forms.Mapping.requiredRadio
import models.declaration.DocumentCategory
import models.declaration.DocumentCategory.{RelatedDocument, SimplifiedDeclaration}
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, FormError, Forms}
import play.api.libs.json.{JsValue, Json}
import services.DocumentType
import utils.validators.forms.FieldValidator._

case class Document(documentType: String, documentReference: String, documentCategory: DocumentCategory, goodsItemIdentifier: Option[String]) {
  def toJson: JsValue = Json.toJson(this)(Document.format)
}

object Document extends DeclarationPage {
  def fromJsonString(value: String): Option[Document] = Json.fromJson(Json.parse(value)).asOpt

  implicit val format = Json.format[Document]

  val formId = "PreviousDocuments"

  val correctDocumentCategories = Set(SimplifiedDeclaration.value, RelatedDocument.value)

  val mapping =
    Forms.mapping(documentTypeMapping, documentReferenceMapping, documentCategoryMapping, goodsIdentifierMapping)(Document.apply)(Document.unapply)

  def form(): Form[Document] = Form(mapping)

  def treatLikeOptional(document: Form[Document]): Form[Document] = {
    val errorsToIgnore = Seq(
      FormError("documentType", "declaration.previousDocuments.documentType.empty"),
      FormError("documentReference", "declaration.previousDocuments.documentReference.empty"),
      FormError("documentCategory", "declaration.previousDocuments.documentCategory.error.empty")
    )

    if (document.errors == errorsToIgnore && document.data.get("goodsItemIdentifier").getOrElse("").isEmpty)
      document.copy(errors = Seq.empty)
    else document
  }

  private def goodsIdentifierMapping =
    "goodsItemIdentifier" -> optional(text().verifying("declaration.previousDocuments.goodsItemIdentifier.error", isNumeric and noLongerThan(3)))

  private def documentCategoryMapping =
    "documentCategory" -> requiredRadio("declaration.previousDocuments.documentCategory.error.empty")
      .verifying("declaration.previousDocuments.documentCategory.error.empty", nonEmpty)
      .verifying("declaration.previousDocuments.documentCategory.error.incorrect", isEmpty or isContainedIn(correctDocumentCategories))
      .transform[DocumentCategory]({
        case SimplifiedDeclaration.value => SimplifiedDeclaration
        case RelatedDocument.value       => RelatedDocument
      }, category => category.value)

  private def documentReferenceMapping =
    "documentReference" -> text()
      .verifying("declaration.previousDocuments.documentReference.empty", nonEmpty)
      .verifying(
        "declaration.previousDocuments.documentReference.error",
        isEmpty or (isAlphanumericWithSpecialCharacters(Set('-', '/', ':')) and noLongerThan(35))
      )

  private def documentTypeMapping =
    "documentType" -> text()
      .verifying("declaration.previousDocuments.documentType.empty", nonEmpty)
      .verifying("declaration.previousDocuments.documentType.error", isEmpty or isContainedIn(DocumentType.allDocuments.map(_.code)))
}

case class PreviousDocumentsData(documents: Seq[Document])

object PreviousDocumentsData {
  implicit val format = Json.format[PreviousDocumentsData]

  val maxAmountOfItems = 99

  val isScreenMandatory = false
}

object DocumentChangeOrRemove extends DeclarationPage

object DocumentSummary extends DeclarationPage
