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
import forms.declaration.destinationCountries.{DestinationCountries, DestinationCountriesSupplementary}
import helpers.views.declaration.{CommonMessages, DestinationCountriesMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.destination_countries_supplementary
import views.tags.ViewTest

@ViewTest
class DestinationCountriesViewSpec extends ViewSpec with DestinationCountriesMessages with CommonMessages {

  private val form: Form[DestinationCountriesSupplementary] = DestinationCountries.supplementaryForm
  private def createView(form: Form[DestinationCountriesSupplementary] = form): Html =
    destination_countries_supplementary(form)(fakeJourneyRequest("SMP"), messages, countries, appConfig)

  "Destination Countries View" should {

    "have proper messages for labels" in {

      assertMessage(title, "Countries of dispatch and destination")
      assertMessage(countryOfDestination, "5/8 What is the destination country?")
      assertMessage(countryOfDispatch, "5/14 Enter the country of dispatch code")
    }

    "have proper messages for error labels" in {

      assertMessage(countryOfDispatchError, "Country of dispatch is incorrect")
      assertMessage(countryOfDispatchEmpty, "Country of dispatch cannot be empty")
      assertMessage(countryOfDestinationError, "Country of destination is incorrect")
    }
  }

  "Destination Countries View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title))
    }

    "display header" in {

      getElementByCss(createView(), "h1").text() must be(messages(title))
    }

    "display empty input with label for Dispatch country" in {

      val view = createView()

      getElementById(view, "countryOfDispatch-hint").text() must be(messages(countryOfDispatchHint))
      getElementById(view, "countryOfDispatch-label").ownText() must be(messages(countryOfDispatch))
      getElementById(view, "countryOfDispatch").attr("value") must be("")
    }

    "display empty input with label for Destination country" in {

      val view = createView()

      getElementById(view, "countryOfDestination-hint").text() must be(messages(countryOfDestinationHint))
      getElementById(view, "countryOfDestination-label").ownText() must be(messages(countryOfDestination))
      getElementById(view, "countryOfDestination").attr("value") must be("")
    }

    "display \"Back\" button that links to \"Declaration holder of authorisation\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/holder-of-authorisation")
    }

    "display \"Save and continue\" button" in {

      val saveButton = getElementByCss(createView(), "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Destination Countries View for invalid input" should {

    "display error when dispatch country is empty" in {

      val view = createView(
        DestinationCountries.supplementaryForm.fillAndValidate(DestinationCountriesSupplementary("", "Germany"))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(countryOfDispatchEmpty), "#countryOfDispatch")

      getElementByCss(view, "span.error-message").text() must be(messages(countryOfDispatchEmpty))
    }

    "display error when dispatch country is incorrect" in {

      val view = createView(
        DestinationCountries.supplementaryForm
          .fillAndValidate(DestinationCountriesSupplementary(TestHelper.createRandomAlphanumericString(10), "Germany"))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(countryOfDispatchError), "#countryOfDispatch")

      getElementByCss(view, "span.error-message").text() must be(messages(countryOfDispatchError))
    }

    "display error when destination country is empty" in {

      val view = createView(
        DestinationCountries.supplementaryForm.fillAndValidate(DestinationCountriesSupplementary("Germany", ""))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(countryOfDestinationEmpty), "#countryOfDestination")

      getElementByCss(view, "span.error-message").text() must be(messages(countryOfDestinationEmpty))
    }

    "display error when destination country is incorrect" in {

      val view = createView(
        DestinationCountries.supplementaryForm
          .fillAndValidate(DestinationCountriesSupplementary("Germany", TestHelper.createRandomAlphanumericString(10)))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(countryOfDestinationError), "#countryOfDestination")

      getElementByCss(view, "span.error-message").text() must be(messages(countryOfDestinationError))
    }

    "display errors when both countries are incorrect" in {

      val view = createView(
        DestinationCountries.supplementaryForm
          .fillAndValidate(
            DestinationCountriesSupplementary(
              TestHelper.createRandomAlphanumericString(10),
              TestHelper.createRandomAlphanumericString(10)
            )
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(countryOfDispatchError), "#countryOfDispatch")
      checkErrorLink(view, 2, messages(countryOfDestinationError), "#countryOfDestination")

      val spanErrors = getElementsByCss(view, "span.error-message")
      spanErrors.size() must be(2)

      spanErrors.get(0).text() must be(messages(countryOfDispatchError))
      spanErrors.get(1).text() must be(messages(countryOfDestinationError))
    }

    "display errors when both countries are empty" in {

      val view =
        createView(DestinationCountries.supplementaryForm.fillAndValidate(DestinationCountriesSupplementary("", "")))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(countryOfDispatchEmpty), "#countryOfDispatch")
      checkErrorLink(view, 2, messages(countryOfDestinationEmpty), "#countryOfDestination")

      val spanErrors = getElementsByCss(view, "span.error-message")
      spanErrors.size() must be(2)

      spanErrors.get(0).text() must be(messages(countryOfDispatchEmpty))
      spanErrors.get(1).text() must be(messages(countryOfDestinationEmpty))
    }

    "display errors when dispatch country is empty and destination is incorrect" in {

      val view = createView(
        DestinationCountries.supplementaryForm
          .fillAndValidate(DestinationCountriesSupplementary("", TestHelper.createRandomAlphanumericString(10)))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(countryOfDispatchEmpty), "#countryOfDispatch")
      checkErrorLink(view, 2, messages(countryOfDestinationError), "#countryOfDestination")

      val spanErrors = getElementsByCss(view, "span.error-message")
      spanErrors.size() must be(2)

      spanErrors.get(0).text() must be(messages(countryOfDispatchEmpty))
      spanErrors.get(1).text() must be(messages(countryOfDestinationError))
    }
  }

  "Destination Countries View when filled" should {

    "display data for both countries in inputs" in {

      val view =
        createView(DestinationCountries.supplementaryForm.fill(DestinationCountriesSupplementary("Ukraine", "Poland")))

      getElementById(view, "countryOfDispatch").attr("value") must be("Ukraine")
      getElementById(view, "countryOfDestination").attr("value") must be("Poland")
    }

    "display data only for dispatch country input" in {

      val view =
        createView(DestinationCountries.supplementaryForm.fill(DestinationCountriesSupplementary("Ukraine", "")))

      getElementById(view, "countryOfDispatch").attr("value") must be("Ukraine")
      getElementById(view, "countryOfDestination").attr("value") must be("")
    }

    "display data only for destination country input" in {

      val view =
        createView(DestinationCountries.supplementaryForm.fill(DestinationCountriesSupplementary("", "Poland")))

      getElementById(view, "countryOfDispatch").attr("value") must be("")
      getElementById(view, "countryOfDestination").attr("value") must be("Poland")
    }
  }
}
