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

import connectors.CodeListConnector
import forms.common.{Address, Eori}
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import play.api.data.{Forms, Mapping}
import play.api.data.Forms.optional
import play.api.i18n.Messages
import play.api.libs.json.Json
import services.DiffTools
import services.DiffTools.{combinePointers, compareDifference, ExportsDeclarationDiff}

case class EntityDetails(
  eori: Option[Eori], // alphanumeric, max length 17 characters
  address: Option[Address]
) extends DiffTools[EntityDetails] {
  override def createDiff(original: EntityDetails, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareDifference(original.eori, eori, combinePointers(pointerString, EntityDetails.eoriPointer, sequenceId)),
      createDiffOfOptions(original.address, address, combinePointers(pointerString, Address.pointer, sequenceId))
    ).flatten
}

object EntityDetails extends FieldMapping {
  implicit val format = Json.format[EntityDetails]

  val pointer: ExportsFieldPointer = "details"
  val eoriPointer: ExportsFieldPointer = "eori"

  def addressMapping(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[EntityDetails] =
    Forms
      .mapping("address" -> Address.mapping)(address => EntityDetails(None, Some(address)))(entityDetails => entityDetails.address)

  def optionalAddressMapping(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[EntityDetails] =
    Forms
      .mapping("address" -> optional(Address.mapping))(maybeAddress => EntityDetails(None, maybeAddress))(entityDetails =>
        Some(entityDetails.address)
      )
}
