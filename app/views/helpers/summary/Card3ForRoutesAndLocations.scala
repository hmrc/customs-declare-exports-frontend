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

package views.helpers.summary

import connectors.CodeListConnector
import controllers.section3.routes._
import controllers.section4.routes.{InvoiceAndExchangeRateChoiceController, PreviousDocumentsSummaryController}
import forms.section3.LocationOfGoods.suffixForGVMS
import models.DeclarationType._
import models.ExportsDeclaration
import models.declaration.Locations
import models.requests.JourneyRequest
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.{Html, HtmlFormat}
import services.Countries
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import views.helpers.CountryHelper
import views.html.summary.summary_card

import javax.inject.{Inject, Singleton}

@Singleton
class Card3ForRoutesAndLocations @Inject() (summaryCard: summary_card, countryHelper: CountryHelper)(implicit codeListConnector: CodeListConnector)
    extends SummaryCard {

  // Called by the Final CYA page
  def eval(declaration: ExportsDeclaration, actionsEnabled: Boolean = true)(implicit messages: Messages): Html = {
    val locations = declaration.locations
    val hasData =
      locations.hasRoutingCountries.isDefined ||
        locations.routingCountries.nonEmpty ||
        locations.destinationCountry.isDefined ||
        locations.goodsLocation.isDefined |
        locations.officeOfExit.isDefined

    if (hasData) content(declaration, actionsEnabled) else HtmlFormat.empty
  }

  // Called by the Mini CYA page
  override def content(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Html =
    summaryCard(card(3), rows(declaration, actionsEnabled))

  override def backLink(implicit request: JourneyRequest[_]): Call = OfficeOfExitController.displayPage

  override def continueTo(implicit request: JourneyRequest[_]): Call =
    request.declarationType match {
      case SUPPLEMENTARY | STANDARD            => InvoiceAndExchangeRateChoiceController.displayPage
      case OCCASIONAL | SIMPLIFIED | CLEARANCE => PreviousDocumentsSummaryController.displayPage
    }

  private def rows(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Seq[SummarySection] =
    List(
      maybeSummarySection(
        List(
          routingCountries(declaration.locations, actionsEnabled),
          destinationCountry(declaration.locations, actionsEnabled),
          goodsLocation(declaration, actionsEnabled),
          additionalInformation(declaration.locations),
          officeOfExit(declaration.locations, actionsEnabled)
        )
      )
    ).flatten

  private def routingCountries(locations: Locations, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    locations.hasRoutingCountries.map { _ =>
      lazy val countries = Countries
        .findByCodes(locations.routingCountries.flatMap(_.country.code))
        .map(countryHelper.getShortNameForCountry)
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
      lazy val countryName: String = {
        for {
          code <- destinationCountry.code
          country <- Countries.findByCode(code)
          shortName = countryHelper.getShortNameForCountry(country)
        } yield shortName
      }.getOrElse("")

      SummaryListRow(
        key("countries.countryOfDestination"),
        value(countryName),
        classes = "destination-country",
        changeLink(DestinationCountryController.displayPage, "countries.countryOfDestination", actionsEnabled)
      )
    }

  private def goodsLocation(declaration: ExportsDeclaration, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    declaration.locations.goodsLocation.map { goodsLocation =>
      val cssNoBorderOnGVMS = if (goodsLocation.value.endsWith(suffixForGVMS)) "govuk-summary-list__row--no-border " else ""
      val actions =
        if (declaration.isAmendmentDraft) None
        else changeLink(LocationOfGoodsController.displayPage, "locations.goodsLocationCode", actionsEnabled)

      SummaryListRow(key("locations.goodsLocationCode"), value(goodsLocation.value), classes = s"${cssNoBorderOnGVMS}goods-location-code", actions)
    }

  private def additionalInformation(locations: Locations)(implicit messages: Messages): Option[SummaryListRow] =
    locations.goodsLocation.find(_.value.endsWith(suffixForGVMS)).map { _ =>
      SummaryListRow(
        key("locations.rrs01AdditionalInformation"),
        valueKey("declaration.summary.locations.rrs01AdditionalInformation.text"),
        classes = "rrs01-additional-information"
      )
    }

  private def officeOfExit(locations: Locations, actionsEnabled: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    locations.officeOfExit.map { officeOfExit =>
      SummaryListRow(
        key("locations.officeOfExit"),
        value(officeOfExit.officeId),
        classes = "office-of-exit",
        changeLink(OfficeOfExitController.displayPage, "locations.officeOfExit", actionsEnabled)
      )
    }
}
