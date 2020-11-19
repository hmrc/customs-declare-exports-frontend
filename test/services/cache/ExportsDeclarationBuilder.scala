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

package services.cache

import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}
import java.util.UUID

import forms.common.{Address, Eori, YesNoAnswer}
import forms.declaration.DispatchLocation.AllowedDispatchLocations.OutsideEU
import forms.declaration.{DeclarationHolder, _}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import forms.declaration.carrier.CarrierDetails
import forms.declaration.consignor.ConsignorDetails
import forms.declaration.countries.Country
import forms.declaration.exporter.ExporterDetails
import forms.declaration.officeOfExit.{AllowedUKOfficeOfExitAnswers, OfficeOfExit}
import forms.{Ducr, Lrn}
import models.DeclarationStatus.DeclarationStatus
import models.DeclarationType.DeclarationType
import models.declaration._
import models.{DeclarationStatus, DeclarationType, ExportsDeclaration}

//noinspection ScalaStyle
trait ExportsDeclarationBuilder {

  protected type ExportsDeclarationModifier = ExportsDeclaration => ExportsDeclaration
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

  def withTotalNumberOfItems(totalAmountInvoiced: Option[String] = None, exchangeRate: Option[String] = None): ExportsDeclarationModifier =
    _.copy(totalNumberOfItems = Some(TotalNumberOfItems(exchangeRate, totalAmountInvoiced)))

  def withTotalPackageQuantity(quantity: String): ExportsDeclarationModifier =
    _.copy(totalPackageQuantity = Some(TotalPackageQuantity(Some(quantity))))

  def withoutTotalPackageQuantity: ExportsDeclarationModifier = _.copy(totalPackageQuantity = None)

  def withAdditionalDeclarationType(decType: AdditionalDeclarationType = AdditionalDeclarationType.STANDARD_FRONTIER): ExportsDeclarationModifier =
    _.copy(additionalDeclarationType = Some(decType))

  def withDispatchLocation(location: String = OutsideEU): ExportsDeclarationModifier =
    _.copy(dispatchLocation = Some(DispatchLocation(location)))

  def withGoodsLocation(goodsLocationForm: GoodsLocationForm): ExportsDeclarationModifier = { model =>
    model.copy(locations = model.locations.copy(goodsLocation = Some(goodsLocationForm.toModel())))
  }

  def withoutGoodsLocation(): ExportsDeclarationModifier = { model =>
    model.copy(locations = model.locations.copy(goodsLocation = None))
  }

  def withOriginationCountry(originationCountry: Country = Country(Some("GB"))): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(originationCountry = Some(originationCountry)))

  def withoutOriginationCountry(): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(originationCountry = None))

  def withDestinationCountry(destinationCountry: Country = Country(Some("GB"))): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(destinationCountry = Some(destinationCountry)))

  def withoutDestinationCountry(): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(destinationCountry = None))

  def withRoutingQuestion(answer: Boolean = true): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(hasRoutingCountries = Some(answer)))

  def withoutRoutingQuestion(): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(hasRoutingCountries = None))

  def withRoutingCountries(routingCountries: Seq[Country] = Seq(Country(Some("FR")), Country(Some("GB")))): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(routingCountries = routingCountries))

  def withoutRoutingCountries(): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(routingCountries = Seq.empty))

  def withoutItems(): ExportsDeclarationModifier = _.copy(items = Seq.empty)

  def withItem(item: ExportItem = ExportItem(uuid)): ExportsDeclarationModifier =
    m => m.copy(items = m.items :+ item)

  def withItems(item1: ExportItem, others: ExportItem*): ExportsDeclarationModifier =
    _.copy(items = Seq(item1) ++ others)

  def withItems(count: Int): ExportsDeclarationModifier =
    cache => cache.copy(items = cache.items ++ (1 to count).map(index => ExportItem(id = uuid, sequenceId = index)).toSet)

  private def uuid: String = UUID.randomUUID().toString

  def withEntryIntoDeclarantsRecords(isEidr: String = "Yes"): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(isEntryIntoDeclarantsRecords = Some(YesNoAnswer(isEidr))))

  def withPersonPresentingGoodsDetails(eori: Option[Eori] = None): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(personPresentingGoodsDetails = eori.map(PersonPresentingGoodsDetails(_))))

  def withoutExporterDetails(): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(exporterDetails = None))

  def withExporterDetails(eori: Option[Eori] = None, address: Option[Address] = None): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(exporterDetails = Some(ExporterDetails(EntityDetails(eori, address)))))

  def withoutDeclarantDetails(): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarantDetails = None))

  def withDeclarantDetails(eori: Option[Eori] = None, address: Option[Address] = None): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarantDetails = Some(DeclarantDetails(EntityDetails(eori, address)))))

  def withDeclarantIsExporter(isExporter: String = "Yes"): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarantIsExporter = Some(DeclarantIsExporter(isExporter))))

  def withoutDeclarationHolders(): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationHoldersData = None))

  def withDeclarationHolders(authorisationTypeCode: Option[String] = None, eori: Option[Eori] = None): ExportsDeclarationModifier = { cache =>
    val existing: Seq[DeclarationHolder] = cache.parties.declarationHoldersData.map(_.holders).getOrElse(Seq.empty)
    val holdersData = DeclarationHoldersData(existing :+ DeclarationHolder(authorisationTypeCode, eori))
    cache.copy(parties = cache.parties.copy(declarationHoldersData = Some(holdersData)))
  }

  def withDeclarationHolders(holders: DeclarationHoldersData): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationHoldersData = Some(holders)))

  def withDeclarationHolders(holders: DeclarationHolder*): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationHoldersData = Some(DeclarationHoldersData(holders))))

  def withoutDepartureTransport(): ExportsDeclarationModifier = declaration => {
    declaration.copy(transport = declaration.transport.copy(transportPayment = None))
  }

  def withDepartureTransport(
    borderModeOfTransportCode: ModeOfTransportCode,
    meansOfTransportOnDepartureType: String,
    meansOfTransportOnDepartureIDNumber: String
  ): ExportsDeclarationModifier =
    withDepartureTransport(
      TransportLeavingTheBorder(Some(borderModeOfTransportCode)),
      meansOfTransportOnDepartureType,
      meansOfTransportOnDepartureIDNumber
    )

  def withDepartureTransport(
    borderModeOfTransportCode: TransportLeavingTheBorder,
    meansOfTransportOnDepartureType: String,
    meansOfTransportOnDepartureIDNumber: String
  ): ExportsDeclarationModifier = declaration => {
    declaration.copy(
      transport = declaration.transport.copy(
        borderModeOfTransportCode = Some(borderModeOfTransportCode),
        meansOfTransportOnDepartureType = Some(meansOfTransportOnDepartureType),
        meansOfTransportOnDepartureIDNumber = Some(meansOfTransportOnDepartureIDNumber)
      )
    )
  }

  def withConsignmentReferences(ducr: String = DUCR, lrn: String = LRN.value): ExportsDeclarationModifier =
    withConsignmentReferences(ConsignmentReferences(Ducr(ducr), Lrn(lrn)))

  def withConsignmentReferences(consignmentReferences: ConsignmentReferences): ExportsDeclarationModifier =
    _.copy(consignmentReferences = Some(consignmentReferences))

  def withoutConsignmentReference(): ExportsDeclarationModifier = _.copy(consignmentReferences = None)

  def withConsigneeDetails(consigneeDetails: ConsigneeDetails): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(consigneeDetails = Some(consigneeDetails)))

  def withConsigneeDetails(eori: Option[Eori], address: Option[Address]): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(consigneeDetails = Some(ConsigneeDetails(EntityDetails(eori, address)))))

  def withConsignorDetails(consignorDetails: ConsignorDetails): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(consignorDetails = Some(consignorDetails)))

  def withConsignorDetails(eori: Option[Eori], address: Option[Address]): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(consignorDetails = Some(ConsignorDetails(EntityDetails(eori, address)))))

  def withoutConsigneeDetails(): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(consigneeDetails = None))

  def withDeclarationAdditionalActors(data: DeclarationAdditionalActors*): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationAdditionalActorsData = Some(DeclarationAdditionalActorsData(data))))

  def withDeclarationAdditionalActors(declarationAdditionalActorsData: DeclarationAdditionalActorsData): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationAdditionalActorsData = Some(declarationAdditionalActorsData)))

  def withRepresentativeDetails(eori: Option[Eori], statusCode: Option[String], representingOtherAgent: Option[String]): ExportsDeclarationModifier =
    cache =>
      cache.copy(
        parties =
          cache.parties.copy(representativeDetails = Some(RepresentativeDetails(Some(EntityDetails(eori, None)), statusCode, representingOtherAgent)))
    )

  def withRepresentativeDetails(representativeDetails: RepresentativeDetails): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(representativeDetails = Some(representativeDetails)))

  def withoutRepresentativeDetails(): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(representativeDetails = None))

  def withPreviousDocumentsData(previousDocumentsData: Option[PreviousDocumentsData]): ExportsDeclarationModifier =
    _.copy(previousDocuments = previousDocumentsData)

  def withPreviousDocuments(previousDocuments: Document*): ExportsDeclarationModifier =
    _.copy(previousDocuments = Some(PreviousDocumentsData(previousDocuments)))

  def withoutPreviousDocuments(): ExportsDeclarationModifier =
    _.copy(previousDocuments = None)

  def withPreviousDocuments(previousDocumentsData: PreviousDocumentsData): ExportsDeclarationModifier =
    _.copy(previousDocuments = Some(previousDocumentsData))

  def withNatureOfTransaction(nature: NatureOfTransaction): ExportsDeclarationModifier =
    _.copy(natureOfTransaction = Some(nature))

  def withNatureOfTransaction(natureType: String): ExportsDeclarationModifier =
    _.copy(natureOfTransaction = Some(NatureOfTransaction(natureType)))

  def withoutNatureOfTransaction(): ExportsDeclarationModifier =
    _.copy(natureOfTransaction = None)

  def withoutBorderTransport(): ExportsDeclarationModifier = declaration => {
    declaration.copy(
      transport = declaration.transport.copy(
        meansOfTransportCrossingTheBorderNationality = None,
        meansOfTransportCrossingTheBorderType = None,
        meansOfTransportCrossingTheBorderIDNumber = None
      )
    )
  }

  def withoutBorderModeOfTransportCode(): ExportsDeclarationModifier = declaration => {
    declaration.copy(transport = declaration.transport.copy(borderModeOfTransportCode = None))
  }

  def withoutMeansOfTransportOnDepartureType(): ExportsDeclarationModifier = declaration => {
    declaration.copy(transport = declaration.transport.copy(meansOfTransportOnDepartureType = None, meansOfTransportOnDepartureIDNumber = None))
  }

  def withoutTransportPayment(): ExportsDeclarationModifier = declaration => {
    declaration.copy(transport = declaration.transport.copy(transportPayment = None))
  }

  def withBorderTransport(details: BorderTransport): ExportsDeclarationModifier = declaration => {
    declaration.copy(
      transport = declaration.transport.copy(
        meansOfTransportCrossingTheBorderNationality = details.meansOfTransportCrossingTheBorderNationality,
        meansOfTransportCrossingTheBorderType = Some(details.meansOfTransportCrossingTheBorderType),
        meansOfTransportCrossingTheBorderIDNumber = Some(details.meansOfTransportCrossingTheBorderIDNumber)
      )
    )
  }

  def withBorderTransport(
    meansOfTransportCrossingTheBorderNationality: Option[String] = None,
    meansOfTransportCrossingTheBorderType: String = "20",
    meansOfTransportCrossingTheBorderIDNumber: String = "123"
  ): ExportsDeclarationModifier =
    withBorderTransport(
      BorderTransport(meansOfTransportCrossingTheBorderNationality, meansOfTransportCrossingTheBorderType, meansOfTransportCrossingTheBorderIDNumber)
    )

  def withDestinationCountries(
    countryOfDispatch: Country = Country(Some("GB")),
    countriesOfRouting: Seq[Country] = Seq.empty,
    countryOfDestination: Country = Country(Some("US"))
  ): ExportsDeclarationModifier = {
    withOriginationCountry(countryOfDispatch)
    withDestinationCountry(countryOfDestination)
    withRoutingCountries(countriesOfRouting)
  }

  def withoutCarrierDetails(): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(carrierDetails = None))

  def withCarrierDetails(eori: Option[Eori] = None, address: Option[Address] = None): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(carrierDetails = Some(CarrierDetails(EntityDetails(eori, address)))))

  def withoutWarehouseIdentification(): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(warehouseIdentification = None))

  def withSupervisingCustomsOffice(supervisingCustomsOffice: Option[SupervisingCustomsOffice]): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(supervisingCustomsOffice = supervisingCustomsOffice))

  def withoutSupervisingCustomsOffice(): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(supervisingCustomsOffice = None))

  def withInlandModeOfTransportCode(inlandModeOfTransportCode: Option[InlandModeOfTransportCode]): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(inlandModeOfTransportCode = inlandModeOfTransportCode))

  def withoutInlandModeOfTransportCode(): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(inlandModeOfTransportCode = None))

  def withWarehouseIdentification(warehouseIdentification: Option[WarehouseIdentification] = None): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(warehouseIdentification = warehouseIdentification))

  def withoutOfficeOfExit(): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(officeOfExit = None))

  def withOfficeOfExit(officeId: String = "", isUkOfficeOfExit: String = AllowedUKOfficeOfExitAnswers.yes): ExportsDeclarationModifier =
    withOptionalOfficeOfExit(Some(officeId), Some(isUkOfficeOfExit))

  def withOptionalOfficeOfExit(
    officeId: Option[String] = None,
    isUkOfficeOfExit: Option[String] = Some(AllowedUKOfficeOfExitAnswers.yes)
  ): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(officeOfExit = Some(OfficeOfExit(officeId, isUkOfficeOfExit))))

  def withOfficeOfExitOutsideUK(officeId: String = ""): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(officeOfExit = Some(OfficeOfExit(Some(officeId), Some(AllowedUKOfficeOfExitAnswers.no)))))

  def withTransportPayment(data: Option[TransportPayment]): ExportsDeclarationModifier =
    cache => {
      cache.copy(transport = cache.transport.copy(transportPayment = data))
    }

  def withContainerData(data: Container): ExportsDeclarationModifier = withContainerData(Seq(data))

  def withContainerData(data: Seq[Container]): ExportsDeclarationModifier =
    cache => {
      cache.copy(transport = cache.transport.copy(containers = Some(data)))
    }

  def withoutContainerData(): ExportsDeclarationModifier =
    cache => {
      cache.copy(transport = cache.transport.copy(containers = None))
    }

  def withIsExs(data: IsExs = IsExs("Yes")): ExportsDeclarationModifier = cache => cache.copy(parties = cache.parties.copy(isExs = Some(data)))

  def withoutIsExs(): ExportsDeclarationModifier = cache => cache.copy(parties = cache.parties.copy(isExs = None))
}
