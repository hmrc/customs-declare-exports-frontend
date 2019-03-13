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

package views.supplementary

import forms.supplementary.DestinationCountries
import play.api.data.Form
import play.twirl.api.Html
import views.helpers.{Item, ViewSpec}
import views.html.supplementary.destination_countries
import views.tags.ViewTest

@ViewTest
class DestinationCountriesViewSpec extends ViewSpec {

  private val form: Form[DestinationCountries] = DestinationCountries.form()
  private def createView(form: Form[DestinationCountries] = form): Html = destination_countries(appConfig, form)(fakeRequest, messages, countries)

  private val prefix = s"${basePrefix}destinationCountries."

  private val title = Item(prefix, "title")
  private val countryOfDestination = Item(prefix, "countryOfDestination")
  private val countryOfDispatch = Item(prefix, "countryOfDispatch")

  "Destination Countries View" should {

    "have proper messages for labels" in {

      assertMessage(title.withPrefix, "Declaration countries of dispatch/destination")
      assertMessage(countryOfDestination.withPrefix, "5/8 Enter the country of destination")
      assertMessage(countryOfDispatch.withPrefix, "5/14 Enter the country of dispatch")
    }

    "have proper messages for error labels" in {

      assertMessage(countryOfDispatch.withError, "Country of dispatch is incorrect")
      assertMessage(countryOfDispatch.withEmpty, "Country of dispatch cannot be empty")
      assertMessage(countryOfDestination.withError, "Country of destination is incorrect")
    }
  }

  "Destination Countries View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be("Declaration countries of dispatch/destination")
    }

    "display header" in {

      getElementByCss(createView(), "h1").text() must be(messages(title.withPrefix))
    }

    "display empty input with label for Dispatch country" in {

      val view = createView()

      getElementByCss(view, "#countryOfDispatch-outer>label").text() must be(messages(countryOfDispatch.withPrefix))
      getElementById(view, countryOfDispatch.key).attr("value") must be("")
    }

    "display empty input with label for Destination country" in {

      val view = createView()

      getElementByCss(view, "#countryOfDestination-outer>label").text() must be(messages(countryOfDestination.withPrefix))
      getElementById(view, countryOfDestination.key).attr("value") must be("")
    }

    "display \"Back\" button that links to \"Declaration holder of authorisation\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be("Back")
      backButton.attr("href") must be("/customs-declare-exports/declaration/supplementary/holder-of-authorisation")
    }

    "display \"Save and continue\" button" in {

      val saveButton = getElementByCss(createView(), "#submit")
      saveButton.text() must be("Save and continue")
    }
  }

  "Destination Countries View for invalid input" should {

    "display error when dispatch country is empty" in {

      val view = createView(DestinationCountries.form().withError(countryOfDispatch.key, messages(countryOfDispatch.withEmpty)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(countryOfDispatch.withEmpty), countryOfDispatch.asLink)

      getElementByCss(view, "span.error-message").text() must be(messages(countryOfDispatch.withEmpty))
    }

    "display error when dispatch country is incorrect" in {

      val view = createView(DestinationCountries.form().withError(countryOfDispatch.key, messages(countryOfDispatch.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(countryOfDispatch.withError), countryOfDispatch.asLink)

      getElementByCss(view, "span.error-message").text() must be(messages(countryOfDispatch.withError))
    }

    "display error when destination country is incorrect" in {

      val view = createView(DestinationCountries.form().withError(countryOfDestination.key, messages(countryOfDestination.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(countryOfDestination.withError), countryOfDestination.asLink)

      getElementByCss(view, "span.error-message").text() must be(messages(countryOfDestination.withError))
    }

    "display errors when both countries are incorrect" in {

      val form = DestinationCountries.form()
        .withError(countryOfDispatch.key, messages(countryOfDispatch.withError))
        .withError(countryOfDestination.key, messages(countryOfDestination.withError))
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(countryOfDispatch.withError), countryOfDispatch.asLink)
      checkErrorLink(view, 2, messages(countryOfDestination.withError), countryOfDestination.asLink)

      val spanErrors = getElementsByCss(view, "span.error-message")
      spanErrors.size() must be(2)

      spanErrors.get(0).text() must be(messages(countryOfDispatch.withError))
      spanErrors.get(1).text() must be(messages(countryOfDestination.withError))
    }

    "display errors when dispatch country is empty and destination is incorrect" in {

      val form = DestinationCountries.form()
        .withError(countryOfDispatch.key, messages(countryOfDispatch.withEmpty))
        .withError(countryOfDestination.key, messages(countryOfDestination.withError))
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, messages(countryOfDispatch.withEmpty), countryOfDispatch.asLink)
      checkErrorLink(view, 2, messages(countryOfDestination.withError), countryOfDestination.asLink)

      val spanErrors = getElementsByCss(view, "span.error-message")
      spanErrors.size() must be(2)

      spanErrors.get(0).text() must be(messages(countryOfDispatch.withEmpty))
      spanErrors.get(1).text() must be(messages(countryOfDestination.withError))
    }
  }

  "Destination Countries View when filled" should {

    "display both countries in inputs" in {

      val view = createView(DestinationCountries.form().fill(DestinationCountries( "Ukraine", Some("Poland"))))

      getElementById(view, countryOfDispatch.key).attr("value") must be("Ukraine")
      getElementById(view, countryOfDestination.key).attr("value") must be("Poland")
    }

    "display only dispatch country in inputs" in {

      val view = createView(DestinationCountries.form().fill(DestinationCountries("Ukraine", Some(""))))

      getElementById(view, countryOfDispatch.key).attr("value") must be("Ukraine")
      getElementById(view, countryOfDestination.key).attr("value") must be("")
    }

    "display only destination country in inputs" in {

      val view = createView(DestinationCountries.form().fill(DestinationCountries("", Some("Poland"))))

      getElementById(view, countryOfDispatch.key).attr("value") must be("")
      getElementById(view, countryOfDestination.key).attr("value") must be("Poland")
    }
  }
}
