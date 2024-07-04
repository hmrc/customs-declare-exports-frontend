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

import base.Injector
import controllers.section3.routes._
import controllers.section4.routes.{InvoiceAndExchangeRateChoiceController, PreviousDocumentsSummaryController}
import forms.declaration.countries.Country
import forms.section3.LocationOfGoods
import models.DeclarationType._
import models.declaration.DeclarationStatus.AMENDMENT_DRAFT
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec

class Card3ForRoutesAndLocationsSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  private val declaration =
    aDeclaration(
      withRoutingCountries(List(Country(Some("FR")), Country(Some("IT")))),
      withDestinationCountry(Country(Some("ES"))),
      withGoodsLocation(LocationOfGoods("GBAUEMAEMAEMA")),
      withOfficeOfExit("123")
    )

  private val card3ForRoutesAndLocations = instanceOf[Card3ForRoutesAndLocations]

  "'Routes and locations' section" should {
    val view = card3ForRoutesAndLocations.eval(declaration)(messages)

    "have the expected heading" in {
      view.getElementsByTag("h2").first.text mustBe messages(s"declaration.summary.section.3")
    }

    "notify the user when there are no routing countries" in {
      val declaration1 = declaration.copy(locations = declaration.locations.copy(routingCountries = List.empty))
      val row = card3ForRoutesAndLocations.eval(declaration1)(messages).getElementsByClass("routing-countries")

      val expectedValue = messages("site.none")
      val call = Some(RoutingCountriesController.submitRoutingCountry)
      checkSummaryRow(row, "countries.routingCountries", expectedValue, call, "countries.routingCountries")
    }

    "show the routing countries" in {
      val row = view.getElementsByClass("routing-countries")

      val call = Some(RoutingCountriesController.submitRoutingCountry)
      checkSummaryRow(row, "countries.routingCountries", "France, Italy", call, "countries.routingCountries")
    }

    "show the destination country" in {
      val row = view.getElementsByClass("destination-country")

      val call = Some(DestinationCountryController.displayPage)
      checkSummaryRow(row, "countries.countryOfDestination", "Spain", call, "countries.countryOfDestination")
    }

    "show the goods location code" in {
      val row = view.getElementsByClass("goods-location-code")

      val call = Some(LocationOfGoodsController.displayPage)
      checkSummaryRow(row, "locations.goodsLocationCode", "GBAUEMAEMAEMA", call, "locations.goodsLocationCode")
    }

    "show the goods location code without 'Change' link" when {
      "the declaration is under amendment" in {
        val declarationToAmend = aDeclarationAfter(declaration, withStatus(AMENDMENT_DRAFT))
        val view = card3ForRoutesAndLocations.eval(declarationToAmend)(messages)
        val row = view.getElementsByClass("goods-location-code")

        checkSummaryRow(row, "locations.goodsLocationCode", "GBAUEMAEMAEMA", None, "ign")
      }
    }

    "show the 'RRS01' additional information" when {
      "the goods location code ends with 'GVM'" in {
        val declaration = aDeclaration(withGoodsLocation(LocationOfGoods("GBAUABDABDABDGVM")), withOfficeOfExit("123"))
        val view = card3ForRoutesAndLocations.eval(declaration)(messages)

        val row1 = view.getElementsByClass("goods-location-code")
        assert(row1.first.hasClass("govuk-summary-list__row--no-border"))

        val row2 = view.getElementsByClass("rrs01-additional-information")

        val expectedValue = messages("declaration.summary.locations.rrs01AdditionalInformation.text")
        checkSummaryRow(row2, "locations.rrs01AdditionalInformation", expectedValue, None, "ign")
      }
    }

    "NOT show the 'RRS01' additional information" when {
      "the goods location code does not end with 'GVM'" in {
        view.getElementsByClass("locations.rrs01AdditionalInformation").size mustBe 0
      }
    }

    "show the office of exit" in {
      val row = view.getElementsByClass("office-of-exit")

      val call = Some(OfficeOfExitController.displayPage)
      checkSummaryRow(row, "locations.officeOfExit", "123", call, "locations.officeOfExit")
    }

    "NOT have change links" when {
      "'actionsEnabled' is false" in {
        val view = card3ForRoutesAndLocations.eval(declaration, false)(messages)
        view.getElementsByClass(summaryActionsClassName) mustBe empty
      }
    }

    "Card3ForRoutesAndLocationsSection.content" must {
      "return the html of the cya card" in {
        val cardContent = card3ForRoutesAndLocations.content(declaration)
        cardContent.getElementsByClass("routes-and-locations-card").text mustBe messages("declaration.summary.section.3")
      }
    }

    "Card3ForRoutesAndLocationsSection.continueTo" when {
      onJourney(OCCASIONAL, SIMPLIFIED, CLEARANCE) { implicit request =>
        s"${request.declarationType}" must {
          "go to DeclarantExporterController" in {
            card3ForRoutesAndLocations.continueTo mustBe PreviousDocumentsSummaryController.displayPage
          }
        }
      }
      onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
        s"${request.declarationType}" must {
          "go to DeclarantExporterController" in {
            card3ForRoutesAndLocations.continueTo mustBe InvoiceAndExchangeRateChoiceController.displayPage
          }
        }
      }
    }

    "Card3ForRoutesAndLocationsSection.backLink" must {
      "go to OfficeOfExitController" in {
        card3ForRoutesAndLocations.backLink(journeyRequest(aDeclarationAfter(declaration))) mustBe OfficeOfExitController.displayPage
      }
    }
  }
}
