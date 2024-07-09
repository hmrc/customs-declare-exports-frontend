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

package forms.section5

import forms.common.DeclarationPageBaseSpec
import forms.section5.NactCode._
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString}

class NactCodeSpec extends DeclarationPageBaseSpec {

  def formData(code: Option[String]) =
    JsObject(Map(nactCodeKey -> JsString(code.getOrElse(""))))

  "NactCode mapping" should {

    "return form with errors" when {
      "provided with invalid code" in {
        val form = NactCode.form.bind(formData(Some("invalid")), JsonBindMaxChars)

        form.errors mustBe Seq(FormError(nactCodeKey, "declaration.nationalAdditionalCode.error.invalid"))
      }

      "provided with missing code" in {
        val form = NactCode.form.bind(formData(None), JsonBindMaxChars)

        form.errors mustBe Seq(FormError(nactCodeKey, "declaration.nationalAdditionalCode.error.empty"))
      }
    }

    "return form without errors" when {
      "provided with valid input" in {
        val form = NactCode.form.bind(formData(Some("VATR")), JsonBindMaxChars)

        form.errors mustBe empty
      }
    }
  }

  "NactCode" when {
    testTariffContentKeysNoSpecialisation(NactCode, "tariff.declaration.item.nationalAdditionalCode")
  }
}
