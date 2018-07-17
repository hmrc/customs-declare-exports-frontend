/*
 * Copyright 2018 HM Revenue & Customs
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

package forms

import org.scalatest.{Matchers, WordSpec}

class OwnDescriptionFormProviderSpec extends WordSpec with Matchers {

  val form = new OwnDescriptionFormProvider()()

  "OwnDescriptorFormProvider" should {
    "bind valid data" in {
      val result = form.bind(Map("choice" -> "Yes", "description" -> "Own Description"))

      result.apply("choice").value map { choice =>
        choice shouldBe "Yes"
      }

      result.apply("description").value map { description =>
        description shouldBe "Own Description"
      }
    }
  }
}