/*
 * Copyright 2021 HM Revenue & Customs
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

import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import forms.declaration.countries.Country
import models.DeclarationStatus.DeclarationStatus
import models.DeclarationType.DeclarationType
import models.declaration._
import play.api.libs.json._
import java.time.{Clock, Instant}

import forms.declaration.additionaldocuments.DocumentsProduced

case class ExportsDeclaration(
  id: String,
  status: DeclarationStatus,
  createdDateTime: Instant,
  updatedDateTime: Instant,
  sourceId: Option[String],
  `type`: DeclarationType,
  additionalDeclarationType: Option[AdditionalDeclarationType] = None,
  consignmentReferences: Option[ConsignmentReferences] = None,
  linkDucrToMucr: Option[YesNoAnswer] = None,
  mucr: Option[Mucr] = None,
  transport: Transport = Transport(),
  parties: Parties = Parties(),
  locations: Locations = Locations(),
  items: Seq[ExportItem] = Seq.empty,
  totalNumberOfItems: Option[TotalNumberOfItems] = None,
  totalPackageQuantity: Option[TotalPackageQuantity] = None,
  previousDocuments: Option[PreviousDocumentsData] = None,
  natureOfTransaction: Option[NatureOfTransaction] = None
) {

  lazy val lrn: Option[String] = consignmentReferences.map(_.lrn.value)
  lazy val ducr: Option[String] = consignmentReferences.map(_.ducr.ducr)

  def documentsProducedDataIfAny(itemId: String): Option[DocumentsProducedData] =
    itemBy(itemId).flatMap(_.documentsProducedData)

  def documentsProducedData(itemId: String): DocumentsProducedData =
    documentsProducedDataIfAny(itemId).getOrElse(DocumentsProducedData(None, Seq.empty))

  def additionalDocuments(itemId: String): Seq[DocumentsProduced] =
    documentsProducedDataIfAny(itemId).map(_.documents).getOrElse(Seq.empty)

  def additionalDocumentsRequired(itemId: String): Option[YesNoAnswer] =
    documentsProducedDataIfAny(itemId).flatMap(_.isRequired)

  def addOrUpdateContainer(container: Container): ExportsDeclaration =
    copy(transport = transport.addOrUpdateContainer(container))

  def amend()(implicit clock: Clock = Clock.systemUTC()): ExportsDeclaration = {
    val currentTime = Instant.now(clock)
    this.copy(status = DeclarationStatus.DRAFT, createdDateTime = currentTime, updatedDateTime = currentTime, sourceId = Some(id))
  }

  def clearRoutingCountries(): ExportsDeclaration =
    copy(locations = locations.copy(hasRoutingCountries = Some(false), routingCountries = Seq.empty))

  def commodityCode(itemId: String): Option[String] =
    itemBy(itemId).flatMap(_.commodityDetails.flatMap(_.combinedNomenclatureCode))

  def containerBy(containerId: String): Option[Container] = containers.find(_.id.equalsIgnoreCase(containerId))

  def containers: Seq[Container] = transport.containers.getOrElse(Seq.empty)

  def containRoutingCountries(): Boolean = locations.routingCountries.nonEmpty

  def hasContainers: Boolean = containers.nonEmpty

  def isComplete: Boolean = status == DeclarationStatus.COMPLETE

  def isDeclarantExporter: Boolean = parties.declarantIsExporter.exists(_.isExporter)

  def isExs: Boolean = parties.isExs.exists(_.isExs == YesNoAnswers.yes)
  def isNotExs: Boolean = !isExs

  def isEntryIntoDeclarantsRecords: Boolean = parties.isEntryIntoDeclarantsRecords.exists(_.answer == YesNoAnswers.yes)
  def isNotEntryIntoDeclarantsRecords: Boolean = !isEntryIntoDeclarantsRecords

  def isAdditionalDocumentationRequired: Boolean = parties.declarationHoldersData.exists(_.holders.exists(_.isAdditionalDocumentationRequired))

  def itemBy(itemId: String): Option[ExportItem] = items.find(_.id.equalsIgnoreCase(itemId))

  def hasPreviousDocuments: Boolean = previousDocuments.map(_.documents).exists(_.nonEmpty)

  def requiresWarehouseId: Boolean = items.exists(_.requiresWarehouseId)

  def getCommodityCodeOfItem(itemId: String): Option[String] =
    for {
      exportItem <- itemBy(itemId)
      commodityDetails <- exportItem.commodityDetails
      commodityCode <- commodityDetails.combinedNomenclatureCode
    } yield commodityCode

  def isCommodityCodeOfItemPrefixedWith(itemId: String, prefix: Seq[Int]): Boolean =
    getCommodityCodeOfItem(itemId) match {
      case Some(commodityCode) if (commodityCode.trim.length > 0) =>
        prefix.exists(digits => commodityCode.startsWith(digits.toString))
      case _ => false
    }

  def removeCountryOfRouting(country: Country): ExportsDeclaration =
    copy(locations = locations.copy(routingCountries = locations.routingCountries.filterNot(_ == country)))

  def removeSupervisingCustomsOffice: ExportsDeclaration =
    copy(locations = locations.copy(supervisingCustomsOffice = None))

  def updateTransportLeavingBorder(code: ModeOfTransportCode): ExportsDeclaration =
    copy(transport = transport.copy(borderModeOfTransportCode = Some(TransportLeavingTheBorder(Some(code)))))

  def updateTransportLeavingBorder(transportLeavingTheBorder: TransportLeavingTheBorder): ExportsDeclaration =
    copy(transport = transport.copy(borderModeOfTransportCode = Some(transportLeavingTheBorder)))

  def updateDepartureTransport(departure: DepartureTransport): ExportsDeclaration =
    copy(
      transport = transport.copy(
        meansOfTransportOnDepartureType = departure.meansOfTransportOnDepartureType,
        meansOfTransportOnDepartureIDNumber = departure.meansOfTransportOnDepartureIDNumber
      )
    )

  def updateBorderTransport(formData: BorderTransport): ExportsDeclaration =
    copy(
      transport = transport.copy(
        meansOfTransportCrossingTheBorderType = Some(formData.meansOfTransportCrossingTheBorderType),
        meansOfTransportCrossingTheBorderIDNumber = Some(formData.meansOfTransportCrossingTheBorderIDNumber),
        meansOfTransportCrossingTheBorderNationality = formData.meansOfTransportCrossingTheBorderNationality
      )
    )

  def updatedItem(itemId: String, update: ExportItem => ExportItem): ExportsDeclaration =
    copy(items = items.map(item => if (item.id == itemId) update(item) else item))

  def updateType(`type`: DeclarationType): ExportsDeclaration = copy(`type` = `type`)

  def updateCountriesOfRouting(routingCountries: Seq[Country]): ExportsDeclaration =
    copy(locations = locations.copy(routingCountries = routingCountries))

  def updateOriginationCountry(originationCountry: Country): ExportsDeclaration =
    copy(locations = locations.copy(originationCountry = Some(originationCountry)))

  def updateDestinationCountry(destinationCountry: Country): ExportsDeclaration =
    copy(locations = locations.copy(destinationCountry = Some(destinationCountry)))

  def updateRoutingQuestion(answer: Boolean): ExportsDeclaration =
    copy(locations = locations.copy(hasRoutingCountries = Some(answer)))

  def updateContainers(containers: Seq[Container]): ExportsDeclaration =
    copy(transport = transport.copy(containers = Some(containers)))

  def updateTransportPayment(payment: TransportPayment): ExportsDeclaration =
    copy(transport = transport.copy(expressConsignment = Some(YesNoAnswer.Yes), transportPayment = Some(payment)))

  def updatePreviousDocuments(previousDocuments: Seq[Document]): ExportsDeclaration =
    copy(previousDocuments = Some(PreviousDocumentsData(previousDocuments)))

  def transform(function: ExportsDeclaration => ExportsDeclaration): ExportsDeclaration = function(this)
}

object ExportsDeclaration {

  implicit val format: OFormat[ExportsDeclaration] = Json.format[ExportsDeclaration]
}
