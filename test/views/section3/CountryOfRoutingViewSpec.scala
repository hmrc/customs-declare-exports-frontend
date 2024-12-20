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

package views.section3

import base.Injector
import connectors.CodeListConnector
import controllers.section3.routes.RoutingCountriesController
import forms.common.{Countries, Country}
import forms.common.Countries.RoutingCountryPage
import models.DeclarationType.{OCCASIONAL, SIMPLIFIED, STANDARD}
import models.codes.{Country => ModelCountry}
import models.requests.JourneyRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.data.Form
import play.twirl.api.Html
import services.cache.ExportsTestHelper
import views.html.section3.country_of_routing
import views.common.PageWithButtonsSpec

import scala.collection.immutable.ListMap

class CountryOfRoutingViewSpec extends PageWithButtonsSpec with ExportsTestHelper with Injector {

  implicit val mockCodeListConnector: CodeListConnector = mock[CodeListConnector]

  val expectedCountryName = "Mauritius"

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("MU" -> ModelCountry(expectedCountryName, "MU")))
  }

  override protected def afterEach(): Unit = {
    reset(mockCodeListConnector)
    super.afterEach()
  }

  private val page = instanceOf[country_of_routing]

  override val typeAndViewInstance = (STANDARD, page(routingForm(request))(_, _))

  private def createView()(implicit request: JourneyRequest[_]): Html =
    page(routingForm(request))(request, messages)

  private def routingForm(request: JourneyRequest[_]): Form[Country] =
    Countries.form(RoutingCountryPage)(request, messages(request), mockCodeListConnector)

  "Routing Country view" should {

    "have defined translation for used labels" in {
      val messages = realMessagesApi.preferred(request)
      messages must haveTranslationFor("declaration.routingCountries.title")
      messages must haveTranslationFor("declaration.routingCountry.empty")
      messages must haveTranslationFor("declaration.routingCountries.body.p1")
      messages must haveTranslationFor("declaration.routingCountries.body.p2")
    }
  }

  onJourney(OCCASIONAL, SIMPLIFIED, STANDARD) { implicit request =>
    "Routing Country view" should {
      val view = createView()

      s"have page heading for ${request.declarationType}" in {
        view.getElementById("section-header").text() must include(messages("declaration.section.3"))
      }

      "display the expected page title" in {
        val view = createView()(journeyRequest(aDeclaration(withDestinationCountry(Country(Some("MU"))))))
        view.getElementsByTag("h1").text mustBe messages("declaration.routingCountries.title", expectedCountryName)
      }

      s"display back button that links to 'Country of Routing question' page  for ${request.declarationType}" in {
        val backButton = view.getElementById("back-link")

        backButton.text() mustBe messages("site.backToPreviousQuestion")
        backButton must haveHref(RoutingCountriesController.displayRoutingQuestion)
      }

      checkAllSaveButtonsAreDisplayed(createView())
    }
  }
}
