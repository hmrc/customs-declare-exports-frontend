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
import forms.supplementary.ExporterDetails
import forms.supplementary.ExporterDetailsSpec._
import helpers.views.supplementary.CommonMessages
import play.api.test.Helpers._

class ExporterDetailsPageControllerSpec extends CustomExportsBaseSpec with CommonMessages {

  val uri = uriWithContextPath("/declaration/exporter-details")

  before {
    authorizedUser()
    withCaching[ExporterDetails](None)
  }

  "Exporter Details Page Controller on GET" should {

    "return 200 with a success" in {
      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }
  }

  "Exporter Details Page Controller on POST" should {

    "validate request - empty values" in {

      val result = route(app, postRequest(uri, emptyExporterDetailsJSON)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages(eoriOrAddressEmpty))
    }

    "validate request - incorrect values" in {

      val result = route(app, postRequest(uri, incorrectExporterDetailsJSON)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages(eoriError))
      stringResult must include(messages(fullNameError))
      stringResult must include(messages(addressLineError))
      stringResult must include(messages(townOrCityError))
      stringResult must include(messages(postCodeError))
      stringResult must include(messages(countryError))
    }

    "validate request and redirect - only EORI provided" in {

      val result = route(app, postRequest(uri, correctExporterDetailsEORIOnlyJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/declarant-details")
      )
    }

    "validate request and redirect - only address provided" in {

      val result = route(app, postRequest(uri, correctExporterDetailsAddressOnlyJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/declarant-details")
      )
    }

    "validate request and redirect - correct values" in {

      val result = route(app, postRequest(uri, correctExporterDetailsJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/declarant-details")
      )
    }
  }
}
