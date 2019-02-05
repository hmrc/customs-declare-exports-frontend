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
import forms.supplementary.SupervisingCustomsOffice
import forms.supplementary.SupervisingCustomsOfficeSpec._
import play.api.test.Helpers._

class SupervisingCustomsOfficeControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/declaration/supplementary/supervising-office")

  "Supervising customs office controller" should {
    "display supervising customs office form" in {
      authorizedUser()
      withCaching[SupervisingCustomsOffice](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.supervisingCustomsOffice"))
      stringResult must include(messages("supplementary.supervisingCustomsOffice.title"))
      stringResult must include(messages("supplementary.supervisingCustomsOffice.hint"))
    }

    "validate form - incorrect values" in {
      authorizedUser()
      withCaching[SupervisingCustomsOffice](None)

      val result = route(app, postRequest(uri, incorrectSupervisingCustomsOfficeJSON)).get

      contentAsString(result) must include(messages("supplementary.supervisingCustomsOffice.error"))
    }

    "validate form - no answer" in {
      authorizedUser()
      withCaching[SupervisingCustomsOffice](None)

      val result = route(app, postRequest(uri, emptySupervisingCustomsOfficeJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/warehouse"))
    }

    "validate form - correct value" in {
      authorizedUser()
      withCaching[SupervisingCustomsOffice](None)

      val result = route(app, postRequest(uri, correctSupervisingCustomsOfficeJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/supplementary/warehouse"))
    }
  }
}
