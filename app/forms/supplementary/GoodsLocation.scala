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

import play.api.data.Form
import play.api.data.Forms
import play.api.data.Forms.{optional, text}
import play.api.libs.json.Json
import services.Countries.allCountries
import utils.validators.FormFieldValidator._

case class GoodsLocation(
  country: Option[String],
  typeOfLocation: Option[String],
  qualifierOfIdentification: Option[String],
  identificationOfLocation: Option[String],
  additionalIdentifier: Option[String],
  streetAndNumber: Option[String],
  postCode: Option[String],
  city: Option[String]
)

object GoodsLocation {
  implicit val format = Json.format[GoodsLocation]

  val formId = "GoodsLocation"

  val mapping = Forms.mapping(
    "country" -> optional(
      text()
        .verifying("supplementary.country.empty", _.trim.nonEmpty)
        .verifying(
          "supplementary.country.error",
          input => input.trim.isEmpty || allCountries.filter(country => country.countryName == input).nonEmpty
        )
    ),
    "typeOfLocation" -> optional(
      text().verifying("supplementary.goodsLocation.typeOfLocation.error", isAlphabetic and hasSpecificLength(1))
    ),
    "qualifierOfIdentification" -> optional(
      text()
        .verifying("supplementary.goodsLocation.qualifierOfIdentification.error", isAlphabetic and hasSpecificLength(1))
    ),
    "identificationOfLocation" -> optional(
      text().verifying(
        "supplementary.goodsLocation.identificationOfLocation.error",
        isAlphanumeric and hasSpecificLength(3)
      )
    ),
    "additionalIdentifier" -> optional(
      text().verifying("supplementary.goodsLocation.additionalIdentifier.error", isAlphanumeric and noLongerThan(32))
    ),
    "streetAndNumber" -> optional(
      text().verifying("supplementary.goodsLocation.streetAndNumber.error", isAlphanumeric and noLongerThan(70))
    ),
    "postCode" -> optional(
      text().verifying("supplementary.goodsLocation.postCode.error", isAlphanumeric and noLongerThan(9))
    ),
    "city" -> optional(
      text()
        .verifying("supplementary.goodsLocation.city.error", isAlphanumeric and noLongerThan(35))
    )
  )(GoodsLocation.apply)(GoodsLocation.unapply)

  def form(): Form[GoodsLocation] = Form(mapping)

  def toMetadataProperties(location: GoodsLocation): Map[String, String] =
    Map(
      "declaration.goodsShipment.consignment.goodsLocation.address.countryCode" -> location.country.getOrElse(""),
      "declaration.goodsShipment.consignment.goodsLocation.typeCode" -> location.typeOfLocation.getOrElse(""),
      "declaration.goodsShipment.consignment.goodsLocation.address.typeCode" -> location.qualifierOfIdentification
        .getOrElse(""),
      "declaration.goodsShipment.consignment.goodsLocation.name" -> location.identificationOfLocation.getOrElse(""),
      "declaration.goodsShipment.consignment.goodsLocation.ID" -> location.additionalIdentifier.getOrElse(""),
      "declaration.goodsShipment.consignment.goodsLocation.address.line" -> location.streetAndNumber.getOrElse(""),
      "declaration.goodsShipment.consignment.goodsLocation.address.postCodeID" -> location.postCode.getOrElse(""),
      "declaration.goodsShipment.consignment.goodsLocation.address.cityName" -> location.city.getOrElse("")
    )
}
