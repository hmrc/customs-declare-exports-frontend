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
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, FormError, Forms, Mapping}
import play.api.libs.json.Json
import services.DocumentType
import utils.validators.forms.FieldValidator._

case class Document(documentType: String, documentReference: String, goodsItemIdentifier: Option[String])

object Document extends DeclarationPage {

  implicit val format = Json.format[Document]

  val formId = "PreviousDocuments"

  val mapping = Forms.mapping(documentTypeMapping, documentReferenceMapping, goodsIdentifierMapping)(Document.apply)(Document.unapply)

  def form: Form[Document] = Form(mapping)

  def treatLikeOptional(document: Form[Document]): Form[Document] = {
    val errorsToIgnore = Seq(
      FormError("documentType", "declaration.previousDocuments.documentType.empty"),
      FormError("documentReference", "declaration.previousDocuments.documentReference.empty")
    )

    if (document.errors == errorsToIgnore && document.data.get("goodsItemIdentifier").getOrElse("").isEmpty)
      document.copy(errors = Seq.empty)
    else document
  }

  private def documentTypeMapping: (String, Mapping[String]) =
    "documentType" -> text()
      .verifying("declaration.previousDocuments.documentType.empty", nonEmpty)
      .verifying("declaration.previousDocuments.documentType.error", isEmpty or isContainedIn(DocumentType.allDocuments.map(_.code)))

  private def documentReferenceMapping: (String, Mapping[String]) =
    "documentReference" -> text()
      .verifying("declaration.previousDocuments.documentReference.empty", nonEmpty)
      .verifying(
        "declaration.previousDocuments.documentReference.error",
        isEmpty or (isAlphanumericWithSpecialCharacters(Set('-', '/', ':')) and noLongerThan(35))
      )

  private def goodsIdentifierMapping: (String, Mapping[Option[String]]) =
    "goodsItemIdentifier" -> optional(text.verifying("declaration.previousDocuments.goodsItemIdentifier.error", isNumeric and noLongerThan(3)))

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.addPreviousDocument.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}

case class PreviousDocumentsData(documents: Seq[Document])

object PreviousDocumentsData {
  implicit val format = Json.format[PreviousDocumentsData]

  val maxAmountOfItems = 99

  val isScreenMandatory = false
}

object DocumentChangeOrRemove extends DeclarationPage {
  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.previousDocuments.remove.clearance"))
}

object DocumentSummary extends DeclarationPage
