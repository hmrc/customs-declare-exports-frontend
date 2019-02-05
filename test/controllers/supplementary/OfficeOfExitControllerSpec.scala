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
import play.api.test.Helpers._

class OfficeOfExitControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/declaration/supplementary/office-of-exit")

  "Office of exit controller" should {
    "display office of exit form" in {
      authorizedUser()
      withCaching[OfficeOfExit](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.officeOfExit.title"))
      stringResult must include(messages("supplementary.officeOfExit"))
      stringResult must include(messages("supplementary.officeOfExit.hint"))
    }

    "validate form - incorrect value" in {
      authorizedUser()
      withCaching[OfficeOfExit](None)

      val result = route(app, postRequest(uri, incorrectOfficeOfExitJSON)).get

      contentAsString(result) must include(messages("supplementary.officeOfExit.error"))
    }

    "validate form - empty form" in {
      authorizedUser()
      withCaching[OfficeOfExit](None)

      val result = route(app, postRequest(uri, emptyOfficeOfExitJSON)).get

      contentAsString(result) must include(messages("supplementary.officeOfExit.empty"))
    }

    "validate form - correct values" in {
      authorizedUser()
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
