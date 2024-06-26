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

import forms.common.DeclarationPageBaseSpec
import forms.section2.PersonPresentingGoodsDetails

class PersonPresentingGoodsDetailsSpec extends DeclarationPageBaseSpec {

  "PersonPresentingGoodsDetails form" when {
    "used for binding data" should {

      "return form without errors" when {
        "provided with correct EORI" in {

          val inputData = Map("eori" -> "GB123456789000")

          val form = PersonPresentingGoodsDetails.form.bind(inputData)

          form.hasErrors mustBe false
        }
      }

      "return form with errors" when {

        "provided with empty form" in {

          val inputData = Map.empty[String, String]

          val form = PersonPresentingGoodsDetails.form.bind(inputData)

          form.hasErrors mustBe true
          form.errors.length mustBe 1
          form.errors.head.message mustBe "error.required"
        }

        "provided with empty EORI" in {

          val inputData = Map("eori" -> "")

          val form = PersonPresentingGoodsDetails.form.bind(inputData)

          form.hasErrors mustBe true
          form.errors.length mustBe 1
          form.errors.head.message mustBe "declaration.eori.empty"
        }

        "provided with incorrect EORI" in {

          val inputData = Map("eori" -> "GB123!@#$%^")

          val form = PersonPresentingGoodsDetails.form.bind(inputData)

          form.hasErrors mustBe true
          form.errors.length mustBe 1
          form.errors.head.message mustBe "declaration.eori.error.format"
        }
      }
    }
  }

  "PersonPresentingGoodsDetails" when {
    testTariffContentKeysNoSpecialisation(PersonPresentingGoodsDetails, "tariff.declaration.personPresentingGoods", getClearanceTariffKeys)
  }
}
