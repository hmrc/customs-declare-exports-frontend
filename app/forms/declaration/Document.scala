/*
 * Copyright 2019 HM Revenue & Customs
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

import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class Document(
  documentCategory: String,
  documentType: String,
  documentReference: String,
  goodsItemIdentifier: Option[String]
)

object Document {
  implicit val format = Json.format[Document]

  val formId = "PreviousDocuments"

  val correctDocumentCategories = Set("X", "Y", "Z")

  val mapping = Forms.mapping(
    "documentCategory" -> optional(
      text()
        .verifying(
          "supplementary.previousDocuments.documentCategory.error.incorrect",
          isEmpty or isContainedIn(correctDocumentCategories)
        )
    ).verifying("supplementary.previousDocuments.documentCategory.error.empty", _.isDefined)
      .transform[String](optValue => optValue.getOrElse(""), docCategory => Some(docCategory)),
    "documentType" -> text()
      .verifying("supplementary.previousDocuments.documentType.empty", nonEmpty)
      .verifying("supplementary.previousDocuments.documentType.error", isEmpty or (isAlphanumeric and noLongerThan(3))),
    "documentReference" -> text()
      .verifying("supplementary.previousDocuments.documentReference.empty", nonEmpty)
      .verifying(
        "supplementary.previousDocuments.documentReference.error",
        isEmpty or (isAlphanumeric and noLongerThan(35))
      ),
    "goodsItemIdentifier" -> optional(
      text().verifying("supplementary.previousDocuments.goodsItemIdentifier.error", isNumeric and noLongerThan(3))
    )
  )(Document.apply)(Document.unapply)

  def form(): Form[Document] = Form(mapping)

  object AllowedValues {
    val TemporaryStorage = "X"
    val SimplifiedDeclaration = "Y"
    val PreviousDocument = "Z"
  }
}

case class PreviousDocumentsData(documents: Seq[Document])

object PreviousDocumentsData {
  implicit val format = Json.format[PreviousDocumentsData]

  val maxAmountOfItems = 99

  val isScreenMandatory = true
}
