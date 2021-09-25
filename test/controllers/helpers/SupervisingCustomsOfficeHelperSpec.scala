/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.helpers

import base.UnitSpec
import controllers.helpers.SupervisingCustomsOfficeHelper.isConditionForAllProcedureCodesVerified
import models.codes.AdditionalProcedureCode.NO_APC_APPLIES_CODE
import services.cache.{ExportsDeclarationBuilder, ExportsItemBuilder}

class SupervisingCustomsOfficeHelperSpec extends UnitSpec with ExportsDeclarationBuilder with ExportsItemBuilder {

  val condVerified = anItem(withProcedureCodes(Some("1040"), Seq(NO_APC_APPLIES_CODE)))

  "SupervisingCustomsOfficeHelper on isConditionForAllProcedureCodesVerified" should {

    "return true" when {

      "a declaration contains a single item with ProcedureCodesData(1040-000)" in {
        val declaration = aDeclaration(withItem(condVerified))
        assert(isConditionForAllProcedureCodesVerified(declaration))
      }

      "a declaration contains all items with ProcedureCodesData(1040-000)" in {
        val declaration = aDeclaration(withItem(condVerified), withItem(condVerified), withItem(condVerified))
        assert(isConditionForAllProcedureCodesVerified(declaration))
      }
    }

    "return false" when {

      val condNotVerified = anItem(withProcedureCodes(Some("3171"), Seq("1CS")))

      "a declaration does not contain any item with ProcedureCodesData(1040-000)" in {
        val declaration = aDeclaration(withItem(condNotVerified))
        isConditionForAllProcedureCodesVerified(declaration) mustBe false
      }

      "a declaration contains at least one items without ProcedureCodesData(1040-000)" in {
        val declaration = aDeclaration(withItem(condVerified), withItem(condNotVerified), withItem(condVerified))
        isConditionForAllProcedureCodesVerified(declaration) mustBe false
      }

      "a declaration does not contain items" in {
        val declaration = aDeclaration()
        isConditionForAllProcedureCodesVerified(declaration) mustBe false
      }
    }
  }
}
