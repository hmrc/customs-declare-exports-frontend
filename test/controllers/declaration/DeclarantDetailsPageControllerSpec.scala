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

package controllers.declaration

import base.CustomExportsBaseSpec
import forms.Choice
import forms.Choice.choiceId
import forms.common.Address
import forms.declaration.DeclarantDetailsSpec._
import forms.declaration.{DeclarantDetails, EntityDetails}
import play.api.test.Helpers._

class DeclarantDetailsPageControllerSpec extends CustomExportsBaseSpec {

  private val uri = uriWithContextPath("/declaration/declarant-details")

  before {
    authorizedUser()
    withCaching[DeclarantDetails](None)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Declarant Details Controller on GET" should {

    "return 200 status code" in {
      val result = route(app, getRequest(uri)).get

      status(result) mustBe OK
    }

    "read item from cache and display it" in {

      val cachedData = DeclarantDetails(
        EntityDetails(Some("67890"), Some(Address("WonderWoman", "Test Street", "Leeds", "LS18BN", "Germany")))
      )
      withCaching[DeclarantDetails](Some(cachedData), "DeclarantDetails")

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      status(result) mustBe OK
      page must include("67890")
      page must include("WonderWoman")
      page must include("Test Street")
      page must include("Leeds")
      page must include("LS18BN")
      page must include("Germany")
    }
  }

  "Declarant Details Controller on POST" should {

    "validate request and redirect - both EORI and business details are empty" in {

      val result = route(app, postRequest(uri, emptyDeclarantDetailsJSON)).get

      status(result) mustBe BAD_REQUEST
    }

    "validate request and redirect - only EORI provided" in {

      val result = route(app, postRequest(uri, correctDeclarantDetailsEORIOnlyJSON)).get
      val header = result.futureValue.header

      status(result) mustBe SEE_OTHER
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/representative-details"))
    }

    "validate request and redirect - only address provided" in {

      val result = route(app, postRequest(uri, correctDeclarantDetailsAddressOnlyJSON)).get
      val header = result.futureValue.header

      status(result) mustBe SEE_OTHER
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/representative-details"))
    }

    "validate request and redirect - all values provided" in {

      val result = route(app, postRequest(uri, correctDeclarantDetailsJSON)).get
      val header = result.futureValue.header

      status(result) mustBe SEE_OTHER
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/representative-details"))
    }
  }
}
