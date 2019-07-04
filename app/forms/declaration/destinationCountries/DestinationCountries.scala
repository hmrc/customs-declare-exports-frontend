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

import play.api.data.Forms.{default, seq, text}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import services.Countries.allCountries

case class DestinationCountries(
  countryOfDispatch: String,
  countriesOfRouting: Seq[String],
  countryOfDestination: String
)

object DestinationCountries {
  val limit = 99

  val formId = "DestinationCountries"

  implicit val format: OFormat[DestinationCountries] = Json.format[DestinationCountries]

  def apply(countryOfDispatch: String, countryOfDestination: String): DestinationCountries =
    DestinationCountries(countryOfDispatch, Seq.empty, countryOfDestination)

  private def emptyOrValidCountry: String => Boolean = { input =>
    input.isEmpty || allCountries.exists(_.countryCode == input)
  }

  object Supplementary {
    private def form2Object: (String, String) => DestinationCountries = {
      case (countryOfDispatch, countryOfDestination) =>
        DestinationCountries(countryOfDispatch, Seq.empty, countryOfDestination)
    }

    private def object2Form: DestinationCountries => Option[(String, String)] =
      d => Some((d.countryOfDispatch, d.countryOfDestination))

    val mapping: Mapping[DestinationCountries] = Forms.mapping(
      "countryOfDispatch" -> text()
        .verifying("declaration.destinationCountries.countryOfDispatch.empty", _.trim.nonEmpty)
        .verifying("declaration.destinationCountries.countryOfDispatch.error", emptyOrValidCountry),
      "countryOfDestination" -> text()
        .verifying("declaration.destinationCountries.countryOfDestination.empty", _.trim.nonEmpty)
        .verifying("declaration.destinationCountries.countryOfDestination.error", emptyOrValidCountry)
    )(form2Object)(object2Form)

    def form: Form[DestinationCountries] = Form(mapping)
  }

  object Standard {
    def form: Form[DestinationCountries] = Form(mapping)

    val mapping: Mapping[DestinationCountries] = Forms.mapping(
      "countryOfDispatch" -> text()
        .verifying("declaration.destinationCountries.countryOfDispatch.error", emptyOrValidCountry),
      "countriesOfRouting" -> default(seq(text()), Seq.empty),
      "countryOfDestination" -> text()
        .verifying("declaration.destinationCountries.countryOfDispatch.error", emptyOrValidCountry)
    )(DestinationCountries.apply)(DestinationCountries.unapply)
  }

  def empty(): DestinationCountries = DestinationCountries("", Seq.empty, "")
}
