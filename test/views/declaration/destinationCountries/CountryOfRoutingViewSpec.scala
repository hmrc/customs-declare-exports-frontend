/*
 * Copyright 2022 HM Revenue & Customs
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

package views.declaration.destinationCountries

import base.Injector
import connectors.CodeListConnector
import controllers.declaration.routes
import forms.declaration.countries.Countries.RoutingCountryPage
import forms.declaration.countries.{Countries, Country}
import models.codes.{Country => ModelCountry}
import models.DeclarationType.{OCCASIONAL, SIMPLIFIED, STANDARD}
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.twirl.api.Html
import services.cache.ExportsTestHelper
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.destinationCountries.country_of_routing

import scala.collection.immutable.ListMap

class CountryOfRoutingViewSpec extends UnitViewSpec with Stubs with ExportsTestHelper with Injector with BeforeAndAfterEach {

  implicit val mockCodeListConnector = mock[CodeListConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> ModelCountry("United Kingdom", "GB")))
  }

  override protected def afterEach(): Unit = {
    reset(mockCodeListConnector)
    super.afterEach()
  }

  private val countryOfRoutingPage = instanceOf[country_of_routing]

  private def routingForm(request: JourneyRequest[_]): Form[Country] =
    Countries.form(RoutingCountryPage)(request, messages(request), mockCodeListConnector)

  private def createView(mode: Mode = Mode.Normal)(implicit request: JourneyRequest[_]): Html =
    countryOfRoutingPage(mode, routingForm(request), "Somewhere", Seq.empty[ModelCountry])(request, messages)

  "Routing Country view" should {

    "have defined translation for used labels" in {

      val messages = realMessagesApi.preferred(request)
      messages must haveTranslationFor("declaration.routingCountries.title")
      messages must haveTranslationFor("declaration.routingCountry.empty")
      messages must haveTranslationFor("declaration.routingCountries.body.p1")
      messages must haveTranslationFor("declaration.routingCountries.body.p2")
      messages must haveTranslationFor("tariff.expander.title.clearance")
      messages must haveTranslationFor("tariff.declaration.countryOfRouting.common.text")
    }
  }

  onJourney(OCCASIONAL, SIMPLIFIED, STANDARD) { implicit request =>
    "Routing Country view" should {

      s"have page heading for ${request.declarationType}" in {

        createView()(request).getElementById("section-header").text() must include(messages("declaration.section.3"))
      }

      s"display back button that links to 'Country of Routing question' page  for ${request.declarationType}" in {

        val backButton = createView()(request).getElementById("back-link")

        backButton.text() mustBe messages("site.back")
        backButton must haveHref(routes.RoutingCountriesController.displayRoutingQuestion())
      }

      val createViewWithMode: Mode => Document = mode => createView(mode = mode)
      checkAllSaveButtonsAreDisplayed(createViewWithMode)
    }
  }
}
