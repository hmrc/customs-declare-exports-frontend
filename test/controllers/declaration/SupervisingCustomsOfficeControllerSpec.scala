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
import forms.declaration.SupervisingCustomsOffice
import forms.declaration.SupervisingCustomsOfficeSpec._
import helpers.views.declaration.SupervisingCustomsOfficeMessages
import play.api.test.Helpers._

class SupervisingCustomsOfficeControllerSpec extends CustomExportsBaseSpec with SupervisingCustomsOfficeMessages {

  val uri = uriWithContextPath("/declaration/supervising-office")

  before {
    authorizedUser()
    withCaching[SupervisingCustomsOffice](None)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Supervising Customs Office Controller on GET" should {

    "return 200 code" in {

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }
  }

  "Supervising Customs Office Controller on POST" should {

    "validate request - incorrect values" in {

      val result = route(app, postRequest(uri, incorrectSupervisingCustomsOfficeJSON)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(scoError))
    }

    "validate request and redirect - no answer" in {

      val result = route(app, postRequest(uri, emptySupervisingCustomsOfficeJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/warehouse"))
    }

    "validate request and redirect - correct value" in {

      val result = route(app, postRequest(uri, correctSupervisingCustomsOfficeJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/warehouse"))
    }
  }
}
