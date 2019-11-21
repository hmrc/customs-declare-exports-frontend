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

import forms.DeclarationPage
import play.api.data.{Form, FormError}
import play.api.data.Forms.{single, text}
import services.Countries.allCountries

object DestinationCountries {

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

  def form(page: CountryPage): Form[String] = Form(
    single(
      "country" -> text()
        .verifying(s"declaration.${page.id}.empty", _.trim.nonEmpty)
        .verifying(s"declaration.${page.id}.error", emptyOrValidCountry)
    )
  )

  def validateCountryDuplication(form: Form[String], cachedCountries: Seq[String]): Form[String] = {
    val isCountryDuplicated = form.value.exists(cachedCountries.contains(_))

    if (isCountryDuplicated) form.copy(errors = Seq(FormError("country", "declaration.routingCountries.duplication")))
    else form
  }

  def validateCountriesLimit(questionForm: Form[Boolean], cachedCountries: Seq[String]): Form[Boolean] =
    if (cachedCountries.length >= limit) questionForm.copy(errors = Seq(FormError("country", "declaration.routingCountries.limit")))
    else questionForm

  private val limit = 99

  private def emptyOrValidCountry: String => Boolean = { input =>
    input.isEmpty || allCountries.exists(_.countryCode == input)
  }
}
