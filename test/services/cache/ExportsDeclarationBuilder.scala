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

import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}
import java.util.UUID

import forms.common.Address
import forms.declaration.DispatchLocation.AllowedDispatchLocations.OutsideEU
import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import forms.declaration.destinationCountries.DestinationCountries
import forms.declaration.officeOfExit.OfficeOfExit
import forms.{Ducr, Lrn}
import models.DeclarationStatus.DeclarationStatus
import models.DeclarationType.DeclarationType
import models.declaration._
import models.{DeclarationStatus, DeclarationType, ExportsDeclaration}

//noinspection ScalaStyle
trait ExportsDeclarationBuilder {

  private type ExportsDeclarationModifier = ExportsDeclaration => ExportsDeclaration
  protected val DUCR = "5GB123456789000-123ABC456DEFIIIII"
  protected val LRN = Lrn("FG7676767889")
  private val modelWithDefaults: ExportsDeclaration = ExportsDeclaration(
    uuid,
    status = DeclarationStatus.COMPLETE,
    createdDateTime = LocalDateTime.of(2019, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC),
    updatedDateTime = LocalDateTime.of(2019, 2, 2, 0, 0, 0).toInstant(ZoneOffset.UTC),
    `type` = DeclarationType.STANDARD,
    sourceId = None
  )

  def aDeclaration(modifiers: ExportsDeclarationModifier*): ExportsDeclaration =
    modifiers.foldLeft(modelWithDefaults)((current, modifier) => modifier(current))

  def aDeclarationAfter(declaration: ExportsDeclaration, modifiers: ExportsDeclarationModifier*): ExportsDeclaration =
    modifiers.foldLeft(declaration)((current, modifier) => modifier(current))

  def withId(id: String = uuid): ExportsDeclarationModifier = _.copy(id = id)

  // ************************************************* Builders ********************************************************

  def withStatus(status: DeclarationStatus): ExportsDeclarationModifier = _.copy(status = status)

  def withType(`type`: DeclarationType): ExportsDeclarationModifier = _.copy(`type` = `type`)

  def withSourceId(id: String = uuid): ExportsDeclarationModifier = _.copy(sourceId = Some(id))

  def withoutSourceId(): ExportsDeclarationModifier = _.copy(sourceId = None)

  def withCreatedDate(date: LocalDateTime): ExportsDeclarationModifier =
    _.copy(createdDateTime = date.toInstant(ZoneOffset.UTC))

  def withCreatedDate(date: LocalDate): ExportsDeclarationModifier =
    _.copy(createdDateTime = date.atStartOfDay().toInstant(ZoneOffset.UTC))

  def withCreatedTime(createdDateTime: Instant): ExportsDeclarationModifier = _.copy(createdDateTime = createdDateTime)

  def withUpdateDate(date: LocalDateTime): ExportsDeclarationModifier =
    _.copy(updatedDateTime = date.toInstant(ZoneOffset.UTC))

  def withUpdateDate(date: LocalDate): ExportsDeclarationModifier =
    _.copy(updatedDateTime = date.atStartOfDay().toInstant(ZoneOffset.UTC))

  def withUpdateTime(updateDateTime: Instant): ExportsDeclarationModifier = _.copy(updatedDateTime = updateDateTime)

  def withoutTotalNumberOfItems(): ExportsDeclarationModifier = _.copy(totalNumberOfItems = None)

  def withTotalNumberOfItems(totalNumberOfItems: TotalNumberOfItems): ExportsDeclarationModifier =
    _.copy(totalNumberOfItems = Some(totalNumberOfItems))

  def withTotalNumberOfItems(
    totalAmountInvoiced: Option[String] = None,
    exchangeRate: Option[String] = None,
    totalPackage: String = "1"
  ): ExportsDeclarationModifier =
    _.copy(totalNumberOfItems = Some(TotalNumberOfItems(totalAmountInvoiced, exchangeRate, totalPackage)))

  def withAdditionalDeclarationType(decType: AdditionalDeclarationType = AdditionalDeclarationType.STANDARD_FRONTIER): ExportsDeclarationModifier =
    _.copy(additionalDeclarationType = Some(decType))

  def withDispatchLocation(location: String = OutsideEU): ExportsDeclarationModifier =
    _.copy(dispatchLocation = Some(DispatchLocation(location)))

  def withGoodsLocation(goodsLocation: GoodsLocation): ExportsDeclarationModifier = { m =>
    m.copy(locations = m.locations.copy(goodsLocation = Some(goodsLocation)))
  }

  def withOriginationCountry(originationCountry: String = "GB"): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(originationCountry = Some(originationCountry)))

  def withoutOriginationCountry(): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(originationCountry = None))

  def withDestinationCountry(destinationCountry: String = "GB"): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(destinationCountry = Some(destinationCountry)))

  def withoutDestinationCountry(): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(destinationCountry = None))

  def withRoutingQuestion(answer: Boolean = true): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(hasRoutingCountries = Some(answer)))

  def withoutRoutingQuestion(): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(hasRoutingCountries = None))

  def withRoutingCountries(routingCountries: Seq[String] = Seq("FR", "GB")): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(routingCountries = routingCountries))

  def withoutRoutingCountries(routingCountries: Seq[String] = Seq("FR", "GB")): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(routingCountries = Seq.empty))

  def withoutItems(): ExportsDeclarationModifier = _.copy(items = Set.empty)

  def withItem(item: ExportItem = ExportItem(uuid)): ExportsDeclarationModifier =
    m => m.copy(items = m.items + item)

  def withItems(item1: ExportItem, others: ExportItem*): ExportsDeclarationModifier =
    _.copy(items = Set(item1) ++ others)

  def withItems(count: Int): ExportsDeclarationModifier =
    cache => cache.copy(items = cache.items ++ (1 to count).map(_ => ExportItem(id = uuid)).toSet)

  private def uuid: String = UUID.randomUUID().toString

  def withoutExporterDetails(): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(exporterDetails = None))

  def withExporterDetails(eori: Option[String] = None, address: Option[Address] = None): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(exporterDetails = Some(ExporterDetails(EntityDetails(eori, address)))))

  def withoutDeclarantDetails(): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarantDetails = None))

  def withDeclarantDetails(eori: Option[String] = None, address: Option[Address] = None): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarantDetails = Some(DeclarantDetails(EntityDetails(eori, address)))))

  def withoutDeclarationHolders(): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationHoldersData = None))

  def withDeclarationHolders(authorisationTypeCode: Option[String] = None, eori: Option[String] = None): ExportsDeclarationModifier = { cache =>
    val existing: Seq[DeclarationHolder] = cache.parties.declarationHoldersData.map(_.holders).getOrElse(Seq.empty)
    val holdersData = DeclarationHoldersData(existing :+ DeclarationHolder(authorisationTypeCode, eori))
    cache.copy(parties = cache.parties.copy(declarationHoldersData = Some(holdersData)))
  }

  def withDeclarationHolders(holders: DeclarationHoldersData): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationHoldersData = Some(holders)))

  def withDeclarationHolders(holders: DeclarationHolder*): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationHoldersData = Some(DeclarationHoldersData(holders))))

  def withoutDepartureTransport(): ExportsDeclarationModifier = _.copy(departureTransport = None)

  def withDepartureTransport(
    borderModeOfTransportCode: String = "",
    meansOfTransportOnDepartureType: String = "",
    meansOfTransportOnDepartureIDNumber: String = ""
  ): ExportsDeclarationModifier =
    _.copy(
      departureTransport = Some(DepartureTransport(borderModeOfTransportCode, meansOfTransportOnDepartureType, meansOfTransportOnDepartureIDNumber))
    )

  def withConsignmentReferences(ducr: String = DUCR, lrn: String = LRN.value): ExportsDeclarationModifier =
    withConsignmentReferences(ConsignmentReferences(Ducr(ducr), Lrn(lrn)))

  def withConsignmentReferences(consignmentReferences: ConsignmentReferences): ExportsDeclarationModifier =
    _.copy(consignmentReferences = Some(consignmentReferences))

  def withoutConsignmentReference(): ExportsDeclarationModifier = _.copy(consignmentReferences = None)

  def withConsigneeDetails(consigneeDetails: ConsigneeDetails): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(consigneeDetails = Some(consigneeDetails)))

  def withConsigneeDetails(eori: Option[String], address: Option[Address]): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(consigneeDetails = Some(ConsigneeDetails(EntityDetails(eori, address)))))

  def withoutConsigneeDetails(): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(consigneeDetails = None))

  def withDeclarationAdditionalActors(data: DeclarationAdditionalActors*): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationAdditionalActorsData = Some(DeclarationAdditionalActorsData(data))))

  def withDeclarationAdditionalActors(declarationAdditionalActorsData: DeclarationAdditionalActorsData): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationAdditionalActorsData = Some(declarationAdditionalActorsData)))

  def withRepresentativeDetails(representativeDetails: RepresentativeDetails): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(representativeDetails = Some(representativeDetails)))

  def withoutRepresentativeDetails(): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(representativeDetails = None))

  def withPreviousDocumentsData(previousDocumentsData: Option[PreviousDocumentsData]): ExportsDeclarationModifier =
    _.copy(previousDocuments = previousDocumentsData)

  def withPreviousDocuments(previousDocuments: Document*): ExportsDeclarationModifier =
    _.copy(previousDocuments = Some(PreviousDocumentsData(previousDocuments)))

  def withPreviousDocuments(previousDocumentsData: PreviousDocumentsData): ExportsDeclarationModifier =
    _.copy(previousDocuments = Some(previousDocumentsData))

  def withNatureOfTransaction(nature: NatureOfTransaction): ExportsDeclarationModifier =
    _.copy(natureOfTransaction = Some(nature))

  def withNatureOfTransaction(natureType: String): ExportsDeclarationModifier =
    _.copy(natureOfTransaction = Some(NatureOfTransaction(natureType)))

  def withoutBorderTransport(): ExportsDeclarationModifier = _.copy(borderTransport = None)

  def withBorderTransport(details: BorderTransport): ExportsDeclarationModifier =
    _.copy(borderTransport = Some(details))

  def withBorderTransport(
    meansOfTransportCrossingTheBorderNationality: Option[String] = None,
    container: Boolean = false,
    meansOfTransportCrossingTheBorderType: String = "",
    meansOfTransportCrossingTheBorderIDNumber: String = "",
    paymentMethod: Option[String] = None
  ): ExportsDeclarationModifier =
    _.copy(
      borderTransport = Some(
        BorderTransport(
          meansOfTransportCrossingTheBorderNationality = meansOfTransportCrossingTheBorderNationality,
          container = container,
          meansOfTransportCrossingTheBorderType = meansOfTransportCrossingTheBorderType,
          meansOfTransportCrossingTheBorderIDNumber = meansOfTransportCrossingTheBorderIDNumber,
          paymentMethod = paymentMethod
        )
      )
    )

  def withDestinationCountries(
    countryOfDispatch: String = "GB",
    countriesOfRouting: Seq[String] = Seq.empty,
    countryOfDestination: String = "US"
  ): ExportsDeclarationModifier = {
    withOriginationCountry(countryOfDispatch)
    withDestinationCountry(countryOfDestination)
    withRoutingCountries(countriesOfRouting)
  }

  def withoutCarrierDetails(): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(carrierDetails = None))

  def withCarrierDetails(eori: Option[String] = None, address: Option[Address] = None): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(carrierDetails = Some(CarrierDetails(EntityDetails(eori, address)))))

  def withoutWarehouseIdentification(): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(warehouseIdentification = None))

  def withWarehouseIdentification(
    supervisingCustomsOffice: Option[String] = None,
    identificationType: Option[String] = None,
    identificationNumber: Option[String] = None,
    inlandModeOfTransportCode: Option[String] = None
  ): ExportsDeclarationModifier =
    withWarehouseIdentification(WarehouseDetails(supervisingCustomsOffice, identificationType, identificationNumber, inlandModeOfTransportCode))

  def withWarehouseIdentification(warehouseDetails: WarehouseDetails): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(warehouseIdentification = Some(warehouseDetails)))

  def withoutOfficeOfExit(): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(officeOfExit = None))

  def withOfficeOfExit(officeId: String = "", circumstancesCode: Option[String] = None): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(officeOfExit = Some(OfficeOfExit(officeId, circumstancesCode))))

  def withContainerData(data: TransportInformationContainerData): ExportsDeclarationModifier =
    _.copy(containerData = Some(data))

  def withContainerData(data: Container*): ExportsDeclarationModifier =
    cache => cache.copy(containerData = Some(TransportInformationContainerData(cache.containerData.map(_.containers).getOrElse(Seq.empty) ++ data)))

  def withoutContainerData(): ExportsDeclarationModifier = _.copy(containerData = None)

}
