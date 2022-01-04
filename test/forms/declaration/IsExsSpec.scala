/*
 * Copyright 2022 HM Revenue & Customs
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

import base.UnitWithMocksSpec
import play.api.data.FormError

class IsExsSpec extends UnitWithMocksSpec {

  "Is Exs" should {

    "has a correct key" in {

      IsExs.isExsKey mustBe "isExs"
    }
  }

  "Is Exs form" should {

    "has not errors" when {

      "the answer is Yes" in {

        IsExs.form.fillAndValidate(IsExs("Yes")).errors mustBe empty
      }

      "the answer is No" in {

        IsExs.form.fillAndValidate(IsExs("No")).errors mustBe empty
      }
    }

    "has error" when {

      "there is no answer" in {

        val expectedError = FormError(IsExs.isExsKey, "declaration.exs.error")

        val result = IsExs.form.fillAndValidate(IsExs(""))
        result.errors.size mustBe 1
        result.errors.head mustBe expectedError
      }

      "there is incorrect answer (Not possible throught the UI)" in {

        val expectedError = FormError(IsExs.isExsKey, "declaration.exs.error")

        val result = IsExs.form.fillAndValidate(IsExs("incorrect"))
        result.errors.size mustBe 1
        result.errors.head mustBe expectedError
      }
    }
  }
}
