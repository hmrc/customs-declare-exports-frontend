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
import javax.inject.Inject
import services.Countries.allCountries
import services.mapping.ModifyingBuilder
import services.mapping.goodsshipment.ConsigneeBuilder.{createConsignee, isDefined}
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment
import wco.datamodel.wco.declaration_ds.dms._2._

class ConsigneeBuilder @Inject()() extends ModifyingBuilder[ConsigneeDetails, GoodsShipment] {

  override def buildThenAdd(consigneeDetails: ConsigneeDetails, goodsShipment: GoodsShipment) =
    if (isDefined(consigneeDetails))
      goodsShipment.setConsignee(createConsignee(consigneeDetails.details))

}

object ConsigneeBuilder {

  private def isDefined(consigneeDetails: ConsigneeDetails): Boolean =
    consigneeDetails.details.eori.getOrElse("").nonEmpty ||
      (consigneeDetails.details.address.isDefined && consigneeDetails.details.address.get.isDefined())

  private def createConsignee(details: EntityDetails): GoodsShipment.Consignee = {
    val consignee = new GoodsShipment.Consignee()

    details.eori.foreach { value =>
      val id = new ConsigneeIdentificationIDType()
      id.setValue(details.eori.orNull)
      consignee.setID(id)
    }

    details.address.foreach { address =>
      val consigneeAddress = new GoodsShipment.Consignee.Address()

      if (address.fullName.nonEmpty) {
        val name = new ConsigneeNameTextType()
        name.setValue(address.fullName)
        consignee.setName(name)
      }

      if (address.addressLine.nonEmpty) {
        val line = new AddressLineTextType()
        line.setValue(address.addressLine)
        consigneeAddress.setLine(line)
      }

      if (address.townOrCity.nonEmpty) {
        val city = new AddressCityNameTextType
        city.setValue(address.townOrCity)
        consigneeAddress.setCityName(city)
      }

      if (address.postCode.nonEmpty) {
        val postcode = new AddressPostcodeIDType()
        postcode.setValue(address.postCode)
        consigneeAddress.setPostcodeID(postcode)
      }

      if (address.country.nonEmpty) {
        val countryCode = new AddressCountryCodeType
        countryCode.setValue(
          allCountries.find(country => address.country.contains(country.countryName)).map(_.countryCode).getOrElse("")
        )
        consigneeAddress.setCountryCode(countryCode)
      }
      consignee.setAddress(consigneeAddress)
    }

    consignee
  }
}
