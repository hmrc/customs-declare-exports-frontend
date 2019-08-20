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
import base.TestHelper._
import forms.Choice
import forms.Choice.AllowedChoiceValues.{StandardDec, SupplementaryDec}
import forms.declaration.RepresentativeDetails
import forms.declaration.RepresentativeDetailsSpec._
import helpers.views.declaration.{CommonMessages, RepresentativeDetailsMessages}
import models.ExportsDeclaration
import models.declaration.Parties
import org.mockito.Mockito.reset
import org.scalatest.OptionValues
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.test.Helpers._

import scala.concurrent.Future

class RepresentativeDetailsControllerSpec
    extends CustomExportsBaseSpec with RepresentativeDetailsMessages with CommonMessages with OptionValues {

  import RepresentativeDetailsControllerSpec._
  private val uri = uriWithContextPath("/declaration/representative-details")

  val supplementaryModel = aDeclaration(withChoice(SupplementaryDec))

  val standardModel = aDeclaration(withChoice(StandardDec))

  override def beforeEach() {
    super.beforeEach()
    authorizedUser()
  }

  override def afterEach() {
    reset(mockExportsCacheService)
    super.afterEach()
  }

  "Representative Address Controller on GET" should {

    "return 200 code" in {
      withNewCaching(supplementaryModel)

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
      verifyTheCacheIsUnchanged()
    }

    "not populate the form fields if cache is empty" in {
      withNewCaching(supplementaryModel)

      val result = route(app, getRequest(uri)).get

      contentAsString(result) mustNot include("checked=\"checked\"")
      verifyTheCacheIsUnchanged()
    }

    "populate the form fields with data from cache" in {
      val model = aDeclaration(
        withChoice(Choice.AllowedChoiceValues.SupplementaryDec),
        withRepresentativeDetails(correctRepresentativeDetails)
      )
      withNewCaching(model)

      val result = route(app, getRequest(uri)).get

      contentAsString(result) must include("checked=\"checked\"")
    }
  }

  "Representative Address Controller on POST" should {

    "display the form page with error for empty field" when {

      "status is empty but eori provided" in {
        withNewCaching(supplementaryModel)

        val emptyForm = buildRepresentativeDetailsJsonInput(eori = "12345678")
        val result = route(app, postRequest(uri, emptyForm)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(repTypeErrorEmpty))
        verifyTheCacheIsUnchanged()
      }

      "status provided but both EORI and address are empty" in {
        withNewCaching(supplementaryModel)

        val emptyForm = buildRepresentativeDetailsJsonInput(status = "2")
        val result = route(app, postRequest(uri, emptyForm)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(eoriOrAddressEmpty))
        verifyTheCacheIsUnchanged()
      }
    }

    "display the form page with error for wrong value" when {

      "wrong value provided for EORI" in {
        withNewCaching(supplementaryModel)

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(eoriError))
        verifyTheCacheIsUnchanged()
      }

      "wrong value provided for full name" in {
        withNewCaching(supplementaryModel)

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(fullNameError))
        verifyTheCacheIsUnchanged()
      }

      "wrong value provided for first address line" in {
        withNewCaching(supplementaryModel)

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(addressLineError))
        verifyTheCacheIsUnchanged()
      }

      "wrong value provided for city" in {
        withNewCaching(supplementaryModel)

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(townOrCityError))
        verifyTheCacheIsUnchanged()
      }

      "wrong value provided for postcode" in {
        withNewCaching(supplementaryModel)

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(postCodeError))
        verifyTheCacheIsUnchanged()
      }

      "wrong value provided for country" in {
        withNewCaching(supplementaryModel)

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(countryError))
        verifyTheCacheIsUnchanged()
      }
    }

    "accept form with status and EORI if on supplementary journey" in {
      withNewCaching(supplementaryModel)
      val result = route(app, postRequest(uri, correctRepresentativeDetailsEORIOnlyJSON)).get

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some("/customs-declare-exports/declaration/additional-actors"))
      theCacheModelUpdated.parties.representativeDetails must be(Some(correctRepresentativeDetailsEORIOnly))
    }

    "accept form with status and EORI if on standard journey" in {
      withNewCaching(standardModel)
      val result =
        route(app, postRequest(uri, correctRepresentativeDetailsEORIOnlyJSON)).get

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some("/customs-declare-exports/declaration/carrier-details"))
      theCacheModelUpdated.parties.representativeDetails must be(Some(correctRepresentativeDetailsEORIOnly))
    }

    "accept form with status and address if on supplementary journey" in {
      withNewCaching(supplementaryModel)
      val result = route(app, postRequest(uri, correctRepresentativeDetailsAddressOnlyJSON)).get

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some("/customs-declare-exports/declaration/additional-actors"))
      theCacheModelUpdated.parties.representativeDetails must be(Some(correctRepresentativeDetailsAddressOnly))
    }

    "accept form with status and address only if on standard journey" in {
      withNewCaching(standardModel)
      val result = route(app, postRequest(uri, correctRepresentativeDetailsAddressOnlyJSON)).get

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some("/customs-declare-exports/declaration/carrier-details"))
      theCacheModelUpdated.parties.representativeDetails must be(Some(correctRepresentativeDetailsAddressOnly))
    }

    "save data to the cache" in {

      val representativeDetails = Parties(representativeDetails = Some(correctRepresentativeDetails))

      val model = aDeclaration(withChoice(Choice.AllowedChoiceValues.SupplementaryDec))
      withNewCaching(model)

      route(app, postRequest(uri, correctRepresentativeDetailsJSON)).get.futureValue

      theCacheModelUpdated.parties.representativeDetails.value must be(correctRepresentativeDetails)
    }

    "return 303 code" when {
      "data is correct" in {
        withNewCaching(supplementaryModel)
        val result =
          route(app, postRequest(uri, correctRepresentativeDetailsJSON)).get

        status(result) must be(SEE_OTHER)
        theCacheModelUpdated.parties.representativeDetails must be(Some(correctRepresentativeDetails))
      }

      "data is empty" in {
        withNewCaching(supplementaryModel)
        val result = route(app, postRequest(uri, Json.obj())).get

        status(result) must be(SEE_OTHER)
        theCacheModelUpdated.parties.representativeDetails must be(Some(RepresentativeDetails(None, None)))
      }
    }

    "redirect to Additional Actors page if on supplementary journey" in {
      withNewCaching(supplementaryModel)

      val result =
        route(app, postRequest(uri, correctRepresentativeDetailsJSON)).get

      redirectLocation(result) must be(Some("/customs-declare-exports/declaration/additional-actors"))
      theCacheModelUpdated.parties.representativeDetails must be(Some(correctRepresentativeDetails))
    }

    "redirect to Consignee Details page if on standard journey" in {
      withNewCaching(standardModel)

      val result =
        route(app, postRequest(uri, correctRepresentativeDetailsJSON)).get

      redirectLocation(result) must be(Some("/customs-declare-exports/declaration/carrier-details"))
      theCacheModelUpdated.parties.representativeDetails must be(Some(correctRepresentativeDetails))
    }
  }
}

object RepresentativeDetailsControllerSpec {

  val incorrectRepresentativeDetails: JsValue = buildRepresentativeDetailsJsonInput(
    eori = createRandomAlphanumericString(18),
    fullName = createRandomAlphanumericString(71),
    addressLine = createRandomAlphanumericString(71),
    townOrCity = createRandomAlphanumericString(36),
    postCode = createRandomAlphanumericString(10),
    country = createRandomAlphanumericString(3)
  )

  def buildRepresentativeDetailsJsonInput(
    eori: String = "",
    fullName: String = "",
    addressLine: String = "",
    townOrCity: String = "",
    postCode: String = "",
    country: String = "",
    status: String = ""
  ): JsValue = JsObject(
    Map(
      "details.eori" -> JsString(eori),
      "details.address.fullName" -> JsString(fullName),
      "details.address.addressLine" -> JsString(addressLine),
      "details.address.townOrCity" -> JsString(townOrCity),
      "details.address.postCode" -> JsString(postCode),
      "details.address.country" -> JsString(country),
      "statusCode" -> JsString(status)
    )
  )
}
