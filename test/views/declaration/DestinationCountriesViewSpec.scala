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

package views.declaration

import base.Injector
import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.declaration.destinationCountries.DestinationCountries
import forms.declaration.destinationCountries.DestinationCountries.OriginationCountryPage
import helpers.views.declaration.CommonMessages
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.destination_countries_standard
import views.tags.ViewTest

@ViewTest
class DestinationCountriesViewSpec extends UnitViewSpec with ExportsTestData with CommonMessages with Stubs with Injector {

  private val form: Form[String] = DestinationCountries.form(OriginationCountryPage)
  private val destiantionCountriesStandardPage = new destination_countries_standard(mainTemplate)
  private val view: Document =
    destiantionCountriesStandardPage(Mode.Normal, form)(journeyRequest(), messages)

  "Destination countries" should {

    "have correct message keys" in {

      val messages = instanceOf[MessagesApi].preferred(journeyRequest())

      messages must haveTranslationFor("declaration.destinationCountries.title")
      messages must haveTranslationFor("declaration.destinationCountries.routing")
      messages must haveTranslationFor("declaration.destinationCountries.routing.hint")
      messages must haveTranslationFor("declaration.destinationCountries.countriesOfRouting.error")
      messages must haveTranslationFor("declaration.destinationCountries.countriesOfRouting.empty")
    }
  }

  "Destination Countries View on empty page" should {

    "display page title" in {

      view.getElementById("title").text() mustBe messages("declaration.destinationCountries.title")
    }

    "display empty input with label for routing countries" in {

      view.getElementById("country-hint").text() mustBe messages("declaration.destinationCountries.routing.hint")
      view.getElementById("country-label").ownText() mustBe messages("declaration.destinationCountries.routing")
    }

    "display 'Back' button that links to 'Origination Country' page" in {

      val backButton = view.getElementById("link-back")

      backButton.text() mustBe messages(backCaption)
      backButton.attr("href") mustBe routes.DestinationCountryController.displayPage().url
    }

    "display 'Save and continue' button" in {
      val saveButton = view.getElementById("submit")
      saveButton.text() mustBe messages(saveAndContinueCaption)
    }

    "display 'Save and return' button" in {
      val saveButton = view.getElementById("submit_and_return")
      saveButton.text() mustBe messages(saveAndReturnCaption)
      saveButton.attr("name") mustBe SaveAndReturn.toString
    }
  }
}
