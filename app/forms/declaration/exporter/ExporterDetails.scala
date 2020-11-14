/*
 * Copyright 2020 HM Revenue & Customs
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

package forms.declaration.exporter

import forms.DeclarationPage
import forms.declaration.EntityDetails
import forms.declaration.consignor.{ConsignorDetails, ConsignorEoriNumber}
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.ExportsDeclaration
import play.api.data.{Form, Forms}
import play.api.libs.json.Json

case class ExporterDetails(details: EntityDetails)

object ExporterDetails extends DeclarationPage {
  implicit val format = Json.format[ExporterDetails]

  val defaultMapping = Forms.mapping("details" -> EntityDetails.defaultMapping)(ExporterDetails.apply)(ExporterDetails.unapply)

  val optionalMapping = Forms.mapping("details" -> EntityDetails.optionalMapping)(ExporterDetails.apply)(ExporterDetails.unapply)

  def form(declarationType: DeclarationType, cachedModel: Option[ExportsDeclaration] = None): Form[ExporterDetails] = declarationType match {
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
}
