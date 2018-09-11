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

package controllers

import base.CustomExportsBaseSpec
import play.api.test.Helpers._
import base.ExportsTestData._
import forms.SimpleDeclarationForm

class SimpleDeclarationControllerSpec extends CustomExportsBaseSpec{

  val uri = uriWithContextPath("/simple-declaration")

  "SimpleDeclarationSpec" should {
    "process only authenticated user requests " in {
      authorizedUser()
      withCaching(None)
      val result = route(app, getRequest(uri))

      result.map(status(_) must be (OK))
    }

    "return 200 with a success" in {
      authorizedUser()
      withCaching[SimpleDeclarationForm](None)

      val result = route(app, getRequest(uri))

      result.map(status(_) must be (OK))
    }

    "display Simple-declaration" in {
      authorizedUser()
      withCaching(None)

      val result = route(app, getRequest(uri))

      result.map(contentAsString(_).contains("Do you have a representative?") must be (true))
      result.map(contentAsString(_).contains("Is consolidate DUCR to wider shipment?") must be (true))
      result.map(contentAsString(_).contains("Building and street") must be (true))
    }

    "should validate form submitted" in {
      authorizedUser()
      withCaching(None)
      succesfulCustomsDeclarationReponse()

      val result = route(app, postRequest(uri, wrongJson))
      result.map(contentAsString(_) must not be ("Declaration has been submitted successfully."))
    }

    "should redirect to next page" in {
      authorizedUser()
      withCaching(None)
      succesfulCustomsDeclarationReponse()

      val result = route(app, postRequest(uri, jsonBody))
      result.map(status(_) must be(OK))
      result.map(contentAsString(_) must be ("Declaration has been submitted successfully."))
    }
  }
}
