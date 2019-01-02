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

package controllers

import base.CustomExportsBaseSpec
import base.ExportsTestData._
import forms.CancelDeclarationForm
import play.api.test.Helpers._

class CancelDeclarationControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/cancel-declaration")

  "CancelDeclarationController" should {
    "return 200 with a success" in {
      authorizedUser()
      withCaching[CancelDeclarationForm](None)

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "display cancel-declaration view" in {
      authorizedUser()
      withCaching(None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      stringResult must include("WCODataModelVersionCode")
      stringResult must include("ResponsibleAgencyName")
      stringResult must include("Submitter ID (Mandatory)")
    }

    "validate cancellation form submitted" in {
      authorizedUser()
      withCaching(None)
      successfulCustomsDeclarationResponse()

      val result = route(app, postRequest(uri, wrongJson)).get
      contentAsString(result) must include("Please enter a value")
    }

    "redirect to error page when cancellation failed in customs declarations" in {
      authorizedUser()
      withCaching(None)
      customsDeclaration400Response()

      val result = route(app, postRequest(uri, cancelJsonBody)).get
      val stringResult = contentAsString(result)

      status(result) must be(BAD_REQUEST)
      stringResult must include("There is a problem with a service")
      stringResult must include("Please try again later.")
    }

    "redirect to next page" in {
      authorizedUser()
      withCaching(None)
      successfulCustomsDeclarationResponse()

      val result = route(app, postRequest(uri, cancelJsonBody)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include("Confirmation page")
      stringResult must include("Your reference number is")
    }
  }
}
