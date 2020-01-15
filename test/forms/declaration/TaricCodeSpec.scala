/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.declaration.TaricCode._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString}

class TaricCodeSpec extends WordSpec with MustMatchers {

  def formData(code: Option[String]) =
    JsObject(Map(taricCodeKey -> JsString(code.getOrElse(""))))

  "TaricCode mapping" should {

    "return form with errors" when {
      "provided with code too long" in {
        val form = TaricCode.form.bind(formData(Some("12345")))

        form.errors mustBe Seq(FormError(taricCodeKey, "declaration.taricAdditionalCodes.error.invalid"))
      }

      "provided with code too short" in {
        val form = TaricCode.form.bind(formData(Some("123")))

        form.errors mustBe Seq(FormError(taricCodeKey, "declaration.taricAdditionalCodes.error.invalid"))
      }

      "provided with non-alphanumeric code" in {
        val form = TaricCode.form.bind(formData(Some("!234")))

        form.errors mustBe Seq(FormError(taricCodeKey, "declaration.taricAdditionalCodes.error.invalid"))
      }

      "provided with non-alphanumeric code and too long" in {
        val form = TaricCode.form.bind(formData(Some("!23456789")))

        form.errors mustBe Seq(FormError(taricCodeKey, "declaration.taricAdditionalCodes.error.invalid"))
      }
    }

    "return form without errors" when {
      "provided with valid input" in {
        val form = TaricCode.form.bind(formData(Some("1234")))

        form.errors mustBe empty
      }

    }
  }
}
