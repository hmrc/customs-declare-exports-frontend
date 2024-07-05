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

package forms.section2.exporter

import connectors.CodeListConnector
import forms.DeclarationPage
import forms.section2.{Details, EntityDetails}
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.ExportsFieldPointer.ExportsFieldPointer
import models.viewmodels.TariffContentKey
import models.{AmendmentOp, ExportsDeclaration, FieldMapping}
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, ExportsDeclarationDiff}

case class ExporterDetails(details: EntityDetails) extends Details with DiffTools[ExporterDetails] with AmendmentOp {

  override def createDiff(original: ExporterDetails, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(details.createDiff(original.details, combinePointers(pointerString, sequenceId))).flatten

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    details.valueAdded(pointer)

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    details.valueRemoved(pointer)
}

object ExporterDetails extends DeclarationPage with FieldMapping {
  implicit val format: OFormat[ExporterDetails] = Json.format[ExporterDetails]

  val pointer: ExportsFieldPointer = "exporterDetails"

  def defaultMapping(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[ExporterDetails] =
    Forms.mapping("details" -> EntityDetails.addressMapping(35))(ExporterDetails.apply)(ExporterDetails.unapply)

  def optionalMapping(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[ExporterDetails] =
    Forms.mapping("details" -> EntityDetails.optionalAddressMapping(35))(ExporterDetails.apply)(ExporterDetails.unapply)

  def form(
    declarationType: DeclarationType,
    cachedModel: Option[ExportsDeclaration] = None
  )(implicit messages: Messages, codeListConnector: CodeListConnector): Form[ExporterDetails] =
    declarationType match {
      case CLEARANCE if cachedModel.exists(_.isNotEntryIntoDeclarantsRecords) => Form(optionalMapping)
      case _                                                                  => Form(defaultMapping)
    }

  def from(exporterEoriNumber: ExporterEoriNumber, savedExporterDetails: Option[ExporterDetails]): ExporterDetails =
    exporterEoriNumber.eori match {
      case None =>
        savedExporterDetails.flatMap(_.details.address) match {
          case None          => ExporterDetails(EntityDetails(None, None))
          case Some(address) => ExporterDetails(EntityDetails(None, Some(address)))
        }
      case Some(_) => ExporterDetails(EntityDetails(exporterEoriNumber.eori, None))
    }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.exporterAddress.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
