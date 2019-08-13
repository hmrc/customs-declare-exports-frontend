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
import forms.Choice.AllowedChoiceValues.{StandardDec, SupplementaryDec}
import forms.common.Address
import forms.declaration.ExporterDetailsSpec._
import helpers.views.declaration.CommonMessages
import org.mockito.Mockito
import play.api.test.Helpers._

class ExporterDetailsControllerSpec extends CustomExportsBaseSpec with CommonMessages {

  private val uri = uriWithContextPath("/declaration/exporter-details")

  val supplementaryModel = aDeclaration(withChoice(SupplementaryDec))

  override def beforeEach() {
    super.beforeEach()
    authorizedUser()
  }

  override def afterEach() = {
    super.afterEach()
    Mockito.reset(mockExportsCacheService)
  }

  "Exporter Details Controller on POST" should {

    "validate request - incorrect values" in {
      withNewCaching(supplementaryModel)
      val result =
        route(app, postRequest(uri, incorrectExporterDetailsJSON, sessionId = supplementaryModel.sessionId)).get
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
        withNewCaching(supplementaryModel)

        val result =
          route(app, postRequest(uri, correctExporterDetailsEORIOnlyJSON, sessionId = supplementaryModel.sessionId)).get

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/consignee-details"))
        theCacheModelUpdated.parties.exporterDetails must be(Some(correctExporterDetailsEORIOnly))
      }

      "validate request and redirect to consignee-details page with only address provided" in {
        withNewCaching(supplementaryModel)

        val result = route(
          app,
          postRequest(uri, correctExporterDetailsAddressOnlyJSON, sessionId = supplementaryModel.sessionId)
        ).get

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/consignee-details"))
        theCacheModelUpdated.parties.exporterDetails must be(Some(correctExporterDetailsAddressOnly))
      }

      "validate request and redirect to consignee-details page with correct values" in {
        withNewCaching(supplementaryModel)

        val result =
          route(app, postRequest(uri, correctExporterDetailsJSON, sessionId = supplementaryModel.sessionId)).get

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/consignee-details"))
        theCacheModelUpdated.parties.exporterDetails must be(Some(correctExporterDetails))

      }
    }

    "on the standard journey " should {

      val standardModel = aDeclaration(withChoice(StandardDec))

      "validate request and redirect to consignee-details page with only EORI provided" in {
        withNewCaching(standardModel)

        val result =
          route(app, postRequest(uri, correctExporterDetailsEORIOnlyJSON, sessionId = standardModel.sessionId)).get

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/consignee-details"))
        theCacheModelUpdated.parties.exporterDetails must be(Some(correctExporterDetailsEORIOnly))
      }

      "validate request and redirect to consignee-details page with only address provided" in {
        withNewCaching(standardModel)

        val result =
          route(app, postRequest(uri, correctExporterDetailsAddressOnlyJSON, sessionId = standardModel.sessionId)).get

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/consignee-details"))
        theCacheModelUpdated.parties.exporterDetails must be(Some(correctExporterDetailsAddressOnly))
      }

      "validate request and redirect to consignee-details page with correct values" in {
        withNewCaching(standardModel)

        val result = route(app, postRequest(uri, correctExporterDetailsJSON, sessionId = standardModel.sessionId)).get

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some("/customs-declare-exports/declaration/consignee-details"))
        theCacheModelUpdated.parties.exporterDetails must be(Some(correctExporterDetails))
      }
    }
  }
}
