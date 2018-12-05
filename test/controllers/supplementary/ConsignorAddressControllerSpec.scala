/*
 * Copyright 2018 HM Revenue & Customs
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
import forms.supplementary.ConsignorAddressForm
import play.api.test.Helpers._

class ConsignorAddressControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/consignor-address")

  "Movement controller" should {
    "display consignor address form" in {
      authorizedUser()
      withCaching[ConsignorAddressForm](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.consignor.add"))
      stringResult must include(messages("supplementary.consignor.add.hint"))
      stringResult must include(messages("supplementary.consignor.eori"))
      stringResult must include(messages("supplementary.consignor.fullName"))
      stringResult must include(messages("supplementary.consignor.address"))
      stringResult must include(messages("supplementary.consignor.townOrCity"))
      stringResult must include(messages("supplementary.consignor.postCode"))
      stringResult must include(messages("supplementary.consignor.country"))
    }

    "validate form - incorrect values" in {
      authorizedUser()
      withCaching[ConsignorAddressForm](None)

      val result = route(app, postRequest(uri, incorrectConsignorAddress)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.consignor.eori.error"))
      stringResult must include(messages("supplementary.consignor.fullName.error"))
      stringResult must include(messages("supplementary.consignor.address.error"))
      stringResult must include(messages("supplementary.consignor.townOrCity.error"))
      stringResult must include(messages("supplementary.consignor.postCode.error"))
      stringResult must include(messages("supplementary.consignor.country.error"))
    }

    "validate form - mandatory fields" in {
      authorizedUser()
      withCaching[ConsignorAddressForm](None)

      val result = route(app, postRequest(uri, emptyConsignorAddress)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.consignor.eori.empty"))
      stringResult must include(messages("supplementary.consignor.fullName.empty"))
      stringResult must include(messages("supplementary.consignor.address.empty"))
      stringResult must include(messages("supplementary.consignor.townOrCity.empty"))
      stringResult must include(messages("supplementary.consignor.postCode.empty"))
      stringResult must include(messages("supplementary.consignor.country.empty"))
    }

    "validate form - correct values" in {
      authorizedUser()
      withCaching[ConsignorAddressForm](None)

      val result = route(app, postRequest(uri, correctConsignorAddress)).get

      status(result) mustBe(OK)
      contentAsString(result) must include("Declarant identification and address")
    }
  }
}
