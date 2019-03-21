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

package controllers.supplementary

import base.CustomExportsBaseSpec
import forms.supplementary.DestinationCountries
import forms.supplementary.DestinationCountriesSpec._
import helpers.views.supplementary.DestinationCountriesMessages
import play.api.test.Helpers._

class DestinationCountriesControllerSpec extends CustomExportsBaseSpec with DestinationCountriesMessages {

  val uri = uriWithContextPath("/declaration/supplementary/destination-countries")

  before {
    authorizedUser()
    withCaching[DestinationCountries](None)
  }

  "Destination Countries Controller on GET" should {

    "return 200 status code" in {

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }
  }

  "Destination Countries Controller on POST" should {

    "validate request - incorrect values" in {

      val result = route(app, postRequest(uri, incorrectDestinationCountriesJSON)).get
      val stringResult = contentAsString(result)

      status(result) must be(BAD_REQUEST)
      stringResult must include(messages(countryOfDestinationError))
      stringResult must include(messages(countryOfDispatchError))
    }

    "validate request - country of dispatch missing" in {

      val result = route(app, postRequest(uri, emptyDestinationCountriesJSON)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(countryOfDispatchEmpty))
    }

    "validate request - country of destination missing" in {
      withCaching[DestinationCountries](None)

      val result = route(app, postRequest(uri, emptyDispatchCountriesJSON)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages("supplementary.destinationCountries.countryOfDestination.empty"))
    }

    "validate request and redirect - correct values" in {

      val result = route(app, postRequest(uri, correctDestinationCountriesJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/location-of-goods")
      )
    }
  }
}
