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
import services.Countries.allCountries
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.Consignment
import wco.datamodel.wco.declaration_ds.dms._2._

object GoodsLocationBuilder {

  def build()(implicit cacheMap: CacheMap): Consignment.GoodsLocation =
    cacheMap
      .getEntry[GoodsLocation](GoodsLocation.formId)
      .filter(isDefined)
      .map(goods => buildEoriOrAddress(goods))
      .orNull

  private def isDefined(goodsLocation: GoodsLocation) =
    goodsLocation.additionalIdentifier.isDefined ||
      goodsLocation.postCode.isDefined

  private def buildEoriOrAddress(goods: GoodsLocation) = {
    val goodsLocation = new Consignment.GoodsLocation()

    goods.additionalIdentifier.foreach { value =>
      val id = new GoodsLocationIdentificationIDType()
      id.setValue(value)
      goodsLocation.setID(id)
    }

    if (goods.typeOfLocation.nonEmpty) {
      val goodsTypeCode = new GoodsLocationTypeCodeType()
      goodsTypeCode.setValue(goods.typeOfLocation)
      goodsLocation.setTypeCode(goodsTypeCode)
    }

    goods.identificationOfLocation.foreach { value =>
      val name = new GoodsLocationNameTextType()
      name.setValue(value)
      goodsLocation.setName(name)
    }

    if (goods.postCode.isDefined) {
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
        allCountries.find(country => goods.country.contains(country.countryName)).map(_.countryCode).getOrElse("")
      )
      goodsAddress.setCountryCode(countryCode)
    }
    goodsAddress
  }
}
