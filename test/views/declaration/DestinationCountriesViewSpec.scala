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

import base.TestHelper
import controllers.util.SaveAndReturn
import forms.declaration.destinationCountries.DestinationCountries
import helpers.views.declaration.{CommonMessages, DestinationCountriesMessages}
import models.Mode
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.destination_countries_supplementary
import views.tags.ViewTest

@ViewTest
class DestinationCountriesViewSpec extends ViewSpec with DestinationCountriesMessages with CommonMessages {

  private val form: Form[DestinationCountries] = DestinationCountries.Supplementary.form
  private val destiantionCountriesSupplementaryPage = app.injector.instanceOf[destination_countries_supplementary]
  private def createView(form: Form[DestinationCountries] = form): Html =
    destiantionCountriesSupplementaryPage(Mode.Normal, form)(fakeJourneyRequest("SMP"), messages)

  "Destination Countries View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() must be(messages(title))
    }

    "display empty input with label for Dispatch country" in {

      val view = createView()

      view.getElementById("countryOfDispatch-hint").text() must be(messages(countryOfDispatchHint))
      view.getElementById("countryOfDispatch-label").ownText() must be(messages(countryOfDispatch))
      view.getElementById("countryOfDispatch").attr("value") must be("")
    }

    "display empty input with label for Destination country" in {

      val view = createView()

      view.getElementById("countryOfDestination-hint").text() must be(messages(countryOfDestinationHint))
      view.getElementById("countryOfDestination-label").ownText() must be(messages(countryOfDestination))
      view.getElementById("countryOfDestination").attr("value") must be("")
    }

    "display 'Back' button that links to 'Declaration holder of authorisation' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/holder-of-authorisation")
    }

    "display 'Save and continue' button" in {
      val saveButton = createView().getElementById("submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }

    "display 'Save and return' button" in {
      val saveButton = createView().getElementById("submit_and_return")
      saveButton.text() must be(messages(saveAndReturnCaption))
      saveButton.attr("name") must be(SaveAndReturn.toString)
    }
  }

  "Destination Countries View for invalid input" should {

    "display error when dispatch country is empty" in {

      val view =
        createView(DestinationCountries.Supplementary.form.fillAndValidate(DestinationCountries("", "DE")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(countryOfDispatchEmpty), "#countryOfDispatch")

      view.select("span.error-message").text() must be(messages(countryOfDispatchEmpty))
    }

    "display error when dispatch country is incorrect" in {

      val view = createView(
        DestinationCountries.Supplementary.form
          .fillAndValidate(DestinationCountries(TestHelper.createRandomAlphanumericString(10), "DE"))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(countryOfDispatchError), "#countryOfDispatch")

      view.select("span.error-message").text() must be(messages(countryOfDispatchError))
    }

    "display error when destination country is empty" in {

      val view =
        createView(DestinationCountries.Supplementary.form.fillAndValidate(DestinationCountries("DE", "")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(countryOfDestinationEmpty), "#countryOfDestination")

      view.select("span.error-message").text() must be(messages(countryOfDestinationEmpty))
    }

    "display error when destination country is incorrect" in {

      val view = createView(
        DestinationCountries.Supplementary.form
          .fillAndValidate(DestinationCountries("DE", TestHelper.createRandomAlphanumericString(10)))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(countryOfDestinationError), "#countryOfDestination")

      view.select("span.error-message").text() must be(messages(countryOfDestinationError))
    }

    "display errors when both countries are incorrect" in {

      val view = createView(
        DestinationCountries.Supplementary.form
          .fillAndValidate(
            DestinationCountries(
              TestHelper.createRandomAlphanumericString(10),
              TestHelper.createRandomAlphanumericString(10)
            )
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(countryOfDispatchError), "#countryOfDispatch")
      checkErrorLink(view, 2, messages(countryOfDestinationError), "#countryOfDestination")

      val spanErrors = view.select("span.error-message")
      spanErrors.size() must be(2)

      spanErrors.get(0).text() must be(messages(countryOfDispatchError))
      spanErrors.get(1).text() must be(messages(countryOfDestinationError))
    }

    "display errors when both countries are empty" in {

      val view =
        createView(DestinationCountries.Supplementary.form.fillAndValidate(DestinationCountries("", "")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(countryOfDispatchEmpty), "#countryOfDispatch")
      checkErrorLink(view, 2, messages(countryOfDestinationEmpty), "#countryOfDestination")

      val spanErrors = view.select("span.error-message")
      spanErrors.size() must be(2)

      spanErrors.get(0).text() must be(messages(countryOfDispatchEmpty))
      spanErrors.get(1).text() must be(messages(countryOfDestinationEmpty))
    }

    "display errors when dispatch country is empty and destination is incorrect" in {

      val view = createView(
        DestinationCountries.Supplementary.form
          .fillAndValidate(DestinationCountries("", TestHelper.createRandomAlphanumericString(10)))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(countryOfDispatchEmpty), "#countryOfDispatch")
      checkErrorLink(view, 2, messages(countryOfDestinationError), "#countryOfDestination")

      val spanErrors = view.select("span.error-message")
      spanErrors.size() must be(2)

      spanErrors.get(0).text() must be(messages(countryOfDispatchEmpty))
      spanErrors.get(1).text() must be(messages(countryOfDestinationError))
    }
  }

  "Destination Countries View when filled" should {

    "display data for both countries in inputs" in {

      val view =
        createView(DestinationCountries.Supplementary.form.fill(DestinationCountries("GB", "PL")))

      getSelectedValue(view, "countryOfDispatch") mustBe "GB"
      getSelectedValue(view, "countryOfDestination") mustBe "PL"
    }

    "display data only for dispatch country input" in {

      val view =
        createView(DestinationCountries.Supplementary.form.fill(DestinationCountries("GB", "")))

      getSelectedValue(view, "countryOfDispatch") mustBe "GB"
      getSelectedValue(view, "countryOfDestination") mustBe ""
    }

    "display data only for destination country input" in {

      val view =
        createView(DestinationCountries.Supplementary.form.fill(DestinationCountries("", "PL")))

      getSelectedValue(view, "countryOfDispatch") mustBe ""
      getSelectedValue(view, "countryOfDestination") mustBe "PL"
    }
  }
}
