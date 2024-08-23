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

package forms.section2

import connectors.CodeListConnector
import forms.DeclarationPage
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import models.viewmodels.TariffContentKey
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{ExportsDeclarationDiff, combinePointers}

case class ConsigneeDetails(details: EntityDetails) extends Details with DiffTools[ConsigneeDetails] {
  override def createDiff(original: ConsigneeDetails, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(details.createDiff(original.details, combinePointers(pointerString, sequenceId))).flatten
}

object ConsigneeDetails extends DeclarationPage with FieldMapping {

  implicit val format: OFormat[ConsigneeDetails] = Json.format[ConsigneeDetails]

  val pointer: ExportsFieldPointer = "consigneeDetails.details"

  val id = "ConsigneeDetails"

  private def mapping(
    declarationType: DeclarationType
  )(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[ConsigneeDetails] =
    if (declarationType == CLEARANCE)
      Forms.mapping("details" -> EntityDetails.partialAndOptionalAddressMapping(35))(ConsigneeDetails.apply)(ConsigneeDetails.unapply)
    else Forms.mapping("details" -> EntityDetails.addressMapping(35))(ConsigneeDetails.apply)(ConsigneeDetails.unapply)

  def form(declarationType: DeclarationType)(implicit messages: Messages, codeListConnector: CodeListConnector): Form[ConsigneeDetails] = Form(
    mapping(declarationType)
  )

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.consigneeDetails.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
