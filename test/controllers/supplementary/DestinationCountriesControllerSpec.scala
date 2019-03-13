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
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._

class DestinationCountriesControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  val uri = uriWithContextPath("/declaration/supplementary/destination-countries")

  before {
    authorizedUser()
  }

  "Destination Countries Controller on page" should {

    "return 200 status code" in {
      withCaching[DestinationCountries](None)

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "validate form - incorrect values" in {
      withCaching[DestinationCountries](None)

      val result = route(app, postRequest(uri, incorrectDestinationCountriesJSON)).get
      val stringResult = contentAsString(result)

      status(result) must be(BAD_REQUEST)
      stringResult must include(messages("supplementary.destinationCountries.countryOfDestination.error"))
      stringResult must include(messages("supplementary.destinationCountries.countryOfDispatch.error"))
    }

    "validate form - country of dispatch missing" in {
      withCaching[DestinationCountries](None)

      val result = route(app, postRequest(uri, emptyDestinationCountriesJSON)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages("supplementary.destinationCountries.countryOfDispatch.empty"))
    }

    "validate form and redirect - country of destination missing" in {
      withCaching[DestinationCountries](None)

      val result = route(app, postRequest(uri, emptyDispatchCountriesJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/location-of-goods")
      )
    }

    "validate form and redirect - correct values" in {
      withCaching[DestinationCountries](None)

      val result = route(app, postRequest(uri, correctDestinationCountriesJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/location-of-goods")
      )
    }
  }
}
