/*
 * Copyright 2023 HM Revenue & Customs
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

package views.helpers.summary

import connectors.CodeListConnector
import controllers.declaration.routes.{DestinationCountryController, RoutingCountriesController}
import models.ExportsDeclaration
import models.declaration.Locations
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import services.Countries
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukSummaryList, SummaryList}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import views.helpers.CountryHelper

import javax.inject.{Inject, Singleton}

@Singleton
class Card3ForRouteOfGoods @Inject() (govukSummaryList: GovukSummaryList, countryHelper: CountryHelper)(implicit codeListConnector: CodeListConnector)
    extends SummaryHelper {

  def eval(declaration: ExportsDeclaration, actionsEnabled: Boolean = true)(implicit messages: Messages): Html = {
    val locations = declaration.locations
    val hasData =
      locations.hasRoutingCountries.isDefined || locations.routingCountries.nonEmpty || locations.destinationCountry.isDefined

    if (hasData) displayCard(locations, actionsEnabled) else HtmlFormat.empty
  }

  private def displayCard(locations: Locations, actionsEnabled: Boolean)(implicit messages: Messages): Html =
    govukSummaryList(SummaryList(rows(locations, actionsEnabled), card("countries")))

  private def rows(locations: Locations, actionsEnabled: Boolean)(implicit messages: Messages): Seq[SummaryListRow] =
    List(routingCountries(locations, actionsEnabled), destinationCountry(locations, actionsEnabled)).flatten

  private def routingCountries(locations: Locations, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    locations.hasRoutingCountries.map { _ =>
      lazy val countries = Countries
        .findByCodes(locations.routingCountries.flatMap(_.country.code))
        .map(countryHelper.getShortNameForCountry(_))
        .mkString(", ")

      SummaryListRow(
        key("countries.routingCountries"),
        if (locations.routingCountries.isEmpty) valueKey("site.none") else value(countries),
        classes = "routing-countries",
        changeLink(RoutingCountriesController.submitRoutingCountry, "countries.routingCountries", actionsEnabled)
      )
    }

  private def destinationCountry(locations: Locations, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    locations.destinationCountry.map { destinationCountry =>
      lazy val country = destinationCountry.code.map { code =>
        countryHelper.getShortNameForCountry(Countries.findByCode(code))
      }.getOrElse("")

      SummaryListRow(
        key("countries.countryOfDestination"),
        value(country),
        classes = "destination-country",
        changeLink(DestinationCountryController.displayPage, "countries.countryOfDestination", actionsEnabled)
      )
    }
}
