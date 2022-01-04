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

package forms.declaration.additionaldeclarationtype

import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import base.UnitSpec

class AdditionalDeclarationTypeClearanceDecSpec extends UnitSpec {

  private val validTypes = Seq(AdditionalDeclarationType.CLEARANCE_PRE_LODGED, AdditionalDeclarationType.CLEARANCE_FRONTIER)

  "AdditionalDeclarationType mapping user for binding data" should {

    "return form with errors" when {
      "provided with empty input" in {
        val form = AdditionalDeclarationTypeClearanceDec.form().bind(Map[String, String]())

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.declarationType.inputText.error.empty")
      }

      "provided with a value not defined in AllowedAdditionalDeclarationTypes" in {
        val form = AdditionalDeclarationTypeClearanceDec.form().bind(Map[String, String]("additionalDeclarationType" -> "#"))

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.declarationType.inputText.error.incorrect")
      }

      for (additionalType: AdditionalDeclarationType <- AdditionalDeclarationType.values.filterNot(validTypes.contains)) {
        s"provided with an invalid AdditionalDeclarationType $additionalType" in {
          val form = AdditionalDeclarationTypeClearanceDec.form().bind(Map[String, String]("additionalDeclarationType" -> additionalType.toString))

          form.hasErrors must be(true)
          form.errors.length must equal(1)
          form.errors.head.message must equal("declaration.declarationType.inputText.error.incorrect")
        }
      }
    }

    "return form without errors" when {
      for (additionalType: AdditionalDeclarationType <- validTypes) {
        s"provided with valid input $additionalType" in {
          val form = AdditionalDeclarationTypeClearanceDec
            .form()
            .bind(Map[String, String]("additionalDeclarationType" -> additionalType.toString))

          form.hasErrors must be(false)
        }
      }
    }
  }

}
