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

import play.api.data.FormError
import unit.base.UnitSpec

class RoutingQuestionYesNoSpec extends UnitSpec {

  "Routing Country model" should {

    "should have defined two options for form" in {

      RoutingQuestionYesNo.yes mustBe "Yes"
      RoutingQuestionYesNo.no mustBe "No"
    }

    "contains correct list of allowedValues" in {

      RoutingQuestionYesNo.allowedValues mustBe Seq(RoutingQuestionYesNo.yes, RoutingQuestionYesNo.no)
    }

    "contains errors after form validation" when {

      "input is empty for add" in {

        val incorrectForm = Map("answer" -> "")

        val result = RoutingQuestionYesNo.formAdd().bind(incorrectForm)

        result.errors.length mustBe 1
        result.errors.head mustBe FormError("answer", "declaration.routingQuestion.add.empty")
      }

      "input is empty for remove" in {

        val incorrectForm = Map("answer" -> "")

        val result = RoutingQuestionYesNo.formRemove().bind(incorrectForm)

        result.errors.length mustBe 1
        result.errors.head mustBe FormError("answer", "declaration.routingQuestion.remove.empty")
      }

      "input is empty for first" in {

        val incorrectForm = Map("answer" -> "")

        val result = RoutingQuestionYesNo.formFirst().bind(incorrectForm)

        result.errors.length mustBe 1
        result.errors.head mustBe FormError("answer", "declaration.routingQuestion.empty")
      }

      "input is incorrect" in {

        val incorrectForm = Map("answer" -> "incorrect")

        val result = RoutingQuestionYesNo.formFirst().bind(incorrectForm)

        result.errors.length mustBe 1
        result.errors.head mustBe FormError("answer", "declaration.routingQuestion.error")
      }
    }

    "doesn't have any errors when value is correct" in {

      val correctForm = Map("answer" -> "Yes")

      val result = RoutingQuestionYesNo.formRemove().bind(correctForm)

      result.errors mustBe empty
    }
  }
}
