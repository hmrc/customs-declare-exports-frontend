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
import forms.declaration.OfficeOfExit
import forms.declaration.OfficeOfExitSpec._
import helpers.views.declaration.OfficeOfExitMessages
import play.api.test.Helpers._

class OfficeOfExitControllerSpec extends CustomExportsBaseSpec with OfficeOfExitMessages {

  private val uri: String = uriWithContextPath("/declaration/office-of-exit")

  before {
    authorizedUser()
    withCaching[OfficeOfExit](None)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Office Of Exit Controller on GET" should {

    "return 200 with a success" in {

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "read item from cache and display it" in {

      val cachedData = OfficeOfExit("999AAA45")
      withCaching[OfficeOfExit](Some(cachedData), "OfficeOfExit")

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      status(result) must be(OK)
      page must include("999AAA45")
    }
  }

  "Office Of Exit Controller on POST" should {

    "validate request and redirect - incorrect value" in {

      val result = route(app, postRequest(uri, incorrectOfficeOfExitJSON)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(officeOfExitError))
    }

    "validate request and redirect - empty form" in {

      val result = route(app, postRequest(uri, emptyOfficeOfExitJSON)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(officeOfExitEmpty))
    }

    "validate request and redirect - correct values" in {

      val result = route(app, postRequest(uri, correctOfficeOfExitJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/total-numbers-of-items"))
    }
  }
}
