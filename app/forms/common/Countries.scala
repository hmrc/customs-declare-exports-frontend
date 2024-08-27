/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.common

import connectors.CodeListConnector
import forms.DeclarationPage
import models.DeclarationType._
import models.requests.JourneyRequest
import models.viewmodels.TariffContentKey
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.Messages
import services.Countries.isValidCountryCode
import utils.validators.forms.FieldValidator._

object Countries {

  sealed trait CountryPage extends DeclarationPage {
    val id: String
  }

  case object DestinationCountryPage extends CountryPage {
    override val id = "destinationCountry"

    override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
      Seq(
        TariffContentKey(s"tariff.declaration.destinationCountry.1.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"),
        TariffContentKey(s"tariff.declaration.destinationCountry.2.${DeclarationPage.getJourneyTypeSpecialisation(decType)}")
      )
  }

  case object RoutingCountryPage extends CountryPage {
    override val id = "routingCountry"

    override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
      Seq(TariffContentKey("tariff.declaration.countryOfRouting.common"))
  }

  val fieldId = "countryCode"

  private val invalidDestinationCountry = "GB"

  private def mapping(
    page: CountryPage,
    cachedCountries: Seq[Country]
  )(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[String] = {
    val standardMapping = text()
      .verifying(s"declaration.${page.id}.empty", _.trim.nonEmpty)
      .verifying(s"declaration.${page.id}.error", isEmpty or isValidCountryCode _)
      .verifying(s"declaration.routingCountries.duplication", !cachedCountries.flatMap(_.code).contains(_))
      .verifying(s"declaration.routingCountries.limit", _ => cachedCountries.length < limit)

    page match {
      case DestinationCountryPage => standardMapping.verifying("declaration.destinationCountry.error.uk", _ != invalidDestinationCountry)
      case _                      => standardMapping
    }
  }

  private def mandatoryMapping(
    page: CountryPage,
    cachedCountries: Seq[Country]
  )(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[Country] =
    Forms.mapping(fieldId -> mapping(page, cachedCountries)) {
      // apply
      country => if (country.nonEmpty) Country(Some(country)) else Country(None)
    }(_.code) // unapply

  private def optionalForm(page: CountryPage, cachedCountries: Seq[Country])(
    implicit messages: Messages,
    codeListConnector: CodeListConnector
  ): Mapping[Country] =
    Forms.mapping(fieldId -> optional(mapping(page, cachedCountries)))(Country.apply)(Country.unapply)

  def form(
    page: CountryPage,
    cachedCountries: Seq[Country] = Seq.empty
  )(implicit request: JourneyRequest[_], messages: Messages, codeListConnector: CodeListConnector): Form[Country] =
    request.declarationType match {
      case CLEARANCE => Form(optionalForm(page, cachedCountries))
      case _         => Form(mandatoryMapping(page, cachedCountries))
    }

  val limit = 99
}
