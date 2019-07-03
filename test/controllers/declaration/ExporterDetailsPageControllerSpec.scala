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
import forms.declaration.ExporterDetailsSpec._
import forms.declaration.{EntityDetails, ExporterDetails}
import helpers.views.declaration.CommonMessages
import play.api.test.Helpers._

class ExporterDetailsPageControllerSpec extends CustomExportsBaseSpec with CommonMessages {

  private val uri = uriWithContextPath("/declaration/exporter-details")

  before {
    authorizedUser()
    withNewCaching(createModel())
    withCaching[ExporterDetails](None)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Exporter Details Controller on GET" should {

    "return 200 with a success" in {
      withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "read item from cache and display it" in {

      val cachedData = ExporterDetails(
        EntityDetails(Some("99980"), Some(Address("CaptainAmerica", "Test Street", "Leeds", "LS18BN", "Portugal")))
      )
      withCaching[ExporterDetails](Some(cachedData), "ExporterDetails")

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
      withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

      val result = route(app, postRequest(uri, emptyExporterDetailsJSON)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages(eoriOrAddressEmpty))
    }

    "validate request - incorrect values" in {
      withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

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
        withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

        val result = route(app, postRequest(uri, correctExporterDetailsEORIOnlyJSON)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/consignee-details"))
      }

      "validate request and redirect to consignee-details page with only address provided" in {
        withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

        val result = route(app, postRequest(uri, correctExporterDetailsAddressOnlyJSON)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/consignee-details"))
      }

      "validate request and redirect to consignee-details page with correct values" in {
        withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

        val result = route(app, postRequest(uri, correctExporterDetailsJSON)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/consignee-details"))
      }
    }

    "on the standard journey " should {

      "validate request and redirect to consignee-details page with only EORI provided" in {
        withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.StandardDec)), choiceId)

        val result = route(app, postRequest(uri, correctExporterDetailsEORIOnlyJSON)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/consignee-details"))
      }

      "validate request and redirect to consignee-details page with only address provided" in {
        withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.StandardDec)), choiceId)

        val result = route(app, postRequest(uri, correctExporterDetailsAddressOnlyJSON)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/consignee-details"))
      }

      "validate request and redirect to consignee-details page with correct values" in {
        withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.StandardDec)), choiceId)

        val result = route(app, postRequest(uri, correctExporterDetailsJSON)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/consignee-details"))
      }
    }
  }
}
