/*
 * Copyright 2020 HM Revenue & Customs
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

package forms.declaration.countries

import forms.DeclarationPage
import models.DeclarationType._
import models.requests.JourneyRequest
import play.api.data.{Form, Forms, Mapping}
import play.api.data.Forms.{optional, text}
import play.api.mvc.AnyContent
import services.Countries.allCountries

object Countries {

  sealed trait CountryPage extends DeclarationPage {
    val id: String
  }

  case object OriginationCountryPage extends CountryPage {
    override val id = "originationCountry"
  }

  case object DestinationCountryPage extends CountryPage {
    override val id = "destinationCountry"
  }

  case object FirstRoutingCountryPage extends CountryPage {
    override val id = "firstRoutingCountry"
  }

  case object NextRoutingCountryPage extends CountryPage {
    override val id = "routingCountry"
  }

  private def mapping(page: CountryPage, cachedCountries: Seq[Country] = Seq.empty): Mapping[String] =
    text()
      .verifying(s"declaration.${page.id}.empty", _.trim.nonEmpty)
      .verifying(s"declaration.${page.id}.error", emptyOrValidCountry)
      .verifying(s"declaration.routingCountries.duplication", !cachedCountries.flatMap(_.code).contains(_))
      .verifying(s"declaration.routingCountries.limit", _ => cachedCountries.length < limit)

  private def mandatoryMapping(page: CountryPage, cachedCountries: Seq[Country] = Seq.empty): Mapping[Country] =
    Forms.mapping("code" -> mapping(page, cachedCountries))(country => if (country.nonEmpty) Country(Some(country)) else Country(None))(
      country => country.code
    )

  private def optionalForm(page: CountryPage, cachedCountries: Seq[Country] = Seq.empty): Mapping[Country] =
    Forms.mapping("code" -> optional(mapping(page, cachedCountries)))(Country.apply)(Country.unapply)

  def form(page: CountryPage, cachedCountries: Seq[Country] = Seq.empty)(implicit request: JourneyRequest[_]): Form[Country] =
    request.declarationType match {
      case CLEARANCE => Form(optionalForm(page, cachedCountries))
      case _         => Form(mandatoryMapping(page, cachedCountries))
    }

  val limit = 99

  private def emptyOrValidCountry: String => Boolean = input => input.isEmpty || allCountries.exists(_.countryCode == input)
}
