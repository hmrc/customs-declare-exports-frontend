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
import forms.supplementary.RepresentativeDetails.StatusCodes._
import forms.supplementary.{AddressAndIdentification, RepresentativeDetails}
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
  private val uri = uriWithContextPath("/declaration/supplementary/representative")

  before {
    authorizedUser()
  }

  "RepresentativeAddressController on displayRepresentativeDetailsPage" should {
    "return 200 code" in {
      val result = displayPageTestScenario()
      status(result) must be(OK)
    }

    "display page title" in {
      val result = displayPageTestScenario()
      contentAsString(result) must include(messages("supplementary.representative.title"))
    }

    "display \"back\" button that links to declarant address page" in {
      val result = displayPageTestScenario()
      contentAsString(result) must include(messages("site.back"))
      contentAsString(result) must include("declarant-address")
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
      contentAsString(result) must include(messages("supplementary.fullName"))
    }

    "display element to enter first address line" in {
      val result = displayPageTestScenario()
      contentAsString(result) must include(messages("supplementary.addressLine"))
    }

    "display element to enter city" in {
      val result = displayPageTestScenario()
      contentAsString(result) must include(messages("supplementary.townOrCity"))
    }

    "display element to enter postcode" in {
      val result = displayPageTestScenario()
      contentAsString(result) must include(messages("supplementary.postCode"))
    }

    "display element to enter country" in {
      val result = displayPageTestScenario()
      contentAsString(result) must include(messages("supplementary.country"))
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
      val representativeAddress = RepresentativeDetails(
        address = AddressAndIdentification(
          eori = Some("GB111222333444"),
          fullName = Some("Full Name"),
          addressLine = Some("Address Line"),
          townOrCity = Some("Town or City"),
          postCode = Some("PostCode"),
          country = Some("UK")
        ),
        statusCode = DirectRepresentative
      )
      val result = displayPageTestScenario(Some(representativeAddress))
      contentAsString(result) must include("checked=\"checked\"")
    }

    def displayPageTestScenario(cacheValue: Option[RepresentativeDetails] = None): Future[Result] = {
      withCaching[RepresentativeDetails](cacheValue, RepresentativeDetails.formId)
      route(app, getRequest(uri)).get
    }
  }

  "RepresentativeAddressController on submitRepresentativeData" should {

    "accept empty form" in {
      withCaching[RepresentativeDetails](None)

      val emptyFormWithStatus = emptyRepresentativeDetailsWithStatus
      val result = route(app, postRequest(uri, emptyFormWithStatus)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/consignee-address")
      )
    }

    "return error when status is missing" in {
      withCaching[RepresentativeDetails](None)

      val emptyForm = emptyRepresentativeDetails
      val result = route(app, postRequest(uri, emptyForm)).get

      contentAsString(result) must include(messages("supplementary.representative.representationType.error.empty"))
    }

    "display the form page with error for wrong value" when {
      "Wrong value provided for EORI" in {
        withCaching[RepresentativeDetails](None)

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        contentAsString(result) must include(messages("supplementary.eori.error"))
      }

      "Wrong value provided for full name" in {
        withCaching[RepresentativeDetails](None)

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        contentAsString(result) must include(messages("supplementary.fullName.error"))
      }

      "Wrong value provided for first address line" in {
        withCaching[RepresentativeDetails](None)

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        contentAsString(result) must include(messages("supplementary.addressLine.error"))
      }

      "Wrong value provided for city" in {
        withCaching[RepresentativeDetails](None)

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        contentAsString(result) must include(messages("supplementary.townOrCity.error"))
      }

      "Wrong value provided for postcode" in {
        withCaching[RepresentativeDetails](None)

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        contentAsString(result) must include(messages("supplementary.postCode.error"))
      }

      "Wrong value provided for country" in {
        withCaching[RepresentativeDetails](None)

        val incorrectFormData = incorrectRepresentativeDetails
        val result = route(app, postRequest(uri, incorrectFormData)).get

        contentAsString(result) must include(messages("supplementary.country.error"))
      }
    }

    "save the data to the cache" in {
      reset(mockCustomsCacheService)
      withCaching[RepresentativeDetails](None)

      route(app, postRequest(uri, correctRepresentativeDetails)).get.futureValue

      verify(mockCustomsCacheService)
        .cache[RepresentativeDetails](any(), ArgumentMatchers.eq(RepresentativeDetails.formId), any())(
          any(),
          any(),
          any()
        )
    }

    "return 303 code" in {
      withCaching[RepresentativeDetails](None)

      val result = route(app, postRequest(uri, correctRepresentativeDetails)).get

      status(result) must be(SEE_OTHER)
    }

    "redirect to \"additional-actors\" page" in {
      withCaching[RepresentativeDetails](None)

      val result = route(app, postRequest(uri, correctRepresentativeDetails)).get
      val header = result.futureValue.header

      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/consignee-address")
      )
    }

  }
}

object RepresentativeDetailsPageControllerSpec {

  val correctRepresentativeDetails: JsValue = JsObject(
    Map(
      "address.eori" -> JsString("PL213472539481923"),
      "address.fullName" -> JsString("Full name"),
      "address.addressLine" -> JsString("Address"),
      "address.townOrCity" -> JsString("Town or city"),
      "address.postCode" -> JsString("PostCode1"),
      "address.country" -> JsString("Poland"),
      "statusCode" -> JsString("2")
    )
  )

  val incorrectRepresentativeDetails: JsValue = JsObject(
    Map(
      "address.eori" -> JsString(createRandomString(18)),
      "address.fullName" -> JsString(createRandomString(71)),
      "address.addressLine" -> JsString(createRandomString(71)),
      "address.townOrCity" -> JsString(createRandomString(36)),
      "address.postCode" -> JsString(createRandomString(10)),
      "address.country" -> JsString(createRandomString(3)),
      "statusCode" -> JsString("")
    )
  )

  val emptyRepresentativeDetails: JsValue = JsObject(
    Map(
      "address.eori" -> JsString(""),
      "address.fullName" -> JsString(""),
      "address.addressLine" -> JsString(""),
      "address.townOrCity" -> JsString(""),
      "address.postCode" -> JsString(""),
      "address.country" -> JsString(""),
      "statusCode" -> JsString("")
    )
  )

  val emptyRepresentativeDetailsWithStatus: JsValue = JsObject(
    Map(
      "address.eori" -> JsString(""),
      "address.fullName" -> JsString(""),
      "address.addressLine" -> JsString(""),
      "address.townOrCity" -> JsString(""),
      "address.postCode" -> JsString(""),
      "address.country" -> JsString(""),
      "statusCode" -> JsString("2")
    )
  )
}
