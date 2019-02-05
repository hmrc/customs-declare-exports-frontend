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
import forms.supplementary.DeclarationHolder
import forms.supplementary.DeclarationHolderSpec._
import play.api.test.Helpers._

class DeclarationHolderControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/declaration/supplementary/holder-of-authorisation")

  "Declaration Holder of Authorisation controller" should {
    "display declaration holder of authorisation form" in {
      authorizedUser()
      withCaching[DeclarationHolder](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.declarationHolder.title"))
      stringResult must include(messages("supplementary.declarationHolder.authorisationCode"))
      stringResult must include(messages("supplementary.declarationHolder.authorisationCode.hint"))
      stringResult must include(messages("supplementary.eori"))
      stringResult must include(messages("supplementary.eori.hint"))
    }

    "validate form - incorrect values" in {
      authorizedUser()
      withCaching[DeclarationHolder](None)

      val result = route(app, postRequest(uri, incorrectDeclarationHolderJSON)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.declarationHolder.authorisationCode.error"))
      stringResult must include(messages("supplementary.eori.error"))
    }

    "validate form - no answers" in {
      authorizedUser()
      withCaching[DeclarationHolder](None)

      val result = route(app, postRequest(uri, emptyDeclarationHolderJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/destination-countries")
      )
    }

    "validate form - correct values" in {
      authorizedUser()
      withCaching[DeclarationHolder](None)

      val result = route(app, postRequest(uri, correctDeclarationHolderJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/destination-countries")
      )
    }
  }
}
