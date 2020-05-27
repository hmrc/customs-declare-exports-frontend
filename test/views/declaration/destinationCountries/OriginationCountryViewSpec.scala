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

package views.declaration.destinationCountries

import base.Injector
import controllers.declaration.routes
import forms.declaration.countries.{Countries, Country}
import forms.declaration.countries.Countries.OriginationCountryPage
import models.DeclarationType.{STANDARD, SUPPLEMENTARY}
import models.Mode
import models.requests.JourneyRequest
import play.api.data.Form
import play.twirl.api.Html
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.destinationCountries.origination_country

class OriginationCountryViewSpec extends UnitViewSpec with Stubs with ExportsTestData with Injector {

  val originationCountryPage = instanceOf[origination_country]

  private def form(request: JourneyRequest[_]): Form[Country] =
    Countries.form(OriginationCountryPage)(request)
  private def view(request: JourneyRequest[_]): Html =
    originationCountryPage(Mode.Normal, form(request))(request, messages)

  "Origination country view spec" should {

    "have defined translation for used labels" in {

      val messages = realMessagesApi.preferred(request)
      messages must haveTranslationFor("declaration.originationCountry.title")
      messages must haveTranslationFor("declaration.originationCountry.heading")
      messages must haveTranslationFor("declaration.originationCountry.empty")
      messages must haveTranslationFor("declaration.originationCountry.error")
    }
  }

  onJourney(STANDARD, SUPPLEMENTARY) { request =>
    "Origination country view spec" should {

      s"display page question for ${request.declarationType}" in {

        view(request).getElementsByClass("govuk-fieldset__legend").text() mustBe messages("declaration.originationCountry.title")
      }

      s"display page heading for ${request.declarationType}" in {

        view(request).getElementById("section-header").text() must include(messages("declaration.originationCountry.heading"))
      }

      s"display back button that links to 'Declaration Holder' page for ${request.declarationType}" in {

        val backButton = view(request).getElementById("back-link")

        backButton.text() mustBe messages("site.back")
        backButton must haveHref(routes.DeclarationHolderController.displayPage())
      }

      s"display 'Save and continue' button for ${request.declarationType}" in {

        view(request).getElementById("submit").text() mustBe messages("site.save_and_continue")
      }

      s"display 'Save and return' button for ${request.declarationType}" in {

        view(request).getElementById("submit_and_return").text() mustBe messages("site.save_and_come_back_later")
      }
    }
  }
}
