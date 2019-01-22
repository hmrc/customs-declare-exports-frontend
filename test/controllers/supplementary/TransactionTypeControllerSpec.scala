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
import forms.supplementary.TransactionType
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class TransactionTypeControllerSpec extends CustomExportsBaseSpec {

  val uri = uriWithContextPath("/declaration/supplementary/transaction-type")

  "Transaction type controller" should {
    "display transaction type form" in {
      authorizedUser()
      withCaching[TransactionType](None)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.transactionType.documentTypeCode.header"))
      stringResult must include(messages("supplementary.transactionType.documentTypeCode"))
      stringResult must include(messages("supplementary.transactionType.hint"))
      stringResult must include(messages("supplementary.transactionType.identifier"))
    }

    "validate form - empty value" in {
      authorizedUser()
      withCaching[TransactionType](None)

      val emptyForm: JsValue = JsObject(Map("documentTypeCode" -> JsString(""), "identifier" -> JsString("")))
      val result = route(app, postRequest(uri, emptyForm)).get

      contentAsString(result) must include(messages("supplementary.transactionType.documentTypeCode.empty"))
    }

    "validate form - incorrect values" in {
      authorizedUser()
      withCaching[TransactionType](None)

      val incorrectForm: JsValue = JsObject(Map("documentTypeCode" -> JsString("123"), "identifier" -> JsString("123")))
      val result = route(app, postRequest(uri, incorrectForm)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.transactionType.documentTypeCode.error"))
      stringResult must include(messages("supplementary.transactionType.identifier.error"))
    }

    "validate form - correct values" in {
      authorizedUser()
      withCaching[TransactionType](None)

      val correctForm: JsValue = JsObject(Map("documentTypeCode" -> JsString("1"), "identifier" -> JsString("1")))
      val result = route(app, postRequest(uri, correctForm)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/good-item-number")
      )
    }
  }

}
