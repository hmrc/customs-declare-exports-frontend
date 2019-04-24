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

package services.mapping.declaration
import forms.common.Address
import forms.declaration.DeclarantDetails
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.dec_dms._2.Declaration.Declarant
import wco.datamodel.wco.declaration_ds.dms._2._

object DeclarantBuilder {

  def build(implicit cacheMap: CacheMap): Declaration.Declarant =
    cacheMap
      .getEntry[DeclarantDetails](DeclarantDetails.id)
      .map(declarantDetails => mapToWCODeclarant(declarantDetails))
      .orNull

  def mapToWCODeclarant(declarantDetails: DeclarantDetails): Declarant = {
    val declarant = new Declarant

    val declarantAddress = declarantDetails.details.address match {
      case Some(address) => {
        val declarantNameTextType = new DeclarantNameTextType
        declarantNameTextType.setValue(address.fullName)
        declarant.setName(declarantNameTextType)
        mapToWCODeclarantAddress(address)
      }
      case _ => new Declarant.Address
    }

    declarant.setAddress(declarantAddress)
    declarant
  }

  private def mapToWCODeclarantAddress(address: Address): Declarant.Address = {
    val declarantAddress = new Declarant.Address
    val addressLineTextType = new AddressLineTextType
    addressLineTextType.setValue(address.addressLine)

    val addressCityNameTextType = new AddressCityNameTextType
    addressCityNameTextType.setValue(address.townOrCity)

    val addressPostcodeIDType = new AddressPostcodeIDType
    addressPostcodeIDType.setValue(address.postCode)

    val addressCountryCodeType = new AddressCountryCodeType
    addressCountryCodeType.setValue(deriveCountryCode(address.country))

    declarantAddress.setLine(addressLineTextType)
    declarantAddress.setCityName(addressCityNameTextType)
    declarantAddress.setPostcodeID(addressPostcodeIDType)
    declarantAddress.setCountryCode(addressCountryCodeType)

    declarantAddress
  }

  private def deriveCountryCode(addressCountry: String): String =
    services.Countries.allCountries
      .find(country => addressCountry.contains(country.countryName))
      .map(_.countryCode)
      .getOrElse("")
}
