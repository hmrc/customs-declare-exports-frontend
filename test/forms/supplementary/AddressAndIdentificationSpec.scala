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

package forms.supplementary

import org.scalatest.{MustMatchers, WordSpec}

class AddressAndIdentificationSpec extends WordSpec with MustMatchers {

  val eori = "GB111222333444"
  val fullName = "Full name"
  val addressLine = "Address line"
  val townOrCity = "Town or City"
  val postCode = "Postcode"
  val country = "Poland"
  val countryCode = "PL"

  val address: AddressAndIdentification =
    AddressAndIdentification(
      eori = Some(eori),
      fullName = Some(fullName),
      addressLine = Some(addressLine),
      townOrCity = Some(townOrCity),
      postCode = Some(postCode),
      country = Some(country)
    )

  val consignorAddressProperties: Map[String, String] =
    Map(
      "declaration.goodsShipment.governmentAgencyGoodsItem.consignor.id" -> eori,
      "declaration.goodsShipment.governmentAgencyGoodsItem.consignor.name" -> fullName,
      "declaration.goodsShipment.governmentAgencyGoodsItem.consignor.address.line" -> addressLine,
      "declaration.goodsShipment.governmentAgencyGoodsItem.consignor.address.cityName" -> townOrCity,
      "declaration.goodsShipment.governmentAgencyGoodsItem.consignor.address.postcodeId" -> postCode,
      "declaration.goodsShipment.governmentAgencyGoodsItem.consignor.address.countryCode" -> countryCode
    )

  val declarantAddressProperties: Map[String, String] =
    Map(
      "declaration.declarant.id" -> eori,
      "declaration.declarant.name" -> fullName,
      "declaration.declarant.address.line" -> addressLine,
      "declaration.declarant.address.cityName" -> townOrCity,
      "declaration.declarant.address.postcodeId" -> postCode,
      "declaration.declarant.address.countryCode" -> countryCode
    )

  "Address" should {
    "correctly convert address to consignor address properties" in {
      AddressAndIdentification.toConsignorMetadataProperties(address) must be(consignorAddressProperties)
    }

    "correctly convert address to declarant address properties" in {
      AddressAndIdentification.toDeclarantMetadataProperties(address) must be(declarantAddressProperties)
    }
  }
}
