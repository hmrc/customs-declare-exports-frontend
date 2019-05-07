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
import forms.declaration.{ConsigneeDetails, EntityDetails}
import services.Countries.allCountries
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment
import wco.datamodel.wco.declaration_ds.dms._2._

object ConsigneeBuilder {

  def build(implicit cacheMap: CacheMap): GoodsShipment.Consignee =
    cacheMap
      .getEntry[ConsigneeDetails](ConsigneeDetails.id)
      .map(consigneeDetails => createConsignee(consigneeDetails.details))
      .orNull

  private def createConsignee(details: EntityDetails): GoodsShipment.Consignee = {
    val consignee = new GoodsShipment.Consignee()

    val id = new ConsigneeIdentificationIDType()
    id.setValue(details.eori.orNull)
    consignee.setID(id)

    val consigneeAddress = new GoodsShipment.Consignee.Address()
    details.address.map(address => {

      if (!Option(address.fullName).getOrElse("").isEmpty) {
        val name = new ConsigneeNameTextType()
        name.setValue(address.fullName)
        consignee.setName(name)
      }

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

      consigneeAddress.setLine(line)
      consigneeAddress.setCityName(city)
      consigneeAddress.setCountryCode(countryCode)
      consigneeAddress.setPostcodeID(postcode)
    })

    consignee.setAddress(consigneeAddress)
    consignee
  }
}
