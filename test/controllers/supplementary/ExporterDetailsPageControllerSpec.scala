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
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._

class ExporterDetailsPageControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  val uri = uriWithContextPath("/declaration/supplementary/exporter-details")

  before {
    authorizedUser()
  }

  // TODO: seems like mapping is wrong here - consignor or exporter e.g: supplementary.consignor or supplementary.exporter
  "Exporter Details Page Controller on page" should {

    "display exporter address form" in {
      withCaching[ExporterDetails](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.consignor.title"))
      stringResult must include(messages("supplementary.consignor.title.hint"))
      stringResult must include(messages("supplementary.eori"))
      stringResult must include(messages("supplementary.eori.hint"))
      stringResult must include(messages("supplementary.address.fullName"))
      stringResult must include(messages("supplementary.address.addressLine"))
      stringResult must include(messages("supplementary.address.townOrCity"))
      stringResult must include(messages("supplementary.address.postCode"))
      stringResult must include(messages("supplementary.address.country"))
    }

    "display \"Back\" button that links to \"Consignment References\" page" in {
      withCaching[ExporterDetails](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("site.back"))
      stringResult must include(messages("/declaration/supplementary/consignment-references"))
    }

    "display \"Save and continue\" button on page" in {
      withCaching[ExporterDetails](None)

      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      resultAsString must include(messages("site.save_and_continue"))
      resultAsString must include("button id=\"submit\" class=\"button\"")
    }

    "validate form - empty values" in {
      withCaching[ExporterDetails](None)

      val result = route(app, postRequest(uri, emptyExporterDetailsJSON)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.namedEntityDetails.error"))
    }

    "validate form - incorrect values" in {
      withCaching[ExporterDetails](None)

      val result = route(app, postRequest(uri, incorrectExporterDetailsJSON)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.eori.error"))
      stringResult must include(messages("supplementary.address.fullName.error"))
      stringResult must include(messages("supplementary.address.addressLine.error"))
      stringResult must include(messages("supplementary.address.townOrCity.error"))
      stringResult must include(messages("supplementary.address.postCode.error"))
      stringResult must include(messages("supplementary.address.country.error"))
    }

    "validate form and redirect - only eori provided" in {
      withCaching[ExporterDetails](None)

      val result = route(app, postRequest(uri, correctExporterDetailsEORIOnlyJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/declarant-details")
      )
    }

    "validate form and redirect - only address provided" in {
      withCaching[ExporterDetails](None)

      val result = route(app, postRequest(uri, correctExporterDetailsAddressOnlyJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/declarant-details")
      )
    }

    "validate form and redirect - correct values" in {
      withCaching[ExporterDetails](None)

      val result = route(app, postRequest(uri, correctExporterDetailsJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/declarant-details")
      )
    }
  }
}
