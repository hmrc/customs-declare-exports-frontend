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

package forms.declaration

import forms.declaration.UNDangerousGoodsCode._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString}

class UnDangerousGoodsCodeSpec extends WordSpec with MustMatchers {

  def formData(hasCode: String, code: Option[String]) =
    JsObject(Map(hasDangerousGoodsCodeKey -> JsString(hasCode), dangerousGoodsCodeKey -> JsString(code.getOrElse(""))))

  "UNDangerousGoodsCode mapping" should {

    "return form with errors" when {
      "provided with code too long" in {
        val form = UNDangerousGoodsCode.form.bind(formData("Yes", Some("12345")))

        form.errors mustBe Seq(FormError(dangerousGoodsCodeKey, "declaration.unDangerousGoodsCode.error.length"))
      }

      "provided with non-numeric code" in {
        val form = UNDangerousGoodsCode.form.bind(formData("Yes", Some("12E4")))

        form.errors mustBe Seq(FormError(dangerousGoodsCodeKey, "declaration.unDangerousGoodsCode.error.specialCharacters"))
      }

      "provided with no code when user said yes" in {
        val form = UNDangerousGoodsCode.form.bind(formData("Yes", None))

        form.errors mustBe Seq(FormError(dangerousGoodsCodeKey, "declaration.unDangerousGoodsCode.error.empty"))
      }
    }

    "return form without errors" when {
      "provided with valid input when user said Yes" in {
        val form = UNDangerousGoodsCode.form.bind(formData("Yes", Some("1234")))

        form.hasErrors must be(false)
      }

      "provided with no input when user said No" in {
        val form = UNDangerousGoodsCode.form.bind(formData("No", None))

        form.hasErrors must be(false)
      }

    }
  }
}
