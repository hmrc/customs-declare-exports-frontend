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
import base.ExportsTestData._
import forms.supplementary.DeclarationAdditionalActors
import play.api.test.Helpers._

class DeclarationAdditionalActorsControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/declaration/supplementary/additional-actors")

  "Declaration additional actors controller" should {
    "display declaration additional actors form" in {
      authorizedUser()
      withCaching[DeclarationAdditionalActors](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.additionalActors.title"))
      stringResult must include(messages("supplementary.eori"))
      stringResult must include(messages("supplementary.eori.hint"))
      stringResult must include(messages("supplementary.partyType"))
      stringResult must include(messages("supplementary.partyType.CS"))
      stringResult must include(messages("supplementary.partyType.MF"))
      stringResult must include(messages("supplementary.partyType.FW"))
      stringResult must include(messages("supplementary.partyType.WH"))
    }

    "validate form - incorrect values" in {
      authorizedUser()
      withCaching[DeclarationAdditionalActors](None)

      val result = route(app, postRequest(uri, incorrectAdditionalActors)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.eori.error"))
      stringResult must include(messages("supplementary.partyType.error"))
    }

    "validate form - optional data allowed" in {
      pending
      authorizedUser()
      withCaching[DeclarationAdditionalActors](None)

      val result = route(app, postRequest(uri, emptyAdditionalActors)).get
      val header = result.futureValue.header

      status(result) mustBe (SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration-holder-of-authorization"))
    }

    "validate form - correct values" in {
      pending
      authorizedUser()
      withCaching[DeclarationAdditionalActors](None)

      val result = route(app, postRequest(uri, correctAdditionalActors)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration-holder-of-authorization"))
    }
  }
}
