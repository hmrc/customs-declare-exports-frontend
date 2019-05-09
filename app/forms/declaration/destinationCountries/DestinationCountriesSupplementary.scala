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

package forms.declaration.destinationCountries

import forms.MetadataPropertiesConvertable
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import services.Countries.allCountries

case class DestinationCountriesSupplementary(countryOfDispatch: String, countryOfDestination: String)
    extends MetadataPropertiesConvertable {

  override def toMetadataProperties(): Map[String, String] =
    Map(
      "declaration.goodsShipment.exportCountry.id" ->
        allCountries.find(country => countryOfDispatch.contains(country.countryCode)).map(_.countryCode).getOrElse(""),
      "declaration.goodsShipment.destination.countryCode" ->
        allCountries
          .find(country => countryOfDestination.contains(country.countryCode))
          .map(_.countryCode)
          .getOrElse("")
    )
}

object DestinationCountriesSupplementary {
  implicit val format = Json.format[DestinationCountriesSupplementary]

  val mapping = Forms.mapping(
    "countryOfDispatch" -> text()
      .verifying("declaration.destinationCountries.countryOfDispatch.empty", _.trim.nonEmpty)
      .verifying(
        "declaration.destinationCountries.countryOfDispatch.error",
        input => input.isEmpty || allCountries.exists(country => country.countryCode == input)
      ),
    "countryOfDestination" -> text()
      .verifying("declaration.destinationCountries.countryOfDestination.empty", _.trim.nonEmpty)
      .verifying(
        "declaration.destinationCountries.countryOfDestination.error",
        input => input.isEmpty || allCountries.exists(country => country.countryCode == input)
      )
  )(DestinationCountriesSupplementary.apply)(DestinationCountriesSupplementary.unapply)
}
