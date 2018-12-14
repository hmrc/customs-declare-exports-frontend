/*
 * Copyright 2018 HM Revenue & Customs
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
import forms.supplementary.{Address, RepresentativeAddress}
import org.scalatest.BeforeAndAfter
import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.Future

class RepresentativeDetailsPageControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  private val uri = uriWithContextPath("/declaration/supplementary/representative")

  before {
    authorizedUser()
  }


  "RepresentativeAddressController on displayForm" should {
    "return 200 code" in {
      val result = displayPageTestScenario()
      status(result) must be(OK)
    }

    "display page title with hint" in {
      val result = displayPageTestScenario()
      contentAsString(result) must include(messages("supplementary.representative.title"))
      contentAsString(result) must include(messages("supplementary.representative.title.hint"))
    }

    "display \"back\" button that links to declarant address page" in {
      val result = displayPageTestScenario()
      contentAsString(result) must include(messages("site.back"))
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
      contentAsString(result) must include(messages("supplementary.representative.eori"))
      contentAsString(result) must include(messages("supplementary.representative.eori.hint"))
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

    "display element to enter post code" in {
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
      val representativeAddress = RepresentativeAddress(
        address = Address(
          eori = "GB111222333444",
          fullName = "Full Name",
          addressLine = "Address Line",
          townOrCity = "Town or City",
          postCode = "PostCode",
          country = "UK"
        ),
        statusCode = "2"
      )
      val result = displayPageTestScenario(Some(representativeAddress))
      contentAsString(result) must include("checked=\"checked\"")
    }


    def displayPageTestScenario(cacheValue: Option[RepresentativeAddress] = None): Future[Result] = {
      withCaching[RepresentativeAddress](cacheValue, RepresentativeAddress.formId)
      route(app, getRequest(uri)).get
    }

  }


  "RepresentativeAddressController on submitRepresentativeData" should {
    // TODO: Write tests similar to DeclarationTypePageControllerSpec
  }

}
