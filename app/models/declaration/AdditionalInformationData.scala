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
import forms.common.YesNoAnswer.{No, Yes}
import forms.declaration.AdditionalInformation
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import play.api.libs.json.Json
import services.DiffTools
import services.DiffTools.{combinePointers, ExportsDeclarationDiff}

case class AdditionalInformationData(isRequired: Option[YesNoAnswer], items: Seq[AdditionalInformation])
    extends DiffTools[AdditionalInformationData] with IsoData[AdditionalInformation] {

  // isRequired field is not used to produce the WCO XML payload
  def createDiff(original: AdditionalInformationData, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    createDiff(original.items, items, combinePointers(pointerString, AdditionalInformation.pointer, sequenceId))

  override def createDiffWithEmpty(originalIsEmpty: Boolean, pointerString: ExportsFieldPointer): ExportsDeclarationDiff =
    if (originalIsEmpty)
      createDiff(Seq.empty, items, combinePointers(pointerString, AdditionalInformation.pointer))
    else createDiff(items, Seq.empty, combinePointers(pointerString, AdditionalInformation.pointer))
}

object AdditionalInformationData extends FieldMapping {
  implicit val format = Json.format[AdditionalInformationData]

  def apply(items: Seq[AdditionalInformation]): AdditionalInformationData =
    new AdditionalInformationData(if (items.nonEmpty) Yes else No, items)

  def default: AdditionalInformationData = AdditionalInformationData(None, Seq.empty)

  val formId = "AdditionalInformationData"

  val maxNumberOfItems = 99

  val pointer: ExportsFieldPointer = "additionalInformation"
}
