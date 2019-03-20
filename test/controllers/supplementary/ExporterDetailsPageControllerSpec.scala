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
import play.api.test.Helpers._

class ExporterDetailsPageControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/declaration/supplementary/exporter-details")

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

      stringResult must include(messages("supplementary.namedEntityDetails.error"))
    }

    "validate request - incorrect values" in {

      val result = route(app, postRequest(uri, incorrectExporterDetailsJSON)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.eori.error"))
      stringResult must include(messages("supplementary.address.fullName.error"))
      stringResult must include(messages("supplementary.address.addressLine.error"))
      stringResult must include(messages("supplementary.address.townOrCity.error"))
      stringResult must include(messages("supplementary.address.postCode.error"))
      stringResult must include(messages("supplementary.address.country.error"))
    }

    "validate request and redirect - only EORI provided" in {

      val result = route(app, postRequest(uri, correctExporterDetailsEORIOnlyJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/declarant-details")
      )
    }

    "validate request and redirect - only address provided" in {

      val result = route(app, postRequest(uri, correctExporterDetailsAddressOnlyJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/declarant-details")
      )
    }

    "validate request and redirect - correct values" in {

      val result = route(app, postRequest(uri, correctExporterDetailsJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/declarant-details")
      )
    }
  }
}
