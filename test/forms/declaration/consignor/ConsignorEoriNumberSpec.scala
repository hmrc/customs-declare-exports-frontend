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

package forms.declaration.consignor

import base.FormSpec
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.{DeclarationPageBaseSpec, Eori}
import forms.section2.consignor.ConsignorEoriNumber.form
import forms.section2.consignor.ConsignorEoriNumber
import play.api.data.FormError

class ConsignorEoriNumberSpec extends FormSpec with DeclarationPageBaseSpec {

  "Consignor Eori Number form" should {

    "has no errors" when {

      "the answer is 'No'" in {

        val correctModel = ConsignorEoriNumber(None, YesNoAnswers.no)

        val result = form.fillAndValidate(correctModel)

        result.errors mustBe empty
      }

      "the answer is 'Yes' and EORI is provided" in {

        val correctModel = ConsignorEoriNumber(Some(Eori("GB1234567890123")), YesNoAnswers.yes)

        val result = form.fillAndValidate(correctModel)

        result.errors mustBe empty
      }
    }

    "has errors" when {

      "the answer is 'Yes' and EORI is not provided" in {

        val incorrectData = Map("eori" -> "", "hasEori" -> "Yes")

        val result = form.bind(incorrectData)

        result.errors mustBe Seq(FormError("eori", "declaration.consignorEori.eori.empty"))
      }

      "there is no answer on the question" in {

        val incorrectData = Map.empty[String, String]

        val result = form.bind(incorrectData)

        result.errors mustBe Seq(FormError("hasEori", "declaration.consignorEori.hasEori.empty"))
      }
    }
  }

  "ConsignorEoriNumber" when {
    testTariffContentKeysNoSpecialisation(ConsignorEoriNumber, "tariff.declaration.consignorEoriNumber", getClearanceTariffKeys)
  }
}
