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
import base.TestHelper._
import forms.AdditionalInformation
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class AdditionalInformationControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/declaration/supplementary/additional-information")

  "Additional Information Controller" should {
    "display additional information form" in {
      authorizedUser()
      withCaching[AdditionalInformation](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.additionalInformation.title"))
      stringResult must include(messages("supplementary.additionalInformation.code"))
      stringResult must include(messages("supplementary.additionalInformation.description"))
    }

    "validate form - incorrect values" in {
      authorizedUser()
      withCaching[AdditionalInformation](None)

      val incorrectAdditionalInformation: JsValue =
        JsObject(Map("code" -> JsString(createRandomString(6)), "description" -> JsString(createRandomString(71))))
      val result = route(app, postRequest(uri, incorrectAdditionalInformation)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.additionalInformation.code.error"))
      stringResult must include(messages("supplementary.additionalInformation.description.error"))
    }

    "validate form - empty form" in {
      authorizedUser()
      withCaching[AdditionalInformation](None)

      val emptyAdditionalInformation: JsValue = JsObject(Map[String, JsString]())
      val result = route(app, postRequest(uri, emptyAdditionalInformation)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/add-document")
      )
    }

    "validate form - special characters" in {
      authorizedUser()
      withCaching[AdditionalInformation](None)

      val additionalInformationWithSpecialChars: JsValue = JsObject(
        Map(
          "code" -> JsString("12345"),
          "decription" -> JsString("Description with ,. /'")
        )


      )
    }

    "validate form - correct values" in {
      authorizedUser()
      withCaching[AdditionalInformation](None)

      val correctAdditionalInformation: JsValue =
        JsObject(Map("code" -> JsString("12345"), "description" -> JsString(createRandomString(70))))

      val result = route(app, postRequest(uri, correctAdditionalInformation)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/add-document")
      )
    }
  }
}
