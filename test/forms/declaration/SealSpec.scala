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

import base.CustomExportsBaseSpec
import forms.FormMatchers
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.MustMatchers
import org.scalatest.prop.PropertyChecks
import play.api.data.Form

class SealSpec extends CustomExportsBaseSpec with MustMatchers with PropertyChecks with Generators with FormMatchers {

  "SealMapping" should {

    "bind" when {

      "valid values are bound" in {

        forAll { seal: Seal =>
          Form(Seal.formMapping)
            .fillAndValidate(seal)
            .fold(_ => fail("form should not fail"), success => success mustBe seal)
        }
      }
    }

    "fail" when {

      "seal longer than 20 characters is supplied" in {

        forAll(arbitrary[Seal], stringsLongerThan(21)) { (seal, id) =>
          val data = seal.copy(id = id)
          Form(Seal.formMapping)
            .fillAndValidate(data)
            .fold(
              _ must haveErrorMessage("Seal identification number must be 20 characters or less"),
              _ => fail("should not succeed")
            )

        }
      }
      "id not supplied" in {
        Form(Seal.formMapping)
          .bind(Map[String, String]("id" -> ""))
          .fold(_ must haveErrorMessage("Please provide seal Id"), _ => fail("should not succeed"))
      }
    }
  }
}
