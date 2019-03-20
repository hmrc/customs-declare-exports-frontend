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
import base.TestHelper._
import forms.Choice
import forms.Choice.choiceId
import forms.supplementary.RepresentativeDetails
import forms.supplementary.RepresentativeDetailsSpec._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.Future

class RepresentativeDetailsPageControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  import RepresentativeDetailsPageControllerSpec._
  private val uri = uriWithContextPath("/declaration/supplementary/representative-details")

  before {
    authorizedUser()
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Representative Address Controller on display page" should {

    "return 200 code" in {
      val result = displayPageTestScenario()

      status(result) must be(OK)
    }

    "display page title" in {
      val result = displayPageTestScenario()

      contentAsString(result) must include(messages("supplementary.representative.title"))
    }

    "display \"Back\" button that links to \"Declarant details\" page" in {
      val result = displayPageTestScenario()

      contentAsString(result) must include(messages("site.back"))
      contentAsString(result) must include("/declaration/supplementary/declarant-details")
    }

    "display page header" in {
      val result = displayPageTestScenario()

      contentAsString(result) must include(messages("supplementary.representative.header"))
    }

    "display information to enter EORI number" in {
      val result = displayPageTestScenario()

      contentAsString(result) must include(messages("supplementary.representative.eori.info"))
    }

    "display element to enter EORI number" in {
      val result = displayPageTestScenario()

      contentAsString(result) must include(messages("supplementary.representative.eori.info"))
      contentAsString(result) must include(messages("supplementary.eori"))
      contentAsString(result) must include(messages("supplementary.eori.hint"))
    }

    "display information to enter representatives name and address" in {
      val result = displayPageTestScenario()

      contentAsString(result) must include(messages("supplementary.representative.address.info"))
    }

    "display element to enter full name" in {
      val result = displayPageTestScenario()

      contentAsString(result) must include(messages("supplementary.address.fullName"))
    }

    "display element to enter first address line" in {
      val result = displayPageTestScenario()

      contentAsString(result) must include(messages("supplementary.address.addressLine"))
    }

    "display element to enter city" in {
      val result = displayPageTestScenario()

      contentAsString(result) must include(messages("supplementary.address.townOrCity"))
    }

    "display element to enter postcode" in {
      val result = displayPageTestScenario()

      contentAsString(result) must include(messages("supplementary.address.postCode"))
    }

    "display element to enter country" in {
      val result = displayPageTestScenario()

      contentAsString(result) must include(messages("supplementary.address.country"))
    }

    "display information to choose type of representation" in {
      val result = displayPageTestScenario()

      contentAsString(result) must include(messages("supplementary.representative.representationType.header"))
    }

    "display radio button element to enter Representative Status Code" in {
      val result = displayPageTestScenario()

      contentAsString(result) must include(messages("supplementary.representative.representationType.direct"))
      contentAsString(result) must include(messages("supplementary.representative.representationType.indirect"))
    }

    "display \"Save and continue\" button" in {
      val result = displayPageTestScenario()

      contentAsString(result) must include(messages("site.save_and_continue"))
      contentAsString(result) must include("button id=\"submit\" class=\"button\"")
    }

    "not populate the form fields if cache is empty" in {
      val result = displayPageTestScenario()

      contentAsString(result) mustNot include("checked=\"checked\"")
    }

    "populate the form fields with data from cache" in {
      val result = displayPageTestScenario(Some(correctRepresentativeDetails))

      contentAsString(result) must include("checked=\"checked\"")
    }

    def displayPageTestScenario(cacheValue: Option[RepresentativeDetails] = None): Future[Result] = {
      withCaching[RepresentativeDetails](cacheValue, RepresentativeDetails.formId)
      route(app, getRequest(uri)).get
    }
  }

  "Representative Address Controller on submit" should {

    "display the form page with error for empty field" when {

      "status is empty" in {
        withCaching[RepresentativeDetails](None)
        withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

        val emptyForm = buildRepresentativeDetailsJsonInput()
        val result = route(app, postRequest(uri, emptyForm)).get

        contentAsString(result) must include(messages("supplementary.representative.representationType.error.empty"))
      }

      "status provided but both EORI and address are empty" in {
        withCaching[RepresentativeDetails](None)
        withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

        val emptyForm = buildRepresentativeDetailsJsonInput(status = "2")
        val result = route(app, postRequest(uri, emptyForm)).get

        contentAsString(result) must include(messages("supplementary.namedEntityDetails.error"))
      }
    }

    "display the form page with error for wrong value" when {

      "wrong value provided for EORI" in {
        withCaching[RepresentativeDetails](None)
        withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        contentAsString(result) must include(messages("supplementary.eori.error"))
      }

      "wrong value provided for full name" in {
        withCaching[RepresentativeDetails](None)
        withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        contentAsString(result) must include(messages("supplementary.address.fullName.error"))
      }

      "wrong value provided for first address line" in {
        withCaching[RepresentativeDetails](None)
        withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        contentAsString(result) must include(messages("supplementary.address.addressLine.error"))
      }

      "wrong value provided for city" in {
        withCaching[RepresentativeDetails](None)
        withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        contentAsString(result) must include(messages("supplementary.address.townOrCity.error"))
      }

      "wrong value provided for postcode" in {
        withCaching[RepresentativeDetails](None)
        withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        contentAsString(result) must include(messages("supplementary.address.postCode.error"))
      }

      "wrong value provided for country" in {
        withCaching[RepresentativeDetails](None)
        withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        contentAsString(result) must include(messages("supplementary.address.country.error"))
      }
    }

    "accept form with status and EORI only" in {
      withCaching[RepresentativeDetails](None)
      withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

      val result = route(app, postRequest(uri, correctRepresentativeDetailsEORIOnlyJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/additional-actors")
      )
    }

    "accept form with status and address only" in {
      withCaching[RepresentativeDetails](None)
      withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

      val result = route(app, postRequest(uri, correctRepresentativeDetailsAddressOnlyJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/additional-actors")
      )
    }

    "save data to the cache" in {
      reset(mockCustomsCacheService)
      withCaching[RepresentativeDetails](None)
      withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

      route(app, postRequest(uri, correctRepresentativeDetailsJSON)).get.futureValue

      verify(mockCustomsCacheService)
        .cache[RepresentativeDetails](any(), ArgumentMatchers.eq(RepresentativeDetails.formId), any())(
          any(),
          any(),
          any()
        )
    }

    "return 303 code" in {
      withCaching[RepresentativeDetails](None)
      withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

      val result = route(app, postRequest(uri, correctRepresentativeDetailsJSON)).get

      status(result) must be(SEE_OTHER)
    }

    "redirect to Additional Actors page" in {
      withCaching[RepresentativeDetails](None)
      withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

      val result = route(app, postRequest(uri, correctRepresentativeDetailsJSON)).get
      val header = result.futureValue.header

      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/additional-actors")
      )
    }
  }

}

object RepresentativeDetailsPageControllerSpec {

  val incorrectRepresentativeDetails: JsValue = buildRepresentativeDetailsJsonInput(
    eori = createRandomString(18),
    fullName = createRandomString(71),
    addressLine = createRandomString(71),
    townOrCity = createRandomString(36),
    postCode = createRandomString(10),
    country = createRandomString(3)
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
