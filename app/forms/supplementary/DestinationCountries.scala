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

case class DestinationCountries(countryOfDispatch: String, countryOfDestination:String)
    extends MetadataPropertiesConvertable {

  override def toMetadataProperties(): Map[String, String] =
    Map(
      "declaration.goodsShipment.exportCountry.id" ->
        allCountries.find(country => countryOfDispatch.contains(country.countryName)).map(_.countryCode).getOrElse(""),
      "declaration.goodsShipment.destination.countryCode" ->
        allCountries
          .find(country => countryOfDestination.contains(country.countryName))
          .map(_.countryCode)
          .getOrElse("")
    )
}

object DestinationCountries {
  implicit val format = Json.format[DestinationCountries]

  val formId = "DestinationCountries"

  val mapping = Forms.mapping(
    "countryOfDispatch" -> text()
      .verifying("supplementary.destinationCountries.countryOfDispatch.empty", _.trim.nonEmpty)
      .verifying(
        "supplementary.destinationCountries.countryOfDispatch.error",
        input => input.isEmpty || allCountries.exists(country => country.countryName == input)
      ),
    "countryOfDestination" -> text()
      .verifying("supplementary.destinationCountries.countryOfDestination.empty", _.trim.nonEmpty)
      .verifying(
        "supplementary.destinationCountries.countryOfDestination.error",
        input => input.isEmpty || allCountries.exists(country => country.countryName == input)
      )
  )(DestinationCountries.apply)(DestinationCountries.unapply)

  def form(): Form[DestinationCountries] = Form(mapping)
}
