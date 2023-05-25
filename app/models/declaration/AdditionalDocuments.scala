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

package models.declaration

import forms.common.YesNoAnswer
import forms.declaration.additionaldocuments.AdditionalDocument
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import play.api.libs.json.Json
import services.DiffTools
import services.DiffTools.{combinePointers, ExportsDeclarationDiff}

case class AdditionalDocuments(isRequired: Option[YesNoAnswer], documents: Seq[AdditionalDocument])
    extends DiffTools[AdditionalDocuments] with IsoData[AdditionalDocument] {
  // isRequired field is not used to produce the WCO XML payload
  override val subPointer: ExportsFieldPointer = AdditionalDocument.pointer
  override val elements: Seq[AdditionalDocument] = documents

  def createDiff(original: AdditionalDocuments, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    createDiff(original.documents, documents, combinePointers(pointerString, subPointer, sequenceId))
}

object AdditionalDocuments extends FieldMapping {
  implicit val format = Json.format[AdditionalDocuments]

  val maxNumberOfItems = 99

  val pointer: ExportsFieldPointer = "additionalDocuments"
}
