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

import forms.MetadataPropertiesConvertable
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
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
) extends MetadataPropertiesConvertable {

  override def toMetadataProperties(): Map[String, String] =
    Map(
      "declaration.goodsShipment.consignment.goodsLocation.address.countryCode" -> country.getOrElse(""),
      "declaration.goodsShipment.consignment.goodsLocation.typeCode" -> typeOfLocation.getOrElse(""),
      "declaration.goodsShipment.consignment.goodsLocation.address.typeCode" -> qualifierOfIdentification.getOrElse(""),
      "declaration.goodsShipment.consignment.goodsLocation.name" -> identificationOfLocation.getOrElse(""),
      "declaration.goodsShipment.consignment.goodsLocation.ID" -> additionalIdentifier.getOrElse(""),
      "declaration.goodsShipment.consignment.goodsLocation.address.line" -> streetAndNumber.getOrElse(""),
      "declaration.goodsShipment.consignment.goodsLocation.address.postCodeID" -> postCode.getOrElse(""),
      "declaration.goodsShipment.consignment.goodsLocation.address.cityName" -> city.getOrElse("")
    )
}

object GoodsLocation {
  implicit val format = Json.format[GoodsLocation]

  val formId = "GoodsLocation"

  val mapping = Forms.mapping(
    "country" -> optional(
      text()
        .verifying("supplementary.country.empty", _.trim.nonEmpty)
        .verifying(
          "supplementary.country.error",
          input => input.trim.isEmpty || allCountries.exists(country => country.countryName == input)
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
      text().verifying(
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
