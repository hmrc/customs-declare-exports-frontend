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
import forms.supplementary.DeclarantDetails
import forms.supplementary.DeclarantDetailsSpec._
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._

class DeclarantDetailsPageControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  private val uri = uriWithContextPath("/declaration/supplementary/declarant-details")

  before {
    authorizedUser()
  }

  "Declarant Details Page Controller on GET" should {

    "return 200 status code" in {
      withCaching[DeclarantDetails](None)
      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }
  }

  "Declarant Details Page Controller on page" should {

    "validate form and redirect - only eori provided" in {
      withCaching[DeclarantDetails](None)

      val result = route(app, postRequest(uri, correctDeclarantDetailsEORIOnlyJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/representative-details")
      )
    }

    "validate form and redirect - only address provided" in {
      withCaching[DeclarantDetails](None)

      val result = route(app, postRequest(uri, correctDeclarantDetailsAddressOnlyJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/representative-details")
      )
    }

    "validate form and redirect - all values provided" in {
      withCaching[DeclarantDetails](None)

      val result = route(app, postRequest(uri, correctDeclarantDetailsJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/representative-details")
      )
    }
  }
}
