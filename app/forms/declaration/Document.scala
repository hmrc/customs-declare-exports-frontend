/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.{JsValue, Json}
import services.DocumentType
import utils.validators.forms.FieldValidator._

case class Document(documentCategory: String, documentType: String, documentReference: String, goodsItemIdentifier: Option[String]) {
  def toJson: JsValue = Json.toJson(this)(Document.format)
}

object Document extends DeclarationPage {
  def fromJsonString(value: String): Option[Document] = Json.fromJson(Json.parse(value)).asOpt

  implicit val format = Json.format[Document]

  val formId = "PreviousDocuments"

  import AllowedValues._

  val correctDocumentCategories = Set(SimplifiedDeclaration, RelatedDocument)

  val mapping = Forms.mapping(
    "documentCategory" -> text()
      .verifying("declaration.previousDocuments.documentCategory.error.empty", nonEmpty)
      .verifying("declaration.previousDocuments.documentCategory.error.incorrect", isEmpty or isContainedIn(correctDocumentCategories)),
    "documentType" -> text()
      .verifying("declaration.previousDocuments.documentType.empty", nonEmpty)
      .verifying("declaration.previousDocuments.documentType.error", isEmpty or isContainedIn(DocumentType.allDocuments.map(_.code))),
    "documentReference" -> text()
      .verifying("declaration.previousDocuments.documentReference.empty", nonEmpty)
      .verifying(
        "declaration.previousDocuments.documentReference.error",
        isEmpty or (isAlphanumericWithSpecialCharacters(Set('-', '/', ':')) and noLongerThan(35))
      ),
    "goodsItemIdentifier" -> optional(text().verifying("declaration.previousDocuments.goodsItemIdentifier.error", isNumeric and noLongerThan(3)))
  )(Document.apply)(Document.unapply)

  def form(): Form[Document] = Form(mapping)

  object AllowedValues {
    val SimplifiedDeclaration = "Y"
    val RelatedDocument = "Z"
  }
}

case class PreviousDocumentsData(documents: Seq[Document])

object PreviousDocumentsData {
  implicit val format = Json.format[PreviousDocumentsData]

  val maxAmountOfItems = 99

  val isScreenMandatory = false
}

object DocumentChangeOrRemove extends DeclarationPage

object DocumentSummary extends DeclarationPage
