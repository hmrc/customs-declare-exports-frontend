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

package forms.declaration.consignor

import connectors.CodeListConnector
import forms.DeclarationPage
import forms.declaration.{Details, EntityDetails}
import models.viewmodels.TariffContentKey
import models.DeclarationType.DeclarationType
import models.ExportsFieldPointer.ExportsFieldPointer
import models.{AmendmentOp, FieldMapping}
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, ExportsDeclarationDiff}

case class ConsignorDetails(details: EntityDetails) extends Details with DiffTools[ConsignorDetails] with AmendmentOp {

  override def createDiff(original: ConsignorDetails, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(details.createDiff(original.details, combinePointers(pointerString, sequenceId))).flatten

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    details.valueAdded(pointer)

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    details.valueRemoved(pointer)
}

object ConsignorDetails extends DeclarationPage with FieldMapping {
  implicit val format: OFormat[ConsignorDetails] = Json.format[ConsignorDetails]

  val pointer: ExportsFieldPointer = "consignorDetails"

  val id = "ConsignorDetails"

  def mapping(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[ConsignorDetails] =
    Forms.mapping("details" -> EntityDetails.addressMapping)(ConsignorDetails.apply)(ConsignorDetails.unapply)

  def form(implicit messages: Messages, codeListConnector: CodeListConnector): Form[ConsignorDetails] = Form(mapping)

  def from(consignorEoriDetails: ConsignorEoriNumber, savedConsignorDetails: Option[ConsignorDetails]): ConsignorDetails =
    consignorEoriDetails.eori match {
      case None =>
        savedConsignorDetails.flatMap(_.details.address) match {
          case None          => ConsignorDetails(EntityDetails(None, None))
          case Some(address) => ConsignorDetails(EntityDetails(None, Some(address)))
        }
      case Some(_) => ConsignorDetails(EntityDetails(consignorEoriDetails.eori, None))
    }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.consignorAddress.clearance"))
}
