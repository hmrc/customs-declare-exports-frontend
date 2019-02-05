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
import play.api.test.Helpers._

class DestinationCountriesControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/declaration/supplementary/destination-countries")

  "Destination Countries contoller" should {
    "display declaration countries form" in {
      authorizedUser()
      withCaching[DestinationCountries](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.destinationCountries.title"))
      stringResult must include(messages("supplementary.destinationCountries.countryOfDestination"))
      stringResult must include(messages("supplementary.destinationCountries.countryOfDispatch"))
    }

    "validate form - incorrect values" in {
      authorizedUser()
      withCaching[DestinationCountries](None)

      val result = route(app, postRequest(uri, incorrectDestinationCountriesJSON)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.destinationCountries.countryOfDestination.error"))
      stringResult must include(messages("supplementary.destinationCountries.countryOfDispatch.error"))
    }

    "validate form - country of dispatch missing" in {
      authorizedUser()
      withCaching[DestinationCountries](None)

      val result = route(app, postRequest(uri, emptyDestinationCountriesJSON)).get

      contentAsString(result) must include(messages("supplementary.destinationCountries.countryOfDispatch.empty"))
    }

    "validate form - correct values" in {
      authorizedUser()
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
