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

import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import services.Countries.allCountries

case class DestinationCountries(countryOfDestination: Option[String], countryOfDispatch: String)

object DestinationCountries {
  implicit val format = Json.format[DestinationCountries]

  val formId = "DestinationCountries"

  val mapping = Forms.mapping(
    "countryOfDestination" -> optional(
      text().verifying(
        "supplementary.destinationCountries.countryOfDestination.error",
        input => input.isEmpty || !allCountries.filter(country => country.countryName == input).isEmpty
      )
    ),
    "countryOfDispatch" -> text()
      .verifying("supplementary.destinationCountries.countryOfDispatch.empty", _.trim.nonEmpty)
      .verifying(
        "supplementary.destinationCountries.countryOfDispatch.error",
        input => input.isEmpty || !allCountries.filter(country => country.countryName == input).isEmpty
      )
  )(DestinationCountries.apply)(DestinationCountries.unapply)

  def form(): Form[DestinationCountries] = Form(mapping)

  def toMetadataProperites(countries: DestinationCountries): Map[String, String] =
    Map(
      "declaration.goodsShipment.destination.countryCode" -> countries.countryOfDestination.getOrElse(""),
      "declaration.goodsShipment.exportCountry.ID" -> countries.countryOfDispatch
    )
}
