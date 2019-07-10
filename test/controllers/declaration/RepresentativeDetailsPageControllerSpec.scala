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
import forms.Choice.choiceId
import forms.declaration.RepresentativeDetails
import forms.declaration.RepresentativeDetailsSpec._
import helpers.views.declaration.{CommonMessages, RepresentativeDetailsMessages}
import models.declaration.Parties
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import services.cache.ExportsCacheModel

import scala.concurrent.Future

class RepresentativeDetailsPageControllerSpec
    extends CustomExportsBaseSpec with RepresentativeDetailsMessages with CommonMessages {

  import RepresentativeDetailsPageControllerSpec._
  private val uri = uriWithContextPath("/declaration/representative-details")

  override def beforeEach() {
    authorizedUser()
    withNewCaching(createModel())
    withCaching[Choice](Some(Choice(SupplementaryDec)), choiceId)
    withCaching[RepresentativeDetails](None, RepresentativeDetails.formId)
  }

  override def afterEach() {
    reset(mockCustomsCacheService)
    reset(mockExportsCacheService)
  }

  "Representative Address Controller on GET" should {

    "return 200 code" in {

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "not populate the form fields if cache is empty" in {

      val result = route(app, getRequest(uri)).get

      contentAsString(result) mustNot include("checked=\"checked\"")
    }

    "populate the form fields with data from cache" in {

      withCaching[RepresentativeDetails](Some(correctRepresentativeDetails), RepresentativeDetails.formId)
      val result = route(app, getRequest(uri)).get

      contentAsString(result) must include("checked=\"checked\"")
    }
  }

  "Representative Address Controller on POST" should {

    "display the form page with error for empty field" when {

      "status is empty but eori provided" in {

        val emptyForm = buildRepresentativeDetailsJsonInput(eori = "12345678")
        val result = route(app, postRequest(uri, emptyForm)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(repTypeErrorEmpty))
      }

      "status provided but both EORI and address are empty" in {

        val emptyForm = buildRepresentativeDetailsJsonInput(status = "2")
        val result = route(app, postRequest(uri, emptyForm)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(eoriOrAddressEmpty))
      }
    }

    "display the form page with error for wrong value" when {

      "wrong value provided for EORI" in {

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(eoriError))
      }

      "wrong value provided for full name" in {

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(fullNameError))
      }

      "wrong value provided for first address line" in {

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(addressLineError))
      }

      "wrong value provided for city" in {

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(townOrCityError))
      }

      "wrong value provided for postcode" in {

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(postCodeError))
      }

      "wrong value provided for country" in {

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(countryError))
      }
    }

    "accept form with status and EORI if on supplementary journey" in {
      withCaching[RepresentativeDetails](None)
      withCaching[Choice](Some(Choice(SupplementaryDec)), choiceId)

      val result = route(app, postRequest(uri, correctRepresentativeDetailsEORIOnlyJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/additional-actors"))
    }

    "accept form with status and EORI if on standard journey" in {
      withCaching[Choice](Some(Choice(StandardDec)), choiceId)

      val result = route(app, postRequest(uri, correctRepresentativeDetailsEORIOnlyJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/carrier-details"))
    }

    "accept form with status and address if on supplementary journey" in {

      val result = route(app, postRequest(uri, correctRepresentativeDetailsAddressOnlyJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/additional-actors"))
    }

    "accept form with status and address only if on standard journey" in {
      withCaching[Choice](Some(Choice(StandardDec)), choiceId)

      val result = route(app, postRequest(uri, correctRepresentativeDetailsAddressOnlyJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/carrier-details"))
    }

    "save data to the cache" in {

      val model = createModel("12345")
      when(mockExportsCacheService.get(any())).thenReturn(Future.successful(Some(model)))

      val representativeDetails = Parties(representativeDetails = Some(correctRepresentativeDetails))

      val updatedModel = model.copy(parties = representativeDetails)

      when(mockExportsCacheService.update(any(), any[ExportsCacheModel]))
        .thenReturn(Future.successful(Some(updatedModel)))

      route(app, postRequest(uri, correctRepresentativeDetailsJSON)).get.futureValue

      theCacheModelUpdated.parties.representativeDetails.get must be(correctRepresentativeDetails)

      verify(mockCustomsCacheService)
        .cache[RepresentativeDetails](any(), ArgumentMatchers.eq(RepresentativeDetails.formId), any())(
          any(),
          any(),
          any()
        )
    }

    "return 303 code" when {
      "data is correct" in {
        val result = route(app, postRequest(uri, correctRepresentativeDetailsJSON)).get

        status(result) must be(SEE_OTHER)
      }

      "data is empty" in {
        val result = route(app, postRequest(uri, JsObject(Map[String, JsValue]().empty))).get

        status(result) must be(SEE_OTHER)
      }
    }

    "redirect to Additional Actors page if on supplementary journey" in {

      val result = route(app, postRequest(uri, correctRepresentativeDetailsJSON)).get
      val header = result.futureValue.header

      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/additional-actors"))
    }

    "redirect to Consignee Details page if on standard journey" in {
      withCaching[RepresentativeDetails](None)
      withCaching[Choice](Some(Choice(StandardDec)), choiceId)

      val result = route(app, postRequest(uri, correctRepresentativeDetailsJSON)).get
      val header = result.futureValue.header

      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/carrier-details"))
    }
  }
}

object RepresentativeDetailsPageControllerSpec {

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
