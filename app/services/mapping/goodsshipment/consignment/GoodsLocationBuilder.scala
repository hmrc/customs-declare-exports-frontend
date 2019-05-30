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

package services.mapping.goodsshipment.consignment
import forms.declaration.{CarrierDetails, EntityDetails}
import services.Countries.allCountries
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.Consignment
import wco.datamodel.wco.declaration_ds.dms._2._

object GoodsLocationBuilder {

  def build()(implicit cacheMap: CacheMap): Consignment.GoodsLocation =
    cacheMap
      .getEntry[CarrierDetails](CarrierDetails.id)
      .filter(isDefined)
      .map(carrierDetails => buildEoriOrAddress(carrierDetails.details))
      .orNull

  private def isDefined(carrierDetails: CarrierDetails) =
    carrierDetails.details.eori.isDefined ||
      carrierDetails.details.address.isDefined

  private def buildEoriOrAddress(details: EntityDetails) = {
    val goodsLocation = new Consignment.GoodsLocation()

    details.eori.foreach { value =>
      val id = new GoodsLocationIdentificationIDType()
      id.setValue(value)
      goodsLocation.setID(id)
    }

    details.address.foreach { address =>
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
}
