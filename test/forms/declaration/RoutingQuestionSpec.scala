/*
 * Copyright 2019 HM Revenue & Customs
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

import forms.declaration.RoutingQuestion
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString}
import unit.base.UnitSpec

class RoutingQuestionSpec extends UnitSpec {

  "Routing Country model" should {

    "return correct value for toBoolean method" in {

      RoutingQuestion(RoutingQuestion.yes).toBoolean mustBe true
      RoutingQuestion(RoutingQuestion.no).toBoolean mustBe false
    }

    "should have defined two options for form" in {

      RoutingQuestion.yes mustBe "Yes"
      RoutingQuestion.no mustBe "No"
    }

    "return correct value form answerFromBoolean method" in {

      RoutingQuestion.answerFromBoolean(true) mustBe RoutingQuestion(RoutingQuestion.yes)
      RoutingQuestion.answerFromBoolean(false) mustBe RoutingQuestion(RoutingQuestion.no)
    }

    "return correct value from answerToBoolean method" in {

      RoutingQuestion.answerToBoolean(RoutingQuestion(RoutingQuestion.yes)) mustBe true
      RoutingQuestion.answerToBoolean(RoutingQuestion(RoutingQuestion.no)) mustBe false
    }

    "contains correct list of allowedValues" in {

      RoutingQuestion.allowedValues mustBe Seq(RoutingQuestion.yes, RoutingQuestion.no)
    }

    "contains errors after form validation" when {

      "input is empty" in {

        val incorrectForm = Map("hasRoutingCountries" -> "")

        val result = RoutingQuestion.form().bind(incorrectForm)

        result.errors.length mustBe 1
        result.errors.head mustBe FormError("hasRoutingCountries", "declaration.routingQuestion.empty")
      }

      "input is incorrect" in {

        val incorrectForm = Map("hasRoutingCountries" -> "incorrect")

        val result = RoutingQuestion.form().bind(incorrectForm)

        result.errors.length mustBe 1
        result.errors.head mustBe FormError("hasRoutingCountries", "declaration.routingQuestion.error")
      }
    }

    "doesn't have any errors when value is correct" in {

      val correctForm = Map("hasRoutingCountries" -> "Yes")

      val result = RoutingQuestion.form().bind(correctForm)

      result.errors mustBe empty
    }
  }
}
