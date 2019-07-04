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

package controllers.declaration

import base.CustomExportsBaseSpec
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.Choice
import forms.Choice.choiceId
import forms.declaration.DestinationCountriesSpec._
import forms.declaration.destinationCountries.DestinationCountries
import helpers.views.declaration.DestinationCountriesMessages
import org.mockito.Mockito.reset
import play.api.test.Helpers._

class DestinationCountriesControllerSpec extends CustomExportsBaseSpec with DestinationCountriesMessages {

  private val uri = uriWithContextPath("/declaration/destination-countries")

  private val addActionUrlEncoded = (Add.toString, "")
  private val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")

  before {
    reset(mockCustomsCacheService)
  }
  trait SupplementarySetUp {
    authorizedUser()
    withNewCaching(createModel())
    withCaching[DestinationCountries](None)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  trait StandardSetUp {
    authorizedUser()
    withNewCaching(createModel())
    withCaching[DestinationCountries](None)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.StandardDec)), choiceId)
  }

  "Destination Countries Controller on GET" should {

    "return 200 status code" when {

      "user is during supplementary declaration" in new SupplementarySetUp {

        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
      }

      "user is during standard declaration" in new StandardSetUp {

        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
      }
    }

    "read item from cache and display it" when {

      "user is during supplementary declaration" in new SupplementarySetUp {

        val cachedData = DestinationCountries("Netherlands", "Belgium")
        withCaching[DestinationCountries](Some(cachedData), DestinationCountries.formId)

        val result = route(app, getRequest(uri)).get
        val page = contentAsString(result)

        status(result) must be(OK)
        page must include("Netherlands")
        page must include("Belgium")
      }

      "user is during standard declaration" in new StandardSetUp {

        val cachedData = DestinationCountries("Poland", Seq("Slovakia", "Italy"), "United Kingdom")
        withCaching[DestinationCountries](Some(cachedData), DestinationCountries.formId)

        val result = route(app, getRequest(uri)).get
        val page = contentAsString(result)

        status(result) must be(OK)
        page must include("Poland")
        page must include("Slovakia")
        page must include("Italy")
        page must include("United Kingdom")
      }
    }
  }

  "Destination Countries Controller on POST" should {

    "show page with errors for incorrect destination countries for supplementary declaration" in new SupplementarySetUp {

      val result = route(app, postRequest(uri, incorrectDestinationCountriesJSON)).get
      val stringResult = contentAsString(result)

      status(result) must be(BAD_REQUEST)
      stringResult must include(messages(countryOfDestinationError))
      stringResult must include(messages(countryOfDispatchError))
    }

    "show page with errors for incorrect country of routing for standard declaration" in new StandardSetUp {

      val body = Seq(
        ("countryOfDispatch", ""),
        ("countriesOfRouting[]", "Country"),
        ("countryOfDestination", ""),
        addActionUrlEncoded
      )

      val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(countriesOfRoutingError))
    }

    "show page with errors for missing country of dispatch" when {

      "user is during supplementary declaration" in new SupplementarySetUp {

        val result = route(app, postRequest(uri, emptyDestinationCountriesJSON)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(countryOfDispatchEmpty))
      }

      "user is during standard declaration" in new StandardSetUp {

        val body = Seq(
          ("countryOfDispatch", ""),
          ("countriesOfRouting[]", "Poland"),
          ("countryOfDestination", "Poland"),
          saveAndContinueActionUrlEncoded
        )

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(countryOfDispatchEmpty))
      }
    }

    "show page with errors for missing countries of routing" when {

      "user is during standard declaration" in new StandardSetUp {

        val body = Seq(
          ("countryOfDispatch", ""),
          ("countriesOfRouting[]", ""),
          ("countryOfDestination", ""),
          saveAndContinueActionUrlEncoded
        )

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(countriesOfRoutingEmpty))
      }
    }

    "show page with errors for missing country of destination" when {

      "user is during supplementary declaration" in new SupplementarySetUp {

        val result = route(app, postRequest(uri, emptyDestinationCountrySupplementaryJSON)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(countryOfDestinationEmpty))
      }

      "user is during standard declaration" in new StandardSetUp {

        val body = Seq(
          ("countryOfDispatch", "Poland"),
          ("countriesOfRouting[]", "Poland"),
          ("countryOfDestination", ""),
          saveAndContinueActionUrlEncoded
        )

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(countryOfDestinationEmpty))
      }
    }

    "validate input and add country of routing if value is correct" in new StandardSetUp {

      val body = Seq(
        ("countryOfDispatch", ""),
        ("countriesOfRouting[]", "PL"),
        ("countryOfDestination", ""),
        addActionUrlEncoded
      )

      val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

      status(result) must be(OK)
    }

    "show error message for standard declaration" when {
      "user try to add more than 99 countries" in new StandardSetUp {

        val fullCache = Seq.fill(99)("Slovakia")
        val cachedData = DestinationCountries("Poland", fullCache, "England")
        withCaching[DestinationCountries](Some(cachedData), DestinationCountries.formId)

        val body = Seq(
          ("countryOfDispatch", ""),
          ("countriesOfRouting[]", "Poland"),
          ("countryOfDestination", ""),
          addActionUrlEncoded
        )
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.limit"))
      }

      "user try to add duplicated value" in new StandardSetUp {
        val cachedData = DestinationCountries("Poland", Seq("Poland"), "England")
        withCaching[DestinationCountries](Some(cachedData), DestinationCountries.formId)

        val body = Seq(
          ("countryOfDispatch", ""),
          ("countriesOfRouting[]", "Poland"),
          ("countryOfDestination", ""),
          addActionUrlEncoded
        )
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.duplication"))
      }
    }

    "remove country from the cache" when {
      "country exist and user is during standard declaration" in new StandardSetUp {

        val cachedData = DestinationCountries("Poland", Seq("Slovakia", "Italy"), "England")
        withCaching[DestinationCountries](Some(cachedData), DestinationCountries.formId)

        val action = Remove(Seq("0"))
        val body = (action.label, action.keys.head)

        val result = route(app, postRequestFormUrlEncoded(uri, body)).get

        status(result) must be(OK)
      }
    }

    "show global error page when the action is incorrect" in new StandardSetUp {

      val result = route(app, postRequestFormUrlEncoded(uri)).get

      status(result) must be(BAD_REQUEST)
    }

    "validate user input and redirect to location of goods page" when {

      "user is during supplementary declaration and provide correct values" in new SupplementarySetUp {

        val result = route(app, postRequest(uri, correctDestinationCountriesJSON)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/location-of-goods"))
      }

      "user is during standard declaration and provide correct values" in new StandardSetUp {

        val cachedData = DestinationCountries("", Seq("SK", "IT"), "")
        withCaching[DestinationCountries](Some(cachedData), DestinationCountries.formId)

        val body = Seq(
          ("countryOfDispatch", "PL"),
          ("countriesOfRouting[]", ""),
          ("countryOfDestination", "PL"),
          saveAndContinueActionUrlEncoded
        )

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(SEE_OTHER)
      }
    }
  }
}
