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

package forms.section2

import connectors.CodeListConnector
import forms.common.{Address, Eori}
import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.Parties
import models.declaration.Parties.partiesPrefix
import models.{ExportsDeclaration, FieldMapping}
import play.api.data.Forms.optional
import play.api.data.{Forms, Mapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
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

  lazy val isEmpty: Boolean = eori.isEmpty && address.isEmpty
  lazy val nonEmpty: Boolean = !isEmpty
}

object EntityDetails extends FieldMapping {

  implicit val format: OFormat[EntityDetails] = Json.format[EntityDetails]

  val pointer: ExportsFieldPointer = "details"
  val eoriPointer: ExportsFieldPointer = "eori"

  private lazy val parties = s"${ExportsDeclaration.pointer}.${Parties.pointer}"

  lazy val mappingsForAmendment = Map(
    s"${parties}.carrierDetails" -> s"${partiesPrefix}.carrier.eori",
    s"${parties}.consigneeDetails" -> s"${partiesPrefix}.consignee.eori",
    s"${parties}.consignorDetails" -> s"${partiesPrefix}.consignor.eori",
    s"${parties}.declarantDetails" -> s"${partiesPrefix}.declarant.eori",
    s"${parties}.exporterDetails" -> s"${partiesPrefix}.exporter.eori",
    s"${parties}.representativeDetails" -> s"${partiesPrefix}.representative.eori"
  )

  def addressMapping(maxAddressLength: Int = 70)(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[EntityDetails] =
    Forms
      .mapping("address" -> Address.mapping(maxAddressLength))(address => EntityDetails(None, Some(address)))(entityDetails => entityDetails.address)

  def optionalAddressMapping(maxAddressLength: Int = 70)(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[EntityDetails] =
    Forms
      .mapping("address" -> optional(Address.mapping(maxAddressLength)))(maybeAddress => EntityDetails(None, maybeAddress))(entityDetails =>
        Some(entityDetails.address)
      )

  def partialAndOptionalAddressMapping(
    maxAddressLength: Int = 70
  )(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[EntityDetails] =
    Forms
      .mapping("address" -> optional(Address.partialAndOptionalMapping(maxAddressLength)))(maybeAddress => EntityDetails(None, maybeAddress))(
        entityDetails => Some(entityDetails.address)
      )
}
