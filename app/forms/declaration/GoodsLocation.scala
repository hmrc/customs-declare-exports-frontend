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

package forms.declaration

import forms.MetadataPropertiesConvertable
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import services.Countries.allCountries
import utils.validators.forms.FieldValidator._

case class GoodsLocation(
  country: String,
  typeOfLocation: String,
  qualifierOfIdentification: String,
  identificationOfLocation: Option[String],
  additionalIdentifier: Option[String],
  streetAndNumber: Option[String],
  postCode: Option[String],
  city: Option[String]
) extends MetadataPropertiesConvertable {

  override def toMetadataProperties(): Map[String, String] =
    Map(
      "declaration.goodsShipment.consignment.goodsLocation.typeCode" -> typeOfLocation,
      "declaration.goodsShipment.consignment.goodsLocation.name" -> identificationOfLocation.getOrElse(""),
      "declaration.goodsShipment.consignment.goodsLocation.id" -> additionalIdentifier.getOrElse(""),
      "declaration.goodsShipment.consignment.goodsLocation.address.typeCode" -> qualifierOfIdentification,
      "declaration.goodsShipment.consignment.goodsLocation.address.line" -> streetAndNumber.getOrElse(""),
      "declaration.goodsShipment.consignment.goodsLocation.address.postcodeId" -> postCode.getOrElse(""),
      "declaration.goodsShipment.consignment.goodsLocation.address.cityName" -> city.getOrElse(""),
      "declaration.goodsShipment.consignment.goodsLocation.address.countryCode" ->
        allCountries.find(c => country.equals(c.countryName)).map(_.countryCode).getOrElse("")
    )
}

object GoodsLocation {
  implicit val format = Json.format[GoodsLocation]

  val formId = "GoodsLocation"

  val mapping = Forms.mapping(
    "country" ->
      text()
        .verifying("supplementary.address.country.empty", _.trim.nonEmpty)
        .verifying(
          "supplementary.address.country.error",
          input => input.trim.isEmpty || allCountries.exists(country => country.countryName == input)
        ),
    "typeOfLocation" -> text()
      .verifying("supplementary.goodsLocation.typeOfLocation.empty", nonEmpty)
      .verifying(
        "supplementary.goodsLocation.typeOfLocation.error",
        isEmpty or (isAlphabetic and hasSpecificLength(1))
      ),
    "qualifierOfIdentification" -> text()
      .verifying("supplementary.goodsLocation.qualifierOfIdentification.empty", nonEmpty)
      .verifying(
        "supplementary.goodsLocation.qualifierOfIdentification.error",
        isEmpty or (isAlphabetic and hasSpecificLength(1))
      ),
    "identificationOfLocation" -> optional(
      text()
        .verifying(
          "supplementary.goodsLocation.identificationOfLocation.error",
          isEmpty or (isAlphanumeric and hasSpecificLength(3))
        )
    ),
    "additionalIdentifier" -> optional(
      text()
        .verifying(
          "supplementary.goodsLocation.additionalIdentifier.error",
          input => isAlphanumeric(input.replaceAll(" ", "")) && noLongerThan(32)(input)
        )
    ),
    "streetAndNumber" -> optional(
      text().verifying(
        "supplementary.goodsLocation.streetAndNumber.error",
        input => isAlphanumeric(input.replaceAll(" ", "")) && noLongerThan(70)(input)
      )
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
}
