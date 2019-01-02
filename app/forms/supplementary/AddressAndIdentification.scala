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

import play.api.data.Forms.{mapping, text}
import play.api.libs.json.Json
import services.Countries.allCountries
import utils.validators.FormFieldValidator.{isAlphanumeric, noLongerThan}

case class AddressAndIdentification(
  eori: String, // alphanumeric, max length 17 characters
  fullName: String, // alphanumeric length 1 - 70
  addressLine: String, // alphanumeric length 1 - 70
  townOrCity: String, // alphanumeric length 1 - 35
  postCode: String, // alphanumeric length 1 - 9
  country: String // 2 upper case alphabetic characters
)

object AddressAndIdentification {
  implicit val format = Json.format[AddressAndIdentification]

  val addressMapping = mapping(
    "eori" -> text()
      .verifying("supplementary.eori.empty", _.trim.nonEmpty)
      .verifying("supplementary.eori.error", validateField(_, 17)),
    "fullName" -> text()
      .verifying("supplementary.fullName.empty", _.trim.nonEmpty)
      .verifying("supplementary.fullName.error", validateField(_, 70)),
    "addressLine" -> text()
      .verifying("supplementary.addressLine.empty", _.trim.nonEmpty)
      .verifying("supplementary.addressLine.error", validateField(_, 70)),
    "townOrCity" -> text()
      .verifying("supplementary.townOrCity.empty", _.trim.nonEmpty)
      .verifying("supplementary.townOrCity.error", validateField(_, 35)),
    "postCode" -> text()
      .verifying("supplementary.postCode.empty", _.trim.nonEmpty)
      .verifying("supplementary.postCode.error", validateField(_, 9)),
    "country" -> text()
      .verifying("supplementary.country.empty", _.trim.nonEmpty)
      .verifying(
        "supplementary.country.error",
        input => input.isEmpty || !allCountries.filter(country => country.countryName == input).isEmpty
      )
  )(AddressAndIdentification.apply)(AddressAndIdentification.unapply)

  private def validateField(input: String, maxLength: Int): Boolean =
    noLongerThan(input, maxLength) && isAlphanumeric(input.replaceAll(" ", ""))

  def toConsignorMetadataProperties(address: AddressAndIdentification): Map[String, String] =
    Map(
      "declaration.goodsShipment.governmentAgencyGoodsItem.consignor.id" -> address.eori,
      "declaration.goodsShipment.governmentAgencyGoodsItem.consignor.name" -> address.fullName,
      "declaration.goodsShipment.governmentAgencyGoodsItem.consignor.address.line" -> address.addressLine,
      "declaration.goodsShipment.governmentAgencyGoodsItem.consignor.address.cityName" -> address.townOrCity,
      "declaration.goodsShipment.governmentAgencyGoodsItem.consignor.address.postcodeId" -> address.postCode,
      "declaration.goodsShipment.governmentAgencyGoodsItem.consignor.address.countryCode" ->
        allCountries.find(country => country.countryName == address.country).map(_.countryCode).getOrElse("")
    )

  def toDeclarantMetadataProperties(address: AddressAndIdentification): Map[String, String] =
    Map(
      "declaration.declarant.id" -> address.eori,
      "declaration.declarant.name" -> address.fullName,
      "declaration.declarant.address.line" -> address.addressLine,
      "declaration.declarant.address.cityName" -> address.townOrCity,
      "declaration.declarant.address.postcodeId" -> address.postCode,
      "declaration.declarant.address.countryCode" ->
        allCountries.find(country => country.countryName == address.country).map(_.countryCode).getOrElse("")
    )
}
