/*
 * Copyright 2022 HM Revenue & Customs
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

import base.ExportsTestData._
import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}
import java.util.UUID

import forms.common.YesNoAnswer.{No, YesNoAnswers}
import forms.common.{Address, Eori, YesNoAnswer}
import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import forms.declaration.carrier.CarrierDetails
import forms.declaration.consignor.ConsignorDetails
import forms.declaration.countries.Country
import forms.declaration.declarationHolder.DeclarationHolder
import forms.declaration.exporter.ExporterDetails
import forms.declaration.officeOfExit.OfficeOfExit
import forms.{Ducr, Lrn, Mrn}
import models.DeclarationStatus.DeclarationStatus
import models.DeclarationType.DeclarationType
import models.declaration._
import models.{DeclarationStatus, DeclarationType, ExportsDeclaration}

//noinspection ScalaStyle
trait ExportsDeclarationBuilder {

  protected type ExportsDeclarationModifier = ExportsDeclaration => ExportsDeclaration
  protected val DUCR = ducr
  protected val LRN = Lrn(lrn)
  protected val MUCR = Mucr(mucr)
  protected val MRN = Mrn(mrn)
  private val modelWithDefaults: ExportsDeclaration = ExportsDeclaration(
    uuid,
    status = DeclarationStatus.COMPLETE,
    createdDateTime = LocalDateTime.of(2019, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC),
    updatedDateTime = LocalDateTime.of(2019, 2, 2, 0, 0, 0).toInstant(ZoneOffset.UTC),
    `type` = DeclarationType.STANDARD,
    sourceId = None
  )

  private def uuid: String = UUID.randomUUID.toString

  def aDeclaration(modifiers: ExportsDeclarationModifier*): ExportsDeclaration =
    modifiers.foldLeft(modelWithDefaults)((current, modifier) => modifier(current))

  def aDeclarationAfter(declaration: ExportsDeclaration, modifiers: ExportsDeclarationModifier*): ExportsDeclaration =
    modifiers.foldLeft(declaration)((current, modifier) => modifier(current))

  def withId(id: String = uuid): ExportsDeclarationModifier = _.copy(id = id)

  // ************************************************* Builders ********************************************************

  def withParentDeclarationId(parentId: String): ExportsDeclarationModifier = _.copy(parentDeclarationId = Some(parentId))

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

  def withTotalNumberOfItems(invoiceAndExchangeRate: InvoiceAndExchangeRate): ExportsDeclarationModifier = { declaration =>
    declaration.copy(totalNumberOfItems =
      Some(
        InvoiceAndPackageTotals(
          totalAmountInvoiced = Some(invoiceAndExchangeRate.totalAmountInvoiced),
          totalAmountInvoicedCurrency = invoiceAndExchangeRate.totalAmountInvoicedCurrency,
          exchangeRate = invoiceAndExchangeRate.exchangeRate,
          agreedExchangeRate = Some(invoiceAndExchangeRate.agreedExchangeRate),
          totalPackage = declaration.totalNumberOfItems.flatMap(_.totalPackage)
        )
      )
    )
  }

  def withTotalNumberOfItems(
    totalAmountInvoiced: Option[String] = None,
    exchangeRate: Option[String] = None,
    totalAmountInvoicedCurrency: Option[String] = None,
    agreedExchangeRate: Option[String] = None
  ): ExportsDeclarationModifier = { declaration =>
    declaration.copy(totalNumberOfItems =
      Some(
        InvoiceAndPackageTotals(
          totalAmountInvoiced = totalAmountInvoiced,
          totalAmountInvoicedCurrency = totalAmountInvoicedCurrency,
          exchangeRate = exchangeRate,
          agreedExchangeRate = agreedExchangeRate,
          totalPackage = declaration.totalNumberOfItems.flatMap(_.totalPackage)
        )
      )
    )
  }

  def withTotalPackageQuantity(quantity: String): ExportsDeclarationModifier = { declaration =>
    declaration.copy(totalNumberOfItems =
      Some(
        InvoiceAndPackageTotals(
          totalAmountInvoiced = declaration.totalNumberOfItems.flatMap(_.totalAmountInvoiced),
          totalAmountInvoicedCurrency = declaration.totalNumberOfItems.flatMap(_.totalAmountInvoicedCurrency),
          exchangeRate = declaration.totalNumberOfItems.flatMap(_.exchangeRate),
          agreedExchangeRate = declaration.totalNumberOfItems.flatMap(_.agreedExchangeRate),
          totalPackage = Some(quantity)
        )
      )
    )
  }

  def withoutTotalPackageQuantity: ExportsDeclarationModifier = { declaration =>
    declaration.copy(totalNumberOfItems = declaration.totalNumberOfItems.map(_.copy(totalPackage = None)))
  }

  def withAdditionalDeclarationType(decType: AdditionalDeclarationType = AdditionalDeclarationType.STANDARD_FRONTIER): ExportsDeclarationModifier =
    _.copy(additionalDeclarationType = Some(decType))

  def withGoodsLocation(locationOfGoods: LocationOfGoods): ExportsDeclarationModifier = { model =>
    model.copy(locations = model.locations.copy(goodsLocation = Some(locationOfGoods.toModel())))
  }

  def withoutGoodsLocation(): ExportsDeclarationModifier = { model =>
    model.copy(locations = model.locations.copy(goodsLocation = None))
  }

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

  def withEntryIntoDeclarantsRecords(isEidr: String = YesNoAnswers.yes): ExportsDeclarationModifier =
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

  def withDeclarationHolders(
    authorisationTypeCode: Option[String] = None,
    eori: Option[Eori] = None,
    eoriSource: Option[EoriSource] = None
  ): ExportsDeclarationModifier = { cache =>
    val holders = cache.declarationHolders
    val holdersData = DeclarationHoldersData(holders :+ DeclarationHolder(authorisationTypeCode, eori, eoriSource))
    cache.copy(parties = cache.parties.copy(declarationHoldersData = Some(holdersData)))
  }

  def withDeclarationHolders(holders: DeclarationHoldersData): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationHoldersData = Some(holders)))

  def withDeclarationHolders(holders: DeclarationHolder*): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationHoldersData = Some(DeclarationHoldersData(holders))))

  def withoutDepartureTransport(): ExportsDeclarationModifier = declaration =>
    declaration.copy(transport = declaration.transport.copy(expressConsignment = No, transportPayment = None))

  def withDepartureTransport(
    borderModeOfTransportCode: ModeOfTransportCode,
    meansOfTransportOnDepartureType: String = "",
    meansOfTransportOnDepartureIDNumber: String = ""
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
  ): ExportsDeclarationModifier = declaration =>
    declaration.copy(transport =
      declaration.transport.copy(
        borderModeOfTransportCode = Some(borderModeOfTransportCode),
        meansOfTransportOnDepartureType = Some(meansOfTransportOnDepartureType),
        meansOfTransportOnDepartureIDNumber = Some(meansOfTransportOnDepartureIDNumber)
      )
    )

  def withConsignmentReferences(ducr: String = DUCR, lrn: String = LRN.value): ExportsDeclarationModifier =
    withConsignmentReferences(ConsignmentReferences(Ducr(ducr), Lrn(lrn)))

  def withConsignmentReferences(consignmentReferences: ConsignmentReferences): ExportsDeclarationModifier =
    _.copy(consignmentReferences = Some(consignmentReferences))

  def withLinkDucrToMucr(linkDucrToMucr: String = YesNoAnswers.yes): ExportsDeclarationModifier =
    _.copy(linkDucrToMucr = Some(YesNoAnswer(linkDucrToMucr)))

  def withMucr(mucr: Mucr = MUCR): ExportsDeclarationModifier = _.copy(mucr = Some(mucr))

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

  def withAuthorisationProcedureCodeChoice(authorisationProcedureCodeChoice: Option[AuthorisationProcedureCodeChoice]): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(authorisationProcedureCodeChoice = authorisationProcedureCodeChoice))

  def withRepresentativeDetails(eori: Option[Eori], statusCode: Option[String], representingOtherAgent: Option[String]): ExportsDeclarationModifier =
    cache =>
      cache.copy(parties =
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

  val withoutNatureOfTransaction: ExportsDeclarationModifier = _.copy(natureOfTransaction = None)

  val withoutBorderTransport: ExportsDeclarationModifier =
    declaration =>
      declaration.copy(transport =
        declaration.transport.copy(meansOfTransportCrossingTheBorderType = None, meansOfTransportCrossingTheBorderIDNumber = None)
      )

  val withoutTransportCountry: ExportsDeclarationModifier =
    declaration => declaration.copy(transport = declaration.transport.copy(transportCrossingTheBorderNationality = None))

  def withBorderModeOfTransportCode(maybeModeOfTransportCode: Option[ModeOfTransportCode]): ExportsDeclarationModifier =
    declaration =>
      declaration.copy(transport = declaration.transport.copy(borderModeOfTransportCode = Some(TransportLeavingTheBorder(maybeModeOfTransportCode))))

  val withoutBorderModeOfTransportCode: ExportsDeclarationModifier =
    declaration => declaration.copy(transport = declaration.transport.copy(borderModeOfTransportCode = None))

  val withoutMeansOfTransportOnDepartureType: ExportsDeclarationModifier =
    declaration =>
      declaration.copy(transport = declaration.transport.copy(meansOfTransportOnDepartureType = None, meansOfTransportOnDepartureIDNumber = None))

  val withoutTransportPayment: ExportsDeclarationModifier =
    declaration => declaration.copy(transport = declaration.transport.copy(expressConsignment = No, transportPayment = None))

  def withBorderTransport(details: BorderTransport): ExportsDeclarationModifier = declaration =>
    declaration.copy(transport =
      declaration.transport.copy(
        meansOfTransportCrossingTheBorderType = Some(details.meansOfTransportCrossingTheBorderType),
        meansOfTransportCrossingTheBorderIDNumber = Some(details.meansOfTransportCrossingTheBorderIDNumber)
      )
    )

  def withBorderTransport(
    meansOfTransportCrossingTheBorderType: String = "20",
    meansOfTransportCrossingTheBorderIDNumber: String = "123"
  ): ExportsDeclarationModifier =
    withBorderTransport(BorderTransport(meansOfTransportCrossingTheBorderType, meansOfTransportCrossingTheBorderIDNumber))

  def withTransportCountry(countryName: Option[String]): ExportsDeclarationModifier =
    declaration =>
      declaration.copy(transport = declaration.transport.copy(transportCrossingTheBorderNationality = Some(TransportCountry(countryName))))

  def withDestinationCountries(
    countriesOfRouting: Seq[Country] = Seq.empty,
    countryOfDestination: Country = Country(Some("US"))
  ): ExportsDeclarationModifier = {
    withDestinationCountry(countryOfDestination)
    withRoutingCountries(countriesOfRouting)
  }

  def withoutCarrierDetails(): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(carrierDetails = None))

  def withCarrierDetails(eori: Option[Eori] = None, address: Option[Address] = None): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(carrierDetails = Some(CarrierDetails(EntityDetails(eori, address)))))

  def withoutWarehouseIdentification(): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(warehouseIdentification = None))

  def withWarehouseIdentification(warehouseIdentification: Option[WarehouseIdentification] = None): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(warehouseIdentification = warehouseIdentification))

  def withSupervisingCustomsOffice(supervisingCustomsOffice: Option[SupervisingCustomsOffice]): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(supervisingCustomsOffice = supervisingCustomsOffice))

  def withoutSupervisingCustomsOffice(): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(supervisingCustomsOffice = None))

  def withInlandOrBorder(inlandOrBorder: Option[InlandOrBorder]): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(inlandOrBorder = inlandOrBorder))

  def withoutInlandOrBorder: ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(inlandOrBorder = None))

  def withInlandModeOfTransportCode(modeOfTransportCode: ModeOfTransportCode): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(inlandModeOfTransportCode = Some(InlandModeOfTransportCode(Some(modeOfTransportCode)))))

  def withoutInlandModeOfTransportCode(): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(inlandModeOfTransportCode = None))

  def withoutOfficeOfExit(): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(officeOfExit = None))

  def withOfficeOfExit(officeId: String = ""): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(officeOfExit = Some(OfficeOfExit(officeId))))

  def withTransportPayment(transportPayment: Option[TransportPayment]): ExportsDeclarationModifier =
    cache =>
      cache.copy(transport =
        cache.transport.copy(
          expressConsignment = Some(YesNoAnswer(transportPayment.fold(YesNoAnswers.no)(_ => YesNoAnswers.yes))),
          transportPayment = transportPayment
        )
      )

  def withContainerData(data: Container): ExportsDeclarationModifier = withContainerData(Seq(data))

  def withContainerData(data: Seq[Container]): ExportsDeclarationModifier =
    cache => cache.copy(transport = cache.transport.copy(containers = Some(data)))

  def withoutContainerData(): ExportsDeclarationModifier =
    cache => cache.copy(transport = cache.transport.copy(containers = None))

  def withIsExs(data: IsExs = IsExs("Yes")): ExportsDeclarationModifier = cache => cache.copy(parties = cache.parties.copy(isExs = Some(data)))

  def withoutIsExs(): ExportsDeclarationModifier = cache => cache.copy(parties = cache.parties.copy(isExs = None))
}
