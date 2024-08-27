/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.section3

import base.UnitWithMocksSpec
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import play.api.data.FormError

class RoutingCountryQuestionYesNoSpec extends UnitWithMocksSpec {

  "Routing Country model" should {

    "contains correct list of allowedValues" in {

      YesNoAnswer.allowedValues mustBe Seq(YesNoAnswers.yes, YesNoAnswers.no)
    }

    "contains errors after form validation" when {

      "input is empty for add" in {

        val incorrectForm = Map("answer" -> "")

        val result = RoutingCountryQuestionYesNo.formAdd().bind(incorrectForm)

        result.errors.length mustBe 1
        result.errors.head mustBe FormError("answer", "declaration.routingCountryQuestion.add.empty")
      }

      "input is empty for remove" in {

        val incorrectForm = Map("answer" -> "")

        val result = RoutingCountryQuestionYesNo.formRemove().bind(incorrectForm)

        result.errors.length mustBe 1
        result.errors.head mustBe FormError("answer", "declaration.routingCountryQuestion.remove.empty")
      }

      "input is empty for first" in {

        val incorrectForm = Map("answer" -> "")

        val result = RoutingCountryQuestionYesNo.formFirst().bind(incorrectForm)

        result.errors.length mustBe 1
        result.errors.head mustBe FormError("answer", "declaration.routingCountryQuestion.empty")
      }

      "input is incorrect" in {

        val incorrectForm = Map("answer" -> "incorrect")

        val result = RoutingCountryQuestionYesNo.formFirst().bind(incorrectForm)

        result.errors.length mustBe 1
        result.errors.head mustBe FormError("answer", "declaration.routingCountryQuestion.empty")
      }
    }

    "doesn't have any errors when value is correct" in {

      val correctForm = Map("answer" -> "Yes")

      val result = RoutingCountryQuestionYesNo.formRemove().bind(correctForm)

      result.errors mustBe empty
    }
  }
}
