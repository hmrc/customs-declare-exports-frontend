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
import forms.declaration.countries.Countries.{FirstRoutingCountryPage, NextRoutingCountryPage}
import forms.declaration.countries.{Countries, Country}
import models.codes.{Country => ModelCountry}
import models.DeclarationType.{OCCASIONAL, SIMPLIFIED, STANDARD}
import models.Mode
import models.requests.JourneyRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.twirl.api.Html
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.destinationCountries.change_routing_country

import scala.collection.immutable.ListMap

class ChangeRoutingCountryViewSpec extends UnitViewSpec with Stubs with ExportsTestData with Injector with BeforeAndAfterEach {

  implicit val mockCodeListConnector = mock[CodeListConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> ModelCountry("United Kingdom", "GB")))
  }

  override protected def afterEach(): Unit = {
    reset(mockCodeListConnector)
    super.afterEach()
  }

  private val countryToChange = "GB"
  private val changeRoutingCountryPage = instanceOf[change_routing_country]

  private def firstRoutingForm(request: JourneyRequest[_]): Form[Country] =
    Countries.form(FirstRoutingCountryPage)(request, messages(request), mockCodeListConnector)
  private def nextRoutingForm(request: JourneyRequest[_]): Form[Country] =
    Countries.form(NextRoutingCountryPage)(request, messages(request), mockCodeListConnector)

  private def firstRoutingView(implicit request: JourneyRequest[_]): Html =
    changeRoutingCountryPage(Mode.Normal, firstRoutingForm(request), FirstRoutingCountryPage, countryToChange)(request, messages)
  private def nextRoutingView(implicit request: JourneyRequest[_]): Html =
    changeRoutingCountryPage(Mode.Normal, nextRoutingForm(request), NextRoutingCountryPage, countryToChange)(request, messages)

  onJourney(OCCASIONAL, SIMPLIFIED, STANDARD) { implicit request =>
    "Change routing country pages" should {

      s"have page heading for ${request.declarationType}" in {

        firstRoutingView(request).getElementById("section-header").text() must include(messages("declaration.section.3"))
        nextRoutingView(request).getElementById("section-header").text() must include(messages("declaration.section.3"))
      }

      s"have page question during changing first routing country for ${request.declarationType}" in {

        firstRoutingView(request).getElementsByTag("h1") must containMessageForElements("declaration.firstRoutingCountry.question")
      }

      s"have page question during changing next routing country for ${request.declarationType}" in {

        nextRoutingView(request).getElementsByTag("h1") must containMessageForElements("declaration.routingCountry.question")
      }

      s"display back button that links to 'Countries summary' for first routing country page for ${request.declarationType}" in {

        val backButton = firstRoutingView(request).getElementById("back-link")

        backButton.text() mustBe messages("site.back")
        backButton must haveHref(routes.RoutingCountriesController.displayRoutingCountry())
      }

      s"display back button that links to 'Countries summary' for next routing country page for ${request.declarationType}" in {

        val backButton = firstRoutingView(request).getElementById("back-link")

        backButton.text() mustBe messages("site.back")
        backButton must haveHref(routes.RoutingCountriesController.displayRoutingCountry())
      }

      s"display 'Save and continue' button for ${request.declarationType}" in {

        firstRoutingView(request).getElementById("submit").text() mustBe messages("site.save_and_continue")
        nextRoutingView(request).getElementById("submit").text() mustBe messages("site.save_and_continue")
      }

      s"display 'Save and return' button for ${request.declarationType}" in {

        firstRoutingView(request).getElementById("submit_and_return").text() mustBe messages("site.save_and_come_back_later")
        nextRoutingView(request).getElementById("submit_and_return").text() mustBe messages("site.save_and_come_back_later")
      }
    }
  }
}
