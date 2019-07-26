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
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypeSupplementaryDec.AllowedAdditionalDeclarationTypes
import forms.declaration.destinationCountries.DestinationCountries
import forms.declaration._
import forms.declaration.officeOfExit.OfficeOfExit
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

  def withChoice(choice: String): CacheModifier = _.copy(choice = choice)

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
    cache => cache.copy(items = cache.items + ExportItem(id = id))

  def withItems(count: Int): CacheModifier =
    cache => cache.copy(items = cache.items ++ (1 to count).map(_ => ExportItem(id = uuid)).toSet)

  def withoutExporterDetails(): CacheModifier =
    cache => cache.copy(parties = cache.parties.copy(exporterDetails = None))

  def withExporterDetails(eori: Option[String] = None, address: Option[Address] = None): CacheModifier =
    cache =>
      cache.copy(parties = cache.parties.copy(exporterDetails = Some(ExporterDetails(EntityDetails(eori, address)))))

  def withoutDeclarantDetails(): CacheModifier =
    cache => cache.copy(parties = cache.parties.copy(declarantDetails = None))

  def withDeclarantDetails(eori: Option[String] = None, address: Option[Address] = None): CacheModifier =
    cache =>
      cache.copy(parties = cache.parties.copy(declarantDetails = Some(DeclarantDetails(EntityDetails(eori, address)))))

  def withoutDeclarationHolders(): CacheModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationHoldersData = None))

  def withDeclarationHolder(
    authorisationTypeCode: Option[String] = None,
    eori: Option[String] = None
  ): CacheModifier = { cache =>
    val existing: Seq[DeclarationHolder] = cache.parties.declarationHoldersData.map(_.holders).getOrElse(Seq.empty)
    val holdersData = DeclarationHoldersData(existing :+ DeclarationHolder(authorisationTypeCode, eori))
    cache.copy(parties = cache.parties.copy(declarationHoldersData = Some(holdersData)))
  }

  def withDeclarationHolders(holders: DeclarationHolder*): CacheModifier =
    cache => cache.copy(parties = cache.parties.copy(declarationHoldersData = Some(DeclarationHoldersData(holders))))

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
    cache => cache.copy(locations = cache.locations.copy(destinationCountries = None))

  def withDestinationCountries(
    countryOfDispatch: String = "GB",
    countriesOfRouting: Seq[String] = Seq.empty,
    countryOfDestination: String = "US"
  ): CacheModifier =
    cache =>
      cache.copy(
        locations = cache.locations.copy(
          destinationCountries = Some(DestinationCountries(countryOfDispatch, countriesOfRouting, countryOfDestination))
        )
    )

  def withoutCarrierDetails(): CacheModifier = cache => cache.copy(parties = cache.parties.copy(carrierDetails = None))

  def withCarrierDetails(eori: Option[String] = None, address: Option[Address] = None): CacheModifier =
    cache =>
      cache.copy(parties = cache.parties.copy(carrierDetails = Some(CarrierDetails(EntityDetails(eori, address)))))

  def withoutWarehouseIdentification(): CacheModifier =
    cache => cache.copy(locations = cache.locations.copy(warehouseIdentification = None))

  def withWarehouseIdentification(
    supervisingCustomsOffice: Option[String] = None,
    identificationType: Option[String] = None,
    identificationNumber: Option[String] = None,
    inlandModeOfTransportCode: Option[String] = None
  ): CacheModifier =
    cache =>
      cache.copy(
        locations = cache.locations.copy(
          warehouseIdentification = Some(
            WarehouseIdentification(
              supervisingCustomsOffice,
              identificationType,
              identificationNumber,
              inlandModeOfTransportCode
            )
          )
        )
    )

  def withoutOfficeOfExit(): CacheModifier = cache => cache.copy(locations = cache.locations.copy(officeOfExit = None))

  def withOfficeOfExit(
    code: String = "",
    presentationOfficeId: Option[String] = None,
    circumstancesCode: Option[String] = None
  ): CacheModifier =
    cache =>
      cache.copy(
        locations =
          cache.locations.copy(officeOfExit = Some(OfficeOfExit(code, presentationOfficeId, circumstancesCode)))
    )
}
