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

import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.DeclarantEoirConfirmation.isEoriKey
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class DeclarantEoriConfirmationSpec extends WordSpec with MustMatchers {

  import DeclarantEoriConfirmationSpec._

  "Declarant EORI confirmation mapping used for binding data" should {

    "return form with errors" when {

      "provided with empty input" in {

        val form = DeclarantEoirConfirmation.form().bind(emptyJSON)

        form.hasErrors mustBe true
        form.errors.length must equal(1)
        form.errors.head.message must equal("error.yesNo.required")
      }

      "provided with invalid input" in {

        val form = DeclarantEoirConfirmation.form().bind(invalidJSON)

        form.hasErrors mustBe true
        form.errors.length must equal(1)
        form.errors.head.message must equal("error.yesNo.required")
      }

      "provided with valid input" in {

        val form = DeclarantEoirConfirmation.form().bind(validJSON)

        form.hasErrors mustBe false
      }
    }
  }

}

object DeclarantEoriConfirmationSpec {

  val emptyJSON: JsValue = JsObject(Map(isEoriKey -> JsString("")))
  val invalidJSON: JsValue = JsObject(Map(isEoriKey -> JsString("invalid")))
  val validJSON: JsValue = JsObject(Map(isEoriKey -> JsString(YesNoAnswers.yes)))
}
