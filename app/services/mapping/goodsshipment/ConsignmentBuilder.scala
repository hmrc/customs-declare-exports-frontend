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

package services.mapping.goodsshipment
import forms.declaration.{CarrierDetails, EntityDetails, TransportInformation}
import services.Countries.allCountries
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.Consignment
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.Consignment.{
  ArrivalTransportMeans,
  DepartureTransportMeans
}
import wco.datamodel.wco.declaration_ds.dms._2._

object ConsignmentBuilder {

  def build(implicit cacheMap: CacheMap): GoodsShipment.Consignment =
    cacheMap
      .getEntry[CarrierDetails](CarrierDetails.id)
      .map(carrierDetails => createConsignmentAddress(carrierDetails.details))
      .orNull

  private def createConsignmentAddress(
    details: EntityDetails
  )(implicit cacheMap: CacheMap): GoodsShipment.Consignment = {
    val id = new GoodsLocationIdentificationIDType()
    id.setValue(details.eori.orNull)

    val name = new GoodsLocationNameTextType()

    val goodsAddress = new Consignment.GoodsLocation.Address()
    details.address.map(address => {
      name.setValue(address.fullName)

      val line = new AddressLineTextType()
      line.setValue(address.addressLine)

      val city = new AddressCityNameTextType
      city.setValue(address.townOrCity)

      val postcode = new AddressPostcodeIDType()
      postcode.setValue(address.postCode)

      val countryCode = new AddressCountryCodeType
      countryCode.setValue(
        allCountries.find(country => address.country.contains(country.countryName)).map(_.countryCode).getOrElse("")
      )

      goodsAddress.setLine(line)
      goodsAddress.setCityName(city)
      goodsAddress.setCountryCode(countryCode)
      goodsAddress.setPostcodeID(postcode)
    })

    val goodsLocation = new GoodsShipment.Consignment.GoodsLocation()
    goodsLocation.setID(id)
    goodsLocation.setName(name)
    goodsLocation.setAddress(goodsAddress)

    val consignment = new GoodsShipment.Consignment()
    consignment.setGoodsLocation(goodsLocation)
    consignment.setContainerCode(buildContainerCode)
    consignment.setArrivalTransportMeans(buildArrivalTransportMeans)
    consignment.setDepartureTransportMeans(buildDepartureTransportMeans())
    consignment
  }

  private def buildDepartureTransportMeans()(implicit cacheMap: CacheMap): Consignment.DepartureTransportMeans =
    cacheMap
      .getEntry[TransportInformation](TransportInformation.id)
      .map(createDepartureTransportMeans)
      .orNull

  private def createDepartureTransportMeans(data: TransportInformation): Consignment.DepartureTransportMeans = {
    val id = new DepartureTransportMeansIdentificationIDType()
    id.setValue(data.meansOfTransportOnDepartureIDNumber.getOrElse(""))

    val identificationTypeCode = new DepartureTransportMeansIdentificationTypeCodeType()
    identificationTypeCode.setValue(data.meansOfTransportCrossingTheBorderType)

    val departureTransportMeans = new DepartureTransportMeans()
    departureTransportMeans.setID(id)
    departureTransportMeans.setIdentificationTypeCode(identificationTypeCode)
    departureTransportMeans
  }

  private def buildArrivalTransportMeans()(implicit cacheMap: CacheMap): Consignment.ArrivalTransportMeans = {
    val modeCodeType = new ArrivalTransportMeansModeCodeType()
    modeCodeType.setValue(
      cacheMap
        .getEntry[TransportInformation](TransportInformation.id)
        .map(transportInformation => transportInformation.inlandModeOfTransportCode.getOrElse(""))
        .getOrElse("")
    )

    val arrivalTransportMeans = new ArrivalTransportMeans()
    arrivalTransportMeans.setModeCode(modeCodeType)
    arrivalTransportMeans
  }

  private def buildContainerCode()(implicit cacheMap: CacheMap): ConsignmentContainerCodeType = {
    val codeType = new ConsignmentContainerCodeType()
    codeType.setValue(
      cacheMap
        .getEntry[TransportInformation](TransportInformation.id)
        .map(transportInformation => if (transportInformation.container) "1" else "0")
        .getOrElse("0")
    )
    codeType
  }
}
