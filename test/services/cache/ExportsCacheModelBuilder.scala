package services.cache

import java.time.LocalDateTime
import java.util.UUID

import forms.common.Address
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDec.AllowedAdditionalDeclarationTypes
import forms.declaration.destinationCountries.DestinationCountries
import forms.declaration._
import forms.{Choice, Ducr}
import models.declaration.DeclarationHoldersData

trait ExportsCacheModelBuilder {

  protected val DUCR = "5GB123456789000-123ABC456DEFIIIII"
  protected val LRN = "FG7676767889"

  private def uuid: String = UUID.randomUUID().toString

  private val modelWithDefaults: ExportsCacheModel = ExportsCacheModel(
    sessionId = uuid,
    draftId = uuid,
    createdDateTime = LocalDateTime.of(2019, 1, 1, 0, 0, 0),
    updatedDateTime = LocalDateTime.of(2019, 2, 2, 0, 0, 0),
    choice = Choice.AllowedChoiceValues.StandardDec
  )

  private type CacheModifier = ExportsCacheModel => ExportsCacheModel

  def aCacheModel(modifiers: (CacheModifier)*): ExportsCacheModel =
    modifiers.foldLeft(modelWithDefaults)((current, modifier) => modifier(current))

  // ************************************************* Builders ********************************************************

  def withoutTotalNumberOfItems(): CacheModifier = _.copy(totalNumberOfItems = None)

  def withTotalNumberOfItems(
    totalAmountInvoiced: Option[String] = None,
    exchangeRate: Option[String] = None,
    totalPackage: String = "1"
  ): CacheModifier =
    _.copy(totalNumberOfItems = Some(TotalNumberOfItems(totalAmountInvoiced, exchangeRate, totalPackage)))

  def withConsignmentReference(ducr: Option[String] = Some(DUCR), lrn: String = LRN): CacheModifier =
    _.copy(consignmentReferences = Some(ConsignmentReferences(ducr.map(Ducr(_)), lrn)))

  def withAdditionalDeclarationType(decType: String = AllowedAdditionalDeclarationTypes.Standard): CacheModifier =
    _.copy(additionalDeclarationType = Some(AdditionalDeclarationType(decType)))

  def withDispatchLocation(location: String = "GB"): CacheModifier =
    _.copy(dispatchLocation = Some(DispatchLocation(location)))

  def withItem(id: String = uuid): CacheModifier =
    m => m.copy(items = m.items + ExportItem(id = id))

  def withItems(count: Int): CacheModifier =
    m => m.copy(items = m.items ++ (1 to count).map(_ => ExportItem(id = uuid)).toSet)

  def withoutDeclarationHolders(): CacheModifier = m => m.copy(parties = m.parties.copy(declarationHoldersData = None))

  def withDeclarationHolder(
    authorisationTypeCode: Option[String] = None,
    eori: Option[String] = None
  ): CacheModifier = { m =>
    val existing: Seq[DeclarationHolder] = m.parties.declarationHoldersData.map(_.holders).getOrElse(Seq.empty)
    val holdersData = DeclarationHoldersData(existing :+ DeclarationHolder(authorisationTypeCode, eori))
    m.copy(parties = m.parties.copy(declarationHoldersData = Some(holdersData)))
  }

  def withDeclarationHolders(holders: DeclarationHolder*): CacheModifier =
    m => m.copy(parties = m.parties.copy(declarationHoldersData = Some(DeclarationHoldersData(holders))))

  def withoutTransportDetails(): CacheModifier = _.copy(transportDetails = None)

  def withTransportDetails(
    meansOfTransportCrossingTheBorderNationality: Option[String] = None,
    container: Boolean = false,
    meansOfTransportCrossingTheBorderType: String = "",
    meansOfTransportCrossingTheBorderIDNumber: Option[String] = None,
    paymentMethod: Option[String] = None
  ): CacheModifier =
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

  def withoutDestinationCountries(): CacheModifier =
    m => m.copy(locations = m.locations.copy(destinationCountries = None))

  def withDestinationCountries(
    countryOfDispatch: String = "GB",
    countriesOfRouting: Seq[String] = Seq.empty,
    countryOfDestination: String = "US"
  ): CacheModifier =
    m =>
      m.copy(
        locations = m.locations.copy(
          destinationCountries = Some(DestinationCountries(countryOfDispatch, countriesOfRouting, countryOfDestination))
        )
    )

  def withoutCarrierDetails(): CacheModifier = m => m.copy(parties = m.parties.copy(carrierDetails = None))

  def withCarrierDetails(eori: Option[String] = None,
                         address: Option[Address] =  None): CacheModifier =
    m => m.copy(parties = m.parties.copy(carrierDetails = Some(CarrierDetails(EntityDetails(eori, address)))))
}
