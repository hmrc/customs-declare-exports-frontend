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

package forms.section2.carrier

import connectors.CodeListConnector
import forms.DeclarationPage
import forms.common.Eori
import forms.declaration.{Details, EntityDetails}
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.ExportsFieldPointer.ExportsFieldPointer
import models.viewmodels.TariffContentKey
import models.{AmendmentOp, FieldMapping}
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, ExportsDeclarationDiff}

case class CarrierDetails(details: EntityDetails) extends Details with DiffTools[CarrierDetails] with AmendmentOp {

  override def createDiff(original: CarrierDetails, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(details.createDiff(original.details, combinePointers(pointerString, sequenceId))).flatten

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    details.valueAdded(pointer)

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    details.valueRemoved(pointer)
}

object CarrierDetails extends DeclarationPage with FieldMapping {
  implicit val format: OFormat[CarrierDetails] = Json.format[CarrierDetails]

  val pointer: ExportsFieldPointer = "carrierDetails"

  val id = "CarrierDetails"

  def defaultMapping(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[CarrierDetails] =
    Forms.mapping("details" -> EntityDetails.addressMapping())(CarrierDetails.apply)(CarrierDetails.unapply)

  def optionalMapping(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[CarrierDetails] =
    Forms.mapping("details" -> EntityDetails.optionalAddressMapping())(CarrierDetails.apply)(CarrierDetails.unapply)

  def form(declarationType: DeclarationType)(implicit messages: Messages, codeListConnector: CodeListConnector): Form[CarrierDetails] =
    declarationType match {
      case CLEARANCE => Form(optionalMapping)
      case _         => Form(defaultMapping)
    }

  def from(carrierEoriDetails: CarrierEoriNumber, savedCarrierDetails: Option[CarrierDetails]): CarrierDetails =
    carrierEoriDetails.eori match {
      case None =>
        savedCarrierDetails.flatMap(_.details.address) match {
          case None          => CarrierDetails(EntityDetails(None, None))
          case Some(address) => CarrierDetails(EntityDetails(None, Some(address)))
        }
      case Some(_) => CarrierDetails(EntityDetails(carrierEoriDetails.eori, None))
    }

  def from(eori: String): CarrierDetails = CarrierDetails(EntityDetails(Some(Eori(eori)), None))

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.carrierAddress.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
