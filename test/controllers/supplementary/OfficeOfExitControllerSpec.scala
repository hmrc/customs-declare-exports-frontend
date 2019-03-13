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
import forms.supplementary.OfficeOfExit
import forms.supplementary.OfficeOfExitSpec._
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._

class OfficeOfExitControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  val uri = uriWithContextPath("/declaration/supplementary/office-of-exit")

  before {
    authorizedUser()
  }

  "Office Of Exit Controller on display page" should {

    "display office of exit form" in {
      withCaching[OfficeOfExit](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.officeOfExit.title"))
      stringResult must include(messages("supplementary.officeOfExit"))
      stringResult must include(messages("supplementary.officeOfExit.hint"))
    }

    "display \"Back\" button that links to \"Location of goods\" page" in {

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("site.back"))
      stringResult must include(messages("/declaration/supplementary/location-of-goods"))
    }

    "display \"Save and continue\" button on page" in {

      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      resultAsString must include(messages("site.save_and_continue"))
      resultAsString must include("button id=\"submit\" class=\"button\"")
    }

    "validate form - incorrect value" in {
      withCaching[OfficeOfExit](None)

      val result = route(app, postRequest(uri, incorrectOfficeOfExitJSON)).get

      contentAsString(result) must include(messages("supplementary.officeOfExit.error"))
    }

    "validate form - empty form" in {
      withCaching[OfficeOfExit](None)

      val result = route(app, postRequest(uri, emptyOfficeOfExitJSON)).get

      contentAsString(result) must include(messages("supplementary.officeOfExit.empty"))
    }

    "validate form and redirect - correct values" in {
      withCaching[OfficeOfExit](None)

      val result = route(app, postRequest(uri, correctOfficeOfExitJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/transport-information")
      )
    }
  }
}
