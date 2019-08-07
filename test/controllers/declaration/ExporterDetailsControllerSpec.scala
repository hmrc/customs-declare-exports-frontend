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
import forms.Choice.AllowedChoiceValues.SupplementaryDec
import forms.common.Address
import forms.declaration.ExporterDetailsSpec._
import helpers.views.declaration.CommonMessages
import org.mockito.Mockito
import play.api.test.Helpers._

class ExporterDetailsControllerSpec extends CustomExportsBaseSpec with CommonMessages {

  private val uri = uriWithContextPath("/declaration/exporter-details")

  override def beforeEach() {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aCacheModel(withChoice(SupplementaryDec)))
  }

  override def afterEach() = {
    super.afterEach()
    Mockito.reset(mockExportsCacheService)
  }

  "Exporter Details Controller on GET" should {

    "return 200 with a success" in {
      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
      verifyTheCacheIsUnchanged()
    }

    "read item from cache and display it" in {
      val cachedData = aCacheModel(
        withChoice("SMP"),
        withExporterDetails(
          Some("99980"),
          Some(Address("CaptainAmerica", "Test Street", "Leeds", "LS18BN", "Portugal"))
        )
      )
      withNewCaching(cachedData)

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      status(result) must be(OK)
      page must include("99980")
      page must include("CaptainAmerica")
      page must include("Test Street")
      page must include("Leeds")
      page must include("LS18BN")
      page must include("Portugal")
    }
  }

  "Exporter Details Controller on POST" should {

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

    "on the supplementary journey " should {

      "validate request and redirect to consignee-details page with only EORI provided" in {
        val result = route(app, postRequest(uri, correctExporterDetailsEORIOnlyJSON)).get

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be (Some("/customs-declare-exports/declaration/consignee-details"))
        theCacheModelUpdated.parties.exporterDetails must be(Some(correctExporterDetailsEORIOnly))
      }

      "validate request and redirect to consignee-details page with only address provided" in {
        val result = route(app, postRequest(uri, correctExporterDetailsAddressOnlyJSON)).get

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be (Some("/customs-declare-exports/declaration/consignee-details"))
        theCacheModelUpdated.parties.exporterDetails must be(Some(correctExporterDetailsAddressOnly))
      }

      "validate request and redirect to consignee-details page with correct values" in {
        val result = route(app, postRequest(uri, correctExporterDetailsJSON)).get

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be (Some("/customs-declare-exports/declaration/consignee-details"))
        theCacheModelUpdated.parties.exporterDetails must be(Some(correctExporterDetails))

      }
    }

    "on the standard journey " should {

      "validate request and redirect to consignee-details page with only EORI provided" in {
        val result = route(app, postRequest(uri, correctExporterDetailsEORIOnlyJSON)).get

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be (Some("/customs-declare-exports/declaration/consignee-details"))
        theCacheModelUpdated.parties.exporterDetails must be(Some(correctExporterDetailsEORIOnly))
      }

      "validate request and redirect to consignee-details page with only address provided" in {
        val result = route(app, postRequest(uri, correctExporterDetailsAddressOnlyJSON)).get

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be (Some("/customs-declare-exports/declaration/consignee-details"))
        theCacheModelUpdated.parties.exporterDetails must be(Some(correctExporterDetailsAddressOnly))
      }

      "validate request and redirect to consignee-details page with correct values" in {
        val result = route(app, postRequest(uri, correctExporterDetailsJSON)).get

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be (Some("/customs-declare-exports/declaration/consignee-details"))
        theCacheModelUpdated.parties.exporterDetails must be(Some(correctExporterDetails))
      }
    }
  }
}
