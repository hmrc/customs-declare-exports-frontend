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

import base.{Injector, TestHelper}
import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.declaration.destinationCountries.DestinationCountries
import helpers.views.declaration.{CommonMessages, DestinationCountriesMessages}
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.destination_countries_supplementary
import views.tags.ViewTest

@ViewTest
class DestinationCountriesViewSpec
    extends UnitViewSpec with ExportsTestData with DestinationCountriesMessages with CommonMessages with Stubs with Injector {

  private val form: Form[DestinationCountries] = DestinationCountries.Supplementary.form
  private val destiantionCountriesSupplementaryPage = new destination_countries_supplementary(mainTemplate)
  private def createView(form: Form[DestinationCountries] = form): Document =
    destiantionCountriesSupplementaryPage(Mode.Normal, form)(journeyRequest(), messages)

  "Destination countries" should {

    "have correct message keys" in {

      val messages = instanceOf[MessagesApi].preferred(journeyRequest())

      messages must haveTranslationFor("declaration.destinationCountries.title")
      messages must haveTranslationFor("declaration.destinationCountries.countryOfDestination")
      messages must haveTranslationFor("declaration.destinationCountries.countryOfDestination.hint")
      messages must haveTranslationFor("declaration.destinationCountries.countryOfDestination.error")
      messages must haveTranslationFor("declaration.destinationCountries.countryOfDestination.empty")
      messages must haveTranslationFor("declaration.destinationCountries.countryOfDispatch")
      messages must haveTranslationFor("declaration.destinationCountries.countryOfDispatch.hint")
      messages must haveTranslationFor("declaration.destinationCountries.routing")
      messages must haveTranslationFor("declaration.destinationCountries.routing.hint")
      messages must haveTranslationFor("declaration.destinationCountries.countryOfDispatch.empty")
      messages must haveTranslationFor("declaration.destinationCountries.countryOfDispatch.error")
      messages must haveTranslationFor("declaration.destinationCountries.countriesOfRouting")
      messages must haveTranslationFor("declaration.destinationCountries.countriesOfRouting.hint")
      messages must haveTranslationFor("declaration.destinationCountries.countriesOfRouting.error")
      messages must haveTranslationFor("declaration.destinationCountries.countriesOfRouting.empty")
    }
  }

  "Destination Countries View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() mustBe messages(title)
    }

    "display empty input with label for Dispatch country" in {

      val view = createView()

      view.getElementById("countryOfDispatch-hint").text() mustBe messages(countryOfDispatchHint)
      view.getElementById("countryOfDispatch-label").ownText() mustBe messages(countryOfDispatch)
      view.getElementById("countryOfDispatch").attr("value") mustBe empty
    }

    "display empty input with label for Destination country" in {

      val view = createView()

      view.getElementById("countryOfDestination-hint").text() mustBe messages(countryOfDestinationHint)
      view.getElementById("countryOfDestination-label").ownText() mustBe messages(countryOfDestination)
      view.getElementById("countryOfDestination").attr("value") mustBe empty
    }

    "display 'Back' button that links to 'Declaration holder of authorisation' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() mustBe messages(backCaption)
      backButton.attr("href") mustBe routes.DeclarationHolderController.displayPage().url
    }

    "display 'Save and continue' button" in {
      val saveButton = createView().getElementById("submit")
      saveButton.text() mustBe messages(saveAndContinueCaption)
    }

    "display 'Save and return' button" in {
      val saveButton = createView().getElementById("submit_and_return")
      saveButton.text() mustBe messages(saveAndReturnCaption)
      saveButton.attr("name") mustBe SaveAndReturn.toString
    }
  }

  "Destination Countries View for invalid input" should {

    "display error when dispatch country is empty" in {

      val view =
        createView(DestinationCountries.Supplementary.form.fillAndValidate(DestinationCountries("", "DE")))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("countryOfDispatch", "#countryOfDispatch")

      view.select("span.error-message").text() mustBe messages(countryOfDispatchEmpty)
    }

    "display error when dispatch country is incorrect" in {

      val view = createView(
        DestinationCountries.Supplementary.form
          .fillAndValidate(DestinationCountries(TestHelper.createRandomAlphanumericString(10), "DE"))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("countryOfDispatch", "#countryOfDispatch")

      view.select("span.error-message").text() mustBe messages(countryOfDispatchError)
    }

    "display error when destination country is empty" in {

      val view =
        createView(DestinationCountries.Supplementary.form.fillAndValidate(DestinationCountries("DE", "")))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("countryOfDestination", "#countryOfDestination")

      view.select("span.error-message").text() mustBe messages(countryOfDestinationEmpty)
    }

    "display error when destination country is incorrect" in {

      val view = createView(
        DestinationCountries.Supplementary.form
          .fillAndValidate(DestinationCountries("DE", TestHelper.createRandomAlphanumericString(10)))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("countryOfDestination", "#countryOfDestination")

      view.select("span.error-message").text() mustBe messages(countryOfDestinationError)
    }

    "display errors when both countries are incorrect" in {

      val view = createView(
        DestinationCountries.Supplementary.form
          .fillAndValidate(DestinationCountries(TestHelper.createRandomAlphanumericString(10), TestHelper.createRandomAlphanumericString(10)))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("countryOfDispatch", "#countryOfDispatch")
      view must haveFieldErrorLink("countryOfDestination", "#countryOfDestination")

      val spanErrors = view.select("span.error-message")
      spanErrors.size() mustBe 2

      spanErrors.get(0).text() mustBe messages(countryOfDispatchError)
      spanErrors.get(1).text() mustBe messages(countryOfDestinationError)
    }

    "display errors when both countries are empty" in {

      val view =
        createView(DestinationCountries.Supplementary.form.fillAndValidate(DestinationCountries("", "")))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("countryOfDispatch", "#countryOfDispatch")
      view must haveFieldErrorLink("countryOfDestination", "#countryOfDestination")

      val spanErrors = view.select("span.error-message")
      spanErrors.size() mustBe 2

      spanErrors.get(0).text() mustBe messages(countryOfDispatchEmpty)
      spanErrors.get(1).text() mustBe messages(countryOfDestinationEmpty)
    }

    "display errors when dispatch country is empty and destination is incorrect" in {

      val view = createView(
        DestinationCountries.Supplementary.form
          .fillAndValidate(DestinationCountries("", TestHelper.createRandomAlphanumericString(10)))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("countryOfDispatch", "#countryOfDispatch")
      view must haveFieldErrorLink("countryOfDestination", "#countryOfDestination")

      val spanErrors = view.select("span.error-message")
      spanErrors.size() mustBe 2

      spanErrors.get(0).text() mustBe messages(countryOfDispatchEmpty)
      spanErrors.get(1).text() mustBe messages(countryOfDestinationError)
    }
  }

  "Destination Countries View when filled" should {

    "display data for both countries in inputs" in {

      val view =
        createView(DestinationCountries.Supplementary.form.fill(DestinationCountries("GB", "PL")))

      view.getElementById("countryOfDispatch").attr("value") mustBe "GB"
      view.getElementById("countryOfDestination").attr("value") mustBe "PL"
    }

    "display data only for dispatch country input" in {

      val view =
        createView(DestinationCountries.Supplementary.form.fill(DestinationCountries("GB", "")))

      view.getElementById("countryOfDispatch").attr("value") mustBe "GB"
      view.getElementById("countryOfDestination").attr("value") mustBe empty
    }

    "display data only for destination country input" in {

      val view =
        createView(DestinationCountries.Supplementary.form.fill(DestinationCountries("", "PL")))

      view.getElementById("countryOfDispatch").attr("value") mustBe empty
      view.getElementById("countryOfDestination").attr("value") mustBe "PL"
    }
  }
}
