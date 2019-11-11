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

import play.api.data.Forms.{default, ignored, seq, single, text}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import services.Countries.allCountries

case class DestinationCountries(countryOfDispatch: String, countriesOfRouting: Seq[String], countryOfDestination: String)

object DestinationCountries {

  /**
    * New structure foe handling Destination Countries
    */
  sealed trait CountryPage {
    val id: String
  }

  case object OriginationCountryPage extends CountryPage {
    override val id = "originationCountry"
  }

  case object DestinationCountryPage extends CountryPage {
    override val id = "destinationCountry"
  }

  def form(page: CountryPage): Form[String] = Form(
    single(
      "country" -> text()
        .verifying(s"declaration.${page.id}.empty", _.trim.nonEmpty)
        .verifying(s"declaration.${page.id}.error", emptyOrValidCountry)
    )
  )

  /**
    * Old structure that is only for time being during migration screens to new structure
    */
  val limit = 99

  implicit val format: OFormat[DestinationCountries] = Json.format[DestinationCountries]

  private def emptyOrValidCountry: String => Boolean = { input =>
    input.isEmpty || allCountries.exists(_.countryCode == input)
  }

  object Standard {
    def form(countryOfDispatch: String, countryOfDestination: String): Form[DestinationCountries] = Form(
      Forms.mapping(
        "countryOfDispatch" -> ignored(countryOfDispatch),
        "countriesOfRouting" -> default(seq(text()), Seq.empty),
        "countryOfDestination" -> ignored(countryOfDestination)
      )(DestinationCountries.apply)(DestinationCountries.unapply)
    )
  }

  def empty(): DestinationCountries = DestinationCountries("", Seq.empty, "")
}
