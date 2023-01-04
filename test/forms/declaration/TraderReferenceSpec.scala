/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.declaration

import base.UnitSpec
import forms.declaration.TraderReference.traderReferenceKey
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString}

class TraderReferenceSpec extends UnitSpec {

  "Trader reference mapping" should {

    "return form with errors" when {

      "nothing is entered" in {
        val form = TraderReference.form.bind(JsObject(Map(traderReferenceKey -> JsString(""))), JsonBindMaxChars)

        form.errors mustBe Seq(FormError(traderReferenceKey, "declaration.traderReference.error.empty"))
      }

      "string is over 19 characters long" in {
        val form = TraderReference.form.bind(JsObject(Map(traderReferenceKey -> JsString("this/string/is/too/long"))), JsonBindMaxChars)

        form.errors mustBe Seq(FormError(traderReferenceKey, "declaration.traderReference.error.invalid"))
      }

      "string contains chars other than numbers, uppercase letters, brackets or forward slashes" in {
        val form = TraderReference.form.bind(JsObject(Map(traderReferenceKey -> JsString("|[]!"))), JsonBindMaxChars)

        form.errors mustBe Seq(FormError(traderReferenceKey, "declaration.traderReference.error.invalid"))
      }

      "string contains lowercase letters" in {
        val form = TraderReference.form.bind(JsObject(Map(traderReferenceKey -> JsString("badstring"))), JsonBindMaxChars)

        form.errors mustBe Seq(FormError(traderReferenceKey, "declaration.traderReference.error.invalid"))
      }
    }

    "return form without errors" when {

      "submitted with valid input" in {
        val form = TraderReference.form.bind(JsObject(Map(traderReferenceKey -> JsString("INVOICE123/4"))), JsonBindMaxChars)

        form.hasErrors mustBe false
      }
    }
  }
}
