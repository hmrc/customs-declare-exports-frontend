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
import forms.DeclarationPage
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import models.ExportsFieldPointer.ExportsFieldPointer
import models.{AmendmentOp, FieldMapping}
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, ExportsDeclarationDiff}

case class ConsigneeDetails(details: EntityDetails) extends DiffTools[ConsigneeDetails] with AmendmentOp {

  override def createDiff(original: ConsigneeDetails, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(details.createDiff(original.details, combinePointers(pointerString, sequenceId))).flatten

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    details.valueAdded(pointer)

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    details.valueRemoved(pointer)
}

object ConsigneeDetails extends DeclarationPage with FieldMapping {

  implicit val format: OFormat[ConsigneeDetails] = Json.format[ConsigneeDetails]

  val pointer: ExportsFieldPointer = "consigneeDetails"

  val id = "ConsigneeDetails"

  def mapping(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[ConsigneeDetails] =
    Forms.mapping("details" -> EntityDetails.addressMapping(35))(ConsigneeDetails.apply)(ConsigneeDetails.unapply)

  def form(implicit messages: Messages, codeListConnector: CodeListConnector): Form[ConsigneeDetails] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.consigneeDetails.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
