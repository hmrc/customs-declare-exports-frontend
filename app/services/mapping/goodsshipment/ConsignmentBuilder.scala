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

  def build(implicit cacheMap: CacheMap): GoodsShipment.Consignment = {
    val consignment = new GoodsShipment.Consignment()

    consignment.setGoodsLocation(buildGoodsLocation(cacheMap))
    consignment.setContainerCode(buildContainerCode)
    consignment.setArrivalTransportMeans(buildArrivalTransportMeans)
    consignment.setDepartureTransportMeans(buildDepartureTransportMeans())
    consignment
  }

  private def buildGoodsLocation(cacheMap: CacheMap): Consignment.GoodsLocation =
    cacheMap
      .getEntry[CarrierDetails](CarrierDetails.id)
      .filter(
        carrierDetails =>
          carrierDetails.details.eori.getOrElse("").nonEmpty ||
            (carrierDetails.details.address.isDefined && carrierDetails.details.address.get.isDefined())
      )
      .map(carrierDetails => buildEoriOrAddress(carrierDetails.details))
      .orNull

  private def buildEoriOrAddress(details: EntityDetails) = {
    val goodsLocation = new Consignment.GoodsLocation()
    if (details.eori.getOrElse("").nonEmpty) {
      val id = new GoodsLocationIdentificationIDType()
      id.setValue(details.eori.orNull)
      goodsLocation.setID(id)
    } else {
      val goodsAddress = new Consignment.GoodsLocation.Address()
      details.address.map(address => {

        if (address.fullName.nonEmpty) {
          val name = new GoodsLocationNameTextType()
          name.setValue(address.fullName)
          goodsLocation.setName(name)
        }

        if (address.addressLine.nonEmpty) {
          val line = new AddressLineTextType()
          line.setValue(address.addressLine)
          goodsAddress.setLine(line)
        }

        if (address.townOrCity.nonEmpty) {
          val city = new AddressCityNameTextType
          city.setValue(address.townOrCity)
          goodsAddress.setCityName(city)
        }

        if (address.postCode.nonEmpty) {
          val postcode = new AddressPostcodeIDType()
          postcode.setValue(address.postCode)
          goodsAddress.setPostcodeID(postcode)
        }

        if (address.country.nonEmpty) {
          val countryCode = new AddressCountryCodeType
          countryCode.setValue(
            allCountries.find(country => address.country.contains(country.countryName)).map(_.countryCode).getOrElse("")
          )
          goodsAddress.setCountryCode(countryCode)
        }
      })
      goodsLocation.setAddress(goodsAddress)
    }
    goodsLocation
  }

  private def buildDepartureTransportMeans()(implicit cacheMap: CacheMap): Consignment.DepartureTransportMeans =
    cacheMap
      .getEntry[TransportInformation](TransportInformation.id)
      .filter(isTransportInformationDefined)
      .map(createDepartureTransportMeans)
      .orNull

  private def isTransportInformationDefined(transportInformation: TransportInformation): Boolean =
    transportInformation.meansOfTransportOnDepartureIDNumber.getOrElse("").nonEmpty ||
      transportInformation.inlandModeOfTransportCode.getOrElse("").nonEmpty ||
      transportInformation.meansOfTransportCrossingTheBorderType.nonEmpty

  private def createDepartureTransportMeans(data: TransportInformation): Consignment.DepartureTransportMeans = {
    val departureTransportMeans = new DepartureTransportMeans()

    val idValue = data.meansOfTransportOnDepartureIDNumber.getOrElse("")
    if (idValue.nonEmpty) {
      val id = new DepartureTransportMeansIdentificationIDType()
      id.setValue(idValue)
      departureTransportMeans.setID(id)
    }

    if (data.meansOfTransportCrossingTheBorderType.nonEmpty) {
      val identificationTypeCode = new DepartureTransportMeansIdentificationTypeCodeType()
      identificationTypeCode.setValue(data.meansOfTransportCrossingTheBorderType)
      departureTransportMeans.setIdentificationTypeCode(identificationTypeCode)
    }

    departureTransportMeans
  }

  private def buildArrivalTransportMeans()(implicit cacheMap: CacheMap): Consignment.ArrivalTransportMeans =
    cacheMap
      .getEntry[TransportInformation](TransportInformation.id)
      .filter(_.inlandModeOfTransportCode.getOrElse("").nonEmpty)
      .map(transportInformation => {
        val arrivalTransportMeans = new ArrivalTransportMeans()
        val modeCodeType = new ArrivalTransportMeansModeCodeType()
        modeCodeType.setValue(transportInformation.inlandModeOfTransportCode.get)
        arrivalTransportMeans.setModeCode(modeCodeType)
        arrivalTransportMeans
      })
      .orNull

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

  private def isDefined(carrierDetails: CarrierDetails, cacheMap: CacheMap): Boolean =
    carrierDetails.details.eori.getOrElse("").nonEmpty ||
      (carrierDetails.details.address.isDefined && carrierDetails.details.address.get.isDefined()) ||
      (cacheMap
        .getEntry[TransportInformation](TransportInformation.id)
        .isDefined && isTransportInformationDefined(
        cacheMap
          .getEntry[TransportInformation](TransportInformation.id)
          .get
      ))
}
