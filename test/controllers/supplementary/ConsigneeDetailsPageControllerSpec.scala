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
import base.ExportsTestData._
import forms.supplementary.ConsigneeDetails
import play.api.test.Helpers._

class ConsigneeDetailsPageControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/declaration/supplementary/consignee-details")

  "Consignee address controller" should {
    "display consignee address form" in {
      authorizedUser()
      withCaching[ConsigneeDetails](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.consignee.title"))
      stringResult must include(messages("supplementary.consignee.title.hint"))
      stringResult must include(messages("supplementary.eori"))
      stringResult must include(messages("supplementary.address.fullName"))
      stringResult must include(messages("supplementary.address.addressLine"))
      stringResult must include(messages("supplementary.address.townOrCity"))
      stringResult must include(messages("supplementary.address.postCode"))
      stringResult must include(messages("supplementary.address.country"))
    }

    "validate form - incorrect values" in {
      authorizedUser()
      withCaching[ConsigneeDetails](None)

      val result = route(app, postRequest(uri, incorrectEntityDetails)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.eori.error"))
      stringResult must include(messages("supplementary.address.fullName.error"))
      stringResult must include(messages("supplementary.address.addressLine.error"))
      stringResult must include(messages("supplementary.address.townOrCity.error"))
      stringResult must include(messages("supplementary.address.postCode.error"))
      stringResult must include(messages("supplementary.address.country.error"))
    }

    "validate form - only eori provided" in {
      authorizedUser()
      withCaching[ConsigneeDetails](None)

      val result = route(app, postRequest(uri, entityDetailsEORIOnly)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/additional-actors")
      )
    }

    "validate form - only address provided" in {
      authorizedUser()
      withCaching[ConsigneeDetails](None)

      val result = route(app, postRequest(uri, entityDetailsAddressOnly)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/additional-actors")
      )
    }

    "validate form - all values provided" in {
      authorizedUser()
      withCaching[ConsigneeDetails](None)

      val result = route(app, postRequest(uri, entityDetailsAllValues)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/additional-actors")
      )
    }
  }
}
