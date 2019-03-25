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
import play.api.test.Helpers._

class SupervisingCustomsOfficeControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/declaration/supervising-office")

  "Supervising Customs Office Controller on display" should {

    "display supervising customs office form" in {
      authorizedUser()
      withCaching[SupervisingCustomsOffice](None)
      withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.supervisingCustomsOffice"))
      stringResult must include(messages("supplementary.supervisingCustomsOffice.title"))
      stringResult must include(messages("supplementary.supervisingCustomsOffice.hint"))
    }

    "display \"Back\" button that links to \"Procedure codes\" page" in {

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("site.back"))
      stringResult must include(messages("/declaration/previous-documents"))
    }

    "display \"Save and continue\" button on page" in {
      withCaching[SupervisingCustomsOffice](None)
      withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      resultAsString must include(messages("site.save_and_continue"))
      resultAsString must include("button id=\"submit\" class=\"button\"")
    }

    "validate form - incorrect values" in {
      authorizedUser()
      withCaching[SupervisingCustomsOffice](None)
      withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

      val result = route(app, postRequest(uri, incorrectSupervisingCustomsOfficeJSON)).get

      contentAsString(result) must include(messages("supplementary.supervisingCustomsOffice.error"))
    }

    "validate form and redirect - no answer" in {
      authorizedUser()
      withCaching[SupervisingCustomsOffice](None)
      withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

      val result = route(app, postRequest(uri, emptySupervisingCustomsOfficeJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/warehouse"))
    }

    "validate form and redirect - correct value" in {
      authorizedUser()
      withCaching[SupervisingCustomsOffice](None)
      withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

      val result = route(app, postRequest(uri, correctSupervisingCustomsOfficeJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/warehouse"))
    }
  }
}
