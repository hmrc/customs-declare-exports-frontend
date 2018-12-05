/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.data.Forms.{mapping, text}
import play.api.libs.json.Json

case class Address(
  eori: String, // alphanumeric, max length 17 characters
  fullName: String, // alphanumeric length 1 - 70
  addressLine: String, // alphanumeric length 1 - 70
  townOrCity: String, // alphanumeric length 1 - 35
  postCode: String, // alphanumeric length 1 - 9
  country: String // 2 upper case alphabetic characters
)

object Address {
  implicit val format = Json.format[Address]

  val addressMapping = mapping(
    "eori" -> text().verifying("supplementary.eori.empty", _.nonEmpty)
      .verifying("supplementary.eori.error", _.length <= 17),
    "fullName" -> text().verifying("supplementary.fullName.empty", _.nonEmpty)
      .verifying("supplementary.fullName.error", _.length <= 70),
    "addressLine" -> text().verifying("supplementary.addressLine.empty", _.nonEmpty)
      .verifying("supplementary.addressLine.error", _.length <= 70),
    "townOrCity" -> text().verifying("supplementary.townOrCity.empty", _.nonEmpty)
      .verifying("supplementary.townOrCity.error", _.length <= 35),
    "postCode" -> text().verifying("supplementary.postCode.empty", _.nonEmpty)
      .verifying("supplementary.postCode.error", _.length <= 9),
    "country" -> text().verifying("supplementary.country.empty", _.nonEmpty)
      .verifying("supplementary.country.error", _.length <= 2)
  )(Address.apply)(Address.unapply)

  def toConsignorMetadataProperties(address: Address): Map[String, String] =
    Map(
      "declaration.goodsShipment.governmentAgencyGoodsItem.consignor.ID" -> address.eori,
      "declaration.goodsShipment.governmentAgencyGoodsItem.consignor.name" -> address.fullName,
      "declaration.goodsShipment.governmentAgencyGoodsItem.consignor.address.line" -> address.addressLine,
      "declaration.goodsShipment.governmentAgencyGoodsItem.consignor.address.cityName" -> address.townOrCity,
      "declaration.goodsShipment.governmentAgencyGoodsItem.consignor.address.postCodeID" -> address.postCode,
      "declaration.goodsShipment.governmentAgencyGoodsItem.consignor.address.countryCode" -> address.country
    )

  def toDeclarantMetadataProperties(address: Address): Map[String, String] =
    Map(
      "declaration.declarant.ID" -> address.eori,
      "declaration.declarant.name" -> address.fullName,
      "declaration.declarant.address.line" -> address.addressLine,
      "declaration.declarant.address.cityName" -> address.townOrCity,
      "declaration.declarant.address.postCodeID" -> address.postCode,
      "declaration.declarant.address.countryCode" -> address.country
    )
}
