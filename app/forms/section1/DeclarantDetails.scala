/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.section1

import forms.DeclarationPage
import forms.section2.EntityDetails
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, ExportsDeclarationDiff}

case class DeclarantDetails(details: EntityDetails) extends DiffTools[DeclarantDetails] {
  override def createDiff(original: DeclarantDetails, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(details.createDiff(original.details, combinePointers(pointerString, sequenceId))).flatten
}

object DeclarantDetails extends DeclarationPage with FieldMapping {

  val pointer: ExportsFieldPointer = "declarantDetails"

  implicit val format: OFormat[DeclarantDetails] = Json.format[DeclarantDetails]
}
