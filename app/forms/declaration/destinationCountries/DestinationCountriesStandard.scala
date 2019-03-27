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

import play.api.data.Forms
import play.api.data.Forms.{default, seq, text}
import play.api.libs.json.Json

case class DestinationCountriesStandard(
  countryOfDispatch: String,
  countriesOfRouting: Seq[String],
  countryOfDestination: String
)

object DestinationCountriesStandard {
  implicit val format = Json.format[DestinationCountriesStandard]

  val limit = 99

  val mapping = Forms.mapping(
    "countryOfDispatch" -> text(),
    "countriesOfRouting" -> default(seq(text()), Seq.empty),
    "countryOfDestination" -> text()
  )(DestinationCountriesStandard.apply)(DestinationCountriesStandard.unapply)

  def empty(): DestinationCountriesStandard = DestinationCountriesStandard("", Seq.empty, "")
}
