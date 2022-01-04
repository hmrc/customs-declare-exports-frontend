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

import base.JourneyTypeTestRunner
import forms.common.DeclarationPageBaseSpec
import forms.declaration.AuthorisationProcedureCodeChoice.formFieldName
import models.declaration.AuthorisationProcedureCode.Code1040

class AuthorisationProcedureCodeChoiceSpec extends DeclarationPageBaseSpec with JourneyTypeTestRunner {

  "AuthorisationProcedureCodeChoice mapping" should {
    "return Right" when {
      "provided with a correct value" in {
        val input = Map(formFieldName -> Code1040.toString)

        val result = AuthorisationProcedureCodeChoice.mapping.bind(input)

        result.isRight mustBe true
        result.right.get mustBe AuthorisationProcedureCodeChoice(Code1040)
      }
    }

    "return form with errors" when {
      "provided with empty String" in {
        val input = Map(formFieldName -> "")

        val result = AuthorisationProcedureCodeChoice.mapping.bind(input)

        result.isLeft mustBe true
        result.left.get.head.message mustBe "declaration.authorisations.procedureCodeChoice.error.empty"
      }

      "provided with an invalid String value" in {
        val input = Map(formFieldName -> "sdfsdf")

        val result = AuthorisationProcedureCodeChoice.mapping.bind(input)

        result.isLeft mustBe true
        result.left.get.head.message mustBe "declaration.authorisations.procedureCodeChoice.error.empty"
      }
    }
  }
}
