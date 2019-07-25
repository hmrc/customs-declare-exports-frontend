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

import forms.Choice
import forms.Choice.AllowedChoiceValues
import forms.common.Address
import forms.declaration.{CarrierDetails, EntityDetails}
import services.Countries.allCountries
import services.cache.ExportsCacheModel
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.dec_dms._2.Declaration.Consignment.Carrier
import wco.datamodel.wco.declaration_ds.dms._2._

object ConsignmentCarrierBuilder {

  def buildThenAdd(model: ExportsCacheModel, consignment: Declaration.Consignment): Unit = {
    if(model.choice.equals(AllowedChoiceValues.StandardDec)) {
      model.parties.carrierDetails
        .filter(isDefined)
        .map(_.details)
        .map(buildEoriOrAddress)
        .foreach(consignment.setCarrier)
    }
  }

  def build()(implicit cacheMap: CacheMap, choice: Choice): Declaration.Consignment.Carrier =
    choice match {
      case Choice(AllowedChoiceValues.StandardDec)      => buildCarrierDetails
      case Choice(AllowedChoiceValues.SupplementaryDec) => null
    }

  def buildCarrierDetails()(implicit cacheMap: CacheMap): Declaration.Consignment.Carrier =
    cacheMap
      .getEntry[CarrierDetails](CarrierDetails.id)
      .filter(isDefined)
      .map(carrierDetails => buildEoriOrAddress(carrierDetails.details))
      .orNull

  private def isDefined(carrierDetails: CarrierDetails) =
    carrierDetails.details.address.isDefined ||
      carrierDetails.details.eori.isDefined

  private def buildEoriOrAddress(details: EntityDetails) = {
    val carrier = new Declaration.Consignment.Carrier

    details.eori
      .filter(_.nonEmpty)
      .foreach { value =>
        val carrierId = new CarrierIdentificationIDType()
        carrierId.setValue(value)
        carrier.setID(carrierId)
      }

    details.address
      .filter(_.fullName.nonEmpty)
      .foreach { address =>
        val carrierName = new CarrierNameTextType()
        carrierName.setValue(address.fullName)
        carrier.setName(carrierName)
      }

    details.address
      .filter(
        address =>
          address.addressLine.nonEmpty || address.townOrCity.nonEmpty || address.postCode.nonEmpty || address.country.nonEmpty
      )
      .foreach { address =>
        carrier.setAddress(createAddress(address))
      }
    carrier
  }

  private def createAddress(address: Address) = {

    val carrierAddress = new Carrier.Address()

    val line = new AddressLineTextType()
    line.setValue(address.addressLine)
    carrierAddress.setLine(line)

    val city = new AddressCityNameTextType
    city.setValue(address.townOrCity)
    carrierAddress.setCityName(city)

    val postcode = new AddressPostcodeIDType()
    postcode.setValue(address.postCode)
    carrierAddress.setPostcodeID(postcode)

    val countryCode = new AddressCountryCodeType
    countryCode.setValue(
      allCountries
        .find(country => address.country.contains(country.countryName))
        .map(_.countryCode)
        .getOrElse("")
    )
    carrierAddress.setCountryCode(countryCode)
    carrierAddress
  }
}
