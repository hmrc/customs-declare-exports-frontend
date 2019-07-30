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

import java.time.LocalDateTime

import base.CustomExportsBaseSpec
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.common.Address
import forms.declaration.DeclarantDetailsSpec._
import forms.declaration.{DeclarantDetails, EntityDetails}
import models.declaration.Parties
import org.mockito.Mockito.reset
import play.api.test.Helpers._
import services.cache.ExportsCacheModel

class DeclarantDetailsControllerSpec extends CustomExportsBaseSpec {

  private val uri = uriWithContextPath("/declaration/declarant-details")

  override def beforeEach() {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aCacheModel(withChoice(SupplementaryDec)))
    withCaching[DeclarantDetails](None)
  }

  override def afterEach() {
    super.afterEach()
    reset(mockExportsCacheService)
  }

  "Declarant Details Controller on GET" should {

    "return 200 status code" in {
      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
      verifyTheCacheIsUnchanged()
    }

    "read item from cache and display it" in {
      val cachedData = aCacheModel(
        withChoice("SMP"),
        withDeclarantDetails(Some("67890"), Some(Address("WonderWoman", "Test Street", "Leeds", "LS18BN", "Germany")))
      )

      withNewCaching(cachedData)

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      status(result) must be(OK)
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

      status(result) must be(BAD_REQUEST)
      verifyTheCacheIsUnchanged()
    }

    "validate request and redirect - only EORI provided" in {

      val result = route(app, postRequest(uri, correctDeclarantDetailsEORIOnlyJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/representative-details"))
      theCacheModelUpdated.parties.declarantDetails must be(Some(correctDeclarantDetailsEORIOnly))
    }

    "validate request and redirect - only address provided" in {

      val result = route(app, postRequest(uri, correctDeclarantDetailsAddressOnlyJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/representative-details"))
      theCacheModelUpdated.parties.declarantDetails must be(Some(correctDeclarantDetailsAddressOnly))
    }

    "validate request and redirect - all values provided" in {

      val result = route(app, postRequest(uri, correctDeclarantDetailsJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/representative-details"))
      theCacheModelUpdated.parties.declarantDetails must be(Some(correctDeclarantDetails))
    }
  }
}
