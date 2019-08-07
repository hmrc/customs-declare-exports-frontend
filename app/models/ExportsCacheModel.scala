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

import java.time.{Instant, LocalDateTime, ZoneOffset}

import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import models.declaration.{Locations, Parties, TransportInformationContainerData}
import play.api.libs.json._
import services.cache.ExportItem

case class ExportsCacheModel(
  sessionId: String,
  draftId: String,
  createdDateTime: LocalDateTime,
  updatedDateTime: LocalDateTime,
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
  natureOfTransaction: Option[NatureOfTransaction] = None,
  seals: Seq[Seal] = Seq.empty
)

object ExportsCacheModel {
  implicit val formatInstant: OFormat[LocalDateTime] = new OFormat[LocalDateTime] {
    override def writes(datetime: LocalDateTime): JsObject =
      Json.obj("$date" -> datetime.toInstant(ZoneOffset.UTC).toEpochMilli)

    override def reads(json: JsValue): JsResult[LocalDateTime] =
      json match {
        case JsObject(map) if map.contains("$date") =>
          map("$date") match {
            case JsNumber(v) => JsSuccess(Instant.ofEpochMilli(v.toLong).atOffset(ZoneOffset.UTC).toLocalDateTime)
            case _           => JsError("Unexpected Date Format. Expected a Number (Epoch Milliseconds)")
          }
        case _ => JsError("Unexpected Date Format. Expected an object containing a $date field.")
      }
  }
  implicit val format: OFormat[ExportsCacheModel] = Json.format[ExportsCacheModel]
}
