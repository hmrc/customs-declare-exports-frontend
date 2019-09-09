/*
 * Copyright 2019 HM Revenue & Customs
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

package models

import java.time.{Clock, Instant}

import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import models.DeclarationStatus.DeclarationStatus
import models.declaration.{Container, Locations, Parties, TransportInformationContainerData}
import play.api.libs.json._
import services.cache.ExportItem

case class ExportsDeclaration(
  id: Option[String] = None,
  status: DeclarationStatus = DeclarationStatus.COMPLETE,
  createdDateTime: Instant,
  updatedDateTime: Instant,
  sourceId: Option[String],
  choice: String,
  dispatchLocation: Option[DispatchLocation] = None,
  additionalDeclarationType: Option[AdditionalDeclarationType] = None,
  consignmentReferences: Option[ConsignmentReferences] = None,
  borderTransport: Option[BorderTransport] = None,
  transportDetails: Option[TransportDetails] = None,
  containerData: Option[TransportInformationContainerData] = None,
  parties: Parties = Parties(),
  locations: Locations = Locations(),
  items: Set[ExportItem] = Set.empty,
  totalNumberOfItems: Option[TotalNumberOfItems] = None,
  previousDocuments: Option[PreviousDocumentsData] = None,
  natureOfTransaction: Option[NatureOfTransaction] = None
) {
  val lrn: Option[String] = this.consignmentReferences.map(_.lrn)
  val ducr: Option[String] = this.consignmentReferences.map(_.ducr.ducr)

  def updatedItem(itemId: String, update: ExportItem => ExportItem): ExportsDeclaration =
    itemBy(itemId).fold(this) { item =>
      val updated = update(item)
      copy(items = items.filterNot(_.id.equalsIgnoreCase(itemId)) + updated)
    }

  def itemBy(itemId: String): Option[ExportItem] = items.find(_.id.equalsIgnoreCase(itemId))

  def containers: Seq[Container] = containerData.map(_.containers).getOrElse(Seq.empty)

  def containerBy(containerId: String): Option[Container] = containers.find(_.id.equalsIgnoreCase(containerId))

  def amend(sourceId: String)(implicit clock: Clock = Clock.systemUTC()): ExportsDeclaration = {
    val currentTime = Instant.now(clock)
    this.copy(
      id = None,
      status = DeclarationStatus.DRAFT,
      createdDateTime = currentTime,
      updatedDateTime = currentTime,
      sourceId = Some(sourceId)
    )
  }
}

object ExportsDeclaration {
  implicit val formatInstant: OFormat[Instant] = new OFormat[Instant] {
    override def writes(datetime: Instant): JsObject =
      Json.obj("$date" -> datetime.toEpochMilli)

    override def reads(json: JsValue): JsResult[Instant] =
      json match {
        case JsObject(map) if map.contains("$date") =>
          map("$date") match {
            case JsNumber(v) => JsSuccess(Instant.ofEpochMilli(v.toLong))
            case _           => JsError("Unexpected Date Format. Expected a Number (Epoch Milliseconds)")
          }
        case _ => JsError("Unexpected Date Format. Expected an object containing a $date field.")
      }
  }
  implicit val format: OFormat[ExportsDeclaration] = Json.format[ExportsDeclaration]
}
