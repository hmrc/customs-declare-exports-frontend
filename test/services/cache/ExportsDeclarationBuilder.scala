/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.common.YesNoAnswer.{No, YesNoAnswers}
import forms.common.{Address, Eori, YesNoAnswer}
import forms.declaration._
import forms.section1.additionaldeclarationtype.AdditionalDeclarationType.{AdditionalDeclarationType, STANDARD_FRONTIER}
import forms.declaration.countries.Country
import forms.declaration.officeOfExit.OfficeOfExit
import forms.section1.{ConsignmentReferences, DeclarantDetails, Mucr}
import forms.section2.authorisationHolder.AuthorisationHolder
import forms.section2.carrier.CarrierDetails
import forms.section2.{AdditionalActor, AuthorisationProcedureCodeChoice, ConsigneeDetails, DeclarantIsExporter, IsExs, PersonPresentingGoodsDetails}
import forms.section2.consignor.ConsignorDetails
import forms.section2.exporter.ExporterDetails
import forms.{Ducr, Lrn, Mrn}
import models.DeclarationType.DeclarationType
import models.declaration.DeclarationStatus.DeclarationStatus
import models.declaration._
import models.declaration.submissions.EnhancedStatus.EnhancedStatus
import models.{DeclarationMeta, DeclarationType, ExportsDeclaration}

import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}
import java.util.UUID

//noinspection ScalaStyle
trait ExportsDeclarationBuilder {

  protected type ExportsDeclarationModifier = ExportsDeclaration => ExportsDeclaration
  protected val DUCR = ducr
  protected val LRN = Lrn(lrn)
  protected val MUCR = Mucr(mucr)
  protected val MRN = Mrn(mrn)
  private val modelWithDefaults: ExportsDeclaration = ExportsDeclaration(
    uuid,
    declarationMeta = DeclarationMeta(
      status = DeclarationStatus.COMPLETE,
      createdDateTime = LocalDateTime.of(2019, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC),
      updatedDateTime = LocalDateTime.of(2019, 2, 2, 0, 0, 0).toInstant(ZoneOffset.UTC)
    ),
    `type` = DeclarationType.STANDARD,
    eori = ""
  )

  private def uuid: String = UUID.randomUUID.toString

  def aDeclaration(modifiers: ExportsDeclarationModifier*): ExportsDeclaration =
    modifiers.foldLeft(modelWithDefaults)((current, modifier) => modifier(current))

  def aDeclarationAfter(declaration: ExportsDeclaration, modifiers: ExportsDeclarationModifier*): ExportsDeclaration =
    modifiers.foldLeft(declaration)((current, modifier) => modifier(current))

  def withId(id: String = uuid): ExportsDeclarationModifier = _.copy(id = id)

  def withAssociatedSubmissionId(submissionId: Option[String]): ExportsDeclarationModifier =
    cache => cache.copy(declarationMeta = cache.declarationMeta.copy(associatedSubmissionId = submissionId))

  // ************************************************* Builders ********************************************************

  def withReadyForSubmission(readyForSubmission: Boolean = true): ExportsDeclarationModifier =
    declaration => declaration.copy(declarationMeta = declaration.declarationMeta.copy(readyForSubmission = Some(readyForSubmission)))

  def withSummaryWasVisited(summaryWasVisited: Boolean = true): ExportsDeclarationModifier =
    declaration => declaration.copy(declarationMeta = declaration.declarationMeta.copy(summaryWasVisited = Some(summaryWasVisited)))

  def withParentDeclarationId(parentId: String): ExportsDeclarationModifier =
    declaration => declaration.copy(declarationMeta = declaration.declarationMeta.copy(parentDeclarationId = Some(parentId)))

  def withParentDeclarationEnhancedStatus(status: EnhancedStatus): ExportsDeclarationModifier =
    declaration => declaration.copy(declarationMeta = declaration.declarationMeta.copy(parentDeclarationEnhancedStatus = Some(status)))

  def withStatus(status: DeclarationStatus): ExportsDeclarationModifier =
    declaration => declaration.copy(declarationMeta = declaration.declarationMeta.copy(status = status))

  def withType(`type`: DeclarationType): ExportsDeclarationModifier = _.copy(`type` = `type`)

  def withCreatedDate(date: LocalDateTime): ExportsDeclarationModifier =
    declaration => declaration.copy(declarationMeta = declaration.declarationMeta.copy(createdDateTime = date.toInstant(ZoneOffset.UTC)))

  def withCreatedDate(date: LocalDate): ExportsDeclarationModifier =
    declaration =>
      declaration.copy(declarationMeta = declaration.declarationMeta.copy(createdDateTime = date.atStartOfDay().toInstant(ZoneOffset.UTC)))

  def withCreatedTime(createdDateTime: Instant): ExportsDeclarationModifier =
    declaration => declaration.copy(declarationMeta = declaration.declarationMeta.copy(createdDateTime = createdDateTime))

  def withUpdateDate(date: LocalDateTime): ExportsDeclarationModifier =
    declaration => declaration.copy(declarationMeta = declaration.declarationMeta.copy(updatedDateTime = date.toInstant(ZoneOffset.UTC)))

  def withUpdateDate(date: LocalDate): ExportsDeclarationModifier =
    declaration =>
      declaration.copy(declarationMeta = declaration.declarationMeta.copy(updatedDateTime = date.atStartOfDay().toInstant(ZoneOffset.UTC)))

  def withUpdateTime(updateDateTime: Instant): ExportsDeclarationModifier =
    declaration => declaration.copy(declarationMeta = declaration.declarationMeta.copy(updatedDateTime = updateDateTime))

  val withoutTotalNumberOfItems: ExportsDeclarationModifier = _.copy(totalNumberOfItems = None)

  def withTotalNumberOfItems(invoiceAndExchangeRate: InvoiceAndExchangeRate): ExportsDeclarationModifier =
    declaration =>
      declaration.copy(totalNumberOfItems =
        Some(
          InvoiceAndPackageTotals(
            totalAmountInvoiced = invoiceAndExchangeRate.totalAmountInvoiced,
            totalAmountInvoicedCurrency = invoiceAndExchangeRate.totalAmountInvoicedCurrency,
            exchangeRate = invoiceAndExchangeRate.exchangeRate,
            agreedExchangeRate = Some(invoiceAndExchangeRate.agreedExchangeRate),
            totalPackage = declaration.totalNumberOfItems.flatMap(_.totalPackage)
          )
        )
      )

  def withTotalNumberOfItems(
    totalAmountInvoiced: Option[String] = None,
    exchangeRate: Option[String] = None,
    totalAmountInvoicedCurrency: Option[String] = None,
    agreedExchangeRate: Option[String] = None
  ): ExportsDeclarationModifier =
    declaration =>
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

  def withTotalPackageQuantity(quantity: String): ExportsDeclarationModifier =
    declaration =>
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

  val withoutTotalPackageQuantity: ExportsDeclarationModifier =
    declaration => declaration.copy(totalNumberOfItems = declaration.totalNumberOfItems.map(_.copy(totalPackage = None)))

  def withAdditionalDeclarationType(decType: AdditionalDeclarationType = STANDARD_FRONTIER): ExportsDeclarationModifier =
    _.copy(additionalDeclarationType = Some(decType))

  def withGoodsLocation(locationOfGoods: LocationOfGoods): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(goodsLocation = Some(locationOfGoods.toModel)))

  val withoutGoodsLocation: ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(goodsLocation = None))

  def withDestinationCountry(destinationCountry: Country = Country(Some("US"))): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(destinationCountry = Some(destinationCountry)))

  val withoutDestinationCountry: ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(destinationCountry = None))

  def withRoutingQuestion(answer: Boolean = true): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(hasRoutingCountries = Some(answer)))

  val withoutRoutingQuestion: ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(hasRoutingCountries = None))

  def withRoutingCountries(countries: Seq[Country] = Seq(Country(Some("FR")), Country(Some("GB")))): ExportsDeclarationModifier = {
    require(countries.nonEmpty)
    model => {
      val meta = model.declarationMeta
      val routingCountries = countries.zipWithIndex.map { case (country, ix) => RoutingCountry(ix + 1, country) }
      val seqIdKey = implicitly[EsoKeyProvider[RoutingCountry]].seqIdKey
      model.copy(
        declarationMeta = meta.copy(maxSequenceIds = meta.maxSequenceIds + (seqIdKey -> routingCountries.last.sequenceId)),
        locations = model.locations.copy(routingCountries = routingCountries, hasRoutingCountries = Some(true))
      )
    }
  }

  def withRoutingCountriesWithSeqId(routingCountries: Seq[RoutingCountry]): ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(routingCountries = routingCountries, hasRoutingCountries = Some(true)))

  val withoutRoutingCountries: ExportsDeclarationModifier =
    model => model.copy(locations = model.locations.copy(routingCountries = Seq.empty))

  val withoutItems: ExportsDeclarationModifier = _.copy(items = Seq.empty)

  def withItem(item: ExportItem = ExportItem(uuid)): ExportsDeclarationModifier =
    updateItems(item)

  def withItems(item1: ExportItem, others: ExportItem*): ExportsDeclarationModifier =
    updateItems(item1 +: others: _*)

  def withItems(count: Int): ExportsDeclarationModifier =
    updateItems((1 to count).map(index => ExportItem(id = uuid, sequenceId = index)): _*)

  private def updateItems(itemsToAdd: ExportItem*): ExportsDeclarationModifier =
    declaration => {
      val meta = declaration.declarationMeta
      val items = declaration.items ++ itemsToAdd
      val seqIdKey = implicitly[EsoKeyProvider[ExportItem]].seqIdKey
      declaration.copy(declarationMeta = meta.copy(maxSequenceIds = meta.maxSequenceIds + (seqIdKey -> items.last.sequenceId)), items = items)
    }

  def withEntryIntoDeclarantsRecords(isEidr: String = YesNoAnswers.yes): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(isEntryIntoDeclarantsRecords = Some(YesNoAnswer(isEidr))))

  val withoutEntryIntoDeclarantsRecords: ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(isEntryIntoDeclarantsRecords = None))

  def withPersonPresentingGoods(eori: Option[Eori] = None): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(personPresentingGoodsDetails = eori.map(PersonPresentingGoodsDetails(_))))

  val withoutPersonPresentingGoods: ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(personPresentingGoodsDetails = None))

  val withoutExporterDetails: ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(exporterDetails = None))

  def withExporterDetails(eori: Option[Eori] = None, address: Option[Address] = None): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(exporterDetails = Some(ExporterDetails(EntityDetails(eori, address)))))

  val withoutDeclarantDetails: ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarantDetails = None))

  def withDeclarantDetails(eori: Option[Eori] = None, address: Option[Address] = None): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarantDetails = Some(DeclarantDetails(EntityDetails(eori, address)))))

  def withDeclarantIsExporter(isExporter: String = "Yes"): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarantIsExporter = Some(DeclarantIsExporter(isExporter))))

  val withoutDeclarantIsExporter: ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarantIsExporter = None))

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

  val withoutDepartureTransport: ExportsDeclarationModifier = declaration => declaration.updateDepartureTransport(DepartureTransport(None, None))

  def withConsignmentReferences(ducr: String = DUCR, lrn: String = LRN.lrn): ExportsDeclarationModifier =
    withConsignmentReferences(ConsignmentReferences(Some(Ducr(ducr)), Some(Lrn(lrn))))

  def withConsignmentReferences(consignmentReferences: ConsignmentReferences): ExportsDeclarationModifier =
    _.copy(consignmentReferences = Some(consignmentReferences))

  def withLinkDucrToMucr(linkDucrToMucr: String = YesNoAnswers.yes): ExportsDeclarationModifier =
    _.copy(linkDucrToMucr = Some(YesNoAnswer(linkDucrToMucr)))

  def withMucr(mucr: Mucr = MUCR): ExportsDeclarationModifier = _.copy(mucr = Some(mucr))

  val withoutConsignmentReference: ExportsDeclarationModifier = _.copy(consignmentReferences = None)

  def withRepresentativeDetails(eori: Option[Eori], statusCode: Option[String], representingOtherAgent: Option[String]): ExportsDeclarationModifier =
    cache =>
      cache.copy(parties =
        cache.parties.copy(representativeDetails = Some(RepresentativeDetails(Some(EntityDetails(eori, None)), statusCode, representingOtherAgent)))
      )

  def withRepresentativeDetails(representativeDetails: RepresentativeDetails): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(representativeDetails = Some(representativeDetails)))

  val withoutRepresentativeDetails: ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(representativeDetails = None))

  def withConsigneeDetails(consigneeDetails: ConsigneeDetails): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(consigneeDetails = Some(consigneeDetails)))

  def withConsigneeDetails(eori: Option[Eori], address: Option[Address]): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(consigneeDetails = Some(ConsigneeDetails(EntityDetails(eori, address)))))

  val withoutConsigneeDetails: ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(consigneeDetails = None))

  def withConsignorDetails(consignorDetails: ConsignorDetails): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(consignorDetails = Some(consignorDetails)))

  def withConsignorDetails(eori: Option[Eori], address: Option[Address]): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(consignorDetails = Some(ConsignorDetails(EntityDetails(eori, address)))))

  val withoutConsignorDetails: ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(consignorDetails = None))

  def withAdditionalActors(data: AdditionalActor*): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationAdditionalActorsData = Some(AdditionalActors(data))))

  def withAdditionalActors(declarationAdditionalActorsData: AdditionalActors): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationAdditionalActorsData = Some(declarationAdditionalActorsData)))

  val withoutAdditionalActors: ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationAdditionalActorsData = None))

  def withAuthorisationHolders(
    authorisationTypeCode: Option[String] = None,
    eori: Option[Eori] = None,
    eoriSource: Option[EoriSource] = None
  ): ExportsDeclarationModifier = { cache =>
    val holders = cache.authorisationHolders
    val holdersData = AuthorisationHolders(holders :+ AuthorisationHolder(authorisationTypeCode, eori, eoriSource))
    cache.copy(parties = cache.parties.copy(declarationHoldersData = Some(holdersData)))
  }

  def withAuthorisationHolders(holders: AuthorisationHolders): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationHoldersData = Some(holders)))

  def withAuthorisationHolders(holders: AuthorisationHolder*): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationHoldersData = Some(AuthorisationHolders(holders))))

  val withoutAuthorisationHolders: ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationHoldersData = None))

  def withAuthorisationProcedureCodeChoice(authorisationProcedureCodeChoice: Option[AuthorisationProcedureCodeChoice]): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(authorisationProcedureCodeChoice = authorisationProcedureCodeChoice))

  def withPreviousDocumentsData(previousDocumentsData: Option[PreviousDocumentsData]): ExportsDeclarationModifier =
    _.copy(previousDocuments = previousDocumentsData)

  def withPreviousDocuments(previousDocuments: Document*): ExportsDeclarationModifier =
    _.copy(previousDocuments = Some(PreviousDocumentsData(previousDocuments)))

  val withoutPreviousDocuments: ExportsDeclarationModifier =
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

  def withTransportLeavingTheBorder(maybeModeOfTransportCode: Option[ModeOfTransportCode]): ExportsDeclarationModifier =
    declaration =>
      declaration.copy(transport = declaration.transport.copy(borderModeOfTransportCode = Some(TransportLeavingTheBorder(maybeModeOfTransportCode))))

  val withoutTransportLeavingTheBorder: ExportsDeclarationModifier =
    declaration => declaration.copy(transport = declaration.transport.copy(borderModeOfTransportCode = None))

  val withoutMeansOfTransportOnDepartureType: ExportsDeclarationModifier =
    declaration =>
      declaration.copy(transport = declaration.transport.copy(meansOfTransportOnDepartureType = None, meansOfTransportOnDepartureIDNumber = None))

  val withoutTransportPayment: ExportsDeclarationModifier =
    declaration => declaration.copy(transport = declaration.transport.copy(expressConsignment = No, transportPayment = None))

  def withBorderTransport(details: BorderTransport): ExportsDeclarationModifier =
    declaration =>
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

  def withTransportCountry(countryCode: Option[String]): ExportsDeclarationModifier =
    declaration =>
      declaration.copy(transport = declaration.transport.copy(transportCrossingTheBorderNationality = Some(TransportCountry(countryCode))))

  val withoutCarrierDetails: ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(carrierDetails = None))

  def withCarrierDetails(eori: Option[Eori] = None, address: Option[Address] = None): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(carrierDetails = Some(CarrierDetails(EntityDetails(eori, address)))))

  val withoutWarehouseIdentification: ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(warehouseIdentification = None))

  def withWarehouseIdentification(warehouseIdentification: Option[WarehouseIdentification] = None): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(warehouseIdentification = warehouseIdentification))

  def withSupervisingCustomsOffice(supervisingCustomsOffice: Option[SupervisingCustomsOffice]): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(supervisingCustomsOffice = supervisingCustomsOffice))

  val withoutSupervisingCustomsOffice: ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(supervisingCustomsOffice = None))

  def withInlandOrBorder(inlandOrBorder: Option[InlandOrBorder]): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(inlandOrBorder = inlandOrBorder))

  val withoutInlandOrBorder: ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(inlandOrBorder = None))

  def withInlandModeOfTransportCode(modeOfTransportCode: ModeOfTransportCode): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(inlandModeOfTransportCode = Some(InlandModeOfTransportCode(Some(modeOfTransportCode)))))

  val withoutInlandModeOfTransportCode: ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(inlandModeOfTransportCode = None))

  val withoutOfficeOfExit: ExportsDeclarationModifier =
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

  def withContainerData(containersToAdd: Container*): ExportsDeclarationModifier =
    declaration => {
      val meta = declaration.declarationMeta
      val transport = declaration.transport

      val containers = transport.containers.fold(containersToAdd)(_ ++ containersToAdd)

      val containerSeqId = containers.lastOption.fold(-1)(_.sequenceId)
      val sealSeqId = containers.foldLeft(-1)((seqId: Int, c: Container) => c.seals.lastOption.fold(seqId)(_.sequenceId))

      declaration.copy(
        declarationMeta = meta.copy(maxSequenceIds =
          meta.maxSequenceIds +
            (implicitly[EsoKeyProvider[Container]].seqIdKey -> containerSeqId) +
            (implicitly[EsoKeyProvider[Seal]].seqIdKey -> sealSeqId)
        ),
        transport = transport.copy(containers = Option(containers))
      )
    }

  val withoutContainerData: ExportsDeclarationModifier =
    model => model.copy(transport = model.transport.copy(containers = None))

  def withIsExs(data: IsExs = IsExs("Yes")): ExportsDeclarationModifier = cache => cache.copy(parties = cache.parties.copy(isExs = Some(data)))

  val withoutIsExs: ExportsDeclarationModifier = cache => cache.copy(parties = cache.parties.copy(isExs = None))
}
