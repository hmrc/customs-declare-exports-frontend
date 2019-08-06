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
import forms.declaration.GoodsLocation
import javax.inject.Inject
import services.Countries.allCountries
import services.mapping.ModifyingBuilder
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.Consignment
import wco.datamodel.wco.declaration_ds.dms._2._

class GoodsLocationBuilder @Inject()() extends ModifyingBuilder[GoodsLocation, GoodsShipment.Consignment] {
  override def buildThenAdd(model: GoodsLocation, consignment: Consignment): Unit =
    if (isDefined(model)) {
      consignment.setGoodsLocation(buildEoriOrAddress(model))
    }

  private def isDefined(goodsLocation: GoodsLocation) =
    goodsLocation.additionalQualifier.isDefined ||
      goodsLocation.postCode.isDefined ||
      goodsLocation.country.nonEmpty ||
      goodsLocation.city.nonEmpty ||
      goodsLocation.addressLine.nonEmpty ||
      goodsLocation.identificationOfLocation.nonEmpty ||
      goodsLocation.qualifierOfIdentification.nonEmpty ||
      goodsLocation.typeOfLocation.nonEmpty

  private def buildEoriOrAddress(goods: GoodsLocation) = {
    val goodsLocation = new Consignment.GoodsLocation()

    goods.additionalQualifier.foreach { value =>
      val name = new GoodsLocationNameTextType()
      name.setValue(value)
      goodsLocation.setName(name)

    }

    if (goods.typeOfLocation.nonEmpty) {
      val goodsTypeCode = new GoodsLocationTypeCodeType()
      goodsTypeCode.setValue(goods.typeOfLocation)
      goodsLocation.setTypeCode(goodsTypeCode)
    }

    goods.identificationOfLocation.foreach { value =>
      val id = new GoodsLocationIdentificationIDType()
      id.setValue(value)
      goodsLocation.setID(id)
    }

    if (goods.addressLine.isDefined || goods.city.isDefined || goods.postCode.isDefined || goods.country.nonEmpty) {
      goodsLocation.setAddress(createAddress(goods))
    }

    goodsLocation
  }

  private def createAddress(goods: GoodsLocation): Consignment.GoodsLocation.Address = {
    val goodsAddress = new Consignment.GoodsLocation.Address()

    goods.addressLine.foreach { value =>
      val line = new AddressLineTextType()
      line.setValue(value)
      goodsAddress.setLine(line)
    }

    goods.city.foreach { value =>
      val city = new AddressCityNameTextType
      city.setValue(value)
      goodsAddress.setCityName(city)
    }

    goods.postCode.foreach { value =>
      val postcode = new AddressPostcodeIDType()
      postcode.setValue(value)
      goodsAddress.setPostcodeID(postcode)
    }

    if (goods.country.nonEmpty) {
      val countryCode = new AddressCountryCodeType
      countryCode.setValue(
        allCountries.find(c => goods.country.contains(c.countryName)).map(_.countryCode).getOrElse("")
      )
      goodsAddress.setCountryCode(countryCode)
    }

    val qualifier = goods.qualifierOfIdentification
    val addressTypeCode = new AddressTypeCodeType()
    addressTypeCode.setValue(qualifier)
    goodsAddress.setTypeCode(addressTypeCode)

    goodsAddress
  }
}
