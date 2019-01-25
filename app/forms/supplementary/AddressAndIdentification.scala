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

import play.api.data.Forms.{mapping, optional, text}
import play.api.libs.json.Json
import services.Countries.allCountries
import utils.validators.FormFieldValidator.isAlphanumeric

case class AddressAndIdentification(
  eori: Option[String], // alphanumeric, max length 17 characters
  fullName: Option[String], // alphanumeric length 1 - 70
  addressLine: Option[String], // alphanumeric length 1 - 70
  townOrCity: Option[String], // alphanumeric length 1 - 35
  postCode: Option[String], // alphanumeric length 1 - 9
  country: Option[String] // 2 upper case alphabetic characters
)

object AddressAndIdentification {
  implicit val format = Json.format[AddressAndIdentification]

  val addressMapping = mapping(
    "eori" -> optional(
      text()
        .verifying("supplementary.eori.empty", _.trim.nonEmpty)
        .verifying("supplementary.eori.error", validateField(17))
    ),
    "fullName" -> optional(
      text()
        .verifying("supplementary.address.fullName.empty", _.trim.nonEmpty)
        .verifying("supplementary.address.fullName.error", validateField(70))
    ),
    "addressLine" -> optional(
      text()
        .verifying("supplementary.address.addressLine.empty", _.trim.nonEmpty)
        .verifying("supplementary.address.addressLine.error", validateField(70))
    ),
    "townOrCity" -> optional(
      text()
        .verifying("supplementary.address.townOrCity.empty", _.trim.nonEmpty)
        .verifying("supplementary.address.townOrCity.error", validateField(35))
    ),
    "postCode" -> optional(
      text()
        .verifying("supplementary.address.postCode.empty", _.trim.nonEmpty)
        .verifying("supplementary.address.postCode.error", validateField(9))
    ),
    "country" -> optional(
      text()
        .verifying("supplementary.address.country.empty", _.trim.nonEmpty)
        .verifying(
          "supplementary.address.country.error",
          input => input.isEmpty || allCountries.exists(country => country.countryName == input)
        )
    )
  )(AddressAndIdentification.apply)(AddressAndIdentification.unapply)

  private def validateField: Int => String => Boolean =
    (length: Int) => (input: String) => input.length <= length && isAlphanumeric(input.replaceAll(" ", ""))

  def toExporterMetadataProperties(address: AddressAndIdentification): Map[String, String] =
    Map(
      "declaration.goodsShipment.governmentAgencyGoodsItems[0].consignor.id" -> address.eori.getOrElse(""),
      "declaration.goodsShipment.governmentAgencyGoodsItems[0].consignor.name" -> address.fullName.getOrElse(""),
      "declaration.goodsShipment.governmentAgencyGoodsItems[0].consignor.address.line" -> address.addressLine.getOrElse(""),
      "declaration.goodsShipment.governmentAgencyGoodsItems[0].consignor.address.cityName" -> address.townOrCity
        .getOrElse(""),
      "declaration.goodsShipment.governmentAgencyGoodsItems[0].consignor.address.postcodeId" -> address.postCode
        .getOrElse(""),
      "declaration.goodsShipment.governmentAgencyGoodsItems[0].consignor.address.countryCode" ->
        allCountries.find(country => address.country.contains(country.countryName)).map(_.countryCode).getOrElse("")
    )

  def toDeclarantMetadataProperties(address: AddressAndIdentification): Map[String, String] =
    Map(
      "declaration.declarant.id" -> address.eori.getOrElse(""),
      "declaration.declarant.name" -> address.fullName.getOrElse(""),
      "declaration.declarant.address.line" -> address.addressLine.getOrElse(""),
      "declaration.declarant.address.cityName" -> address.townOrCity.getOrElse(""),
      "declaration.declarant.address.postcodeId" -> address.postCode.getOrElse(""),
      "declaration.declarant.address.countryCode" ->
        allCountries.find(country => address.country.contains(country.countryName)).map(_.countryCode).getOrElse("")
    )

  def toConsigneeMetadataProperties(address: AddressAndIdentification): Map[String, String] = ???
}
