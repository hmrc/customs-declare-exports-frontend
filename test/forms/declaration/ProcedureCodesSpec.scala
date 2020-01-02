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

import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError

class ProcedureCodesSpec extends WordSpec with MustMatchers {
  import ProcedureCodesSpec._

  "Procedure Code form" should {

    "return form with errors" when {

      "procedure code is incorrect" in {
        val form = ProcedureCodes.form().bind(incorrectProcedureCode)

        form.errors mustBe Seq(FormError("procedureCode", "supplementary.procedureCodes.procedureCode.error.length"))
      }

      "additional code has incorrect length" in {
        val form = ProcedureCodes.form().bind(tooLongAdditionalProcedureCode)

        form.errors.length must be(1)
        form.errors.head must be(FormError("additionalProcedureCode", "supplementary.procedureCodes.additionalProcedureCode.error.length"))
      }

      "additional code has special characters" in {

        val form = ProcedureCodes.form().bind(additionalProcedureCodeSpecialChars)

        form.errors.length must be(1)
        form.errors.head must be(FormError("additionalProcedureCode", "supplementary.procedureCodes.additionalProcedureCode.error.specialCharacters"))
      }
    }
  }
}

object ProcedureCodesSpec {

  val incorrectProcedureCode: Map[String, String] = Map("procedureCode" -> "21", "additionalProcedureCode" -> "123")

  val tooLongAdditionalProcedureCode: Map[String, String] =
    Map("procedureCode" -> "2112", "additionalProcedureCode" -> "123456")

  val additionalProcedureCodeSpecialChars: Map[String, String] =
    Map("procedureCode" -> "2112", "additionalProcedureCode" -> "#$%")
}
