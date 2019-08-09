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
import java.util.UUID

import forms.common.Address
import forms.declaration._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDec.AllowedAdditionalDeclarationTypes
import forms.declaration.destinationCountries.DestinationCountries
import forms.declaration.officeOfExit.OfficeOfExit
import forms.{Choice, Ducr}
import models.ExportsDeclaration
import models.declaration.{
  DeclarationAdditionalActorsData,
  DeclarationHoldersData,
  Locations,
  TransportInformationContainerData
}

//noinspection ScalaStyle
trait ExportsDeclarationBuilder {

  protected val DUCR = "5GB123456789000-123ABC456DEFIIIII"
  protected val LRN = "FG7676767889"

  private def uuid: String = UUID.randomUUID().toString

  private val modelWithDefaults: ExportsDeclaration = ExportsDeclaration(
    sessionId = uuid,
    draftId = uuid,
    createdDateTime = LocalDateTime.of(2019, 1, 1, 0, 0, 0),
    updatedDateTime = LocalDateTime.of(2019, 2, 2, 0, 0, 0),
    choice = Choice.AllowedChoiceValues.StandardDec
  )

  private type ExportsDeclarationModifier = ExportsDeclaration => ExportsDeclaration

  def aDeclaration(modifiers: ExportsDeclarationModifier*): ExportsDeclaration =
    modifiers.foldLeft(modelWithDefaults)((current, modifier) => modifier(current))

  // ************************************************* Builders ********************************************************

  def withSessionId(id: String = uuid): ExportsDeclarationModifier = _.copy(sessionId = id)

  def withChoice(choice: String): ExportsDeclarationModifier = _.copy(choice = choice)

  def withoutTotalNumberOfItems(): ExportsDeclarationModifier = _.copy(totalNumberOfItems = None)

  def withTotalNumberOfItems(
    totalAmountInvoiced: Option[String] = None,
    exchangeRate: Option[String] = None,
    totalPackage: String = "1"
  ): ExportsDeclarationModifier =
    _.copy(totalNumberOfItems = Some(TotalNumberOfItems(totalAmountInvoiced, exchangeRate, totalPackage)))

  def withAdditionalDeclarationType(
    decType: String = AllowedAdditionalDeclarationTypes.Standard
  ): ExportsDeclarationModifier =
    _.copy(additionalDeclarationType = Some(AdditionalDeclarationType(decType)))

  def withDispatchLocation(location: String = "GB"): ExportsDeclarationModifier =
    _.copy(dispatchLocation = Some(DispatchLocation(location)))

  def withGoodsLocation(goodsLocation: GoodsLocation): ExportsDeclarationModifier = { m =>
    m.copy(locations = m.locations.copy(goodsLocation = Some(goodsLocation)))
  }

  def withDestinationCountries(destinationCountries: Option[DestinationCountries]): ExportsDeclarationModifier = { m =>
    {
      val location: Locations = m.locations.copy(destinationCountries = destinationCountries)
      m.copy(locations = location)
    }
  }
  def withoutItems(): ExportsDeclarationModifier = _.copy(items = Set.empty)

  def withItem(item: ExportItem = ExportItem(uuid)): ExportsDeclarationModifier =
    m => m.copy(items = m.items + item)

  def withItems(item1: ExportItem, others: ExportItem*): ExportsDeclarationModifier =
    _.copy(items = Set(item1) ++ others)

  def withItems(count: Int): ExportsDeclarationModifier =
    cache => cache.copy(items = cache.items ++ (1 to count).map(_ => ExportItem(id = uuid)).toSet)

  def withoutExporterDetails(): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(exporterDetails = None))

  def withExporterDetails(eori: Option[String] = None, address: Option[Address] = None): ExportsDeclarationModifier =
    cache =>
      cache.copy(parties = cache.parties.copy(exporterDetails = Some(ExporterDetails(EntityDetails(eori, address)))))

  def withoutDeclarantDetails(): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarantDetails = None))

  def withDeclarantDetails(eori: Option[String] = None, address: Option[Address] = None): ExportsDeclarationModifier =
    cache =>
      cache.copy(parties = cache.parties.copy(declarantDetails = Some(DeclarantDetails(EntityDetails(eori, address)))))

  def withoutDeclarationHolders(): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationHoldersData = None))

  def withDeclarationHolders(
    authorisationTypeCode: Option[String] = None,
    eori: Option[String] = None
  ): ExportsDeclarationModifier = { cache =>
    val existing: Seq[DeclarationHolder] = cache.parties.declarationHoldersData.map(_.holders).getOrElse(Seq.empty)
    val holdersData = DeclarationHoldersData(existing :+ DeclarationHolder(authorisationTypeCode, eori))
    cache.copy(parties = cache.parties.copy(declarationHoldersData = Some(holdersData)))
  }

  def withDeclarationHolders(holders: DeclarationHoldersData): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationHoldersData = Some(holders)))

  def withDeclarationHolders(holders: DeclarationHolder*): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationHoldersData = Some(DeclarationHoldersData(holders))))

  def withoutBorderTransport(): ExportsDeclarationModifier = _.copy(borderTransport = None)

  def withBorderTransport(
    borderModeOfTransportCode: String = "",
    meansOfTransportOnDepartureType: String = "",
    meansOfTransportOnDepartureIDNumber: Option[String] = None
  ): ExportsDeclarationModifier =
    _.copy(
      borderTransport = Some(
        BorderTransport(borderModeOfTransportCode, meansOfTransportOnDepartureType, meansOfTransportOnDepartureIDNumber)
      )
    )

  def withConsignmentReferences(ducr: Option[String] = Some(DUCR), lrn: String = LRN): ExportsDeclarationModifier =
    withConsignmentReferences(ConsignmentReferences(ducr.map(Ducr(_)), lrn))

  def withConsignmentReferences(consignmentReferences: ConsignmentReferences): ExportsDeclarationModifier =
    _.copy(consignmentReferences = Some(consignmentReferences))

  def withoutConsignmentReference(): ExportsDeclarationModifier = _.copy(consignmentReferences = None)

  def withConsigneeDetails(consigneeDetails: ConsigneeDetails): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(consigneeDetails = Some(consigneeDetails)))

  def withConsigneeDetails(eori: Option[String], address: Option[Address]): ExportsDeclarationModifier =
    cache =>
      cache.copy(parties = cache.parties.copy(consigneeDetails = Some(ConsigneeDetails(EntityDetails(eori, address)))))

  def withoutConsigneeDetails(): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(consigneeDetails = None))

  def withDeclarationAdditionalActors(data: DeclarationAdditionalActors*): ExportsDeclarationModifier =
    cache =>
      cache.copy(
        parties = cache.parties.copy(declarationAdditionalActorsData = Some(DeclarationAdditionalActorsData(data)))
    )

  def withDeclarationAdditionalActors(
    declarationAdditionalActorsData: DeclarationAdditionalActorsData
  ): ExportsDeclarationModifier =
    cache =>
      cache.copy(parties = cache.parties.copy(declarationAdditionalActorsData = Some(declarationAdditionalActorsData)))

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

  def withoutTransportDetails(): ExportsDeclarationModifier = _.copy(transportDetails = None)

  def withTransportDetails(details: TransportDetails): ExportsDeclarationModifier =
    _.copy(transportDetails = Some(details))

  def withTransportDetails(
    meansOfTransportCrossingTheBorderNationality: Option[String] = None,
    container: Boolean = false,
    meansOfTransportCrossingTheBorderType: String = "",
    meansOfTransportCrossingTheBorderIDNumber: Option[String] = None,
    paymentMethod: Option[String] = None
  ): ExportsDeclarationModifier =
    _.copy(
      transportDetails = Some(
        TransportDetails(
          meansOfTransportCrossingTheBorderNationality = meansOfTransportCrossingTheBorderNationality,
          container = container,
          meansOfTransportCrossingTheBorderType = meansOfTransportCrossingTheBorderType,
          meansOfTransportCrossingTheBorderIDNumber = meansOfTransportCrossingTheBorderIDNumber,
          paymentMethod = paymentMethod
        )
      )
    )

  def withoutDestinationCountries(): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(destinationCountries = None))

  def withDestinationCountries(
    countryOfDispatch: String = "GB",
    countriesOfRouting: Seq[String] = Seq.empty,
    countryOfDestination: String = "US"
  ): ExportsDeclarationModifier =
    withDestinationCountries(DestinationCountries(countryOfDispatch, countriesOfRouting, countryOfDestination))

  def withDestinationCountries(destinationCountries: DestinationCountries): ExportsDeclarationModifier = { m =>
    m.copy(locations = m.locations.copy(destinationCountries = Some(destinationCountries)))
  }

  def withoutCarrierDetails(): ExportsDeclarationModifier =
    cache => cache.copy(parties = cache.parties.copy(carrierDetails = None))

  def withCarrierDetails(eori: Option[String] = None, address: Option[Address] = None): ExportsDeclarationModifier =
    cache =>
      cache.copy(parties = cache.parties.copy(carrierDetails = Some(CarrierDetails(EntityDetails(eori, address)))))

  def withoutWarehouseIdentification(): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(warehouseIdentification = None))

  def withWarehouseIdentification(warehouseIdentification: WarehouseIdentification): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(warehouseIdentification = Some(warehouseIdentification)))

  def withWarehouseIdentification(
    supervisingCustomsOffice: Option[String] = None,
    identificationType: Option[String] = None,
    identificationNumber: Option[String] = None,
    inlandModeOfTransportCode: Option[String] = None
  ): ExportsDeclarationModifier =
    withWarehouseIdentification(
      WarehouseIdentification(
        supervisingCustomsOffice,
        identificationType,
        identificationNumber,
        inlandModeOfTransportCode
      )
    )

  def withoutOfficeOfExit(): ExportsDeclarationModifier =
    cache => cache.copy(locations = cache.locations.copy(officeOfExit = None))

  def withOfficeOfExit(
    officeId: String = "",
    presentationOfficeId: Option[String] = None,
    circumstancesCode: Option[String] = None
  ): ExportsDeclarationModifier =
    cache =>
      cache.copy(
        locations =
          cache.locations.copy(officeOfExit = Some(OfficeOfExit(officeId, presentationOfficeId, circumstancesCode)))
    )

  def withContainerData(data: TransportInformationContainerData): ExportsDeclarationModifier =
    _.copy(containerData = Some(data))

  def withContainerData(data: TransportInformationContainer*): ExportsDeclarationModifier =
    cache =>
      cache.copy(
        containerData =
          Some(TransportInformationContainerData(cache.containerData.map(_.containers).getOrElse(Seq.empty) ++ data))
    )

  def withoutContainerData(): ExportsDeclarationModifier = _.copy(containerData = None)

  def withoutSeal(): ExportsDeclarationModifier = _.copy(seals = Seq.empty)

  def withSeal(seal1: Seal, others: Seal*): ExportsDeclarationModifier =
    cache => cache.copy(seals = cache.seals ++ Seq(seal1) ++ others)

  def withSeals(seals: Seq[Seal]): ExportsDeclarationModifier = _.copy(seals = seals)
}
