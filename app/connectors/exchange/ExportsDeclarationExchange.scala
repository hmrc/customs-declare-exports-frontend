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

package connectors.exchange

import java.time.Instant

import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import models.DeclarationStatus.DeclarationStatus
import models.DeclarationType.DeclarationType
import models.ExportsDeclaration
import models.declaration._

case class ExportsDeclarationExchange(
  id: Option[String] = None,
  status: DeclarationStatus,
  createdDateTime: Instant,
  updatedDateTime: Instant,
  sourceId: Option[String],
  `type`: DeclarationType,
  dispatchLocation: Option[DispatchLocation] = None,
  additionalDeclarationType: Option[AdditionalDeclarationType] = None,
  consignmentReferences: Option[ConsignmentReferences] = None,
  transport: Transport = Transport(),
  parties: Parties = Parties(),
  locations: Locations = Locations(),
  items: Set[ExportItem] = Set.empty[ExportItem],
  totalNumberOfItems: Option[TotalNumberOfItems] = None,
  previousDocuments: Option[PreviousDocumentsData] = None,
  natureOfTransaction: Option[NatureOfTransaction] = None
) {
  def toExportsDeclaration: ExportsDeclaration = ExportsDeclaration(
    id = this.id.get,
    status = this.status,
    createdDateTime = this.createdDateTime,
    updatedDateTime = this.updatedDateTime,
    sourceId = this.sourceId,
    `type` = this.`type`,
    dispatchLocation = this.dispatchLocation,
    additionalDeclarationType = this.additionalDeclarationType,
    consignmentReferences = this.consignmentReferences,
    transport = this.transport,
    parties = this.parties,
    locations = this.locations,
    items = this.items,
    totalNumberOfItems = this.totalNumberOfItems,
    previousDocuments = this.previousDocuments,
    natureOfTransaction = this.natureOfTransaction
  )
}

object ExportsDeclarationExchange {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  def version1(
    id: String,
    status: DeclarationStatus,
    createdDateTime: Instant,
    updatedDateTime: Instant,
    sourceId: Option[String],
    `type`: DeclarationType,
    dispatchLocation: Option[DispatchLocation] = None,
    additionalDeclarationType: Option[AdditionalDeclarationType] = None,
    consignmentReferences: Option[ConsignmentReferences] = None,
    departureTransport: Option[DepartureTransport] = None,
    borderTransport: Option[BorderTransport] = None,
    transportInformation: Option[TransportInformation] = None,
    parties: Parties = Parties(),
    locations: Locations = Locations(),
    items: Set[ExportItem] = Set.empty[ExportItem],
    totalNumberOfItems: Option[TotalNumberOfItems] = None,
    previousDocuments: Option[PreviousDocumentsData] = None,
    natureOfTransaction: Option[NatureOfTransaction] = None
  ): ExportsDeclarationExchange = {
    val transport = Transport(
      transportPayment = transportInformation.flatMap(_.transportPayment),
      containers = transportInformation.map(_.containers).getOrElse(Seq.empty),
      borderModeOfTransportCode = departureTransport.map(_.borderModeOfTransportCode),
      meansOfTransportOnDepartureType = departureTransport.map(_.meansOfTransportOnDepartureType),
      meansOfTransportOnDepartureIDNumber = departureTransport.map(_.meansOfTransportOnDepartureIDNumber),
      meansOfTransportCrossingTheBorderNationality = borderTransport.flatMap(_.meansOfTransportCrossingTheBorderNationality),
      meansOfTransportCrossingTheBorderType = borderTransport.map(_.meansOfTransportCrossingTheBorderType),
      meansOfTransportCrossingTheBorderIDNumber = borderTransport.map(_.meansOfTransportCrossingTheBorderIDNumber)
    )

    new ExportsDeclarationExchange(
      Some(id),
      status,
      createdDateTime,
      updatedDateTime,
      sourceId,
      `type`,
      dispatchLocation,
      additionalDeclarationType,
      consignmentReferences,
      transport,
      parties,
      locations,
      items,
      totalNumberOfItems,
      previousDocuments,
      natureOfTransaction
    )

  }

  def version2(
    id: String,
    status: DeclarationStatus,
    createdDateTime: Instant,
    updatedDateTime: Instant,
    sourceId: Option[String],
    `type`: DeclarationType,
    dispatchLocation: Option[DispatchLocation] = None,
    additionalDeclarationType: Option[AdditionalDeclarationType] = None,
    consignmentReferences: Option[ConsignmentReferences] = None,
    transport: Transport = Transport(),
    parties: Parties = Parties(),
    locations: Locations = Locations(),
    items: Set[ExportItem] = Set.empty[ExportItem],
    totalNumberOfItems: Option[TotalNumberOfItems] = None,
    previousDocuments: Option[PreviousDocumentsData] = None,
    natureOfTransaction: Option[NatureOfTransaction] = None
  ): ExportsDeclarationExchange =
    new ExportsDeclarationExchange(
      Some(id),
      status,
      createdDateTime,
      updatedDateTime,
      sourceId,
      `type`,
      dispatchLocation,
      additionalDeclarationType,
      consignmentReferences,
      transport,
      parties,
      locations,
      items,
      totalNumberOfItems,
      previousDocuments,
      natureOfTransaction
    )

  val readsVersion1: Reads[ExportsDeclarationExchange] = (
    (__ \ "id").read[String] and
      (__ \ "status").read[DeclarationStatus] and
      (__ \ "createdDateTime").read[Instant] and
      (__ \ "updatedDateTime").read[Instant] and
      (__ \ "sourceId").readNullable[String] and
      (__ \ "type").read[DeclarationType] and
      (__ \ "dispatchLocation").readNullable[DispatchLocation] and
      (__ \ "additionalDeclarationType").readNullable[AdditionalDeclarationType] and
      (__ \ "consignmentReferences").readNullable[ConsignmentReferences] and
      (__ \ "departureTransport").readNullable[DepartureTransport] and
      (__ \ "borderTransport").readNullable[BorderTransport] and
      (__ \ "transportInformation").readNullable[TransportInformation] and
      (__ \ "parties").read[Parties] and
      (__ \ "locations").read[Locations] and
      (__ \ "items").read[Set[ExportItem]] and
      (__ \ "totalNumberOfItems").readNullable[TotalNumberOfItems] and
      (__ \ "previousDocuments").readNullable[PreviousDocumentsData] and
      (__ \ "natureOfTransaction").readNullable[NatureOfTransaction]
  ).apply(ExportsDeclarationExchange.version1 _)

  val readsVersion2: Reads[ExportsDeclarationExchange] = (
    (__ \ "id").read[String] and
      (__ \ "status").read[DeclarationStatus] and
      (__ \ "createdDateTime").read[Instant] and
      (__ \ "updatedDateTime").read[Instant] and
      (__ \ "sourceId").readNullable[String] and
      (__ \ "type").read[DeclarationType] and
      (__ \ "dispatchLocation").readNullable[DispatchLocation] and
      (__ \ "additionalDeclarationType").readNullable[AdditionalDeclarationType] and
      (__ \ "consignmentReferences").readNullable[ConsignmentReferences] and
      (__ \ "transport").read[Transport] and
      (__ \ "parties").read[Parties] and
      (__ \ "locations").read[Locations] and
      (__ \ "items").read[Set[ExportItem]] and
      (__ \ "totalNumberOfItems").readNullable[TotalNumberOfItems] and
      (__ \ "previousDocuments").readNullable[PreviousDocumentsData] and
      (__ \ "natureOfTransaction").readNullable[NatureOfTransaction]
  ).apply(ExportsDeclarationExchange.version2 _)

  val writesVersion2: OWrites[ExportsDeclarationExchange] = OWrites[ExportsDeclarationExchange] { declaration =>
    val values = Seq(
      Some("id" -> Json.toJson(declaration.id)),
      Some("status" -> Json.toJson(declaration.status)),
      Some("createdDateTime" -> Json.toJson(declaration.createdDateTime)),
      Some("updatedDateTime" -> Json.toJson(declaration.updatedDateTime)),
      declaration.sourceId.map(source => "sourceId" -> Json.toJson(source)),
      Some("type" -> Json.toJson(declaration.`type`)),
      declaration.dispatchLocation.map("dispatchLocation" -> Json.toJson(_)),
      declaration.additionalDeclarationType.map("additionalDeclarationType" -> Json.toJson(_)),
      declaration.consignmentReferences.map("consignmentReferences" -> Json.toJson(_)),
      Some("transport" -> Json.toJson(declaration.transport)),
      Some("parties" -> Json.toJson(declaration.parties)),
      Some("locations" -> Json.toJson(declaration.locations)),
      Some("items" -> Json.toJson(declaration.items)),
      declaration.totalNumberOfItems.map("totalNumberOfItems" -> Json.toJson(_)),
      declaration.previousDocuments.map("previousDocuments" -> Json.toJson(_)),
      declaration.natureOfTransaction.map("natureOfTransaction" -> Json.toJson(_))
    )
    JsObject(values.flatten)
  }

  implicit val format: OFormat[ExportsDeclarationExchange] = Json.format[ExportsDeclarationExchange]

  private def buildDeclaration(declaration: ExportsDeclaration, idProvider: ExportsDeclaration => Option[String]): ExportsDeclarationExchange = {
    ExportsDeclarationExchange(
      id = idProvider(declaration),
      status = declaration.status,
      createdDateTime = declaration.createdDateTime,
      updatedDateTime = declaration.updatedDateTime,
      sourceId = declaration.sourceId,
      `type` = declaration.`type`,
      dispatchLocation = declaration.dispatchLocation,
      additionalDeclarationType = declaration.additionalDeclarationType,
      consignmentReferences = declaration.consignmentReferences,
      transport = declaration.transport,
      parties = declaration.parties,
      locations = declaration.locations,
      items = declaration.items,
      totalNumberOfItems = declaration.totalNumberOfItems,
      previousDocuments = declaration.previousDocuments,
      natureOfTransaction = declaration.natureOfTransaction
    )
  }


  def apply(declaration: ExportsDeclaration): ExportsDeclarationExchange =
    buildDeclaration(declaration, declaration => Some(declaration.id))

  def withoutId(declaration: ExportsDeclaration): ExportsDeclarationExchange =
    buildDeclaration(declaration, _ => None)
}
