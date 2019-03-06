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
import forms.supplementary.ConsigneeDetails
import forms.supplementary.ConsigneeDetailsSpec._
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._

class ConsigneeDetailsPageControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  private val uri = uriWithContextPath("/declaration/supplementary/consignee-details")

  before {
    authorizedUser()
  }

  "Consignee Details Page controller" should {

    "validate form and redirect - only eori provided" in {
      withCaching[ConsigneeDetails](None)

      val result = route(app, postRequest(uri, correctConsigneeDetailsEORIOnlyJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/additional-actors")
      )
    }

    "validate form and redirect - only address provided" in {
      withCaching[ConsigneeDetails](None)

      val result = route(app, postRequest(uri, correctConsigneeDetailsAddressOnlyJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/additional-actors")
      )
    }

    "validate form and redirect - all values provided" in {
      withCaching[ConsigneeDetails](None)

      val result = route(app, postRequest(uri, correctConsigneeDetailsJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/additional-actors")
      )
    }
  }
}
