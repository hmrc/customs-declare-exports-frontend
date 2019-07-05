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

import java.time.LocalDateTime

import forms.declaration.{NatureOfTransaction, _}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import javax.inject.Inject
import models.declaration.{AdditionalInformationData, DocumentsProducedData, Locations, Parties, TransportInformationContainerData}
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.ImplicitBSONHandlers._
import reactivemongo.play.json.commands.JSONFindAndModifyCommand.FindAndModifyResult
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.objectIdFormats

import scala.concurrent.{ExecutionContext, Future}
import models.declaration.{Locations, Parties, TransportInformationContainerData}

class ExportsCacheModelRepository @Inject()(mc: ReactiveMongoComponent)(implicit ec: ExecutionContext)
    extends ReactiveRepository[ExportsCacheModel, BSONObjectID](
      "exportsJourneyCache",
      mc.mongoConnector.db,
      ExportsCacheModel.format,
      objectIdFormats
    ) {

  implicit val journeyFormats = ExportsCacheModel.format

  def get(sessionId: String): Future[Either[String, ExportsCacheModel]] =
    find("sessionId" -> sessionId).map(_.headOption).map {
      case Some(model) => Right(model)
      case None        => Left(s"Unable to find model with sessionID: $sessionId")
    }

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
  exporterDetails: Option[ExporterDetails] = None,
  warehouseIdentification: Option[WarehouseIdentification] = None,
  borderTransport: Option[BorderTransport] = None,
  transportDetails: Option[TransportDetails] = None,
  containerData: Option[TransportInformationContainerData] = None,
  parties: Parties = Parties(),
  locations: Locations = Locations(),
  items: Set[ExportItem] = Set.empty,
  totalNumberOfItems: Option[TotalNumberOfItems] = None,
  previousDocuments: Option[PreviousDocumentsData] = None,
  natureOfTransaction: Option[NatureOfTransaction] = None
)

object ExportsCacheModel {
  implicit val format = Json.format[ExportsCacheModel]
}
