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

package services.cache

import java.time.{Instant, LocalDateTime, ZoneOffset}

import config.AppConfig
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import forms.declaration.{NatureOfTransaction, _}
import javax.inject.Inject
import models.declaration.{Locations, Parties, TransportInformationContainerData}
import play.api.libs.json.{JsError, JsNumber, JsObject, JsResult, JsSuccess, JsValue, Json, OFormat}
import play.modules.reactivemongo.ReactiveMongoComponent
import play.mvc.Http.Session
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.play.json.ImplicitBSONHandlers._
import reactivemongo.play.json.collection.JSONBatchCommands.FindAndModifyCommand
import reactivemongo.play.json.commands.JSONFindAndModifyCommand.FindAndModifyResult
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.objectIdFormats

import scala.concurrent.{ExecutionContext, Future}

class ExportsCacheModelRepository @Inject()(mc: ReactiveMongoComponent, appConfig: AppConfig)(
  implicit ec: ExecutionContext
) extends ReactiveRepository[ExportsCacheModel, BSONObjectID](
      "exportsJourneyCache",
      mc.mongoConnector.db,
      ExportsCacheModel.format,
      objectIdFormats
    ) {

  implicit val journeyFormats = ExportsCacheModel.format

  override def indexes: Seq[Index] = super.indexes ++ Seq(
    Index(
      key = Seq("updatedDateTime" -> IndexType.Ascending),
      name = Some("ttl"),
      options = BSONDocument("expireAfterSeconds" -> appConfig.cacheTimeToLive.toSeconds)
    )
  )

  def get(sessionId: String): Future[Option[ExportsCacheModel]] =
    find("sessionId" -> sessionId).map(_.headOption)

  def upsert(sessionId: String, journeyCacheModel: ExportsCacheModel): Future[Option[ExportsCacheModel]] =
    collection
      .findAndUpdate(
        selector = bySessionId(sessionId),
        update = journeyCacheModel,
        fetchNewObject = true,
        upsert = true
      )
      .map { updateResult =>
        if (updateResult.value.isEmpty) logDatabaseUpdateError(updateResult)
        updateResult.result[ExportsCacheModel]
      }

  def remove(sessionId: String): Future[FindAndModifyCommand.FindAndModifyResult] =
    collection.findAndRemove(selector = bySessionId(sessionId))

  private def bySessionId(id: String): JsObject =
    Json.obj("sessionId" -> id)

  private def logDatabaseUpdateError(res: FindAndModifyResult): Unit =
    res.lastError.foreach(_.err.foreach(errorMsg => logger.error(s"Problem during database update: $errorMsg")))

}

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
) {
  def itemBy(itemId: String): Option[ExportItem] = items.find(_.id.equalsIgnoreCase(itemId))
}

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
