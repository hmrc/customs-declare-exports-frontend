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

package forms.declaration.additionaldocuments

import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.{PredicateOpsForFunctions, isAlphanumeric, noLongerThan}

case class DocumentIdentifierAndPart(documentIdentifier: Option[String], documentPart: Option[String])

object DocumentIdentifierAndPart {

  implicit val format = Json.format[DocumentIdentifierAndPart]

  val documentIdentifierKey = "documentIdentifier"
  val documentPartKey = "documentPart"

  val mapping = Forms
    .mapping(
      documentIdentifierKey -> optional(
        text().verifying("supplementary.addDocument.documentIdentifier.error", isAlphanumeric and noLongerThan(30))
      ),
      documentPartKey -> optional(
        text().verifying("supplementary.addDocument.documentPart.error", isAlphanumeric and noLongerThan(5))
      )
    )(DocumentIdentifierAndPart.apply)(DocumentIdentifierAndPart.unapply)
    .verifying("supplementary.addDocument.error.documentIdentifierAndPart", validateDocumentIdentifierAndPart(_))

  private def validateDocumentIdentifierAndPart(doc: DocumentIdentifierAndPart): Boolean =
    (doc.documentIdentifier.isEmpty && doc.documentPart.isEmpty) || (doc.documentIdentifier.nonEmpty && doc.documentPart.nonEmpty)

  def form(): Form[DocumentIdentifierAndPart] = Form(mapping)
}
